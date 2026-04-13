package com.jfdedit3.dualspacelite

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

class AppRegistry(private val context: Context) {
    private val prefs = context.getSharedPreferences("dual_space_apps", Context.MODE_PRIVATE)

    fun loadInstalledApps(): List<AppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        return pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .map {
                AppInfo(
                    packageName = it.activityInfo.packageName,
                    label = it.loadLabel(pm).toString()
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    fun pinnedPackages(): Set<String> = prefs.getStringSet("pinned", emptySet()) ?: emptySet()

    fun setPinnedPackages(packages: Set<String>) {
        prefs.edit().putStringSet("pinned", packages).apply()
    }

    fun loadPinnedApps(): List<AppInfo> {
        val pinned = pinnedPackages()
        return loadInstalledApps().filter { it.packageName in pinned }
    }

    fun launch(packageName: String): Boolean {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launchIntent)
        return true
    }
}
