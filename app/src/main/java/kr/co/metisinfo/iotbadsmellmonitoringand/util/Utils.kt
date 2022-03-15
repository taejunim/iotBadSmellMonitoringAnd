package kr.co.metisinfo.iotbadsmellmonitoringand.util

import android.content.res.ColorStateList
import android.content.res.Resources
import android.util.TypedValue
import androidx.core.content.ContextCompat
import kr.co.metisinfo.iotbadsmellmonitoringand.MainApplication
import kr.co.metisinfo.iotbadsmellmonitoringand.R
import java.text.SimpleDateFormat
import java.util.regex.Pattern

class Utils {

    companion object {
        val ymdFormatter = SimpleDateFormat("yyyy-MM-dd")
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm")
        val timeFormatter = SimpleDateFormat("HH:mm")

        private val resource: Resources = MainApplication.getContext().resources

        val radioButtonColorStateList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked), // unchecked
                intArrayOf(android.R.attr.state_checked) // checked
            ), intArrayOf(
                ContextCompat.getColor(MainApplication.getContext(), R.color.color_gray), // unchecked color
                ContextCompat.getColor(MainApplication.getContext(), R.color.color_red) // checked color
            )
        )

        //Float 값을 DP 로 변환
        fun convertToDp(value : Float) : Int {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resource.displayMetrics).toInt()
        }

        //dp 값을 px 로 변환
        fun convertToPixel(dipValue: Float): Float {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, resource.displayMetrics)
        }

        fun checkRegex(type: String, value: String) : Boolean {

            //val regex = "^[a-zA-Z0-9]+$"    // 영문 또는 숫자

            var regex = ""
            when (type) {
                "id" -> regex = "^[a-zA-Z0-9ㄱ-ㅎ가-힣]+$"
                "password" -> regex = "^[a-zA-Z0-9~!@#$%^&*()+|=]{4,15}$"
                "name" -> regex = "^[ㄱ-ㅎ가-힣]{2,10}$"
                "age" -> regex = "^[0-9]{1,3}$"
            }

            val pattern = Pattern.compile(regex)
            val matcher = pattern.matcher(value)

            if (matcher.matches()) {
                return true
            }

            return false
        }
    }
}