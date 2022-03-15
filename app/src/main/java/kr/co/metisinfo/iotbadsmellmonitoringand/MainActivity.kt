package kr.co.metisinfo.iotbadsmellmonitoringand

import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.navigation.NavigationView
import kr.co.metisinfo.iotbadsmellmonitoringand.databinding.ActivityMainBinding
import kr.co.metisinfo.iotbadsmellmonitoringand.databinding.NavigationViewHeaderBinding
import kr.co.metisinfo.iotbadsmellmonitoringand.model.RegisterModel
import kr.co.metisinfo.iotbadsmellmonitoringand.model.WeatherModel
import kr.co.metisinfo.iotbadsmellmonitoringand.util.Utils.Companion.convertToDp

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navigationView: NavigationView
    private lateinit var navigationBinding: NavigationViewHeaderBinding

    override fun initData() {

        var pushStatus = MainApplication.prefs.getBoolean("pushStatus", false)

        if (pushStatus) {
            instance.setAlarm()
        }
    }

    override fun initLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        Log.d("metis","MainActivity 시작")
        binding.includeHeader.textTitle.setText(R.string.main) // 타이틀 제목
        binding.includeHeader.backButton.visibility = View.GONE // 뒤로가기 버튼 안보이게
        binding.includeHeader.navigationViewButton.visibility = View.VISIBLE // 사이드 메뉴 버튼 보이게

        //현재 날씨 레이아웃 그리기
        drawWeatherLayout("-", "-", "-", "-", "", "")

        //네비게이션 뷰 그리기
        drawNavigationView()
    }

    override fun setOnClickListener() {
        binding.includeHeader.navigationViewButton.setOnClickListener {
            binding.navigationViewLayout.openDrawer(GravityCompat.START)
        }

        binding.weatherRefreshButton.setOnClickListener {
            getWeatherApiData() //현재 날씨 API
        }
    }

    /**
     * DATA CALLBACK
     */
    override fun callback(apiName: String, data: Any) {

        Log.d("metis", "callback data : $data")

        when (apiName) {
            "weather" -> {
                val weatherModel = data as WeatherModel
                drawWeatherLayout(weatherModel.temperature, weatherModel.humidity, weatherModel.windDirection, weatherModel.windSpeed, weatherModel.precipitationStatus, weatherModel.skyStatus)
            }
            "registerStatus" -> {
                val registerStatusList = data as List<RegisterModel>
                drawRegisterStatusLayout(registerStatusList) //접수 현황 레이아웃 그리기

                //악취 강도 레이아웃 그리기
                drawSmellIntensityLayout()
            }
        }
    }

    //현재 날씨 레이아웃 그리기
    private fun drawWeatherLayout(temperature: String, humidity: String, windDirection: String, windSpeed: String, precipitationStatus: String, skyStatus: String) {
        binding.temperatureText.text = resource.getString(R.string.main_temperature, temperature)
        binding.humidityText.text = resource.getString(R.string.main_humidity,humidity)
        binding.windDirectionText.text = resource.getString(R.string.main_wind, windDirection)
        binding.windSpeedText.text = resource.getString(R.string.main_wind_speed, windSpeed)

        val timeValue = getTimeForWeatherBackground() //날짜 배경색 가져오기

        when (timeValue) {
            0 -> binding.weatherBackground.setBackgroundResource(R.drawable.weather_background_dawn)
            1 -> binding.weatherBackground.setBackgroundResource(R.drawable.weather_background_morning)
            2 -> binding.weatherBackground.setBackgroundResource(R.drawable.weather_background_day)
            3 -> binding.weatherBackground.setBackgroundResource(R.drawable.weather_background_evening)
            4 -> binding.weatherBackground.setBackgroundResource(R.drawable.weather_background_night)
        }

        when (precipitationStatus) { // 강수형태가 0 이면 하늘상태 코드로

            "0" -> {
                when (skyStatus) {
                    //구름 없음
                    "1" -> {
                        when (timeValue) {
                            in 1 .. 2 -> { //낮
                                binding.currentWeatherImage.setBackgroundResource(R.drawable.weather_sun) //맑음
                            }
                            else -> { //밤
                                binding.currentWeatherImage.setBackgroundResource(R.drawable.weather_moon) //맑음
                            }
                        }
                    }
                    //구름 많음
                    "3" -> {
                        when (timeValue) {
                            in 1 .. 2 -> { //낮
                                binding.currentWeatherImage.setBackgroundResource(R.drawable.weather_cloud_sun) //구름많음
                            }
                            else -> { //밤
                                binding.currentWeatherImage.setBackgroundResource(R.drawable.weather_cloud_moon) //맑음
                            }
                        }
                    }
                    "4" -> binding.currentWeatherImage.setBackgroundResource(R.drawable.weather_cloud) //흐림
                }
            }
            "" -> binding.currentWeatherImage.setBackgroundResource(R.drawable.weather_sun) //기타
            else -> {
                when (precipitationStatus) {
                    "1" -> {
                        when (timeValue) {
                            in 1 .. 2 -> { //낮
                                binding.currentWeatherImage.setBackgroundResource(R.drawable.weather_rain) //비
                            }
                            else -> { //밤
                                binding.currentWeatherImage.setBackgroundResource(R.drawable.weather_shower_moon) //비
                            }
                        }
                    }
                    "2" -> binding.currentWeatherImage.setBackgroundResource(R.drawable.weather_sleet) //비/눈
                    "3" -> binding.currentWeatherImage.setBackgroundResource(R.drawable.weather_snow) //눈
                    "4", "5" -> {
                        when (timeValue) {
                            in 1 .. 2 -> { //낮
                                binding.currentWeatherImage.setBackgroundResource(R.drawable.weather_shower) //소나기
                            }
                            else -> { //밤
                                binding.currentWeatherImage.setBackgroundResource(R.drawable.weather_shower_moon) //소나기
                            }
                        }
                    }
                    "6" -> binding.currentWeatherImage.setBackgroundResource(R.drawable.weather_sleet) //빗방울/눈날림
                    "7" -> binding.currentWeatherImage.setBackgroundResource(R.drawable.weather_snow) //눈날림
                }
            }
        }
    }

    //접수 현황 레이아웃 그리기
    private fun drawRegisterStatusLayout(registerStatusList : List<RegisterModel>) {

        val registerStatusList = registerStatusList

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

        val intensityList = instance.intensityList
        var tempButton = Button(this)
        for (i in intensityList.indices) {

            val intensityButton = Button(this)

            val smellTypeText = intensityList[i].codeIdName + " / " + intensityList[i].codeComment

            val layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, convertToDp(30F))

            layoutParams.addRule(RelativeLayout.BELOW, tempButton.id)
            layoutParams.setMargins(convertToDp(30F),convertToDp(10F),convertToDp(30F),0)
            intensityButton.id = i+1
            intensityButton.layoutParams = layoutParams
            intensityButton.gravity = Gravity.LEFT or Gravity.CENTER
            intensityButton.setPadding(convertToDp(20F),0,0,0)
            intensityButton.setTextSize(TypedValue.COMPLEX_UNIT_SP,16F)
            intensityButton.setTextColor(Color.WHITE)
            intensityButton.setBackgroundResource(resource.getIdentifier("intensity_"+i+"_button", "drawable", "kr.co.metisinfo.iotbadsmellmonitoringand"))
            intensityButton.setOnClickListener {
                Log.d("metis", "text : " + intensityList[i].codeIdName + " / " + intensityList[i].codeComment)
                val intent = Intent(this, RegisterActivity::class.java)
                intent.putExtra("intensityId", intensityList[i].codeId)
                intent.putExtra("intensityText", smellTypeText)
                intent.putExtra("intensityResource", "intensity_"+i+"_button")
                startActivity (intent)
            }
            intensityButton.text = smellTypeText
            binding.intensityButtonLayout.addView(intensityButton)
            tempButton = intensityButton
        }
    }

    //네비게이션 뷰 그리기
    private fun drawNavigationView() {
        navigationView = binding.navigationView
        navigationBinding = DataBindingUtil.inflate(layoutInflater, R.layout.navigation_view_header, navigationView, true)
        navigationBinding.navigationUserName.text = MainApplication.prefs.getString("userName", "")
        navigationBinding.navigationUserId.text = MainApplication.prefs.getString("userId", "")
        navigationView.setNavigationItemSelectedListener(this)
    }

    //네비게이션 아이템 선택
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.navigation_view_history-> {
                Log.d("metis", "MyPageActivity clicked")
                var intent = Intent(this@MainActivity, HistoryActivity::class.java)
                startActivity(intent)
            }
            R.id.navigation_view_my_page-> {
                Log.d("metis", "MyPageActivity clicked")
                var intent = Intent(this@MainActivity, MyPageActivity::class.java)
                startActivity(intent)
            }
            R.id.navigation_view_logout-> {

                MainApplication.prefs.setBoolean("isLogin", false)
                MainApplication.prefs.setString("userId", "")
                MainApplication.prefs.setString("userName", "")
                MainApplication.prefs.setString("userPassword", "")

                var intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        binding.navigationViewLayout.closeDrawer(GravityCompat.START)
        return false
    }

    override fun onResume() {
        super.onResume()
        getWeatherApiData() //현재 날씨 API
        getUserTodayRegisterInfo() //접수 현황
    }
}