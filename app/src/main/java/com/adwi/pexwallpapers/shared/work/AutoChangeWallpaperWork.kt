package com.adwi.pexwallpapers.shared.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result.failure
import androidx.work.ListenableWorker.Result.success
import androidx.work.WorkerParameters
import com.adwi.pexwallpapers.shared.tools.Channel
import com.adwi.pexwallpapers.shared.tools.NotificationTools
import com.adwi.pexwallpapers.shared.tools.WallpaperSetter
import com.github.ajalt.timberkt.Timber
import com.github.ajalt.timberkt.d
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay

const val NOTIFICATION_WORK = "appName_notification_work"
const val TAG = "NewWallpaperWork"
const val WORKER_NEW_WALLPAPER_IMAGE_URL_FULL = "WallpaperUrlFull"
const val WORKER_NEW_WALLPAPER_NOTIFICATION_IMAGE = "WallpaperUrlTiny"
const val WORKER_DELAY = "WallpaperDelay"

@HiltWorker
class AutoChangeWallpaperWork @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val notificationTools: NotificationTools,
    private val wallpaperSetter: WallpaperSetter
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Get arguments
            val wallpaperImage = inputData.getString(WORKER_NEW_WALLPAPER_IMAGE_URL_FULL)
            val notificationImage = inputData.getString(WORKER_NEW_WALLPAPER_NOTIFICATION_IMAGE)
            val duration = inputData.getLong(WORKER_DELAY, 0)
            Timber.tag(TAG).d { "$wallpaperImage" }
            Timber.tag(TAG).d { "$notificationImage" }
            if (wallpaperImage != null && notificationImage != null) {
                // Set wallpaper
                wallpaperSetter.setWallpaperByImagePath(wallpaperImage, setHomeScreen = true)
                delay(duration)
                //Send notification
                val notificationId = "pex_new_wallpaper"
                val id = inputData.getLong(notificationId, 0).toInt()
                notificationTools.sendNotification(
                    id = id,
                    channel = Channel.NEW_WALLPAPER,
                    imageUrl = notificationImage
                )
                Timber.tag(TAG).d { "doWork - success" }
                success()
            } else {
                Timber.tag(TAG).d { "wallpaper or notificationImage is null" }
                failure()
            }
            success()
        } catch (ex: Exception) {
            Timber.tag(TAG).d { ex.toString() }
            failure()
        }
    }
}