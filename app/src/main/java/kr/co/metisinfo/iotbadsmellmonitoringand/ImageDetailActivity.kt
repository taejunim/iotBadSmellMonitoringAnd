package kr.co.metisinfo.iotbadsmellmonitoringand

import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import kr.co.metisinfo.iotbadsmellmonitoringand.databinding.ActivityImageDetailBinding

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
}