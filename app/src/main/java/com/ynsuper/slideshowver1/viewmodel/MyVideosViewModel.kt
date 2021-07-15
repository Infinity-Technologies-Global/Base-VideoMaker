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
import com.ynsuper.slideshowver1.view.activity.MyVideosActivity
import com.ynsuper.slideshowver1.view.adapter.StoryListAdapter

class MyVideosViewModel : BaseViewModel() {

    private lateinit var binding: ActivityMyVideosBinding


    private var appDatabase: AppDatabase? = null
    private val adapter: StoryListAdapter = StoryListAdapter()
    private var activity: MyVideosActivity? = null


    fun setBinding(binding: ActivityMyVideosBinding) {
        this.binding = binding
    }

    fun loadDataSaved() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity!!.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            loadDataVideoSaved()

        }

    }

    fun setCurrentActivity(myVideoActivity: MyVideosActivity) {
        this.activity = myVideoActivity
    }

    fun getAllFlow() {
        if (appDatabase != null) {
            appDatabase?.storyDao()?.getAllFlowable()?.observe(activity!!, Observer {
                adapter.patch(it.sortedByDescending { d -> d.createdAt })
                if (adapter.items.size > 0 ){
                    binding.recycleMyVideoList.visibility = View.VISIBLE
                    binding.linearNoVideo.visibility = View.GONE
                }
            })
        }

    }

    private fun loadDataVideoSaved() {

        appDatabase =
            Room.databaseBuilder(activity!!.baseContext, AppDatabase::class.java, "slideshow-v1")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()
        getAllFlow()
        setRecyclerViewConfigurations()

    }

    private fun setRecyclerViewConfigurations() {
        val layoutManager = GridLayoutManager(activity, 3)
        binding.recycleMyVideoList.layoutManager = layoutManager
        binding.recycleMyVideoList.adapter = adapter
        binding.recycleMyVideoList.setHasFixedSize(true)
    }

    fun backImageClick() {
        activity?.finish()
    }
}