package kr.co.metisinfo.iotbadsmellmonitoringand

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.navigation.NavigationView
import kr.co.metisinfo.iotbadsmellmonitoringand.adapter.ViewPagerAdapter
import kr.co.metisinfo.iotbadsmellmonitoringand.databinding.ActivityMainBinding
import kr.co.metisinfo.iotbadsmellmonitoringand.databinding.NavigationViewHeaderBinding
import kr.co.metisinfo.iotbadsmellmonitoringand.model.LoginResult
import kr.co.metisinfo.iotbadsmellmonitoringand.model.RegionMaster
import kr.co.metisinfo.iotbadsmellmonitoringand.model.UserModel
import kr.co.metisinfo.iotbadsmellmonitoringand.model.WeatherModel
import kr.co.metisinfo.iotbadsmellmonitoringand.util.Utils.Companion.convertToDp
import kr.co.metisinfo.iotbadsmellmonitoringand.util.Utils.Companion.getPercentWidth
import kr.co.metisinfo.iotbadsmellmonitoringand.util.Utils.Companion.ymdFormatter
import me.relex.circleindicator.CircleIndicator3
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navigationView: NavigationView
    private lateinit var navigationBinding: NavigationViewHeaderBinding

    private lateinit var viewPager: ViewPager2
    private lateinit var viewPagerAdapter: FragmentStateAdapter
    private val pageNumber = 2
    private lateinit var indicator: CircleIndicator3

    var dialog: Dialog? = null

    var fontSize = 16F

    override fun initData() {

    }

    override fun initLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
    }

    fun drawLayout() {

        binding.includeHeader.textTitle.setText(R.string.main) // 타이틀 제목
        binding.includeHeader.backButton.visibility = View.GONE // 뒤로가기 버튼 안보이게
        binding.includeHeader.navigationViewButton.visibility = View.VISIBLE // 사이드 메뉴 버튼 보이게

        //가입시 설정한 지역 텍스트 set
        try {
            if (instance.regionList.isNotEmpty() && MainApplication.prefs.getString("userRegionMaster", "") != "") {
                val regionMasterText = instance.regionList.filter { it.mCodeId == MainApplication.prefs.getString("userRegionMaster", "")}.map(RegionMaster::mCodeIdName)[0]

                binding.currentWeatherText.text = resource.getString(R.string.main_current_weather, regionMasterText) // 타이틀 제목
            }
        } catch (e: IndexOutOfBoundsException) {
            Log.d("metis", "지역 데이터 없음 ")
            binding.currentWeatherText.text = instance.defaultRegionTitle + " 현재 날씨"
        }

        //현재 날씨 레이아웃 그리기
        drawWeatherLayout("-", "-", "-", "-", "", "")

        //네비게이션 뷰 그리기
        drawNavigationView()

        //공지사항 시간 체크
        checkNoticeTime()

        //금일 악취 접수 현황, 우리동네 악취 현황 viewPager
        setViewPager()

        //악취 강도 레이아웃 그리기
        drawSmellIntensityLayout(fontSize)

        binding.includeHeader.navigationViewButton.setOnClickListener {
            binding.navigationViewLayout.openDrawer(GravityCompat.START)
        }

        binding.weatherRefreshButton.setOnClickListener {
            showLoading(binding.loading)
            getWeatherApiData() //현재 날씨 API
        }
    }

    override fun setOnClickListener() {

    }

    /**
     * DATA CALLBACK
     */
    override fun callback(apiName: String, data: Any) {

        if (apiName == "checkAccount") {
            if (data == "success") {
                if (checkFoldedDisplay() < 900) {
                    fontSize = 14F
                } else {
                    fontSize = 16F
                }

                //알람 허용
                if (NotificationManagerCompat.from(this@MainActivity).areNotificationsEnabled()) {
                    val pushStatus = MainApplication.prefs.getBoolean("pushStatus", false)
                    if (pushStatus) {
                        instance.setAlarm()
                    } else {
                        instance.cancelAlarm()
                    }
                }

                //알람 거부 -> 푸시 허용하든 거부하든 알람 취소
                else {
                    MainApplication.prefs.setBoolean("pushStatus", false)
                    MainApplication.instance.cancelAlarm()
                }

                showLoading(binding.loading)

                Log.d("isLogin", "485 :isLogin : " + MainApplication.prefs.getBoolean("isLogin", false))
                Log.d("isLogin", "485 :userId : " + MainApplication.prefs.getString("userId", ""))

                getApiData()
            } //API 응답 실패
            else if (data == "fail") {
                instance.finish(this@MainActivity)
            }
        }
        else
        if (apiName == "baseData") {

            if (data == "success") {
                succeededApiCount++

                if (succeededApiCount == instance.codeGroupArray.lastIndex + 1) {
                    getNoticeInfo()
                    succeededApiCount = 0
                }
            }

            //API 응답 실패
            else if (data == "fail") {
                instance.finish(this@MainActivity)
            }
        }

        else if (apiName == "noticeInfo" && data == "success") {

            hideLoading(binding.loading)

            drawLayout()

            getCoordinates() //기상청 데이터를 가져오기 위한 x,y 좌표 API
        }

        //공지사항 API 오류
        else if (apiName == "noticeInfo" && data == "fail") {
            instance.finish(this@MainActivity)
        }

        //가입시 설정한 사용자의 X,Y 좌표
        else if (apiName == "coordinates") {
            getWeatherApiData() //현재 날씨 API
        }

        //가입시 설정한 사용자의 X,Y 좌표
        else if (apiName == "weather") {

            //drawLayout()
            hideLoading(binding.loading)
            val weatherModel = data as WeatherModel
            drawWeatherLayout(weatherModel.temperature, weatherModel.humidity, weatherModel.windDirection, weatherModel.windSpeed, weatherModel.precipitationStatus, weatherModel.skyStatus)
        }
        //서버 무응답
        else if (apiName == "noResponse") {
            instance.finish(this@MainActivity)
        } else {
            instance.finish(this@MainActivity)
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

    //악취 강도 레이아웃 그리기
    private fun drawSmellIntensityLayout(fontSize: Float) {

        val intensityList = instance.intensityList
        var tempButton = Button(this)
        for (i in intensityList.indices) {

            val intensityButton = Button(this)

            val smellTypeText = intensityList[i].codeIdName + " / " + intensityList[i].codeComment

            val layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, convertToDp(30F))

            layoutParams.addRule(RelativeLayout.BELOW, tempButton.id)

            if (i == intensityList.lastIndex) {
                layoutParams.setMargins(convertToDp(30F),convertToDp(10F),convertToDp(30F),convertToDp(10F))
            } else {
                layoutParams.setMargins(convertToDp(30F),convertToDp(10F),convertToDp(30F),0)
            }

            intensityButton.id = i+1
            intensityButton.layoutParams = layoutParams
            intensityButton.gravity = Gravity.LEFT or Gravity.CENTER
            intensityButton.setPadding(convertToDp(20F),0,0,0)
            intensityButton.setTextSize(TypedValue.COMPLEX_UNIT_SP,fontSize)
            intensityButton.setTextColor(Color.WHITE)
            intensityButton.setBackgroundResource(resource.getIdentifier("intensity_"+i+"_button", "drawable", "kr.co.metisinfo.iotbadsmellmonitoringand"))
            intensityButton.setOnClickListener {
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

    //공지사항 시간 체크
    private fun checkNoticeTime() {

        //공지사항이 없으면 표출 안함
        if (instance.noticeModel.noticeTitle.isNotEmpty() && instance.noticeModel.noticeContents.isNotEmpty()) {

            val doNotShowUpNoticeTime = MainApplication.prefs.getLong("doNotShowUpNoticeTime", 0) //"오늘 하루 동안 보지 않기" 를 클릭한 데이터 메모리에 저장

            if (doNotShowUpNoticeTime != "0") {

                val storedDate = ymdFormatter.format(doNotShowUpNoticeTime.toLong())
                val currentDate = ymdFormatter.format(Date())

                //저장된 일시와 현재 일시 다르면 초기화 후 공지사항 표출
                if (storedDate != currentDate) {

                    MainApplication.prefs.setLong("doNotShowUpNoticeTime", 0)
                    popUpNoticeDialog()
                }

            } else {
                popUpNoticeDialog()
            }
        }
    }

    //금일 악취 접수 현황, 우리동네 악취 현황 viewPager
    private fun setViewPager() {

        viewPager = binding.viewPager

        viewPagerAdapter = ViewPagerAdapter(this, pageNumber)
        viewPager.adapter = viewPagerAdapter

        //viewPager 하단 indicator
        indicator = binding.indicator
        indicator.setViewPager(viewPager)
        indicator.createIndicators(pageNumber,0)

        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        viewPager.currentItem = 0

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                if (positionOffsetPixels == 0) {
                    viewPager.currentItem = position
                }
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                indicator.animatePageSelected(position % pageNumber)
            }
        })
    }

    //공지사항 표출
    private fun popUpNoticeDialog() {

        dialog = Dialog(this@MainActivity)

        val inf = layoutInflater
        val dialogView: View = inf.inflate(R.layout.dialog_notice, null)
        val noticeTitle = dialogView.findViewById<TextView>(R.id.notice_title)
        val noticeContents = dialogView.findViewById<TextView>(R.id.notice_contents)
        val doNotShowUpButton = dialogView.findViewById<LinearLayout>(R.id.do_not_show_up_button)
        val confirmationButton = dialogView.findViewById<LinearLayout>(R.id.confirmation_button)

        noticeTitle.text = instance.noticeModel.noticeTitle
        noticeContents.text = instance.noticeModel.noticeContents

        doNotShowUpButton.setOnClickListener { v: View? ->
            MainApplication.prefs.setLong("doNotShowUpNoticeTime", Date().time)
            dialog?.dismiss()
        }

        confirmationButton.setOnClickListener { v: View? ->
            dialog?.dismiss()
        }

        dialog?.setContentView(dialogView) // Dialog에 선언했던 layout 적용
        dialog?.setCancelable(false) // 외부 터치나 백키로 dimiss 시키는 것 막음
        dialog?.window?.setBackgroundDrawableResource(R.drawable.notice_background) // 팝업 다이얼로그 둥근 모서리 백그라운드 적용
        dialog?.window?.setLayout(getPercentWidth(this, 90), WindowManager.LayoutParams.WRAP_CONTENT) //너비를 기기의 60% 적용, 높이는 내용 크기에 맞게
        dialog?.show()
    }

    //네비게이션 아이템 선택
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.navigation_view_history-> {
                var intent = Intent(this@MainActivity, HistoryActivity::class.java)
                startActivity(intent)
            }
            R.id.navigation_view_my_page-> {
                var intent = Intent(this@MainActivity, MyPageActivity::class.java)
                startActivity(intent)
            }
            R.id.navigation_view_logout-> {
                MainApplication.prefs.setBoolean("isLogin", false)
                MainApplication.prefs.setString("userId", "")
                MainApplication.prefs.setString("userName", "")
                MainApplication.prefs.setString("userPassword", "")
                MainApplication.prefs.setString("userRegionMaster", "")
                MainApplication.prefs.setString("userRegionMasterName", "")
                MainApplication.prefs.setString("userRegionDetail", "")

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



//        val userId = MainApplication.prefs.getString("userId","")
//        val userPassword = MainApplication.prefs.getString("userPassword", "")
//        val data = UserModel(userId,userPassword,"","","","","","","","","","","")
//
//        showLoading(binding.loading)
//
//        instance.apiService.userLogin(data).enqueue(object : Callback<LoginResult> {
//            override fun onResponse(call: Call<LoginResult>, response: Response<LoginResult>) {
//
//                hideLoading(binding.loading)
//
//                val result = response.body()?.result
//
//                if (result != "success") {
//
//                    MainApplication.prefs.setBoolean("isLogin", false)
//                    MainApplication.prefs.setString("userId", "")
//                    MainApplication.prefs.setString("userName", "")
//                    MainApplication.prefs.setString("userPassword", "")
//                    MainApplication.prefs.setString("userRegionMaster", "")
//                    MainApplication.prefs.setString("userRegionMasterName", "")
//                    MainApplication.prefs.setString("userRegionDetail", "")
//
//                    Toast.makeText(this@MainActivity, resource.getString(R.string.sign_in_status), Toast.LENGTH_LONG).show()
//
//                    Log.d("isLogin", "442 : isLogin : " + MainApplication.prefs.getBoolean("isLogin", false))
//                    Log.d("isLogin", "442 : userId : " + MainApplication.prefs.getString("userId", ""))
//
//
//
//
//
//                } else {
//                    if (checkFoldedDisplay() < 900) {
//                        fontSize = 14F
//                    } else {
//                        fontSize = 16F
//                    }
//
//                    //알람 허용
//                    if (NotificationManagerCompat.from(this@MainActivity).areNotificationsEnabled()) {
//                        val pushStatus = MainApplication.prefs.getBoolean("pushStatus", false)
//                        if (pushStatus) {
//                            instance.setAlarm()
//                        } else {
//                            instance.cancelAlarm()
//                        }
//                    }
//
//                    //알람 거부 -> 푸시 허용하든 거부하든 알람 취소
//                    else {
//                        MainApplication.prefs.setBoolean("pushStatus", false)
//                        MainApplication.instance.cancelAlarm()
//                    }
//
//                    showLoading(binding.loading)
//
//                    Log.d("isLogin", "485 :isLogin : " + MainApplication.prefs.getBoolean("isLogin", false))
//                    Log.d("isLogin", "485 :userId : " + MainApplication.prefs.getString("userId", ""))
//
//                    getApiData()
//                }
//            }
//
//            override fun onFailure(call: Call<LoginResult>, t: Throwable) {
//                hideLoading(binding.loading)
//                Log.d("metis", "onFailure : " + t.message.toString())
//            }
//        })

        checkAccount()





    }

    override fun onPause() {
        super.onPause()
        dialog?.dismiss()
    }
}