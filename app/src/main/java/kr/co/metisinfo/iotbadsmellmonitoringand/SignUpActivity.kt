package kr.co.metisinfo.iotbadsmellmonitoringand

import android.util.Log
import androidx.databinding.DataBindingUtil
import kr.co.metisinfo.iotbadsmellmonitoringand.databinding.ActivitySignUpBinding
import kr.co.metisinfo.iotbadsmellmonitoringand.model.ResponseResult
import kr.co.metisinfo.iotbadsmellmonitoringand.model.UserModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SignUpActivity : BaseActivity() {

    private lateinit var binding: ActivitySignUpBinding

    override fun initLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)

        binding.includeHeader.textTitle.setText(R.string.sign_up)
    }
    override fun setOnClickListener() {

        binding.includeHeader.backButton.setOnClickListener { finish() }

        binding.registrationButton.setOnClickListener {

            val data = UserModel("teerjwi24","teerjwi24","30","임태준","001","001","","")

            apiService.signIn(data).enqueue(object : Callback<ResponseResult> {

                override fun onResponse(
                    call: Call<ResponseResult>,
                    response: Response<ResponseResult>) {

                    Log.d("metis",response.toString())
                    Log.d("metis", "회원가입 결과 -> " + response.body().toString())
                }

                override fun onFailure(call: Call<ResponseResult>, t: Throwable) {
                    Log.d("metis",t.message.toString())
                    Log.d("metis", "fail")
                }


            })
        }
    }
}