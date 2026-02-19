package com.xrayradar.android

import com.xrayradar.android.internal.Scope
import com.xrayradar.android.internal.model.BreadcrumbPayload
import org.junit.Assert.assertEquals
import org.junit.Test

class ScopeTest {
    @Test
    fun `breadcrumbs keep most recent max`() {
        val scope = Scope(maxBreadcrumbs = 2)
        scope.addBreadcrumb(BreadcrumbPayload("t1", "a"))
        scope.addBreadcrumb(BreadcrumbPayload("t2", "b"))
        scope.addBreadcrumb(BreadcrumbPayload("t3", "c"))

        val breadcrumbs = scope.getBreadcrumbs()
        assertEquals(2, breadcrumbs.size)
        assertEquals("b", breadcrumbs[0].message)
        assertEquals("c", breadcrumbs[1].message)
    }
}
