package com.ynsuper.slideshowver1.view.menu

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import com.ynsuper.slideshowver1.R
import com.ynsuper.slideshowver1.base.BaseCustomConstraintLayout
import com.ynsuper.slideshowver1.callback.TopBarController
import com.ynsuper.slideshowver1.view.SlideShowActivity
import kotlinx.android.synthetic.main.item_layout_edit_top_view.view.*
import kotlinx.android.synthetic.main.layout_ratioview.view.*
import kotlinx.android.synthetic.main.layout_template_view.view.*

class RatioViewLayout : BaseCustomConstraintLayout {

    private lateinit var topBarController: TopBarController

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

    fun setTopBarName(name: String){
        text_name_top_bar.text = name
    }
    private fun initView() {
        setTopBarName(context.getString(R.string.text_ratio))
        image_ratio_1_1.setOnClickListener {
            (context as SlideShowActivity).changeRatioView(1,1)
        }
        image_ratio_4_5.setOnClickListener {
            (context as SlideShowActivity).changeRatioView(4,5)
        }
        image_ratio_9_16.setOnClickListener {
            (context as SlideShowActivity).changeRatioView(9,16)
        }
        image_ratio_16_9.setOnClickListener {
            (context as SlideShowActivity).changeRatioView(16,9)
        }
        image_ratio_4_3.setOnClickListener {
            (context as SlideShowActivity).changeRatioView(4,3)
        }
        image_ratio_2_3.setOnClickListener {
            (context as SlideShowActivity).changeRatioView(2,3)
        }
        image_ratio_2_1.setOnClickListener {
            (context as SlideShowActivity).changeRatioView(2,1)
        }

        image_submit_menu.setOnClickListener {
            topBarController.clickSubmitTopBar()
        }
    }

    fun setTopbarController(topbarController: TopBarController) {
        this.topBarController = topbarController
    }

    fun getRecycleViewTransitions(): RecyclerView {
        return recyclerViewTransitions
    }
}