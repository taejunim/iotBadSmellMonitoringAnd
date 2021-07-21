package kr.co.metisinfo.iotbadsmellmonitoringand

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.inputmethod.EditorInfo
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

    override fun initData() {

        checkLocationPermission()
    }

    override fun initLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
    }
    override fun setOnClickListener() {

        binding.loginButton.setOnClickListener {

            if (checkBlank()) {
                login()
            }
        }

        binding.signUpButton.setOnClickListener {

            var intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.lostPasswordText.setOnClickListener {
            Toast.makeText(this@LoginActivity, resource.getString(R.string.sign_in_ask_text), Toast.LENGTH_SHORT).show()
        }
        
        binding.userPassword.setOnEditorActionListener { v, actionId, event ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(binding.userPassword)
                login()
                handled = true
            }
            handled
        }
    }

    private fun login() {
        val userId = binding.userId.text.toString()
        val userPassword = binding.userPassword.text.toString()
        val data = UserModel(userId,userPassword,"","","","","","","")

        instance.apiService.userLogin(data).enqueue(object : Callback<LoginResult> {
            override fun onResponse(call: Call<LoginResult>, response: Response<LoginResult>) {
                Log.d("metis",response.toString())
                Log.d("metis", "결과 -> " + response.body().toString())

                val result = response.body()?.result
                val userData: UserModel = response.body()!!.data

                if (result == "success") {

                    MainApplication.prefs.setBoolean("isLogin", true)
                    MainApplication.prefs.setString("userId", userData.userId)
                    MainApplication.prefs.setString("userName", userData.userName)
                    MainApplication.prefs.setString("userPassword", userPassword)

                    val initialRun = MainApplication.prefs.getBoolean("initialRun", true)
                    if (initialRun) {
                        Log.d("metis", "==================== initialRun -> " + initialRun)
                        MainApplication.prefs.setBoolean("pushStatus", true)
                        MainApplication.prefs.setBoolean("initialRun", false)
                    }


                    Toast.makeText(this@LoginActivity, resource.getString(R.string.sign_in_welcome_text), Toast.LENGTH_SHORT).show()

                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed ({
                        var intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }, 1000)

                } else if (result == "fail") {
                    Toast.makeText(this@LoginActivity, resource.getString(R.string.incorrect_data), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResult>, t: Throwable) {
                Log.d("metis", "onFailure : " + t.message.toString())
                Toast.makeText(this@LoginActivity, resource.getString(R.string.server_no_response), Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    /**
     * DATA CALLBACK
     */
    override fun callback(apiName: String, data: Any) {
        Log.d("metis", "callback data : $data")
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