package com.hitchy.core.platform

// TODO(P14-A01): Firebase Analytics and real event logging
class NoOpAnalytics : Analytics {
    override fun log(event: String, params: Map<String, Any>) {
        // No-op until P14-A01
    }
}
