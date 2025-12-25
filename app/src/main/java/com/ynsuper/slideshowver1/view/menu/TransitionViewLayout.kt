package com.ynsuper.slideshowver1.view.menu

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import com.ynsuper.slideshowver1.R
import com.ynsuper.slideshowver1.base.BaseCustomConstraintLayout
import com.ynsuper.slideshowver1.callback.TopBarController
import android.widget.TextView
import android.widget.ImageView

class TransitionViewLayout : BaseCustomConstraintLayout {

    private lateinit var topBarController: TopBarController
    private lateinit var textNameTopBarView: TextView
    private lateinit var imageSubmitMenuView: ImageView
    private lateinit var recyclerViewTransitionsView: RecyclerView

    constructor(context: Context?) : super(context) {
        init(context)
        setLayoutInflate(R.layout.layout_template_view)

        initView()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!,
        attrs
    ) {
        init(context)
        setLayoutInflate(R.layout.layout_template_view)
        initView()

    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context!!, attrs, defStyleAttr) {
        init(context)
        setLayoutInflate(R.layout.layout_template_view)
        initView()

    }

    fun setTopBarName(name: String){
        textNameTopBarView.text = name
    }
    private fun initView() {
        bindViews()
        setTopBarName(context.getString(R.string.text_transition))
        imageSubmitMenuView.setOnClickListener {
            topBarController.clickSubmitTopBar()
        }
    }
    
    private fun bindViews() {
        textNameTopBarView = findViewById(R.id.text_name_top_bar)
        imageSubmitMenuView = findViewById(R.id.image_submit_menu)
        recyclerViewTransitionsView = findViewById(R.id.recyclerViewTransitions)
    }

    fun setTopbarController(topbarController: TopBarController) {
        this.topBarController = topbarController
    }

    fun getRecycleViewTransitions(): RecyclerView {
        return recyclerViewTransitionsView
    }
}