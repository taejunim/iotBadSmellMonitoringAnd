package kr.co.metisinfo.iotbadsmellmonitoringand

import android.content.res.Resources
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.co.metisinfo.iotbadsmellmonitoringand.util.ApiService

abstract class BaseActivity : AppCompatActivity(){

    val apiService = ApiService.create()
    val resource: Resources = MainApplication.getContext().resources

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initLayout()
        setOnClickListener()
    }

    abstract fun initLayout()

    abstract fun setOnClickListener()
}