package kr.co.metisinfo.iotbadsmellmonitoringand

import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import kr.co.metisinfo.iotbadsmellmonitoringand.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    val instance = MainApplication.instance

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
    }

    override fun setOnClickListener() {
        binding.includeHeader.sideMenuButton.setOnClickListener {
            Log.d("metis","MainActivity - Side Menu 구현해야 ")
        }
    }

    override fun initData() {
    }

}