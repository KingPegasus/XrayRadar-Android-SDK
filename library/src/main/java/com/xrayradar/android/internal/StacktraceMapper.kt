package com.xrayradar.android.internal

import com.xrayradar.android.internal.model.StackFramePayload

internal fun mapStacktrace(throwable: Throwable, packageName: String): List<StackFramePayload> {
    return throwable.stackTrace.map {
        val file = it.fileName ?: "unknown"
        val cls = it.className
        StackFramePayload(
            filename = file,
            function = "${cls}.${it.methodName}",
            lineno = it.lineNumber,
            absPath = "$cls:$file",
            inApp = cls.startsWith(packageName),
        )
    }
}
