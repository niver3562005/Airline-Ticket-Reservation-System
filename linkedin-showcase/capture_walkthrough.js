const fs = require('fs');
const path = require('path');

const projectRoot = process.argv[2] || process.cwd();
const outputDir = path.join(projectRoot, 'linkedin-showcase', 'extended-captures');
const baseUrl = 'http://localhost:8081';
const debugPort = 9333;
fs.mkdirSync(outputDir, { recursive: true });

const delay = ms => new Promise(resolve => setTimeout(resolve, ms));

async function json(url) {
  const response = await fetch(url);
  if (!response.ok) throw new Error(`HTTP ${response.status} for ${url}`);
  return response.json();
}

class CdpClient {
  constructor(url) {
    this.nextId = 1;
    this.pending = new Map();
    this.socket = new WebSocket(url);
  }

  async connect() {
    await new Promise((resolve, reject) => {
      this.socket.addEventListener('open', resolve, { once: true });
      this.socket.addEventListener('error', reject, { once: true });
    });
    this.socket.addEventListener('message', event => {
      const message = JSON.parse(event.data);
      if (!message.id || !this.pending.has(message.id)) return;
      const { resolve, reject } = this.pending.get(message.id);
      this.pending.delete(message.id);
      if (message.error) reject(new Error(message.error.message));
      else resolve(message.result);
    });
  }

  call(method, params = {}) {
    const id = this.nextId++;
    return new Promise((resolve, reject) => {
      this.pending.set(id, { resolve, reject });
      this.socket.send(JSON.stringify({ id, method, params }));
    });
  }

  close() { this.socket.close(); }
}

let cdp;
const scenes = [];

async function evaluate(expression) {
  const result = await cdp.call('Runtime.evaluate', {
    expression,
    awaitPromise: true,
    returnByValue: true,
    userGesture: true
  });
  if (result.exceptionDetails) throw new Error(result.exceptionDetails.text || 'Browser evaluation failed');
  return result.result.value;
}

async function waitForPage() {
  await delay(1250);
  await evaluate(`Promise.race([
    Promise.all(Array.from(document.images).filter(image => !image.loading || image.getBoundingClientRect().top < innerHeight * 1.5).map(image => image.complete ? true : new Promise(resolve => { image.onload = image.onerror = resolve; }))),
    new Promise(resolve => setTimeout(resolve, 1800))
  ])`);
  await delay(250);
}

async function navigate(route) {
  const url = route.startsWith('http') ? route : baseUrl + route;
  await cdp.call('Page.navigate', { url });
  await waitForPage();
}

async function field(selector, value) {
  return evaluate(`(() => {
    const element = document.querySelector(${JSON.stringify(selector)});
    if (!element) throw new Error('Missing field: ' + ${JSON.stringify(selector)});
    const setter = Object.getOwnPropertyDescriptor(element instanceof HTMLSelectElement ? HTMLSelectElement.prototype : HTMLInputElement.prototype, 'value')?.set;
    if (setter) setter.call(element, ${JSON.stringify(value)}); else element.value = ${JSON.stringify(value)};
    element.dispatchEvent(new Event('input', { bubbles: true }));
    element.dispatchEvent(new Event('change', { bubbles: true }));
    return element.value;
  })()`);
}

async function textarea(selector, value) {
  return evaluate(`(() => {
    const element = document.querySelector(${JSON.stringify(selector)});
    if (!element) throw new Error('Missing textarea: ' + ${JSON.stringify(selector)});
    const setter = Object.getOwnPropertyDescriptor(HTMLTextAreaElement.prototype, 'value').set;
    setter.call(element, ${JSON.stringify(value)});
    element.dispatchEvent(new Event('input', { bubbles: true }));
    element.dispatchEvent(new Event('change', { bubbles: true }));
    return element.value;
  })()`);
}

async function checked(selector, value = true) {
  return evaluate(`(() => {
    const element = document.querySelector(${JSON.stringify(selector)});
    if (!element) throw new Error('Missing checkbox: ' + ${JSON.stringify(selector)});
    element.checked = ${value};
    element.dispatchEvent(new Event('input', { bubbles: true }));
    element.dispatchEvent(new Event('change', { bubbles: true }));
    return element.checked;
  })()`);
}

async function submit(selector) {
  await evaluate(`(() => {
    const form = document.querySelector(${JSON.stringify(selector)});
    if (!form) throw new Error('Missing form: ' + ${JSON.stringify(selector)});
    form.removeAttribute('data-confirm');
    setTimeout(() => form.requestSubmit(), 0);
    return true;
  })()`);
  await waitForPage();
}

async function scrollTo(selector) {
  const position = await evaluate(`(() => {
    const element = document.querySelector(${JSON.stringify(selector)});
    if (!element) return null;
    window.scrollTo({ top: Math.max(0, element.getBoundingClientRect().top + window.scrollY - 118), behavior: 'instant' });
    return window.scrollY;
  })()`);
  if (position === null) await top();
  await delay(400);
}

async function top() {
  await evaluate(`window.scrollTo({ top: 0, behavior: 'instant' }); document.activeElement?.blur(); true`);
  await delay(300);
}

async function capture(file, title, subtitle, selector = null) {
  if (selector) await scrollTo(selector); else await top();
  await evaluate(`document.activeElement?.blur(); true`);
  const result = await cdp.call('Page.captureScreenshot', {
    format: 'png',
    fromSurface: true,
    captureBeyondViewport: false
  });
  const fileName = `${String(scenes.length + 1).padStart(2, '0')}-${file}.png`;
  fs.writeFileSync(path.join(outputDir, fileName), Buffer.from(result.data, 'base64'));
  scenes.push({ file: fileName, title, subtitle });
  process.stdout.write(`Captured ${fileName}: ${title}\n`);
}

async function resumeAfterCancellation(pnr, email) {
  const existing = fs.readdirSync(outputDir).filter(file => /^\d{2}-.*\.png$/.test(file)).sort();
  existing.forEach(file => scenes.push({ file }));
  await cdp.call('Network.clearBrowserCookies');
  await navigate('/login');
  await field('input[name="username"]', email);
  await field('input[name="password"]', 'Showcase@123');
  await submit('form[action="/login"]');
  await navigate('/manage');
  await field('input[name="pnr"]', pnr);
  await field('input[name="email"]', email);
  await submit('form.lookup-card');
  const cancellationStatus = await evaluate(`(async () => {
    const token = document.querySelector('form.refund-quote-panel input[name="_csrf"]')?.value || document.querySelector('input[name="_csrf"]')?.value;
    const response = await fetch('/manage/cancel', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({ _csrf: token, pnr: ${JSON.stringify(pnr)}, email: ${JSON.stringify(email)}, reason: 'Travel plans changed' }),
      redirect: 'follow'
    });
    return response.status;
  })()`);
  if (cancellationStatus >= 400) throw new Error(`Cancellation returned HTTP ${cancellationStatus}`);
  await navigate('/manage');
  await capture('booking-cancelled', 'Booking cancelled safely', 'A tracked cash-refund request is opened automatically');

  await navigate('/account');
  await capture('cancelled-in-account', 'Account history remains complete', 'Cancelled journeys and connected services stay visible');
  await navigate('/account/notifications');
  await capture('refund-notifications', 'Cancellation and refund alerts', 'Passenger communications are recorded across the journey timeline');

  await cdp.call('Network.clearBrowserCookies');
  await navigate('/login');
  await field('input[name="username"]', 'admin');
  await field('input[name="password"]', 'aeronest123');
  await capture('admin-sign-in', 'Secure staff access', 'Role-based access separates passenger and airline operations');
  await submit('form[action="/login"]');
  await capture('operations-dashboard', 'Airline operations dashboard', 'Flights, bookings, check-in, revenue and activity KPIs');

  await navigate('/admin/flights');
  await capture('flight-operations', 'Flight schedule management', 'Operations teams control aircraft, gates, fares and statuses');
  await navigate('/admin/flights/5');
  await capture('flight-control', 'Live flight control', 'Manifest, seat map, telemetry, delay and operational controls');
  await capture('telemetry-control', 'Telemetry and flight progress', 'Latitude, longitude, altitude, speed and arrival estimates', '.telemetry-admin');

  await navigate(`/admin/bookings/${pnr}`);
  await capture('booking-operations', 'Operational booking detail', 'Payment, travellers, seats and readiness for the complete PNR');
  await capture('manifest-readiness', 'Passenger manifest and security readiness', 'Check-in, documents, security and boarding controls', 'table');

  await navigate('/admin/service-center');
  await capture('service-center', 'Passenger service centre', 'Refunds, feedback, disruptions and delivery monitoring');
  await capture('connected-services-ops', 'Connected-service operations', 'Hotel, cab, agency, café and SIM requests in one queue', '.service-center-grid section:nth-of-type(2)');
  await capture('priority-ops', 'Priority airport operations', 'Fast-track and baggage requests are tracked for fulfilment', '.service-center-grid section:nth-of-type(3)');
  await capture('refund-operations', 'Refund operations', 'Cash returns, fees, processor references and completion status', '.service-center-grid section:nth-of-type(4)');
  await capture('feedback-operations', 'Feedback and service recovery', 'Passenger cases, ratings and auditable staff responses', '.service-center-grid section:nth-of-type(5)');

  await navigate('/admin/airports');
  await capture('airport-management', 'Airport management', 'Validated IATA data and global operational locations');
  await navigate('/admin/aircraft');
  await capture('fleet-management', 'Fleet and seat-layout management', 'Aircraft capacity and cabin configuration controls');

  fs.writeFileSync(path.join(outputDir, 'resume-story.json'), JSON.stringify({ generatedAt: new Date().toISOString(), email, pnr, scenes }, null, 2));
  process.stdout.write(`Completed resumed walkthrough with ${scenes.length} total scenes for ${pnr}\n`);
}

async function main() {
  const targets = await json(`http://127.0.0.1:${debugPort}/json/list`);
  const page = targets.find(target => target.type === 'page' && target.url.startsWith(baseUrl));
  if (!page) throw new Error('AeroVista browser page not found');
  cdp = new CdpClient(page.webSocketDebuggerUrl);
  await cdp.connect();
  await cdp.call('Page.enable');
  await cdp.call('Runtime.enable');
  await cdp.call('Network.enable');
  await cdp.call('Emulation.setDeviceMetricsOverride', { width: 1920, height: 1080, deviceScaleFactor: 1, mobile: false });

  const resumePnr = process.argv[3];
  const resumeEmail = process.argv[4];
  if (resumePnr && resumeEmail) {
    await resumeAfterCancellation(resumePnr, resumeEmail);
    cdp.close();
    return;
  }
  await cdp.call('Network.clearBrowserCookies');

  const marker = Date.now().toString().slice(-8);
  const email = `showcase.${marker}@aerovista.test`;
  const password = 'Showcase@123';

  await navigate('/');
  await evaluate(`localStorage.setItem('aerovistaCookieConsent', JSON.stringify({preferences:true,analytics:true,marketing:true,necessary:true,savedAt:new Date().toISOString()})); location.reload(); true`);
  await waitForPage();
  await capture('home', 'AeroVista', 'A complete premium journey—from inspiration to arrival');

  await navigate('/register');
  await field('input[name="firstName"]', 'Nivetha');
  await field('input[name="lastName"]', 'Ravindran');
  await field('input[name="email"]', email);
  await field('input[name="phone"]', '+94 77 555 0148');
  await field('input[name="password"]', password);
  await field('input[name="confirmPassword"]', password);
  await checked('input[name="marketingOptIn"]');
  await checked('input[name="termsAccepted"]');
  await capture('create-account', 'Create a passenger account', 'Validated registration, secure passwords and travel preferences');
  await submit('form[action="/register"]');
  await capture('verify-email', 'Secure email verification', 'Single-use account activation keeps passenger identities protected');
  const verifyPath = await evaluate(`document.querySelector('a[href*="/verify-email"]')?.getAttribute('href')`);
  if (!verifyPath) throw new Error('Verification link was not created');
  await navigate(verifyPath);

  await field('input[name="username"]', email);
  await field('input[name="password"]', password);
  await checked('input[name="remember-me"]', true).catch(() => false);
  await capture('sign-in', 'Passenger sign-in', 'Secure access to bookings, preferences and journey alerts');
  await submit('form[action="/login"]');
  await capture('account-welcome', 'Personal journey dashboard', 'Loyalty identity, live tools, bookings and recent alerts');

  await navigate('/account/profile');
  await capture('manage-profile', 'Manage the passenger profile', 'Update contact details, preferences and account security');
  await field('input[name="phone"]', '+94 77 555 0199');
  await checked('input[name="marketingOptIn"]', true);
  await submit('form[action="/account/profile"]');
  await capture('profile-updated', 'Profile update confirmed', 'Validated preferences are saved and a notification is created');

  await navigate('/flight-schedule');
  await capture('flight-schedule', 'Published flight schedule', 'Flight numbers, aircraft, gates, times and operating status');

  await navigate('/flights?tripType=ONE_WAY&originId=1&destinationId=6&departureDate=2026-10-24&adults=1&children=0&infants=0&cabinClass=BUSINESS');
  await capture('flight-search', 'Live flight availability', 'Prices, remaining seats, delay alerts and baggage information');

  await navigate('/cabin-guide');
  await capture('cabin-allowances', 'Four cabin experiences', 'Economy through First with 23–50 kg baggage allowances');

  await navigate('/book/5?travellers=1&cabin=BUSINESS');
  await capture('booking-contact', 'Flight checkout', 'Account-aware contact details and a complete flight summary');
  await field('input[name="passengers[0].dateOfBirth"]', '1998-08-14');
  await field('input[name="passengers[0].nationality"]', 'Sri Lankan');
  await field('input[name="passengers[0].passportNumber"]', `N${marker.slice(1)}`);
  await field('input[name="passengers[0].frequentFlyerNumber"]', `AV-${marker}`);
  await field('select[name="passengers[0].mealPreference"]', 'Vegetarian');
  await textarea('textarea[name="passengers[0].specialAssistance"]', 'Priority baggage handling requested.');
  await evaluate(`(() => {
    const select = document.querySelector('select[name="passengers[0].seatNumber"]');
    const option = Array.from(select.options).find(item => item.dataset.cabin === 'BUSINESS');
    if (!option) throw new Error('No Business seat is available');
    select.value = option.value;
    select.dispatchEvent(new Event('change', { bubbles: true }));
    return option.value;
  })()`);
  await capture('passenger-seat', 'Passenger, meal and seat selection', 'Passport validation, Business Class seat and assistance requests', '.traveller-card');
  await checked('input[name="paymentMethod"][value="CARD"]', true);
  await field('input[name="promoCode"]', 'FLY10');
  await capture('payment-promo', 'Protected payment and promotions', 'Server-validated promo codes with no stored card details', '.payment-checkout');
  await submit('form.booking-layout');
  const pnr = await evaluate(`location.pathname.split('/').filter(Boolean).pop()`);
  if (!pnr || pnr.length !== 6) throw new Error(`Booking failed; current URL is ${await evaluate('location.href')}`);
  await capture('booking-confirmation', 'Booking confirmed', 'Ticket, flight details, payment status and a unique PNR');

  await navigate('/account');
  await capture('account-booking', 'Booking added to the account', 'Journey history and real-time booking notifications');

  await navigate('/manage');
  await field('input[name="pnr"]', pnr);
  await field('input[name="email"]', email);
  await submit('form.lookup-card');
  await capture('manage-booking', 'Manage the complete journey', 'Flight times, PNR, readiness stages and passenger controls');
  await capture('journey-readiness', 'Check-in and boarding readiness', 'Document, security and boarding states for every passenger', '.journey-progress');

  const checkInExists = await evaluate(`Boolean(document.querySelector('form[action="/check-in"]'))`);
  if (checkInExists) {
    await submit('form[action="/check-in"]');
    await capture('check-in-complete', 'Online check-in complete', 'The passenger is ready for security and boarding', '.passenger-list');
  }

  await evaluate(`(() => {
    const passenger = document.querySelector('.priority-service-form select[name="passengerId"]');
    passenger.value = Array.from(passenger.options).find(option => option.value)?.value || '';
    passenger.dispatchEvent(new Event('change', { bubbles: true }));
    const type = document.querySelector('.priority-service-form select[name="type"]');
    type.value = 'FAST_TRACK_SECURITY';
    type.dispatchEvent(new Event('change', { bubbles: true }));
    const notes = document.querySelector('.priority-service-form textarea[name="notes"]');
    notes.value = 'Priority security lane and baggage support for departure.';
    notes.dispatchEvent(new Event('input', { bubbles: true }));
    return true;
  })()`);
  await capture('priority-airport-care', 'Priority airport care', 'Fast-track security, boarding, baggage and meet-and-assist', '.priority-service');
  await submit('form.priority-service-form');
  await capture('priority-confirmed', 'Airport service request confirmed', 'A priced, trackable request is sent to the operations team', '.priority-service');

  await navigate('/travel-services?type=HOTEL');
  await capture('accommodations', 'Global accommodation marketplace', '53 distinctive hotels across 50 destinations');
  await navigate('/travel-services?partnerId=63');
  await field('input[name="guests"]', '1');
  await field('input[name="serviceAt"]', '2026-10-24T18:00');
  await field('input[name="serviceEndsAt"]', '2026-10-27T11:00');
  await field('input[name="pickupLocation"]', 'Bangkok Suvarnabhumi Airport');
  await field('input[name="dropoffLocation"]', 'Bangkok Grand Hotel');
  await field('input[name="airlinePnr"]', pnr);
  await textarea('textarea[name="notes"]', 'Late arrival, quiet room and airport pickup requested.');
  await capture('hotel-reservation', 'Flight-connected hotel reservation', 'Dates, guests, transfers, preferences and airline PNR', '.reservation-section');
  await submit('form.partner-reservation-form');
  await capture('service-confirmation', 'Accommodation request confirmed', 'The service appears in passenger care and the account dashboard', '.service-marketplace');

  await navigate('/travel-services?type=CAB');
  await capture('airport-transfers', 'Cabs and airport shuttles', 'Private transfers, shared shuttles and executive chauffeurs');
  await navigate('/travel-services?type=TRAVEL_AGENCY');
  await capture('travel-agencies', 'Verified travel agencies', 'Multi-city planning and destination support connected to the passenger');
  await navigate('/travel-services?type=CAFE');
  await capture('airport-dining', 'Airport dining services', 'Terminal cafés and dietary requests before departure');
  await navigate('/travel-services?type=SIM');
  await capture('travel-sim', 'Travel SIM and eSIM services', 'Connectivity ready from the moment the passenger lands');

  await navigate('/packages');
  await capture('packages', 'Curated flight-and-stay packages', 'City, family, business, wellness and island journeys');
  await navigate('/travel-hub?visaCode=TH');
  await capture('visa-guide', 'Visa and destination guidance', 'Advisory entry details with official authority links');
  await navigate('/flight-status?flightNumber=AN900&flightDate=2026-10-22');
  await capture('live-tracking', 'Real-time flight telemetry', 'Position, speed, altitude, ETA and cabin availability', '#flight-status-result');
  await navigate('/airport-guide/CMB');
  await capture('airport-facilities', 'Airport facilities', 'Lounges, dining, accessibility, transport and family services', '#airport-facilities');
  await navigate('/deals');
  await capture('deals', 'Deals and promotional codes', 'Transparent offers with securely validated discounts');
  await navigate('/payment-options');
  await capture('payment-options', 'Flexible payment options', 'Card, bank transfer and pay-at-counter workflows');

  await navigate('/account/feedback');
  await checked('input[name="rating"][value="5"]', true);
  await field('select[name="category"]', 'DIGITAL');
  await field('input[name="bookingReference"]', pnr);
  await field('input[name="subject"]', 'Excellent complete journey experience');
  await textarea('textarea[name="comments"]', 'The booking, check-in, hotel and airport assistance workflow was clear and easy to manage.');
  await capture('feedback', 'Passenger feedback and service recovery', 'Rated, categorized and linked to the booking reference');
  await submit('form.feedback-form');
  await capture('feedback-recorded', 'Feedback case created', 'The care team receives an auditable service case');

  await navigate('/account/notifications');
  await capture('notifications', 'Multi-channel journey notifications', 'Booking, payment, profile, check-in and partner-service events');

  await navigate('/manage');
  await field('input[name="pnr"]', pnr);
  await field('input[name="email"]', email);
  await submit('form.lookup-card');
  await capture('refund-quote', 'Transparent cancellation quote', 'Amount paid, cancellation fee and cash return before confirmation', '.refund-quote-panel');
  const cancellationStatus = await evaluate(`(async () => {
    const token = document.querySelector('form.refund-quote-panel input[name="_csrf"]')?.value || document.querySelector('input[name="_csrf"]')?.value;
    const response = await fetch('/manage/cancel', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({ _csrf: token, pnr: ${JSON.stringify(pnr)}, email: ${JSON.stringify(email)}, reason: 'Travel plans changed' }),
      redirect: 'follow'
    });
    return response.status;
  })()`);
  if (cancellationStatus >= 400) throw new Error(`Cancellation returned HTTP ${cancellationStatus}`);
  await navigate('/manage');
  await capture('booking-cancelled', 'Booking cancelled safely', 'A tracked cash-refund request is opened automatically');

  await navigate('/account');
  await capture('cancelled-in-account', 'Account history remains complete', 'Cancelled journeys and connected services stay visible');
  await navigate('/account/notifications');
  await capture('refund-notifications', 'Cancellation and refund alerts', 'Passenger communications are recorded across the journey timeline');

  await cdp.call('Network.clearBrowserCookies');
  await navigate('/login');
  await field('input[name="username"]', 'admin');
  await field('input[name="password"]', 'aeronest123');
  await capture('admin-sign-in', 'Secure staff access', 'Role-based access separates passenger and airline operations');
  await submit('form[action="/login"]');
  await capture('operations-dashboard', 'Airline operations dashboard', 'Flights, bookings, check-in, revenue and activity KPIs');

  await navigate('/admin/flights');
  await capture('flight-operations', 'Flight schedule management', 'Operations teams control aircraft, gates, fares and statuses');
  await navigate('/admin/flights/5');
  await capture('flight-control', 'Live flight control', 'Manifest, seat map, telemetry, delay and operational controls');
  await capture('telemetry-control', 'Telemetry and flight progress', 'Latitude, longitude, altitude, speed and arrival estimates', '.telemetry-admin');

  await navigate(`/admin/bookings/${pnr}`);
  await capture('booking-operations', 'Operational booking detail', 'Payment, travellers, seats and readiness for the complete PNR');
  await capture('manifest-readiness', 'Passenger manifest and security readiness', 'Check-in, documents, security and boarding controls', 'table');

  await navigate('/admin/service-center');
  await capture('service-center', 'Passenger service centre', 'Refunds, feedback, disruptions and delivery monitoring');
  await capture('connected-services-ops', 'Connected-service operations', 'Hotel, cab, agency, café and SIM requests in one queue', '.service-center-grid section:nth-of-type(2)');
  await capture('priority-ops', 'Priority airport operations', 'Fast-track and baggage requests are tracked for fulfilment', '.service-center-grid section:nth-of-type(3)');
  await capture('refund-operations', 'Refund operations', 'Cash returns, fees, processor references and completion status', '.service-center-grid section:nth-of-type(4)');
  await capture('feedback-operations', 'Feedback and service recovery', 'Passenger cases, ratings and auditable staff responses', '.service-center-grid section:nth-of-type(5)');

  await navigate('/admin/airports');
  await capture('airport-management', 'Airport management', 'Validated IATA data and global operational locations');
  await navigate('/admin/aircraft');
  await capture('fleet-management', 'Fleet and seat-layout management', 'Aircraft capacity and cabin configuration controls');

  const story = {
    generatedAt: new Date().toISOString(),
    email,
    pnr,
    scenes
  };
  fs.writeFileSync(path.join(outputDir, 'story.json'), JSON.stringify(story, null, 2));
  process.stdout.write(`Completed ${scenes.length} scenes for ${pnr}\n`);
  cdp.close();
}

main().catch(error => {
  console.error(error.stack || error.message);
  if (cdp) cdp.close();
  process.exit(1);
});
