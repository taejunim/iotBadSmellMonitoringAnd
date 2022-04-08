package kr.co.metisinfo.iotbadsmellmonitoringand

import android.content.Intent
import android.os.Build
import android.view.View
import android.view.WindowManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import kr.co.metisinfo.iotbadsmellmonitoringand.databinding.ActivityPrivacyPolicyBinding


class PrivacyPolicyActivity : BaseActivity() {

    private lateinit var binding: ActivityPrivacyPolicyBinding

    override fun initLayout() {

        binding = DataBindingUtil.setContentView(this, R.layout.activity_privacy_policy)

        binding.includeHeader.textTitle.setText(R.string.privacy_policy)
        binding.includeHeader.backButton.visibility = View.VISIBLE
        binding.includeHeader.navigationViewButton.visibility = View.GONE

        binding.webView.webViewClient = WebViewClient()

        val webSettings: WebSettings = binding.webView.settings //세부 세팅 등록

        webSettings.javaScriptEnabled = true // 웹페이지 자바스클비트 허용 여부
        webSettings.loadWithOverviewMode = true // 메타태그 허용 여부
        webSettings.useWideViewPort = true // 화면 사이즈 맞추기 허용 여부
        webSettings.setSupportZoom(false) // 화면 줌 허용 여부
        webSettings.builtInZoomControls = false // 화면 확대 축소 허용 여부
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE // 브라우저 캐시 허용 여부
        webSettings.domStorageEnabled = true // 로컬저장소 허용 여부

        if(Build.VERSION.SDK_INT >= 19) {
            binding.webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            binding.webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null)
        }

        window.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)

        binding.webView.loadUrl(Constants.serverUrl + "/agreement") // 웹뷰에 표시할 웹사이트 주소, 웹뷰 시작
    }

    override fun setOnClickListener() {

        binding.includeHeader.backButton.setOnClickListener { finish() }

        binding.confirmationButton.setOnClickListener {

            if (binding.privacyPolicyAgreementCheckBox.isChecked) {

                var intent = Intent(this, SignUpActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, resource.getString(R.string.privacy_policy_not_checked), Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun initData() {

    }

    override fun callback(apiName: String, data: Any) {

    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}