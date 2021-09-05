package com.adwi.pexwallpapers.shared.tools

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Environment
import android.os.SystemClock
import android.provider.MediaStore
import androidx.core.content.ContextCompat.startActivity
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import dagger.hilt.android.qualifiers.ActivityContext
import java.io.File
import java.io.File.separator
import java.io.FileOutputStream
import java.io.OutputStream
import javax.inject.Inject

class SharingTools @Inject constructor(
    @ActivityContext private val context: Context
) {

    fun openUrlInBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            data = Uri.parse(url)
        }
        startActivity(context, intent, null)
    }

    fun contactSupport() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            data = Uri.parse("mailto:adrianwitaszak@gmial.com")
            putExtra(Intent.EXTRA_SUBJECT, "Support request Nr. 12345678")
        }
        startActivity(context, Intent.createChooser(intent, "Choose an app"), null)
    }

    suspend fun shareImage(imageUrl: String, photographer: String) {
        val uri = saveImageToInternalStorage(imageUrl)
        val intent = Intent(Intent.ACTION_SEND).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(uri, "image/*")
            putExtra(Intent.EXTRA_SUBJECT, "Picture by $photographer")
            putExtra(Intent.EXTRA_TITLE, "Picture by PexWallpapers")
            putExtra(Intent.EXTRA_STREAM, uri)
        }
        // TODO() sort out startActivity FLAG_ACTIVITY_NEW_TASK not working
        startActivity(
            context,
            intent,
//            Intent.createChooser(intent, "Choose an app"),
            null
        )
    }

    suspend fun saveImageLocally(imageUrl: String, photographer: String) {
        val bitmap = getBitmap(imageUrl)
        MediaStore.Images.Media.insertImage(
            context.contentResolver,
            bitmap,
            photographer,
            "Image of $photographer"
        )
    }

    private suspend fun saveImageToInternalStorage(url: String): Uri? {
        val bitmap = getBitmap(url)
        return bitmap.saveImage(context)
    }

    suspend fun getBitmap(url: String): Bitmap {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false) // Disable hardware bitmaps.
            .build()

        val result = (loader.execute(request) as SuccessResult).drawable
        return (result as BitmapDrawable).bitmap
    }
}

fun Bitmap.saveImage(context: Context): Uri? {
    if (android.os.Build.VERSION.SDK_INT >= 29) {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/test_pictures")
        values.put(MediaStore.Images.Media.IS_PENDING, true)
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "img_${SystemClock.uptimeMillis()}")

        val uri: Uri? =
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
            saveImageToStream(this, context.contentResolver.openOutputStream(uri))
            values.put(MediaStore.Images.Media.IS_PENDING, false)
            context.contentResolver.update(uri, values, null, null)
            return uri
        }
    } else {
        val directory =
            File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    .toString() + separator + "test_pictures"
            )
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val fileName = "img_${SystemClock.uptimeMillis()}" + ".jpeg"
        val file = File(directory, fileName)
        saveImageToStream(this, FileOutputStream(file))
        if (file.absolutePath != null) {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.DATA, file.absolutePath)
            // .DATA is deprecated in API 29
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            return Uri.fromFile(file)
        }
    }
    return null
}


fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
    if (outputStream != null) {
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}