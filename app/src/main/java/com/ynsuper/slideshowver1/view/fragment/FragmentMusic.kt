package com.ynsuper.slideshowver1.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.ynsuper.slideshowver1.R
import com.ynsuper.slideshowver1.base.BaseFragment
import com.ynsuper.slideshowver1.databinding.FragmentMusicBinding
import com.ynsuper.slideshowver1.model.MusicModel
import com.ynsuper.slideshowver1.util.Constant
import com.ynsuper.slideshowver1.viewmodel.MusicViewModel

class FragmentMusic(musicByAlbumList: List<MusicModel>?) : BaseFragment() {
    var musicList = musicByAlbumList
    var viewModel: MusicViewModel? = null
    var binding: FragmentMusicBinding? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e(Constant.NDPHH_TAG, "on new fragment created")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentMusicBinding = DataBindingUtil.inflate<FragmentMusicBinding>(
            inflater,
            R.layout.fragment_music, container, false
        )

        viewModel = ViewModelProviders.of(this).get(MusicViewModel::class.java)
        fragmentMusicBinding.lifecycleOwner = viewLifecycleOwner
        fragmentMusicBinding.musicViewModel = viewModel
        viewModel?.binding = fragmentMusicBinding
        Log.e(Constant.NDPHH_TAG, "on new fragment onCreateView")

        return fragmentMusicBinding.root
    }
}