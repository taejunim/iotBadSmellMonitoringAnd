package kr.co.metisinfo.iotbadsmellmonitoringand.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kr.co.metisinfo.iotbadsmellmonitoringand.model.LoginResult
import kr.co.metisinfo.iotbadsmellmonitoringand.model.ResponseResult
import kr.co.metisinfo.iotbadsmellmonitoringand.model.UserModel
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {

    //로그인
    @POST("/api/userLogin")
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    fun userLogin(
        @Body body: UserModel
    ): Call<LoginResult>

    //회원가입
    @POST("/api/userJoinInsert")
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    fun signIn(
        @Body body: UserModel
    ): Call<ResponseResult>

    companion object { // static 처럼 공유객체로 사용가능함. 모든 인스턴스가 공유하는 객체로서 동작함.
        private const val BASE_URL = "http://101.101.219.152:8080" // 주소

        fun create(): ApiService {


            val gson : Gson =   GsonBuilder().setLenient().create();

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
//                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(ApiService::class.java)
        }
    }
}