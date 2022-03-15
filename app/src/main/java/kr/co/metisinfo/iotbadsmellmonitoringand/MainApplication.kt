package kr.co.metisinfo.iotbadsmellmonitoringand

import android.app.*
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import kr.co.metisinfo.iotbadsmellmonitoringand.model.CodeModel
import kr.co.metisinfo.iotbadsmellmonitoringand.model.RegionMaster
import kr.co.metisinfo.iotbadsmellmonitoringand.model.RegisterModel
import kr.co.metisinfo.iotbadsmellmonitoringand.receiver.AlarmReceiver
import kr.co.metisinfo.iotbadsmellmonitoringand.util.ApiService
import kr.co.metisinfo.iotbadsmellmonitoringand.util.PreferenceUtil
import kr.co.metisinfo.iotbadsmellmonitoringand.util.Utils
import java.util.*

class MainApplication : Application() {

    val apiService = ApiService.create()

    val codeGroupArray = arrayOf("WND", "REN", "SMT", "STY")
    var windDirectionMap: MutableMap<String, String> = mutableMapOf() //풍향 코드
    var registerTimeZoneMap: MutableMap<String, String> = mutableMapOf() //신고 시간대
    var intensityList: List<CodeModel> = mutableListOf() //강도
    var regionList: List<RegionMaster> = mutableListOf() //지역
    var smellTypeList: List<CodeModel> = mutableListOf() //취기

    var registerStatusList: List<RegisterModel> = mutableListOf() //접수 현황

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
        //cancelAlarm()
        pendingIntent = PendingIntent.getBroadcast(this, AlarmReceiver.NOTIFICATION_ID, alarmReceiver, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, getAlarmTime(), pendingIntent)
    }

    //푸시 알람 취소
    fun cancelAlarm() {
        pendingIntent = PendingIntent.getBroadcast(this, AlarmReceiver.NOTIFICATION_ID, alarmReceiver, PendingIntent.FLAG_NO_CREATE)
        alarmManager.cancel(pendingIntent)
    }

    fun finish(activity: Activity) {
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(R.string.app_termination) //AlertDialog의 내용 부분
        builder.setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
            activity.finish()
        })
        builder.setCancelable(false)
        builder.create().show() //보이기
    }
}