package kr.co.metisinfo.iotbadsmellmonitoringand

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.databinding.DataBindingUtil
import kr.co.metisinfo.iotbadsmellmonitoringand.databinding.ActivityIntroBinding

class IntroActivity : BaseActivity() {

    lateinit var fadeInAnimation: Animation
    private lateinit var binding: ActivityIntroBinding

    var succeededApiCount = 0

    override fun initLayout() {

        binding = DataBindingUtil.setContentView(this, R.layout.activity_intro)

        fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.intro_fade_in)

        binding.introTitle.startAnimation(fadeInAnimation)
    }

    override fun setOnClickListener() {

    }

    override fun initData() {
        getApiData()
        getNoticeInfo()
    }

    override fun callback(apiName: String, data: Any) {

        if (apiName == "baseData") {

            if (data == "success") {
                succeededApiCount++

                if (succeededApiCount == instance.codeGroupArray.lastIndex + 1) {
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

            //API 응답 실패
            else if (data == "fail") {
                instance.finish(this@IntroActivity)
            }
        }

        //공지사항 API 오류
        else if (apiName == "noticeInfo" && data == "fail") {
            instance.finish(this@IntroActivity)
        }

        //서버 무응답
        else if (apiName == "noResponse") {
            instance.finish(this@IntroActivity)
        } else {
            instance.finish(this@IntroActivity)
        }
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}