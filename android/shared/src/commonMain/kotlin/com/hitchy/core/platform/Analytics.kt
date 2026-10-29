package com.hitchy.core.platform

interface Analytics {
    fun log(event: String, params: Map<String, Any> = emptyMap())
}
