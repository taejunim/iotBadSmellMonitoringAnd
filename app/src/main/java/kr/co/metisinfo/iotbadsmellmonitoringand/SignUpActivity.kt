package kr.co.metisinfo.iotbadsmellmonitoringand

import android.util.Log
import androidx.databinding.DataBindingUtil
import kr.co.metisinfo.iotbadsmellmonitoringand.databinding.ActivityLoginBinding


class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding

    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        val test: String? = "ab"

        var test2 = when {
            test == "aa" -> "aa"
            else -> "else문"
        }

        Log.d("metis", test2)

    }*/

    override fun initLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
    }
    override fun setOnClickListener() {

        binding.loginButton.setOnClickListener {
            val test: String? = "ab"

            var test2 = when {
                test == "aa" -> "aa"
                else -> "else문"
            }

            Log.d("metis", test2)
        }
    }
}