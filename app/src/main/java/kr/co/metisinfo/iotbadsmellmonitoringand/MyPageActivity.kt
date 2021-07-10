package kr.co.metisinfo.iotbadsmellmonitoringand

import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import kr.co.metisinfo.iotbadsmellmonitoringand.databinding.ActivityMyPageBinding


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
    }

    /**
     * DATA CALLBACK
     */
    override fun callback(apiName: String, data: Any) {
        Log.d("metis", "callback data : $data")
    }
}