package kr.co.metisinfo.iotbadsmellmonitoringand

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import kr.co.metisinfo.iotbadsmellmonitoringand.model.CodeModel
import kr.co.metisinfo.iotbadsmellmonitoringand.model.CodeResult
import kr.co.metisinfo.iotbadsmellmonitoringand.receiver.AlarmReceiver
import kr.co.metisinfo.iotbadsmellmonitoringand.util.ApiService
import kr.co.metisinfo.iotbadsmellmonitoringand.util.PreferenceUtil
import kr.co.metisinfo.iotbadsmellmonitoringand.util.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MainApplication : Application() {

    val apiService = ApiService.create()
    val codeGroupArray = arrayOf("WND", "REN", "SMT", "RGN", "STY")
    val windDirectionMap: MutableMap<String, String> = mutableMapOf() //풍향 코드
    val registerTimeZoneMap: MutableMap<String, String> = mutableMapOf() //신고 시간대

    var intensityList: List<CodeModel> = mutableListOf() //강도
    var regionList: List<CodeModel> = mutableListOf() //지역
    var smellTypeList: List<CodeModel> = mutableListOf() //취기

    /** 푸시 관련 **/
    private lateinit var alarmManager: AlarmManager
    private lateinit var alarmReceiver:Intent
    private lateinit var pendingIntent: PendingIntent
    
    init {
        instance = this
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

        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmReceiver = Intent(this, AlarmReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(this, AlarmReceiver.NOTIFICATION_ID, alarmReceiver, PendingIntent.FLAG_UPDATE_CURRENT)

    }

    //코드 API
    fun getApiData() {

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
                }

                override fun onFailure(call: Call<CodeResult>, t: Throwable) {
                    Log.d("metis",t.message.toString())
                    Log.d("metis", "onFailure : fail")
                }
            })
        }
    }

    private fun getAlarmTime() : Long {

        val currentDate: Calendar = Calendar.getInstance()
        currentDate.time = Date()

        val today = Utils.ymdFormatter.format(currentDate.time)
        val currentTime = currentDate.time.time

        currentDate.add(Calendar.DATE, 1)
        val tomorrow = Utils.ymdFormatter.format(currentDate.time)

        val time00 = Utils.dateFormatter.parse("$today ${Constants.PUSH_TIME_00}").time
        val time07 = Utils.dateFormatter.parse("$today ${Constants.PUSH_TIME_07}").time
        val time12 = Utils.dateFormatter.parse("$today ${Constants.PUSH_TIME_12}").time
        val time18 = Utils.dateFormatter.parse("$today ${Constants.PUSH_TIME_18}").time
        val time22 = Utils.dateFormatter.parse("$today ${Constants.PUSH_TIME_22}").time
        val time24 = Utils.dateFormatter.parse("$tomorrow ${Constants.PUSH_TIME_00}").time
        val tomorrow07 = Utils.dateFormatter.parse("$tomorrow ${Constants.PUSH_TIME_07}").time

        //val testTime = Utils.dateFormatter.parse("$today ${Constants.TEST_TIME}").time

        var timeValue : Long? = null
        when (currentTime) {

            //07 에 push
            in time00 .. time07 -> timeValue = time07

            //12 push
            in time07 .. time12 -> timeValue = time12

            //18 push
            in time12 .. time18 -> timeValue = time18

            //22 push
            in time18 .. time22 -> timeValue = time22

            //tomorrow 07 push
            in time22 .. time24 -> timeValue = tomorrow07
        }

        //Log.d("metis", "다음 알람 시간 : " + Utils.timeFormatter.format(testTime) )
        Log.d("metis", "다음 알람 시간 : " + Utils.timeFormatter.format(timeValue) )


        return timeValue!!
    }

    //푸시 알람 설정 -> getAlarmTime() 로 가져온 가까운 시간대에
    fun setAlarm() {
        alarmManager.set(AlarmManager.RTC_WAKEUP, getAlarmTime(), pendingIntent)
    }

    //푸시 알람 취소
    fun cancelAlarm() {
        alarmManager.cancel(pendingIntent)
    }
}