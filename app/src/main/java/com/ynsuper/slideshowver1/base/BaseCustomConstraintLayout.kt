package com.ynsuper.slideshowver1.base

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.ynsuper.slideshowver1.R

open class BaseCustomConstraintLayout : ConstraintLayout {
    private var mContext: Context? = null
    private lateinit var mInflate: View

    constructor(context: Context?) : super(context!!) {
        init(context)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!,
        attrs
    ) {
        init(context)
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context!!, attrs, defStyleAttr) {
        init(context)
    }

    fun init(context: Context?) {
        this.mContext  = context
        setBackgroundColor(Color.WHITE)
    }

    fun setLayoutInflate(mView : Int){
        mInflate = LayoutInflater.from(mContext).inflate(mView,this, true)
    }


}