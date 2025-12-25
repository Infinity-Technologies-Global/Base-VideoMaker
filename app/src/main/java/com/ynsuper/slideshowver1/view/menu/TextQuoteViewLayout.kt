package com.ynsuper.slideshowver1.view.menu

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Px
import androidx.core.widget.addTextChangedListener
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.ynsuper.slideshowver1.R
import com.ynsuper.slideshowver1.base.BaseCustomConstraintLayout
import com.ynsuper.slideshowver1.callback.TopBarController
import com.ynsuper.slideshowver1.view.FontLoader
import com.ynsuper.slideshowver1.view.SlideShowActivity
import com.ynsuper.slideshowver1.view.adapter.FontFamilyAdapter
import com.ynsuper.slideshowver1.view.sticker.QuoteState
import com.ynsuper.slideshowver1.view.sticker.StickerView
import com.ynsuper.slideshowver1.viewmodel.SlideShowViewModel
import kotlin.math.roundToInt

class TextQuoteViewLayout : BaseCustomConstraintLayout {
    private var isApplyForAll: Boolean = false
    private lateinit var preview: StickerView
    private lateinit var topBarController: TopBarController
    private lateinit var slideShowViewModel: SlideShowViewModel
    private var listener: QuoteListener? = null
    private var state: QuoteState? = null

    private lateinit var textNameTopBar: TextView
    private lateinit var checkboxTextAll: CheckBox
    private lateinit var editTextQuote: TextView
    private lateinit var colorPickerView: View
    private lateinit var imageSubmitMenu: ImageView


    constructor(context: Context?) : super(context) {
        init(context)
        setLayoutInflate(R.layout.layout_text_quote)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
        setLayoutInflate(R.layout.layout_text_quote)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
        setLayoutInflate(R.layout.layout_text_quote)
    }


    fun setTopBarName(name: String) {
        textNameTopBar.text = name
    }

    private var bgColor = Color.WHITE

    private var currentFont: FontLoader.FontFamily? = null


    fun setState(state: QuoteState, preview: StickerView) {
        this.state = state
        this.preview = preview
        initView()
    }

    private fun initView() {
        listener = context as SlideShowActivity

        textNameTopBar = findViewById(R.id.text_name_top_bar)
        checkboxTextAll = findViewById(R.id.checkbox_text_all)
        editTextQuote = findViewById(R.id.editText)
        colorPickerView = findViewById(R.id.colorPicker)
        imageSubmitMenu = findViewById(R.id.image_submit_menu)

        setTopBarName(context.getString(R.string.text_quote))

        val fontLoader = FontLoader(context.assets)
        val fonts = fontLoader.getFonts()

        val adapter = FontFamilyAdapter(context, null, fonts)

        val editTextFilledExposedDropdown = findViewById<AutoCompleteTextView>(R.id.fontFamily)
        editTextFilledExposedDropdown.setAdapter(adapter)
        editTextFilledExposedDropdown.isEnabled = true
        editTextFilledExposedDropdown.setOnItemClickListener { adapterView, view, i, l ->
            currentFont = fonts[i]
            preview.setTypeface(currentFont!!.getTypeface(context.assets))
        }

        colorPickerView.setOnClickListener {
            showColorPicker()
        }
        imageSubmitMenu.setOnClickListener {
            saveBitmap()
            topBarController.clickSubmitTopBar()
        }

        editTextQuote.addTextChangedListener {
            preview.setText(it.toString())
        }
        checkboxTextAll.setOnCheckedChangeListener { _, isChecked ->
            isApplyForAll = isChecked
        }

        val defaultSize = 12f.dip()

//        seekBarSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
//                val size = defaultSize + p1.dipF()
//                preview.setTextSize(size)
//            }
//
//            override fun onStartTrackingTouch(p0: SeekBar?) {}
//            override fun onStopTrackingTouch(p0: SeekBar?) {}
//        })


        state?.applyTo(context.assets, preview)

        state?.textColor?.let {
            colorPickerView.setBackgroundColor(it)
        }

        state?.text.let {
            editTextQuote.text = it
        }

        state?.fontFamily?.let {
            currentFont = it
        }
    }

    fun saveBitmap() {
        if (isApplyForAll) {
            slideShowViewModel.setCurrentSlideAdapter(-1)
        }
        listener?.onReceiveQuoteBitmap(preview.getBitmap())
        listener?.newQuoteState(QuoteState.from(preview, currentFont))
    }


    private fun toggleBg() {
        bgColor = if (bgColor == Color.WHITE) Color.DKGRAY
        else Color.WHITE
        preview.setBackgroundColor(bgColor)
    }


    private fun Float.px(): Float {
        return this / resources.displayMetrics.density
    }


    @Px
    private fun Int.dip(): Int {
        return dipF().roundToInt()
    }

    @Px
    private fun Int.dipF(): Float {
        return toFloat().dip()
    }

    @Px
    private fun Float.dip(): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            resources.displayMetrics
        )
    }

    private fun showColorPicker() {
        ColorPickerDialog.Builder(context)
            .setTitle("Color Picker")
            .setPreferenceName("pref-color")
            .setPositiveButton("Confirm", object : ColorEnvelopeListener {
                override fun onColorSelected(envelope: ColorEnvelope, fromUser: Boolean) {
                    colorPickerView.setBackgroundColor(envelope.color)
                    preview.setTextColor(envelope.color)
                }
            })
            .setNegativeButton("Cancel") { dialogInterface, i ->
                dialogInterface.dismiss()
            }
            .attachAlphaSlideBar(true)
            .attachBrightnessSlideBar(true)
            .show()
    }

    fun setTopbarController(
        topBarController: TopBarController,
        slideShowViewModel: SlideShowViewModel) {
        this.topBarController = topBarController
        this.slideShowViewModel = slideShowViewModel

    }


    interface QuoteListener {
        fun onReceiveQuoteBitmap(bitmap: Bitmap)
        fun newQuoteState(quoteState: QuoteState)
    }
}