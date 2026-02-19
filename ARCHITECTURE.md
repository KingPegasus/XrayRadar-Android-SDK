# Architecture

## High-level flow

1. App initializes SDK with DSN + token.
2. Calls to `captureException`/`captureMessage` build event payloads.
3. Event payloads are persisted to local Room queue.
4. WorkManager worker flushes queued events via HTTP transport.
5. Transport posts to `/api/{project_id}/store/` with `X-Xrayradar-Token`.
6. Retryable failures (network, 5xx, 429) are backoff-scheduled.

## Components

- `XrayRadar` (public API)
- `Scope` (breadcrumbs + context state)
- `EventFactory` (payload creation + normalization)
- `QueueManager` + Room entities/DAO
- `SendEventsWorker` (flush + retry policy)
- `HttpTransport`
- Integrations:
  - `CrashHandler`
  - `NetworkBreadcrumbInterceptor`
  - `ActivityBreadcrumbsIntegration`
  - `LogBreadcrumbs`

## Compatibility contract

- Endpoint: `POST /api/{project_id}/store/`
- Header: `X-Xrayradar-Token`
- Payload fields: `event_id`, `timestamp`, `level`, `message`, `contexts`,
  `breadcrumbs`, `exception.values[*].stacktrace.frames`
