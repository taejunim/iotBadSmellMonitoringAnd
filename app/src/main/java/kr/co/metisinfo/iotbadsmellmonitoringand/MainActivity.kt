package kr.co.metisinfo.iotbadsmellmonitoringand

import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import kr.co.metisinfo.iotbadsmellmonitoringand.databinding.ActivityMainBinding
import kr.co.metisinfo.iotbadsmellmonitoringand.util.Utils.Companion.convertToDp

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private val instance = MainApplication.instance

    override fun initLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        Log.d("metis","MainActivity 시작")
        binding.includeHeader.textTitle.setText(R.string.main) // 타이틀 제목
        binding.includeHeader.backButton.visibility = View.GONE // 뒤로가기 버튼 안보이게
        binding.includeHeader.sideMenuButton.visibility = View.VISIBLE // 사이드 메뉴 버튼 보이게

        binding.temperatureText.text = resource.getString(R.string.main_temperature, instance.weatherDataMap["temperature"])
        binding.humidityText.text = resource.getString(R.string.main_humidity,instance.weatherDataMap["humidity"])
        binding.windDirectionText.text = resource.getString(R.string.main_wind, instance.weatherDataMap["windDirection"])
        binding.windSpeedText.text = resource.getString(R.string.main_wind_speed, instance.weatherDataMap["windSpeed"])

        //접수 현황 레이아웃 그리기
        drawRegisterStatusLayout()

        //악취 강도 레이아웃 그리기
        drawSmellIntensityLayout()
    }

    override fun setOnClickListener() {
        binding.includeHeader.sideMenuButton.setOnClickListener {
            Log.d("metis","MainActivity - Side Menu 구현해야 ")
        }
    }

    override fun initData() {
    }

    //접수 현황 레이아웃 그리기
    private fun drawRegisterStatusLayout() {

        val registerStatusList = instance.registerStatusList

        var imageView : ImageView? = null
        var textView : TextView? = null

        for (i in registerStatusList.indices) {

            when (i) {
                0 -> {
                    imageView = binding.registerFirstTimeImage
                    textView = binding.registerFirstTimeText
                }
                1 -> {
                    imageView = binding.registerSecondTimeImage
                    textView = binding.registerSecondTimeText
                }
                2 -> {
                    imageView = binding.registerThirdTimeImage
                    textView = binding.registerThirdTimeText
                }
                3 -> {
                    imageView = binding.registerFourthTimeImage
                    textView = binding.registerFourthTimeText
                }
            }

            when (registerStatusList[i].resultCode) {
                "001" -> imageView?.setBackgroundResource(R.drawable.done)
                "002" -> imageView?.setBackgroundResource(R.drawable.not_done)
                "003" -> imageView?.setBackgroundResource(R.drawable.not_yet)
            }

            textView?.text = registerStatusList[i].smellRegisterTimeName
        }
    }

    //악취 강도 레이아웃 그리기
    private fun drawSmellIntensityLayout() {

        val smellTypeList = instance.smellTypeList
        var tempButton = Button(this)
        for (i in smellTypeList.indices) {

            val intensityButton = Button(this)

            val layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, convertToDp(30F))

            layoutParams.addRule(RelativeLayout.BELOW, tempButton!!.id)
            layoutParams.setMargins(convertToDp(30F),convertToDp(10F),convertToDp(30F),0)
            intensityButton.id = i+1
            intensityButton.layoutParams = layoutParams
            intensityButton.gravity = Gravity.LEFT or Gravity.CENTER
            intensityButton.setPadding(convertToDp(20F),0,0,0)
            intensityButton.setTextSize(TypedValue.COMPLEX_UNIT_SP,16F)
            intensityButton.setTextColor(Color.WHITE)
            intensityButton.setBackgroundResource(resource.getIdentifier("intensity_"+i+"_button", "drawable", "kr.co.metisinfo.iotbadsmellmonitoringand"))
            intensityButton.setOnClickListener {
                Log.d("metis", "text : " + smellTypeList[i].codeIdName + " / " + smellTypeList[i].codeComment)
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity (intent)
            }
            intensityButton.text = smellTypeList[i].codeIdName + " / " + smellTypeList[i].codeComment
            binding.intensityButtonLayout.addView(intensityButton)
            tempButton = intensityButton
        }
    }
}