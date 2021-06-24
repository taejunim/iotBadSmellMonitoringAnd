package kr.co.metisinfo.iotbadsmellmonitoringand.util

import android.content.res.Resources
import android.util.TypedValue
import kr.co.metisinfo.iotbadsmellmonitoringand.MainApplication

class Utils {

    companion object {

        private val resource: Resources = MainApplication.getContext().resources

        //Float 값을 DP 로 변환
        fun convertToDp(value : Float) : Int {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resource.displayMetrics).toInt()
        }
    }
}