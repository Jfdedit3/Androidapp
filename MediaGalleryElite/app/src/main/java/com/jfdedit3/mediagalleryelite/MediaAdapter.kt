package com.jfdedit3.mediagalleryelite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jfdedit3.mediagalleryelite.databinding.ItemMediaBinding

class MediaAdapter(
    private val settings: AppSettings,
    private val favoritesStore: FavoritesStore,
    private val onClick: (MediaItemModel) -> Unit,
    private val onLongClick: (MediaItemModel) -> Unit,
    private val onFavoriteClick: (MediaItemModel) -> Unit
) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    private val items = mutableListOf<MediaItemModel>()
    private val selectedIds = mutableSetOf<Long>()
    private var selectionMode = false

    fun submitList(newItems: List<MediaItemModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun setSelectionMode(enabled: Boolean) {
        selectionMode = enabled
        if (!enabled) selectedIds.clear()
        notifyDataSetChanged()
    }

    fun toggleSelection(item: MediaItemModel) {
        if (selectedIds.contains(item.id)) selectedIds.remove(item.id) else selectedIds.add(item.id)
        notifyDataSetChanged()
    }

    fun isSelected(item: MediaItemModel): Boolean = selectedIds.contains(item.id)
    fun getSelectedItems(): List<MediaItemModel> = items.filter { selectedIds.contains(it.id) }
    fun getSelectedCount(): Int = selectedIds.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = ItemMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MediaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size

    inner class MediaViewHolder(private val binding: ItemMediaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MediaItemModel) {
            binding.mediaName.text = item.name
            binding.mediaName.visibility = if (settings.showFileNames) View.VISIBLE else View.GONE
            binding.metaText.text = when (item.type) {
                MediaType.AUDIO -> item.albumName
                else -> item.folderName
            }
            binding.typeBadge.text = item.type.name
            binding.favoriteButton.setImageResource(
                if (favoritesStore.isFavorite(item.id)) android.R.drawable.btn_star_big_on
                else android.R.drawable.btn_star_big_off
            )

            if (item.type == MediaType.AUDIO) {
                binding.thumbnail.scaleType = ImageView.ScaleType.CENTER_INSIDE
                binding.thumbnail.setImageResource(android.R.drawable.ic_media_play)
            } else {
                binding.thumbnail.scaleType = ImageView.ScaleType.CENTER_CROP
                Glide.with(binding.thumbnail.context).load(item.uri).centerCrop().into(binding.thumbnail)
            }

            val selected = isSelected(item)
            binding.selectionOverlay.visibility = if (selected) View.VISIBLE else View.GONE
            binding.selectionCheck.visibility = if (selected) View.VISIBLE else View.GONE

            binding.favoriteButton.setOnClickListener { onFavoriteClick(item) }
            binding.root.setOnClickListener {
                if (selectionMode) {
                    toggleSelection(item)
                    onClick(item)
                } else {
                    onClick(item)
                }
            }
            binding.root.setOnLongClickListener {
                onLongClick(item)
                true
            }
        }
    }
}
