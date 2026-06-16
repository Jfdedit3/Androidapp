package com.jfdedit3.mediagalleryultra

import android.content.ContentUris
import android.content.Context
import android.content.SharedPreferences
import android.provider.MediaStore

class MediaStoreRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("media_trash", Context.MODE_PRIVATE)

    private fun getTrashSet(): MutableSet<String> {
        return prefs.getStringSet("trash", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
    }

    private fun saveTrashSet(set: Set<String>) {
        prefs.edit().putStringSet("trash", set).apply()
    }

    fun moveToTrash(items: List<MediaItemModel>) {
        val set = getTrashSet()
        items.forEach { set.add(it.uri.toString()) }
        saveTrashSet(set)
    }

    fun isTrashed(uri: String): Boolean {
        return getTrashSet().contains(uri)
    }

    fun restoreFromTrash(uri: String) {
        val set = getTrashSet()
        set.remove(uri)
        saveTrashSet(set)
    }

    fun getTrash(): List<String> = getTrashSet().toList()

    fun loadMedia(): List<MediaItemModel> {
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATE_ADDED
        )

        val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE}=?"

        val selectionArgs = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO.toString()
        )

        val trash = getTrashSet()

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

                val uri = ContentUris.withAppendedId(baseUri, id)

                if (trash.contains(uri.toString())) continue

                items.add(
                    MediaItemModel(
                        id = id,
                        uri = uri,
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