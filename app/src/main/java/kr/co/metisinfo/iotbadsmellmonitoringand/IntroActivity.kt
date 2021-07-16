package kr.co.metisinfo.iotbadsmellmonitoringand

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.databinding.DataBindingUtil
import kr.co.metisinfo.iotbadsmellmonitoringand.databinding.ActivityIntroBinding

class IntroActivity : BaseActivity() {

    lateinit var fadeInAnimation: Animation
    private lateinit var binding: ActivityIntroBinding

    override fun initLayout() {

        binding = DataBindingUtil.setContentView(this, R.layout.activity_intro)

        fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.intro_fade_in)

        binding.introTitle.startAnimation(fadeInAnimation)
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
            }, 2000)
        }
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}