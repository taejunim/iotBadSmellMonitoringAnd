package kr.co.metisinfo.iotbadsmellmonitoringand.adapter

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kr.co.metisinfo.iotbadsmellmonitoringand.MainApplication
import kr.co.metisinfo.iotbadsmellmonitoringand.R
import kr.co.metisinfo.iotbadsmellmonitoringand.model.HistoryModel
import kr.co.metisinfo.iotbadsmellmonitoringand.model.ImageModel
import kr.co.metisinfo.iotbadsmellmonitoringand.model.ImageResult

import kr.co.metisinfo.iotbadsmellmonitoringand.util.ToggleAnimation
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

/**
 * @ Class Name   : ItemRegisterHistroyRecyclerViewAdapter.kt
 * @ Modification : REGISTER HISTORY RECYCLER VIEW ADAPTER CLASS.
 * @
 * @ 최초 생성일      최초 생성자
 * @ ---------    ---------
 * @ 2021.06.23.    고재훈
 * @
 * @  수정일          수정자
 * @ ---------    ---------
 * @
 **/

class ItemRegisterHistroyRecyclerViewAdapter(private val context: Context, private val historyList: List<HistoryModel>) :
    RecyclerView.Adapter<ItemRegisterHistroyRecyclerViewAdapter.CowViewHolder>() {

    class CowViewHolder(
        itemView: View,
        context: Context
    ) : RecyclerView.ViewHolder(itemView) {
        var mContext: Context? = context
        private val instance = MainApplication.instance
        val resource: Resources = MainApplication.getContext().resources
        var smellRegisterNo = ""

        fun bind(historyModel: HistoryModel) {

            val txtName               = itemView.findViewById<TextView>(R.id.reg_dt_txt)
            val imgMore               = itemView.findViewById<RelativeLayout>(R.id.layout_dropdown)
            val historyListSmellValue = itemView.findViewById<Button>(R.id.history_list_smell_value)
            val layoutExpand          = itemView.findViewById<RelativeLayout>(R.id.layout_expand)
            val txtSmellComment       = itemView.findViewById<TextView>(R.id.txt_smell_comment)
            val smellTypeImage        = itemView.findViewById<ImageView>(R.id.smell_type_image)
            val smellTypeText         = itemView.findViewById<TextView>(R.id.smell_type_text)
            val countImage            = itemView.findViewById<ImageView>(R.id.countImage)

            smellRegisterNo           = historyModel.smellRegisterNo
            txtName.text              = historyModel.regDt
            txtSmellComment.text      = historyModel.smellComment

            val smellValue = historyModel.smellValue.replace("00","").toInt()
            val smellType = historyModel.smellType.replace("00","").toInt()

            when (smellValue) {
                1 -> historyListSmellValue?.setBackgroundResource(R.drawable.intensity_0_button)
                2 -> historyListSmellValue?.setBackgroundResource(R.drawable.intensity_1_button)
                3 -> historyListSmellValue?.setBackgroundResource(R.drawable.intensity_2_button)
                4 -> historyListSmellValue?.setBackgroundResource(R.drawable.intensity_3_button)
                5 -> historyListSmellValue?.setBackgroundResource(R.drawable.intensity_4_button)
                6 -> historyListSmellValue?.setBackgroundResource(R.drawable.intensity_5_button)
            }

            historyListSmellValue.text = instance.intensityList[smellValue - 1].codeIdName + " / " + instance.intensityList[smellValue-1].codeComment

            smellTypeImage?.setBackgroundResource(resource.getIdentifier("smell_${historyModel.smellType}","drawable","kr.co.metisinfo.iotbadsmellmonitoringand"))
            countImage?.setBackgroundResource(resource.getIdentifier("smell_${historyModel.smellType}","drawable","kr.co.metisinfo.iotbadsmellmonitoringand"))
            smellTypeText?.text = instance.smellTypeList[smellType-1].codeIdName

            imgMore.setOnClickListener {
                val show = toggleLayout(!historyModel.isExpanded, it, layoutExpand)
                historyModel.isExpanded = show
            }
        }

        private fun toggleLayout(isExpanded: Boolean, view: View, layoutExpand: RelativeLayout): Boolean {
            // 2
            ToggleAnimation.toggleArrow(view, isExpanded)
            if (isExpanded) {
                getRegisterDetailHistory()
                ToggleAnimation.expand(layoutExpand)
            } else {
                ToggleAnimation.collapse(layoutExpand)
            }
            return isExpanded
        }

        /**
         * 악취 이력 상세 이미지 가져오기
         */
        fun getRegisterDetailHistory() {

            val countText         = itemView.findViewById<TextView>(R.id.countText)
            Log.d("metis","smellRegisterNo  : " +  smellRegisterNo)

            instance.apiService.getRegisterDetailHistory(smellRegisterNo).enqueue(object : Callback<ImageResult> {
                override fun onResponse(call: Call<ImageResult>, response: Response<ImageResult>) {
                    var layout = itemView.findViewById<LinearLayout>(R.id.imageListView)
                    var imageResult: List<ImageModel>
                    layout.removeAllViews()
                    if(response.body()!!.data != null) imageResult = response.body()!!.data
                    else {
                        imageResult = ArrayList()
                        layout.isVisible = false
                    }

                    if(imageResult != null) {
                        val param = ViewGroup.MarginLayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                        param.setMargins(10,0,10,10)
                        param.width = 150
                        param.height = 150

                        for (imageModel in imageResult) {
                            var imageView = ImageView(mContext)

                            imageView.layoutParams = param

                            Glide.with(mContext!!).load(imageModel.smellImagePath).override(200,100).fitCenter().into(imageView)

                            layout.addView(imageView)
                            Log.d( "metis", "smellImageNo : " + imageModel.smellImageNo)
                            Log.d( "metis", "smellImagePath : " + imageModel.smellImagePath)
                            Log.d( "metis", "smellRegisterNo : " + imageModel.smellRegisterNo)
                            Log.d( "metis", "regDt : " + imageModel.regDt)

                        }
                    }
                    countText.text = imageResult.size.toString() + " / 5"

                }

                override fun onFailure(call: Call<ImageResult>, t: Throwable) {
                    Log.d("metis",t.message.toString())
                    Log.d("metis", "onFailure : fail")
                }
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CowViewHolder {

        return CowViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_histroy_item_list, parent, false), context)
    }

    override fun onBindViewHolder(holder: CowViewHolder, position: Int) {
        holder.bind(historyList[position])
    }

    override fun getItemCount(): Int {
        return historyList.size
    }

    override fun getItemViewType(position: Int): Int {
        //스크롤을 해도 그 형태를 유지시켜주기 위해 override
        return position
    }
}