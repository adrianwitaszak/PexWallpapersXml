package com.adwi.pexwallpapers.ui.search

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.adwi.pexwallpapers.data.WallpaperRepository
import com.adwi.pexwallpapers.data.local.entity.Wallpaper
import com.adwi.pexwallpapers.shared.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val repository: WallpaperRepository) :
    BaseViewModel() {

    private val currentQuery = MutableStateFlow<String?>(null)

    val hasCurrentQuery = currentQuery.map { it != null }

    val searchResults = currentQuery.flatMapLatest { query ->
        query?.let {
            repository.getSearchResultsPaged(query)
        } ?: emptyFlow()
    }.cachedIn(viewModelScope)

    var refreshInProgress = false
    var pendingScrollToTopAfterRefresh = false

    var newQueryInProgress = false
    var pendingScrollToTopAfterNewQuery = false

    fun onSearchQuerySubmit(query: String) {
        currentQuery.value = query
        newQueryInProgress = true
        pendingScrollToTopAfterNewQuery = true
    }

    fun onFavoriteClick(wallpaper: Wallpaper) {
        val currentlyFavorite = wallpaper.isFavorite
        val updatedWallpaper = wallpaper.copy(isFavorite = !currentlyFavorite)
        viewModelScope.launch {
            repository.updateWallpaper(updatedWallpaper)
        }
    }
}