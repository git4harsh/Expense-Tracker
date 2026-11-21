<!-- Copyright (c) 2026 Harsh (git4harsh). All rights reserved. -->
# Entity Relationship Diagram

```mermaid
erDiagram
    APP_USERS ||--o{ EXPENSES : owns
    APP_USERS ||--o{ CATEGORIES : owns
    APP_USERS ||--o{ REFRESH_TOKENS : has
    CATEGORIES ||--o{ EXPENSES : categorizes
    APP_USERS ||--o{ SECURITY_AUDIT_EVENTS : "audited by email"

    APP_USERS {
        bigint id PK
        varchar email UK "unique, not null"
        varchar name "not null"
        varchar passwordHash "not null"
        timestamp createdAt "not null"
    }

    CATEGORIES {
        bigint id PK
        varchar name "not null"
        bigint owner_id FK "app_users.id"
    }

    EXPENSES {
        bigint id PK
        varchar title "not null"
        varchar description
        decimal amount "not null"
        date expenseDate "not null"
        bigint owner_id FK "app_users.id"
        bigint category_id FK "categories.id, nullable"
    }

    REFRESH_TOKENS {
        bigint id PK
        bigint user_id FK "app_users.id"
        varchar tokenHash UK "SHA-256 hash, not null"
        timestamp expiresAt "not null"
        timestamp createdAt "not null"
        timestamp revokedAt "nullable"
        varchar replacedByTokenHash "nullable (rotation)"
        varchar reason "nullable"
    }

    SECURITY_AUDIT_EVENTS {
        bigint id PK
        varchar eventType "e.g. AUTH_LOGIN, PASSWORD_CHANGE"
        varchar userEmail "indexed"
        varchar ipAddress
        boolean success
        varchar details
        timestamp createdAt "not null"
    }
```

## Notes

- **Ownership:** expenses and categories are scoped to the authenticated user; repository/service logic enforces owner checks.
- **Categories:** optional on expenses (`category_id` nullable) to allow uncategorized spending.
- **Refresh tokens:** stored as hashes (never plaintext), support expiry, revocation, rotation tracking, and reuse-detection response (revoking active tokens on suspicious reuse).
- **Audit events:** persisted for key security actions (register/login/refresh/logout, profile/password changes) to support monitoring and incident review. Password reset flows are not yet implemented; if added, they should emit audit events for request, token issuance, completion, and failures.
- Schema is currently managed by Hibernate `ddl-auto` for local/dev convenience; production deployments should use versioned migrations (Flyway/Liquibase) with explicit indexes/constraints.
