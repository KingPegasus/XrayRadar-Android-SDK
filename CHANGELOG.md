# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.3.0] - 2026-11-14

### Changed

- **OkHttp 5** — The SDK now depends on OkHttp 5.5.1 (was 4.12.0). Apps that pin OkHttp 4 will be upgraded to 5.x; the public OkHttp API is largely compatible, but check any custom interceptors or `Response.body` null handling.
- **Dependencies updated** — kotlinx-serialization 1.13.0, kotlinx-coroutines 1.11.0, WorkManager 2.12.0, Room 2.8.5.
- **Build toolchain** — Gradle 9.9.1, Android Gradle Plugin 9.5.1 (built-in Kotlin), Kotlin 2.4.21, KSP 2.3.13, vanniktech maven-publish 0.37.0, JaCoCo 0.8.16.
- **compileSdk 37** — Required by androidx.core 1.19. `minSdk` is unchanged (24).
- **Maven Central metadata** — POM project URL, organization and developer organization now point to [Rhyme Solutions](https://rhymesolutions.com); README credits Rhyme Solutions.
- **CI** — GitHub Actions updated to the latest Node 24 releases (checkout v7, setup-java v6, setup-android v4, upload-artifact v7, download-artifact v8); workflows install `platforms;android-37.0` and `build-tools;37.0.0`. The coverage badge commit no longer carries `[skip ci]`, which leaked into squash-merge commits and suppressed CI and tag-triggered publishing.

### Fixed

- **Broken repository links** — POM `scm` URLs, the issue template's security advisory link and the CONTRIBUTING clone instructions pointed to a non-existent repository (`xrayradar-android`); they now point to `XrayRadar-Android-SDK`.
- **CI SDK setup** — Removed the obsolete `tools` SDK package, which newer `sdkmanager` versions no longer provide and which made CI and publish workflows fail.
- **Coverage report** — JaCoCo now reads classes from AGP 9's built-in Kotlin output, so coverage is no longer reported as 0% after the toolchain upgrade.


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

[0.3.0]: https://github.com/KingPegasus/XrayRadar-Android-SDK/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/KingPegasus/XrayRadar-Android-SDK/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/KingPegasus/XrayRadar-Android-SDK/releases/tag/v0.1.0
