package kr.co.metisinfo.iotbadsmellmonitoringand.fragment

import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import kr.co.metisinfo.iotbadsmellmonitoringand.MainApplication
import kr.co.metisinfo.iotbadsmellmonitoringand.R
import kr.co.metisinfo.iotbadsmellmonitoringand.databinding.FragmentStatisticsBinding
import kr.co.metisinfo.iotbadsmellmonitoringand.model.StatisticsResult
import kr.co.metisinfo.iotbadsmellmonitoringand.util.Utils.Companion.setComma
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class StatisticsFragment : Fragment() {

    private lateinit var binding: FragmentStatisticsBinding

    val resource: Resources = MainApplication.getContext().resources
    val instance = MainApplication.instance

    private lateinit var spannableStringBuilder : SpannableStringBuilder
    private val userTotalCountPrefix = "(총 악취 접수 : "
    private val userTotalCountSuffix = " 건)"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_statistics, container,false)

        return binding.root
    }

    //통계 현황 가져오기
    private fun getStatisticsInfo() {

        val userRegionMaster = MainApplication.prefs.getString("userRegionMaster", "")
        val userRegionDetail = MainApplication.prefs.getString("userRegionDetail", "")

        instance.apiService.getStatisticsInfo(userRegionMaster, userRegionDetail).enqueue(object : Callback<StatisticsResult> {
            override fun onResponse(call: Call<StatisticsResult>, response: Response<StatisticsResult>) {

                if (response.body() != null) {
                    val statisticsModel = response.body()!!.data

                    val userTotalCount = setComma(statisticsModel.userTotalCount)

                    spannableStringBuilder = SpannableStringBuilder(userTotalCountPrefix + userTotalCount + userTotalCountSuffix)
                    spannableStringBuilder.setSpan(ForegroundColorSpan(Color.parseColor("#E4513D")), userTotalCountPrefix.length, userTotalCountPrefix.length + userTotalCount.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                    binding.statisticsTotalCount.text = spannableStringBuilder
                    binding.statisticsSensingCount.text = if (statisticsModel.userRegisterCount == "") "-" else resource.getString(R.string.statistics_sensing_count, statisticsModel.userRegisterCount)
                    binding.statisticsSensingRatio.text = if (statisticsModel.userRegisterPercentage == "") "-" else resource.getString(R.string.statistics_sensing_ratio, statisticsModel.userRegisterPercentage)
                    binding.statisticsSensingIntensity.text = if (statisticsModel.mainSmellValueName == "") "-" else resource.getString(R.string.statistics_sensing_intensity, statisticsModel.mainSmellValueName)
                    binding.statisticsSensingType.text = if (statisticsModel.mainSmellTypeName == "") "-" else resource.getString(R.string.statistics_sensing_type, statisticsModel.mainSmellTypeName)
                }
            }

            override fun onFailure(call: Call<StatisticsResult>, t: Throwable) {
                Log.d("metis", "onFailure : " + t.message.toString())
                Toast.makeText(context, resource.getString(R.string.server_no_response), Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()

        getStatisticsInfo()
    }
}