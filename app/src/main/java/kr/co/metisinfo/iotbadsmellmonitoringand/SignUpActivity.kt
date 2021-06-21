package kr.co.metisinfo.iotbadsmellmonitoringand

import android.util.Log
import android.view.View
import android.widget.Toast
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
        binding.includeHeader.backButton.visibility = View.VISIBLE
        binding.includeHeader.sideMenuButton.visibility = View.GONE
    }
    override fun setOnClickListener() {

        binding.includeHeader.backButton.setOnClickListener { finish() }

        binding.registrationButton.setOnClickListener {

            if (checkBlank()) {

                val userId = binding.signUpUserIdInput.text.toString()
                val userPassword = binding.signUpPasswordInput.text.toString()
                val userName = binding.signUpUserNameInput.text.toString()
                val userAge = binding.signUpUserAgeInput.text.toString()
                val userGender = getGender()

                val data = UserModel(userId,userPassword,userAge,userName,userGender,"","001","")

                MainApplication.instance.apiService.signIn(data).enqueue(object : Callback<ResponseResult> {
                    override fun onResponse(call: Call<ResponseResult>, response: Response<ResponseResult>) {
                        Log.d("metis",response.toString())
                        Log.d("metis", "회원가입 결과 -> " + response.body().toString())

                        val result = response.body()?.result

                        if (result == "success") {

                            finish()

                        } else if (result == "fail") {
                            Toast.makeText(this@SignUpActivity, resource.getString(R.string.incorrect_data), Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ResponseResult>, t: Throwable) {
                        Log.d("metis",t.message.toString())
                        Log.d("metis", "onFailure : fail")

                    }
                })
            }
        }
    }

    //선택된 성별 가져오기
    private fun getGender() : String {

        var gender = ""

        if (binding.genderToggle.checkedRadioButtonId == binding.genderMale.id) {

            Log.d("metis", "checkedRadioButtonId : " + binding.genderToggle.checkedRadioButtonId)
            Log.d("metis", "binding.genderMale.id : " + binding.genderMale.id)

            gender = "001"

        } else if (binding.genderToggle.checkedRadioButtonId == binding.genderFemale.id) {

            Log.d("metis", "checkedRadioButtonId : " + binding.genderToggle.checkedRadioButtonId)
            Log.d("metis", "binding.genderFemale.id : " + binding.genderFemale.id)

            gender = "002"
        }

        return gender
    }

    //빈칸 체크
    private fun checkBlank(): Boolean {

        //아이디
        if (binding.signUpUserIdInput.text.toString() == "") {

            Toast.makeText(this, resource.getString(R.string.blank_user_id), Toast.LENGTH_SHORT).show()
            return false
        }

        //비밀번호
        else if (binding.signUpPasswordInput.text.toString() == "") {

            Toast.makeText(this, resource.getString(R.string.blank_user_password), Toast.LENGTH_SHORT).show()
            return false
        }

        //비밀번호 확인
        else if (binding.signUpPasswordConfirmInput.text.toString() == "") {

            Toast.makeText(this, resource.getString(R.string.blank_user_password_confirm), Toast.LENGTH_SHORT).show()
            return false
        }

        //비밀번호 일치
        else if (binding.signUpPasswordInput.text.toString() != binding.signUpPasswordConfirmInput.text.toString()) {

            Toast.makeText(this, resource.getString(R.string.blank_user_incorrect_password), Toast.LENGTH_SHORT).show()
            return false
        }

        //이름
        else if (binding.signUpUserNameInput.text.toString() == "") {

            Toast.makeText(this, resource.getString(R.string.blank_user_name), Toast.LENGTH_SHORT).show()
            return false
        }

        //나이
        else if (binding.signUpUserAgeInput.text.toString() == "") {

            Toast.makeText(this, resource.getString(R.string.blank_user_age), Toast.LENGTH_SHORT).show()
            return false
        }

        //성별
        else if (binding.genderToggle.checkedRadioButtonId == null) {

            Toast.makeText(this, resource.getString(R.string.blank_user_gender), Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}