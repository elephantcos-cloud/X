package com.example.proqrscanner.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proqrscanner.databinding.ItemHistoryBinding
import com.example.proqrscanner.domain.model.ScanItem

class HistoryAdapter(private val items: List<ScanItem>) :
    RecyclerView.Adapter<HistoryAdapter.VH>() {

    class VH(val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.binding.tvType.text = item.type
        holder.binding.tvRawValue.text = item.rawValue
        holder.binding.tvTimestamp.text = item.timestamp
    }

    override fun getItemCount() = items.size
}
