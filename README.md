# ECOMM Auth Service

Authentication and session-management microservice for the **ECOMM** e-commerce platform. It owns user identity end-to-end: signup, password and Google login, short-lived JWT access tokens, rotating refresh tokens with theft detection, and session/device management — and it exposes a single source of truth that every other service in the platform can call to verify a caller's identity.

## Where it fits

The platform is split into independently deployable Spring Boot services, registered with and discovered through Eureka:

| Service | Responsibility |
|---|---|
| **Auth Service** *(this repo)* | Identity, tokens, sessions |
| Profile Service | User profile data (created reactively on signup) |
| Product Service | Product catalog |
| Cart Service | Shopping cart |
| Ordering Service | Order lifecycle |
| Payment Service | Payment processing |
| Email Service | Transactional email delivery |
| Service Discovery | Eureka registry |

The Auth Service publishes `user-created` and `signup` events to Kafka on signup so the Profile and Email services can react asynchronously — it never calls them directly.

## Features

- **Email/password signup and login** — passwords hashed with BCrypt.
- **Google Sign-In (OAuth2 / OIDC)** — first-time Google login transparently provisions a local user account.
- **Short-lived JWT access tokens** — HS256, 15-minute expiry, carrying `userId`, `email`, and `roles` claims. Verified on-demand by other services via `POST /auth/validate`.
- **Rotating refresh tokens** — every refresh issues a brand-new token and immediately invalidates the one that was just used, limiting the value of a leaked token.
- **Refresh-token theft detection** — tokens are grouped into a lineage (`familyId`) from the moment a user logs in. If an already-rotated (dead) token is ever replayed, the entire lineage is revoked immediately, logging out that compromised session chain.
- **Hashed token storage** — refresh tokens are stored as SHA-256 hashes, never in plaintext, so a database read alone can't be used to impersonate a session.
- **Per-device and account-wide session control** — `POST /auth/logout` ends the current session; `POST /auth/logout-all` revokes every active session for the account across all devices in one call.
- **HttpOnly, Secure, SameSite=Strict cookies** — the refresh token never touches client-side JavaScript or response bodies.
- **Service discovery** — registers with Eureka so it can be called by name from anywhere in the platform.

## How authentication works

```
1. POST /auth/login          → validates credentials, starts a session,
                                sets an HttpOnly refresh-token cookie
2. POST /auth/refresh        → exchanges the refresh-token cookie for a
                                short-lived access token, rotates the cookie
3. Authorization: Bearer ... → access token is sent to any service that
                                needs to verify the caller
4. POST /auth/validate       → any service can verify an access token here
5. POST /auth/logout         → ends the current session
   POST /auth/logout-all     → ends every session for the account
```

Login intentionally does **not** return an access token — the client always follows up with `/auth/refresh` to obtain one. This keeps token issuance on a single, consistent code path.

## API Reference

| Method | Endpoint | Description | Auth required |
|---|---|---|---|
| `POST` | `/auth/signup` | Create an account | No |
| `POST` | `/auth/login` | Authenticate with email/password, start a session | No |
| `GET`  | `/oauth2/authorization/google` | Begin Google OAuth2 login | No |
| `POST` | `/auth/refresh` | Rotate refresh token, issue a new access token | Refresh-token cookie |
| `POST` | `/auth/logout` | Revoke the current session | Refresh-token cookie |
| `POST` | `/auth/logout-all` | Revoke every session for the account | Refresh-token cookie |
| `POST` | `/auth/validate` | Verify an access token (used by other services) | Access token in body |
| `GET`  | `/user/{id}` | Fetch user details by id | — |

## Tech stack

- **Java 17**, **Spring Boot 3.3.3**
- Spring Security + OAuth2 Client (Google OIDC)
- Spring Data JPA + MySQL
- `jjwt` for JWT signing/parsing
- Spring Kafka for event publishing
- Spring Cloud Netflix Eureka Client for service discovery
- Lombok

## Running locally

**Prerequisites:** JDK 17, MySQL, a running Eureka server, a reachable Kafka broker.

1. Create a MySQL database matching `spring.datasource.url` in `src/main/resources/application.properties`.
2. Create a `.env` file in the project root (loaded automatically at startup) with:
   ```
   GOOGLE_CLIENT_ID=your-google-oauth-client-id
   GOOGLE_CLIENT_SECRET=your-google-oauth-client-secret
   OAUTH_REDIRECT_URL=http://localhost:3000/oauth/callback
   SERVER_PORT=8081
   ```
3. Start the service:
   ```
   ./mvnw spring-boot:run
   ```

The service registers itself with Eureka on startup and is then reachable by other platform services by its Eureka application name.

## In progress

The data model for OTP-based flows (`UserOtp`, `OtpType`) already exists in the codebase; the service/controller layer for these is under active development and not yet exposed:

- **OTP-based login** — one-time-passcode as an alternative to password login.
- **Password reset via OTP** — request a reset code by email, verify it, set a new password.
- **Registration OTP verification** — confirm a new account's email address at signup.

## Roadmap

- Externalize the JWT signing secret and datastore credentials into environment-based configuration for production deployments.
- Access-token revocation, for immediate cross-service logout without waiting out the 15-minute expiry.
- Scheduled cleanup of expired/inactive sessions.
