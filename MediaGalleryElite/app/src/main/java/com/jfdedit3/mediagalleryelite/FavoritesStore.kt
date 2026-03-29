package com.jfdedit3.mediagalleryelite

import android.content.Context

class FavoritesStore(context: Context) {
    private val prefs = context.getSharedPreferences("elite_favorites", Context.MODE_PRIVATE)

    fun isFavorite(id: Long): Boolean = prefs.getBoolean(id.toString(), false)

    fun toggleFavorite(id: Long): Boolean {
        val newValue = !isFavorite(id)
        prefs.edit().putBoolean(id.toString(), newValue).apply()
        return newValue
    }

    fun favoriteIds(): Set<Long> = prefs.all.filterValues { it == true }.keys.mapNotNull { it.toLongOrNull() }.toSet()
}
