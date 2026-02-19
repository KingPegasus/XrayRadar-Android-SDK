# OkHttp integration example

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(NetworkBreadcrumbInterceptor())
    .build()
```

The interceptor records request breadcrumbs with:

- method
- sanitized URL (query removed)
- status code
- duration
