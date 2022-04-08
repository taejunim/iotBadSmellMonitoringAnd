package kr.co.metisinfo.iotbadsmellmonitoringand

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.co.metisinfo.iotbadsmellmonitoringand.adapter.ItemRegisterHistroyRecyclerViewAdapter
import kr.co.metisinfo.iotbadsmellmonitoringand.databinding.ActivityHistoryBinding
import kr.co.metisinfo.iotbadsmellmonitoringand.model.HistoryModel
import kr.co.metisinfo.iotbadsmellmonitoringand.model.HistoryResult
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
    private var isLastData = false

    private var historyResult: MutableList<HistoryModel> = ArrayList()

    /**
     * ACTIVITY INIT DATA
     */
    override fun initData() {
        selectedStartDate = "2021-01-01"
        selectedEndDate = today
        selectedSmellValeCode = ""

    }

    /**
     * ACTIVITY INIT
     */
    override fun initLayout() {
        Log.d("metis","HistoryActivity 시작")
        binding = DataBindingUtil.setContentView(this, R.layout.activity_history)           // XML BIND
        binding.includeHeader.textTitle.setText(R.string.history)                                      // 타이틀 제목
        binding.includeHeader.backButton.visibility = View.VISIBLE                                 // 뒤로가기 버튼 안보이게
        binding.includeHeader.navigationViewButton.visibility = View.GONE                          // 사이드 메뉴 버튼 보이게
        binding.includeHeader.backButton.setOnClickListener { finish() }
        binding.searchStartDateText.text = "2021-01-01"
        binding.searchEndDateText.text = today
        setAdpater()
        initRecycler()
        getRegisterMasterHistory()
        binding.registerHistoryRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val linearLayoutManager: LinearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = linearLayoutManager.itemCount
                val lastItem = linearLayoutManager.findLastCompletelyVisibleItemPosition()

                if(lastItem >= totalItemCount - 1 ) checkIsLastData()
            }
        })
    }
    /**
     * ONCLICK LISTENER SET
     */
    override fun setOnClickListener() {
        binding.searchStartDateText.setOnClickListener(this::clickDatePicker)
        binding.searchEndDateText.setOnClickListener(this::clickDatePicker)
        binding.searchSmellValue.setOnClickListener { binding.spinnerSearchSmellValue.performClick() }
        binding.searchSmellValueDropdown.setOnClickListener { binding.spinnerSearchSmellValue.performClick() }
        binding.searchBtn.setOnClickListener {
            //조회시에 데이터 초기화.
            historyResult.clear()
            pageNum = 0
            isLastData = false
            //recycler도 초기화를 해야함.
            initRecycler()
            getRegisterMasterHistory()
        }
    }

    /**
     * 마지막 데이터인지 체크 / 결과가 없는건지, 더이상 없는건지 체크
     */
    fun checkIsLastData() {
        if(isLastData) {
            //데이터가 존재하지 않을때
            if(pageNum == 0 && historyResult.size == 0) Toast.makeText(this, resource.getString(R.string.history_no_data), Toast.LENGTH_SHORT).show()
            //데이터가 더 이상 존재할 때
            else Toast.makeText(this, resource.getString(R.string.history_no_more_data), Toast.LENGTH_SHORT).show()
        }
        else {
            pageNum += 10
            getRegisterMasterHistory()
        }
    }
    /**
     * 악취 등록 이력 가져오기
     */
    fun getRegisterMasterHistory() {

        val userId = MainApplication.prefs.getString("userId", "")

        showLoading(binding.loading)

        instance.apiService.getRegisterMasterHistory(pageNum, pageCount, selectedSmellValeCode, selectedStartDate, selectedEndDate, userId).enqueue(object : Callback<HistoryResult> {
            override fun onResponse(call: Call<HistoryResult>, response: Response<HistoryResult>) {

                hideLoading(binding.loading)

                if(response.body()!!.data != null) {

                    historyResult.addAll(response.body()!!.data)
                    Log.d("metis", userId + " getRegisterMasterHistory 결과 -> " + historyResult.size.toString())
                    if(response.body()!!.data.size != pageCount) isLastData = true
                }
                else {
                    historyResult = ArrayList()
                    isLastData = true
                    checkIsLastData()
                }
                callback("registerStatus", historyResult)
            }

            override fun onFailure(call: Call<HistoryResult>, t: Throwable) {
                hideLoading(binding.loading)
                Log.d("metis",t.message.toString())
                Log.d("metis", "onFailure : fail")
            }
        })
    }

    /**
     * 악취 강도 Spinner
     */
    fun setAdpater() {

        val smellValue : ArrayList<String> = ArrayList<String>()

        //picker에 전체 추가
        smellValue.add("전체")
        for (smellTypeCode in instance.intensityList) {
            smellValue.add(smellTypeCode.codeIdName)
        }
        smellValueArray = smellValue
        binding.spinnerSearchSmellValue.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, smellValueArray)

        binding.spinnerSearchSmellValue.onItemSelectedListener = this
    }

    fun initRecycler() {
        recyclerAdapter = ItemRegisterHistroyRecyclerViewAdapter(this@HistoryActivity, historyResult)
        binding.registerHistoryRecycler.layoutManager = LinearLayoutManager(this)
        binding.registerHistoryRecycler.adapter = recyclerAdapter
    }

    /**
     * DATA CALLBACK
     */
    override fun callback(apiName: String, data: Any) {
        Log.d("metis", "callback data : $data")

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
        if(position == 0)
            selectedSmellValeCode = ""
        else selectedSmellValeCode = instance.intensityList[position-1].codeId

        Log.d("metis", "selectedSmellTypeCode : $selectedSmellValeCode")
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

}
