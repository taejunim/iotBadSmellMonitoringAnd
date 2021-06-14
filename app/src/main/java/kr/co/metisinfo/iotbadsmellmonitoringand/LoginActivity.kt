package kr.co.metisinfo.iotbadsmellmonitoringand

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import kr.co.metisinfo.iotbadsmellmonitoringand.databinding.ActivityLoginBinding
import kr.co.metisinfo.iotbadsmellmonitoringand.model.LoginResult
import kr.co.metisinfo.iotbadsmellmonitoringand.model.UserModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun initLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
    }
    override fun setOnClickListener() {

        binding.loginButton.setOnClickListener {

            if (checkBlank()) {

                val userId = binding.userId.text.toString()
                val userPassword = binding.userPassword.text.toString()
                val data = UserModel(userId,userPassword,"","","","","","")

                apiService.userLogin(data).enqueue(object : Callback<LoginResult> {
                    override fun onResponse(call: Call<LoginResult>, response: Response<LoginResult>) {
                        Log.d("metis",response.toString())
                        Log.d("metis", "결과 -> " + response.body().toString())

                        val result = response.body()?.result

                        if (result == "success") {

                            var intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()

                        } else if (result == "fail") {
                            Toast.makeText(this@LoginActivity, resource.getString(R.string.incorrect_data), Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<LoginResult>, t: Throwable) {
                        Log.d("metis",t.message.toString())
                        Log.d("metis", "onFailure : fail")

                    }
                })
            }
        }

        binding.signUpButton.setOnClickListener {

            var intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    //빈칸 체크
    private fun checkBlank(): Boolean {

        if (binding.userId.text.toString() == "") {

            Toast.makeText(this, resource.getString(R.string.blank_user_id), Toast.LENGTH_SHORT).show()
            return false

        } else if (binding.userPassword.text.toString() == "") {

            Toast.makeText(this, resource.getString(R.string.blank_user_password), Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}