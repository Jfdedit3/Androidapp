package com.jfdedit3.mediagalleryultra.ui.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jfdedit3.mediagalleryultra.MediaItemModel
import com.jfdedit3.mediagalleryultra.MediaType

class FeedAdapter(
    private val items: List<MediaItemModel>
) : RecyclerView.Adapter<FeedAdapter.VH>() {

    inner class VH(val container: FrameLayout) : RecyclerView.ViewHolder(container)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val layout = FrameLayout(parent.context)
        layout.layoutParams = RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.MATCH_PARENT,
            RecyclerView.LayoutParams.MATCH_PARENT
        )
        return VH(layout)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {

        val item = items[position]
        holder.container.removeAllViews()

        if (item.type == MediaType.IMAGE) {

            val img = ImageView(holder.container.context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }

            Glide.with(img).load(item.uri).into(img)
            holder.container.addView(img)

        } else {

            val player = ExoPlayer.Builder(holder.container.context).build()
            val playerView = PlayerView(holder.container.context)

            player.setMediaItem(MediaItem.fromUri(item.uri))
            player.prepare()
            player.playWhenReady = position == 0

            playerView.player = player

            holder.container.addView(playerView)
        }
    }

    override fun getItemCount(): Int = items.size
}
