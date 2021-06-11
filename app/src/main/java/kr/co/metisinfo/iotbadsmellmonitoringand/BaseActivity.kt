package kr.co.metisinfo.iotbadsmellmonitoringand

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initLayout()
        setOnClickListener()
    }

    public abstract fun initLayout()

    public abstract fun setOnClickListener()
}