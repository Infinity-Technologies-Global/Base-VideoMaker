package com.ynsuper.slideshowver1.view.activity

import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.ynsuper.slideshowver1.R
import com.ynsuper.slideshowver1.base.BaseActivity
import com.ynsuper.slideshowver1.databinding.ActivityMyVideosBinding
import com.ynsuper.slideshowver1.viewmodel.MyVideosViewModel
import kotlinx.android.synthetic.main.activity_my_videos.*

class MyVideosActivity : BaseActivity() {

    private val binding by binding<ActivityMyVideosBinding>(R.layout.activity_my_videos)
    private var viewModel : MyVideosViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    private fun initView() {
        binding.apply {
            lifecycleOwner = this@MyVideosActivity
            viewModel = ViewModelProviders.of(  this@MyVideosActivity).get(MyVideosViewModel::class.java)
            binding.myVideoViewModel = viewModel
            viewModel?.setBinding(binding)
            viewModel?.setCurrentActivity(this@MyVideosActivity)
            viewModel?.loadDataSaved()
        }
        image_back.setOnClickListener {
            viewModel?.backImageClick()
        }
    }
}