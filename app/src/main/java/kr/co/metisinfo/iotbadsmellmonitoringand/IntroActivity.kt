package kr.co.metisinfo.iotbadsmellmonitoringand

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class IntroActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)
    }

    override fun initLayout() {

    }

    override fun setOnClickListener() {

    }

    override fun initData() {
        getApiData()
    }

    override fun callback(apiName: String, data: Any) {
        if (apiName == "baseData") {
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed ({

                var intent: Intent? = when (MainApplication.prefs.getBoolean("isLogin", false)) {
                    true -> Intent(this, MainActivity::class.java)
                    false -> Intent(this, LoginActivity::class.java)
                }

                //val intent = Intent(this, HistoryActivity::class.java)
                //val intent = Intent(this, MainActivity::class.java)
                startActivity (intent)
            }, 500)
        }
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}