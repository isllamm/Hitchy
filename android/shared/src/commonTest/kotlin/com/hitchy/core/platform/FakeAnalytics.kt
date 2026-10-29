package com.hitchy.core.platform

class FakeAnalytics : Analytics {
    data class Event(val name: String, val params: Map<String, Any>)

    val loggedEvents = mutableListOf<Event>()

    override fun log(event: String, params: Map<String, Any>) {
        loggedEvents.add(Event(event, params))
    }
}
