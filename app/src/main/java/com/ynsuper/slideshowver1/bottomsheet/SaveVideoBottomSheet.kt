package com.ynsuper.slideshowver1.bottomsheet

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ynsuper.slideshowver1.R
import com.ynsuper.slideshowver1.callback.SaveStateListener
import android.widget.TextView
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.RadioButton

class SaveVideoBottomSheet : BottomSheetDialogFragment() {

    private var listener: SaveStateListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SaveStateListener)
            listener = context
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_save_option, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)

    }

    private fun initView(view: View) {
        val textNameTopBar: TextView = view.findViewById(R.id.text_name_top_bar)
        val imageSubmitMenu: ImageView = view.findViewById(R.id.image_submit_menu)
        val textExport: TextView = view.findViewById(R.id.text_export)
        val radioGroup: RadioGroup = view.findViewById(R.id.radio_group)
        val radio1: RadioButton = view.findViewById(R.id.radio1)
        val radio2: RadioButton = view.findViewById(R.id.radio2)
        
        textNameTopBar.text = "Resolution"
        imageSubmitMenu.visibility = View.GONE

//        image_close_menu.setOnClickListener {
//            dismiss()
//        }
        textExport.setOnClickListener {
            if (radioGroup.checkedRadioButtonId == -1) {
                Toast.makeText(context, "Please select a resolution!!!", Toast.LENGTH_SHORT).show()
            } else {
                var width = 0
                var height = 0

                if (radio1.isChecked) {
                    width = 720
                    height = 480
                } else if (radio2.isChecked) {
                    width = 1080
                    height = 720
                } else {
                    width = 1920
                    height = 1080
                }
                listener?.onExportVideo(width, height)
                dismiss()

            }
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(): SaveVideoBottomSheet {
            return SaveVideoBottomSheet()
        }
    }

}