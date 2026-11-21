<!-- Copyright (c) 2026 Harsh (git4harsh). All rights reserved. -->
# Design decisions, trade-offs, and roadmap

## Key design decisions

### Stateless auth with short-lived access tokens + rotatable refresh tokens
- **Decision:** Issue JWT access tokens (short-lived, default 15 minutes) for API authorization and opaque refresh tokens (long-lived, stored hashed in DB) for renewal. Refresh rotates tokens (old token revoked and linked to replacement); reuse of revoked tokens triggers revocation of active tokens for the user.
- **Why:** Limits blast radius of leaked access tokens while keeping APIs stateless and scalable. Rotation improves session security compared to non-rotating refresh tokens.
- **Trade-off:** Requires server-side refresh-token storage/revocation state (not fully stateless), but enables logout/revocation and auditability.

### Owner-scoped data access
- **Decision:** All expense/category/profile operations resolve the authenticated principal and query/mutate only resources owned by that user.
- **Why:** Prevents horizontal privilege escalation and keeps authorization logic explicit at service/repository boundaries.
- **Trade-off:** Slightly more query constraints and checks, but essential for correctness and security.

### Validation and consistent error contracts
- **Decision:** Bean Validation on DTOs plus centralized exception handling returning structured JSON errors (`timestamp`, `status`, `error`, `message`) and correlation IDs.
- **Why:** Predictable client behavior, easier debugging, and safer defaults (e.g. generic 500 messages, no stack traces to clients).
- **Trade-off:** Some domain errors are surfaced as `400 Bad Request`; a future improvement is richer problem types/codes.

### Expense search, pagination, and sorting
- **Decision:** Filterable/paginated expense listing with validated bounds and whitelisted sort fields/directions.
- **Why:** Prevents unbounded queries, supports common UI/reporting needs, and avoids unsafe dynamic sorting.
- **Trade-off:** Whitelist requires updates when new sortable fields are added intentionally.

### In-memory auth rate limiting and defense-in-depth headers/CORS
- **Decision:** Lightweight per-IP/endpoint window limiter for `/api/auth/**`, configurable CORS origins, and secure HTTP response headers (CSP for API, no-sniff, frame deny, referrer policy, HSTS, permissions policy).
- **Why:** Reduces brute-force/noise risk and hardens browser-facing behavior with minimal operational complexity.
- **Trade-off:** In-memory limiter is per-instance (not globally consistent across replicas); production should use a distributed rate limiter (e.g. Redis/API gateway/WAF).

### Security auditing
- **Decision:** Persist audit events for key auth/profile/password actions (success/failure) with timestamps, user/email context, IP, and details.
- **Why:** Supports accountability, troubleshooting, and security review.
- **Trade-off:** Audit volume/retention must be managed in production (rotation, privacy, indexing). Password reset is not yet implemented; design anticipates auditable reset request/verification/completion events.

### Event-driven notification demo (Kafka)
- **Decision:** Publish `expense-created` events after transaction commit and consume them for logging/notification demonstration.
- **Why:** Shows decoupling and extensibility for downstream workflows.
- **Trade-off:** Current flow is at-least-once/demo-grade without an outbox/DLQ/idempotency guarantees; reliability hardening is on the roadmap.

### Reproducible runtime and CI quality gates
- **Decision:** Multi-stage Docker image, Compose stack (app + MySQL + Kafka + Prometheus + Grafana), environment-driven config/profiles, GitHub Actions CI, and Maven quality checks (formatting, Checkstyle, SpotBugs, advisory dependency scanning, Dependabot).
- **Why:** Makes the project easy to run/review and enforces baseline engineering hygiene.
- **Trade-off:** Static/security checks can add build time and require tuning; CI dependency scans depend on external vulnerability databases.

## Limitations

- No Flyway/Liquibase migrations yet (`ddl-auto: update` used for convenience).
- No Testcontainers/integration or end-to-end API security test suite in this iteration.
- Kafka delivery is not transactional-outbox based; no DLQ/retry/idempotency framework beyond basic consumer behavior.
- Auth rate limiting is in-memory and single-node.
- No refresh-token/session management UI, device metadata, or MFA.
- No password reset/forgot-password flow (auditing considerations documented).
- No budgets/recurring expenses/attachments/export features in this iteration.
- Grafana credentials and defaults are demo-oriented; must be hardened for non-local exposure.

## Production roadmap

1. **Data & reliability:** add migrations, constraints/index review, backups, and transactional outbox + DLQ/idempotent consumers for Kafka.
2. **Testing:** Testcontainers integration tests, API/security tests, contract tests, and performance/load checks for reporting queries.
3. **Security:** distributed rate limiting/WAF integration, MFA/step-up auth, password reset with audited token lifecycle, secret management/rotation, and tighter production CORS/TLS policies.
4. **Observability & ops:** structured JSON logs, tracing (OpenTelemetry), alerting/SLOs, dashboards per use case, and runbooks.
5. **Product capabilities:** budgets/alerts, recurring expenses, imports/exports, richer analytics, and notification channels.
6. **Platform:** CI/CD to registry/environment, image scanning/signing, IaC, and staged deployments.
