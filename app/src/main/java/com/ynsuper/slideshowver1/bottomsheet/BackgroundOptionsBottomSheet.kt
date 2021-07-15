package com.ynsuper.slideshowver1.bottomsheet

import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.IdRes
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ynsuper.slideshowver1.R
import com.ynsuper.slideshowver1.callback.SceneOptionStateListener
import kotlinx.android.synthetic.main.fragment_background_option.*

class BackgroundOptionsBottomSheet : BottomSheetDialogFragment() {
    private var state: OptionState? = null
    private var listener: SceneOptionStateListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SceneOptionStateListener)
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
        val view = inflater.inflate(R.layout.fragment_background_option, container, false)
        state =
            arguments?.getParcelable("state") ?: throw RuntimeException("There is no initial state")
       val imageSubmitMenu = view.findViewById<ImageView>(R.id.image_submit_menu)
       val imageCloseMenu = view.findViewById<ImageView>(R.id.image_close_menu)
        imageCloseMenu.setOnClickListener {
            dismiss()
        }
       imageSubmitMenu.setOnClickListener {
           saveState()
       }

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setState()


    }


    private fun saveState() {
        this.state?.let {
            listener?.onBackGroundConfigChange(
                it.copy(
                    blur = checkboxBlur.isChecked,
                    crop = currentCrop()
                )
            )
        }
        dismiss()
    }

    private fun currentCrop(): String {
        if (fitCenter.isChecked) return "fit-center"
        if (fitEnd.isChecked) return "fit-end"
        if (fitStart.isChecked) return "fit-start"
        if (fillCenter.isChecked) return "fill-center"
        if (fillEnd.isChecked) return "fill-end"
        if (fillStart.isChecked) return "fill-start"
        return "fit-center"
    }

    private fun setState() {
        state?.let { s ->
            groupfill.check(getCheckedId(s.crop!!))

        }
    }

    @IdRes
    private fun getCheckedId(key: String): Int {
        return when (key) {
            "fit-center" -> R.id.fitCenter
            "fit-end" -> R.id.fitEnd
            "fit-start" -> R.id.fitStart
            "fill-center" -> R.id.fillCenter
            "fill-end" -> R.id.fillEnd
            "fill-start" -> R.id.fillStart
            else -> R.id.fitCenter
        }
    }




    companion object {

        private const val MAX_DURATION = 20 * 1000L // 10 seconds
        private const val MIN_DURATION = 2 * 1000L // 2 seconds



        @JvmStatic
        fun newInstance(state: OptionState): BackgroundOptionsBottomSheet {
            return BackgroundOptionsBottomSheet().apply {
                arguments = bundleOf("state" to state)
            }
        }
    }



    data class OptionState(
        var id: String,
        var duration: Long,
        var crop: String? = "fit-center",
        var blur: Boolean = true,
        var delete: Boolean = false
    ) : Parcelable {

        constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readLong(),
            parcel.readString(),
            parcel.readByte() != 0.toByte(),
            parcel.readByte() != 0.toByte()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(id)
            parcel.writeLong(duration)
            parcel.writeString(crop)
            parcel.writeByte(if (blur) 1 else 0)
            parcel.writeByte(if (delete) 1 else 0)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<OptionState> {
            override fun createFromParcel(parcel: Parcel): OptionState {
                return OptionState(parcel)
            }

            override fun newArray(size: Int): Array<OptionState?> {
                return arrayOfNulls(size)
            }
        }
    }
}