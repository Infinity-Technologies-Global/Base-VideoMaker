package com.ynsuper.slideshowver1.bottomsheet

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.text.bold
import androidx.core.text.color
import androidx.core.text.scale
import androidx.core.view.forEach
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.seanghay.studio.gles.shader.filter.pack.PackFilter
import com.ynsuper.slideshowver1.R
import android.widget.ImageView

class FilterPackDialogFragment : BottomSheetDialogFragment(), SeekBar.OnSeekBarChangeListener {

    private var listener: FilterPackListener? = null
    private val labelTextViews = mutableMapOf<String, TextView>()
    private var filterPack: PackFilter? = null
    
    private lateinit var imageSubmitMenu: ImageView
    private lateinit var buttonReset: ImageView
    private lateinit var layoutBrightness: ViewGroup
    private lateinit var layoutContrast: ViewGroup
    private lateinit var layoutSaturation: ViewGroup
    private lateinit var layoutTint: ViewGroup
    private lateinit var layoutGamma: ViewGroup
    private lateinit var layoutWarmth: ViewGroup
    private lateinit var layoutVibrant: ViewGroup
    private lateinit var layoutSepia: ViewGroup
    private lateinit var layoutIntensity: ViewGroup
    private lateinit var layoutFilterBrightness: ViewGroup
    private lateinit var layoutFilterContrast: ViewGroup
    private lateinit var layoutFilterSaturation: ViewGroup
    private lateinit var layoutFilterTint: ViewGroup
    private lateinit var layoutFilterGamma: ViewGroup
    private lateinit var layoutFilterWarmth: ViewGroup
    private lateinit var layoutFilterVibrant: ViewGroup
    private lateinit var layoutFilterSepia: ViewGroup
    private lateinit var layoutFilterIntensity: ViewGroup

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FilterPackListener) {
            this.listener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindViews(view)

        val packFilter = arguments?.getParcelable<PackFilter>(KEY_FILTER_PACK)
            ?: throw NullPointerException("PackFilter was null")
        this.filterPack = packFilter

        traverseLabels(view)
        setValues(filterPack)
        layoutFilterBrightness.setBackgroundColor(resources.getColor(R.color.colorPrimaryDark))

        buttonReset.visibility = View.VISIBLE
        seekBars.forEach {
            val seekBar = view.findViewById<SeekBar>(it.value)
            seekBar.setOnSeekBarChangeListener(this)
        }
        buttonReset.setOnClickListener {
            this.filterPack = PackFilter()
            setValues(this.filterPack!!)
        }
//        buttonReset {
//
//        }

        imageSubmitMenu.setOnClickListener {
            saveAndClose()
        }

        layoutFilterBrightness.setOnClickListener {
            resetColor()
            hideAllViewSeekBar()
            layoutBrightness.visibility = View.VISIBLE
            layoutFilterBrightness.setBackgroundColor(resources.getColor(R.color.colorPrimaryDark))

        }
        layoutFilterContrast.setOnClickListener {
            resetColor()
            hideAllViewSeekBar()
            layoutContrast.visibility = View.VISIBLE
            layoutFilterContrast.setBackgroundColor(resources.getColor(R.color.colorPrimaryDark))

        }
        layoutFilterSaturation.setOnClickListener {
            resetColor()
            hideAllViewSeekBar()
            layoutSaturation.visibility = View.VISIBLE
            layoutFilterSaturation.setBackgroundColor(resources.getColor(R.color.colorPrimaryDark))

        }
        layoutFilterTint.setOnClickListener {
            resetColor()
            hideAllViewSeekBar()
            layoutTint.visibility = View.VISIBLE
            layoutFilterTint.setBackgroundColor(resources.getColor(R.color.colorPrimaryDark))

        }
        layoutFilterGamma.setOnClickListener {
            resetColor()
            hideAllViewSeekBar()
            layoutGamma.visibility = View.VISIBLE
            layoutFilterGamma.setBackgroundColor(resources.getColor(R.color.colorPrimaryDark))

        }
        layoutFilterWarmth.setOnClickListener {
            resetColor()
            hideAllViewSeekBar()
            layoutWarmth.visibility = View.VISIBLE
            layoutFilterWarmth.setBackgroundColor(resources.getColor(R.color.colorPrimaryDark))

        }
        layoutFilterVibrant.setOnClickListener {
            resetColor()
            hideAllViewSeekBar()
            layoutVibrant.visibility = View.VISIBLE
            layoutFilterVibrant.setBackgroundColor(resources.getColor(R.color.colorPrimaryDark))

        }
        layoutFilterSepia.setOnClickListener {
            resetColor()
            hideAllViewSeekBar()
            layoutSepia.visibility = View.VISIBLE
            layoutFilterSepia.setBackgroundColor(resources.getColor(R.color.colorPrimaryDark))

        }
        layoutFilterIntensity.setOnClickListener {
            resetColor()
            hideAllViewSeekBar()
            layoutIntensity.visibility = View.VISIBLE
            layoutFilterIntensity.setBackgroundColor(resources.getColor(R.color.colorPrimaryDark))
        }


    }
    
    private fun bindViews(view: View) {
        imageSubmitMenu = view.findViewById(R.id.image_submit_menu)
        buttonReset = view.findViewById(R.id.buttonReset)
        layoutBrightness = view.findViewById(R.id.layout_brightness)
        layoutContrast = view.findViewById(R.id.layout_contrast)
        layoutSaturation = view.findViewById(R.id.layout_saturation)
        layoutTint = view.findViewById(R.id.layout_tint)
        layoutGamma = view.findViewById(R.id.layout_gamma)
        layoutWarmth = view.findViewById(R.id.layout_warmth)
        layoutVibrant = view.findViewById(R.id.layout_vibrant)
        layoutSepia = view.findViewById(R.id.layout_sepia)
        layoutIntensity = view.findViewById(R.id.layout_intensity)
        layoutFilterBrightness = view.findViewById(R.id.layout_filter_brightness)
        layoutFilterContrast = view.findViewById(R.id.layout_filter_contrast)
        layoutFilterSaturation = view.findViewById(R.id.layout_filter_saturation)
        layoutFilterTint = view.findViewById(R.id.layout_filter_tint)
        layoutFilterGamma = view.findViewById(R.id.layout_filter_gamma)
        layoutFilterWarmth = view.findViewById(R.id.layout_filter_warmth)
        layoutFilterVibrant = view.findViewById(R.id.layout_filter_vibrant)
        layoutFilterSepia = view.findViewById(R.id.layout_filter_sepia)
        layoutFilterIntensity = view.findViewById(R.id.layout_filter_intensity)
    }

    private fun hideAllViewSeekBar() {
        layoutBrightness.visibility = View.GONE
        layoutContrast.visibility = View.GONE
        layoutSaturation.visibility = View.GONE
        layoutTint.visibility = View.GONE
        layoutGamma.visibility = View.GONE
        layoutWarmth.visibility = View.GONE
        layoutVibrant.visibility = View.GONE
        layoutSepia.visibility = View.GONE
        layoutIntensity.visibility = View.GONE
    }

    private fun resetColor() {
        layoutFilterBrightness.setBackgroundColor(resources.getColor(R.color.color_main_edit))
        layoutFilterContrast.setBackgroundColor(resources.getColor(R.color.color_main_edit))
        layoutFilterSaturation.setBackgroundColor(resources.getColor(R.color.color_main_edit))
        layoutFilterTint.setBackgroundColor(resources.getColor(R.color.color_main_edit))
        layoutFilterGamma.setBackgroundColor(resources.getColor(R.color.color_main_edit))
        layoutFilterWarmth.setBackgroundColor(resources.getColor(R.color.color_main_edit))
        layoutFilterVibrant.setBackgroundColor(resources.getColor(R.color.color_main_edit))
        layoutFilterSepia.setBackgroundColor(resources.getColor(R.color.color_main_edit))
        layoutFilterIntensity.setBackgroundColor(resources.getColor(R.color.color_main_edit))

    }

    private fun saveAndClose() {
        listener?.onFilterPackSaved(filterPack!!.copy())
        dismiss()
    }

    private inline operator fun Button.invoke(crossinline invoker: () -> Unit) {
        setOnClickListener { invoker() }
    }

    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
        if (p0 == null) return
        if (!p2) return

        for (seekBar in seekBars) {
            if (seekBar.value == p0.id) {
                val tag = seekBar.key
                val converter = decodeFunctions[tag]
                packFilterMap[tag]?.set(this.filterPack!!, converter!!(p1.toFloat()))
                setValues(this.filterPack)
            }
        }
    }

    override fun onStartTrackingTouch(p0: SeekBar?) {

    }

    override fun onStopTrackingTouch(p0: SeekBar?) {

    }


    private fun setValues(filterPack: PackFilter?) {
        if (filterPack == null) return
        seekBars.forEach {
            val seekBar = view?.findViewById<SeekBar>(it.value)
            val value = packFilterMap[it.key]?.get(filterPack) ?: 0f

            val converter = encodeFunctions[it.key]
            val progress = converter!!(value)
            seekBar?.progress = progress.toInt()
            val labelConverter = labelFunctions[it.key]

            labelTextViews[it.key]?.text =
                wrap(labels[it.key] ?: "", labelConverter!!(progress).toInt())
        }
    }

    private fun traverseLabels(view: View?) {
        if (view == null) return
        if (view is ViewGroup) view.forEach { traverseLabels(it) }
        else {
            if (view is TextView && view.tag != null) {
                if (view.tag is String) {
                    if (labels.containsKey(view.tag as String)) {
                        labelTextViews[view.tag as String] = view
                    }
                }
            }
        }
    }


    private fun wrap(label: String, progress: Int): CharSequence {
        return SpannableStringBuilder(label)
            .append(" • ")
            .color(Color.WHITE) {
                scale(.8f) {
                    bold {
                        append("$progress%")
                    }
                }
            }
    }


    interface FilterPackListener {
        fun onFilterPackSaved(filterPack: PackFilter)
    }

    companion object {
        private const val KEY_FILTER_PACK = "filterPack"

        private val packFilterMap = mapOf(
            "brightness" to PackFilter::brightness,
            "contrast" to PackFilter::contrast,
            "saturation" to PackFilter::saturation,
            "gamma" to PackFilter::gamma,
            "tint" to PackFilter::tint,
            "warmth" to PackFilter::warmth,
            "sepia" to PackFilter::sepia,
            "vibrant" to PackFilter::vibrant,
            "intensity" to PackFilter::intensity
        )

        private val labels = mapOf(
            "brightness" to "Brightness",
            "contrast" to "Contrast",
            "saturation" to "Saturation",
            "gamma" to "Gamma",
            "tint" to "Tint",
            "warmth" to "Warmth",
            "sepia" to "Sepia",
            "vibrant" to "Vibrant",
            "intensity" to "Intensity"
        )

        private val seekBars = mapOf(
            "brightness" to R.id.brightness,
            "contrast" to R.id.contrast,
            "saturation" to R.id.saturation,
            "gamma" to R.id.gamma,
            "tint" to R.id.tint,
            "warmth" to R.id.warmth,
            "sepia" to R.id.sepia,
            "vibrant" to R.id.vibrant,
            "intensity" to R.id.intensity
        )

        private val encodeFunctions = mapOf<String, ((Float) -> Float)>(
            "brightness" to { value -> (0.5f + value) * 100f },
            "contrast" to { value -> value * 50f },
            "saturation" to { value -> value * 50f },
            "gamma" to { value -> value * 50f },
            "tint" to { value -> (0.5f + value) * 100f },
            "warmth" to { value -> (value + 0.5f) * 100f },
            "sepia" to { value -> value * 100f },
            "vibrant" to { value -> (0.5f + value) * 100f },
            "intensity" to { value -> value * 100f }
        )

        private val decodeFunctions = mapOf<String, ((Float) -> Float)>(
            "brightness" to { value -> (value / 100f) - 0.5f },
            "contrast" to { value -> value / 50f },
            "saturation" to { value -> value / 50f },
            "gamma" to { value -> value / 50f },
            "tint" to { value -> (value / 100f) - 0.5f },
            "warmth" to { value -> (value / 100f) - 0.5f },
            "sepia" to { value -> value / 100f },
            "vibrant" to { value -> (value / 100f) - 0.5f },
            "intensity" to { value -> value / 100f }
        )

        private val labelFunctions = mapOf<String, ((Float) -> Float)>(
            "brightness" to { value -> value - 50f },
            "contrast" to { value -> value - 50f },
            "saturation" to { value -> value - 50f },
            "gamma" to { value -> value - 50f },
            "tint" to { value -> value - 50f },
            "warmth" to { value -> value - 50f },
            "sepia" to { value -> value },
            "vibrant" to { value -> value - 50f },
            "intensity" to { value -> value }
        )


        @JvmStatic
        fun newInstance(filter: PackFilter): FilterPackDialogFragment {
            val fragment = FilterPackDialogFragment()
            val args = Bundle()
            args.putParcelable(KEY_FILTER_PACK, filter)
            fragment.arguments = args
            return fragment
        }
    }
}