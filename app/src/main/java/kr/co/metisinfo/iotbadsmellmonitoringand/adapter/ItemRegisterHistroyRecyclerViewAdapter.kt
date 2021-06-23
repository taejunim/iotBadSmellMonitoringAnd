package kr.co.metisinfo.iotbadsmellmonitoringand.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import kr.co.metisinfo.iotbadsmellmonitoringand.R
import kr.co.metisinfo.iotbadsmellmonitoringand.model.HistoryModel
import kr.co.metisinfo.iotbadsmellmonitoringand.util.ToggleAnimation

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

class ItemRegisterHistroyRecyclerViewAdapter(private val historyList: List<HistoryModel>) :
    RecyclerView.Adapter<ItemRegisterHistroyRecyclerViewAdapter.CowViewHolder>() {

    class CowViewHolder(

        itemView: View

    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(historyModel: HistoryModel) {

            val txtName               = itemView.findViewById<TextView>(R.id.reg_dt_txt)
            val imgMore               = itemView.findViewById<ImageView>(R.id.search_smell_value_dropdown)
            val historyListSmellValue = itemView.findViewById<Button>(R.id.history_list_smell_value)
            val layoutExpand          = itemView.findViewById<LinearLayout>(R.id.layout_expand)

            txtName.text               = historyModel.regId
            historyListSmellValue.text = historyModel.smellValue

            imgMore.setOnClickListener {
                // 1
                val show = toggleLayout(!historyModel.isExpanded, it, layoutExpand)
                historyModel.isExpanded = show
            }
        }

        private fun toggleLayout(isExpanded: Boolean, view: View, layoutExpand: LinearLayout): Boolean {
            // 2
            ToggleAnimation.toggleArrow(view, isExpanded)
            if (isExpanded) {
                ToggleAnimation.expand(layoutExpand)
            } else {
                ToggleAnimation.collapse(layoutExpand)
            }
            return isExpanded
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CowViewHolder {
        return CowViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_histroy_item_list, parent, false))
    }

    override fun onBindViewHolder(holder: CowViewHolder, position: Int) {
        holder.bind(historyList[position])
    }

    override fun getItemCount(): Int {
        return historyList.size
    }

}