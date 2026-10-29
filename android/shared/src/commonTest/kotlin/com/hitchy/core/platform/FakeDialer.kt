package com.hitchy.core.platform

class FakeDialer : Dialer {
    var dialedNumber: String? = null
    var smsNumber: String? = null
    var smsBody: String? = null

    override fun dial(number: String) {
        dialedNumber = number
    }

    override fun sms(number: String, body: String) {
        smsNumber = number
        smsBody = body
    }
}
