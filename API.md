# Expense Tracker API – API Documentation

Base URL (local): `http://localhost:8080`

Interactive documentation:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

> This document is the detailed API reference. For setup, runtime, CI, and project overview, see [README.md](README.md). A ready-to-import client collection is available at [postman/Expense-Tracker-API.postman_collection.json](postman/Expense-Tracker-API.postman_collection.json).

## Authentication and sessions

The API uses **short-lived JWT access tokens** and **rotatable, server-stored refresh tokens**.

- **Access token:** sent as `Authorization: Bearer <accessToken>` to protected endpoints (default lifetime: 15 minutes, configurable via `JWT_ACCESS_EXPIRATION_MINUTES`).
- **Refresh token:** opaque token returned by register/login/refresh; stored server-side as a hash with expiry/revocation/rotation metadata (default lifetime: 30 days, configurable via `JWT_REFRESH_EXPIRATION_DAYS`).
- **Rotation:** `POST /api/auth/refresh` revokes the presented refresh token and issues a new access/refresh pair. Reuse of a revoked/rotated token is treated as suspicious and active refresh tokens for that user may be revoked.
- **Logout:** revoke a specific refresh token, or revoke all active refresh tokens for the authenticated user (`allDevices=true` with Authorization).
- **Public auth endpoints:** `/api/auth/register`, `/api/auth/login`, `/api/auth/refresh`, `/api/auth/logout` (plus Swagger/OpenAPI and selected Actuator endpoints).
- **Brute-force protection:** in-memory per-IP rate limiting is applied to `/api/auth/**` requests (configurable via `APP_AUTH_RATE_LIMIT_MAX_REQUESTS` and `APP_AUTH_RATE_LIMIT_WINDOW_SECONDS`). Exceeded limits return **429 Too Many Requests**.
- **Swagger UI:** click **Authorize** and provide the **access token** value (without the `Bearer ` prefix).

### Register

`POST /api/auth/register` — **201 Created** (auth required: no)

Request body:
```json
{
  "name": "Harsh",
  "email": "harsh@example.com",
  "password": "password123"
}
```

Rules:
- `name`: required, max 80 characters
- `email`: required, valid email, unique, max 120 characters
- `password`: required, 8–72 characters

### Login

`POST /api/auth/login` — **200 OK** (auth required: no)

Request body:
```json
{
  "email": "harsh@example.com",
  "password": "password123"
}
```

### Refresh (rotate tokens)

`POST /api/auth/refresh` — **200 OK** (auth required: no; requires valid refresh token)

Request body:
```json
{
  "refreshToken": "<refresh-token>"
}
```

Behavior:
- Validates and rotates the refresh token (one-time use per issued token).
- Returns a new `accessToken` and a new `refreshToken`.
- Invalid, expired, or revoked tokens return **400 Bad Request**.

### Logout / revoke

`POST /api/auth/logout` — **200 OK**

Request body examples:

Revoke a specific refresh token (no Authorization required):
```json
{
  "refreshToken": "<refresh-token>"
}
```

Revoke all active refresh tokens for the authenticated user (Authorization required):
```json
{
  "allDevices": true
}
```

Response:
```json
{
  "message": "Logged out successfully"
}
```
(or `"Logged out from all devices"`)

Rules:
- Provide `refreshToken` unless `allDevices=true`.
- `allDevices=true` requires `Authorization: Bearer <accessToken>`.

### Auth response

Returned by register, login, and refresh:
```json
{
  "accessToken": "<jwt-access-token>",
  "expiresInSeconds": 900,
  "refreshToken": "<opaque-refresh-token>",
  "tokenType": "Bearer",
  "userId": 1,
  "name": "Harsh",
  "email": "harsh@example.com"
}
```

## Users (profile)

All endpoints below require `Authorization: Bearer <accessToken>`.

### Get current profile

`GET /api/users/me` — **200 OK**

Response:
```json
{
  "id": 1,
  "name": "Harsh",
  "email": "harsh@example.com",
  "createdAt": "2026-11-21T12:00:00Z"
}
```

### Update current profile

`PUT /api/users/me` — **200 OK**

Request body:
```json
{
  "name": "Harsh Kumar"
}
```

Rules:
- `name`: required, max 80 characters

Response: updated profile object. Security audit event recorded.

### Change password

`PUT /api/users/me/password` — **204 No Content**

Request body:
```json
{
  "currentPassword": "password123",
  "newPassword": "newPassword123"
}
```

Rules:
- `currentPassword`: required and must match the current password
- `newPassword`: required, 8–72 characters, must be different from the current password

Security/auditing:
- Password change attempts/outcomes are recorded as security audit events (with user/IP context).
- **Password reset (forgot password) flow is not implemented yet.** Recommended considerations: time-limited single-use reset tokens, out-of-band delivery, rate limiting, session/token revocation on reset, and audit events for request/verification/completion/failure.

## Categories

All endpoints below require authentication and operate only on categories owned by the authenticated user.

### List categories
`GET /api/categories` — **200 OK** → `[{ "id": 1, "name": "Food" }]`

### Create category
`POST /api/categories` — **201 Created**

```json
{ "name": "Food" }
```
- `name`: required, max 50 characters, unique per user (case-insensitive)

### Rename category
`PUT /api/categories/{id}` — **200 OK**

```json
{ "name": "Groceries" }
```

### Delete category
`DELETE /api/categories/{id}` — **204 No Content**

Notes: owner-scoped; deletion may be constrained if expenses still reference the category.

## Expenses

All endpoints below require authentication and operate only on expenses owned by the authenticated user.

### Expense request body (create/update)

```json
{
  "title": "Lunch",
  "description": "Lunch with colleagues",
  "amount": 250.00,
  "expenseDate": "2026-11-21",
  "categoryId": 1
}
```

Rules:
- `title`: required, max 120 characters
- `description`: optional, max 1000 characters
- `amount`: required, `>= 0.01`, up to 10 integer digits and 2 decimal places
- `expenseDate`: required, ISO-8601 date (`yyyy-MM-dd`)
- `categoryId`: optional/nullable; if provided, must reference a category owned by the authenticated user

### Expense response

```json
{
  "id": 10,
  "title": "Lunch",
  "description": "Lunch with colleagues",
  "amount": 250.00,
  "expenseDate": "2026-11-21",
  "categoryId": 1,
  "categoryName": "Food"
}
```
(`categoryId`/`categoryName` are `null` when uncategorized.)

### Create expense
`POST /api/expenses` — **201 Created**

Side effect: on successful commit, an `ExpenseCreatedEvent` is published for forwarding to Kafka topic `expense-created` (requires reachable Kafka broker for delivery/consumption).

### Get expense by id
`GET /api/expenses/{id}` — **200 OK**

### List/search expenses (paginated, sortable)
`GET /api/expenses` — **200 OK**

Query parameters:
- `from` (optional, `yyyy-MM-dd`): include expenses on/after this date
- `to` (optional, `yyyy-MM-dd`): include expenses on/before this date (`from` must be <= `to`)
- `categoryId` (optional, long): filter by category id (owner-scoped)
- `q` (optional, string): case-insensitive search in `title` and `description`
- `page` (optional, int, default `0`, must be >= 0)
- `size` (optional, int, default `20`, must be 1–100)
- `sortBy` (optional, default `expenseDate`): one of `expenseDate`, `amount`, `title`, `id`
- `sortDir` (optional, default `desc`): `asc` or `desc`

Response:
```json
{
  "items": [ /* expense responses */ ],
  "page": 0,
  "size": 20,
  "totalItems": 1,
  "totalPages": 1
}
```

Notes:
- Default ordering: `expenseDate desc, id desc` (a stable `id` tiebreaker is applied when sorting by non-id fields).
- Invalid sort/page/size/date-range values return **400 Bad Request** with a structured error message.

### Update expense
`PUT /api/expenses/{id}` — **200 OK**

### Delete expense
`DELETE /api/expenses/{id}` — **204 No Content**

## Reports

All report endpoints require authentication and are computed over the authenticated user's expenses.

### Monthly summary
`GET /api/reports/monthly?year={year}&month={month}` — **200 OK**

- `year` (required, int)
- `month` (required, int, 1–12)

Response includes `year`, `month`, `total`, `transactionCount`, and `categoryTotals` (includes `"Uncategorized"` when applicable).

### Yearly summary
`GET /api/reports/yearly?year={year}` — **200 OK**

- `year` (required, int; validated range)

Response includes `year`, `total`, `transactionCount`, `monthlyTotals` (all 12 months), and `categoryTotals`.

## Errors, validation, and security behavior

Structured error responses (see [ApiExceptionHandler](src/main/java/com/harsh/expense/common/ApiExceptionHandler.java)):

```json
{
  "timestamp": "2026-11-21T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "..."
}
```

Common responses:
- **400 Bad Request:** validation failures, invalid parameters/sort values, invalid credentials/token states, duplicate/conflict business rules surfaced as bad requests, etc.
- **401/403:** missing/invalid access token or unauthorized access (framework/security handling).
- **429 Too Many Requests:** auth endpoint rate limit exceeded (includes `Retry-After` header).
- **500 Internal Server Error:** unexpected errors return a generic message.

Additional behavior:
- **Correlation ID:** `X-Correlation-Id` accepted/generated, returned in responses, and added to logs (MDC).
- **Security headers:** API responses include defense-in-depth headers (e.g. `X-Content-Type-Options`, frame protections, referrer policy, HSTS, CSP, permissions policy) configured in security settings.
- **CORS:** configurable allowed origins via `APP_CORS_ALLOWED_ORIGINS` (comma-separated). Empty/default uses safe local-development origin patterns; set explicit origins for non-local environments.
- **Auditing:** security audit events are recorded for key actions (register/login/refresh/logout, profile/password changes). Password reset is not yet implemented; see considerations above.

## Usage tips

- **Typical flow:** register/login → store `accessToken` and `refreshToken` → call protected APIs with `Authorization: Bearer <accessToken>` → on expiry/401, call `/api/auth/refresh` → on sign-out, call `/api/auth/logout`.
- **Swagger:** authorize with the access token; re-authorize after refresh.
- **Programmatic calls:** send `Content-Type: application/json` for JSON bodies.
- **Kafka:** to observe `expense-created` publish/consume, ensure Kafka is reachable (`localhost:9092` for host-local, `kafka:29092` in Compose network). See [ARCHITECTURE.md](ARCHITECTURE.md).
- **API client collection:** import [postman/Expense-Tracker-API.postman_collection.json](postman/Expense-Tracker-API.postman_collection.json) and set `baseUrl`, `accessToken`, `refreshToken` variables.