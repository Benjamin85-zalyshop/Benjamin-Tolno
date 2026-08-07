package com.example.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

object ImageUtils {

    /**
     * Center-crops a bitmap into a 1:1 square, scales it to [targetSize]x[targetSize],
     * compresses it as JPEG, and returns a Base64 string.
     */
    fun processAndEncodePhoto(bitmap: Bitmap, targetSize: Int = 512): String? {
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val squareSize = Math.min(width, height)
            val x = (width - squareSize) / 2
            val y = (height - squareSize) / 2
            val croppedBitmap = Bitmap.createBitmap(bitmap, x, y, squareSize, squareSize)

            val scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, targetSize, targetSize, true)

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Reads a bitmap from Uri, center-crops and compresses it to Base64.
     */
    fun processAndEncodeUri(context: Context, uri: Uri, targetSize: Int = 512): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bytes = inputStream.readBytes()
            inputStream.close()
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
            processAndEncodePhoto(bitmap, targetSize)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
