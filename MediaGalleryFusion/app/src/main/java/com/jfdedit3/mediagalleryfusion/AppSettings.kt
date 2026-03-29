package com.jfdedit3.mediagalleryfusion

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class AppSettings(context: Context) {
    private val prefs = context.getSharedPreferences("fusion_settings", Context.MODE_PRIVATE)

    var themeMode: String
        get() = prefs.getString("theme_mode", "auto") ?: "auto"
        set(value) = prefs.edit().putString("theme_mode", value).apply()

    var accentColor: String
        get() = prefs.getString("accent_color", "blue") ?: "blue"
        set(value) = prefs.edit().putString("accent_color", value).apply()

    var portraitColumns: Int
        get() = prefs.getInt("portrait_columns", 3)
        set(value) = prefs.edit().putInt("portrait_columns", value.coerceIn(2, 6)).apply()

    var landscapeColumns: Int
        get() = prefs.getInt("landscape_columns", 5)
        set(value) = prefs.edit().putInt("landscape_columns", value.coerceIn(3, 8)).apply()

    var autoPlayMedia: Boolean
        get() = prefs.getBoolean("auto_play_media", true)
        set(value) = prefs.edit().putBoolean("auto_play_media", value).apply()

    var showFileNames: Boolean
        get() = prefs.getBoolean("show_file_names", true)
        set(value) = prefs.edit().putBoolean("show_file_names", value).apply()

    var enableAnimations: Boolean
        get() = prefs.getBoolean("enable_animations", true)
        set(value) = prefs.edit().putBoolean("enable_animations", value).apply()

    var confirmDeletion: Boolean
        get() = prefs.getBoolean("confirm_deletion", true)
        set(value) = prefs.edit().putBoolean("confirm_deletion", value).apply()

    var sortMode: SortMode
        get() = SortMode.valueOf(prefs.getString("sort_mode", SortMode.DATE.name) ?: SortMode.DATE.name)
        set(value) = prefs.edit().putString("sort_mode", value.name).apply()

    var sortOrder: SortOrder
        get() = SortOrder.valueOf(prefs.getString("sort_order", SortOrder.DESC.name) ?: SortOrder.DESC.name)
        set(value) = prefs.edit().putString("sort_order", value.name).apply()

    var viewMode: ViewMode
        get() = ViewMode.valueOf(prefs.getString("view_mode", ViewMode.GRID.name) ?: ViewMode.GRID.name)
        set(value) = prefs.edit().putString("view_mode", value.name).apply()

    fun applyTheme() {
        val mode = when (themeMode) {
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}
