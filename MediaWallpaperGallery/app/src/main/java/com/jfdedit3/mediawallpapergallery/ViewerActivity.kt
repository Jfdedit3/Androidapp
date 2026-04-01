package com.jfdedit3.mediawallpapergallery

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.jfdedit3.mediawallpapergallery.databinding.ActivityViewerBinding

class ViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewerBinding
    private var currentUri: Uri? = null
    private var currentType: MediaType = MediaType.IMAGE
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUri = intent.getStringExtra(EXTRA_URI)?.let(Uri::parse)
        currentType = runCatching { MediaType.valueOf(intent.getStringExtra(EXTRA_TYPE).orEmpty()) }.getOrDefault(MediaType.IMAGE)

        binding.toolbar.title = intent.getStringExtra(EXTRA_NAME).orEmpty()
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.shareButton.setOnClickListener { shareCurrentMedia() }
        binding.wallpaperButton.setOnClickListener { setCurrentMediaAsWallpaper() }

        val uri = currentUri ?: return
        if (currentType == MediaType.IMAGE) {
            binding.imageView.visibility = View.VISIBLE
            binding.playerView.visibility = View.GONE
            binding.wallpaperButton.text = getString(R.string.set_image_wallpaper)
            Glide.with(this).load(uri).into(binding.imageView)
        } else {
            binding.imageView.visibility = View.GONE
            binding.playerView.visibility = View.VISIBLE
            binding.wallpaperButton.text = getString(R.string.set_video_wallpaper)
            player = ExoPlayer.Builder(this).build().also { exoPlayer ->
                binding.playerView.player = exoPlayer
                exoPlayer.setMediaItem(MediaItem.fromUri(uri))
                exoPlayer.repeatMode = ExoPlayer.REPEAT_MODE_ONE
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
            }
        }
    }

    private fun shareCurrentMedia() {
        val uri = currentUri ?: return
        val type = if (currentType == MediaType.VIDEO) "video/*" else "image/*"
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            this.type = type
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
    }

    private fun setCurrentMediaAsWallpaper() {
        val uri = currentUri ?: return
        if (currentType == MediaType.IMAGE) {
            runCatching {
                contentResolver.openInputStream(uri)?.use { input ->
                    WallpaperManager.getInstance(this).setStream(input)
                }
                Toast.makeText(this, getString(R.string.wallpaper_image_success), Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(this, getString(R.string.wallpaper_failed), Toast.LENGTH_LONG).show()
            }
            return
        }

        runCatching {
            VideoWallpaperService.saveVideoUri(this, uri)
            val component = ComponentName(this, VideoWallpaperService::class.java)
            startActivity(
                Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    component
                )
            )
        }.recoverCatching {
            startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER))
        }.onSuccess {
            Toast.makeText(this, getString(R.string.wallpaper_video_hint), Toast.LENGTH_LONG).show()
        }.onFailure {
            Toast.makeText(this, getString(R.string.wallpaper_failed), Toast.LENGTH_LONG).show()
        }
    }

    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
        binding.playerView.player = null
    }

    companion object {
        const val EXTRA_URI = "extra_uri"
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_TYPE = "extra_type"
    }
}
