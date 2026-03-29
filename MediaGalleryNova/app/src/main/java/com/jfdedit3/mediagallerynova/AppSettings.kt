package com.jfdedit3.mediagallerynova

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class AppSettings(context: Context) {
    private val prefs = context.getSharedPreferences("nova_settings", Context.MODE_PRIVATE)

    var darkMode: Boolean
        get() = prefs.getBoolean("dark_mode", true)
        set(value) = prefs.edit().putBoolean("dark_mode", value).apply()

    var gridColumns: Int
        get() = prefs.getInt("grid_columns", 3)
        set(value) = prefs.edit().putInt("grid_columns", value.coerceIn(2, 5)).apply()

    var autoPlayMedia: Boolean
        get() = prefs.getBoolean("auto_play_media", true)
        set(value) = prefs.edit().putBoolean("auto_play_media", value).apply()

    var showFileNames: Boolean
        get() = prefs.getBoolean("show_file_names", true)
        set(value) = prefs.edit().putBoolean("show_file_names", value).apply()

    fun applyTheme() {
        AppCompatDelegate.setDefaultNightMode(
            if (darkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
