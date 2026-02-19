package com.xrayradar.android.internal

import java.time.Instant

internal fun nowIsoUtc(): String = Instant.now().toString()
