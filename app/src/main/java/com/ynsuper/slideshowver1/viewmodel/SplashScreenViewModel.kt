package com.ynsuper.slideshowver1.viewmodel

import android.content.Intent
import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import com.ynsuper.slideshowver1.MainActivity
import com.ynsuper.slideshowver1.base.BaseViewModel
import com.ynsuper.slideshowver1.databinding.ActivitySplashScreenBinding
import com.ynsuper.slideshowver1.view.SplashScreenActivity

class SplashScreenViewModel : BaseViewModel() {
    private lateinit var binding: ActivitySplashScreenBinding
    private val TIME_SPLASH: Long = 1000
    fun setBinding(binding: ActivitySplashScreenBinding) {
        this.binding = binding
    }

    fun loadSplashScreen(splashScreenActivity: SplashScreenActivity) {
        val countDownTimer =
            object : CountDownTimer(TIME_SPLASH, 10) {
                override fun onTick(millisUntilFinished: Long) {
                    val progress: Float =
                        TIME_SPLASH * 1.0f / millisUntilFinished * 10
                    binding.progressBarSplash.setProgress(progress.toInt())
                }

                override fun onFinish() {
                    gotoHome(splashScreenActivity)

                }
            }.start()
    }

    private fun gotoHome(splashScreenActivity: SplashScreenActivity) {
        val intent = Intent(splashScreenActivity, MainActivity::class.java)
        splashScreenActivity.startActivity(intent)
        splashScreenActivity.finish()
    }

}