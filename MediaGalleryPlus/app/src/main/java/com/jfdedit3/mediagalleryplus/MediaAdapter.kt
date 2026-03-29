package com.jfdedit3.mediagalleryplus

import android.view.LayoutInflater
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.jfdedit3.mediagalleryplus.databinding.ItemMediaBinding

class MediaAdapter(
    private val onClick: (MediaItemModel) -> Unit
) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    private val items = mutableListOf<MediaItemModel>()

    fun submitList(newItems: List<MediaItemModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = ItemMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MediaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class MediaViewHolder(
        private val binding: ItemMediaBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MediaItemModel) {
            binding.mediaName.text = item.name
            binding.typeBadge.text = item.type.name

            if (item.type == MediaType.AUDIO) {
                binding.thumbnail.scaleType = ImageView.ScaleType.CENTER_INSIDE
                binding.thumbnail.setImageResource(android.R.drawable.ic_media_play)
            } else {
                binding.thumbnail.scaleType = ImageView.ScaleType.CENTER_CROP
                Glide.with(binding.thumbnail.context)
                    .load(item.uri)
                    .centerCrop()
                    .into(binding.thumbnail)
            }

            binding.root.setOnClickListener {
                onClick(item)
            }
        }
    }
}
