package com.ynsuper.slideshowver1.view

import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.ynsuper.slideshowver1.R
import com.ynsuper.slideshowver1.base.BaseActivity
import com.ynsuper.slideshowver1.databinding.ActivitySplashScreenBinding
import com.ynsuper.slideshowver1.viewmodel.SplashScreenViewModel

class SplashScreenActivity : BaseActivity() {
    private lateinit var viewModel: SplashScreenViewModel
    private val binding by binding<ActivitySplashScreenBinding>(R.layout.activity_splash_screen)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    private fun initView() {
        binding.apply {
            lifecycleOwner = this@SplashScreenActivity
            viewModel =
                ViewModelProviders.of(this@SplashScreenActivity).get(SplashScreenViewModel::class.java)
            binding.viewmodel = viewModel
            viewModel.setBinding(binding)
            viewmodel?.loadSplashScreen(this@SplashScreenActivity)

        }
    }
}