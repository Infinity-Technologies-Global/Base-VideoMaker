package com.ynsuper.slideshowver1.view.adapter

import android.net.Uri
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.util.Util
import com.seanghay.studio.gles.transition.Transition
import com.ynsuper.slideshowver1.R
import com.ynsuper.slideshowver1.util.Util.toPx
import kotlinx.android.synthetic.main.item_transition.view.*

class TransitionsAdapter(
    var items: List<Transition>
) : RecyclerView.Adapter<TransitionsAdapter.TransitionViewHolder>() {


    var selectionChange: () -> Unit = {}
    var onLongPressed: () -> Unit = {}


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

    override fun onBindViewHolder(holder: TransitionViewHolder, position: Int) {
        holder.bind(items[position])
        var requestOptions = RequestOptions()
        requestOptions = requestOptions.transforms(CenterCrop(), RoundedCorners(10.toPx))
        Glide.with(holder.itemView).load(
            Uri.parse("file:///android_asset/image_transition/" + items[position].imagePreview + ".png"))
            .apply(requestOptions)
            .into(holder.imagePreview!!)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransitionViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_transition, parent, false)
        return TransitionViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.item_transition
    }

    override fun getItemCount(): Int = items.size

    private fun longPressFired() {
        onLongPressed()
    }

    inner class TransitionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var imagePreview: AppCompatImageView? = null

        init {
            itemView.setOnClickListener {
                select(adapterPosition)
            }

            itemView.setOnLongClickListener {
                select(adapterPosition)
                longPressFired()
                true
            }
        }

        fun bind(transition: Transition) {
            with(itemView) {
                imagePreview = itemView.findViewById(R.id.image_preview)
                text_transition.text = transition.name
                isSelected = selectedAt == adapterPosition
            }
        }
    }
}