package com.ynsuper.slideshowver1

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.ynsuper.slideshowver1.base.BaseActivity
import com.ynsuper.slideshowver1.databinding.ActivityMainBinding
import com.ynsuper.slideshowver1.util.Constant
import com.ynsuper.slideshowver1.util.PermissionHelper
import com.ynsuper.slideshowver1.viewmodel.MainViewModel

class MainActivity : BaseActivity() {

    private lateinit var buttonStart: LinearLayout
    private lateinit var buttonMyVideo: LinearLayout
    private val binding by binding<ActivityMainBinding>(R.layout.activity_main)
    private var viewModel: MainViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    private fun initView() {
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        binding.apply {
            lifecycleOwner = this@MainActivity
            mainViewModel = viewModel
        }
        viewModel?.setBinding(binding)
        viewModel?.setMainActivity(this)
        viewModel?.checkRuntimePermission()
        buttonStart = findViewById(R.id.linearCreateVideo)
        buttonMyVideo = findViewById(R.id.linear_my_video)
        buttonStart.setOnClickListener {
            Log.d(Constant.YNSUPER_TAG, "Create Slideshow button clicked")
            viewModel?.startImagePicker()
        }
        binding.navigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> viewModel?.selectNavigatorHome()!!
                R.id.navigation_my_video -> viewModel?.selectNavigatorMyVideo()!!
                R.id.navigation_pro -> viewModel?.selectNavigatorPro()!!
                else -> {
                    false
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        viewModel?.loadDataVideoDraft()
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PermissionHelper.REQUEST_CODE_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Log.d(Constant.YNSUPER_TAG, "Storage permission granted in MainActivity")
                    viewModel?.loadDataVideoDraft()
                } else {
                    Log.w(Constant.YNSUPER_TAG, "Storage permission denied in MainActivity")
                    Toast.makeText(this, "Cần quyền truy cập bộ nhớ để sử dụng app", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}