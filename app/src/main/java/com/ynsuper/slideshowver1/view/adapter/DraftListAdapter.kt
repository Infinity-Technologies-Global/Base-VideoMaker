package com.ynsuper.slideshowver1.view.adapter

import android.media.MediaMetadataRetriever
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ynsuper.slideshowver1.R
import com.ynsuper.slideshowver1.util.entity.SlideEntity
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView


class DraftListAdapter(
    var items: List<SlideEntity> = emptyList()
) : RecyclerView.Adapter<DraftListAdapter.ViewHolder>() {


    var onItemClicked: (SlideEntity) -> Unit = {}
    var onDeleteClick: (SlideEntity) -> Unit = {}


    private val retriver = MediaMetadataRetriever()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_video,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun patch(items: List<SlideEntity>) {

        val callback = object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return this@DraftListAdapter.items[oldItemPosition].id == items[newItemPosition].id
            }

            override fun getOldListSize(): Int {
                return this@DraftListAdapter.items.size
            }

            override fun getNewListSize(): Int {
                return items.size
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return this@DraftListAdapter.items[oldItemPosition] == items[newItemPosition]
            }
        }

        val diff = DiffUtil.calculateDiff(callback)
        diff.dispatchUpdatesTo(this)
        this.items = items
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val moreButton: ImageButton = view.findViewById(R.id.more)
        private val thumbnailView: ImageView = view.findViewById(R.id.thumbnail)
        private val titleTextView: TextView = view.findViewById(R.id.title)
        
        private val popupMenu = PopupMenu(itemView.context, moreButton)

        init {
            popupMenu.inflate(R.menu.more)

            itemView.setOnClickListener {
                onItemClicked(items[adapterPosition])
            }

            moreButton.setOnClickListener {
                popupMenu.show()
            }

            thumbnailView.setOnClickListener {
                onItemClicked(items[adapterPosition])
            }
            popupMenu.setOnMenuItemClickListener {
                if (it.itemId == R.id.delete) {
                    val item = items[adapterPosition]
                    onDeleteClick(item)
                    true
                } else false
            }
        }

        fun bind(item: SlideEntity) {

            val relativeTime = DateUtils.getRelativeTimeSpanString(item.createdAt).toString()
            titleTextView.text = relativeTime
            Glide.with(itemView)
                .load(item.path)
                .centerCrop()
                .into(thumbnailView)


        }


        private fun formatDuration(millis: Long): String {
            return String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(
                    TimeUnit.MILLISECONDS.toHours(
                        millis
                    )
                ),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(
                        millis
                    )
                )
            )
        }

        private fun formatDate(time: Long): String {
            val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            return dateFormat.format(Date(time))
        }

    }


}