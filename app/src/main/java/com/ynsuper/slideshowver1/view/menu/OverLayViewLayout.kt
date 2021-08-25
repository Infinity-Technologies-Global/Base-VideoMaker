package com.ynsuper.slideshowver1.view.menu

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.os.AsyncTask
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.TextureView
import androidx.recyclerview.widget.LinearLayoutManager
import com.ynsuper.slideshowver1.R
import com.ynsuper.slideshowver1.base.BaseCustomConstraintLayout
import com.ynsuper.slideshowver1.callback.TopBarController
import com.ynsuper.slideshowver1.util.FilterUtils.OVERLAY_CONFIG
import com.ynsuper.slideshowver1.view.SlideShowActivity
import com.ynsuper.slideshowver1.view.adapter.FilterViewAdapter
import kotlinx.android.synthetic.main.item_layout_edit_top_view.view.*
import kotlinx.android.synthetic.main.layout_overlay.view.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class OverLayViewLayout : BaseCustomConstraintLayout {
    private lateinit var currentBitmap: TextureView
    private lateinit var mFilterViewAdapter: FilterViewAdapter
    private lateinit var topBarController: TopBarController
    private var state: OptionState? = null

    private val lstBitmapWithOverlay: ArrayList<Bitmap> = ArrayList()


    constructor(context: Context?) : super(context) {
        setLayoutInflate(R.layout.layout_overlay)

    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        setLayoutInflate(R.layout.layout_overlay)

    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setLayoutInflate(R.layout.layout_overlay)

    }

    private fun initView() {
        setTopBarName(context.getString(R.string.text_frame))
        LoadImageFrame().execute()
        image_submit_menu.setOnClickListener {

            topBarController.clickSubmitTopBar()
        }
    }

    inner class LoadImageFrame : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            lstBitmapWithOverlay.clear()
            val bitmap = ThumbnailUtils.extractThumbnail(currentBitmap.getBitmap(), 150, 150
            )
            lstBitmapWithOverlay.addAll(getLstBitmapWithFrame())
            return null
        }

        override fun onPreExecute() {
            super.onPreExecute()
            //showLoading
        }

        override fun onPostExecute(void: Void?) {
            val llmOverlay = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            rvOverlayView.layoutManager = llmOverlay
            rvOverlayView.setHasFixedSize(true)
            mFilterViewAdapter = FilterViewAdapter(
                lstBitmapWithOverlay,
                context as SlideShowActivity, context,
                OVERLAY_CONFIG.toMutableList()
            )

            rvOverlayView.adapter = mFilterViewAdapter
//        compareOverlay.setVisibility(VISIBLE)

        }
    }

    fun getLstBitmapWithFrame(): List<Bitmap> {
        var index = 0
        val arrayBitmap = ArrayList<Bitmap>()
        val images: Array<String> = context.getAssets().list("frame")!!
        images.sort()
        for (image : String in  images){
            val assetManager: AssetManager = context.getAssets()
            val bitmap = BitmapFactory.decodeStream(assetManager.open("frame/frame_"+index+".png"))
            index++
            arrayBitmap.add(bitmap)
        }
        return arrayBitmap

    }

    fun setTopbarController(topbarController: TopBarController) {
        this.topBarController = topbarController
    }

    fun setState(bitmap: TextureView) {
        this.currentBitmap = bitmap
        initView()

    }


    private fun saveState() {
        this.state?.let {

        }

    }


    private fun formatDuration(millis: Long): String {
        return String.format(
            "%02d sec",

            TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(
                    millis
                )
            )
        )
    }


    fun setTopBarName(name: String) {
        text_name_top_bar.text = name
    }


    data class OptionState(
        var id: String,
        var duration: Long,
        var crop: String? = "fit-center",
        var blur: Boolean = true,
        var delete: Boolean = false
    ) : Parcelable {

        constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readLong(),
            parcel.readString(),
            parcel.readByte() != 0.toByte(),
            parcel.readByte() != 0.toByte()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(id)
            parcel.writeLong(duration)
            parcel.writeString(crop)
            parcel.writeByte(if (blur) 1 else 0)
            parcel.writeByte(if (delete) 1 else 0)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<OptionState> {
            override fun createFromParcel(parcel: Parcel): OptionState {
                return OptionState(parcel)
            }

            override fun newArray(size: Int): Array<OptionState?> {
                return arrayOfNulls(size)
            }
        }
    }
}