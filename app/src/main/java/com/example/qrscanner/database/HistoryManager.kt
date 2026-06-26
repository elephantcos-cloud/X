package com.example.qrscanner.database

import android.content.Context
import android.content.SharedPreferences
import com.example.qrscanner.model.ScanItem
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ScanPrefs", Context.MODE_PRIVATE)

    fun saveScan(value: String, type: String) {
        val list = getAllHistory().toMutableList()
        val timestamp = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        val newItem = ScanItem(System.currentTimeMillis(), value, type, timestamp)
        list.add(0, newItem)
        
        val jsonArray = JSONArray()
        list.take(50).forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("value", it.rawValue)
            obj.put("type", it.type)
            obj.put("time", it.timestamp)
            jsonArray.put(obj)
        }
        prefs.edit().putString("history_data", jsonArray.toString()).apply()
    }

    fun getAllHistory(): List<ScanItem> {
        val data = prefs.getString("history_data", null) ?: return emptyList()
        val list = mutableListOf<ScanItem>()
        try {
            val array = JSONArray(data)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ScanItem(
                        obj.getLong("id"),
                        obj.getString("value"),
                        obj.getString("type"),
                        obj.getString("time")
                    )
                )
            }
        } catch (e: Exception) { e.printStackTrace() }
        return list
    }
}
