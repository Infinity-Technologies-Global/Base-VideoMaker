package com.ynsuper.slideshowver1.view.activity

import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.ynsuper.slideshowver1.R
import com.ynsuper.slideshowver1.base.BaseActivity
import com.ynsuper.slideshowver1.databinding.ActivityMyVideosBinding
import com.ynsuper.slideshowver1.databinding.ActivityProFeatureBinding
import com.ynsuper.slideshowver1.viewmodel.MyVideosViewModel
import com.ynsuper.slideshowver1.viewmodel.ProViewModel
import kotlinx.android.synthetic.main.activity_my_videos.*
import kotlinx.android.synthetic.main.activity_pro_feature.*

class ProActivity : BaseActivity() {
    private val binding by binding<ActivityProFeatureBinding>(R.layout.activity_pro_feature)
    private var viewModel : ProViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    private fun initView() {
        binding.apply {
            lifecycleOwner = this@ProActivity
            viewModel = ViewModelProviders.of(  this@ProActivity).get(ProViewModel::class.java)
            binding.proViewModel = viewModel
            viewModel?.setBinding(binding)
            viewModel?.setCurrentActivity(this@ProActivity)
        }
        imageBack.setOnClickListener {
            viewModel?.backImageClick()
        }
    }
}