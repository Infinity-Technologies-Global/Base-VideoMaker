package com.ynsuper.slideshowver1.view.fragment

//import org.apache.tika.exception.TikaException;
//import org.apache.tika.metadata.Metadata;
//import org.apache.tika.parser.ParseContext;
//import org.apache.tika.parser.Parser;
//import org.apache.tika.parser.mp3.Mp3Parser;
//import org.xml.sax.ContentHandler;
//import org.xml.sax.SAXException;
//import org.xml.sax.helpers.DefaultHandler;
import android.app.ProgressDialog
import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.loader.content.CursorLoader
import com.ynsuper.slideshowver1.R
import com.ynsuper.slideshowver1.adapter.MusicAdapter
import com.ynsuper.slideshowver1.base.BaseFragment
import com.ynsuper.slideshowver1.databinding.FragmentMyMusicBinding
import com.ynsuper.slideshowver1.model.AudioModel
import com.ynsuper.slideshowver1.util.Constant
import com.ynsuper.slideshowver1.util.Constants
import com.ynsuper.slideshowver1.viewmodel.MyMusicViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import java.io.*


class FragmentMyMusic(private val onSongClickListener: MusicAdapter.OnSongClickListener) : BaseFragment() {

    private var progressDialog: ProgressDialog? = null
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var viewModel: MyMusicViewModel? = null
    val MEDIA_PATH: String = Environment.getExternalStorageDirectory().toString()
//    val MEDIA_PATH: String = "/storage/sdcard1/"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val fragmentMyMusicBinding = DataBindingUtil.inflate<FragmentMyMusicBinding>(
            inflater,
            R.layout.fragment_my_music, container, false
        )
        viewModel = ViewModelProviders.of(this).get(MyMusicViewModel::class.java)
        fragmentMyMusicBinding.lifecycleOwner = viewLifecycleOwner
        fragmentMyMusicBinding.myMusicViewModel = viewModel
        viewModel?.binding = fragmentMyMusicBinding

//        showProgressDialogWithTitle("Scanning for media, please wait!")

        Thread(
            Runnable {
                val subscribe = Observable.just(getAllAudioFromDevice(context!!))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError { t: Throwable? ->
                        Log.d(Constant.NDPHH_TAG, "ErrMyMusic: " + t!!.message)
                    }
                    .subscribe {
                        if (it != null && it.size > 0) {
                            Log.d(Constant.NDPHH_TAG, "MyMusic size: " + it.size)
                            viewModel?.loadDataMyMusic(it, context, onSongClickListener)
//                            hideProgressDialogWithTitle()
                            viewModel!!.binding!!.txtWaitingScanning.visibility = View.GONE
                        } else {
                            viewModel!!.binding!!.txtWaitingScanning.text = "No music found!"
                        }
                    }
            }
        ).start()

        viewModel!!.binding!!.swipeMyMusic.setOnRefreshListener {
            Thread(
                Runnable {
                    val subscribe = Observable.just(getAllAudioFromDevice(context!!))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnError { t: Throwable? ->
                            Log.d(Constant.NDPHH_TAG, "ErrMyMusic: " + t!!.message)
                        }
                        .subscribe {
                            if (it != null && it.size > 0) {
                                Log.d(Constant.NDPHH_TAG, "MyMusic size: " + it.size)
                                viewModel?.loadDataMyMusic(it, context, onSongClickListener)
//                            hideProgressDialogWithTitle()
                                viewModel!!.binding!!.txtWaitingScanning.visibility = View.GONE
                            } else {
                                viewModel!!.binding!!.txtWaitingScanning.text = "No music found!"
                            }
                        }
                }
            ).start()
            viewModel!!.binding!!.swipeMyMusic.isRefreshing = false
        }

        return fragmentMyMusicBinding.root
    }

    // Method to show Progress bar
    private fun showProgressDialogWithTitle(substring: String) {
        progressDialog = ProgressDialog(context)
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        //Without this user can hide loader by tapping outside screen
        progressDialog!!.setCancelable(false)
        progressDialog!!.setMessage(substring)
        progressDialog!!.show()
    }

    // Method to hide/ dismiss Progress bar
    private fun hideProgressDialogWithTitle() {
        if (progressDialog != null) {
            progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            progressDialog!!.dismiss()
        }
    }

    private fun Disposable.willBeDisposed() {
        addTo(compositeDisposable)
    }

    private fun initView(view: View?) {

    }

    private fun getPlayList(rootPath: String?): ArrayList<AudioModel>? {
        val fileList: ArrayList<AudioModel> = ArrayList()
        return try {
            val rootFolder = File(rootPath)
            val files: Array<File> =
                rootFolder.listFiles() //here you will get NPE if directory doesn't contains  any file,handle it like this.
            for (file in files) {
                if (file.isDirectory) {
                    if (getPlayList(file.absolutePath) != null) {
                        fileList.addAll(getPlayList(file.absolutePath)!!)
                    } else {
                        break
                    }
                } else if (file.name.endsWith(".mp3")) {
                    val song = AudioModel()

                    try {
                        val mmr = MediaMetadataRetriever()
                        mmr.setDataSource(file.absolutePath)

                        val title = file.nameWithoutExtension
                        if (mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                                .toString() == "null") {
                            song.name = title
                        } else {
                            song.name = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                                .toString()
                        }

                        val artists = "Unknown"
                        if (mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                                .toString() == "null") {
                            song.artist = artists
                        } else {
                            song.artist =  mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                                .toString()
                        }

                        val composer = "Unknown"
                        if (mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER)
                                .toString() == "null") {
                            song.composer = composer
                        } else {
                            song.composer = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER)
                                .toString()
                        }

                        val genre = "Unknown"
                        if (mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
                                .toString() == "null") {
                            song.genre = genre
                        } else {
                            song.genre = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
                                .toString()
                        }

                        val album = "Unknown"
                        if (mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                                .toString() == "null") {
                            song.album = album
                        } else {
                            song.album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                                .toString()
                        }
                        song.path = file.absolutePath
                        fileList.add(song)
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace();
                    } catch (e: IOException) {
                        e.printStackTrace();
                    }
                }
            }
            fileList
        } catch (e: Exception) {
            null
        }
    }

    private fun getAllAudioFromDevice(context: Context): List<AudioModel>? {

//        var cursorLoader: CursorLoader? = null
//        if (id === Constants.MEDIA_TYPE_AUDIO) {
//            val audio_condition: String =
//                getSelectionArgsForSingleMediaCondition(getDurationCondition(0, AUDIO_DURATION))
//            val MEDIA_TYPE_AUDIO = arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO.toString())
//            cursorLoader = CursorLoader(
//                mContext, QUERY_URI, PROJECTION, audio_condition, MEDIA_TYPE_AUDIO, ORDER_BY
//            )
//        }



        val tempAudioList: MutableList<AudioModel> = ArrayList()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.AudioColumns.DATA,
            MediaStore.Audio.AudioColumns.ALBUM,
            MediaStore.Audio.ArtistColumns.ARTIST
        )
        val c: Cursor = context.contentResolver.query(
            uri,
            projection,
            null,
            null,
            null
        )!!
        while (c.moveToNext()) {
            val audioModel = AudioModel()
            val path = c.getString(0)
            val album = c.getString(1)
            val artist = c.getString(2)
            val name = path.substring(path.lastIndexOf("/") + 1)
            audioModel.name = name
            audioModel.album = album
            audioModel.artist = artist
            audioModel.path = path
            tempAudioList.add(audioModel)
            Log.e(Constant.NDPHH_TAG, "List local music size: ${tempAudioList.size}")
        }
        c.close()

        return tempAudioList
    }

}