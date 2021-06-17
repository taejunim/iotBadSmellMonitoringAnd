package kr.co.metisinfo.iotbadsmellmonitoringand

import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import kr.co.metisinfo.iotbadsmellmonitoringand.databinding.ActivityMainBinding
import kr.co.metisinfo.iotbadsmellmonitoringand.util.ApiService
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    val weatherApiService = ApiService

    override fun initLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        Log.d("metis","MainActivity 시작")
        binding.includeHeader.textTitle.setText(R.string.main)
        binding.includeHeader.backButton.visibility = View.GONE
        binding.includeHeader.sideMenuButton.visibility = View.VISIBLE

        binding.temperatureText.text = resource.getString(R.string.main_temperature,"21")
        binding.humidityText.text = resource.getString(R.string.main_humidity,"63")
        binding.windText.text = resource.getString(R.string.main_wind,"서")
        binding.windSpeedText.text = resource.getString(R.string.main_wind_speed,"5")

        val parameterMap = getParameters()

        /*weatherApiService.weatherApiCreate().getCurrentWeather(parameterMap.get("base_date").toString(), parameterMap.get("base_time").toString(), parameterMap.get("nx").toString(),
            parameterMap.get("ny").toString()).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                Log.d("metis",response.toString())
                Log.d("metis", "weatherApiService 결과 -> " + response.body().toString())

                if (response.isSuccessful){
                    Log.d("api", response.body().toString())
                    Log.d("api", response.body()!!.response.body.items.item.toString())
                    Log.d("api", response.body()!!.response.body.items.item[0].category)
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.d("metis",t.message.toString())
                Log.d("metis", "onFailure : fail")

            }
        })*/
    }

    override fun setOnClickListener() {
        binding.includeHeader.sideMenuButton.setOnClickListener {
            Log.d("metis","MainActivity - Side Menu 구현해야 ")
        }
    }

    fun getParameters() : Map<String, String> {
        val baseDateformatter = SimpleDateFormat("yyyyMMdd")
        val baseTimeformatter = SimpleDateFormat("HH30")
        val timeformatter = SimpleDateFormat("HH00")
        val currentDate = Date()

        val calendar = Calendar.getInstance()
        calendar.time = currentDate

        calendar.add(Calendar.MINUTE, -30)

        val baseDate = baseDateformatter.format(calendar.time)
        val baseTime = baseTimeformatter.format(calendar.time)

        val nx = "48"
        val ny = "36"

        val parameterMap = mapOf("base_date" to baseDate, "base_time" to baseTime, "nx" to nx, "ny" to ny)

        return parameterMap
    }
}