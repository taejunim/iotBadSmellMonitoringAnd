package kr.co.metisinfo.iotbadsmellmonitoringand

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.metisinfo.iotbadsmellmonitoringand.adapter.ItemRegisterHistroyRecyclerViewAdapter
import kr.co.metisinfo.iotbadsmellmonitoringand.databinding.ActivityHistoryBinding
import kr.co.metisinfo.iotbadsmellmonitoringand.model.RegisterModel
import kr.co.metisinfo.iotbadsmellmonitoringand.model.RegisterResult
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

/**
 * @ Class Name   : HistoryActivity.kt
 * @ Modification : REGISTER HISTORY ACTIVITY CLASS.
 * @
 * @ 최초 생성일      최초 생성자
 * @ ---------    ---------
 * @ 2021.06.21.    고재훈
 * @
 * @  수정일          수정자
 * @ ---------    ---------
 * @
 **/

class HistoryActivity : BaseActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var binding: ActivityHistoryBinding
    private var selectYYYY      = 0                                                                 // 조회 일자 선택 - 연도
    private var selectMM        = 0                                                                 // 조회 일자 선택 - 월
    private var selectDD        = 0                                                                 // 조회 일자 선택 - 일
    private var selectDateGbn   = ""                                                                // 조회 일자 시작 또는 종료 구분을 위한 변수

    private lateinit var recyclerAdapter: ItemRegisterHistroyRecyclerViewAdapter

    private var smellValueArray: List<String> = ArrayList()
    private var selectedSmellValeCode = ""      // 선택된 악취 타입 코드
    private var selectedStartDate = ""          // 선택된 시작 일자
    private var selectedEndDate = ""            // 선택된 종료 일자

    private var pageNum = 0
    private val pageCount = 10

    private lateinit var historyResult: List<RegisterModel>

    /**
     * ACTIVITY INIT DATA
     */
    override fun initData() {

        //selectedStartDate = today
        selectedStartDate = "2021-01-01"
        selectedEndDate = today
        selectedSmellValeCode = instance.intensityList[0].codeId

    }

    
    /**
     * ACTIVITY INIT
     */
    override fun initLayout() {

        binding = DataBindingUtil.setContentView(this, R.layout.activity_history)           // XML BIND

        Log.d("metis","HistoryActivity 시작")
        binding.includeHeader.textTitle.setText(R.string.history)                                   // 타이틀 제목
        binding.includeHeader.backButton.visibility = View.GONE                                     // 뒤로가기 버튼 안보이게
        binding.includeHeader.navigationViewButton.visibility = View.VISIBLE                              // 사이드 메뉴 버튼 보이게

        binding.searchStartDateText.text = "2021-01-01"
        binding.searchEndDateText.text = today

        getRegisterMasterHistory()

        setAdpater()
    }

    /**
     * ONCLICK LISTENER SET
     */
    override fun setOnClickListener() {

        binding.searchStartDateText.setOnClickListener(this::clickDatePicker)
        binding.searchEndDateText.setOnClickListener(this::clickDatePicker)
        binding.searchSmellValue.setOnClickListener { binding.spinnerSearchSmellValue.performClick() }
        binding.searchSmellValueDropdown.setOnClickListener { binding.spinnerSearchSmellValue.performClick() }
    }

    /**
     * 악취 등록 이력 가져오기
     */
    fun getRegisterMasterHistory() {

        val userId = MainApplication.prefs.getString("userId", "")

        Log.d("metis", "pageNum : $pageNum")
        Log.d("metis", "pageCount : $pageCount")
        Log.d("metis", "selectedSmellValeCode : $selectedSmellValeCode")
        Log.d("metis", "selectedStartDate : $selectedStartDate")
        Log.d("metis", "selectedEndDate : $selectedEndDate")

        instance.apiService.getRegisterMasterHistory(pageNum, pageCount, selectedSmellValeCode, selectedStartDate, selectedEndDate, userId).enqueue(object : Callback<RegisterResult> {
            override fun onResponse(call: Call<RegisterResult>, response: Response<RegisterResult>) {

                Log.d("metis",response.toString())
                Log.d("metis", userId + " getRegisterMasterHistory 결과 -> " + response.body().toString())

                historyResult = response.body()!!.data //접수 현황

                if(historyResult != null) {
                    for (historyModel in historyResult) {

                        Log.d( "metis", "smellRegisterTimeName : " + historyModel.smellRegisterTimeName)
                        Log.d("metis", "smellRegisterTime : " + historyModel.smellRegisterTime)
                        Log.d("metis", "regDt : " + historyModel.regDt)

                    }
                }

                callback("registerStatus", historyResult)
            }

            override fun onFailure(call: Call<RegisterResult>, t: Throwable) {
                Log.d("metis", "onFailure : " + t.message.toString())
            }
        })
    }

    /**
     * 악취 강도 Spinner
     */
    fun setAdpater() {

        val smellValue : ArrayList<String> = ArrayList<String>()

        for (smellTypeCode in instance.intensityList) {
            smellValue.add(smellTypeCode.codeIdName)
        }
        smellValueArray = smellValue

        binding.spinnerSearchSmellValue.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, smellValueArray)

        binding.spinnerSearchSmellValue.onItemSelectedListener = this
    }

    fun initRecycler() {

    }

    /**
     * DATA CALLBACK
     */
    override fun callback(apiName: String, data: Any) {
        Log.d("metis", "callback data : $data")

        historyResult = data as List<RegisterModel>


        recyclerAdapter = ItemRegisterHistroyRecyclerViewAdapter(this@HistoryActivity, historyResult)
        binding.registerHistoryRecycler.layoutManager = LinearLayoutManager(this)
        binding.registerHistoryRecycler.adapter = recyclerAdapter


        recyclerAdapter.notifyDataSetChanged()
    }

    /**
     * START / END CLICK DATE PICKER EVENT
     */
    private fun clickDatePicker(view: View){

        val nowCal                    = Calendar.getInstance(Locale.getDefault())
        var dialog: DatePickerDialog? = null

        dialog                        = DatePickerDialog(this@HistoryActivity, datePickerListener, nowCal[Calendar.YEAR],nowCal[Calendar.MONTH], nowCal[Calendar.DATE])
        selectDateGbn                 = view.resources.getResourceEntryName(view.id)               // 시작 또는 종료 날짜를 구분하기 위한 ID SET.

        dialog.setCancelable(false)
        dialog.show()
    }

    /**
     * START / END CALENDAR DATE PICKER LISTENER
     */
    @SuppressLint("SetTextI18n")
    private val datePickerListener = OnDateSetListener { view, year, month, day ->

        selectYYYY = year
        selectMM   = month + 1
        selectDD   = day

        if(selectDateGbn == "search_start_date_text")                                              // 시작 일자
            binding.searchStartDateText.text = String.format("%04d",selectYYYY)+"-"+ String.format("%02d",selectMM)+"-"+String.format("%02d",selectDD)

        else
            binding.searchEndDateText.text = String.format("%04d",selectYYYY)+"-"+ String.format("%02d",selectMM)+"-"+String.format("%02d",selectDD)

        selectedStartDate = binding.searchStartDateText.text.toString()
        selectedEndDate = binding.searchEndDateText.text.toString()

        Log.d("metis", "selectedStartDate : $selectedStartDate")
        Log.d("metis", "selectedEndDate : $selectedEndDate")
    }

    /**
     * DROPBOX SELECTED EVENT.
     */
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        binding.searchSmellValue.text = smellValueArray[position]
        selectedSmellValeCode = instance.intensityList[position].codeId

        Log.d("metis", "selectedSmellTypeCode : $selectedSmellValeCode")
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

}
