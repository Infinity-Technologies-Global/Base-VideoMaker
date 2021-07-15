package com.ynsuper.slideshowver1.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import com.ynsuper.slideshowver1.R
import com.ynsuper.slideshowver1.adapter.AlbumMusicAdapter
import com.ynsuper.slideshowver1.adapter.MusicAdapter
import com.ynsuper.slideshowver1.base.BaseFragment
import com.ynsuper.slideshowver1.databinding.FragmentAlbumMusicBinding
import com.ynsuper.slideshowver1.model.AlbumMusicModel
import com.ynsuper.slideshowver1.viewmodel.AlbumMusicViewModel

class FragmentAlbumMusic(fmManager: FragmentManager,
                         private var onSongClickListener: MusicAdapter.OnSongClickListener
) : BaseFragment() {
    var fm = fmManager
    var categoryMusicList: AlbumMusicModel? = null
    var viewModel: AlbumMusicViewModel? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentAlbumMusicBinding = DataBindingUtil.inflate<FragmentAlbumMusicBinding>(
            inflater,
            R.layout.fragment_album_music, container, false
        )

//        val fragmentTransition = fm!!.beginTransaction()
//        fragmentTransition.add(R.id.albumMusicContainer, fragmentAlbumMusic!!)
//        fragmentTransition.addToBackStack(null)
//        fragmentTransition.commit()

        viewModel = ViewModelProviders.of(this).get(AlbumMusicViewModel::class.java)
        fragmentAlbumMusicBinding.lifecycleOwner = viewLifecycleOwner
        fragmentAlbumMusicBinding.albumViewModel = viewModel
        viewModel?.binding = fragmentAlbumMusicBinding
        categoryMusicList?.let {
            viewModel?.loadDataAlbumMusic(it, context, onSongClickListener)

        }
        viewModel?.setFragmentManager(fm, container!!.id)
//        Log.d("Ynsuper Log", "Ynsuper categoryList ban dau: $categoryMusicList")
//        Log.d("Ynsuper Log", "Ynsuper categoryList sau : ${viewModel!!.getCategoryMusicList()}")
        viewModel?.categoryMusicList = categoryMusicList
        return fragmentAlbumMusicBinding.root

    }

    fun setCategoryList(it: AlbumMusicModel) {
        categoryMusicList = it
    }

    companion object {
        private lateinit var fmManager: FragmentManager
        private var fragmentAlbumMusic: FragmentAlbumMusic? = null

        @JvmStatic
        fun newInstance(fragmentManager: FragmentManager, onSongClickListener: MusicAdapter.OnSongClickListener): FragmentAlbumMusic {
            this.fmManager = fragmentManager
            if (fragmentAlbumMusic==null ){
                fragmentAlbumMusic = FragmentAlbumMusic(fmManager, onSongClickListener)
            }
            return fragmentAlbumMusic as FragmentAlbumMusic
        }
    }

}