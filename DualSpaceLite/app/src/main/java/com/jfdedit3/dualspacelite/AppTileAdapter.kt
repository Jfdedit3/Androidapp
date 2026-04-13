package com.jfdedit3.dualspacelite

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jfdedit3.dualspacelite.databinding.ItemAppTileBinding

class AppTileAdapter(
    private val onClick: (AppInfo) -> Unit,
    private val onLongClick: ((AppInfo) -> Unit)? = null
) : RecyclerView.Adapter<AppTileAdapter.TileViewHolder>() {

    private val items = mutableListOf<AppInfo>()

    fun submitList(list: List<AppInfo>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TileViewHolder {
        val binding = ItemAppTileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TileViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size

    inner class TileViewHolder(private val binding: ItemAppTileBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AppInfo) {
            val pm = binding.root.context.packageManager
            binding.appName.text = item.label
            runCatching {
                val icon = pm.getApplicationIcon(item.packageName)
                binding.appIcon.setImageDrawable(icon)
            }
            binding.root.setOnClickListener { onClick(item) }
            binding.root.setOnLongClickListener {
                onLongClick?.invoke(item)
                true
            }
        }
    }
}
