package kr.co.metisinfo.iotbadsmellmonitoringand

import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import kr.co.metisinfo.iotbadsmellmonitoringand.databinding.ActivityMainBinding
import kr.co.metisinfo.iotbadsmellmonitoringand.model.WeatherResponse
import kr.co.metisinfo.iotbadsmellmonitoringand.util.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    private val weatherApiService = ApiService

    override fun initLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        Log.d("metis","MainActivity 시작")
        binding.includeHeader.textTitle.setText(R.string.main) // 타이틀 제목
        binding.includeHeader.backButton.visibility = View.GONE // 뒤로가기 버튼 안보이게
        binding.includeHeader.sideMenuButton.visibility = View.VISIBLE // 사이드 메뉴 버튼 보이게

        val parameterMap = getParameters() // 날씨 API를 위한 Parameter

        weatherApiService.weatherApiCreate().getCurrentWeather(parameterMap.get("base_date").toString(), parameterMap.get("base_time").toString(), Constants.nx,Constants.ny,
            Constants.dataType, Constants.numOfRows).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {

                if (response.isSuccessful){

                    val nearestTime = parameterMap.get("nearestTime")
                    val itemList = response.body()!!.response.body.items.item

                    for (i in 0..itemList?.size!! - 1) {

                        // 가까운 시간대의 데이터 가져오기
                        if (itemList[i].fcstTime == nearestTime) {

                            val category = itemList[i].category

                            if (category == "T1H") {
                                binding.temperatureText.text = resource.getString(R.string.main_temperature, itemList[i].fcstValue)
                            } else if (category == "REH") {
                                binding.humidityText.text = resource.getString(R.string.main_humidity, itemList[i].fcstValue)
                            } else if (category == "VEC") {
                                binding.windText.text = resource.getString(R.string.main_wind, getWindDirectionText(itemList[i].fcstValue))
                            } else if (category == "WSD") {
                                binding.windSpeedText.text = resource.getString(R.string.main_wind_speed,itemList[i].fcstValue)
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.d("metis",t.message.toString())
                Log.d("metis", "onFailure : fail")

            }
        })
    }

    override fun setOnClickListener() {
        binding.includeHeader.sideMenuButton.setOnClickListener {
            Log.d("metis","MainActivity - Side Menu 구현해야 ")
        }
    }

    //날씨 API 파라미터 구하기
    private fun getParameters() : Map<String, String> {
        val baseDateformatter = SimpleDateFormat("yyyyMMdd")
        val baseTimeformatter = SimpleDateFormat("HH30")
        val timeformatter = SimpleDateFormat("HH00")
        val currentDate = Date()

        val calendar = Calendar.getInstance()
        calendar.time = currentDate

        calendar.add(Calendar.MINUTE, -30)

        val baseDate = baseDateformatter.format(calendar.time) //기준일자
        val baseTime = baseTimeformatter.format(calendar.time) //기준일시

        calendar.add(Calendar.MINUTE, 60)

        val nearestTime = timeformatter.format(calendar.time)

        return mapOf("base_date" to baseDate, "base_time" to baseTime, "nearestTime" to nearestTime)
    }

    //풍향구하기
    private fun getWindDirectionText(windDirectionValue: String): String {

        val originalValue = Integer.parseInt(windDirectionValue)
        val windDirectionText = String.format("%.0f", (originalValue + 22.5 * 0.5) / 22.5)

        return windDirectionMap[windDirectionText].toString()
    }
}