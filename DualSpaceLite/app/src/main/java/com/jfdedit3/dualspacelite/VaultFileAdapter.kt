package com.jfdedit3.dualspacelite

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jfdedit3.dualspacelite.databinding.ItemVaultFileBinding
import java.io.File

class VaultFileAdapter(
    private val onOpen: (File) -> Unit,
    private val onDelete: (File) -> Unit
) : RecyclerView.Adapter<VaultFileAdapter.VaultViewHolder>() {

    private val items = mutableListOf<File>()

    fun submitList(files: List<File>) {
        items.clear()
        items.addAll(files)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VaultViewHolder {
        val binding = ItemVaultFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VaultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VaultViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size

    inner class VaultViewHolder(private val binding: ItemVaultFileBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(file: File) {
            binding.fileName.text = file.name
            binding.openButton.setOnClickListener { onOpen(file) }
            binding.deleteButton.setOnClickListener { onDelete(file) }
        }
    }
}
