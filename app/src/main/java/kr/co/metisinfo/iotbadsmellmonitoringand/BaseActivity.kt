package kr.co.metisinfo.iotbadsmellmonitoringand

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.hardware.display.DisplayManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kr.co.metisinfo.iotbadsmellmonitoringand.Constants.TIME_00
import kr.co.metisinfo.iotbadsmellmonitoringand.Constants.TIME_06
import kr.co.metisinfo.iotbadsmellmonitoringand.Constants.TIME_09
import kr.co.metisinfo.iotbadsmellmonitoringand.Constants.TIME_18
import kr.co.metisinfo.iotbadsmellmonitoringand.Constants.TIME_21
import kr.co.metisinfo.iotbadsmellmonitoringand.model.*
import kr.co.metisinfo.iotbadsmellmonitoringand.util.ApiService
import kr.co.metisinfo.iotbadsmellmonitoringand.util.Utils.Companion.baseDateFormatter
import kr.co.metisinfo.iotbadsmellmonitoringand.util.Utils.Companion.baseTimeFormatter
import kr.co.metisinfo.iotbadsmellmonitoringand.util.Utils.Companion.dateFormatter
import kr.co.metisinfo.iotbadsmellmonitoringand.util.Utils.Companion.simpleTimeFormatter
import kr.co.metisinfo.iotbadsmellmonitoringand.util.Utils.Companion.ymdFormatter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

abstract class BaseActivity : AppCompatActivity() {

    var succeededApiCount = 0

    val resource: Resources = MainApplication.getContext().resources

    private val weatherApiService = ApiService.weatherApiCreate()

    val instance = MainApplication.instance

    val calendar: Calendar = Calendar.getInstance()
    var today: String = ymdFormatter.format(calendar.time)

    var locationMap: MutableMap<String, String> = mutableMapOf()

    val locationManager = instance.getSystemService(LOCATION_SERVICE) as LocationManager

    private var isBackPressed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initData()
        initLayout()
        setOnClickListener()
    }

    abstract fun initLayout()

    abstract fun setOnClickListener()

    abstract fun initData()

    abstract fun callback(apiName: String, data: Any)

    //기상청 데이터를 가져오기 위한 x,y 좌표 API
    fun getCoordinates() {
        //풍향 코드 API
        instance.apiService.getCoordinates(MainApplication.prefs.getString("userRegionMasterName","")).enqueue(object : Callback<CoordinatesResult> {
            override fun onResponse(call: Call<CoordinatesResult>, response: Response<CoordinatesResult>) {

                if (response.body() == null) {
                    callback("coordinates", "fail")
                } else if (response.body()!!.result == "fail") {
                    callback("coordinates", "fail")
                } else if (response.body()!!.result == "success") {

                    val coordinatesModel = response.body()!!.data
                    instance.nx = coordinatesModel.addressX
                    instance.ny = coordinatesModel.addressY

                    callback("coordinates", "success")
                }
            }

            override fun onFailure(call: Call<CoordinatesResult>, t: Throwable) {
                Log.d("metis", "onFailure : " + t.message.toString())
                callback("coordinates", "fail")
            }
        })
    }

    //현재 위치 좌표 가져오기
    fun getLocation() {

        val locationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {

                locationMap["latitude"] = location.latitude.toString()
                locationMap["longitude"] = location.longitude.toString()

                if (locationMap["latitude"] != "" && locationMap["latitude"] != null) {
                    locationManager.removeUpdates(this)
                    callback("location", "")
                }
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0L,0f,locationListener)
        } catch (ex: SecurityException) {
        }
    }

    fun checkAccount() {

        val userId = MainApplication.prefs.getString("userId","")
        val userPassword = MainApplication.prefs.getString("userPassword", "")
        val data = UserModel(userId,userPassword,"","","","","","","","","","","")


        instance.apiService.userLogin(data).enqueue(object : Callback<LoginResult> {
            override fun onResponse(call: Call<LoginResult>, response: Response<LoginResult>) {


                val result = response.body()?.result

                if (result != "success") {

                    MainApplication.prefs.setBoolean("isLogin", false)
                    MainApplication.prefs.setString("userId", "")
                    MainApplication.prefs.setString("userName", "")
                    MainApplication.prefs.setString("userPassword", "")
                    MainApplication.prefs.setString("userRegionMaster", "")
                    MainApplication.prefs.setString("userRegionMasterName", "")
                    MainApplication.prefs.setString("userRegionDetail", "")

                    Toast.makeText(applicationContext, resource.getString(R.string.sign_in_status), Toast.LENGTH_LONG).show()

                    Log.d("isLogin", "442 : isLogin : " + MainApplication.prefs.getBoolean("isLogin", false))
                    Log.d("isLogin", "442 : userId : " + MainApplication.prefs.getString("userId", ""))



                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed ({
                        var intent = Intent(applicationContext, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }, 1000)

                } else {
                    callback("checkAccount", "success")
                }
            }

            override fun onFailure(call: Call<LoginResult>, t: Throwable) {
                Log.d("metis", "onFailure : " + t.message.toString())
            }
        })
    }

    //코드 API
    fun getApiData() {

        //풍향 코드 API
        instance.apiService.getRegionList().enqueue(object : Callback<RegionResult> {
            override fun onResponse(call: Call<RegionResult>, response: Response<RegionResult>) {

                //실패
                if (response.body() == null) {
                    callback("baseData", "fail")
                } else if (response.body()!!.result == "fail") {
                    callback("baseData", "fail")
                }

                //성공
                else if (response.body()!!.result == "success") {

                    instance.regionList = response.body()!!.data.region.master //지역 데이터

                    //코드 데이터 API 시작
                    for (i in instance.codeGroupArray.indices) {

                        instance.apiService.getApiData(instance.codeGroupArray[i]).enqueue(object : Callback<CodeResult> {
                                override fun onResponse(call: Call<CodeResult>, response: Response<CodeResult>) {

                                    //실패
                                    if (response.body() == null) {
                                        callback("baseData", "fail")
                                    } else if (response.body()!!.result == "fail") {
                                        callback("baseData", "fail")
                                    }

                                    //성공
                                    else if (response.body()!!.result == "success") {
                                        when (i) {
                                            //냄새 강도
                                            2 -> {
                                                instance.intensityList = response.body()!!.data
                                                callback("baseData", "success")
                                            }

                                            //취기
                                            3 -> {
                                                instance.smellTypeList = response.body()!!.data
                                                callback("baseData", "success")
                                            }
                                            else -> {

                                                val dataList: List<CodeModel> = response.body()!!.data

                                                for (j in dataList.indices) {
                                                    //풍향 코드
                                                    if (i == 0) {
                                                        val convertedValue = (Integer.parseInt(dataList[j].codeId) - 1).toString() //풍향 변환값 => CODE_ID를 정수로 변환후 -1 한 값
                                                        val directionName = dataList[j].codeIdName //풍향

                                                        instance.windDirectionMap.put(convertedValue, directionName)
                                                    }

                                                    //신고 시간대
                                                    else if (i == 1) {
                                                        instance.registerTimeZoneMap.put(dataList[j].codeId, dataList[j].codeIdName)
                                                    }

                                                    if (j == dataList.lastIndex) {
                                                        callback("baseData", "success")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                override fun onFailure(call: Call<CodeResult>, t: Throwable) {
                                    Log.d("metis", "onFailure : " + t.message.toString())
                                    callback("noResponse", "")
                                }
                            })
                    }
                }
            }

            override fun onFailure(call: Call<RegionResult>, t: Throwable) {
                Log.d("metis", "onFailure : " + t.message.toString())
                callback("noResponse", "")
            }
        })
    }

    //날씨 API
    fun getWeatherApiData() {

        var weatherModel = WeatherModel("-", "-", "-", "-", "-", "-")

        val parameterMap = getParameters() // 날씨 API를 위한 Parameter

        weatherApiService.getCurrentWeather(Constants.serviceKey, parameterMap["base_date"].toString(), parameterMap["base_time"].toString(), instance.nx, instance.ny, Constants.dataType, Constants.numOfRows).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {

                if (response.isSuccessful) {

                    if (response.body() != null) {

                        if (response.body()!!.response.header.resultCode == "00") {
                            val nearestTime = parameterMap["nearestTime"]
                            val itemList = response.body()!!.response.body.items.item

                            for (i in itemList.indices) {

                                // 가까운 시간대의 데이터 가져오기
                                if (itemList[i].fcstTime == nearestTime) {

                                    when (itemList[i].category) {
                                        //온도
                                        "T1H" -> weatherModel.temperature = itemList[i].fcstValue

                                        //습도
                                        "REH" -> weatherModel.humidity = itemList[i].fcstValue

                                        //풍향
                                        "VEC" -> weatherModel.windDirection = getWindDirectionText(itemList[i].fcstValue)

                                        //풍속
                                        "WSD" -> weatherModel.windSpeed = itemList[i].fcstValue

                                        //강수 상태
                                        "PTY" -> weatherModel.precipitationStatus = itemList[i].fcstValue

                                        //기상 상태
                                        "SKY" -> weatherModel.skyStatus = itemList[i].fcstValue
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(this@BaseActivity, resource.getString(R.string.weather_server_no_response), Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        Toast.makeText(this@BaseActivity, resource.getString(R.string.weather_server_no_response), Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Toast.makeText(this@BaseActivity, resource.getString(R.string.weather_server_no_response), Toast.LENGTH_SHORT).show()
                }

                callback("weather", weatherModel) //날씨 데이터 콜백
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.d("metis", "weatherApiService onFailure : " + t.message.toString())
                Toast.makeText(this@BaseActivity, resource.getString(R.string.weather_server_no_response), Toast.LENGTH_SHORT).show()
                callback("weather", weatherModel)
            }
        })
    }

    //공지사항 API
    fun getNoticeInfo() {

        instance.apiService.getNoticeInfo().enqueue(object : Callback<NoticeResult> {
            override fun onResponse(call: Call<NoticeResult>, response: Response<NoticeResult>) {

                if (response.body() == null) {
                    callback("noticeInfo", "fail")
                } else if (response.body()!!.result == "fail") {
                    instance.noticeModel = NoticeModel("", "")
                } else if (response.body()!!.result == "success") {
                    instance.noticeModel = response.body()!!.data
                    callback("noticeInfo", "success")
                }
            }

            override fun onFailure(call: Call<NoticeResult>, t: Throwable) {
                Log.d("metis", "onFailure : " + t.message.toString())
                callback("noResponse", "")
            }
        })
    }

    //날씨 API 파라미터 구하기
    private fun getParameters(): Map<String, String> {

        calendar.time = Date()

        calendar.add(Calendar.MINUTE, -30)

        val baseDate = baseDateFormatter.format(calendar.time) //기준일자
        val baseTime = baseTimeFormatter.format(calendar.time) //기준일시

        calendar.add(Calendar.MINUTE, 60)

        val nearestTime = simpleTimeFormatter.format(calendar.time)

        return mapOf("base_date" to baseDate, "base_time" to baseTime, "nearestTime" to nearestTime)
    }

    //풍향 구하기
    private fun getWindDirectionText(windDirectionValue: String): String {

        val originalValue = Integer.parseInt(windDirectionValue)
        val windDirectionText = String.format("%.0f", (originalValue + 22.5 * 0.5) / 22.5)

        return instance.windDirectionMap[windDirectionText].toString()
    }

    //날짜 배경색 가져오기
    fun getTimeForWeatherBackground(): Int {

        calendar.time = Date()

        val currentTime = calendar.time.time

        calendar.add(Calendar.DATE, 1)

        val tomorrow = ymdFormatter.format(calendar.time)

        val time00 = dateFormatter.parse("$today $TIME_00").time
        val time06 = dateFormatter.parse("$today $TIME_06").time
        val time09 = dateFormatter.parse("$today $TIME_09").time
        val time18 = dateFormatter.parse("$today $TIME_18").time
        val time21 = dateFormatter.parse("$today $TIME_21").time
        val time24 = dateFormatter.parse("$tomorrow $TIME_00").time

        var timeValue = 0
        when (currentTime) {
            in time00..time06 -> timeValue = 0
            in time06..time09 -> timeValue = 1
            in time09..time18 -> timeValue = 2
            in time18..time21 -> timeValue = 3
            in time21..time24 -> timeValue = 4
        }

        return timeValue
    }

    //키보드 내리기
    fun hideKeyboard(editText: EditText) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    //위치/알림 권한 퍼미션 체크
    fun checkPermission() {

        var tempPermissionArray : MutableList<String> = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= 33) {
            tempPermissionArray = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            tempPermissionArray = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        val permissionArray = checkPermissionArray(tempPermissionArray)

        //권한 허용되지 않은게 있다면
        if (permissionArray.isNotEmpty()) {
            ActivityCompat.requestPermissions(this@BaseActivity, permissionArray.toTypedArray(),0)
        }
    }

    fun checkPermissionArray(tempPermissionArray : MutableList<String>): MutableList<String> {

        val permissionArray : MutableList<String> = mutableListOf<String>()

        for (permission in tempPermissionArray) {
            if (ContextCompat.checkSelfPermission(applicationContext, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionArray.add(permission)
            }
        }

        return permissionArray
    }

    fun checkFoldedDisplay() : Int {
        var width = 1000
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){

            val dm = applicationContext.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
            dm?.displays?.forEach {
                width = it.mode.physicalWidth
            }
        }
        return width
    }

    //포커싱됐을 때 value clear
    fun clearEditTextValue(editText: EditText) {
        //포커스가 주어졌을 때 동작
        editText.onFocusChangeListener = OnFocusChangeListener { _, gainFocus ->
            if (gainFocus) {
                editText.setText("")
            }
        }
    }

    //설정창 표출
    fun showSettingActivity(text: Int, context: Context, action: String) {
        Log.d("metis", "action : " + action)
        val builder = AlertDialog.Builder(context)
        builder.setMessage(text) //AlertDialog의 내용 부분
        builder.setPositiveButton("설정", DialogInterface.OnClickListener { dialog, which ->
            val intent = Intent()
            intent.action = action
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        })
        builder.setNegativeButton("취소", null)
        builder.create().show() //보이기
    }

    override fun onBackPressed() {
        if (isBackPressed) {
            finish()
        }
    }

    open fun showLoading(progressBar: ProgressBar) {
        isBackPressed = false
        progressBar.visibility = View.VISIBLE
        //해당페이지 이벤트 막기
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    open fun hideLoading(progressBar: ProgressBar) {
        isBackPressed = true
        progressBar.visibility = View.INVISIBLE
        //이벤트 다시 풀기
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
}