package kr.co.metisinfo.iotbadsmellmonitoringand

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import androidx.databinding.DataBindingUtil
import kr.co.metisinfo.iotbadsmellmonitoringand.databinding.ActivityHistoryBinding
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

    var distanceEntry: List<CharSequence> = ArrayList()

    /**
     * ACTIVITY INIT
     */
    override fun initLayout() {

        binding = DataBindingUtil.setContentView(this, R.layout.activity_history)           // XML BIND

        Log.d("metis","HistoryActivity 시작")
        binding.includeHeader.textTitle.setText(R.string.history)                                   // 타이틀 제목
        binding.includeHeader.backButton.visibility = View.GONE                                     // 뒤로가기 버튼 안보이게
        binding.includeHeader.sideMenuButton.visibility = View.VISIBLE                              // 사이드 메뉴 버튼 보이게
    }

    /**
     * ONCLICK LISTENER SET
     */
    override fun setOnClickListener() {

        binding.searchSatartDateText.setOnClickListener(this::clickDatePicker)
        binding.searchEndDateText.setOnClickListener(this::clickDatePicker)
        binding.searchSmellValue.setOnClickListener { binding.spinnerSearchSmellValue.performClick() }
        binding.searchSmellValueDropdown.setOnClickListener { binding.spinnerSearchSmellValue.performClick() }
    }

    /**
     * ACTIVITY INIT DATA
     */
    override fun initData() {

        distanceEntry = resources.getStringArray(R.array.array_distance_radius).toList()
        val lengthAdapter: Any = ArrayAdapter<Any?>(this, android.R.layout.simple_spinner_dropdown_item, distanceEntry)

        Log.d("metis", "lengthAdapter :"+resources.getStringArray(R.array.array_distance_radius))

        binding.spinnerSearchSmellValue.adapter = lengthAdapter as SpinnerAdapter?
        binding.spinnerSearchSmellValue.onItemSelectedListener = this
    }

    /**
     * START / END CLICK DATE PICKER EVENT
     */
    private fun clickDatePicker(view: View){

        val nowCal                    = Calendar.getInstance(Locale.getDefault())
        var dialog: DatePickerDialog? = null

        dialog                        = DatePickerDialog(this@HistoryActivity, datePickerListener, nowCal[Calendar.YEAR],nowCal[Calendar.MONTH], nowCal[Calendar.DATE])
        selectDateGbn                 = view.resources.getResourceEntryName(view.id);               // 시작 또는 종료 날짜를 구분하기 위한 ID SET.

        dialog.setCancelable(false);
        dialog.show();
    }

    /**
     * START / END CALENDAR DATE PICKER LISTENER
     */
    @SuppressLint("SetTextI18n")
    private val datePickerListener = OnDateSetListener { view, year, month, day ->

        selectYYYY = year
        selectMM   = month + 1
        selectDD   = day

        if(selectDateGbn == "search_satart_date_text")                                              // 시작 일자
            binding.searchSatartDateText.text = String.format("%04d",selectYYYY)+"-"+ String.format("%02d",selectMM)+"-"+String.format("%02d",selectDD)

        else
            binding.searchSatartDateText.text = String.format("%04d",selectYYYY)+"-"+ String.format("%02d",selectMM)+"-"+String.format("%02d",selectDD)
    }


    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        Log.d("metis", "position :$position")

        binding.searchSmellValue.text = distanceEntry[position].toString()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

}
