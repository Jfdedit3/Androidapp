package com.jfdedit3.mediawallpapergallery

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore

class MediaRepository(private val context: Context) {

    fun loadMedia(): List<MediaItem> {
        val collection = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.DATE_ADDED
        )
        val selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=? OR " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
        val selectionArgs = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
        )
        val sortOrder = MediaStore.MediaColumns.DATE_ADDED + " DESC"

        val items = mutableListOf<MediaItem>()
        context.contentResolver.query(collection, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val typeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val mediaTypeValue = cursor.getInt(typeColumn)
                val name = cursor.getString(nameColumn) ?: "Unnamed"
                val mimeType = cursor.getString(mimeTypeColumn) ?: "application/octet-stream"
                val dateAdded = cursor.getLong(dateAddedColumn)

                val mediaType = if (mediaTypeValue == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) MediaType.VIDEO else MediaType.IMAGE
                val baseUri = if (mediaType == MediaType.IMAGE) {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }

                items += MediaItem(
                    id = id,
                    uri = ContentUris.withAppendedId(baseUri, id),
                    name = name,
                    type = mediaType,
                    mimeType = mimeType,
                    dateAddedSeconds = dateAdded
                )
            }
        }
        return items
    }
}
