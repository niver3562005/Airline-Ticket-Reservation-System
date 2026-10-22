document.addEventListener('DOMContentLoaded', () => {
  const nav = document.querySelector('[data-public-nav]');
  if (nav && document.body.classList.contains('home-page')) {
    const updateNav = () => nav.classList.toggle('is-scrolled', window.scrollY > 40);
    window.addEventListener('scroll', updateNav, { passive: true });
    updateNav();
  }

  const navToggle = document.querySelector('[data-nav-toggle]');
  const navMenu = document.querySelector('[data-nav-menu]');
  navToggle?.addEventListener('click', () => {
    const open = navMenu.classList.toggle('open');
    navToggle.setAttribute('aria-expanded', String(open));
  });

  const navDropdowns = document.querySelectorAll('.nav-dropdown');
  navDropdowns.forEach(dropdown => dropdown.addEventListener('toggle', () => {
    if (!dropdown.open) return;
    navDropdowns.forEach(other => { if (other !== dropdown) other.open = false; });
  }));
  document.addEventListener('click', event => {
    if (event.target.closest('.nav-dropdown')) return;
    navDropdowns.forEach(dropdown => { dropdown.open = false; });
  });

  document.querySelectorAll('form[data-confirm]').forEach(form => {
    form.addEventListener('submit', event => {
      if (!window.confirm(form.dataset.confirm)) event.preventDefault();
    });
  });

  const search = document.querySelector('[data-table-search]');
  if (search) {
    const rows = document.querySelectorAll('[data-filter-table] tbody tr');
    search.addEventListener('input', () => {
      const query = search.value.toLowerCase().trim();
      rows.forEach(row => row.hidden = !row.textContent.toLowerCase().includes(query));
    });
  }

  document.querySelectorAll('.traveller-card').forEach(card => {
    const cabin = card.querySelector('.cabin-select');
    const seat = card.querySelector('.seat-select');
    if (!cabin || !seat) return;
    const filterSeats = () => {
      Array.from(seat.options).forEach(option => {
        if (!option.dataset.cabin) return;
        option.hidden = option.dataset.cabin !== cabin.value;
        option.disabled = option.hidden;
      });
      if (seat.selectedOptions[0]?.hidden) seat.value = '';
    };
    cabin.addEventListener('change', filterSeats);
    filterSeats();
  });

  document.querySelectorAll('[data-password-toggle]').forEach(button => {
    button.addEventListener('click', () => {
      const input = button.parentElement.querySelector('input');
      const revealing = input.type === 'password';
      input.type = revealing ? 'text' : 'password';
      button.setAttribute('aria-label', revealing ? 'Hide password' : 'Show password');
    });
  });

  document.querySelectorAll('form[data-loading-form]').forEach(form => {
    form.addEventListener('submit', () => {
      const button = form.querySelector('button[type="submit"]');
      if (button && form.checkValidity()) button.classList.add('is-loading');
    });
  });

  const origin = document.querySelector('[data-origin]');
  const destination = document.querySelector('[data-destination]');
  document.querySelector('[data-swap-route]')?.addEventListener('click', () => {
    const current = origin.value; origin.value = destination.value; destination.value = current;
  });

  const tripTypes = document.querySelectorAll('input[name="tripType"]');
  const returnDate = document.querySelector('[data-return-date]');
  const multiCityNote = document.querySelector('[data-multi-city-note]');
  const updateTripType = () => {
    const selected = document.querySelector('input[name="tripType"]:checked')?.value;
    returnDate?.classList.toggle('visible', selected === 'ROUND_TRIP');
    const returnInput = returnDate?.querySelector('input');
    if (returnInput) returnInput.required = selected === 'ROUND_TRIP';
    if (multiCityNote) multiCityNote.hidden = selected !== 'MULTI_CITY';
  };
  tripTypes.forEach(input => input.addEventListener('change', updateTripType));
  updateTripType();

  const flightResults = document.querySelector('[data-flight-results]');
  if (flightResults) {
    const cards = () => Array.from(flightResults.querySelectorAll('.flight-card'));
    const count = document.querySelector('[data-visible-count]');
    const empty = document.querySelector('[data-filtered-empty]');
    const applyFilters = () => {
      const period = document.querySelector('input[name="timeFilter"]:checked')?.value || 'all';
      let visible = 0;
      cards().forEach(card => {
        const hour = Number(card.dataset.hour);
        const matches = period === 'all' || (period === 'morning' && hour >= 5 && hour < 12) ||
          (period === 'afternoon' && hour >= 12 && hour < 18) || (period === 'evening' && (hour >= 18 || hour < 5));
        card.hidden = !matches;
        if (matches) visible += 1;
      });
      if (count) count.textContent = visible;
      if (empty) empty.hidden = visible !== 0;
    };
    document.querySelectorAll('input[name="timeFilter"]').forEach(input => input.addEventListener('change', applyFilters));
    document.querySelector('[data-reset-filters]')?.addEventListener('click', () => {
      const all = document.querySelector('input[name="timeFilter"][value="all"]'); if (all) all.checked = true; applyFilters();
    });
    document.querySelector('[data-flight-sort]')?.addEventListener('change', event => {
      const type = event.target.value;
      const sorted = cards().sort((a, b) => {
        if (type === 'price') return Number(a.dataset.price) - Number(b.dataset.price);
        if (type === 'departure') return Number(a.dataset.hour) - Number(b.dataset.hour);
        if (type === 'duration') return Number(a.dataset.duration) - Number(b.dataset.duration);
        return 0;
      });
      sorted.forEach(card => flightResults.appendChild(card));
    });
  }

  document.querySelectorAll('.cabin-tabs button').forEach(button => {
    button.addEventListener('click', () => {
      button.parentElement.querySelectorAll('button').forEach(item => item.classList.remove('active'));
      button.classList.add('active');
    });
  });

  document.querySelectorAll('[data-newsletter]').forEach(form => form.addEventListener('submit', event => {
    event.preventDefault();
    const message = form.querySelector('[data-newsletter-message]');
    if (message) message.textContent = 'Thank you. Your demo subscription has been recorded for this session.';
    form.reset();
  }));

  const consentKey = 'aerovistaCookieConsent';
  const cookieBanner = document.querySelector('[data-cookie-banner]');
  const cookieModal = document.querySelector('[data-cookie-modal]');
  const readConsent = () => {
    try { return JSON.parse(localStorage.getItem(consentKey)); } catch (_) { return null; }
  };
  const saveConsent = categories => {
    localStorage.setItem(consentKey, JSON.stringify({ ...categories, necessary: true, savedAt: new Date().toISOString() }));
    if (cookieBanner) cookieBanner.hidden = true;
    if (cookieModal) cookieModal.hidden = true;
    document.body.classList.remove('privacy-centre-open');
  };
  const openSettings = () => {
    const current = readConsent() || {};
    document.querySelectorAll('[data-cookie-category]').forEach(input => { input.checked = Boolean(current[input.dataset.cookieCategory]); });
    if (cookieModal) cookieModal.hidden = false;
    document.body.classList.add('privacy-centre-open');
  };
  if (cookieBanner && !readConsent()) cookieBanner.hidden = false;
  document.querySelectorAll('[data-cookie-settings],[data-cookie-customize]').forEach(button => button.addEventListener('click', openSettings));
  document.querySelector('[data-cookie-accept]')?.addEventListener('click', () => saveConsent({ preferences: true, analytics: true, marketing: true }));
  document.querySelector('[data-cookie-necessary]')?.addEventListener('click', () => saveConsent({ preferences: false, analytics: false, marketing: false }));
  document.querySelector('[data-cookie-save]')?.addEventListener('click', () => {
    const categories = {};
    document.querySelectorAll('[data-cookie-category]').forEach(input => { categories[input.dataset.cookieCategory] = input.checked; });
    saveConsent(categories);
  });
  document.querySelectorAll('[data-cookie-close]').forEach(button => button.addEventListener('click', () => {
    if (cookieModal) cookieModal.hidden = true;
    document.body.classList.remove('privacy-centre-open');
  }));

  document.querySelectorAll('[data-copy-code]').forEach(button => button.addEventListener('click', async () => {
    const code = button.dataset.code;
    try { await navigator.clipboard.writeText(code); } catch (_) { /* clipboard may be unavailable on local HTTP */ }
    button.textContent = 'COPIED · ' + code;
    window.setTimeout(() => { button.textContent = code; }, 1600);
  }));

  document.querySelectorAll('[data-fill-promo]').forEach(button => button.addEventListener('click', () => {
    const input = document.querySelector('input[name="promoCode"]');
    if (input) { input.value = button.dataset.code; input.focus(); }
  }));

  if (document.body.dataset.telemetryRefresh === 'true') {
    window.setTimeout(() => window.location.reload(), 30000);
  }
});
