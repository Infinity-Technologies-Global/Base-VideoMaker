package com.ynsuper.slideshowver1.view

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProviders
import com.seanghay.studio.gles.shader.filter.pack.PackFilter
import com.ynsuper.slideshowver1.R
import com.ynsuper.slideshowver1.adapter.MusicAdapter
import com.ynsuper.slideshowver1.adapter.SoundManager
import com.ynsuper.slideshowver1.base.BaseActivity
import com.ynsuper.slideshowver1.bottomsheet.FilterPackDialogFragment
import com.ynsuper.slideshowver1.callback.FilterListener
import com.ynsuper.slideshowver1.callback.SaveStateListener
import com.ynsuper.slideshowver1.callback.SceneOptionStateListener
import com.ynsuper.slideshowver1.databinding.ActivitySlideshowBinding
import com.ynsuper.slideshowver1.model.ImageModel
import com.ynsuper.slideshowver1.util.Constant
import com.ynsuper.slideshowver1.util.PermissionHelper
import com.ynsuper.slideshowver1.util.entity.AudioEntity
import android.content.pm.PackageManager
import android.widget.Toast
import com.ynsuper.slideshowver1.view.menu.BackgroundOptionsViewLayout
import com.ynsuper.slideshowver1.view.menu.DurationViewLayout
import com.ynsuper.slideshowver1.view.menu.MusicViewLayout
import com.ynsuper.slideshowver1.view.menu.TextQuoteViewLayout
import com.ynsuper.slideshowver1.view.sticker.QuoteState
import com.ynsuper.slideshowver1.viewmodel.SlideShowViewModel
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import java.util.*

class SlideShowActivity : BaseActivity(), SceneOptionStateListener,
    MusicAdapter.OnSongClickListener, MusicViewLayout.OnSelectedSongListener, SaveStateListener,
    TextQuoteViewLayout.QuoteListener , FilterListener, FilterPackDialogFragment.FilterPackListener{
    private val binding by binding<ActivitySlideshowBinding>(R.layout.activity_slideshow)
    private lateinit var viewModel: SlideShowViewModel

    private var mViews: ArrayList<View>? = null
    private var mCurrentView: StickerView? = null
    private var mContentRootView: ConstraintLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(Constant.YNSUPER_TAG, "SlideShowActivity onCreate: started")
        checkAndRequestAudioPermission()
        initView()
        initEvent()

    }
    
    private fun checkAndRequestAudioPermission() {
        if (!PermissionHelper.hasAudioPermission(this)) {
            Log.d(Constant.YNSUPER_TAG, "SlideShowActivity: requesting audio permission")
            PermissionHelper.requestAudioPermission(this)
        }
    }

    private fun initView() {
        Log.d(Constant.YNSUPER_TAG, "SlideShowActivity initView: started")
        
        val imageList = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra(Constant.EXTRA_ARRAY_IMAGE, ImageModel::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra<ImageModel>(Constant.EXTRA_ARRAY_IMAGE)
        }
        
        Log.d(Constant.YNSUPER_TAG, "SlideShowActivity: received ${imageList?.size ?: 0} images")

        binding.apply {
            lifecycleOwner = this@SlideShowActivity
            viewModel =
                ViewModelProviders.of(this@SlideShowActivity).get(SlideShowViewModel::class.java)
            binding.slideShowViewModel = viewModel
            viewModel.setBinding(binding)
            viewModel.initDataBase(this@SlideShowActivity)
            viewModel.loadDataImage(imageList)
            viewModel.loadTextQuote()
            viewModel.loadDataMusic()

            mContentRootView = binding.rlContentRoot
            mViews = ArrayList()

        }
        Log.d(Constant.YNSUPER_TAG, "SlideShowActivity initView: completed")
    }


    override fun onSelectedSong(songName: String) {
        super.onSelectedSong(songName)
        Log.e(Constant.YNSUPER_TAG, "onSelectedSong: $songName")

        viewModel.selectMusicSong(songName)
    }

    private fun initEvent() {
        binding.root.findViewById<View>(R.id.layout_menu_ratio).setOnClickListener { viewModel.selectMenuRatio() }
        binding.root.findViewById<View>(R.id.layout_menu_background).setOnClickListener { viewModel.selectMenuBackground() }
        binding.root.findViewById<View>(R.id.layout__menu_transition).setOnClickListener { viewModel.selectMenuTransition() }
        binding.root.findViewById<View>(R.id.layout_menu_music).setOnClickListener { viewModel.selectMenuMusic() }
        binding.root.findViewById<View>(R.id.layout_menu_duration).setOnClickListener { viewModel.selectMenuSpeed() }
        binding.root.findViewById<View>(R.id.layout_menu_sticker).setOnClickListener { viewModel.selectMenuSticker() }
        binding.root.findViewById<View>(R.id.layout_menu_effect).setOnClickListener { viewModel.selectMenuOverlay() }
        binding.root.findViewById<View>(R.id.layout_menu_filter).setOnClickListener { viewModel.selectMenuFilter() }
        binding.root.findViewById<View>(R.id.layout_menu_text).setOnClickListener { viewModel.selectMenuText() }
        binding.root.findViewById<View>(R.id.image_add_image).setOnClickListener { viewModel.showAddImageSheet() }
        binding.imageSaveDraft.setOnClickListener { viewModel.saveDraft() }
        binding.imageSaveVideo.setOnClickListener { viewModel.saveVideo() }
        binding.imageBack.setOnClickListener { finish() }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            val audioFile = data?.data ?: return

            try {
                val inputStream = contentResolver.openInputStream(audioFile) ?: return
                val cursor = contentResolver.query(audioFile, null, null, null, null)
                val nameColumn = cursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                val name = cursor.getString(nameColumn)

                val outputFile = File(externalCacheDir, "audio-${UUID.randomUUID()}" + name)
                val fileOutputStream = FileOutputStream(outputFile)
                IOUtils.copy(inputStream, fileOutputStream)
                inputStream.close()
                fileOutputStream.flush()
                fileOutputStream.close()
                cursor.close()

                val audio = AudioEntity(path = outputFile.path)
                viewModel.setAudio(audio)
                Log.d(Constant.YNSUPER_TAG, "Audio file saved: ${outputFile.path}")
            } catch (e: Exception) {
                Log.e(Constant.YNSUPER_TAG, "Error loading audio file: ${e.message}", e)
                Toast.makeText(this, "Không thể tải file audio: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PermissionHelper.REQUEST_CODE_AUDIO_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Log.d(Constant.YNSUPER_TAG, "Audio permission granted")
                } else {
                    Log.w(Constant.YNSUPER_TAG, "Audio permission denied")
                    Toast.makeText(this, "Cần quyền truy cập audio để phát nhạc", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewModel.liveDataCategory.observe(this, viewModel.changeCategoryMusicList)
    }


    override fun onDurationChange(state: DurationViewLayout.OptionState) {
        viewModel.onDurationChange(state)
    }

    fun reloadSlideBar() {
        viewModel.reloadSlideBar()
    }

    fun addMoreScene() {
        viewModel.addMoreScene()
    }

    override fun onBackGroundConfigChange(state: BackgroundOptionsViewLayout.OptionState) {
        viewModel.onBackgroundConfigChage(state)
    }

    fun changeCloseImageToBack() {
        viewModel.changeImageCloseToBack()
    }

    fun changeBackToCloseImage() {
        viewModel.changeBackToCloseImage()

    }

    override fun onPause() {
        super.onPause()
        SoundManager.getInstance(this).stopSound()
    }

    fun addSticker(bitmap: Bitmap?) {
        viewModel?.addStickerInVideo(bitmap)
    }

    override fun onExportVideo(width: Int, height: Int) {
        viewModel?.exportVideo(width, height)
    }

    fun changeRatioView(width: Int, height: Int) {
        viewModel?.changeRatioPreview(width, height)
    }

    override fun newQuoteState(quoteState: QuoteState) {
        viewModel.newQuoteState(quoteState)
    }

    override fun onReceiveQuoteBitmap(bitmap: Bitmap) {
        viewModel.onReceiverQuoteBitMap(bitmap)
    }

    override fun onFilterSelected(frameId: Int) {
        viewModel.onOverlaySelected(frameId)
    }

    override fun onBackToGroup() {

    }

    override fun onFilterPackSaved(filterPack: PackFilter) {
        viewModel.onFilterSelected(filterPack)
    }


//    override fun onSongDownloadClick(
//        url: String?,
//        name: String?,
//        progressBar: ProgressBar?,
//        imgDownUse: ImageView?,
//        isInternetMusic: Boolean
//    ) {
//        Log.e(Constant.NDPHH_TAG, "onDownloadClick Slikeshow")
//    }
//
//    override fun onSongClick(musicModel: MusicModel?) {
//        super.onSongClick(musicModel)
//        viewModel.songClick(musicModel)
//    }
}