package kr.co.metisinfo.iotbadsmellmonitoringand.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.*
import androidx.fragment.app.DialogFragment
import kr.co.metisinfo.iotbadsmellmonitoringand.MainApplication
import kr.co.metisinfo.iotbadsmellmonitoringand.R
import kr.co.metisinfo.iotbadsmellmonitoringand.databinding.DialogSmellTypeBinding
import kr.co.metisinfo.iotbadsmellmonitoringand.util.Utils.Companion.convertToDp
import kotlin.math.ceil

class SmellTypeDialog : DialogFragment() {

    private lateinit var binding: DialogSmellTypeBinding

    private val resource: Resources = MainApplication.getContext().resources
    private val instance = MainApplication.instance

    private val smellTypeList = instance.smellTypeList

    internal lateinit var listener: SmellTypeDialogListener

    private lateinit var currentLayout: RelativeLayout
    private lateinit var currentRadioButton: RadioButton
    private lateinit var selectedCodeId: String
    private var selectedIndex: Int = 0

    interface SmellTypeDialogListener {
        fun onInputData(id: String, index: Int)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = context as SmellTypeDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(("$context must implement NoticeDialogListener"))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = DialogSmellTypeBinding.inflate(LayoutInflater.from(context))

        val smellTypeCount = smellTypeList.count()
        val smellTypeLineCount = ceil(smellTypeCount.toDouble() / 3).toInt()

        val totalLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        val totalLayout = LinearLayout(context)
        totalLayout.layoutParams = totalLayoutParams
        totalLayout.orientation = LinearLayout.VERTICAL

        val lineLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, convertToDp(130F))
        lineLayoutParams.setMargins(0,0,0,convertToDp(30F))

        val itemLayoutParams = LinearLayout.LayoutParams(convertToDp(0F), RelativeLayout.LayoutParams.MATCH_PARENT)
        itemLayoutParams.weight = 1F

        val totalAreaLayoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)

        val areaLayoutParams = RelativeLayout.LayoutParams(convertToDp(90F), convertToDp(90F))
        areaLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)

        val imageLayoutParams = RelativeLayout.LayoutParams(convertToDp(50F), convertToDp(50F))
        imageLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
        imageLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        imageLayoutParams.setMargins(0,convertToDp(10F),0,0)

        val textLayoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        textLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        textLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
        textLayoutParams.setMargins(0,0,0,convertToDp(10F))

        val radioButtonParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        radioButtonParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        radioButtonParams.addRule(RelativeLayout.CENTER_HORIZONTAL)

        currentLayout = RelativeLayout(context)

        for (k in 0 until smellTypeLineCount) {

            val lineLayout = LinearLayout(context)
            lineLayout.layoutParams = lineLayoutParams
            lineLayout.orientation = LinearLayout.HORIZONTAL
            lineLayout.weightSum = 3F

            for (i in 0 until 3) {

                var codeIndex = k * 3 + i
                var imageIndex = codeIndex + 1

                val itemLayout = LinearLayout(context)
                itemLayout.layoutParams = itemLayoutParams

                val totalAreaLayout = RelativeLayout(context)
                totalAreaLayout.layoutParams = totalAreaLayoutParams

                val areaLayout = RelativeLayout(context)
                areaLayout.layoutParams = areaLayoutParams
                areaLayout.id = codeIndex + 101

                val smellRadioButton = RadioButton(context)
                smellRadioButton.layoutParams = radioButtonParams
                smellRadioButton.id = codeIndex + 1

                when (codeIndex) {
                    0 -> {
                        areaLayout.setBackgroundResource(R.drawable.smell_type_button)
                        currentLayout = areaLayout
                        currentRadioButton = smellRadioButton

                        selectedCodeId = smellTypeList[codeIndex].codeId
                        selectedIndex = codeIndex
                    }
                    else -> areaLayout.setBackgroundResource(R.drawable.smell_type_unselected_button)
                }

                areaLayout.setOnClickListener {
                    if (areaLayout.id != currentLayout.id && smellRadioButton.id != currentRadioButton.id){
                        areaLayout.setBackgroundResource(R.drawable.smell_type_button)
                        currentLayout.setBackgroundResource(R.drawable.smell_type_unselected_button)
                        currentLayout = areaLayout
                        currentRadioButton = smellRadioButton

                        binding.smellTypeRadioGroup.check(smellRadioButton.id)

                        selectedCodeId = smellTypeList[codeIndex].codeId
                        selectedIndex = codeIndex
                    }
                }

                smellRadioButton.setOnClickListener {
                    if (areaLayout.id != currentLayout.id && smellRadioButton.id != currentRadioButton.id){
                        areaLayout.setBackgroundResource(R.drawable.smell_type_button)
                        currentLayout.setBackgroundResource(R.drawable.smell_type_unselected_button)
                        currentLayout = areaLayout
                        currentRadioButton = smellRadioButton

                        binding.smellTypeRadioGroup.check(smellRadioButton.id)

                        selectedCodeId = smellTypeList[codeIndex].codeId
                        selectedIndex = codeIndex
                    }
                }

                val smellTypeImage = ImageView(context)
                smellTypeImage.layoutParams = imageLayoutParams
                smellTypeImage.setImageResource(resource.getIdentifier("smell_00$imageIndex", "drawable", "kr.co.metisinfo.iotbadsmellmonitoringand"))

                val smellTypeText = TextView(context)
                smellTypeText.layoutParams = textLayoutParams
                smellTypeText.gravity = Gravity.CENTER
                smellTypeText.setTextColor(Color.BLACK)
                smellTypeText.setTextSize(TypedValue.COMPLEX_UNIT_SP,14F)
                smellTypeText.text = smellTypeList[codeIndex].codeIdName

                areaLayout.addView(smellTypeImage)
                areaLayout.addView(smellTypeText)

                totalAreaLayout.addView(areaLayout)
                totalAreaLayout.addView(smellRadioButton)
                itemLayout.addView(totalAreaLayout)
                lineLayout.addView(itemLayout)
            }

            totalLayout.addView(lineLayout)
        }

        binding.smellTypeRadioGroup.addView(totalLayout)
        binding.smellTypeRadioGroup.check(currentRadioButton.id)

        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(binding.root)

        binding.cancellationButton.setOnClickListener {
            dismiss()
        }

        binding.completionButton.setOnClickListener {
            dismiss()
            listener.onInputData(selectedCodeId, selectedIndex)
        }

        return builder.create()
    }
}