# Changelog

All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog and this project adheres to Semantic Versioning.

## [0.1.0] - 2026-02-18

### Added

- Initial Android SDK with:
  - `init`, `captureException`, `captureMessage`
  - Scope helpers (`setUser`, `setTag`, `setExtra`, `setContext`)
  - Breadcrumb support (`addBreadcrumb`, `clearBreadcrumbs`) and configurable sampling (`sampleRate`)
  - Crash capture via uncaught exception handler
  - Offline queue (Room) + retry sender (WorkManager)
  - HTTP transport for `POST /api/{project_id}/store/` with `X-Xrayradar-Token`
- Integrations:
  - `NetworkBreadcrumbInterceptor`
  - `ActivityBreadcrumbsIntegration`
  - `LogBreadcrumbs`
- Tests:
  - DSN parsing, scope limits, payload serialization/truncation, stacktrace mapping, transport path/header checks, backoff
- CI:
  - Unit tests and JaCoCo coverage artifact upload
