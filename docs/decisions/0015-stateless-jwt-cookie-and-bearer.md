# ADR-0015: Stateless JWT in an HttpOnly cookie (web) or a bearer header (Android)

- **Status:** Proposed
- **Date:** 2026-11-22
- **Feature:** auth

## Context

F1 needs sign-in for four roles from a browser and from Android. The backend must stay stateless so it can run as several instances (ADR-0008). Tokens in `localStorage` can be stolen by any script injected into the page (XSS); cookies sent automatically by the browser need protection against cross-site requests (CSRF).

## Decision

- **No server-side session.** The session is a signed JWT (HS256, Spring Security's Nimbus encoder and decoder, no extra library). Claims: `sub` (user id), `role`, `organizationId`, `staffId`, `iss`, `iat`, `exp`. The backend checks signature, issuer and expiry on every request.
- **Secret and lifetime come from configuration** (`JwtProperties`): `JWT_SECRET` (at least 32 bytes, the app refuses to start otherwise), issuer, lifetime (8 hours for the MVP), cookie name and the cookie `Secure` flag.
- **Web:** `POST /auth/login` sets `sa_access=<jwt>; Path=/api; HttpOnly; SameSite=Strict; Max-Age=<lifetime>` and `Secure` outside local runs. The web app never reads the token; it calls `GET /me` to learn who is signed in. `POST /auth/logout` clears the cookie.
- **Android:** reads `accessToken` from the login response body and sends `Authorization: Bearer <token>`.
- **Token lookup:** the `Authorization` header wins; otherwise the `sa_access` cookie.
- **CSRF:** enabled for cookie-authenticated requests with Spring Security's SPA support (`XSRF-TOKEN` cookie readable by the web app, echoed in the `X-XSRF-TOKEN` header on POST/PUT/PATCH/DELETE). Requests with a bearer header and `POST /auth/login` are exempt. `SameSite=Strict` is the first line of defence; the token is the second.
- **Passwords:** Spring Security's delegating encoder; stored as `{bcrypt}…`.
- **Errors:** wrong e-mail and wrong password return the same 401 problem; 401 and 403 are RFC 7807 bodies. Login and logout are in the `auth` rate-limit group.
- **Same origin:** the web app reaches the API under the same origin (`/api` proxied by Vite locally and by nginx in the container), so cookies need no cross-site settings.

## Consequences

- Horizontal scaling needs no shared session store.
- A token cannot be revoked before it expires; logout only removes the browser cookie. A shorter access token plus a refresh cookie can be added later without changing clients' request handling.
- Browser clients must send the CSRF header on state-changing requests; the web API client does this in one place.

## Alternatives considered

- Server sessions (Spring Session + Redis): revocable, but stateful and an extra service.
- Token in `localStorage` with a bearer header in the browser: simple, but exposed to XSS.
- An external identity provider (Keycloak, Auth0): more than the MVP needs.
