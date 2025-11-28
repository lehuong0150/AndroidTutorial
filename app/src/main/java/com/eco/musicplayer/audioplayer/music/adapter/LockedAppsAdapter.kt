package com.eco.musicplayer.audioplayer.music.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.eco.musicplayer.audioplayer.music.databinding.ItemLockedAppBinding
import com.eco.musicplayer.audioplayer.music.models.overlay.AppInfo

class LockedAppsAdapter(
    private val apps: List<AppInfo>,
    private val onItemClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<LockedAppsAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemLockedAppBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(appInfo: AppInfo) {
            binding.tvAppName.text = appInfo.appName
            binding.tvPackageName.text = appInfo.packageName
            binding.switchLock.isChecked = appInfo.isLocked

            binding.root.setOnClickListener {
                onItemClick(appInfo)
            }

            binding.switchLock.setOnClickListener {
                onItemClick(appInfo)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLockedAppBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(apps[position])
    }

    override fun getItemCount() = apps.size
}
