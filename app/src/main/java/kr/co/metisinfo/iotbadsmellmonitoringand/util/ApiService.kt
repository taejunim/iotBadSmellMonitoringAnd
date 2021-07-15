package kr.co.metisinfo.iotbadsmellmonitoringand.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kr.co.metisinfo.iotbadsmellmonitoringand.Constants
import kr.co.metisinfo.iotbadsmellmonitoringand.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
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

    //풍향 코드
    @GET("/api/codeListSelect")
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    fun getWindDirectionCode(
        @Query("codeGroup") codeGroup: String
    ): Call<CodeResult>

    //현재 날씨
    @GET("getUltraSrtFcst?serviceKey=${Constants.serviceKey}")
    fun getCurrentWeather(
        @Query("base_date") baseDate: String,
        @Query("base_time") baseTime: String,
        @Query("nx") nx: String,
        @Query("ny") ny: String,
        @Query("dataType") dataType : String,
        @Query("numOfRows") numOfRows : String
    ): Call<WeatherResponse>

    //금일 접수 현황
    @GET("/api/userTodayRegisterInfo")
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    fun getUserTodayRegisterInfo(
        @Query("userId") userId: String
    ): Call<RegisterResult>

    @Multipart
    @POST("/api/registerInsert")
    fun registerInsert(
        @Part("smellType") smellType: RequestBody,
        @Part("smellValue") smellValue: RequestBody,
        @Part("weatherState") weatherState: RequestBody,
        @Part("temperatureValue") temperatureValue: RequestBody,
        @Part("humidityValue") humidityValue: RequestBody,
        @Part("windDirectionValue") windDirectionValue: RequestBody,
        @Part("windSpeedValue") windSpeedValue: RequestBody,
        @Part("gpsX") gpsX: RequestBody,
        @Part("gpsY") gpsY: RequestBody,
        @Part("smellComment") smellComment: RequestBody,
        @Part("smellRegisterTime") smellRegisterTime: RequestBody,
        @Part("regId") regId: RequestBody,
        @Part imageFile1 : MultipartBody.Part?,
        @Part imageFile2 : MultipartBody.Part?,
        @Part imageFile3 : MultipartBody.Part?,
        @Part imageFile4 : MultipartBody.Part?,
        @Part imageFile5 : MultipartBody.Part?
    ): Call<ResponseResult>

    //비밀번호 변경
    @POST("/api/userPasswordChange")
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    fun userPasswordChange(
        @Body body: UserModel
    ): Call<ResponseResult>

    // 악취 이력 마스터
    @GET("/api/registerMasterHistory")
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    fun getRegisterMasterHistory(
        @Query("firstIndex") firstIndex: Int,
        @Query("recordCountPerPage") recordCountPerPage: Int,
        @Query("smellValue") smellValue: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("regId") regId: String

    ): Call<RegisterResult>


    companion object { // static 처럼 공유객체로 사용가능함. 모든 인스턴스가 공유하는 객체로서 동작함.

        fun create(): ApiService {

            val gson : Gson =   GsonBuilder().setLenient().create();

            return Retrofit.Builder()
                .baseUrl(Constants.serverUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(ApiService::class.java)
        }

        //날씨 API
        fun weatherApiCreate(): ApiService {

            return Retrofit.Builder()
                .baseUrl(Constants.weatherApiUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}