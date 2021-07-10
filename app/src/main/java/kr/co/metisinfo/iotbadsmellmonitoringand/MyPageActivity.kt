package kr.co.metisinfo.iotbadsmellmonitoringand

import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import kr.co.metisinfo.iotbadsmellmonitoringand.databinding.ActivityMyPageBinding
import kr.co.metisinfo.iotbadsmellmonitoringand.model.UserModel


class MyPageActivity : BaseActivity() {

    private lateinit var binding: ActivityMyPageBinding

    override fun initData() {
    }

    override fun initLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_page)

        Log.d("metis", "MyPageActivity 시작")
        binding.includeHeader.textTitle.setText(R.string.my_page) // 타이틀 제목
        binding.includeHeader.backButton.visibility = View.VISIBLE // 뒤로가기 버튼 보이게
        binding.includeHeader.navigationViewButton.visibility = View.GONE // 사이드 메뉴 버튼 안보이게

        binding.myPageUserInformation.navigationUserName.text = MainApplication.prefs.getString("userName", "")
        binding.myPageUserInformation.navigationUserId.text = MainApplication.prefs.getString("userId", "")

        var pushStatus = MainApplication.prefs.getBoolean("pushStatus", false)
        when (pushStatus) {
            true -> binding.myPagePushSwitch.isChecked = true
            false -> binding.myPagePushSwitch.isChecked = false
        }
    }
    override fun setOnClickListener() {
        binding.includeHeader.backButton.setOnClickListener { finish() }

        binding.myPagePushSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                Log.d("metis", "MyPageActivity on")
                MainApplication.prefs.setBoolean("pushStatus", true)
            } else{
                Log.d("metis", "MyPageActivity off")
                MainApplication.prefs.setBoolean("pushStatus", false)
            }
        }

        binding.modificationButton.setOnClickListener {

            if (checkBlank()) {
                val userId = MainApplication.prefs.getString("userId", "")
                val userPassword = binding.myPageNewPasswordInput.text.toString()
                val data = UserModel(userId,userPassword,"","","","","","","")

                instance.changePassword(data)
            }
        }
    }

    //빈칸 체크
    private fun checkBlank(): Boolean {

        when {
            binding.myPageCurrentPasswordInput.text.toString() == "" -> {

                Toast.makeText(this, resource.getString(R.string.my_page_blank_current_password_text), Toast.LENGTH_SHORT).show()
                return false
            }
            binding.myPageCurrentPasswordInput.text.toString() != MainApplication.prefs.getString("userPassword", "") -> {

                Toast.makeText(this, resource.getString(R.string.my_page_incorrect_current_password__text), Toast.LENGTH_SHORT).show()
                return false

            }
            binding.myPageNewPasswordInput.text.toString() == "" -> {

                Toast.makeText(this, resource.getString(R.string.my_page_blank_new_password_text), Toast.LENGTH_SHORT).show()
                return false
            }
            binding.myPageNewPasswordConfirmInput.text.toString() == "" -> {

                Toast.makeText(this, resource.getString(R.string.my_page_blank_new_password_confirm_text), Toast.LENGTH_SHORT).show()
                return false
            }
            binding.myPageNewPasswordInput.text.toString() != binding.myPageNewPasswordConfirmInput.text.toString() -> {

                Toast.makeText(this, resource.getString(R.string.my_page_incorrect_new_password_text), Toast.LENGTH_SHORT).show()
                return false
            }
            else -> return true
        }

    }

    /**
     * DATA CALLBACK
     */
    override fun callback(apiName: String, data: Any) {
        Log.d("metis", "callback data : $data")
    }
}