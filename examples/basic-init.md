# Basic init example

```kotlin
XrayRadar.init(
    context = applicationContext,
    options = XrayRadarOptions(
        dsn = "https://xrayradar.com/1",
        authToken = "your-token",
        environment = "production",
        release = BuildConfig.VERSION_NAME,
    ),
)

XrayRadar.captureMessage("App started", level = "info")
```
