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

    //회원가입을 위한 인증번호
    @GET("/api/getNumberGen")
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    fun getCertificationNumber(
        @Query("userPhone") userPhone: String
    ): Call<CertificationResult>

    //풍향 코드
    @GET("/api/codeListSelect")
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    fun getApiData(
        @Query("codeGroup") codeGroup: String
    ): Call<CodeResult>

    //기상청 데이터를 가져오기 위한 x,y 좌표 API
    @GET("/api/userWeather")
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    fun getCoordinates(
        @Query("userRegion") userRegion: String,
    ): Call<CoordinatesResult>

    //지역 코드
    @GET("/api/getRegionList")
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    fun getRegionList(): Call<RegionResult>

    //현재 날씨
    @GET("getUltraSrtFcst")
    fun getCurrentWeather(
        @Query("serviceKey") serviceKey: String,
        @Query("base_date") baseDate: String,
        @Query("base_time") baseTime: String,
        @Query("nx") nx: String,
        @Query("ny") ny: String,
        @Query("dataType") dataType : String,
        @Query("numOfRows") numOfRows : String
    ): Call<WeatherResponse>

    //공지사항
    @GET("/api/noticeInfo")
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    fun getNoticeInfo(): Call<NoticeResult>

    //금일 접수 현황
    @GET("/api/userTodayRegisterInfo")
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    fun getUserTodayRegisterInfo(
        @Query("userId") userId: String
    ): Call<RegisterResult>

    //우리동네 악취 현황
    @GET("/api/myTownSmellInfo")
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    fun getStatisticsInfo(
        @Query("regionMaster") regionMaster: String,
        @Query("regionDetail") regionDetail: String
    ): Call<StatisticsResult>

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

    ): Call<HistoryResult>

    // 악취 이력 마스터
    @GET("/api/registerDetailHistory")
    @Headers(
            "Accept: application/json",
            "Content-Type: application/json"
    )
    fun getRegisterDetailHistory(
        @Query("smellRegisterNo") smellRegisterNo: String
    ): Call<ImageResult>

    // 아이디 찾기
    @GET("/api/userFindId")
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    fun userFindId(
        @Query("userId") userId: String
    ): Call<ResponseResult>

    // 등록 가능한 시간 체크하기 위한 서버 시간 가져오기
    @GET("/api/currentDate")
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    fun getCurrentDate(): Call<CurrentDateResult>

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