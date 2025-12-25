package com.ynsuper.slideshowver1.view.menu

import android.content.Context
import android.os.Handler
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.recyclerview.widget.LinearLayoutManager
import com.ynsuper.slideshowver1.R
import com.ynsuper.slideshowver1.adapter.ColorTextAdapter
import com.ynsuper.slideshowver1.base.BaseCustomConstraintLayout
import com.ynsuper.slideshowver1.callback.SceneOptionStateListener
import com.ynsuper.slideshowver1.callback.TopBarController
import com.ynsuper.slideshowver1.util.Constants
import com.ynsuper.slideshowver1.view.SlideShowActivity

class BackgroundOptionsViewLayout : BaseCustomConstraintLayout {
    private lateinit var colorTextAdapter: ColorTextAdapter
    private lateinit var topBarController: TopBarController
    private var state: OptionState? = null
    private var listener: SceneOptionStateListener? = null

    private lateinit var checkboxBlurView: CheckBox
    private lateinit var seekBarBlurView: SeekBar
    private lateinit var groupFillView: RadioGroup
    private lateinit var fitCenterView: RadioButton
    private lateinit var fitEndView: RadioButton
    private lateinit var fitStartView: RadioButton
    private lateinit var fillCenterView: RadioButton
    private lateinit var fillEndView: RadioButton
    private lateinit var fillStartView: RadioButton
    private lateinit var recyclerBackgroundColorView: androidx.recyclerview.widget.RecyclerView
    private lateinit var textNameTopBarView: TextView

    constructor(context: Context?) : super(context) {
        init(context)
        setLayoutInflate(R.layout.fragment_background_option)

    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
        init(context)
        setLayoutInflate(R.layout.fragment_background_option)

    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init(context)
        setLayoutInflate(R.layout.fragment_background_option)

    }


    private fun initView() {
        listener = context as SlideShowActivity
        textNameTopBarView = findViewById(R.id.text_name_top_bar)
        checkboxBlurView = findViewById(R.id.checkboxBlur)
        seekBarBlurView = findViewById(R.id.seekBarBlur)
        groupFillView = findViewById(R.id.groupfill)
        fitCenterView = findViewById(R.id.fitCenter)
        fitEndView = findViewById(R.id.fitEnd)
        fitStartView = findViewById(R.id.fitStart)
        fillCenterView = findViewById(R.id.fillCenter)
        fillEndView = findViewById(R.id.fillEnd)
        fillStartView = findViewById(R.id.fillStart)
        recyclerBackgroundColorView = findViewById(R.id.recycleBackgroundColor)

        setTopBarName(context.getString(R.string.text_background))
        setColorTextAdapter()

        checkboxBlurView.isChecked = state?.blur == true
        seekBarBlurView.progress = state?.progressBlur!!
        for (i in 0 until Constants.getColorText().size - 1) {
            if (state?.color == context.resources.getColor(Constants.getColorText()[i].idColor)) {
                colorTextAdapter.selectedPosition = i
                colorTextAdapter.notifyDataSetChanged()
                break
            }
        }



        checkboxBlurView.setOnCheckedChangeListener { _, _ ->
            saveState()
        }
        seekBarBlurView.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                state?.progressBlur = seekBar!!.progress
                Handler().post(Runnable {
                    saveState()
                })
            }

        })
        groupFillView.setOnCheckedChangeListener { _, _ ->
            saveState()
        }

    }

    private fun setColorTextAdapter() {
        colorTextAdapter = ColorTextAdapter(
            Constants.getColorText(),
            context
        ) { _, position ->
            state?.color = resources.getColor(Constants.getColorText().get(position).getIdColor())
            saveState()
        }
        recyclerBackgroundColorView.adapter = colorTextAdapter
        recyclerBackgroundColorView.setHasFixedSize(true)
        val linearLayoutManagerColor = LinearLayoutManager(context)
        linearLayoutManagerColor.orientation = LinearLayoutManager.HORIZONTAL
        recyclerBackgroundColorView.layoutManager = linearLayoutManagerColor
    }


    private fun saveState() {
        this.state?.let {
            listener?.onBackGroundConfigChange(
                it.copy(
                    blur = checkboxBlurView.isChecked,
                    crop = currentCrop()
                )
            )
        }
    }

    private fun currentCrop(): String {
        if (fitCenterView.isChecked) return "fit-center"
        if (fitEndView.isChecked) return "fit-end"
        if (fitStartView.isChecked) return "fit-start"
        if (fillCenterView.isChecked) return "fill-center"
        if (fillEndView.isChecked) return "fill-end"
        if (fillStartView.isChecked) return "fill-start"
        return "fit-center"
    }

    private fun setState() {
        state?.let { s ->
            groupFillView.check(getCheckedId(s.crop!!))

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

    fun setTopbarController(topbarController: TopBarController) {
        this.topBarController = topbarController
    }

    fun setState(state: OptionState) {
        this.state = state
        setState()
        initView()
    }

    fun setTopBarName(name: String) {
        textNameTopBarView.text = name
    }


    data class OptionState(
        var id: String,
        var progressBlur: Int,
        var crop: String? = "fit-center",
        var blur: Boolean = true,
        var delete: Boolean = false,
        var color: Int = 0
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readInt(),
            parcel.readString(),
            parcel.readByte() != 0.toByte(),
            parcel.readByte() != 0.toByte(),
            parcel.readInt()
        ) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(id)
            parcel.writeInt(progressBlur)
            parcel.writeString(crop)
            parcel.writeByte(if (blur) 1 else 0)
            parcel.writeByte(if (delete) 1 else 0)
            parcel.writeInt(color)
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