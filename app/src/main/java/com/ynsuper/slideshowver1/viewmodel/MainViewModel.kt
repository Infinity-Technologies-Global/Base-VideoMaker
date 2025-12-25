package com.ynsuper.slideshowver1.viewmodel

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.ynsuper.slideshowver1.MainActivity
import com.ynsuper.slideshowver1.base.BaseViewModel
import com.ynsuper.slideshowver1.database.AppDatabase
import com.ynsuper.slideshowver1.databinding.ActivityMainBinding
import com.ynsuper.slideshowver1.model.ImageModel
import com.ynsuper.slideshowver1.util.Constant
import com.ynsuper.slideshowver1.util.entity.SlideEntity
import com.ynsuper.slideshowver1.view.SlideShowActivity
import com.ynsuper.slideshowver1.view.activity.MyVideosActivity
import com.ynsuper.slideshowver1.view.activity.ProActivity
import com.ynsuper.slideshowver1.timeline.view.ExoPlayerTimelineActivity
import com.ynsuper.slideshowver1.view.adapter.DraftListAdapter
import gun0912.tedimagepicker.builder.TedImagePicker
import java.io.File

class MainViewModel : BaseViewModel() {
    private var appDatabase: AppDatabase? = null
    private lateinit var binding: ActivityMainBinding
    private val adapter: DraftListAdapter = DraftListAdapter()
    private var activity: MainActivity? = null
    private var lastTime: Long = 0

    fun startImagePicker() {
        val currentActivity = activity ?: run {
            Log.e(Constant.YNSUPER_TAG, "startImagePicker: activity is null")
            return
        }

        Log.d(Constant.YNSUPER_TAG, "startImagePicker: launching TedImagePicker for images and videos")

        val mediaItems = ArrayList<ImageModel>()

        TedImagePicker.with(currentActivity)
            .image()
            .min(0, "Bạn có thể bỏ qua chọn ảnh")
            .max(50, "Bạn chỉ có thể chọn tối đa 50 ảnh")
            .showCameraTile(true)
            .errorListener { message ->
                Log.e(Constant.YNSUPER_TAG, "TedImagePicker (image) error: $message")
            }
            .startMultiImage { imageUris ->
                try {
                    Log.d(
                        Constant.YNSUPER_TAG,
                        "TedImagePicker image callback: selected ${imageUris.size} images"
                    )
                    for (uri in imageUris) {
                        Log.d(Constant.YNSUPER_TAG, "Image URI: $uri")
                        mediaItems.add(ImageModel(uri, isVideo = false))
                    }

                    // Sau khi chọn ảnh xong, cho chọn thêm video
                    TedImagePicker.with(currentActivity)
                        .video()
                        .min(0, "Bạn có thể bỏ qua chọn video")
                        .max(20, "Bạn chỉ có thể chọn tối đa 20 video")
                        .showCameraTile(true)
                        .errorListener { message ->
                            Log.e(
                                Constant.YNSUPER_TAG,
                                "TedImagePicker (video) error: $message"
                            )
                        }
                        .startMultiImage { videoUris ->
                            try {
                                Log.d(
                                    Constant.YNSUPER_TAG,
                                    "TedImagePicker video callback: selected ${videoUris.size} videos"
                                )
                                for (uri in videoUris) {
                                    Log.d(Constant.YNSUPER_TAG, "Video URI: $uri")
                                    mediaItems.add(ImageModel(uri, isVideo = true))
                                }

                                if (mediaItems.isEmpty()) {
                                    Log.w(
                                        Constant.YNSUPER_TAG,
                                        "No media selected (images or videos)"
                                    )
                                    return@startMultiImage
                                }

                                Log.d(
                                    Constant.YNSUPER_TAG,
                                    "Total media selected: ${mediaItems.size}"
                                )
                                // Navigate to ExoPlayer Timeline Demo
                                val intent =
                                    Intent(currentActivity, ExoPlayerTimelineActivity::class.java)
                                intent.putParcelableArrayListExtra(
                                    Constant.EXTRA_ARRAY_IMAGE,
                                    mediaItems
                                )
                                currentActivity.startActivity(intent)
                            } catch (e: Exception) {
                                Log.e(
                                    Constant.YNSUPER_TAG,
                                    "Error in TedImagePicker video callback: ${e.message}",
                                    e
                                )
                            }
                        }
                } catch (e: Exception) {
                    Log.e(
                        Constant.YNSUPER_TAG,
                        "Error in TedImagePicker image callback: ${e.message}",
                        e
                    )
                }
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
            val arrImageModel = ArrayList<ImageModel>()
            for (uri in adapter.items) {
                val file = File(uri.path)
                arrImageModel.add(ImageModel(Uri.fromFile(file)))
            }

            adapter.onItemClicked = {
                // handle draft
                val intent = Intent(activity, SlideShowActivity::class.java)
                intent.putParcelableArrayListExtra(
                    Constant.EXTRA_ARRAY_IMAGE, arrImageModel
                )
                activity!!.startActivity(intent)
            }


        } else {

        }
    }


    private fun loadDataDraftAndVideoSaved() {
        loadDataVideoDraft()
    }

    fun loadDataVideoDraft() {
        appDatabase =
            Room.databaseBuilder(activity!!.baseContext, AppDatabase::class.java, "slideshow-v1")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()
        getAllFlow()
        loadVideoDraft()

    }


    private fun loadVideoDraft() {
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.recycleMyDraft.layoutManager = layoutManager
        binding.recycleMyDraft.adapter = adapter
        binding.recycleMyDraft.setHasFixedSize(true)
    }

    fun setBinding(binding: ActivityMainBinding) {
        this.binding = binding
    }

    fun getAllFlow() {
        val listFilter = ArrayList<SlideEntity>()
        if (appDatabase != null) {

            val fromDb = appDatabase!!.slideDao().getAll()
            fromDb.filter { !File(it.path).exists() }.forEach(appDatabase!!.slideDao()::delete)


            listFilter.addAll(fromDb.filter { File(it.path).exists() })


            adapter.patch(listFilter)
            if (adapter.items.size > 0) {
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