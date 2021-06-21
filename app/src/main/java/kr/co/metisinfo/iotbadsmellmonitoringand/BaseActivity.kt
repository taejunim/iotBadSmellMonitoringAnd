package kr.co.metisinfo.iotbadsmellmonitoringand

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kr.co.metisinfo.iotbadsmellmonitoringand.model.CodeModel
import kr.co.metisinfo.iotbadsmellmonitoringand.model.WindDirectionCodeResult
import kr.co.metisinfo.iotbadsmellmonitoringand.util.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

abstract class BaseActivity : AppCompatActivity(){

    val apiService = ApiService.create()
    val resource: Resources = MainApplication.getContext().resources
    val windDirectionMap: MutableMap<String, String> = mutableMapOf()   //풍향 코드

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initLayout()
        setOnClickListener()

        //풍향 코드 API
        apiService.getWindDirectionCode("WND").enqueue(object : Callback<WindDirectionCodeResult> {
            override fun onResponse(call: Call<WindDirectionCodeResult>, response: Response<WindDirectionCodeResult>) {
                Log.d("metis",response.toString())
                Log.d("metis", "getWindDirectionCode 결과 -> " + response.body().toString())

                val dataList: List<CodeModel> = response.body()!!.data

                for (i in dataList.indices) {
                    val convertedValue = (Integer.parseInt(dataList[i].codeId) - 1).toString() //풍향 변환값 => CODE_ID를 정수로 변환후 -1 한 값
                    val directionName = dataList[i].codeIdName //풍향

                    windDirectionMap.put(convertedValue,directionName)
                }

                Log.d("metis", "windDirectionMap : " + windDirectionMap)
            }

            override fun onFailure(call: Call<WindDirectionCodeResult>, t: Throwable) {
                Log.d("metis",t.message.toString())
                Log.d("metis", "onFailure : fail")
            }
        })
    }

    abstract fun initLayout()

    abstract fun setOnClickListener()
}