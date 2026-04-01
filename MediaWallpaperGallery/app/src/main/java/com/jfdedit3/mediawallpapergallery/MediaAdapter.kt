package com.jfdedit3.mediawallpapergallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jfdedit3.mediawallpapergallery.databinding.ItemMediaBinding

class MediaAdapter(private val onClick: (MediaItem) -> Unit) : ListAdapter<MediaItem, MediaAdapter.MediaViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = ItemMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MediaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MediaViewHolder(private val binding: ItemMediaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MediaItem) {
            binding.mediaName.text = item.name
            binding.mediaType.text = if (item.type == MediaType.VIDEO) "VIDEO" else "IMAGE"
            Glide.with(binding.root.context).load(item.uri).centerCrop().into(binding.thumbnail)
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<MediaItem>() {
            override fun areItemsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean = oldItem == newItem
        }
    }
}
