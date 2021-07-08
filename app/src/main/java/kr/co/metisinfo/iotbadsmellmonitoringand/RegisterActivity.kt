package kr.co.metisinfo.iotbadsmellmonitoringand

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import kr.co.metisinfo.iotbadsmellmonitoringand.databinding.ActivityRegisterBinding
import kr.co.metisinfo.iotbadsmellmonitoringand.dialog.SmellTypeDialog
import kr.co.metisinfo.iotbadsmellmonitoringand.model.RegisterModel
import kr.co.metisinfo.iotbadsmellmonitoringand.model.ResponseResult
import kr.co.metisinfo.iotbadsmellmonitoringand.model.WeatherModel
import kr.co.metisinfo.iotbadsmellmonitoringand.util.Utils.Companion.dateFormatter
import kr.co.metisinfo.iotbadsmellmonitoringand.util.Utils.Companion.ymdFormatter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class RegisterActivity : BaseActivity(), SmellTypeDialog.SmellTypeDialogListener {

    private lateinit var binding: ActivityRegisterBinding

    private val smellTypeList = instance.smellTypeList
    private val registerStatusList = instance.registerStatusList

    private var selectedSmellTypeId = ""

    private var receivedIntensityId = ""
    private var receivedIntensityText = ""
    private var receivedIntensityResource = ""

    private var locationManager = instance.getSystemService(LOCATION_SERVICE) as LocationManager

    private var registerTime = ""

    override fun onInputData(id: String, index: Int) {
        drawSmellTypeButton(id, index)
    }

    private fun drawSmellTypeButton(id: String, index: Int) {
        binding.smellTypeText.text = smellTypeList[index].codeIdName
        binding.smellTypeImage.setBackgroundResource(
            resource.getIdentifier("smell_$id","drawable","kr.co.metisinfo.iotbadsmellmonitoringand"))
        selectedSmellTypeId = smellTypeList[index].codeId
    }

    override fun initData() {

        receivedIntensityId = intent.getStringExtra("intensityId").toString()
        receivedIntensityText = intent.getStringExtra("intensityText").toString()
        receivedIntensityResource = intent.getStringExtra("intensityResource").toString()
    }

    override fun initLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register)

        Log.d("metis", "MainActivity 시작")
        binding.includeHeader.textTitle.setText(R.string.register) // 타이틀 제목
        binding.includeHeader.backButton.visibility = View.VISIBLE // 뒤로가기 버튼 보이게
        binding.includeHeader.sideMenuButton.visibility = View.GONE // 사이드 메뉴 버튼 안보이게

        drawSmellTypeButton("001", 0)

        binding.intensityButton.text = receivedIntensityText
        binding.intensityButton.setBackgroundResource(resource.getIdentifier(receivedIntensityResource,"drawable", "kr.co.metisinfo.iotbadsmellmonitoringand"))
    }

    override fun setOnClickListener() {

        binding.includeHeader.backButton.setOnClickListener { finish() }

        binding.smellTypeImageLayout.setOnClickListener {

            val dialog = SmellTypeDialog()
            dialog.show(supportFragmentManager, "SmellTypeDialog")
        }

        binding.registrationButton.setOnClickListener {

            registerTime = getRegisterTime()

            //등록 시간이 아니면 return
            when (registerTime) {
                "" -> Toast.makeText(this@RegisterActivity, resource.getString(R.string.register_not_register_time_text), Toast.LENGTH_SHORT).show()
                else -> getWeatherApiData() //날씨 데이터
            }
        }
    }

    /**
     * DATA CALLBACK
     */
    override fun callback(data: Any) {
        val weatherModel = data as WeatherModel
        Log.d("metis", "등록 callback data : $weatherModel")

        var weatherStatus = ""
        when (weatherModel.precipitationStatus) { // 강수형태가 0 이면 하늘상태 코드로

            "0" -> {
                when (weatherModel.skyStatus) {
                    "1" -> weatherStatus = "001" //맑음
                    "3" -> weatherStatus = "002" //구름많음
                    "4" -> weatherStatus = "003" //흐림
                }
            }
            "1" -> weatherStatus = "004" //비
            "2" -> weatherStatus = "005" //비/눈
            "3" -> weatherStatus = "006" //눈
            "4" -> weatherStatus = "007" //소나기
            "5" -> weatherStatus = "008" //빗방울
            "6" -> weatherStatus = "009" //빗방울/눈날림
            "7" -> weatherStatus = "010" //눈날림
            "" -> weatherStatus = "011" //기타
        }

        val temperatureValue = weatherModel.temperature
        val humidityValue = weatherModel.humidity

        //현재 풍향 텍스트 가져와서 +1 해주고 세자리 코드로 변환후 값 전송
        val windDirectionValue = String.format("%03d", instance.windDirectionMap.filter { weatherModel.windDirection == it.value }.keys.first().toInt() + 1)

        val windSpeedValue = weatherModel.windSpeed
        val locationMap = getLocation()
        val regId = MainApplication.prefs.getString("userId", "")

        val data = RegisterModel(
            "",
            selectedSmellTypeId,
            receivedIntensityId,
            weatherStatus,
            temperatureValue,
            humidityValue,
            windDirectionValue,
            windSpeedValue,
            locationMap["longitude"].toString(),
            locationMap["latitude"].toString(),
            binding.registerMemoInput.text.toString(),
            registerTime,
            "",
            regId,
            ""
        )

        instance.apiService.registerInsert(data).enqueue(object : Callback<ResponseResult> {
            override fun onResponse(call: Call<ResponseResult>, response: Response<ResponseResult>) {
                Log.d("metis",response.toString())
                Log.d("metis", " data -> " + data)
                Log.d("metis", " registerInsert getWindDirectionCode 결과 -> " + response.body().toString())
                Toast.makeText(this@RegisterActivity, resource.getString(R.string.register_success_text), Toast.LENGTH_SHORT).show()

                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed ({
                    finish()
                }, 2000)
            }

            override fun onFailure(call: Call<ResponseResult>, t: Throwable) {
                Log.d("metis", t.message.toString())
                Log.d("metis", "onFailure : fail")
            }
        })
    }

    //접수 등록시 현재 좌표 구하기
    private fun getLocation() : Map<String, String> {

        var latitude = ""
        var longitude = ""

        val isGPSEnabled: Boolean = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled: Boolean = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        //매니페스트에 권한이 추가되어 있다해도 여기서 다시 한번 확인해야함
        if (Build.VERSION.SDK_INT >= 23 &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this@RegisterActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                0
            )
        } else {
            when { //프로바이더 제공자 활성화 여부 체크
                isNetworkEnabled -> {
                    val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) //인터넷기반으로 위치를 찾음

                    latitude = location?.latitude.toString()
                    longitude = location?.longitude.toString()
                }
                isGPSEnabled -> {
                    val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) //GPS 기반으로 위치를 찾음

                    latitude = location?.latitude.toString()
                    longitude = location?.longitude.toString()
                }
                else -> {

                }
            }
            //몇초 간격과 몇미터를 이동했을시에 호출되는 부분 - 주기적으로 위치 업데이트를 하고 싶다면 사용
            // ****주기적 업데이트를 사용하다가 사용안할시에는 반드시 해제 필요****
            /*lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000, //몇초
                    1F,   //몇미터
                    gpsLocationListener)
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    1000,
                    1F,
                    gpsLocationListener)
            //해제부분. 상황에 맞게 잘 구현하자
            lm.removeUpdates(gpsLocationListener)*/
        }

        return mapOf("latitude" to latitude, "longitude" to longitude)
    }

    //접수 등록 시간 가져오기
    private fun getRegisterTime() : String {

        var registerTime = ""

        calendar.time = Date()

        val today = ymdFormatter.format(calendar.time)
        val currentTime = calendar.time.time

        calendar.add(Calendar.DATE, 1)
        val tomorrow = ymdFormatter.format(calendar.time)
        Log.d("metis", "tomorrow : " + tomorrow)

        for (i in registerStatusList.indices) {

            val registerTimeArray = registerStatusList[i].smellRegisterTimeName.split(" ~ ")

            if (registerStatusList[i].resultCode != "001") {

                val startTime: Long? = dateFormatter.parse(today + " " + registerTimeArray[0]).time
                var endTime: Long?

                //endTime 이 00:00 일 때 날짜가 바뀌므로
                if (registerTimeArray[1] == "00:00") {
                    endTime = dateFormatter.parse(tomorrow + " " + registerTimeArray[1]).time
                } else {
                    endTime = dateFormatter.parse(today + " " + registerTimeArray[1]).time
                }

                //현재 시각이 시작 일시와 종료 일시 사이일 경우
                if (startTime!! < currentTime && endTime > currentTime ) {

                    registerTime = registerStatusList[i].smellRegisterTime
                    Log.d("metis", "startTime : " + startTime)
                    Log.d("metis", "endTime : " + endTime)
                    Log.d("metis", "registerTime : " + registerTime)
                }
            }
        }

        return registerTime
    }

}