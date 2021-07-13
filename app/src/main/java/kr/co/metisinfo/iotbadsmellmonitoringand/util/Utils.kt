package kr.co.metisinfo.iotbadsmellmonitoringand.util

import android.content.res.ColorStateList
import android.content.res.Resources
import android.util.TypedValue
import androidx.core.content.ContextCompat
import kr.co.metisinfo.iotbadsmellmonitoringand.MainApplication
import kr.co.metisinfo.iotbadsmellmonitoringand.R
import java.text.SimpleDateFormat

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
    }
}