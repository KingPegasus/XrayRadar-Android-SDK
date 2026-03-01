# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.2.0] - 2026-03-02

### Added

- **Transport debug logger** — `XrayRadar.setTransportDebugLogger(logger)` to optionally log API (transport) requests and responses for debugging.
- **Sample app** — In-app log panel showing SDK activity and API interaction logs; buttons for crash, capture exception/message, breadcrumb, flush, and HTTP request.
- **CI coverage badge** — README coverage badge (shields.io) plus `update-badges` job on pull requests that updates the badge from JaCoCo results.
- **Expanded test coverage** — Tests for `EventFactory`, `TransportDebugLog`, `NetworkBreadcrumbInterceptor`, `Backoff` (extra cases), `StacktraceMapper` (empty stack, frame fields), `CrashHandler` (capture throws), `TruncatePayload` (edge cases), and `PayloadSerialization` (exception/fingerprint). CONTRIBUTING.md documents test vs coverage report paths and view commands.


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

[0.2.0]: https://github.com/KingPegasus/XrayRadar-Android-SDK/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/KingPegasus/XrayRadar-Android-SDK/releases/tag/v0.1.0
