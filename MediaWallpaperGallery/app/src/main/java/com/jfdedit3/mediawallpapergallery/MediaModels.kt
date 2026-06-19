package com.jfdedit3.mediawallpapergallery

import android.net.Uri

enum class MediaType {
    IMAGE,
    VIDEO
}

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val name: String,
    val type: MediaType,
    val mimeType: String,
    val dateAddedSeconds: Long
)
