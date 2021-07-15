package com.ynsuper.slideshowver1.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.ynsuper.slideshowver1.adapter.MusicAdapter
import com.ynsuper.slideshowver1.base.BaseViewModel
import com.ynsuper.slideshowver1.databinding.FragmentMyMusicBinding
import com.ynsuper.slideshowver1.model.AudioModel
import java.util.ArrayList

class MyMusicViewModel : BaseViewModel() {

    var binding: FragmentMyMusicBinding? = null
    private val TAG: String? = MyMusicViewModel::javaClass.name
    private var context: Context? = null
    private var playListPath: List<AudioModel>? = null

    fun loadDataMyMusic(playList: List<AudioModel>?, context: Context?, onSongClickListener: MusicAdapter.OnSongClickListener) {
        this.context = context
        this.playListPath = playList
        Log.d(TAG, "ndphh myMusicList: ${playListPath?.size}")
        initDataGridView(onSongClickListener)

    }

    private fun initDataGridView(onSongClickListener: MusicAdapter.OnSongClickListener) {
        val adapter = MusicAdapter(
            playListPath,
            context,
            onSongClickListener,
            true
        )
        binding?.lvMyMusic?.adapter = adapter
    }
}