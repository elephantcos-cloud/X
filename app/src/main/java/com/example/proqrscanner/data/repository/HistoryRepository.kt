package com.example.proqrscanner.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.proqrscanner.domain.model.ScanItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("scan_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val maxItems = 50

    fun addScan(value: String, type: String) {
        val list = getAllScans().toMutableList()
        val timestamp = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        val newItem = ScanItem(System.currentTimeMillis(), value, type, timestamp)
        list.add(0, newItem)

        val json = gson.toJson(list.take(maxItems))
        prefs.edit().putString("history", json).apply()
    }

    fun getAllScans(): List<ScanItem> {
        val json = prefs.getString("history", null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<ScanItem>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearHistory() {
        prefs.edit().remove("history").apply()
    }
}
