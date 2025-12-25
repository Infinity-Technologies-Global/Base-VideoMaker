package com.ynsuper.slideshowver1.view.menu

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import com.ynsuper.slideshowver1.R
import com.ynsuper.slideshowver1.base.BaseCustomConstraintLayout
import com.ynsuper.slideshowver1.callback.TopBarController
import com.ynsuper.slideshowver1.view.SlideShowActivity
import android.widget.ImageView
import android.widget.TextView

class RatioViewLayout : BaseCustomConstraintLayout {

    private lateinit var topBarController: TopBarController

    private lateinit var topBarTitleTextView: TextView
    private lateinit var ratio1_1Image: ImageView
    private lateinit var ratio4_5Image: ImageView
    private lateinit var ratio9_16Image: ImageView
    private lateinit var ratio16_9Image: ImageView
    private lateinit var ratio4_3Image: ImageView
    private lateinit var ratio2_3Image: ImageView
    private lateinit var ratio2_1Image: ImageView
    private lateinit var submitImageView: ImageView

    constructor(context: Context?) : super(context) {
        init(context)
        setLayoutInflate(R.layout.layout_ratioview)

        initView()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!,
        attrs
    ) {
        init(context)
        setLayoutInflate(R.layout.layout_ratioview)
        initView()

    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context!!, attrs, defStyleAttr) {
        init(context)
        setLayoutInflate(R.layout.layout_ratioview)
        initView()

    }

    private fun initView() {
        bindViews()
        setTopBarName(context.getString(R.string.text_ratio))
        ratio1_1Image.setOnClickListener {
            (context as SlideShowActivity).changeRatioView(1,1)
        }
        ratio4_5Image.setOnClickListener {
            (context as SlideShowActivity).changeRatioView(4,5)
        }
        ratio9_16Image.setOnClickListener {
            (context as SlideShowActivity).changeRatioView(9,16)
        }
        ratio16_9Image.setOnClickListener {
            (context as SlideShowActivity).changeRatioView(16,9)
        }
        ratio4_3Image.setOnClickListener {
            (context as SlideShowActivity).changeRatioView(4,3)
        }
        ratio2_3Image.setOnClickListener {
            (context as SlideShowActivity).changeRatioView(2,3)
        }
        ratio2_1Image.setOnClickListener {
            (context as SlideShowActivity).changeRatioView(2,1)
        }

        submitImageView.setOnClickListener {
            topBarController.clickSubmitTopBar()
        }
    }

    private fun bindViews() {
        topBarTitleTextView = findViewById(R.id.text_name_top_bar)
        ratio1_1Image = findViewById(R.id.image_ratio_1_1)
        ratio4_5Image = findViewById(R.id.image_ratio_4_5)
        ratio9_16Image = findViewById(R.id.image_ratio_9_16)
        ratio16_9Image = findViewById(R.id.image_ratio_16_9)
        ratio4_3Image = findViewById(R.id.image_ratio_4_3)
        ratio2_3Image = findViewById(R.id.image_ratio_2_3)
        ratio2_1Image = findViewById(R.id.image_ratio_2_1)
        submitImageView = findViewById(R.id.image_submit_menu)
    }

    private fun setTopBarName(name: String){
        topBarTitleTextView.text = name
    }

    fun setTopbarController(topbarController: TopBarController) {
        this.topBarController = topbarController
    }

    fun getRecycleViewTransitions(): RecyclerView? {
        return null
    }
}