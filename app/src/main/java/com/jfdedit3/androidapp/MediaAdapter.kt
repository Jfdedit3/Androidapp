package com.jfdedit3.androidapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jfdedit3.androidapp.databinding.ItemMediaBinding

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
            binding.videoBadge.text = if (item.type == MediaType.VIDEO) "VIDEO" else "IMAGE"

            Glide.with(binding.thumbnail.context)
                .load(item.uri)
                .centerCrop()
                .into(binding.thumbnail)

            binding.root.setOnClickListener {
                onClick(item)
            }
        }
    }
}
