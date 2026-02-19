# XrayRadar Android SDK

[![CI](https://img.shields.io/github/actions/workflow/status/KingPegasus/xrayradar-android/ci.yml?label=CI&style=flat-square)](https://github.com/KingPegasus/XrayRadar-Android-SDK/blob/main/.github/workflows/ci.yml)
![Android API](https://img.shields.io/badge/minSdk-24-blue?style=flat-square)
![Kotlin](https://img.shields.io/badge/kotlin-2.1.x-7f52ff?style=flat-square)
[![License](https://img.shields.io/badge/license-MIT-brightgreen?style=flat-square)](https://github.com/KingPegasus/XrayRadar-Android-SDK/blob/main/LICENSE)

Android SDK for sending crash and error telemetry to XrayRadar with offline queue + retry support.

**Supported:** Android minSdk 24+, Kotlin 2.1.x, Java 17.

## Features

- Crash capture (`Thread.setDefaultUncaughtExceptionHandler` wrapper)
- Manual `captureException` and `captureMessage`
- Breadcrumbs (`addBreadcrumb`) and context helpers (`setUser`, `setTag`, `setExtra`, `setContext`)
- Offline queue via Room + retry sender via WorkManager
- Integrations:
  - `NetworkBreadcrumbInterceptor`
  - `ActivityBreadcrumbsIntegration`
  - `LogBreadcrumbs`

## Install

```kotlin
dependencies {
  implementation("com.xrayradar:xrayradar-android:0.1.0")
}
```

## Quick start (60 seconds)

```kotlin
XrayRadar.init(
    context = applicationContext,
    options = XrayRadarOptions(
        dsn = "https://xrayradar.com/1",
        authToken = "your-token",
        environment = "production",
        release = BuildConfig.VERSION_NAME,
        serverName = "android-client",
    ),
)

XrayRadar.captureMessage("App started", level = "info")
```

## Core API

- `XrayRadar.captureException(throwable, level = "error", message = null)`
- `XrayRadar.captureMessage(message, level = "error")`
- `XrayRadar.addBreadcrumb(...)`
- `XrayRadar.setUser(...)`, `setTag(...)`, `setExtra(...)`, `setContext(...)`
- `XrayRadar.flush()`
- `XrayRadar.close()`

## Integrations

### Network breadcrumbs

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(NetworkBreadcrumbInterceptor())
    .build()
```

### Activity lifecycle breadcrumbs

```kotlin
ActivityBreadcrumbsIntegration.install(application)
```

### Log breadcrumbs

```kotlin
LogBreadcrumbs.install()
LogBreadcrumbs.i("Checkout", "Started checkout")
```

## How delivery works

1. SDK builds event payload.
2. Event is persisted to local Room queue.
3. WorkManager worker flushes queued events when network is available.
4. Retryable failures (network/5xx/429) are backoff-scheduled.

## Privacy defaults

- Query strings are stripped from network breadcrumb URLs.
- Request/response bodies are not captured by network breadcrumbs.
- Device identifiers are not collected by default.

## Transport contract

- Endpoint: `POST /api/{project_id}/store/`
- Header: `X-Xrayradar-Token`
- Payload includes `event_id`, `timestamp`, `level`, `message`, `contexts`, `breadcrumbs`, and optional `exception.values[*].stacktrace.frames`.

## Production checklist

- Use production DSN and token.
- Set `environment` and `release`.
- Verify network permissions and connectivity.
- Keep token out of logs and public source code.

## Troubleshooting

- `401/403`: token or project access issue.
- `429`: server-side rate limiting; SDK retries with backoff.
- No events visible: call `XrayRadar.flush()` and inspect worker/network constraints.

## Development docs

- [Changelog](https://github.com/KingPegasus/XrayRadar-Android-SDK/blob/main/CHANGELOG.md)
- [Architecture](https://github.com/KingPegasus/XrayRadar-Android-SDK/blob/main/ARCHITECTURE.md)
- [Testing](https://github.com/KingPegasus/XrayRadar-Android-SDK/blob/main/TESTING.md)
- [Contributing](https://github.com/KingPegasus/XrayRadar-Android-SDK/blob/main/CONTRIBUTING.md)
- [Security](https://github.com/KingPegasus/XrayRadar-Android-SDK/blob/main/SECURITY.md)
- [Examples](https://github.com/KingPegasus/XrayRadar-Android-SDK/blob/main/examples/README.md)
