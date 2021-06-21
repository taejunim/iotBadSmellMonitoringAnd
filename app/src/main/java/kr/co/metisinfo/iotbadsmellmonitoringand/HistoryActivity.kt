package kr.co.metisinfo.iotbadsmellmonitoringand

import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import kr.co.metisinfo.iotbadsmellmonitoringand.databinding.ActivityHistoryBinding

/**
 * @ Class Name   : HistoryActivity.java
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

class HistoryActivity : BaseActivity() {

    private lateinit var binding: ActivityHistoryBinding

    /*ACTIVITY INIT*/
    override fun initLayout() {

        binding = DataBindingUtil.setContentView(this, R.layout.activity_history)           //XML BIND

        Log.d("metis","MainActivity 시작")
        binding.includeHeader.textTitle.setText(R.string.history)                                   // 타이틀 제목
        binding.includeHeader.backButton.visibility = View.GONE                                     // 뒤로가기 버튼 안보이게
        binding.includeHeader.sideMenuButton.visibility = View.VISIBLE                              // 사이드 메뉴 버튼 보이게
    }

    override fun setOnClickListener() {

    }

}