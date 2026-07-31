# ECOMM Auth Service

Authentication and session-management microservice for the **ECOMM** e-commerce platform. It owns user identity end-to-end: signup, password and Google login, short-lived JWT access tokens, rotating refresh tokens with theft detection, and session/device management — and it exposes a single source of truth that every other service in the platform can call to verify a caller's identity.

## How it fits in the platform

The platform is split into independently deployable Spring Boot services, registered with and discovered through Eureka:

| Service | Responsibility |
|---|---|
| **Auth Service** [*(this repo)*](https://github.com/sarangKaliyath/ECOMM_Auth_ServiceApplication)| Identity, tokens, sessions |
| [Profile Service](https://github.com/sarangKaliyath/ECOMM_Profile_Service_Application) | User profile data (created reactively on signup) |
| [Product Service](https://github.com/sarangKaliyath/ECOMM_Product_ServiceApplication) | Product catalog |
| [Cart Service](https://github.com/sarangKaliyath/ECOMM_Cart_Service_Application) | Shopping cart |
| [Ordering Service](https://github.com/sarangKaliyath/ECOMM_Ordering_Service_Application) | Order lifecycle |
| [Payment Service](https://github.com/sarangKaliyath/ECOMM_Payment_Gateway_Service_Application) | Payment processing |
| [Email Service](https://github.com/sarangKaliyath/ECOMM_Email_Service_Application) | Transactional email delivery |
| [Service Discovery](https://github.com/sarangKaliyath/ECOMM_Service_Discovery_Application) | Eureka registry |

The Auth Service publishes `user-created` events to Kafka on signup so the Profile Service can react asynchronously, and `email` events (welcome, email-verification, password-reset) so the Email Service can send transactional mail — it never calls either service directly.

## Features

- **Email/password signup and login** — passwords hashed with BCrypt.
- **Google Sign-In (OAuth2 / OIDC)** — first-time Google login transparently provisions a local user account.
- **Short-lived JWT access tokens** — HS256, 15-minute expiry, carrying `userId`, `email`, and `roles` claims. Verified on-demand by other services via `POST /auth/validate`.
- **Rotating refresh tokens** — every refresh issues a brand-new token and immediately invalidates the one that was just used, limiting the value of a leaked token.
- **Refresh-token theft detection** — tokens are grouped into a lineage (`familyId`) from the moment a user logs in. If an already-rotated (dead) token is ever replayed, the entire lineage is revoked immediately, logging out that compromised session chain.
- **Hashed token storage** — refresh tokens are stored as SHA-256 hashes, never in plaintext, so a database read alone can't be used to impersonate a session.
- **Email-based verification codes** — one-time codes for password reset and email verification, rate-limited by attempt count and expiry, delivered via the Email Service over Kafka.
- **Password reset via verification code** — `POST /verify/send` + `POST /verify/confirm` issue a short-lived, single-use reset token, which `POST /auth/reset-password` exchanges for a new password. Resetting a password revokes every existing session for the account.
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
| `POST` | `/verify/send` | Send a one-time verification code by email (login, password reset, or email verification) | No |
| `POST` | `/verify/confirm` | Confirm a verification code; returns a one-time reset token when the type is `PASSWORD_RESET` | No |
| `POST` | `/auth/reset-password` | Exchange a reset token (from `/verify/confirm`) for a new password | Reset token in body |
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

Verification codes support four types (`LOGIN`, `PASSWORD_RESET`, `EMAIL_VERIFICATION`, `PHONE_VERIFICATION`), but not all are wired end-to-end yet:

- **OTP-based login** — codes can be sent and confirmed for `LOGIN`, but no endpoint yet accepts a verified code in place of a password to actually log in.
- **Registration email verification** — `EMAIL_VERIFICATION` codes can be sent and confirmed, but signup does not yet require a confirmed code before the account is usable.
- **Phone verification** — modeled as a `VerificationType` but explicitly rejected by the service (`UnsupportedVerificationTypeException`); not implemented.

## Roadmap

- Externalize the JWT signing secret and datastore credentials into environment-based configuration for production deployments.
- Access-token revocation, for immediate cross-service logout without waiting out the 15-minute expiry.
- Scheduled cleanup of expired/inactive sessions.
