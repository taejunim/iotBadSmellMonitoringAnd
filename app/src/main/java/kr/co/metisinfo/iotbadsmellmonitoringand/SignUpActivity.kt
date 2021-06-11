package kr.co.metisinfo.iotbadsmellmonitoringand

import androidx.databinding.DataBindingUtil
import kr.co.metisinfo.iotbadsmellmonitoringand.databinding.ActivitySignUpBinding


class SignUpActivity : BaseActivity() {

    private lateinit var binding: ActivitySignUpBinding

    override fun initLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)

        binding.includeHeader.textTitle.setText(R.string.sign_up)
    }
    override fun setOnClickListener() {

        binding.includeHeader.backButton.setOnClickListener { finish() }
    }
}