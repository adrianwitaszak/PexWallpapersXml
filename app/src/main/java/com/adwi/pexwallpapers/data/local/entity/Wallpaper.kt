package com.adwi.pexwallpapers.data.local.entity

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
@Entity(tableName = "wallpaper_table")
data class Wallpaper(
    @PrimaryKey val id: Int,
    val width: Int? = null,
    var height: Int,
    val url: String? = null,
    val photographer: String,
    val photographerUrl: String? = null,
    val color: String,
    val imageUrl: String,
    val categoryName: String,
    @Embedded val src: @RawValue Src?,
    var isFavorite: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
    var isFirst: Boolean = false,
    var isLast: Boolean = false
) : Parcelable

@Entity(tableName = "curated_wallpapers")
data class CuratedWallpapers(
    @PrimaryKey val wallpaperId: Int
)

@Entity(tableName = "search_results", primaryKeys = ["searchQuery", "wallpaperId"])
data class SearchResult(
    val searchQuery: String,
    val wallpaperId: Int,
    val queryPosition: Int
)
