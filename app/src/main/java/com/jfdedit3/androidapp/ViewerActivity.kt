package com.jfdedit3.androidapp

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.jfdedit3.androidapp.databinding.ActivityViewerBinding

class ViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewerBinding
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uri = intent.getStringExtra(EXTRA_URI)?.let(Uri::parse)
        val name = intent.getStringExtra(EXTRA_NAME).orEmpty()
        val typeName = intent.getStringExtra(EXTRA_TYPE).orEmpty()
        val type = runCatching { MediaType.valueOf(typeName) }.getOrDefault(MediaType.IMAGE)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = name

        binding.toolbar.setNavigationOnClickListener { finish() }

        if (uri != null) {
            when (type) {
                MediaType.IMAGE -> showImage(uri)
                MediaType.VIDEO -> showVideo(uri)
            }
        }
    }

    private fun showImage(uri: Uri) {
        binding.imageView.visibility = View.VISIBLE
        binding.playerView.visibility = View.GONE

        Glide.with(this)
            .load(uri)
            .into(binding.imageView)
    }

    private fun showVideo(uri: Uri) {
        binding.imageView.visibility = View.GONE
        binding.playerView.visibility = View.VISIBLE

        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            binding.playerView.player = exoPlayer
            exoPlayer.setMediaItem(MediaItem.fromUri(uri))
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    private fun releasePlayer() {
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
