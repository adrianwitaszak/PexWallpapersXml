package com.adwi.pexwallpapers.shared.adapter

import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.adwi.pexwallpapers.data.local.entity.Wallpaper
import com.adwi.pexwallpapers.databinding.WallpaperItemBinding

class WallpaperViewHolder(
    private val binding: WallpaperItemBinding,
    private val onItemClick: (Int) -> Unit,
    private val onFavoriteClick: (Int) -> Unit,
    private val onShareClick: (Int) -> Unit,
    private val onPexelsLogoClick: (Int) -> Unit,
    private val requireActivity: FragmentActivity
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(wallpaper: Wallpaper) {
        binding.wallpaper = wallpaper
        binding.executePendingBindings()
    }

    init {
        binding.apply {
            wallpaperImageView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(position)
                }
            }
            wallpaperImageView.setOnLongClickListener {
                Toast.makeText(
                    requireActivity,
                    "Wallpaper: ${wallpaper!!.photographer}",
                    Toast.LENGTH_SHORT
                ).show()
                true

            }
//            pexelsLogo.setOnClickListener {
//                val position = bindingAdapterPosition
//                if (position != RecyclerView.NO_POSITION) {
//                    onPexelsLogoClick(position)
//                }
//            }
//            favoritesBookmark.setOnClickListener {
//                val position = bindingAdapterPosition
//                if (position != RecyclerView.NO_POSITION) {
//                    onFavoriteClick(position)
//                }
//            }
//            shareButton.setOnClickListener {
//                val position = bindingAdapterPosition
//                if (position != RecyclerView.NO_POSITION) {
//                    onShareClick(position)
//                }
//            }
        }
    }
}