package com.jfdedit3.mediagalleryelite

import android.net.Uri

data class MediaItemModel(
    val id: Long,
    val uri: Uri,
    val name: String,
    val type: MediaType,
    val dateAddedSeconds: Long,
    val sizeBytes: Long,
    val durationMs: Long,
    val folderName: String,
    val albumName: String
)

enum class MediaType {
    IMAGE,
    VIDEO,
    AUDIO
}

enum class MediaTab {
    ALL,
    IMAGE,
    VIDEO,
    AUDIO,
    FAVORITES,
    FOLDERS,
    ALBUMS
}

enum class SortMode {
    DATE_DESC,
    DATE_ASC,
    NAME_ASC,
    NAME_DESC,
    SIZE_DESC,
    TYPE_ASC
}
