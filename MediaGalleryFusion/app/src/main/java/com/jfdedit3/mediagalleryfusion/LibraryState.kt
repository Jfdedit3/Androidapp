package com.jfdedit3.mediagalleryfusion

import android.content.Context

class LibraryState(context: Context) {
    private val prefs = context.getSharedPreferences("fusion_library_state", Context.MODE_PRIVATE)

    private fun getLongSet(key: String): MutableSet<String> = prefs.getStringSet(key, emptySet())?.toMutableSet() ?: mutableSetOf()

    fun isFavorite(id: Long): Boolean = getLongSet("favorites").contains(id.toString())
    fun toggleFavorite(id: Long) = toggleSetValue("favorites", id)
    fun isHidden(id: Long): Boolean = getLongSet("hidden").contains(id.toString())
    fun toggleHidden(id: Long) = toggleSetValue("hidden", id)
    fun isTrashed(id: Long): Boolean = getLongSet("trash").contains(id.toString())
    fun addToTrash(ids: List<Long>) = setMany("trash", ids, true)
    fun restoreFromTrash(ids: List<Long>) = setMany("trash", ids, false)

    fun getSearchHistory(): List<String> = prefs.getStringSet("search_history", emptySet())?.toList()?.sortedDescending() ?: emptyList()
    fun pushSearch(query: String) {
        if (query.isBlank()) return
        val current = prefs.getStringSet("search_history", emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(query.trim())
        prefs.edit().putStringSet("search_history", current.takeLast(15).toSet()).apply()
    }

    private fun toggleSetValue(key: String, id: Long) {
        val set = getLongSet(key)
        val value = id.toString()
        if (set.contains(value)) set.remove(value) else set.add(value)
        prefs.edit().putStringSet(key, set).apply()
    }

    private fun setMany(key: String, ids: List<Long>, add: Boolean) {
        val set = getLongSet(key)
        ids.map(Long::toString).forEach {
            if (add) set.add(it) else set.remove(it)
        }
        prefs.edit().putStringSet(key, set).apply()
    }
}
