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

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed ({
            val intent = Intent(this, LoginActivity::class.java)
            //val intent = Intent(this, MainActivity::class.java)
            startActivity (intent)
        }, 500)
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}