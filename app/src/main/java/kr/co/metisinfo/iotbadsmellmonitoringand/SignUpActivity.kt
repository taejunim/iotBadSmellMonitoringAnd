package kr.co.metisinfo.iotbadsmellmonitoringand

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import kr.co.metisinfo.iotbadsmellmonitoringand.databinding.ActivitySignUpBinding
import kr.co.metisinfo.iotbadsmellmonitoringand.model.*
import kr.co.metisinfo.iotbadsmellmonitoringand.util.Utils.Companion.checkRegex
import kr.co.metisinfo.iotbadsmellmonitoringand.util.Utils.Companion.convertToPixel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : BaseActivity() {

    private lateinit var binding: ActivitySignUpBinding

    private var selectedRegionMasterCodeId = ""
    private var selectedRegionMasterCodeName = ""
    private var selectedRegionDetailCodeId = ""
    private var selectedRegionDetailCodeName = ""

    private var isAvailableId = false

    private var regionMasterList: MutableList<SpinnerModel> = ArrayList<SpinnerModel>() //지역 마스터 리스트
    private var regionDetailList: MutableList<SpinnerModel> = ArrayList<SpinnerModel>() //지역 디테일 리스트

    private var userPhone = ""

    private var isCertificated = false
    private var certificationNumber = ""

    override fun initData() {
    }

    override fun initLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)

        binding.includeHeader.textTitle.setText(R.string.sign_up)
        binding.includeHeader.backButton.visibility = View.VISIBLE
        binding.includeHeader.navigationViewButton.visibility = View.GONE

        //지역 레이아웃 그리기
        drawRegionGroup()
    }
    override fun setOnClickListener() {

        binding.includeHeader.backButton.setOnClickListener { finish() }

        binding.registerDuplicationCheckButton.setOnClickListener {

            if (checkId()) {
                val userId = binding.signUpUserIdInput.text.toString()

                instance.apiService.userFindId(userId).enqueue(object : Callback<ResponseResult> {
                    override fun onResponse(call: Call<ResponseResult>, response: Response<ResponseResult>) {
                        Log.d("metis",response.toString())
                        Log.d("metis", "userFindId 결과 -> " + response.body().toString())

                        val result = response.body()!!.result

                        if(result == "fail") {
                            Toast.makeText(this@SignUpActivity, resource.getString(R.string.sign_up_available_user_id), Toast.LENGTH_SHORT).show()
                            isAvailableId = true
                        } else if (result == "success") {
                            Toast.makeText(this@SignUpActivity, resource.getString(R.string.sign_up_duplicate_user_id), Toast.LENGTH_SHORT).show()
                            isAvailableId = false
                        }
                    }

                    override fun onFailure(call: Call<ResponseResult>, t: Throwable) {
                        Toast.makeText(this@SignUpActivity, resource.getString(R.string.sign_in_ask_text), Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        binding.certificationRequestButton.setOnClickListener {

            instance.apiService.getCertificationNumber().enqueue(object : Callback<CertificationResult> {
                override fun onResponse(call: Call<CertificationResult>, response: Response<CertificationResult>) {
                    Log.d("metis",response.toString())
                    Log.d("metis", "getCertificationNumber 결과 -> " + response.body().toString())

                    if (response.body() != null) {

                        //인증요청 응답 성공
                        if (response.body()!!.result == "success") {
                            certificationNumber = response.body()!!.data.authNum //인증번호

                            userPhone = binding.signUpUserPhoneInputFirst.text.toString() + binding.signUpUserPhoneInputSecond.text.toString() + binding.signUpUserPhoneInputThird.text.toString() //입력한 휴대전화번호

                            isCertificated = true //인증 완료 플래그

                        }

                        //인증요청 API 오류
                        else if (response.body()!!.result == "fail") {
                            isCertificated = false
                            Toast.makeText(this@SignUpActivity, resource.getString(R.string.server_no_response), Toast.LENGTH_SHORT).show()
                        }

                    } else {

                        isCertificated = false
                        Toast.makeText(this@SignUpActivity, resource.getString(R.string.server_no_response), Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<CertificationResult>, t: Throwable) {
                    Toast.makeText(this@SignUpActivity, resource.getString(R.string.server_no_response), Toast.LENGTH_SHORT).show()
                }
            })
        }

        binding.signUpUserIdInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                isAvailableId = false
            }
        })

        clearEditTextValue(binding.signUpUserPhoneInputFirst)
        clearEditTextValue(binding.signUpUserPhoneInputSecond)
        clearEditTextValue(binding.signUpUserPhoneInputThird)

        binding.signUpUserPhoneInputFirst.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    if (s.length == 3) {
                        binding.signUpUserPhoneInputSecond.requestFocus()
                        binding.signUpUserPhoneInputSecond.isCursorVisible = true
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
                isCertificated = false
            }
        })

        binding.signUpUserPhoneInputSecond.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    if (s.length == 4) {
                        binding.signUpUserPhoneInputThird.requestFocus()
                        binding.signUpUserPhoneInputThird.isCursorVisible = true
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
                isCertificated = false
            }
        })

        binding.signUpUserPhoneInputThird.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    if (s.length == 4) {
                        hideKeyboard(binding.signUpUserPhoneInputThird)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
                isCertificated = false
            }
        })

        binding.registrationButton.setOnClickListener {

            if (checkId() && checkBlank()) {

                if (!isAvailableId) {
                    Toast.makeText(this@SignUpActivity, resource.getString(R.string.sign_up_not_clicked_check_duplicate_user_id), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val builder = AlertDialog.Builder(this@SignUpActivity)
                builder.setMessage("해당 내용으로 등록하시겠습니까?") //AlertDialog의 내용 부분
                builder.setPositiveButton("예", DialogInterface.OnClickListener { dialog, which ->

                    val userId = binding.signUpUserIdInput.text.toString()
                    val userPassword = binding.signUpPasswordInput.text.toString()
                    val userName = binding.signUpUserNameInput.text.toString()
                    val userAge = binding.signUpUserAgeInput.text.toString()
                    val userGender = getGender()
                    val userRegionMaster = selectedRegionMasterCodeId
                    val userRegionDetail = selectedRegionDetailCodeId
                    val userPhone = userPhone

                    val data = UserModel(userId,userPassword,userAge,userName,userGender,"","001","", userRegionMaster, userRegionDetail, userPhone)

                    instance.apiService.signIn(data).enqueue(object : Callback<ResponseResult> {
                        override fun onResponse(call: Call<ResponseResult>, response: Response<ResponseResult>) {
                            Log.d("metis",response.toString())
                            Log.d("metis", "회원가입 결과 -> " + response.body().toString())

                            val result = response.body()?.result

                            if (result == "success") {

                                Toast.makeText(this@SignUpActivity, resource.getString(R.string.register_success_text), Toast.LENGTH_SHORT).show()

                                val handler = Handler(Looper.getMainLooper())
                                handler.postDelayed ({
                                    finish()
                                }, 2000)

                            } else if (result == "fail") {
                                Toast.makeText(this@SignUpActivity, resource.getString(R.string.incorrect_data), Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<ResponseResult>, t: Throwable) {
                            Log.d("metis", "onFailure : " + t.message.toString())
                            Toast.makeText(this@SignUpActivity, resource.getString(R.string.server_no_response), Toast.LENGTH_SHORT).show()
                        }
                    })
                })
                builder.setNegativeButton("아니오", null)
                builder.create().show() //보이기
            }
        }
    }

    /**
     * DATA CALLBACK
     */
    override fun callback(apiName: String, data: Any) {
        Log.d("metis", "callback data : $data")
    }

    //선택된 성별 가져오기
    private fun getGender() : String {

        var gender = ""

        if (binding.genderToggle.checkedRadioButtonId == binding.genderMale.id) {

            Log.d("metis", "checkedRadioButtonId : " + binding.genderToggle.checkedRadioButtonId)
            Log.d("metis", "binding.genderMale.id : " + binding.genderMale.id)

            gender = "001"

        } else if (binding.genderToggle.checkedRadioButtonId == binding.genderFemale.id) {

            Log.d("metis", "checkedRadioButtonId : " + binding.genderToggle.checkedRadioButtonId)
            Log.d("metis", "binding.genderFemale.id : " + binding.genderFemale.id)

            gender = "002"
        }

        return gender
    }

    //아이디 체크
    private fun checkId(): Boolean {

        when {
            //아이디
            binding.signUpUserIdInput.text.toString().trim() == "" -> {

                Toast.makeText(this, resource.getString(R.string.blank_user_id), Toast.LENGTH_SHORT).show()
                return false
            }

            //아이디 길이 체크
            binding.signUpUserIdInput.text.toString().trim().length < 4 || binding.signUpUserIdInput.text.toString().trim().length > 20 -> {

                Toast.makeText(this, resource.getString(R.string.user_id_length_exceed), Toast.LENGTH_SHORT).show()
                return false
            }

            //아이디
            !checkRegex("id", binding.signUpUserIdInput.text.toString()) -> {

                Toast.makeText(this, resource.getString(R.string.sign_up_incorrect_form_user_id), Toast.LENGTH_SHORT).show()
                return false
            }
            else -> return true
        }
    }

    //빈칸 체크
    private fun checkBlank(): Boolean {

        when {
            //비밀번호
            binding.signUpPasswordInput.text.toString() == "" -> {

                Toast.makeText(this, resource.getString(R.string.blank_user_password), Toast.LENGTH_SHORT).show()
                return false
            }

            //비밀번호 길이 체크
            binding.signUpPasswordInput.text.toString().length < 4 || binding.signUpPasswordInput.text.toString().length > 15 -> {

                Toast.makeText(this, resource.getString(R.string.user_password_length_exceed), Toast.LENGTH_SHORT).show()
                return false
            }

            //비밀번호 확인
            binding.signUpPasswordConfirmInput.text.toString() == "" -> {

                Toast.makeText(this, resource.getString(R.string.blank_user_password_confirm), Toast.LENGTH_SHORT).show()
                return false
            }

            //비밀번호 길이 체크
            binding.signUpPasswordConfirmInput.text.toString().length < 4 || binding.signUpPasswordConfirmInput.text.toString().length > 15 -> {

                Toast.makeText(this, resource.getString(R.string.user_password_confirm_length_exceed), Toast.LENGTH_SHORT).show()
                return false
            }

            //비밀번호 일치
            binding.signUpPasswordInput.text.toString() != binding.signUpPasswordConfirmInput.text.toString() -> {

                Toast.makeText(this, resource.getString(R.string.blank_user_incorrect_password), Toast.LENGTH_SHORT).show()
                return false
            }

            //비밀번호 정규식
            !checkRegex("password", binding.signUpPasswordInput.text.toString()) -> {

                Toast.makeText(this, resource.getString(R.string.sign_up_incorrect_form_user_password), Toast.LENGTH_SHORT).show()
                return false
            }

            //이름
            binding.signUpUserNameInput.text.toString() == "" -> {

                Toast.makeText(this, resource.getString(R.string.blank_user_name), Toast.LENGTH_SHORT).show()
                return false
            }

            //이름 정규식
            !checkRegex("name", binding.signUpUserNameInput.text.toString()) -> {

                Toast.makeText(this, resource.getString(R.string.sign_up_incorrect_form_user_name), Toast.LENGTH_SHORT).show()
                return false
            }

            binding.signUpUserPhoneInputFirst.text.toString().trim().isEmpty() || binding.signUpUserPhoneInputSecond.text.toString().trim().isEmpty()
                    || binding.signUpUserPhoneInputThird.text.toString().trim().isEmpty() -> {

                Toast.makeText(this, resource.getString(R.string.blank_user_phone), Toast.LENGTH_SHORT).show()
                return false
            }

            !isCertificated -> {
                Toast.makeText(this, resource.getString(R.string.sign_up_not_clicked_certification_request_user_phone), Toast.LENGTH_SHORT).show()
                return false
            }

            binding.signUpCertificationNumberInput.text.toString().trim().isEmpty() -> {
                Toast.makeText(this, resource.getString(R.string.blank_certification_number), Toast.LENGTH_SHORT).show()
                return false
            }

            certificationNumber.trim() != binding.signUpCertificationNumberInput.text.toString().trim() -> {
                Toast.makeText(this, resource.getString(R.string.sign_up_incorrect_certification_number), Toast.LENGTH_SHORT).show()
                return false
            }

            //나이
            binding.signUpUserAgeInput.text.toString() == "" -> {

                Toast.makeText(this, resource.getString(R.string.blank_user_age), Toast.LENGTH_SHORT).show()
                return false
            }

            //나이 정규식
            !checkRegex("age", binding.signUpUserAgeInput.text.toString()) -> {

                Toast.makeText(this, resource.getString(R.string.sign_up_incorrect_form_user_age), Toast.LENGTH_SHORT).show()
                return false
            }

            //나이 범위
            Integer.parseInt(binding.signUpUserAgeInput.text.toString()) < 1 || Integer.parseInt(binding.signUpUserAgeInput.text.toString()) > 120 -> {

                Toast.makeText(this, resource.getString(R.string.sign_up_incorrect_form_user_age), Toast.LENGTH_SHORT).show()
                return false
            }

            //성별
            binding.genderToggle.checkedRadioButtonId == null -> {

                Toast.makeText(this, resource.getString(R.string.blank_user_gender), Toast.LENGTH_SHORT).show()
                return false
            }

            //지역
            binding.regionMasterSpinner.selectedItem == null || binding.regionDetailSpinner.selectedItem == null
                    || binding.regionMasterSpinner.selectedItem.toString() == "선택" || binding.regionDetailSpinner.selectedItem.toString() == "선택"
                    || selectedRegionMasterCodeId == "" || selectedRegionMasterCodeName == "" || selectedRegionDetailCodeId == "" || selectedRegionDetailCodeName == "" -> {

                Toast.makeText(this, resource.getString(R.string.blank_user_region), Toast.LENGTH_SHORT).show()

                return false
            }
            else -> return true
        }
    }

    //지역 레이아웃 그리기
    private fun drawRegionGroup() {

        //지역 마스터 spinner adapter
        val regionMasterAdapter = object : ArrayAdapter<SpinnerModel>(this,R.layout.item_spinner) {

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

                val v = super.getView(position, convertView, parent)

                (v.findViewById<View>(R.id.item_text) as TextView).setTextColor(Color.WHITE)

                if (position == count) {
                    (v.findViewById<View>(R.id.item_text) as TextView).text = "" //마지막 포지션의 textView 를 힌트 용으로 사용합니다.
                    (v.findViewById<View>(R.id.item_text) as TextView).setHintTextColor(Color.WHITE)
                    (v.findViewById<View>(R.id.item_text) as TextView).hint = getItem(count).toString() //아이템의 마지막 값을 불러와 hint로 추가해 줍니다.
                }

                return v
            }

            override fun getCount(): Int {
                return super.getCount() - 1 //마지막 아이템은 힌트용으로만 사용하기 때문에 getCount에 1을 빼줍니다.
            }
        }

        //지역 마스터 리스트에 추가
        for (i in instance.regionList.indices) {
            val regionObject = SpinnerModel(instance.regionList[i].mCodeId, instance.regionList[i].mCodeIdName)
            regionMasterList.add(regionObject)
        }

        regionMasterAdapter.addAll(regionMasterList)
        regionMasterAdapter.add(SpinnerModel("000","선택")) //마지막 항목을 "선택" 표시

        binding.regionMasterSpinner.adapter = regionMasterAdapter
        binding.regionMasterSpinner.setSelection(regionMasterAdapter.count) //spinner 초기값 "선택"
        binding.regionMasterSpinner.dropDownVerticalOffset = convertToPixel(30f).toInt() //background 높이 조정

        //지역 디테일 spinner adapter
        val regionDetailAdapter = object : ArrayAdapter<SpinnerModel>(this ,R.layout.item_spinner) {

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

                val v = super.getView(position, convertView, parent)

                (v.findViewById<View>(R.id.item_text) as TextView).setTextColor(Color.WHITE)

                if (position == count) {
                    (v.findViewById<View>(R.id.item_text) as TextView).text = "" //마지막 포지션의 textView 를 힌트 용으로 사용합니다.
                    (v.findViewById<View>(R.id.item_text) as TextView).setHintTextColor(Color.WHITE)
                    (v.findViewById<View>(R.id.item_text) as TextView).hint = getItem(count).toString() //아이템의 마지막 값을 불러와 hint로 추가해 줍니다.
                }

                return v
            }

            override fun getCount(): Int {
                return super.getCount() - 1 //마지막 아이템은 힌트용으로만 사용하기 때문에 getCount에 1을 빼줍니다.
            }
        }

        regionDetailList.add(SpinnerModel("000","선택"))

        regionDetailAdapter.addAll(regionDetailList)

        binding.regionDetailSpinner.adapter = regionDetailAdapter
        binding.regionDetailSpinner.dropDownVerticalOffset = convertToPixel(30f).toInt()

        //지역 마스터 spinner 에서 항목 선택시
        binding.regionMasterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

                //마지막 "선택" 항목 빼고 선택시
                if (position != regionMasterList.size) {

                    val spinnerModel : SpinnerModel = parent.getItemAtPosition(position) as SpinnerModel //spinner 에서 선택한 항목

                    val regionMasterObject: RegionMaster = instance.regionList[position] //API 로 받아온 지역 목록

                    if (spinnerModel.key == regionMasterObject.mCodeId && spinnerModel.value == regionMasterObject.mCodeIdName) {

                        selectedRegionMasterCodeId = spinnerModel.key
                        selectedRegionMasterCodeName = spinnerModel.value

                        regionDetailList.clear()
                        regionDetailAdapter.clear() //지역 마스터 선택시 지역 디테일도 초기값 "선택" 으로 표시 하려면 regionDetailAdapter 도 clear 해줘야 함

                        for (i in regionMasterObject.detail.indices) {
                            val regionObject = SpinnerModel(regionMasterObject.detail[i].dCodeId, regionMasterObject.detail[i].dCodeIdName)
                            regionDetailList.add(regionObject)
                        }

                        regionDetailAdapter.addAll(regionDetailList)
                        regionDetailAdapter.add(SpinnerModel("000","선택"))

                        binding.regionDetailSpinner.adapter = regionDetailAdapter
                        binding.regionDetailSpinner.setSelection(regionDetailAdapter.count)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        //지역 디테일 spinner 에서 항목 선택시
        binding.regionDetailSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected( parent: AdapterView<*>, view: View?, position: Int, id: Long) {

                if (position != regionDetailList.size) {

                    val spinnerModel : SpinnerModel = parent.getItemAtPosition(position) as SpinnerModel

                    val regionDetailObject: RegionDetail = instance.regionList[binding.regionMasterSpinner.selectedItemPosition].detail[position]

                    if (spinnerModel.key == regionDetailObject.dCodeId && spinnerModel.value == regionDetailObject.dCodeIdName) {

                        selectedRegionDetailCodeId = spinnerModel.key
                        selectedRegionDetailCodeName = spinnerModel.value
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }
}