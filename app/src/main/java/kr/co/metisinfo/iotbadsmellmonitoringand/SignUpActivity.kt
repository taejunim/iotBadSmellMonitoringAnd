package kr.co.metisinfo.iotbadsmellmonitoringand

import android.graphics.Color
import android.graphics.drawable.StateListDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.databinding.DataBindingUtil
import kr.co.metisinfo.iotbadsmellmonitoringand.databinding.ActivitySignUpBinding
import kr.co.metisinfo.iotbadsmellmonitoringand.model.ResponseResult
import kr.co.metisinfo.iotbadsmellmonitoringand.model.UserModel
import kr.co.metisinfo.iotbadsmellmonitoringand.util.Utils.Companion.convertToDp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SignUpActivity : BaseActivity() {

    private lateinit var binding: ActivitySignUpBinding

    private var selectedRegionCode = ""

    override fun initData() {
    }

    override fun initLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)

        binding.includeHeader.textTitle.setText(R.string.sign_up)
        binding.includeHeader.backButton.visibility = View.VISIBLE
        binding.includeHeader.navigationViewButton.visibility = View.GONE

        //지역 레이아웃 그리기
        drawRegionGroup()
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
                val userRegion = selectedRegionCode

                val data = UserModel(userId,userPassword,userAge,userName,userGender,"","001","", userRegion)

                instance.apiService.signIn(data).enqueue(object : Callback<ResponseResult> {
                    override fun onResponse(call: Call<ResponseResult>, response: Response<ResponseResult>) {
                        Log.d("metis",response.toString())
                        Log.d("metis", "회원가입 결과 -> " + response.body().toString())

                        val result = response.body()?.result

                        if (result == "success") {

                            Toast.makeText(this@SignUpActivity, resource.getString(R.string.register_success_text), Toast.LENGTH_SHORT).show()

                            val handler = Handler(Looper.getMainLooper())
                            handler.postDelayed ({
                                finish()
                            }, 2000)

                        } else if (result == "fail") {
                            Toast.makeText(this@SignUpActivity, resource.getString(R.string.incorrect_data), Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ResponseResult>, t: Throwable) {
                        Log.d("metis", "onFailure : " + t.message.toString())
                    }
                })
            }
        }
    }

    /**
     * DATA CALLBACK
     */
    override fun callback(apiName: String, data: Any) {
        Log.d("metis", "callback data : $data")
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

        //지역
        else if (selectedRegionCode == "") {

            Toast.makeText(this, resource.getString(R.string.blank_user_region), Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    //지역 레이아웃 그리기
    private fun drawRegionGroup() {

        val radioGroupParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)

        val regionRadioGroup = RadioGroup(this)
        regionRadioGroup.layoutParams = radioGroupParams
        regionRadioGroup.setBackgroundResource(R.drawable.toggle_background)
        regionRadioGroup.orientation = RadioGroup.HORIZONTAL

        val radioButtonParams = LinearLayout.LayoutParams(convertToDp(0F), LinearLayout.LayoutParams.MATCH_PARENT)
        radioButtonParams.weight = 1F
        radioButtonParams.setMargins(convertToDp(2F),convertToDp(2F),0,convertToDp(2F))

        var firstRadioButton: RadioButton? = null

        val regionList = instance.regionList

        for (i in regionList.indices) {

            val regionRadioButton = RadioButton(this)

            regionRadioButton.id = i+1
            regionRadioButton.layoutParams = radioButtonParams
            regionRadioButton.setBackgroundResource(R.drawable.toggle_selected_button)
            regionRadioButton.gravity = Gravity.CENTER
            regionRadioButton.setTextColor(Color.WHITE)
            regionRadioButton.text = regionList[i].codeIdName
            regionRadioButton.buttonDrawable = StateListDrawable()
            regionRadioButton.setOnClickListener {
                selectedRegionCode = regionList[i].codeId
            }

            regionRadioGroup.addView(regionRadioButton)

            if (i == 0) {
                firstRadioButton = regionRadioButton
            }
        }

        regionRadioGroup.check(firstRadioButton!!.id)
        selectedRegionCode = regionList[0].codeId

        binding.regionToggleLayout.addView(regionRadioGroup)
    }
}