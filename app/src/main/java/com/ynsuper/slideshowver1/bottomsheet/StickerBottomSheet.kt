package com.ynsuper.slideshowver1.bottomsheet

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ynsuper.slideshowver1.R
import com.ynsuper.slideshowver1.callback.IUnzipFile
import com.ynsuper.slideshowver1.model.ListSticker
import com.ynsuper.slideshowver1.view.SlideShowActivity
import com.ynsuper.slideshowver1.view.adapter.*


class StickerBottomSheet : BottomSheetDialogFragment() , StickerAdapter.OnClickStickerListener {

    private lateinit var stickerPageAdapter: StickerPagerAdapter
    private  var stickerViewpaper: ViewPager? = null
    private lateinit var topbarEditAdapter: TopTabEditAdapter
    private var arrListSticker: ListSticker? = null
    private lateinit var mContext: SlideShowActivity
    private var addImageAdapter: AddImageAdapter? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.layout_sticker_bottom, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapterStickerTopBar(view)
    }

    private fun setAdapterStickerTopBar(view: View) {
        stickerViewpaper = view.findViewById(R.id.sticker_viewpaper)
        val displayMetrics = DisplayMetrics()
        stickerPageAdapter = StickerPagerAdapter(
            context,
            arrListSticker,
            displayMetrics.widthPixels,
            this, IUnzipFile {
                reloadDataDownload()

            }
        )
        stickerViewpaper?.setAdapter(stickerPageAdapter)
        topbarEditAdapter = TopTabEditAdapter(stickerViewpaper, mContext, arrListSticker)
        val recyclerTabLayout: RecyclerTabLayout = view.findViewById(R.id.recycler_tab_layout)
        recyclerTabLayout.setUpWithAdapter(topbarEditAdapter)
        recyclerTabLayout.setPositionThreshold(0.5f)
//        recyclerTabLayout.setBackgroundColor(getResources().getColor(R.color.white));
    }

    private fun reloadDataDownload() {
        stickerPageAdapter.loadPackageDownloaded(topbarEditAdapter.currentIndicatorPosition)
        topbarEditAdapter.notifyDataSetChanged()
        stickerPageAdapter.notifyDataSetChanged()
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SlideShowActivity) {
            this.mContext = context
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(listSticker: ListSticker?): StickerBottomSheet {
            return StickerBottomSheet().apply {
                this.arrListSticker = listSticker
            }
        }
    }

    override fun addSticker(bitmap: Bitmap?) {
        dismiss()
        (context as SlideShowActivity).addSticker(bitmap)
    }
}