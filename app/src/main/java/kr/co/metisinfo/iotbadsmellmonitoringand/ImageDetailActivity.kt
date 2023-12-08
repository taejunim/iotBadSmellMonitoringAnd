package kr.co.metisinfo.iotbadsmellmonitoringand

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import kr.co.metisinfo.iotbadsmellmonitoringand.databinding.ActivityImageDetailBinding
import kr.co.metisinfo.iotbadsmellmonitoringand.model.LoginResult
import kr.co.metisinfo.iotbadsmellmonitoringand.model.UserModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ImageDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityImageDetailBinding

    var receivedImageUrl = ""

    override fun initLayout() {
        binding = DataBindingUtil.setContentView(this,R.layout.activity_image_detail)

        val imageLayoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        imageLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT)

        var imageView = ImageView(this)
        imageView.layoutParams = imageLayoutParams
        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE

        Glide.with(this).load(receivedImageUrl).into(imageView)

        binding.imageDetailLayout.addView(imageView)
    }

    override fun setOnClickListener() {
        binding.imageDetailCloseButton.setOnClickListener {
            finish()
        }
    }

    override fun initData() {
        receivedImageUrl = intent.getStringExtra("imageUrl").toString()
    }

    override fun callback(apiName: String, data: Any) {

    }

    override fun onResume() {
        super.onResume()

        val userId = MainApplication.prefs.getString("userId","")
        val userPassword = MainApplication.prefs.getString("userPassword", "")
        val data = UserModel(userId,userPassword,"","","","","","","","","","","")


        instance.apiService.userLogin(data).enqueue(object : Callback<LoginResult> {
            override fun onResponse(call: Call<LoginResult>, response: Response<LoginResult>) {


                val result = response.body()?.result
                val userData: UserModel = response.body()!!.data

                if (result != "success") {

                    MainApplication.prefs.setBoolean("isLogin", false)
                    MainApplication.prefs.setString("userId", "")
                    MainApplication.prefs.setString("userName", "")
                    MainApplication.prefs.setString("userPassword", "")
                    MainApplication.prefs.setString("userRegionMaster", "")
                    MainApplication.prefs.setString("userRegionMasterName", "")
                    MainApplication.prefs.setString("userRegionDetail", "")

                    Toast.makeText(this@ImageDetailActivity, resource.getString(R.string.sign_in_status), Toast.LENGTH_LONG).show()

                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed ({
                        var intent = Intent(this@ImageDetailActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK  // 최상단 액티비티 제외한 모든 액티비티 제거
                        startActivity(intent)
                        finish()
                    }, 2000)

                }
            }

            override fun onFailure(call: Call<LoginResult>, t: Throwable) {
                Log.d("metis", "onFailure : " + t.message.toString())
            }
        })

    }
}