package com.example.ui.printer

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.ArrayList

/**
 * Jeu d'instructions ESC/POS pour imprimantes thermiques Bluetooth et terminaux intégrés (POS / Caisse).
 * Supporte le standard ESC/POS 58mm (terminaux mobiles) et 80mm (caisses de comptoir / tablettes).
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
object EscPosPrinterUtils {

    const val ESC: Byte = 27
    const val FS: Byte = 28
    const val GS: Byte = 29
    const val LF: Byte = 10
    const val SP: Byte = 32

    fun initPrinter(): ByteArray {
        return byteArrayOf(ESC, 64)
    }

    fun printLineFeed(): ByteArray {
        return byteArrayOf(LF)
    }

    fun printAndFeedLines(n: Byte): ByteArray {
        return byteArrayOf(ESC, 100, n)
    }

    fun alignLeft(): ByteArray {
        return byteArrayOf(ESC, 97, 0)
    }

    fun alignCenter(): ByteArray {
        return byteArrayOf(ESC, 97, 1)
    }

    fun alignRight(): ByteArray {
        return byteArrayOf(ESC, 97, 2)
    }

    fun emphasizedOn(): ByteArray {
        return byteArrayOf(ESC, 69, 0x0F)
    }

    fun emphasizedOff(): ByteArray {
        return byteArrayOf(ESC, 69, 0)
    }

    fun doubleHeightWidthOn(): ByteArray {
        return byteArrayOf(ESC, 33, 56)
    }

    fun doubleHeightWidthOff(): ByteArray {
        return byteArrayOf(ESC, 33, 0)
    }

    fun fontSizeSetBig(num: Int): ByteArray {
        val realSize: Byte = when (num) {
            1 -> 17 // 0x11
            2 -> 34 // 0x22
            3 -> 51 // 0x33
            else -> 0
        }
        return byteArrayOf(GS, 33, realSize)
    }

    fun feedPaperCut(): ByteArray {
        return byteArrayOf(GS, 86, 65, 0)
    }

    fun feedPaperCutPartial(): ByteArray {
        return byteArrayOf(GS, 86, 66, 0)
    }

    /**
     * Décode une image Bitmap en commandes ESC/POS matricielles (GS v 0).
     */
    fun decodeBitmap(image: Bitmap?, maxWidth: Int): ByteArray {
        if (image == null || image.width <= 0 || image.height <= 0) return byteArrayOf()

        // Redimensionnement au besoin
        val scaledBitmap: Bitmap = if (image.width > maxWidth) {
            val scale = maxWidth.toFloat() / image.width.toFloat()
            val matrix = Matrix().apply { postScale(scale, scale) }
            Bitmap.createBitmap(image, 0, 0, image.width, image.height, matrix, true)
        } else {
            image
        }

        val width = scaledBitmap.width
        val height = scaledBitmap.height

        val widthBytes = if (width % 8 == 0) width / 8 else width / 8 + 1
        val xL = (widthBytes % 256).toByte()
        val xH = (widthBytes / 256).toByte()
        val yL = (height % 256).toByte()
        val yH = (height / 256).toByte()

        val bos = ByteArrayOutputStream()
        // GS v 0 m xL xH yL yH
        bos.write(byteArrayOf(GS, 118, 48, 0, xL, xH, yL, yH))

        for (y in 0 until height) {
            var bitIndex = 7
            var currentByte = 0
            for (x in 0 until width) {
                val pixel = scaledBitmap.getPixel(x, y)
                val red = Color.red(pixel)
                val green = Color.green(pixel)
                val blue = Color.blue(pixel)
                val alpha = Color.alpha(pixel)

                // Si pixel foncé -> 1 (point imprimé), sinon blanc -> 0
                val isBlack = if (alpha < 128) false else (red * 0.299 + green * 0.587 + blue * 0.114) < 165
                if (isBlack) {
                    currentByte = currentByte or (1 shl bitIndex)
                }
                bitIndex--
                if (bitIndex < 0) {
                    bos.write(currentByte)
                    bitIndex = 7
                    currentByte = 0
                }
            }
            if (bitIndex != 7) {
                bos.write(currentByte)
            }
        }

        return bos.toByteArray()
    }
}
