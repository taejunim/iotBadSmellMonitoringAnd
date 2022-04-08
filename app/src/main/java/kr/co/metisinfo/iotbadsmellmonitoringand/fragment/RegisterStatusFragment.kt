package kr.co.metisinfo.iotbadsmellmonitoringand.fragment

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import kr.co.metisinfo.iotbadsmellmonitoringand.MainApplication
import kr.co.metisinfo.iotbadsmellmonitoringand.R
import kr.co.metisinfo.iotbadsmellmonitoringand.databinding.FragmentRegisterStatusBinding

import kr.co.metisinfo.iotbadsmellmonitoringand.model.RegisterModel
import kr.co.metisinfo.iotbadsmellmonitoringand.model.RegisterResult
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//접수 현황 프래그먼트
class RegisterStatusFragment : Fragment() {

    private lateinit var binding: FragmentRegisterStatusBinding

    val resource: Resources = MainApplication.getContext().resources
    val instance = MainApplication.instance

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_register_status, container,false)

        return binding.root
    }

    //접수 현황 가져오기
    private fun getUserTodayRegisterInfo() {

        val userId = MainApplication.prefs.getString("userId", "")

        instance.apiService.getUserTodayRegisterInfo(userId).enqueue(object :
            Callback<RegisterResult> {
            override fun onResponse(call: Call<RegisterResult>, response: Response<RegisterResult>) {

                instance.registerStatusList= response.body()!!.data //접수 현황

                drawRegisterStatusLayout(instance.registerStatusList)
            }

            override fun onFailure(call: Call<RegisterResult>, t: Throwable) {
                Log.d("metis", "onFailure : " + t.message.toString())
                Toast.makeText(context, resource.getString(R.string.server_no_response), Toast.LENGTH_SHORT).show()
            }
        })
    }

    //접수 현황 레이아웃 그리기
    private fun drawRegisterStatusLayout(registerStatusList : List<RegisterModel>) {

        val registerStatusList = registerStatusList

        var imageView : ImageView? = null
        var textView : TextView? = null

        for (i in registerStatusList.indices) {

            when (i) {
                0 -> {
                    imageView = binding.registerFirstTimeImage
                    textView = binding.registerFirstTimeText
                }
                1 -> {
                    imageView = binding.registerSecondTimeImage
                    textView = binding.registerSecondTimeText
                }
                2 -> {
                    imageView = binding.registerThirdTimeImage
                    textView = binding.registerThirdTimeText
                }
            }

            when (registerStatusList[i].resultCode) {
                "001" -> imageView?.setBackgroundResource(R.drawable.done)
                "002" -> imageView?.setBackgroundResource(R.drawable.not_done)
                "003" -> imageView?.setBackgroundResource(R.drawable.not_yet)
            }

            textView?.text = registerStatusList[i].smellRegisterTimeName
        }
    }

    override fun onResume() {
        super.onResume()

        getUserTodayRegisterInfo()
    }
}