package com.jfdedit3.mediawallpapergallery

import android.content.Context
import android.net.Uri
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class VideoWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = VideoWallpaperEngine()

    inner class VideoWallpaperEngine : Engine() {
        private var player: ExoPlayer? = null

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            startPlayback(holder)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            player?.playWhenReady = visible
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            player?.clearVideoSurfaceHolder(holder)
            player?.release()
            player = null
            super.onSurfaceDestroyed(holder)
        }

        override fun onDestroy() {
            player?.release()
            player = null
            super.onDestroy()
        }

        private fun startPlayback(holder: SurfaceHolder) {
            val uriString = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_URI, null) ?: return
            val uri = Uri.parse(uriString)
            player?.release()
            player = ExoPlayer.Builder(this@VideoWallpaperService).build().also { exoPlayer ->
                exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
                exoPlayer.volume = 0f
                exoPlayer.setMediaItem(MediaItem.fromUri(uri))
                exoPlayer.setVideoSurfaceHolder(holder)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
            }
        }
    }

    companion object {
        private const val PREFS_NAME = "video_wallpaper_prefs"
        private const val KEY_URI = "video_uri"

        fun saveVideoUri(context: Context, uri: Uri) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_URI, uri.toString())
                .apply()
        }
    }
}
