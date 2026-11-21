# Interface prototype

Open `index.html` directly in a browser. The five screens share the HTML document, CSS and JavaScript; the screen selector does not require a router or server. Hash links select a screen directly. Use the root `../../index.html` as a convenient entry point.

Source responsibilities:

- `index.html`: semantic screen markup and shared dialog.
- `assets/css/styles.css`: layout and styling, including classes extracted from earlier inline styles.
- `assets/js/demo-data.js`: synthetic staff, organization and calendar fixtures.
- `assets/js/app.js`: local UI interactions; no host-specific Codex API.
- `assets/vendor/lucide.min.js`: local icon library; license included.

There are no external font requests or required CDN calls. Internet is not required to view this prototype. The actual product remains an online client/server system.

Implemented demonstrations: screen navigation, staff/date/slot selection, booking confirmation, rescheduling/cancellation, staff status updates, working-hour form validation, staff calendar filtering, day/week/date navigation, appointment detail dialogs, local appointment overlap checking, organization search/type/status filters, organization create/edit/status and owner changes, appearance preview.

All changes reset on reload. The five views are representative, not the full application. Reference-only service/team dialogs do not implement full management workflows.

Waiting-time and recommendation values are fixed fixtures; no algorithm is implemented. Numeric booking policies start unconfigured. No mandatory owner approval workflow is included.

Screenshots and test results are generated from these sources, not from the earlier branded design.
