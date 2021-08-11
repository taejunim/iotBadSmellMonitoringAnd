package kr.co.metisinfo.iotbadsmellmonitoringand

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.sangcomz.fishbun.FishBun
import com.sangcomz.fishbun.MimeType
import com.sangcomz.fishbun.adapter.image.impl.GlideAdapter
import kr.co.metisinfo.iotbadsmellmonitoringand.adapter.MultiImageAdapter
import kr.co.metisinfo.iotbadsmellmonitoringand.databinding.ActivityRegisterBinding
import kr.co.metisinfo.iotbadsmellmonitoringand.dialog.SmellTypeDialog
import kr.co.metisinfo.iotbadsmellmonitoringand.model.CurrentDateResult
import kr.co.metisinfo.iotbadsmellmonitoringand.model.ResponseResult
import kr.co.metisinfo.iotbadsmellmonitoringand.model.WeatherModel
import kr.co.metisinfo.iotbadsmellmonitoringand.util.Utils.Companion.convertToDp
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

    private var registerTime = ""

    var uriList = ArrayList<Uri>() // 이미지의 uri를 담을 ArrayList 객체
    var recyclerView : RecyclerView? = null // 이미지를 보여줄 리사이클러뷰
    var adapter : MultiImageAdapter? = null // 리사이클러뷰에 적용시킬 어댑터

    private lateinit var imageLayoutParams : RelativeLayout.LayoutParams

    override fun onInputData(id: String, index: Int) {
        drawSmellTypeButton(id, index)
    }

    private fun drawSmellTypeButton(id: String, index: Int) {
        binding.smellTypeText.text = smellTypeList[index].codeIdName
        binding.smellTypeText.setTextColor(Color.BLACK)
        binding.smellTypeImage.setBackgroundResource(resource.getIdentifier("smell_$id","drawable","kr.co.metisinfo.iotbadsmellmonitoringand"))
        binding.smellTypeImage.layoutParams = imageLayoutParams
        binding.smellTypeLayout.setBackgroundResource(R.drawable.smell_type_button)
        selectedSmellTypeId = smellTypeList[index].codeId
    }

    override fun initData() {
        receivedIntensityId = intent.getStringExtra("intensityId").toString()
        receivedIntensityText = intent.getStringExtra("intensityText").toString()
        receivedIntensityResource = intent.getStringExtra("intensityResource").toString()
    }

    override fun initLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register)

        binding.includeHeader.textTitle.setText(R.string.register) // 타이틀 제목
        binding.includeHeader.backButton.visibility = View.VISIBLE // 뒤로가기 버튼 보이게
        binding.includeHeader.navigationViewButton.visibility = View.GONE // 사이드 메뉴 버튼 안보이게

        binding.intensityButton.text = receivedIntensityText
        binding.intensityButton.setBackgroundResource(resource.getIdentifier(receivedIntensityResource,"drawable", "kr.co.metisinfo.iotbadsmellmonitoringand"))

        recyclerView = findViewById(R.id.register_image_view)

        imageLayoutParams = RelativeLayout.LayoutParams(convertToDp(60F), convertToDp(60F))
        imageLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
        imageLayoutParams.setMargins(0,convertToDp(20F),0,0)
    }

    override fun setOnClickListener() {

        binding.includeHeader.backButton.setOnClickListener { finish() }

        binding.smellTypeImageLayout.setOnClickListener {

            val dialog = SmellTypeDialog()
            dialog.show(supportFragmentManager, "SmellTypeDialog")
        }

        binding.registrationButton.setOnClickListener {

            //위치 권한 체크
            if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this@RegisterActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION) ,0)

                Log.d("metis","위치 권한 팝업 출력")

                //사용자가 승인 거절을 누른경우
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Log.d("metis","사용자가 승인 거절을 누른경우")
                }

                //사용자가 승인 거절과 동시에 다시 표시하지 않기 옵션을 선택한 경우
                else {
                    Log.d("metis","사용자가 승인 거절과 동시에 다시 표시하지 않기 옵션을 선택한 경우")
                    val snackBar = Snackbar.make(binding.registerMain,R.string.permission_text, Snackbar.LENGTH_INDEFINITE)
                    snackBar.setAction ("확인") {
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts("package",packageName,null)
                        intent.data = uri
                        startActivity(intent)
                    }
                    snackBar.show()
                }
            }

            //위치 권한있을 시
            else {

                if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

                    val snackBar = Snackbar.make(binding.registerMain,R.string.location_text, Snackbar.LENGTH_INDEFINITE)
                    snackBar.setAction ("확인") {
                        val gpsOptionsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivity(gpsOptionsIntent)
                    }
                    snackBar.show()

                } else {

                    checkCurrentDateBeforeRegister()
                }
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

        binding.registerRefreshButton.setOnClickListener {
            binding.registerBlankLayout.visibility = View.VISIBLE
            binding.registerAddedImageLayout.visibility = View.GONE
            uriList.clear()
            binding.registerUploadImageCountText.text = "0/5"
        }

        binding.registerMemoInput.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                binding.registerScrollView.smoothScrollTo(0, binding.registerScrollView.bottom)
            }
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

        if (requestCode == 27 && resultCode == RESULT_OK) {

            binding.registerBlankLayout.visibility = View.GONE
            binding.registerAddedImageLayout.visibility = View.VISIBLE

            uriList = data?.getParcelableArrayListExtra("intent_path") ?: arrayListOf()
            adapter = MultiImageAdapter(uriList, applicationContext)
            recyclerView!!.adapter = adapter // 리사이클러뷰에 어댑터 세팅
            recyclerView!!.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true) // 리사이클러뷰 수평 스크롤 적용
            binding.registerUploadImageCountText.text = uriList.size.toString() + "/5"

            binding.registerScrollView.scrollTo(0, binding.registrationButton.top)
        }
    }

    /**
     * DATA CALLBACK
     */
    override fun callback(apiName: String, data: Any) {

        when (apiName) {
            "serverDateReceived" -> {
                val currentDate = data as Date
                registerTime = getRegisterTime(currentDate)

                if (selectedSmellTypeId == "") {
                    Toast.makeText(this@RegisterActivity, resource.getString(R.string.register_unselected_smell_type_text), Toast.LENGTH_SHORT).show()
                } else {
                    when (registerTime) {
                        //등록 시간이 아니면 return
                        "" -> Toast.makeText(this@RegisterActivity, resource.getString(R.string.register_not_register_time_text), Toast.LENGTH_SHORT).show()
                        /*"" -> {
                            val builder = AlertDialog.Builder(this@RegisterActivity)
                            builder.setMessage("해당 내용으로 등록하시겠습니까?") //AlertDialog의 내용 부분
                            builder.setPositiveButton("예", DialogInterface.OnClickListener { dialog, which ->
                                //getWeatherApiData() //날씨 데이터 받은 후 등록
                                //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
                                getLocation()
                            })
                            builder.setNegativeButton("아니오", null)
                            builder.create().show() //보이기
                        }*/

                        else -> {
                            val builder = AlertDialog.Builder(this@RegisterActivity)
                            builder.setMessage("해당 내용으로 등록하시겠습니까?") //AlertDialog의 내용 부분
                            builder.setPositiveButton("예", DialogInterface.OnClickListener { dialog, which ->
                                //getWeatherApiData() //날씨 데이터 받은 후 등록
                                getLocation()
                            })
                            builder.setNegativeButton("아니오", null)
                            builder.create().show() //보이기
                        }
                    }
                }

            }

            "location" -> {
                getWeatherApiData() //날씨 데이터 받은 후 등록
            }

            "weather" -> {
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

                //val locationMap = getLocation()

                Log.d("metis", " locationMap[\"longitude\"].toString() -> " + locationMap["longitude"].toString())
                Log.d("metis", " locationMap[\"latitude\"].toString() -> " + locationMap["latitude"].toString())

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
                        Toast.makeText(this@RegisterActivity, resource.getString(R.string.register_fail_text), Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }

    //접수 등록 시간 가져오기
    private fun getRegisterTime(date: Date) : String {

        var registerTime = ""

        //calendar.time = Date()
        calendar.time = date

        val today = ymdFormatter.format(calendar.time)
        val currentTime = calendar.time.time

        calendar.add(Calendar.DATE, 1)
        val tomorrow = ymdFormatter.format(calendar.time)
        Log.d("metis", "tomorrow : " + tomorrow)

        for (i in instance.registerStatusList.indices) {

            val registerTimeArray = instance.registerStatusList[i].smellRegisterTimeName.split(" ~ ")

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

                registerTime = instance.registerStatusList[i].smellRegisterTime
                Log.d("metis", "startTime : " + startTime)
                Log.d("metis", "endTime : " + endTime)
                Log.d("metis", "registerTime : " + registerTime)
            }
        }

        return registerTime
    }

    private fun checkCurrentDate() {

        instance.apiService.getCurrentDate().enqueue(object : Callback<CurrentDateResult> {
            override fun onResponse(call: Call<CurrentDateResult>, response: Response<CurrentDateResult>) {

                val result = response.body()!!.result

                if (result == "success") {

                    val currentDate = dateFormatter.parse(response.body()!!.data)

                    if (getRegisterTime(currentDate!!) == "") {
                        binding.registrationButton.setBackgroundResource(R.drawable.round_gray_button)
                    } else {
                        binding.registrationButton.setBackgroundResource(R.drawable.round_blue_button)
                    }
                } else {
                    if (getRegisterTime(Date()) == "") {
                        binding.registrationButton.setBackgroundResource(R.drawable.round_gray_button)
                    } else {
                        binding.registrationButton.setBackgroundResource(R.drawable.round_blue_button)
                    }
                }
            }

            override fun onFailure(call: Call<CurrentDateResult>, t: Throwable) {
                Log.d("metis", "onFailure : " + t.message.toString())
                Toast.makeText(this@RegisterActivity, resource.getString(R.string.server_no_response), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkCurrentDateBeforeRegister () {
        instance.apiService.getCurrentDate().enqueue(object : Callback<CurrentDateResult> {
            override fun onResponse(call: Call<CurrentDateResult>, response: Response<CurrentDateResult>) {

                val result = response.body()!!.result

                if (result == "success") {

                    val currentDate = dateFormatter.parse(response.body()!!.data)

                    callback("serverDateReceived",currentDate)

                } else {
                    Toast.makeText(this@RegisterActivity, resource.getString(R.string.register_impossible_registration_text), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CurrentDateResult>, t: Throwable) {
                Log.d("metis", "onFailure : " + t.message.toString())
                Toast.makeText(this@RegisterActivity, resource.getString(R.string.server_no_response), Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()

        checkCurrentDate()
    }

}