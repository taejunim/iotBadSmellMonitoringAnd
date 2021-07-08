package kr.co.metisinfo.iotbadsmellmonitoringand.util

import android.content.res.Resources
import android.util.TypedValue
import kr.co.metisinfo.iotbadsmellmonitoringand.MainApplication
import java.text.SimpleDateFormat

class Utils {

    companion object {
        val ymdFormatter = SimpleDateFormat("yyyy-MM-dd")
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm")
        private val resource: Resources = MainApplication.getContext().resources

        //Float 값을 DP 로 변환
        fun convertToDp(value : Float) : Int {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resource.displayMetrics).toInt()
        }
    }
}