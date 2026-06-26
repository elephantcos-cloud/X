package com.example.qrscanner.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.qrscanner.databinding.ItemHistoryBinding
import com.example.qrscanner.model.ScanItem

class HistoryAdapter(private val items: List<ScanItem>) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvType.text = item.type
        holder.binding.tvRawValue.text = item.rawValue
        holder.binding.tvTimestamp.text = item.timestamp
    }

    override fun getItemCount(): Int = items.size
}
