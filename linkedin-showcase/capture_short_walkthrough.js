const fs = require('fs');
const path = require('path');

const projectRoot = process.argv[2] || process.cwd();
const outputDir = path.join(projectRoot, 'linkedin-showcase', 'short-captures');
const existingCaptureDir = path.join(projectRoot, 'linkedin-showcase', 'extended-captures');
const baseUrl = 'http://localhost:8081';
const debugPort = 9333;
const scenes = [];

fs.mkdirSync(outputDir, { recursive: true });

const delay = milliseconds => new Promise(resolve => setTimeout(resolve, milliseconds));

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
  await delay(900);
  await evaluate(`Promise.race([
    Promise.all(Array.from(document.images).map(image => image.complete ? true : new Promise(resolve => { image.onload = image.onerror = resolve; }))),
    new Promise(resolve => setTimeout(resolve, 1600))
  ])`);
  await delay(200);
}

async function navigate(route) {
  await cdp.call('Page.navigate', { url: route.startsWith('http') ? route : baseUrl + route });
  await waitForPage();
}

async function field(selector, value) {
  await evaluate(`(() => {
    const element = document.querySelector(${JSON.stringify(selector)});
    if (!element) throw new Error('Missing field: ' + ${JSON.stringify(selector)});
    const setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value').set;
    setter.call(element, ${JSON.stringify(value)});
    element.dispatchEvent(new Event('input', { bubbles: true }));
    element.dispatchEvent(new Event('change', { bubbles: true }));
    return true;
  })()`);
}

async function submit(selector) {
  await evaluate(`(() => {
    const form = document.querySelector(${JSON.stringify(selector)});
    if (!form) throw new Error('Missing form: ' + ${JSON.stringify(selector)});
    form.requestSubmit();
    return true;
  })()`);
  await waitForPage();
}

async function capture(file, title, subtitle) {
  await evaluate(`window.scrollTo({ top: 0, behavior: 'instant' }); document.activeElement?.blur(); true`);
  await delay(250);
  const screenshot = await cdp.call('Page.captureScreenshot', {
    format: 'png',
    fromSurface: true,
    captureBeyondViewport: false
  });
  const fileName = `${String(scenes.length + 1).padStart(2, '0')}-${file}.png`;
  fs.writeFileSync(path.join(outputDir, fileName), Buffer.from(screenshot.data, 'base64'));
  scenes.push({ file: fileName, title, subtitle });
  process.stdout.write(`Captured ${fileName}: ${title}\n`);
}

function reuseCapture(sourceFile, file, title, subtitle) {
  const fileName = `${String(scenes.length + 1).padStart(2, '0')}-${file}.png`;
  fs.copyFileSync(path.join(existingCaptureDir, sourceFile), path.join(outputDir, fileName));
  scenes.push({ file: fileName, title, subtitle });
  process.stdout.write(`Reused ${fileName}: ${title}\n`);
}

async function login(portal, username, password) {
  await navigate(`/login/${portal}`);
  await field('input[name="username"]', username);
  await field('input[name="password"]', password);
  await submit('form[action="/login"]');
}

async function main() {
  const targets = await json(`http://127.0.0.1:${debugPort}/json/list`);
  const page = targets.find(target => target.type === 'page');
  if (!page) throw new Error('Chrome debugging page not found');

  cdp = new CdpClient(page.webSocketDebuggerUrl);
  await cdp.connect();
  await cdp.call('Page.enable');
  await cdp.call('Runtime.enable');
  await cdp.call('Network.enable');
  await cdp.call('Emulation.setDeviceMetricsOverride', {
    width: 1920,
    height: 1080,
    deviceScaleFactor: 1,
    mobile: false
  });
  await cdp.call('Network.clearBrowserCookies');

  await navigate('/');
  await evaluate(`localStorage.setItem('aerovistaCookieConsent', JSON.stringify({necessary:true,preferences:true,analytics:true,marketing:false})); location.reload(); true`);
  await waitForPage();
  await capture('home', 'AeroVista', 'A premium journey from search to arrival');

  await navigate('/login');
  await capture('workspace-selector', 'One platform. Three workspaces.', 'Dedicated access for customers, airline staff and administrators');

  await navigate('/login/customer');
  await capture('customer-login', 'Secure customer access', 'Bookings, profiles, alerts and refunds in one account');
  await field('input[name="username"]', 'traveller@aerovista.test');
  await field('input[name="password"]', 'Travel@123');
  await submit('form[action="/login"]');
  await capture('customer-dashboard', 'The passenger journey hub', 'Manage bookings, check-in, services, payments and feedback');

  await navigate('/flight-schedule?route=AN880');
  await capture('long-term-schedule', 'Year-round flight schedules', 'Searchable future dates, rotating times, fares and baggage packages');
  const bookingPath = await evaluate(`document.querySelector('a[href^="/book/"]')?.getAttribute('href')`);
  if (!bookingPath) throw new Error('No bookable long-term flight was found');
  await navigate(bookingPath);
  await capture('flight-checkout', 'Complete flight checkout', 'Cabin, passenger, seat, promotion and payment choices');

  reuseCapture('14-booking-confirmation.png', 'booking-confirmation', 'Booking confirmed', 'A complete itinerary, payment status and passenger PNR');
  reuseCapture('16-manage-booking.png', 'manage-booking', 'Manage the journey', 'Check-in, readiness, connected services and cancellation controls');
  reuseCapture('37-refund-quote.png', 'refund-quote', 'Transparent cancellations', 'Passengers see fees and expected cash returns before confirming');

  await navigate('/travel-services?type=HOTEL');
  await capture('travel-services', 'Travel beyond the flight', 'Hotels, transfers, agencies, cafés and eSIM services');
  await navigate(`/flight-status?flightNumber=AN900&flightDate=${new Date().toISOString().slice(0, 10)}`);
  await capture('live-flight', 'Live flight intelligence', 'Status, position, altitude, speed and estimated arrival');

  await cdp.call('Network.clearBrowserCookies');
  await navigate('/login/staff');
  await capture('staff-login', 'Airline staff access', 'Daily operations and passenger care stay separate and secure');
  await field('input[name="username"]', 'agent');
  await field('input[name="password"]', 'agent123');
  await submit('form[action="/login"]');
  await capture('staff-dashboard', 'Staff operations workspace', 'Flights, manifests, boarding, disruptions and service recovery');
  await navigate('/operations/flights');
  await capture('flight-operations', 'Long-term schedule control', 'Aircraft, gates, fares, packages and flight status management');
  await navigate('/operations/service-center');
  await capture('service-center', 'Passenger service centre', 'Refunds, feedback, priority services and partner requests');

  await cdp.call('Network.clearBrowserCookies');
  await navigate('/login/admin');
  await capture('admin-login', 'Administrator access', 'Protected governance for commercial and operational controls');
  await field('input[name="username"]', 'admin');
  await field('input[name="password"]', 'aeronest123');
  await submit('form[action="/login"]');
  await capture('admin-dashboard', 'Administrator control centre', 'Fleet, airports, schedules, revenue and service oversight');
  await navigate('/admin/aircraft');
  await capture('fleet-management', 'Fleet and capacity management', 'Aircraft availability and cabin layouts ready for operations');

  fs.writeFileSync(path.join(outputDir, 'story.json'), JSON.stringify({
    generatedAt: new Date().toISOString(),
    scenes
  }, null, 2));
  process.stdout.write(`Completed short walkthrough with ${scenes.length} scenes\n`);
  cdp.close();
}

main().catch(error => {
  console.error(error);
  if (cdp) cdp.close();
  process.exitCode = 1;
});
