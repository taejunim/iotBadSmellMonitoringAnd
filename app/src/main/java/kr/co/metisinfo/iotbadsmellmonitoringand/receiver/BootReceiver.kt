package kr.co.metisinfo.iotbadsmellmonitoringand.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kr.co.metisinfo.iotbadsmellmonitoringand.MainApplication


class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            var pushStatus = MainApplication.prefs.getBoolean("pushStatus", false)

            if (pushStatus) {
                MainApplication.instance.setAlarm()
            }
        }
    }
}