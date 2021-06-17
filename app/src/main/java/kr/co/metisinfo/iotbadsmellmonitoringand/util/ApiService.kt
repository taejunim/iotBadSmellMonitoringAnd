package kr.co.metisinfo.iotbadsmellmonitoringand.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kr.co.metisinfo.iotbadsmellmonitoringand.model.LoginResult
import kr.co.metisinfo.iotbadsmellmonitoringand.model.ResponseResult
import kr.co.metisinfo.iotbadsmellmonitoringand.model.UserModel
import kr.co.metisinfo.iotbadsmellmonitoringand.model.WeatherResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

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

    //현재 날씨
    @GET("/getUltraSrtFcst?serviceKey=$serviceKey&dataType=$dataType&numOfRows=$numOfRows")
    @Headers(
        "Content-Type: application/json"
    )
    fun getCurrentWeather(
        @Query("base_date") baseDate: String,
        @Query("base_time") baseTime: String,
        @Query("nx") nx: String,
        @Query("ny") ny: String
    ): Call<WeatherResponse>

    companion object { // static 처럼 공유객체로 사용가능함. 모든 인스턴스가 공유하는 객체로서 동작함.
        private const val BASE_URL = "http://101.101.219.152:8080" // 주소
        private const val WEATHER_API_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService/" // 날씨 API 주소
        private const val serviceKey = "aDVsltIrJTOtDLpTA6qnVPhVhaT%2FaciIUGI30aiipGikIAAZOI4KxfVFBqW9q3s%2B3xgVzKx6c3gJdUVGaNJ9Bg%3D%3D" // 주소
        private const val dataType = "JSON" // 응답 자료 형식
        private const val numOfRows = "1000" // 한 페이지당 결과 수

        fun create(): ApiService {

            val gson : Gson =   GsonBuilder().setLenient().create();

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
//                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(ApiService::class.java)
        }

        fun weatherApiCreate(): ApiService {

            //val gson : Gson =   GsonBuilder().setLenient().create();

            return Retrofit.Builder()
                .baseUrl(WEATHER_API_URL)
//                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}