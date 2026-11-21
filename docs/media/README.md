<!-- Copyright (c) 2026 Harsh (git4harsh). All rights reserved. -->
# Demo media (screenshots / GIFs)

This folder is reserved for reviewer-facing demo artifacts referenced by the main README.

## Recommended captures

1. **Swagger auth + API flow** (`swagger-flow.gif` or screenshots)
   - Register/login, click **Authorize**, create category/expense, fetch reports.
   - URL: `http://localhost:8080/swagger-ui.html`

2. **Grafana dashboard** (`grafana-dashboard.png` / `.gif`)
   - Show Prometheus-scraped API metrics/dashboard panels.
   - URL: `http://localhost:3000`

3. **End-to-end happy path** (`happy-path.gif`, optional)
   - Terminal/Swagger actions showing expense creation and resulting metrics/logs.

## Capture tips

- Start the stack with `docker compose up --build` (see root [README](../../README.md)).
- Use a fresh demo user and sample expenses so charts/reports show data.
- Redact secrets/tokens before saving media.
- Keep files reasonably sized for GitHub viewing (GIF/PNG/WebP).

Add the files here and keep filenames consistent with links in the README.
