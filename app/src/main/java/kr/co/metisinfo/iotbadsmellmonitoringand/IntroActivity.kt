package kr.co.metisinfo.iotbadsmellmonitoringand

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

         MainApplication.instance.getApiData()

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

    override fun onPause() {
        super.onPause()
        finish()
    }
}