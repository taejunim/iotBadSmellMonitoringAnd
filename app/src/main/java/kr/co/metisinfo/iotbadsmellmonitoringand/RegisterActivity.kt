package kr.co.metisinfo.iotbadsmellmonitoringand

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sangcomz.fishbun.FishBun
import com.sangcomz.fishbun.MimeType
import com.sangcomz.fishbun.adapter.image.impl.GlideAdapter
import kr.co.metisinfo.iotbadsmellmonitoringand.adapter.MultiImageAdapter
import kr.co.metisinfo.iotbadsmellmonitoringand.databinding.ActivityRegisterBinding
import kr.co.metisinfo.iotbadsmellmonitoringand.dialog.SmellTypeDialog
import kr.co.metisinfo.iotbadsmellmonitoringand.model.ResponseResult
import kr.co.metisinfo.iotbadsmellmonitoringand.model.WeatherModel
import kr.co.metisinfo.iotbadsmellmonitoringand.util.Utils.Companion.dateFormatter
import kr.co.metisinfo.iotbadsmellmonitoringand.util.Utils.Companion.ymdFormatter
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*

class RegisterActivity : BaseActivity(), SmellTypeDialog.SmellTypeDialogListener {

    private lateinit var binding: ActivityRegisterBinding

    private val smellTypeList = instance.smellTypeList

    private var selectedSmellTypeId = ""

    private var receivedIntensityId = ""
    private var receivedIntensityText = ""
    private var receivedIntensityResource = ""

    private var locationManager = instance.getSystemService(LOCATION_SERVICE) as LocationManager

    private var registerTime = ""

    var uriList = ArrayList<Uri>() // 이미지의 uri를 담을 ArrayList 객체
    var recyclerView : RecyclerView? = null // 이미지를 보여줄 리사이클러뷰
    var adapter : MultiImageAdapter? = null // 리사이클러뷰에 적용시킬 어댑터

    override fun onInputData(id: String, index: Int) {
        drawSmellTypeButton(id, index)
    }

    private fun drawSmellTypeButton(id: String, index: Int) {
        binding.smellTypeText.text = smellTypeList[index].codeIdName
        binding.smellTypeImage.setBackgroundResource(
            resource.getIdentifier("smell_$id","drawable","kr.co.metisinfo.iotbadsmellmonitoringand"))
        selectedSmellTypeId = smellTypeList[index].codeId
    }

    override fun initData() {

        receivedIntensityId = intent.getStringExtra("intensityId").toString()
        receivedIntensityText = intent.getStringExtra("intensityText").toString()
        receivedIntensityResource = intent.getStringExtra("intensityResource").toString()
    }

    override fun initLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register)

        Log.d("metis", "MainActivity 시작")
        binding.includeHeader.textTitle.setText(R.string.register) // 타이틀 제목
        binding.includeHeader.backButton.visibility = View.VISIBLE // 뒤로가기 버튼 보이게
        binding.includeHeader.navigationViewButton.visibility = View.GONE // 사이드 메뉴 버튼 안보이게

        drawSmellTypeButton("001", 0)

        binding.intensityButton.text = receivedIntensityText
        binding.intensityButton.setBackgroundResource(resource.getIdentifier(receivedIntensityResource,"drawable", "kr.co.metisinfo.iotbadsmellmonitoringand"))
        recyclerView = findViewById(R.id.register_image_view)
    }

    override fun setOnClickListener() {

        binding.includeHeader.backButton.setOnClickListener { finish() }

        binding.smellTypeImageLayout.setOnClickListener {

            val dialog = SmellTypeDialog()
            dialog.show(supportFragmentManager, "SmellTypeDialog")
        }

        binding.registrationButton.setOnClickListener {

            registerTime = getRegisterTime()

            //등록 시간이 아니면 return
            when (registerTime) {
                //"" -> Toast.makeText(this@RegisterActivity, resource.getString(R.string.register_not_register_time_text), Toast.LENGTH_SHORT).show()
                "" -> getWeatherApiData()
                else -> getWeatherApiData() //날씨 데이터
            }
        }

        binding.registerBlankLayout.setOnClickListener {

            FishBun.with(this@RegisterActivity)
                .setImageAdapter(GlideAdapter())
                .setMaxCount(5)
                .setMinCount(1)
                .setPickerSpanCount(5)
                .setActionBarColor(
                    Color.parseColor("#FFFFFF"),
                    Color.parseColor("#FFFFFF"),
                    false
                )
                .setActionBarTitleColor(Color.parseColor("#010101"))
                .setSelectedImages(uriList)
                .setAlbumSpanCount(2, 3)
                //.setButtonInAlbumActivity(false)
                .hasCameraInPickerPage(true)
                .exceptMimeType(arrayListOf(MimeType.GIF))
                .setReachLimitAutomaticClose(false)
                .setHomeAsUpIndicatorDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.back
                    )
                )
                .setDoneButtonDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.check_black
                    )
                )
                .setAllViewTitle("전체")
                .setActionBarTitle("사진을 선택해주세요")
                .textOnNothingSelected("최소 1개 이상 선택해주세요!")
                .startAlbum()

        }
    }

    private fun getRealPathFromURI(index: Int, contentURI: Uri): MultipartBody.Part? {
        val filePath: String?

        val cursor =
            MainApplication.instance.contentResolver.query(contentURI, null, null, null, null)
        if (cursor == null) {
            filePath = contentURI.path
        } else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            filePath = cursor.getString(idx)
            cursor.close()
        }

        val imageFile = File(filePath)

        var imageBody : RequestBody = RequestBody.create(MediaType.parse("image/*"),imageFile)

        val serverKey = "img$index"
        val serverFileName = "image$index.jpg"

        return MultipartBody.Part.createFormData(serverKey,serverFileName,imageBody)
    }

    // 앨범에서 액티비티로 돌아온 후 실행되는 메서드
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        binding.registerBlankLayout.visibility = View.GONE
        binding.registerAddedImageLayout.visibility = View.VISIBLE

        if (requestCode == 27 && resultCode == RESULT_OK) {

            uriList = data?.getParcelableArrayListExtra("intent_path") ?: arrayListOf()
            adapter = MultiImageAdapter(uriList, applicationContext)
            recyclerView!!.adapter = adapter // 리사이클러뷰에 어댑터 세팅
            recyclerView!!.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true) // 리사이클러뷰 수평 스크롤 적용
        }
    }

    /**
     * DATA CALLBACK
     */
    override fun callback(apiName: String, data: Any) {
        val weatherModel = data as WeatherModel
        Log.d("metis", "등록 callback data : $weatherModel")

        var weatherStatus = ""
        when (weatherModel.precipitationStatus) { // 강수형태가 0 이면 하늘상태 코드로

            "0" -> {
                when (weatherModel.skyStatus) {
                    "1" -> weatherStatus = "001" //맑음
                    "3" -> weatherStatus = "002" //구름많음
                    "4" -> weatherStatus = "003" //흐림
                }
            }
            "1" -> weatherStatus = "004" //비
            "2" -> weatherStatus = "005" //비/눈
            "3" -> weatherStatus = "006" //눈
            "4" -> weatherStatus = "007" //소나기
            "5" -> weatherStatus = "008" //빗방울
            "6" -> weatherStatus = "009" //빗방울/눈날림
            "7" -> weatherStatus = "010" //눈날림
            "" -> weatherStatus = "011" //기타
        }

        val locationMap = getLocation()

        val smellType = RequestBody.create(MediaType.parse("text/plain"), selectedSmellTypeId)
        val smellValue = RequestBody.create(MediaType.parse("text/plain"), receivedIntensityId)
        val weatherState = RequestBody.create(MediaType.parse("text/plain"), weatherStatus)
        val temperatureValue = RequestBody.create(MediaType.parse("text/plain"), weatherModel.temperature)
        val humidityValue = RequestBody.create(MediaType.parse("text/plain"), weatherModel.humidity)
        val windDirectionValue = RequestBody.create(MediaType.parse("text/plain"), String.format("%03d", instance.windDirectionMap.filter { weatherModel.windDirection == it.value }.keys.first().toInt() + 1))
        val windSpeedValue = RequestBody.create(MediaType.parse("text/plain"), weatherModel.windSpeed)
        val gpsX = RequestBody.create(MediaType.parse("text/plain"), locationMap["longitude"].toString())
        val gpsY = RequestBody.create(MediaType.parse("text/plain"), locationMap["latitude"].toString(),)
        val smellComment = RequestBody.create(MediaType.parse("text/plain"), binding.registerMemoInput.text.toString())
        val smellRegisterTime = RequestBody.create(MediaType.parse("text/plain"), registerTime)
        val regId = RequestBody.create(MediaType.parse("text/plain"), MainApplication.prefs.getString("userId", ""))

        var imageFile1: MultipartBody.Part? = try { getRealPathFromURI(1,uriList[0]) } catch (e: IndexOutOfBoundsException) { null }
        var imageFile2: MultipartBody.Part? = try { getRealPathFromURI(2,uriList[1]) } catch (e: IndexOutOfBoundsException) { null }
        var imageFile3: MultipartBody.Part? = try { getRealPathFromURI(3,uriList[2]) } catch (e: IndexOutOfBoundsException) { null }
        var imageFile4: MultipartBody.Part? = try { getRealPathFromURI(4,uriList[3]) } catch (e: IndexOutOfBoundsException) { null }
        var imageFile5: MultipartBody.Part? = try { getRealPathFromURI(5,uriList[4]) } catch (e: IndexOutOfBoundsException) { null }

        instance.apiService.registerInsert(
            smellType,
            smellValue,
            weatherState,
            temperatureValue,
            humidityValue,
            windDirectionValue,
            windSpeedValue,
            gpsX,
            gpsY,
            smellComment,
            smellRegisterTime,
            regId,
            imageFile1,
            imageFile2,
            imageFile3,
            imageFile4,
            imageFile5

        ).enqueue(object : Callback<ResponseResult> {
            override fun onResponse(call: Call<ResponseResult>, response: Response<ResponseResult>) {
                Log.d("metis",response.toString())
                Log.d("metis", " data -> " + data)
                Log.d("metis", " registerInsert getWindDirectionCode 결과 -> " + response.body().toString())
                Toast.makeText(this@RegisterActivity, resource.getString(R.string.register_success_text), Toast.LENGTH_SHORT).show()

                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed ({
                    finish()
                }, 2000)
            }

            override fun onFailure(call: Call<ResponseResult>, t: Throwable) {
                Log.d("metis", t.message.toString())
            }
        })
    }

    //접수 등록시 현재 좌표 구하기
    private fun getLocation() : Map<String, String> {

        var latitude = ""
        var longitude = ""

        val isGPSEnabled: Boolean = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled: Boolean = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        //매니페스트에 권한이 추가되어 있다해도 여기서 다시 한번 확인해야함
        if (Build.VERSION.SDK_INT >= 23 &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this@RegisterActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                0
            )
        } else {
            when { //프로바이더 제공자 활성화 여부 체크
                isNetworkEnabled -> {
                    val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) //인터넷기반으로 위치를 찾음

                    latitude = location?.latitude.toString()
                    longitude = location?.longitude.toString()
                }
                isGPSEnabled -> {
                    val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) //GPS 기반으로 위치를 찾음

                    latitude = location?.latitude.toString()
                    longitude = location?.longitude.toString()
                }
                else -> {

                }
            }
            //몇초 간격과 몇미터를 이동했을시에 호출되는 부분 - 주기적으로 위치 업데이트를 하고 싶다면 사용
            // ****주기적 업데이트를 사용하다가 사용안할시에는 반드시 해제 필요****
            /*lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000, //몇초
                    1F,   //몇미터
                    gpsLocationListener)
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    1000,
                    1F,
                    gpsLocationListener)
            //해제부분. 상황에 맞게 잘 구현하자
            lm.removeUpdates(gpsLocationListener)*/
        }

        return mapOf("latitude" to latitude, "longitude" to longitude)
    }

    //접수 등록 시간 가져오기
    private fun getRegisterTime() : String {

        var registerTime = ""

        calendar.time = Date()

        val today = ymdFormatter.format(calendar.time)
        val currentTime = calendar.time.time

        calendar.add(Calendar.DATE, 1)
        val tomorrow = ymdFormatter.format(calendar.time)
        Log.d("metis", "tomorrow : " + tomorrow)

        for (i in registerStatusList.indices) {

            val registerTimeArray = registerStatusList[i].smellRegisterTimeName.split(" ~ ")

            if (registerStatusList[i].resultCode != "001") {

                val startTime: Long? = dateFormatter.parse(today + " " + registerTimeArray[0]).time
                var endTime: Long?

                //endTime 이 00:00 일 때 날짜가 바뀌므로
                if (registerTimeArray[1] == "00:00") {
                    endTime = dateFormatter.parse(tomorrow + " " + registerTimeArray[1]).time
                } else {
                    endTime = dateFormatter.parse(today + " " + registerTimeArray[1]).time
                }

                //현재 시각이 시작 일시와 종료 일시 사이일 경우
                if (startTime!! < currentTime && endTime > currentTime ) {

                    registerTime = registerStatusList[i].smellRegisterTime
                    Log.d("metis", "startTime : " + startTime)
                    Log.d("metis", "endTime : " + endTime)
                    Log.d("metis", "registerTime : " + registerTime)
                }
            }
        }

        return registerTime
    }

}