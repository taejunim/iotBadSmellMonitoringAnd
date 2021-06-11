package kr.co.metisinfo.iotbadsmellmonitoringand

import android.content.Intent
import android.util.Log
import androidx.databinding.DataBindingUtil
import kr.co.metisinfo.iotbadsmellmonitoringand.databinding.ActivityLoginBinding
import kr.co.metisinfo.iotbadsmellmonitoringand.model.ResponseResult
import kr.co.metisinfo.iotbadsmellmonitoringand.model.UserModel
import kr.co.metisinfo.iotbadsmellmonitoringand.util.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding

    val apiService = ApiService.create()

    override fun initLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
    }
    override fun setOnClickListener() {

        binding.loginButton.setOnClickListener {

            val data = UserModel("test123","test123")

            apiService.userLogin(data).enqueue(object : Callback<ResponseResult> {
                override fun onResponse(
                    call: Call<ResponseResult>,
                    response: Response<ResponseResult>
                ) {
                    Log.d("metis",response.toString())
                    Log.d("metis", "결과 -> " + response.body().toString())
                }

                override fun onFailure(call: Call<ResponseResult>, t: Throwable) {
                    Log.d("metis",t.message.toString())
                    Log.d("metis", "fail")
                }
            })
        }

        binding.signUpButton.setOnClickListener {

            var intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}