# Contributing

Thanks for contributing to XrayRadar Android SDK.

## Prerequisites

- JDK 17
- Android SDK (compileSdk 35)
- Gradle 8.10+

## Setup

```bash
git clone <repo-url>
cd xrayradar-android
```

## Build and test

```bash
gradle :library:testDebugUnitTest
gradle :library:jacocoTestReport
```

## Publish (Sonatype/Maven Central)

Publishing uses `maven-publish` + `signing` from `library/build.gradle.kts`.

Set credentials via environment variables or Gradle properties:

- `SONATYPE_USERNAME` / `SONATYPE_PASSWORD`
- `SIGNING_KEY` / `SIGNING_PASSWORD` (ASCII-armored private key + passphrase)

Then publish:

```bash
gradle :library:publishReleasePublicationToSonatypeRepository
```

Use a `-SNAPSHOT` version to publish to Sonatype snapshots.

CI workflow:

- `.github/workflows/publish.yml`
  - tag `v*` -> publishes to Sonatype
  - manual dispatch supports dry-run mode

## Sample app

```bash
gradle :sample:assembleDebug
```

## Coding guidelines

- Keep public API small and stable.
- Maintain server ingest compatibility (`POST /api/{project_id}/store/`).
- Add tests for new behavior and edge cases.

## Pull requests

- Update docs and changelog for user-facing changes.
- Ensure CI passes.
