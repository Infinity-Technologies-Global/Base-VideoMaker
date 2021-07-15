package com.ynsuper.slideshowver1.viewmodel

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ynsuper.slideshowver1.R
import com.ynsuper.slideshowver1.adapter.AlbumMusicAdapter
import com.ynsuper.slideshowver1.adapter.MusicAdapter
import com.ynsuper.slideshowver1.adapter.SoundManager
import com.ynsuper.slideshowver1.base.BaseViewModel
import com.ynsuper.slideshowver1.callback.APIService
import com.ynsuper.slideshowver1.databinding.FragmentAlbumMusicBinding
import com.ynsuper.slideshowver1.model.AlbumMusicModel
import com.ynsuper.slideshowver1.model.MusicModel
import com.ynsuper.slideshowver1.util.Constant
import com.ynsuper.slideshowver1.view.SlideShowActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers


class AlbumMusicViewModel : BaseViewModel(), AlbumMusicAdapter.OnAlbumMusicClickListener {
    private var rootId: Int? = null
    private var fragmentManager: FragmentManager? = null
    var binding: FragmentAlbumMusicBinding? = null
    var categoryMusicList: AlbumMusicModel? = null
    var context: Context? = null
    var musicByAlbumList: List<MusicModel>? = null
    var onSongClickListener: MusicAdapter.OnSongClickListener? = null

//    private var mediaPlayer: MediaPlayer? = null

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    fun loadDataAlbumMusic(categoryMusicList: AlbumMusicModel, context: Context?, onSongClickListener: MusicAdapter.OnSongClickListener) {
        this.context = context
        this.categoryMusicList = categoryMusicList
        this.onSongClickListener = onSongClickListener
        Log.d(Constant.NDPHH_TAG, "categoryList: ${categoryMusicList.getData()!!.size}")
        initDataGridView()

        loadEvents()
    }

    private fun loadEvents() {

    }

    private fun initDataGridView() {
        val layoutManager = GridLayoutManager(context, 3, RecyclerView.VERTICAL, false)
        val adapter = AlbumMusicAdapter(
            categoryMusicList,
            context,
            this
        )
        binding?.recyclerViewAlbum?.layoutManager = layoutManager
        binding?.recyclerViewAlbum?.adapter = adapter
    }

    override fun onAlbumClick(path: String?) {
        Log.e(Constant.NDPHH_TAG, "onAlbumClick: $path")
        APIService.service.getListMusic(path!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError({
                Toast.makeText(context,"Cannot connect to server", Toast.LENGTH_SHORT).show()

            })
            .doFinally {
            }.subscribeBy {
                if (it != null) {
                    Log.d(Constant.NDPHH_TAG, "ListMusicByAlbum nek: ${it.size}")
                    musicByAlbumList = it
                    if (context is SlideShowActivity){
                        (context as SlideShowActivity).changeCloseImageToBack()
                    }
                    binding?.recyclerViewAlbum?.visibility = View.GONE
                    binding?.listViewMusic?.visibility = View.VISIBLE
//                    binding?.containerSeekbar?.visibility = View.VISIBLE
                    val adapter = MusicAdapter(
                        musicByAlbumList,
                        context,
                        onSongClickListener
                    )
                    binding?.listViewMusic?.adapter = adapter
                }
            }.willBeDisposed()

    }

    private fun Disposable.willBeDisposed() {
        addTo(compositeDisposable)
    }

    fun setFragmentManager(fragmentManager: FragmentManager?, id: Int) {
        this.rootId = id
        this.fragmentManager = fragmentManager
    }

}