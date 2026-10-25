package com.fernando.launcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppAdapter(
    private val onClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppAdapter.VH>() {

    private var all: List<AppInfo> = emptyList()
    private var shown: List<AppInfo> = emptyList()

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.icon)
        val label: TextView = view.findViewById(R.id.label)
    }

    fun submit(apps: List<AppInfo>) {
        all = apps
        shown = apps
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        val q = query.trim().lowercase()
        shown = if (q.isEmpty()) all else all.filter { it.label.lowercase().contains(q) }
        notifyDataSetChanged()
    }

    fun firstOrNull(): AppInfo? = shown.firstOrNull()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val app = shown[position]
        holder.icon.setImageDrawable(app.icon)
        holder.label.text = app.label
        holder.itemView.setOnClickListener { onClick(app) }
    }

    override fun getItemCount() = shown.size
}
