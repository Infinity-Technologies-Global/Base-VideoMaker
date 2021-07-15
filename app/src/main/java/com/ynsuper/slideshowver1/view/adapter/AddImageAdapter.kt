package com.ynsuper.slideshowver1.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.ynsuper.slideshowver1.R
import com.ynsuper.slideshowver1.util.Scene
import com.ynsuper.slideshowver1.util.Util.toPx
import com.ynsuper.slideshowver1.view.SlideShowActivity
import java.util.*

class AddImageAdapter(
    var items: ArrayList<Scene>,
    var mContext: SlideShowActivity
) : RecyclerView.Adapter<AddImageAdapter.ImageViewHolder>() {


    var selectionChange: () -> Unit = {}


    var selectedAt = -1
        set(value) {
            field = value
            selectionChange()
        }


    fun select(at: Int) {
        if (at == selectedAt) return
        notifyItemChanged(selectedAt)
        selectedAt = at
        notifyItemChanged(selectedAt)
    }


    init {
        setHasStableIds(true)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(items[position])
        var requestOptions = RequestOptions()
        requestOptions = requestOptions.transforms(CenterCrop(), RoundedCorners(10.toPx))

        Glide.with(holder.itemView).load(items.get(position).originalPath)
            .centerCrop()
            .apply(requestOptions)
            .into(holder.imagePreview!!)


    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_add_image, parent, false)

        return ImageViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.item_add_image
    }

    override fun getItemCount(): Int = items.size


    fun onMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(items, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(items, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
//        notifyDataSetChanged()
    }

    fun onSwipe(position: Int, direction: Int) {
//        notifyItemRemoved(position);
    }

    inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var imagePreview: AppCompatImageView? = null
        var imageSwap: AppCompatImageView? = null
        var imageDelete: AppCompatImageView? = null

        init {
            itemView.setOnClickListener {
                select(adapterPosition)
            }
        }

        fun bind(scene: Scene) {
            with(itemView) {
                imagePreview = itemView.findViewById(R.id.image_scene)
                imageSwap = itemView.findViewById(R.id.image_swap)
                imageDelete = itemView.findViewById(R.id.image_remove_scene)
                isSelected = selectedAt == adapterPosition
            }
            imageDelete?.setOnClickListener {
                items.remove(scene)
                notifyDataSetChanged()
                mContext.reloadSlideBar()
            }
        }
    }
}