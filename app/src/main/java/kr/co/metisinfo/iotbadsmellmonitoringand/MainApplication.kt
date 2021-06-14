package kr.co.metisinfo.iotbadsmellmonitoringand

import android.app.Application
import android.content.Context

class MainApplication : Application() {

    init {
        instance = this
    }

    companion object {
        lateinit var instance: MainApplication
        fun getContext() : Context {
            return instance.applicationContext
        }
    }
}