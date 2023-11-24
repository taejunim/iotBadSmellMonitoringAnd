package kr.co.metisinfo.iotbadsmellmonitoringand

import android.app.*
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.util.Log
import kr.co.metisinfo.iotbadsmellmonitoringand.model.*
import kr.co.metisinfo.iotbadsmellmonitoringand.receiver.AlarmReceiver
import kr.co.metisinfo.iotbadsmellmonitoringand.util.ApiService
import kr.co.metisinfo.iotbadsmellmonitoringand.util.PreferenceUtil
import kr.co.metisinfo.iotbadsmellmonitoringand.util.Utils
import java.util.*

class MainApplication : Application() {

    val apiService = ApiService.create()

    var nx = "53" //제주시 지역 기본 X
    var ny = "38" //제주시 지역 기본 Y

    val defaultRegionTitle = "제주도"

    /**
     * code API 불러올 항목 array
     * WND : 풍향
     * REN : 악취 접수 시간대
     * SMT : 악취 강도
     * STY : 취기
     */
    val codeGroupArray = arrayOf("WND", "REN", "SMT", "STY")
    var windDirectionMap: MutableMap<String, String> = mutableMapOf() //풍향 코드
    var registerTimeZoneMap: MutableMap<String, String> = mutableMapOf() //악취 접수 시간대
    var intensityList: List<CodeModel> = mutableListOf() //악취 강도
    var smellTypeList: List<CodeModel> = mutableListOf() //취기

    var regionList: List<RegionMaster> = mutableListOf() //지역
    var regionMasterList: MutableList<SpinnerModel> = ArrayList<SpinnerModel>() //지역 마스터 리스트
    var regionDetailList: MutableList<SpinnerModel> = ArrayList<SpinnerModel>() //지역 디테일 리스트

    var registerStatusList: List<RegisterModel> = mutableListOf() //접수 현황

    var noticeModel: NoticeModel = NoticeModel("", "") //공지사항

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
        pendingIntent = PendingIntent.getBroadcast(this, AlarmReceiver.NOTIFICATION_ID, alarmReceiver, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        if (!prefs.isExist("pushStatus")) {
            prefs.setBoolean("pushStatus", true)
        }
    }

    //푸시 알람 시간 계산
    private fun getAlarmTime() : Long {

        val currentDate: Calendar = Calendar.getInstance()
        currentDate.time = Date()

        val today = Utils.ymdFormatter.format(currentDate.time)
        val currentTime = currentDate.time.time

        currentDate.add(Calendar.DATE, 1)
        val tomorrow = Utils.ymdFormatter.format(currentDate.time)

        val time00 = Utils.dateFormatter.parse("$today ${Constants.PUSH_TIME_00}").time
        val time06 = Utils.dateFormatter.parse("$today ${Constants.PUSH_TIME_06}").time
        val time11 = Utils.dateFormatter.parse("$today ${Constants.PUSH_TIME_11}").time
        val time19 = Utils.dateFormatter.parse("$today ${Constants.PUSH_TIME_19}").time
        //val time19 = Utils.dateFormatter.parse("$today ${Constants.TEST_TIME}").time
        val time24 = Utils.dateFormatter.parse("$tomorrow ${Constants.PUSH_TIME_00}").time
        val tomorrow06 = Utils.dateFormatter.parse("$tomorrow ${Constants.PUSH_TIME_06}").time

        var timeValue : Long? = null
        when (currentTime) {

            //06 에 push
            in time00 .. time06 -> timeValue = time06

            //11 push
            in time06 .. time11 -> timeValue = time11

            //19 push
            in time11 .. time19 -> timeValue = time19

            //tomorrow 06 push
            in time19 .. time24 -> timeValue = tomorrow06
        }

        Log.d("metis", "다음 알람 시간 : " + Utils.timeFormatter.format(timeValue) )

        return timeValue!!
    }

    //푸시 알람 설정 -> getAlarmTime() 로 가져온 가까운 시간대에
    fun setAlarm() {
        pendingIntent = PendingIntent.getBroadcast(this, AlarmReceiver.NOTIFICATION_ID, alarmReceiver, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        if (Build.VERSION.SDK_INT >= 31) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, getAlarmTime(), pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, getAlarmTime(), pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, getAlarmTime(), pendingIntent)
        }
    }

    //푸시 알람 취소
    fun cancelAlarm() {
        pendingIntent = PendingIntent.getBroadcast(this, AlarmReceiver.NOTIFICATION_ID, alarmReceiver,PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
    }

    //앱 종료
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