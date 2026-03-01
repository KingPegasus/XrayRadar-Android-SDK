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
./gradlew :library:testDebugUnitTest
./gradlew :library:jacocoTestReport
```

Run both in one go:

```bash
./gradlew :library:testDebugUnitTest :library:jacocoTestReport
```

### Test report vs coverage report

| Report | Path | What it shows |
|--------|------|----------------|
| **Test results** | `library/build/reports/tests/testDebugUnitTest/index.html` | Which tests passed/failed and duration. No coverage. |
| **Coverage (JaCoCo)** | `library/build/reports/jacoco/jacocoTestReport/html/index.html` | Line/branch coverage by package and class. |

View the **test report** in a browser:

```bash
xdg-open library/build/reports/tests/testDebugUnitTest/index.html
# or: python3 -m http.server 8080 --directory library/build/reports/tests/testDebugUnitTest
```

View the **coverage report** in a browser:

```bash
xdg-open library/build/reports/jacoco/jacocoTestReport/html/index.html
# or: python3 -m http.server 8080 --directory library/build/reports/jacoco/jacocoTestReport/html
```

## Publish (Sonatype/Maven Central)

Publishing uses `maven-publish` + `signing` from `library/build.gradle.kts`.

Set credentials via environment variables or Gradle properties:

- `SONATYPE_USERNAME` / `SONATYPE_PASSWORD`
- `SIGNING_KEY` / `SIGNING_PASSWORD` (ASCII-armored private key + passphrase)

Then publish (Central Portal):

```bash
./gradlew :library:publishToMavenCentral
```

Set `ORG_GRADLE_PROJECT_mavenCentralUsername`, `ORG_GRADLE_PROJECT_mavenCentralPassword`, `ORG_GRADLE_PROJECT_signingInMemoryKey`, and `ORG_GRADLE_PROJECT_signingInMemoryKeyPassword` (or use `gradle.properties`). Use a `-SNAPSHOT` version to publish to Central snapshots.

CI workflow:

- `.github/workflows/publish.yml`
  - tag `v*` -> publishes to Maven Central (Central Portal)
  - manual dispatch supports dry-run mode

## Sample app

```bash
./gradlew :sample:assembleDebug
```

## Coding guidelines

- Keep public API small and stable.
- Maintain server ingest compatibility (`POST /api/{project_id}/store/`).
- Add tests for new behavior and edge cases.

## Pull requests

- Update docs and changelog for user-facing changes.
- Ensure CI passes.
