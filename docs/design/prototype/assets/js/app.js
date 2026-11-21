/* CNG491 UI prototype. All changes are local and reset on page reload. */
(() => {
  'use strict';
  const root = document.getElementById('cng491-preview');
  const $ = (selector, parent = root) => parent.querySelector(selector);
  const $$ = (selector, parent = root) => [...parent.querySelectorAll(selector)];
  const demo = structuredClone(window.CNG491_DEMO);
  const escape = value => String(value).replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
  const icon = name => `<i class="icon" data-lucide="${name}" aria-hidden="true"></i>`;
  const refreshIcons = () => window.lucide?.createIcons({ attrs: { width: 16, height: 16 } });
  const initials = name => name.split(' ').map(word => word[0]).join('');
  const time = hour => `${String(Math.floor(hour)).padStart(2, '0')}:${hour % 1 ? '30' : '00'}`;
  const dateISO = date => date.toISOString().slice(0, 10);
  const parseDate = value => new Date(`${value}T00:00:00Z`);
  const displayDate = value => new Intl.DateTimeFormat('en-GB', { dateStyle: 'full', timeZone: 'UTC' }).format(parseDate(value));
  const feedback = (element, message) => {
    element.replaceChildren();
    const div = document.createElement('div');
    div.className = 'message';
    div.textContent = message;
    element.append(div);
  };

  // Screen navigation is for reviewing the five representative SRS interfaces.
  const screenNames = ['book-appointment', 'my-appointments', 'staff-workspace', 'organization-owner', 'system-admin'];
  function showScreen(number, updateHash = true) {
    number = Math.max(1, Math.min(5, number));
    $$('section[role="tabpanel"]').forEach((panel, index) => { panel.hidden = index !== number - 1; });
    $$('[role="tab"]').forEach((tab, index) => {
      tab.setAttribute('aria-selected', String(index === number - 1));
      tab.tabIndex = index === number - 1 ? 0 : -1;
    });
    if (updateHash) history.replaceState(null, '', `#${screenNames[number - 1]}`);
  }
  $$('[role="tab"]').forEach((tab, index) => {
    tab.onclick = () => showScreen(index + 1);
    tab.onkeydown = event => {
      if (!['ArrowLeft', 'ArrowRight', 'Home', 'End'].includes(event.key)) return;
      event.preventDefault();
      const next = event.key === 'Home' ? 0 : event.key === 'End' ? 4 : (index + (event.key === 'ArrowRight' ? 1 : 4)) % 5;
      showScreen(next + 1);
      $$('[role="tab"]')[next].focus();
    };
  });
  $$('[data-open]').forEach(button => { button.onclick = () => showScreen(Number(button.dataset.open)); });
  window.addEventListener('hashchange', () => showScreen(Math.max(0, screenNames.indexOf(location.hash.slice(1))) + 1, false));

  // Shared accessible dialog.
  let saveDialog = () => {};
  let dialogTrigger;
  const overlay = $('[data-modal]');
  function closeDialog() {
    overlay.hidden = true;
    dialogTrigger?.focus();
  }
  function dialog(title, content, save = () => {}, actionLabel = 'Save changes') {
    dialogTrigger = document.activeElement;
    $('#cng491-modal-title').textContent = title;
    $('[data-modal-body]').innerHTML = content;
    $('[data-modal-save]').textContent = actionLabel;
    saveDialog = save;
    $$('[data-modal-body] .field').forEach((field, index) => {
      const control = field.querySelector('input,select,textarea');
      const label = field.querySelector('label');
      if (control && label) { control.id ||= `dialog-field-${index}`; label.htmlFor = control.id; }
    });
    overlay.hidden = false;
    refreshIcons();
    (overlay.querySelector('input:not([readonly]),select,button')).focus();
  }
  $$('[data-close-modal]').forEach(button => { button.onclick = closeDialog; });
  $('[data-modal-save]').onclick = () => { if (saveDialog() !== false) closeDialog(); };
  overlay.onkeydown = event => {
    if (event.key === 'Escape') closeDialog();
    if (event.key !== 'Tab') return;
    const nodes = [...overlay.querySelectorAll('button,input,select,textarea')].filter(node => !node.disabled && node.offsetParent);
    const first = nodes[0], last = nodes[nodes.length - 1];
    if (event.shiftKey && document.activeElement === first) { event.preventDefault(); last.focus(); }
    else if (!event.shiftKey && document.activeElement === last) { event.preventDefault(); first.focus(); }
  };

  // Role-specific sidebars; the product has no invented brand or logo.
  const navigation = {
    staff: [['Daily agenda', 'calendar-days'], ['Working hours', 'clock-3']],
    owner: [['Team schedule', 'calendar-days'], ['Team members', 'users-round'], ['Services', 'layers-2'], ['Working hours', 'clock-3'], ['Booking rules', 'sliders-horizontal']],
    admin: [['Organizations', 'building-2']]
  };
  Object.entries(navigation).forEach(([role, links]) => {
    const user = role === 'staff' ? 'Maya Chen' : role === 'owner' ? 'Sarah Mitchell' : 'Jordan Ellis';
    const label = { staff: 'Staff', owner: 'Organization Owner', admin: 'System Admin' }[role];
    $(`[data-sidebar="${role}"]`).innerHTML = `<span class="product-name">Appointments</span>
      ${role === 'admin' ? '' : `<div class="workspace"><strong>${escape(demo.organization)}</strong>Student services</div>`}
      <div class="nav-label">${role === 'admin' ? 'PLATFORM' : 'WORKSPACE'}</div>
      <nav>${links.map(([name, symbol], index) => `<button type="button" class="side-link ${index ? '' : 'active'}" data-nav="${name}" data-role="${role}">${icon(symbol)}${name}</button>`).join('')}</nav>
      <div class="side-bottom"><div class="profile cluster"><span class="avatar">${initials(user)}</span><div><strong>${user}</strong><small>${label}</small></div></div></div>`;
  });

  // Booking and rescheduling. Wait values and recommendations are demo fixtures.
  const flows = {
    book: { staff: 0, wait: 5, time: '10:00', date: demo.date },
    reschedule: { staff: 0, wait: 3, time: '09:00', date: '2026-10-28' }
  };
  function select(group, button) {
    $$('button', group).forEach(node => {
      const chosen = node === button;
      node.classList.toggle('selected', chosen);
      node.setAttribute('aria-pressed', String(chosen));
    });
  }
  function renderFlow(scope) {
    const state = flows[scope];
    $$(`[data-${scope}-wait]`).forEach(node => { node.textContent = state.wait; });
    if (scope === 'book') {
      $('[data-book-staff]').textContent = demo.staff[state.staff].name;
      $('#fs1 .summary strong').innerHTML = `${escape(displayDate(state.date).replace(', 2026', ''))} · <span data-book-time>${state.time}</span>`;
    }
  }
  $$('[data-staff]').forEach(button => {
    button.setAttribute('aria-pressed', String(button.classList.contains('selected')));
    button.onclick = () => {
      const scope = button.dataset.scope, state = flows[scope];
      state.staff = demo.staff.findIndex(staff => staff.name === button.dataset.staff);
      state.wait = scope === 'book' ? demo.staff[state.staff].wait : demo.staff[state.staff].rescheduleWait;
      $$(`[data-scope="${scope}"]`).forEach(node => {
        node.classList.toggle('selected', node === button);
        node.setAttribute('aria-pressed', String(node === button));
      });
      const slots = $(`[data-slots="${scope}"]`);
      const buttons = $$('button', slots);
      buttons[0].disabled = state.staff === 1;
      const next = buttons.find(node => !node.disabled);
      select(slots, next);
      state.time = next.textContent;
      renderFlow(scope);
    };
  });
  $$('[data-slots]').forEach(group => {
    $$('button', group).forEach(button => {
      button.setAttribute('aria-pressed', String(button.classList.contains('selected')));
      button.onclick = () => { select(group, button); flows[group.dataset.slots].time = button.textContent; renderFlow(group.dataset.slots); };
    });
  });
  $$('[data-days] button').forEach(button => {
    button.setAttribute('aria-pressed', String(button.classList.contains('selected')));
    if (!button.disabled) button.onclick = () => {
      select(button.parentElement, button);
      flows.book.date = `2026-10-${button.querySelector('strong').textContent}`;
      renderFlow('book');
    };
  });
  $$('[data-confirm]').forEach(button => {
    button.onclick = () => {
      const booking = button.dataset.confirm === 'booking';
      const flow = flows[booking ? 'book' : 'reschedule'];
      if (!booking) {
        const card = $('#fs2 [data-period-panel="upcoming"] .appointment');
        $('.appointment-body > .muted', card).textContent = `${demo.staff[flow.staff].name} · ${demo.staff[flow.staff].title}`;
        $('.date-block', card).innerHTML = `<div class="date-square">OCT<strong>28</strong></div><div><strong>${displayDate(flow.date)}</strong><p class="muted">${flow.time} · 30 minutes</p></div>`;
      }
      feedback($(`[data-feedback="${button.dataset.confirm}"]`), `${booking ? 'Appointment confirmed' : 'Appointment rescheduled'} in this prototype: ${displayDate(flow.date)}, ${flow.time}, ${demo.staff[flow.staff].name}.`);
    };
  });
  $$('[data-period]').forEach(button => {
    button.onclick = () => {
      $$('[data-period]').forEach(node => node.classList.toggle('selected', node === button));
      $$('[data-period-panel]').forEach(panel => { panel.hidden = panel.dataset.periodPanel !== button.dataset.period; });
    };
  });
  $('[data-toggle-reschedule]').onclick = () => { $('[data-reschedule]').hidden = !$('[data-reschedule]').hidden; };
  $$('[data-cancel]').forEach(button => {
    button.onclick = () => dialog('Cancel appointment?', '<p class="muted">Cancel this appointment in the prototype?</p>', () => {
      const card = button.closest('.appointment');
      $('.pill', card).textContent = 'Cancelled';
      if ($('.reschedule', card)) $('.reschedule', card).hidden = true;
      button.closest('.row').hidden = true;
    }, 'Cancel appointment');
  });
  $('[data-second-reschedule]').onclick = () => dialog('Reschedule appointment', `<div class="field"><label>New date</label><input type="date" value="2026-11-03" id="verify-date"></div><div class="field"><label>Available staff</label><select id="verify-staff"><option>Elena Ortiz</option><option>Oliver Reed</option></select></div><div class="field"><label>Available time</label><select id="verify-time"><option>14:00</option><option>14:30</option></select></div><div class="message">Recommended: Elena Ortiz · Earliest availability<br>Estimated wait: ~4 minutes · Sample values</div>`, () => {
    const value = $('#verify-date').value;
    if (!value) return false;
    const card = $('[data-second-reschedule]').closest('.appointment-body');
    $('.date-block', card).innerHTML = `<div><strong>${escape(displayDate(value))}</strong><p class="muted">${$('#verify-time').value} · ${escape($('#verify-staff').value)}</p></div>`;
  }, 'Confirm new time');
  $$('[data-profile]').forEach(button => { button.onclick = () => dialog('Customer profile', '<div class="definition"><div><label>NAME</label><strong>Alex Morgan</strong></div><div><label>ROLE</label><strong>Customer</strong></div></div>', undefined, 'Done'); });

  // Staff operations demonstrate states; the final transition rules remain open.
  let selectedAppointment = $('.agenda-item.selected');
  $$('[data-person]').forEach(button => {
    button.onclick = () => {
      selectedAppointment = button;
      $$('[data-person]').forEach(node => node.classList.toggle('selected', node === button));
      $('[data-detail-name]').textContent = button.dataset.person;
      $('[data-detail-time]').textContent = button.dataset.time;
      $('[data-detail-status]').textContent = button.dataset.status;
      $('.detail-name .avatar').textContent = initials(button.dataset.person);
      $('[data-staff-feedback]').replaceChildren();
    };
  });
  function staffTotals() {
    const rows = $$('[data-person]');
    const done = rows.filter(row => row.dataset.status === 'Completed').length;
    const metrics = $$('#fs3 .metric');
    $('strong', metrics[0]).textContent = String(rows.length).padStart(2, '0');
    $('small', metrics[0]).textContent = `${done} completed · ${rows.length - done} remaining`;
    $('strong', metrics[3]).textContent = `${Math.round(rows.length * .5 / 6 * 100)}%`;
    $('small', metrics[3]).textContent = `${rows.length * .5} of 6 sample hours booked`;
  }
  $$('[data-status-action]').forEach(button => {
    button.onclick = () => {
      selectedAppointment.dataset.status = button.dataset.statusAction;
      $('.pill', selectedAppointment).textContent = button.dataset.statusAction;
      $('[data-detail-status]').textContent = button.dataset.statusAction;
      feedback($('[data-staff-feedback]'), `Prototype status: ${button.dataset.statusAction}.`);
      staffTotals();
    };
  });
  function workingHours(role = 'staff') {
    dialog(role === 'staff' ? 'Working hours & availability' : 'Organization working hours', `<div class="field"><label>Date</label><input type="date" id="hours-date" value="2026-10-29"></div><div class="setting-grid"><div class="field"><label>Start</label><input type="time" id="hours-start" value="09:00"></div><div class="field"><label>End</label><input type="time" id="hours-end" value="16:00"></div></div><div class="field"><label>Availability</label><select id="hours-availability"><option>Available</option><option>Unavailable</option></select></div><p class="demo-help">Changes affect this prototype only.</p><div data-hours-error role="alert"></div>`, () => {
      const start = $('#hours-start').value, end = $('#hours-end').value;
      if (!$('#hours-date').value || !start || !end || start >= end) { $('[data-hours-error]').textContent = 'Choose a date and an end time after the start time.'; return false; }
      if (role === 'staff') feedback($('[data-staff-feedback]'), `Prototype availability: ${$('#hours-availability').value}, ${$('#hours-date').value}, ${start}–${end}.`);
    });
  }
  $$('[data-hours]').forEach(button => { button.onclick = () => workingHours('staff'); });

  // Organization management has no mandatory schedule approval workflow.
  let bookingRules = { window: '', cancellation: '', buffer: '', capacity: '' };
  function showRules() {
    dialog('Booking rules', `<div class="field"><label>Booking window (days)</label><input id="rule-window" type="number" min="1" value="${bookingRules.window}" placeholder="Not configured"></div><div class="field"><label>Cancellation limit (hours)</label><input id="rule-cancel" type="number" min="0" value="${bookingRules.cancellation}" placeholder="Not configured"></div><div class="field"><label>Buffer (minutes)</label><input id="rule-buffer" type="number" min="0" value="${bookingRules.buffer}" placeholder="Not configured"></div><div class="field"><label>Capacity value</label><input id="rule-capacity" type="number" min="1" value="${bookingRules.capacity}" placeholder="Not configured"></div><p class="demo-help">Capacity meaning and default rules are pending team confirmation. No production policy is applied.</p>`, () => {
      const inputs = $$('[data-modal-body] input');
      if (inputs.some(input => !input.checkValidity())) { inputs.find(input => !input.checkValidity()).reportValidity(); return false; }
      bookingRules = { window: $('#rule-window').value, cancellation: $('#rule-cancel').value, buffer: $('#rule-buffer').value, capacity: $('#rule-capacity').value };
    });
  }
  $('[data-rules]').onclick = showRules;
  $('[data-owner-brand]').onclick = () => { $('[data-owner-brand-panel]').hidden = !$('[data-owner-brand-panel]').hidden; };
  $('[data-logo]').onclick = () => $('[data-logo-input]').click();
  $('[data-logo-input]').onchange = event => {
    const file = event.target.files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => {
      const image = new Image(); image.alt = 'Organization logo preview'; image.src = reader.result; image.className = 'organization-logo-image';
      $('#fs4 .org-logo').replaceChildren(image);
    };
    reader.readAsDataURL(file);
  };
  $('[data-brand-save]').onclick = () => {
    root.style.setProperty('--teal', $('#primary-color').value);
    feedback($('[data-brand-feedback]'), `Prototype appearance saved. Customer label: ${$('#customer-label').value}.`);
  };
  $$('[data-nav]').forEach(button => {
    button.onclick = () => {
      const name = button.dataset.nav;
      if (['Daily agenda', 'Team schedule', 'Organizations'].includes(name)) return;
      if (name === 'Working hours') return workingHours(button.dataset.role);
      if (name === 'Booking rules') return showRules();
      if (name === 'Team members') return dialog(name, `<div class="definition">${demo.staff.map(staff => `<div><label>${escape(staff.title)}</label><strong>${staff.name} · Active</strong></div>`).join('')}</div>`, undefined, 'Done');
      if (name === 'Services') return dialog(name, `<div class="definition">${demo.services.map(service => `<div><label>SERVICE</label><strong>${service}</strong></div>`).join('')}</div>`, undefined, 'Done');
    };
  });

  // Calendar uses fixed sample dates, not recurring copies in every week.
  const sampleMonday = parseDate('2026-10-26');
  const calendar = demo.calendar.map(([staff, day, start, duration, name, service, status]) => {
    const date = new Date(sampleMonday); date.setUTCDate(date.getUTCDate() + day);
    return { staff, date: dateISO(date), start, duration, name, service, status };
  });
  let calendarDate = demo.date;
  let calendarView = 'day';
  const category = ['advising', 'documents', 'registration'];
  function mondayOf(value) {
    const date = parseDate(value); date.setUTCDate(date.getUTCDate() - (date.getUTCDay() + 6) % 7); return date;
  }
  function showCalendar() {
    const filter = $('[data-team-filter]').value;
    const team = demo.staff.map((staff, index) => ({ ...staff, index })).filter(staff => filter === 'all' || staff.name === filter);
    const monday = mondayOf(calendarDate), friday = new Date(monday); friday.setUTCDate(friday.getUTCDate() + 4);
    const format = date => new Intl.DateTimeFormat('en-US', { month: 'short', day: 'numeric', timeZone: 'UTC' }).format(date);
    $('[data-cal-date]').textContent = calendarView === 'day' ? format(parseDate(calendarDate)) + ', 2026' : `${format(monday)} – ${format(friday)}`;
    $('[data-cal-weekday]').textContent = calendarView === 'week' ? 'Team week' : new Intl.DateTimeFormat('en-US', { weekday: 'long', timeZone: 'UTC' }).format(parseDate(calendarDate));
    $('[data-cal-subtitle]').textContent = calendarView === 'week' ? 'Monday–Friday · Team appointments' : `${format(parseDate(calendarDate))} · Staff availability & appointments`;
    const visible = calendar.filter(event => team.some(staff => staff.index === event.staff) && (calendarView === 'day' ? event.date === calendarDate : event.date >= dateISO(monday) && event.date <= dateISO(friday)));
    const days = [0, 1, 2, 3, 4].map(index => { const date = new Date(monday); date.setUTCDate(date.getUTCDate() + index); return date; });
    const columns = calendarView === 'day' ? team.map(staff => ({ key: staff.index, name: staff.name, initials: staff.initials, subtitle: staff.title, color: staff.color })) : days.map(date => ({ key: dateISO(date), name: new Intl.DateTimeFormat('en-US', { weekday: 'long', timeZone: 'UTC' }).format(date), initials: String(date.getUTCDate()), subtitle: format(date), color: '' }));
    const header = `<div class="calendar-staff-head"><div class="zone">Local</div>${columns.map(col => `<div><span class="avatar ${col.color}">${col.initials}</span><div><strong>${col.name}</strong><small>${col.subtitle}</small></div></div>`).join('')}</div>`;
    const rail = `<div class="time-rail">${[9,10,11,12,13,14,15,16].map(hour => `<span style="top:${(hour - 9) * 60}px">${time(hour)}</span>`).join('')}</div>`;
    const lanes = columns.map(col => {
      const events = visible.filter(event => calendarView === 'day' ? event.staff === col.key : event.date === col.key);
      const staffInLane = [...new Set(events.map(event => event.staff))].sort();
      return `<div class="staff-lane">${calendarView === 'day' ? '<div class="cal-break" style="top:180px;height:60px">Lunch break</div>' : ''}${events.map(event => {
        const count = calendarView === 'week' ? staffInLane.length : 1, index = calendarView === 'week' ? staffInLane.indexOf(event.staff) : 0;
        const label = `${event.name}, ${demo.services[event.service]}, ${event.date}, ${time(event.start)}–${time(event.start + event.duration)}, ${demo.staff[event.staff].name}`;
        return `<button type="button" class="cal-event ${category[event.service]} ${event.duration < 1 ? 'short-event' : ''}" data-event="${calendar.indexOf(event)}" aria-label="${escape(label)}" title="${escape(label)}" style="top:${(event.start - 9) * 60 + 2}px;height:${event.duration * 60 - 4}px;left:calc(${index * 100 / count}% + 3px);width:calc(${100 / count}% - 5px)"><span class="event-time">${time(event.start)}–${time(event.start + event.duration)}</span><strong>${escape(event.name)}</strong>${event.duration >= 1 ? `<small>${demo.services[event.service]}</small>` : ''}</button>`;
      }).join('')}</div>`;
    }).join('');
    const container = $('[data-team-calendar]');
    container.style.setProperty('--cols', columns.length);
    container.innerHTML = header + `<div class="calendar-body">${rail}${lanes}</div>`;
    $('[data-cal-count]').innerHTML = `${visible.length}<small>${calendarView === 'week' ? 'this week' : 'in this day'}</small>`;
    const strip = $('.operations-strip');
    $('strong', strip.children[1]).innerHTML = `${team.length}<small>in this view</small>`;
    const workingHours = (calendarView === 'week' ? 5 : 1) * team.length * 7;
    $('strong', strip.children[2]).innerHTML = `${Math.round(visible.reduce((sum, event) => sum + event.duration, 0) / workingHours * 100)}%<small>of sample working hours</small>`;
    $('[data-completed-count]').innerHTML = `${visible.filter(event => event.status === 'Completed').length}<small>in this view</small>`;
    $$('[data-event]').forEach(button => {
      button.onclick = () => {
        const event = calendar[Number(button.dataset.event)];
        dialog('Appointment details', `<h3 class="dialog-customer">${escape(event.name)}</h3><p class="muted">${demo.services[event.service]}</p><div class="definition"><div><label>STAFF</label><strong>${demo.staff[event.staff].name}</strong></div><div><label>DATE</label><strong>${event.date}</strong></div><div><label>TIME</label><strong>${time(event.start)}–${time(event.start + event.duration)}</strong></div><div><label>STATUS</label><strong>${event.status}</strong></div></div>`, undefined, 'Done');
      };
    });
  }
  $$('[data-cal-view]').forEach(button => { button.onclick = () => { calendarView = button.dataset.calView; $$('[data-cal-view]').forEach(node => node.classList.toggle('chosen', node === button)); showCalendar(); }; });
  $('[data-team-filter]').onchange = showCalendar;
  function moveCalendar(direction) { const date = parseDate(calendarDate); date.setUTCDate(date.getUTCDate() + direction * (calendarView === 'day' ? 1 : 7)); calendarDate = dateISO(date); showCalendar(); }
  $('[data-cal-prev]').onclick = () => moveCalendar(-1);
  $('[data-cal-next]').onclick = () => moveCalendar(1);
  $('[data-cal-today]').textContent = 'Sample day';
  $('[data-cal-today]').onclick = () => { calendarDate = demo.date; showCalendar(); };
  $('[data-owner-add]').onclick = () => dialog('New appointment', `<div class="field"><label>Customer name</label><input id="new-customer" required></div><div class="field"><label>Staff</label><select id="new-staff">${demo.staff.map((staff, index) => `<option value="${index}">${staff.name}</option>`).join('')}</select></div><div class="field"><label>Start time · ${calendarDate}</label><select id="new-start"><option value="14.5">14:30</option><option value="16.5">16:30</option></select></div><p class="demo-help">30-minute sample appointment. The prototype checks overlaps.</p><div data-new-error role="alert"></div>`, () => {
    const name = $('#new-customer').value.trim(), staff = Number($('#new-staff').value), start = Number($('#new-start').value);
    if (!name) { $('[data-new-error]').textContent = 'Enter a customer name.'; return false; }
    if (calendar.some(event => event.staff === staff && event.date === calendarDate && start < event.start + event.duration && start + .5 > event.start)) { $('[data-new-error]').textContent = 'This slot is already occupied.'; return false; }
    calendar.push({ staff, date: calendarDate, start, duration: .5, name, service: staff === 2 ? 1 : staff === 3 ? 2 : 0, status: 'Confirmed' });
    showCalendar();
  }, 'Create appointment');

  // Platform organization administration.
  let selectedOrganization = 0;
  function showOrganizations() {
    const query = $('[data-org-search]').value.trim().toLowerCase();
    const type = $('[data-org-type]').value, status = $('[data-org-filter]').value;
    const rows = demo.organizations.map((organization, index) => ({ ...organization, index })).filter(row => `${row.name} ${row.owner}`.toLowerCase().includes(query) && (!type || type === row.type) && (!status || status === row.status));
    $('[data-org-table]').innerHTML = rows.map(row => `<tr class="${row.index === selectedOrganization ? 'chosen' : ''}"><td><button type="button" class="org-pick" data-pick="${row.index}"><strong>${escape(row.name)}</strong><span class="muted">ORG-${String(row.index + 1).padStart(3, '0')}</span></button></td><td>${row.type}</td><td>${escape(row.owner)}</td><td><span class="pill ${row.status === 'Active' ? 'green' : ''}">${row.status}</span></td><td><button type="button" class="text-button" data-pick="${row.index}" aria-label="View ${escape(row.name)}">${icon('arrow-up-right')}</button></td></tr>`).join('') || '<tr><td colspan="5">No organizations match these filters.</td></tr>';
    $('[data-result-count]').textContent = `Showing ${rows.length} organizations`;
    $('[data-org-count]').textContent = String(demo.organizations.length).padStart(2, '0');
    $('[data-active-count]').textContent = demo.organizations.filter(row => row.status === 'Active').length;
    $('[data-inactive-count]').textContent = demo.organizations.filter(row => row.status === 'Inactive').length;
    $$('[data-pick]').forEach(button => { button.onclick = () => { selectedOrganization = Number(button.dataset.pick); showOrganizations(); organizationDetails(); }; });
    refreshIcons();
  }
  function organizationDetails() {
    const organization = demo.organizations[selectedOrganization];
    $('[data-org-name]').textContent = organization.name;
    $('[data-org-initial]').textContent = organization.name[0];
    $('[data-org-type-label]').textContent = organization.type;
    $('[data-org-owner]').textContent = organization.owner;
    $('[data-org-id]').textContent = `ORG-${String(selectedOrganization + 1).padStart(3, '0')}`;
    $('[data-org-status]').textContent = organization.status;
    $('[data-org-status]').className = `pill ${organization.status === 'Active' ? 'green' : ''}`;
    $('[data-org-access]').textContent = `Platform access ${organization.status === 'Active' ? 'enabled' : 'disabled'}`;
    $('[data-toggle-org]').textContent = `${organization.status === 'Active' ? 'Deactivate' : 'Activate'} organization`;
  }
  function organizationForm(edit) {
    const organization = edit ? demo.organizations[selectedOrganization] : { name: '', owner: '', type: 'University', status: 'Active' };
    dialog(edit ? 'Edit organization' : 'Add organization', `<div class="field"><label>Organization name</label><input id="org-name" value="${escape(organization.name)}" required></div><div class="field"><label>Type</label><select id="org-type">${['University', 'Clinic', 'Dental practice'].map(type => `<option ${type === organization.type ? 'selected' : ''}>${type}</option>`).join('')}</select></div><div class="field"><label>Assigned owner</label><input id="org-owner" value="${escape(organization.owner)}" required></div><div data-org-error role="alert"></div>`, () => {
      const name = $('#org-name').value.trim(), owner = $('#org-owner').value.trim();
      if (!name || !owner) { $('[data-org-error]').textContent = 'Enter an organization name and owner.'; return false; }
      const next = { name, owner, type: $('#org-type').value, status: organization.status };
      if (edit) demo.organizations[selectedOrganization] = next;
      else { demo.organizations.push(next); selectedOrganization = demo.organizations.length - 1; }
      showOrganizations(); organizationDetails();
    }, edit ? 'Save organization' : 'Add organization');
  }
  ['[data-org-search]', '[data-org-type]', '[data-org-filter]'].forEach(selector => $(selector).addEventListener('input', showOrganizations));
  $('[data-toggle-org]').onclick = () => { const row = demo.organizations[selectedOrganization]; row.status = row.status === 'Active' ? 'Inactive' : 'Active'; showOrganizations(); organizationDetails(); };
  $('[data-add-org]').onclick = () => organizationForm(false);
  $('[data-edit-org]').onclick = () => organizationForm(true);
  $('[data-change-owner]').onclick = () => dialog('Change organization owner', `<div class="field"><label>Assigned owner</label><input id="next-owner" value="${escape(demo.organizations[selectedOrganization].owner)}"></div>`, () => {
    const owner = $('#next-owner').value.trim();
    if (!owner) return false;
    demo.organizations[selectedOrganization].owner = owner; showOrganizations(); organizationDetails();
  });

  renderFlow('book'); staffTotals(); showOrganizations(); organizationDetails(); showCalendar();
  showScreen(Math.max(0, screenNames.indexOf(location.hash.slice(1))) + 1, false);
  refreshIcons();
})();
