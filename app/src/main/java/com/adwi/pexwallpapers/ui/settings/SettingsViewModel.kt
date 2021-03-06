package com.adwi.pexwallpapers.ui.settings

import com.adwi.pexwallpapers.R
import com.adwi.pexwallpapers.data.local.entity.Settings
import com.adwi.pexwallpapers.data.local.entity.Wallpaper
import com.adwi.pexwallpapers.data.repository.FavoritesRepository
import com.adwi.pexwallpapers.data.repository.interfaces.SettingsRepositoryInterface
import com.adwi.pexwallpapers.di.IoDispatcher
import com.adwi.pexwallpapers.shared.tools.image.ImageTools
import com.adwi.pexwallpapers.shared.tools.sharing.SharingTools
import com.adwi.pexwallpapers.shared.work.WorkTools
import com.adwi.pexwallpapers.ui.base.BaseViewModel
import com.adwi.pexwallpapers.util.Constants.Companion.WORK_AUTO_WALLPAPER
import com.adwi.pexwallpapers.util.onDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepositoryInterface,
    private val favoritesRepository: FavoritesRepository,
    private val sharingTools: SharingTools,
    private val workTools: WorkTools,
    private val imageTools: ImageTools,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : BaseViewModel() {

     val favorites = MutableStateFlow<List<Wallpaper>>(emptyList())

    val settings = repository.getSettings()

    init {
        getFavorites()
    }

    private fun getFavorites() {
        onDispatcher(ioDispatcher) {
            favorites.value = favoritesRepository.getAllFavorites().first()
        }
    }

    fun updateNewWallpaperSet(checked: Boolean) {
        onDispatcher(ioDispatcher) { repository.updateNewWallpaperSet(checked) }
    }

    fun updateWallpaperRecommendations(checked: Boolean) {
        onDispatcher(ioDispatcher) { repository.updateWallpaperRecommendations(checked) }
    }

    fun updateAutoChangeWallpaper(checked: Boolean) {
        onDispatcher(ioDispatcher) { repository.updateAutoChangeWallpaper(checked) }
    }

    fun updateChangePeriodType(radioButton: Int) {
        onDispatcher(ioDispatcher) { repository.updateChangePeriodType(radioButton) }
    }

    fun updateChangePeriodValue(periodValue: Float) {
        onDispatcher(ioDispatcher) { repository.updateChangePeriodValue(periodValue) }
    }

    fun updateDownloadOverWiFi(checked: Boolean) {
        onDispatcher(ioDispatcher) { repository.updateDownloadOverWiFi(checked) }
    }

    fun updateAutoHome(checked: Boolean) {
        onDispatcher(ioDispatcher) { repository.updateAutoHome(checked) }
    }

    fun updateAutoLock(checked: Boolean) {
        onDispatcher(ioDispatcher) { repository.updateAutoLock(checked) }
    }

    fun resetSettings() {
        onDispatcher(ioDispatcher) { repository.resetAllSettings() }
    }

    fun contactSupport() {
        sharingTools.contactSupport()
    }

    fun cancelWorks(workTag: String) {
        workTools.cancelWorks(workTag)
        imageTools.deleteAllBackups()
    }

    fun saveSettings(settings: Settings) {
        onDispatcher(ioDispatcher) {
            if (settings.autoChangeWallpaper && favorites.value.isNotEmpty()) {
                workTools.setupAutoChangeWallpaperWorks(
                    favorites = favorites.value,
                    timeUnit = getTimeUnit(settings.selectedButton),
                    timeValue = settings.sliderValue
                )
            } else {
                // Cancel works if not favorites list is empty
                cancelWorks(WORK_AUTO_WALLPAPER)
            }
        }
    }

    private fun getTimeUnit(buttonId: Int): TimeUnit {
        return when (buttonId) {
            R.id.minutes_radio_button -> TimeUnit.MINUTES
            R.id.hours_radio_button -> TimeUnit.HOURS
            else -> TimeUnit.DAYS
        }
    }
}