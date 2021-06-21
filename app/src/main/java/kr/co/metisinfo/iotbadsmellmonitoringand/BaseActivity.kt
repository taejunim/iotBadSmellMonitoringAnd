package kr.co.metisinfo.iotbadsmellmonitoringand

import android.content.res.Resources
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity(){

    val resource: Resources = MainApplication.getContext().resources

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initLayout()
        setOnClickListener()
        initData()
    }

    abstract fun initLayout()

    abstract fun setOnClickListener()

    abstract fun initData()
}