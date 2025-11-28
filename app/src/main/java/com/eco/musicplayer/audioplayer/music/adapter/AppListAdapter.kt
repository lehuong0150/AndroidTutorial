package com.eco.musicplayer.audioplayer.music.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.eco.musicplayer.audioplayer.music.databinding.ItemAppSelectBinding
import com.eco.musicplayer.audioplayer.music.models.overlay.AppInfo

class AppListAdapter(
    private val apps: List<AppInfo>,
    private val onItemClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppListAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemAppSelectBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(appInfo: AppInfo) {
            binding.tvAppName.text = appInfo.appName
            binding.tvPackageName.text = appInfo.packageName

            binding.root.setOnClickListener {
                onItemClick(appInfo)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppSelectBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(apps[position])
    }

    override fun getItemCount() = apps.size
}
