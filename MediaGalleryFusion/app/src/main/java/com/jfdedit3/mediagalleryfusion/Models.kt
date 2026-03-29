package com.jfdedit3.mediagalleryfusion

import android.net.Uri

data class MediaItemModel(
    val id: Long,
    val uri: Uri,
    val name: String,
    val type: MediaType,
    val dateAddedSeconds: Long,
    val sizeBytes: Long,
    val durationMs: Long,
    val bucketName: String,
    val relativePath: String
)

enum class MediaType { IMAGE, VIDEO, AUDIO }
enum class MediaTab { ALL, IMAGE, VIDEO, AUDIO }
enum class SortMode { NAME, DATE, SIZE, DURATION, TYPE }
enum class SortOrder { ASC, DESC }
enum class ViewMode { GRID, LIST, COMPACT }
enum class QuickFilter { NONE, FAVORITES, RECENTS, LARGE_FILES, SCREENSHOTS, DOWNLOADS, TRASH, HIDDEN }
