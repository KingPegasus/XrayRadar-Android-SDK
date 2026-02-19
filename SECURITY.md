# Security Policy

## Checking for vulnerabilities

- **Dependencies:** Dependabot runs weekly and opens PRs for vulnerable Gradle and GitHub Actions dependencies. Review and merge those updates.
- **Manual dependency check:** From the project root, list dependency trees and inspect for known issues:
  - `./gradlew :library:dependencies` (and `:sample:dependencies` if needed)
  - Optionally add the [OWASP Dependency-Check Gradle plugin](https://github.com/dependency-check/dependency-check-gradle) and run `dependencyCheckAnalyze` for a CVE report (first run downloads NVD data and can take several minutes).
- **Code:** Use Android Studio’s built-in inspections and keep dependencies (OkHttp, Room, WorkManager, etc.) updated to supported versions.

## Reporting a vulnerability

Please report security issues privately and do not open a public issue.

- Email: dev@xrayradar.com
- Include:
  - A clear description
  - Reproduction steps
  - Potential impact
  - Any suggested mitigation

We will acknowledge receipt and follow up with triage status.

## Scope

This SDK handles client-side error capture and event transport. Please report:

- Token handling issues
- Sensitive data leakage risks
- Transport/auth bypasses
- Dependency-related vulnerabilities affecting runtime behavior
