package com.jfdedit3.mediagalleryultra

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore

class MediaStoreRepository(private val context: Context) {
    fun loadMedia(): List<MediaItemModel> {
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATE_ADDED
        )

        val selection = buildString {
            append("${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR ")
            append("${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR ")
            append("${MediaStore.Files.FileColumns.MEDIA_TYPE}=?")
        }

        val selectionArgs = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO.toString()
        )

        val items = mutableListOf<MediaItemModel>()
        val collection = MediaStore.Files.getContentUri("external")

        context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val mediaTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val mediaType = cursor.getInt(mediaTypeColumn)
                val name = cursor.getString(nameColumn) ?: "Untitled"
                val dateAdded = cursor.getLong(dateAddedColumn)

                val type = when (mediaType) {
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> MediaType.VIDEO
                    MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO -> MediaType.AUDIO
                    else -> MediaType.IMAGE
                }

                val baseUri = when (type) {
                    MediaType.IMAGE -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    MediaType.VIDEO -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    MediaType.AUDIO -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                items.add(
                    MediaItemModel(
                        id = id,
                        uri = ContentUris.withAppendedId(baseUri, id),
                        name = name,
                        type = type,
                        dateAddedSeconds = dateAdded
                    )
                )
            }
        }

        return items
    }
}
