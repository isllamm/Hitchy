package com.hitchy.core.platform

interface Dialer {
    fun dial(number: String)
    fun sms(number: String, body: String)
}
