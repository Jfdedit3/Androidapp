package com.jfdedit3.dualspacelite

import android.content.Context

class PinManager(context: Context) {
    private val prefs = context.getSharedPreferences("dual_space_pin", Context.MODE_PRIVATE)

    fun hasPin(): Boolean = prefs.contains("pin")
    fun savePin(pin: String) = prefs.edit().putString("pin", pin).apply()
    fun verifyPin(pin: String): Boolean = prefs.getString("pin", "") == pin
    fun clearPin() = prefs.edit().remove("pin").apply()
}
