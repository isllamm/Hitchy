package com.hitchy.core.platform

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

class AndroidDialer(
    private val context: Context,
    private val activityProvider: ActivityProvider? = null,
) : Dialer {

    override fun dial(number: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = "tel:$number".toUri()
        }
        startIntent(intent)
    }

    override fun sms(number: String, body: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "smsto:$number".toUri()
            putExtra("sms_body", body)
        }
        startIntent(intent)
    }

    private fun startIntent(intent: Intent) {
        val currentActivity = activityProvider?.currentActivity
        if (currentActivity != null) {
            currentActivity.startActivity(intent)
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}
