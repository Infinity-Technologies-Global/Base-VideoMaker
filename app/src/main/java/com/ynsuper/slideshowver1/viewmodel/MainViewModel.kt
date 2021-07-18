package com.ynsuper.slideshowver1.viewmodel

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.GridLayoutManager
import androidx.room.Room
import com.ynsuper.slideshowver1.MainActivity
import com.ynsuper.slideshowver1.R
import com.ynsuper.slideshowver1.base.BaseViewModel
import com.ynsuper.slideshowver1.database.AppDatabase
import com.ynsuper.slideshowver1.databinding.ActivityMainBinding
import com.ynsuper.slideshowver1.model.ImageModel
import com.ynsuper.slideshowver1.util.Constant
import com.ynsuper.slideshowver1.util.entity.StoryEntity
import com.ynsuper.slideshowver1.view.SlideShowActivity
import com.ynsuper.slideshowver1.view.activity.MyVideosActivity
import com.ynsuper.slideshowver1.view.activity.ProActivity
import com.ynsuper.slideshowver1.view.adapter.DraftListAdapter
import com.ynsuper.slideshowver1.view.adapter.StoryListAdapter
import gun0912.tedimagepicker.builder.TedImagePicker
import java.io.File

class MainViewModel : BaseViewModel() {
    private var appDatabase: AppDatabase? = null
    private lateinit var binding: ActivityMainBinding
    private val adapter: DraftListAdapter = DraftListAdapter()
    private var activity: MainActivity? = null

    fun startImagePicker() {
        activity?.let { data ->
            TedImagePicker.with(data)
                .startMultiImage { uriList ->
                    val arrImageModel = ArrayList<ImageModel>()
                    for (uri in uriList) {
                        arrImageModel.add(ImageModel(uri))
                    }
                    val intent = Intent(data, SlideShowActivity::class.java)
                    intent.putParcelableArrayListExtra(
                        Constant.EXTRA_ARRAY_IMAGE,
                        arrImageModel
                    )
                    data.startActivity(intent)
                }
        } ?: run {
            Log.v(Constant.YNSUPER_TAG, "Permission is granted");
        }
    }

    fun setMainActivity(mainActivity: MainActivity) {
        this.activity = mainActivity
    }

    fun checkRuntimePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity!!.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED
        ) {

            Log.v(Constant.YNSUPER_TAG, "Permission is granted");
            //File write logic here
            loadDataDraftAndVideoSaved()
            adapter.onItemClicked = {
                // handle draft

            }


        } else {

        }
    }


    private fun loadDataDraftAndVideoSaved() {
        loadDataVideoSaved()
        loadDataDraft()
    }

    private fun loadDataVideoSaved() {
        appDatabase =
            Room.databaseBuilder(activity!!.baseContext, AppDatabase::class.java, "slideshow-v1")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()
        getAllFlow()
        loadDataVideo()

    }

    private fun loadDataDraft() {

    }



    private fun loadDataVideo() {
        val layoutManager = GridLayoutManager(activity, 3)
        binding.recycleMyDraft.layoutManager = layoutManager
        binding.recycleMyDraft.adapter = adapter
        binding.recycleMyDraft.setHasFixedSize(true)
    }

    fun setBinding(binding: ActivityMainBinding) {
        this.binding = binding
    }

    fun getAllFlow() {
        if (appDatabase != null) {

                adapter.patch(appDatabase?.slideDao()?.getAll()?.sortedByDescending { d -> d.createdAt }!!)
                if (adapter.items.size > 0){
                    binding.linearNoVideo.visibility = View.GONE
                    binding.recycleMyDraft.visibility = View.VISIBLE
                }
            }
    }


    fun selectNavigatorHome(): Boolean {

        return true
    }

    fun selectNavigatorMyVideo(): Boolean? {
        val intent = Intent(activity, MyVideosActivity::class.java)
        activity?.startActivity(intent)
        return true
    }

    fun selectNavigatorPro(): Boolean? {
        val intent = Intent(activity, ProActivity::class.java)
        activity?.startActivity(intent)
        return true
    }

}