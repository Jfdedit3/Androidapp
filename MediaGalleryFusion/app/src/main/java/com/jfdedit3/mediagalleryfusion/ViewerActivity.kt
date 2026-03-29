package com.jfdedit3.mediagalleryfusion

import android.app.WallpaperManager
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.jfdedit3.mediagalleryfusion.databinding.ActivityViewerBinding

class ViewerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewerBinding
    private lateinit var settings: AppSettings
    private var player: ExoPlayer? = null
    private var currentUri: Uri? = null
    private var currentType: MediaType = MediaType.IMAGE

    override fun onCreate(savedInstanceState: Bundle?) {
        settings = AppSettings(this)
        settings.applyTheme()
        super.onCreate(savedInstanceState)
        binding = ActivityViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUri = intent.getStringExtra(EXTRA_URI)?.let(Uri::parse)
        currentType = runCatching { MediaType.valueOf(intent.getStringExtra(EXTRA_TYPE).orEmpty()) }.getOrDefault(MediaType.IMAGE)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra(EXTRA_NAME).orEmpty()
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.shareButton.setOnClickListener { shareCurrentMedia() }
        binding.wallpaperButton.setOnClickListener { setAsWallpaper() }
        binding.favoriteButton.setOnClickListener { Toast.makeText(this, getString(R.string.viewer_hint), Toast.LENGTH_SHORT).show() }

        currentUri?.let { uri ->
            when (currentType) {
                MediaType.IMAGE -> showImage(uri)
                MediaType.VIDEO, MediaType.AUDIO -> showPlayer(uri, currentType == MediaType.AUDIO)
            }
        }
        binding.wallpaperButton.visibility = if (currentType == MediaType.IMAGE) View.VISIBLE else View.GONE
    }

    private fun showImage(uri: Uri) {
        binding.imageView.visibility = View.VISIBLE
        binding.playerView.visibility = View.GONE
        binding.audioHint.visibility = View.GONE
        Glide.with(this).load(uri).into(binding.imageView)
    }

    private fun showPlayer(uri: Uri, audioOnly: Boolean) {
        binding.imageView.visibility = View.GONE
        binding.playerView.visibility = View.VISIBLE
        binding.audioHint.visibility = if (audioOnly) View.VISIBLE else View.GONE
        player = ExoPlayer.Builder(this).build().also { exo ->
            binding.playerView.player = exo
            exo.setMediaItem(MediaItem.fromUri(uri))
            exo.prepare()
            exo.playWhenReady = settings.autoPlayMedia
        }
    }

    private fun shareCurrentMedia() {
        val uri = currentUri ?: return
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }, getString(R.string.share_media)))
    }

    private fun setAsWallpaper() {
        val uri = currentUri ?: return
        if (currentType != MediaType.IMAGE) return
        runCatching {
            contentResolver.openInputStream(uri)?.use {
                WallpaperManager.getInstance(this).setBitmap(BitmapFactory.decodeStream(it))
            }
            Toast.makeText(this, getString(R.string.wallpaper_success), Toast.LENGTH_SHORT).show()
        }.onFailure {
            Toast.makeText(this, it.message ?: getString(R.string.wallpaper_failed), Toast.LENGTH_LONG).show()
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
