package kr.co.metisinfo.iotbadsmellmonitoringand

import android.app.Application
import android.content.Context
import android.util.Log
import kr.co.metisinfo.iotbadsmellmonitoringand.model.CodeModel
import kr.co.metisinfo.iotbadsmellmonitoringand.model.CodeResult
import kr.co.metisinfo.iotbadsmellmonitoringand.model.RegisterModel
import kr.co.metisinfo.iotbadsmellmonitoringand.model.RegisterResult
import kr.co.metisinfo.iotbadsmellmonitoringand.util.ApiService
import kr.co.metisinfo.iotbadsmellmonitoringand.util.PreferenceUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainApplication : Application() {

    val apiService = ApiService.create()
    val codeGroupArray = arrayOf("WND", "REN", "SMT", "RGN", "STY")
    val windDirectionMap: MutableMap<String, String> = mutableMapOf() //풍향 코드
    val registerTimeZoneMap: MutableMap<String, String> = mutableMapOf() //신고 시간대
    var registerStatusList: List<RegisterModel> = mutableListOf() //접수 현황
    var intensityList: List<CodeModel> = mutableListOf() //강도
    var regionList: List<CodeModel> = mutableListOf() //지역
    var smellTypeList: List<CodeModel> = mutableListOf() //취기
    
    init {
        instance = this
        getApiData()
    }

    companion object {
        lateinit var instance: MainApplication
        lateinit var prefs: PreferenceUtil

        fun getContext() : Context {
            return instance.applicationContext
        }
    }

    override fun onCreate() {
        prefs = PreferenceUtil(applicationContext)
        super.onCreate()
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

                    //냄새 타입
                    if (i == 2) {
                        intensityList = response.body()!!.data
                        Log.d("metis", "intensityList : " + intensityList)
                    }

                    //지역
                    else if (i == 3) {
                        regionList = response.body()!!.data
                        Log.d("metis", "regionList : " + regionList)
                    }

                    //취기
                    else if (i == 4) {
                        smellTypeList = response.body()!!.data
                        Log.d("metis", "smellTypeList : " + smellTypeList)
                    }

                    if (i == codeGroupArray.indices.last) {
                        Log.d("metis","codeGroupArray.indices.last : " + codeGroupArray.indices.last)
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

    fun getUserTodayRegisterInfo() {

        val userId = MainApplication.prefs.getString("userId", "")

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
                }
            }

            override fun onFailure(call: Call<RegisterResult>, t: Throwable) {
                Log.d("metis",t.message.toString())
                Log.d("metis", "onFailure : fail")
            }
        })
    }
}