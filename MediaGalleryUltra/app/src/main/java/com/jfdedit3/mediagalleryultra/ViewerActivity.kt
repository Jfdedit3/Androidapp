package com.jfdedit3.mediagalleryultra

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
import com.jfdedit3.mediagalleryultra.databinding.ActivityViewerBinding

class ViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewerBinding
    private var player: ExoPlayer? = null
    private var currentUri: Uri? = null
    private var currentType: MediaType = MediaType.IMAGE
    private var playbackPosition: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUri = intent.getStringExtra(EXTRA_URI)?.let(Uri::parse)
        currentType = runCatching {
            MediaType.valueOf(intent.getStringExtra(EXTRA_TYPE).orEmpty())
        }.getOrDefault(MediaType.IMAGE)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra(EXTRA_NAME).orEmpty()
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.shareButton.setOnClickListener { shareCurrentMedia() }
        binding.wallpaperButton.setOnClickListener { setAsWallpaper() }

        binding.wallpaperButton.visibility = if (currentType == MediaType.IMAGE) View.VISIBLE else View.GONE
    }

    override fun onStart() {
        super.onStart()
        currentUri?.let { uri ->
            when (currentType) {
                MediaType.IMAGE -> showImage(uri)
                MediaType.VIDEO, MediaType.AUDIO -> showPlayer(uri, currentType == MediaType.AUDIO)
            }
        }
    }

    private fun showImage(uri: Uri) {
        releasePlayer()
        binding.imageView.visibility = View.VISIBLE
        binding.playerView.visibility = View.GONE
        binding.audioHint.visibility = View.GONE

        Glide.with(this).load(uri).into(binding.imageView)
    }

    private fun showPlayer(uri: Uri, audioOnly: Boolean) {
        binding.imageView.visibility = View.GONE
        binding.playerView.visibility = View.VISIBLE
        binding.audioHint.visibility = if (audioOnly) View.VISIBLE else View.GONE

        if (player == null) {
            player = ExoPlayer.Builder(this).build()
            binding.playerView.player = player
        }

        player?.apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            seekTo(playbackPosition)
            playWhenReady = true
        }
    }

    private fun releasePlayer() {
        player?.let {
            playbackPosition = it.currentPosition
            it.release()
        }
        player = null
        binding.playerView.player = null
    }

    private fun shareCurrentMedia() {
        val uri = currentUri ?: return
        val mimeType = when (currentType) {
            MediaType.IMAGE -> "image/*"
            MediaType.VIDEO -> "video/*"
            MediaType.AUDIO -> "audio/*"
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_media)))
    }

    private fun setAsWallpaper() {
        val uri = currentUri ?: return
        if (currentType != MediaType.IMAGE) return

        runCatching {
            contentResolver.openInputStream(uri)?.use { input ->
                val bitmap = BitmapFactory.decodeStream(input)
                WallpaperManager.getInstance(this).setBitmap(bitmap)
            }
            Toast.makeText(this, getString(R.string.wallpaper_success), Toast.LENGTH_SHORT).show()
        }.onFailure {
            Toast.makeText(this, it.message ?: getString(R.string.wallpaper_failed), Toast.LENGTH_LONG).show()
        }
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    companion object {
        const val EXTRA_URI = "extra_uri"
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_TYPE = "extra_type"
    }
}