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
import com.ynsuper.slideshowver1.util.entity.StoryEntity
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView


class StoryListAdapter(
    var items: List<StoryEntity> = emptyList()
) : RecyclerView.Adapter<StoryListAdapter.ViewHolder>() {


    var onItemClicked: (StoryEntity) -> Unit = {}
    var onSharedClick: (StoryEntity) -> Unit = {}
    var onDeleteClick: (StoryEntity) -> Unit = {}


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

    fun patch(items: List<StoryEntity>) {

        val callback = object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return this@StoryListAdapter.items[oldItemPosition].id == items[newItemPosition].id
            }

            override fun getOldListSize(): Int {
                return this@StoryListAdapter.items.size
            }

            override fun getNewListSize(): Int {
                return items.size
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return this@StoryListAdapter.items[oldItemPosition] == items[newItemPosition]
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
        private val subtitleTextView: TextView = view.findViewById(R.id.subtitle)
        
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
                if (it.itemId == R.id.rename) {

                }
                if (it.itemId == R.id.share) {

                    val item = items[adapterPosition]
                    onSharedClick(item)
                    true

                } else if (it.itemId == R.id.delete) {
                    val item = items[adapterPosition]
                    onDeleteClick(item)
                    true
                } else false
            }
        }

        fun bind(item: StoryEntity) {
            val relativeTime = DateUtils.getRelativeTimeSpanString(item.createdAt).toString()
            retriver.setDataSource(item.path)
            val duration =
                retriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
            titleTextView.text = item.title
//            titleTextView.text = SpannableStringBuilder(item.title)
//                .append(" \n")
//                .scale(.8f) {
//                    color(Color.parseColor("#3a3a3a")) {
//                        append(relativeTime)
//                    }
//                }

            subtitleTextView.text = formatDuration(duration)
//            subtitleTextView.setText(
//                SpannableStringBuilder(formatDate(item.createdAt) + " \n")
//                    .color(Color.BLACK) { append(formatDuration(duration)) }
//            )


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