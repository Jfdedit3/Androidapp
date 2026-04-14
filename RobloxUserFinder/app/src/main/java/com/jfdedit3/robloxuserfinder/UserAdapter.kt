package com.jfdedit3.robloxuserfinder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jfdedit3.robloxuserfinder.databinding.ItemUserBinding

class UserAdapter : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private val items = mutableListOf<RobloxUser>()

    fun submitList(list: List<RobloxUser>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size

    inner class UserViewHolder(private val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RobloxUser) {
            binding.usernameText.text = item.username
            binding.displayNameText.text = item.displayName
            binding.idText.text = "ID: ${item.id}"
            Glide.with(binding.avatarImage.context)
                .load(item.avatarUrl)
                .placeholder(android.R.drawable.sym_def_app_icon)
                .into(binding.avatarImage)
        }
    }
}
