package kr.co.metisinfo.iotbadsmellmonitoringand

import android.app.Application
import android.content.Context
import android.util.Log
import kr.co.metisinfo.iotbadsmellmonitoringand.model.*
import kr.co.metisinfo.iotbadsmellmonitoringand.util.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class MainApplication : Application() {
    
    val weatherApiService = ApiService.weatherApiCreate()
    val weatherDataMap: MutableMap<String, String> = mutableMapOf() //날씨 데이터

    val apiService = ApiService.create()
    val codeGroupArray = arrayOf("WND", "REN")
    val windDirectionMap: MutableMap<String, String> = mutableMapOf() //풍향 코드
    val registerTimeZoneMap: MutableMap<String, String> = mutableMapOf() //신고 시간대
    var registerStatusList: List<RegisterModel> = mutableListOf()
    
    init {
        Log.d("metis","1")
        instance = this
        getApiData()
    }

    companion object {
        lateinit var instance: MainApplication
        fun getContext() : Context {
            return instance.applicationContext
        }
    }

    //코드 API
    private fun getApiData() {

        for (i in codeGroupArray.indices) {

            //풍향 코드 API
            apiService.getWindDirectionCode(codeGroupArray[i]).enqueue(object : Callback<CodeResult> {
                override fun onResponse(call: Call<CodeResult>, response: Response<CodeResult>) {
                    Log.d("metis",response.toString())
                    Log.d("metis", codeGroupArray[i] + " getWindDirectionCode 결과 -> " + response.body().toString())

                    val dataList: List<CodeModel> = response.body()!!.data

                    for (j in dataList.indices) {

                        //풍향 코드
                        if (i == 0 ) {
                            val convertedValue = (Integer.parseInt(dataList[j].codeId) - 1).toString() //풍향 변환값 => CODE_ID를 정수로 변환후 -1 한 값
                            val directionName = dataList[j].codeIdName //풍향

                            windDirectionMap.put(convertedValue,directionName)
                            Log.d("metis", "windDirectionMap : " + windDirectionMap)
                        }

                        //신고 시간대
                        else if (i == 1) {
                            registerTimeZoneMap.put(dataList[j].codeId,dataList[j].codeIdName)
                            Log.d("metis", "registerTimeZoneMap : " + registerTimeZoneMap)
                        }
                    }

                    if (i == codeGroupArray.indices.last) {
                        Log.d("metis","codeGroupArray.indices.last : " + codeGroupArray.indices.last)
                        getWeatherApiData()
                        getUserTodayRegisterInfo()
                    }
                }

                override fun onFailure(call: Call<CodeResult>, t: Throwable) {
                    Log.d("metis",t.message.toString())
                    Log.d("metis", "onFailure : fail")
                }
            })
        }
    }

    //날씨 API
    fun getWeatherApiData() {

        val parameterMap = getParameters() // 날씨 API를 위한 Parameter

        weatherApiService.getCurrentWeather(parameterMap["base_date"].toString(), parameterMap["base_time"].toString(), Constants.nx,Constants.ny,
            Constants.dataType, Constants.numOfRows).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {

                Log.d("metis","response.body() : " + response.body())
                if (response.isSuccessful){

                    val nearestTime = parameterMap["nearestTime"]
                    val itemList = response.body()!!.response.body.items.item

                    for (i in itemList.indices) {

                        // 가까운 시간대의 데이터 가져오기
                        if (itemList[i].fcstTime == nearestTime) {

                            val category = itemList[i].category

                            if (category == "T1H") {
                                weatherDataMap.put("temperature", itemList[i].fcstValue)
                            } else if (category == "REH") {
                                weatherDataMap.put("humidity", itemList[i].fcstValue)
                            } else if (category == "VEC") {
                                weatherDataMap.put("windDirection", getWindDirectionText(itemList[i].fcstValue))
                            } else if (category == "WSD") {
                                weatherDataMap.put("windSpeed", itemList[i].fcstValue)
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

    fun getUserTodayRegisterInfo() {

        val userId = "test2"

        apiService.getUserTodayRegisterInfo(userId).enqueue(object : Callback<RegisterResult> {
            override fun onResponse(call: Call<RegisterResult>, response: Response<RegisterResult>) {
                Log.d("metis",response.toString())
                Log.d("metis", userId + " getUserTodayRegisterInfo 결과 -> " + response.body().toString())

                registerStatusList = response.body()!!.data

                for (j in registerStatusList.indices) {

                    Log.d("metis", "smellRegisterTimeName : " + registerStatusList[j].smellRegisterTimeName)
                    Log.d("metis", "resultCode : " + registerStatusList[j].resultCode)
                    Log.d("metis", "regDt : " + registerStatusList[j].regDt)
                    Log.d("metis", "smellRegisterTime : " + registerStatusList[j].smellRegisterTime)

                    //풍향 코드
                    /*if (i == 0 ) {
                        val convertedValue = (Integer.parseInt(dataList[j].codeId) - 1).toString() //풍향 변환값 => CODE_ID를 정수로 변환후 -1 한 값
                        val directionName = dataList[j].codeIdName //풍향

                        windDirectionMap.put(convertedValue,directionName)
                        Log.d("metis", "windDirectionMap : " + windDirectionMap)
                    }

                    //신고 시간대
                    else if (i == 1) {
                        registerTimeZoneMap.put(dataList[j].codeId,dataList[j].codeIdName)
                        Log.d("metis", "registerTimeZoneMap : " + registerTimeZoneMap)
                    }*/
                }


            }

            override fun onFailure(call: Call<RegisterResult>, t: Throwable) {
                Log.d("metis",t.message.toString())
                Log.d("metis", "onFailure : fail")
            }
        })
    }
}