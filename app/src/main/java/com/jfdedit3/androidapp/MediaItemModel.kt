package com.jfdedit3.androidapp

import android.net.Uri

data class MediaItemModel(
    val id: Long,
    val uri: Uri,
    val name: String,
    val type: MediaType,
    val dateAddedSeconds: Long
)

enum class MediaType {
    IMAGE,
    VIDEO
}
