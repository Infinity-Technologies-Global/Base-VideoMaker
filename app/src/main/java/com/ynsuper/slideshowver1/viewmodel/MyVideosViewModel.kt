package com.ynsuper.slideshowver1.viewmodel

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.room.Room
import com.ynsuper.slideshowver1.R
import com.ynsuper.slideshowver1.base.BaseViewModel
import com.ynsuper.slideshowver1.database.AppDatabase
import com.ynsuper.slideshowver1.databinding.ActivityMyVideosBinding
import com.ynsuper.slideshowver1.util.entity.StoryEntity
import com.ynsuper.slideshowver1.view.activity.MyVideosActivity
import com.ynsuper.slideshowver1.view.adapter.StoryListAdapter
import java.io.File

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
                if (adapter.items.size > 0) {
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
        adapter.onItemClicked = {
            play(it.path)
        }
        adapter.onDeleteClick = {
            createDialogDelete(it)
        }

        adapter.onSharedClick = { shareVideo(it.path)
        }
    }

    private fun shareVideo(filePath: String) {
        val file = File(filePath)
        val uri =
            FileProvider.getUriForFile(activity!!, "${activity!!.packageName}.fileprovider", file)

        ShareCompat.IntentBuilder.from(activity!!)
            .setStream(uri)
            .setType("video/mp4")
            .setChooserTitle("Share video...")
            .startChooser()

    }

    private fun createDialogDelete(story: StoryEntity) {
        val alertDialogBuilder = AlertDialog.Builder(activity)
        val view: View = activity?.layoutInflater!!.inflate(R.layout.dialog_remove_video, null)

        alertDialogBuilder?.setView(view)

        val dialog = alertDialogBuilder?.create()



        dialog.getWindow()?.setBackgroundDrawableResource(android.R.color.transparent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        alertDialogBuilder.show()
        activity!!.findViewById<TextView>(R.id.text_comfirm)?.setOnClickListener {
            appDatabase?.storyDao()?.delete(story)
            File(story.path).deleteRecursively()
            dialog.hide()
        }
        activity!!.findViewById<TextView>(R.id.text_cancel)?.setOnClickListener {
            dialog.hide()
        }
    }

    private fun play(path: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(path))
        intent.setDataAndType(Uri.parse(path), "video/mp4")
        activity!!.startActivity(intent)
    }

    fun backImageClick() {
        activity?.finish()
    }
}