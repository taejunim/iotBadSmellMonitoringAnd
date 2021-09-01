package kr.co.metisinfo.iotbadsmellmonitoringand

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kr.co.metisinfo.iotbadsmellmonitoringand.Constants.TIME_00
import kr.co.metisinfo.iotbadsmellmonitoringand.Constants.TIME_06
import kr.co.metisinfo.iotbadsmellmonitoringand.Constants.TIME_09
import kr.co.metisinfo.iotbadsmellmonitoringand.Constants.TIME_18
import kr.co.metisinfo.iotbadsmellmonitoringand.Constants.TIME_21
import kr.co.metisinfo.iotbadsmellmonitoringand.model.*
import kr.co.metisinfo.iotbadsmellmonitoringand.util.ApiService
import kr.co.metisinfo.iotbadsmellmonitoringand.util.Utils.Companion.dateFormatter
import kr.co.metisinfo.iotbadsmellmonitoringand.util.Utils.Companion.ymdFormatter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

abstract class BaseActivity : AppCompatActivity(){

    val resource: Resources = MainApplication.getContext().resources

    private val weatherApiService = ApiService.weatherApiCreate()

    val instance = MainApplication.instance

    val calendar: Calendar = Calendar.getInstance()
    var today: String = ymdFormatter.format(calendar.time)

    var locationMap: MutableMap<String, String> = mutableMapOf()

    val locationManager = instance.getSystemService(LOCATION_SERVICE) as LocationManager

    /*var locationManager = instance.getSystemService(LOCATION_SERVICE) as LocationManager
    //define the listener
    val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {

            Log.d("metis", " LocationListener 위치 " + location.longitude + ":" + location.latitude)
            locationManager.removeUpdates(this)

            locationMap["latitude"] = location.latitude.toString()
            locationMap["longitude"] = location.longitude.toString()

            callback("location", "")
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }*/

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

    fun getLocation() {

        //define the listener
        val locationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {

                Log.d("metis", " LocationListener 위치 " + location.longitude + ":" + location.latitude)
                locationMap["latitude"] = location.latitude.toString()
                locationMap["longitude"] = location.longitude.toString()

                //callback("location", "")

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
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
        } catch (ex: SecurityException) {
        }
    }

    //코드 API
    fun getApiData() {

        for (i in instance.codeGroupArray.indices) {

            //풍향 코드 API
            instance.apiService.getWindDirectionCode(instance.codeGroupArray[i]).enqueue(object : Callback<CodeResult> {
                override fun onResponse(call: Call<CodeResult>, response: Response<CodeResult>) {

                    val dataList: List<CodeModel> = response.body()!!.data

                    for (j in dataList.indices) {

                        //풍향 코드
                        if (i == 0 ) {
                            val convertedValue = (Integer.parseInt(dataList[j].codeId) - 1).toString() //풍향 변환값 => CODE_ID를 정수로 변환후 -1 한 값
                            val directionName = dataList[j].codeIdName //풍향

                            instance.windDirectionMap.put(convertedValue,directionName)
                        }

                        //신고 시간대
                        else if (i == 1) {
                            instance.registerTimeZoneMap.put(dataList[j].codeId,dataList[j].codeIdName)
                        }
                    }

                    when (i) {
                        //냄새 타입
                        2 -> instance.intensityList = response.body()!!.data

                        //지역
                        3 -> instance.regionList = response.body()!!.data

                        //취기
                        4 -> {
                            instance.smellTypeList = response.body()!!.data
                            callback("baseData","")
                        }
                    }
                }

                override fun onFailure(call: Call<CodeResult>, t: Throwable) {
                    Log.d("metis", "onFailure : " + t.message.toString())
                    Toast.makeText(this@BaseActivity, resource.getString(R.string.server_no_response), Toast.LENGTH_SHORT).show()
                    callback("noResponse","")
                }
            })
        }
    }

    //날씨 API
    fun getWeatherApiData() {

        var weatherModel = WeatherModel("-","-","-","-","-","-")

        val parameterMap = getParameters() // 날씨 API를 위한 Parameter

        weatherApiService.getCurrentWeather(parameterMap["base_date"].toString(), parameterMap["base_time"].toString(), Constants.nx, Constants.ny,
            Constants.dataType, Constants.numOfRows).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {

                Log.d("metis","weatherApiService response.body() : " + response.body())
                if (response.isSuccessful){

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

                    callback("weather", weatherModel) //날씨 데이터 콜백
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.d("metis", "weatherApiService onFailure : " + t.message.toString())
                Toast.makeText(this@BaseActivity, resource.getString(R.string.weather_server_no_response), Toast.LENGTH_SHORT).show()
                callback("weather", weatherModel)
            }
        })
    }

    //날씨 API 파라미터 구하기
    private fun getParameters() : Map<String, String> {
        val baseDateformatter = SimpleDateFormat("yyyyMMdd")
        val baseTimeformatter = SimpleDateFormat("HH30")
        val timeformatter = SimpleDateFormat("HH00")

        calendar.time = Date()

        calendar.add(Calendar.MINUTE, -30)

        val baseDate = baseDateformatter.format(calendar.time) //기준일자
        val baseTime = baseTimeformatter.format(calendar.time) //기준일시

        calendar.add(Calendar.MINUTE, 60)

        val nearestTime = timeformatter.format(calendar.time)

        return mapOf("base_date" to baseDate, "base_time" to baseTime, "nearestTime" to nearestTime)
    }

    //풍향 구하기
    private fun getWindDirectionText(windDirectionValue: String): String {

        val originalValue = Integer.parseInt(windDirectionValue)
        val windDirectionText = String.format("%.0f", (originalValue + 22.5 * 0.5) / 22.5)

        return instance.windDirectionMap[windDirectionText].toString()
    }

    //날짜 배경색 가져오기
    fun getTimeForWeatherBackground() : Int {

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
            in time00 .. time06 -> timeValue = 0
            in time06 .. time09 -> timeValue = 1
            in time09 .. time18 -> timeValue = 2
            in time18 .. time21 -> timeValue = 3
            in time21 .. time24 -> timeValue = 4
        }

        return timeValue
    }

    //접수 현황 가져오기
    fun getUserTodayRegisterInfo() {

        val userId = MainApplication.prefs.getString("userId", "")

        instance.apiService.getUserTodayRegisterInfo(userId).enqueue(object : Callback<RegisterResult> {
            override fun onResponse(call: Call<RegisterResult>, response: Response<RegisterResult>) {

                instance.registerStatusList= response.body()!!.data //접수 현황

                callback("registerStatus", instance.registerStatusList)
            }

            override fun onFailure(call: Call<RegisterResult>, t: Throwable) {
                Log.d("metis", "onFailure : " + t.message.toString())
                Toast.makeText(this@BaseActivity, resource.getString(R.string.server_no_response), Toast.LENGTH_SHORT).show()
            }
        })
    }

    //키보드 내리기
    fun hideKeyboard(editText: EditText) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    //위치 권한 퍼미션 체크
    fun checkLocationPermission() : Boolean {
        return if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(applicationContext,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@BaseActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
            false
        } else {
            true
        }
    }
}