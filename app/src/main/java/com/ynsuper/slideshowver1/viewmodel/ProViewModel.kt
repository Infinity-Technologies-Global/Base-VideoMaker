package com.ynsuper.slideshowver1.viewmodel

import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.GridLayoutManager
import androidx.room.Room
import com.ynsuper.slideshowver1.MainActivity
import com.ynsuper.slideshowver1.base.BaseViewModel
import com.ynsuper.slideshowver1.database.AppDatabase
import com.ynsuper.slideshowver1.databinding.ActivityMyVideosBinding
import com.ynsuper.slideshowver1.databinding.ActivityProFeatureBinding
import com.ynsuper.slideshowver1.view.activity.MyVideosActivity
import com.ynsuper.slideshowver1.view.activity.ProActivity
import com.ynsuper.slideshowver1.view.adapter.StoryListAdapter

class ProViewModel : BaseViewModel() {

    private lateinit var binding: ActivityProFeatureBinding


    private var activity: ProActivity? = null


    fun setBinding(binding: ActivityProFeatureBinding) {
        this.binding = binding
    }

    fun setCurrentActivity(proActivity: ProActivity) {
        this.activity = proActivity
    }

    fun backImageClick() {
        binding.imageBack.setOnClickListener {
            activity?.finish()
        }
    }


}