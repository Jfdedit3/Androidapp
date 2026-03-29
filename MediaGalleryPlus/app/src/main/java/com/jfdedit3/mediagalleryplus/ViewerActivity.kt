package com.jfdedit3.mediagalleryplus

import android.app.RecoverableSecurityException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.jfdedit3.mediagalleryplus.databinding.ActivityViewerBinding

class ViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewerBinding
    private var player: ExoPlayer? = null
    private var currentUri: Uri? = null
    private var currentType: MediaType = MediaType.IMAGE

    private val deleteLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, getString(R.string.deleted_success), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

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
        binding.deleteButton.setOnClickListener { deleteCurrentMedia() }

        currentUri?.let { uri ->
            when (currentType) {
                MediaType.IMAGE -> showImage(uri)
                MediaType.VIDEO, MediaType.AUDIO -> showPlayer(uri, currentType == MediaType.AUDIO)
            }
        }
    }

    private fun showImage(uri: Uri) {
        binding.imageView.visibility = View.VISIBLE
        binding.playerView.visibility = View.GONE
        binding.audioHint.visibility = View.GONE

        Glide.with(this)
            .load(uri)
            .into(binding.imageView)
    }

    private fun showPlayer(uri: Uri, audioOnly: Boolean) {
        binding.imageView.visibility = View.GONE
        binding.playerView.visibility = View.VISIBLE
        binding.audioHint.visibility = if (audioOnly) View.VISIBLE else View.GONE

        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            binding.playerView.player = exoPlayer
            exoPlayer.setMediaItem(MediaItem.fromUri(uri))
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
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

    private fun deleteCurrentMedia() {
        val uri = currentUri ?: return

        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val intentSender = MediaStore.createDeleteRequest(contentResolver, listOf(uri)).intentSender
                deleteLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            } else {
                val deleted = contentResolver.delete(uri, null, null)
                if (deleted > 0) {
                    Toast.makeText(this, getString(R.string.deleted_success), Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, getString(R.string.delete_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }.recoverCatching { error ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && error is RecoverableSecurityException) {
                deleteLauncher.launch(
                    IntentSenderRequest.Builder(error.userAction.actionIntent.intentSender).build()
                )
            } else {
                throw error
            }
        }.onFailure {
            Toast.makeText(this, it.message ?: getString(R.string.delete_failed), Toast.LENGTH_LONG).show()
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
