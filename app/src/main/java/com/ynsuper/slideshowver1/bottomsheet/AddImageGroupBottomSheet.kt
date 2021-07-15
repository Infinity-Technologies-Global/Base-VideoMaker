package com.ynsuper.slideshowver1.bottomsheet

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ynsuper.slideshowver1.R
import com.ynsuper.slideshowver1.callback.SimpleItemTouchHelperCallback
import com.ynsuper.slideshowver1.callback.SimpleItemTouchHelperCallback.ItemTouchListenner
import com.ynsuper.slideshowver1.util.Scene
import com.ynsuper.slideshowver1.view.SlideShowActivity
import com.ynsuper.slideshowver1.view.adapter.AddImageAdapter
import java.util.*


class AddImageGroupBottomSheet : BottomSheetDialogFragment() {

    private lateinit var mContext: SlideShowActivity
    private lateinit var listScene: List<Scene>
    private var addImageAdapter: AddImageAdapter? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_add_image, container, false)
        loadDataRecycleView(view)
        val imageSubmitMenu = view.findViewById<ImageView>(R.id.image_submit_menu)
        val imageCloseMenu = view.findViewById<ImageView>(R.id.image_close_menu)
        imageSubmitMenu.setOnClickListener { dismiss() }
        imageCloseMenu.setOnClickListener { dismiss() }
        return view
    }

    private fun loadDataRecycleView(view: View) {
        addImageAdapter = listScene?.let {
            AddImageAdapter(it as ArrayList<Scene>,mContext)
        }
        val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val recycleImageView = view.findViewById<RecyclerView>(R.id.recycleImageView)
        val addScene = view.findViewById<ImageView>(R.id.image_add_scene)
        recycleImageView.layoutManager = linearLayoutManager
        recycleImageView.adapter = addImageAdapter
        val callback: ItemTouchHelper.Callback =
            SimpleItemTouchHelperCallback(object : ItemTouchListenner {
                override fun onMove(oldPosition: Int, newPosition: Int) {
                    addImageAdapter!!.onMove(oldPosition, newPosition)
                }

                override fun swipe(position: Int, direction: Int) {
//                    addImageAdapter!!.onSwipe(position, direction)
//                    if(direction == SimpleItemTouchHelperCallback.) {
////                        listScene.get(position).remove(position);
//                        addImageAdapter!!.notifyItemRemoved(position);
//                    }else{
//                        addImageAdapter!!.notifyItemChanged(position);
//                    }
//                    return true;
                }
            })
        val itemTouchHelper =
            ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recycleImageView)
        addScene.setOnClickListener {
            mContext.addMoreScene()
        }
    }


    //    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
//
//        dialog.setOnShowListener {
//            val bottomSheet = (it as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
//            val behavior = BottomSheetBehavior.from(bottomSheet!!)
//            behavior.state = BottomSheetBehavior.STATE_EXPANDED
//
//            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
//                override fun onStateChanged(bottomSheet: View, newState: Int) {
//                    if (newState == BottomSheetBehavior.STATE_DRAGGING) {
//                        behavior.state = BottomSheetBehavior.STATE_EXPANDED
//                    }
//                }
//
//                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
//            })
//        }
//
//        // Do something with your dialog like setContentView() or whatever
//        return dialog
//    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SlideShowActivity){
            this.mContext = context
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(listImage: List<Scene>): AddImageGroupBottomSheet {
            return AddImageGroupBottomSheet().apply {
                this.listScene = listImage
            }
        }
    }
}