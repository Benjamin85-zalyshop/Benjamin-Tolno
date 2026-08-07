package com.example.ui.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

object QrCodeUtils {

    fun generateQrBitmap(content: String, sizePx: Int = 400): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bmp
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    const val BASE_VERIFY_URL = "https://scolapay-b6289.web.app"

    fun buildStudentQrData(
        studentId: Int,
        remoteId: String,
        name: String,
        grade: String,
        section: String,
        totalFee: String = "",
        paidFee: String = "",
        dueFee: String = "",
        percent: String = "",
        term: String = "",
        avg: String = "",
        rank: String = "",
        size: String = "",
        mention: String = ""
    ): String {
        val matricule = if (remoteId.length >= 5) remoteId.take(5).uppercase() else studentId.toString()
        val safeName = try { java.net.URLEncoder.encode(name, "UTF-8") } catch (e: Exception) { name }
        val safeGrade = try { java.net.URLEncoder.encode(grade, "UTF-8") } catch (e: Exception) { grade }
        val safeSection = try { java.net.URLEncoder.encode(section, "UTF-8") } catch (e: Exception) { section }
        
        var url = "$BASE_VERIFY_URL?id=$studentId&mat=$matricule&name=$safeName&grade=$safeGrade&section=$safeSection"
        if (totalFee.isNotEmpty()) url += "&totalFee=${java.net.URLEncoder.encode(totalFee, "UTF-8")}"
        if (paidFee.isNotEmpty()) url += "&paidFee=${java.net.URLEncoder.encode(paidFee, "UTF-8")}"
        if (dueFee.isNotEmpty()) url += "&dueFee=${java.net.URLEncoder.encode(dueFee, "UTF-8")}"
        if (percent.isNotEmpty()) url += "&percent=${java.net.URLEncoder.encode(percent, "UTF-8")}"
        if (term.isNotEmpty()) url += "&term=${java.net.URLEncoder.encode(term, "UTF-8")}"
        if (avg.isNotEmpty()) url += "&avg=${java.net.URLEncoder.encode(avg, "UTF-8")}"
        if (rank.isNotEmpty()) url += "&rank=${java.net.URLEncoder.encode(rank, "UTF-8")}"
        if (size.isNotEmpty()) url += "&size=${java.net.URLEncoder.encode(size, "UTF-8")}"
        if (mention.isNotEmpty()) url += "&mention=${java.net.URLEncoder.encode(mention, "UTF-8")}"
        
        return url
    }

    fun buildReportCardQrData(
        studentId: Int,
        remoteId: String,
        name: String,
        grade: String,
        section: String,
        term: String,
        average: Float,
        rank: Int,
        classSize: Int,
        mention: String,
        schoolName: String = "",
        schoolYear: String = "",
        totalFee: String = "",
        paidFee: String = "",
        dueFee: String = "",
        percent: String = ""
    ): String {
        val matricule = if (remoteId.length >= 5) remoteId.take(5).uppercase() else studentId.toString()
        val safeName = try { java.net.URLEncoder.encode(name, "UTF-8") } catch (e: Exception) { name }
        val safeGrade = try { java.net.URLEncoder.encode(grade, "UTF-8") } catch (e: Exception) { grade }
        val safeSection = try { java.net.URLEncoder.encode(section, "UTF-8") } catch (e: Exception) { section }
        val safeTerm = try { java.net.URLEncoder.encode(term, "UTF-8") } catch (e: Exception) { term }
        val safeMention = try { java.net.URLEncoder.encode(mention, "UTF-8") } catch (e: Exception) { mention }
        val safeSchool = try { java.net.URLEncoder.encode(schoolName, "UTF-8") } catch (e: Exception) { schoolName }
        val safeYear = try { java.net.URLEncoder.encode(schoolYear, "UTF-8") } catch (e: Exception) { schoolYear }
        val avgStr = String.format(java.util.Locale.US, "%.2f", average)
        
        var url = "$BASE_VERIFY_URL?id=$studentId&mat=$matricule&name=$safeName&grade=$safeGrade&section=$safeSection&term=$safeTerm&avg=$avgStr&rank=$rank&size=$classSize&mention=$safeMention&school=$safeSchool&year=$safeYear"
        if (totalFee.isNotEmpty()) url += "&totalFee=${java.net.URLEncoder.encode(totalFee, "UTF-8")}"
        if (paidFee.isNotEmpty()) url += "&paidFee=${java.net.URLEncoder.encode(paidFee, "UTF-8")}"
        if (dueFee.isNotEmpty()) url += "&dueFee=${java.net.URLEncoder.encode(dueFee, "UTF-8")}"
        if (percent.isNotEmpty()) url += "&percent=${java.net.URLEncoder.encode(percent, "UTF-8")}"
        
        return url
    }

    data class QrParsedData(
        val rawContent: String,
        val studentId: Int?,
        val matricule: String?
    )

    fun parseQrContent(content: String): QrParsedData {
        val cleanContent = content.trim()

        if (cleanContent.contains("/verify") || cleanContent.contains("id=") || cleanContent.contains("mat=")) {
            try {
                val uri = android.net.Uri.parse(cleanContent)
                val idStr = uri.getQueryParameter("id")
                val mat = uri.getQueryParameter("mat")
                val id = idStr?.toIntOrNull()
                if (id != null || !mat.isNullOrEmpty()) {
                    return QrParsedData(cleanContent, id, mat)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (cleanContent.startsWith("SCOLA_STUDENT:")) {
            val params = cleanContent.removePrefix("SCOLA_STUDENT:").split(";")
            var id: Int? = null
            var mat: String? = null
            for (param in params) {
                val kv = param.split("=")
                if (kv.size == 2) {
                    when (kv[0]) {
                        "id" -> id = kv[1].toIntOrNull()
                        "matricule" -> mat = kv[1]
                    }
                }
            }
            return QrParsedData(cleanContent, id, mat)
        }
        
        // Handle #MATRICULE format or raw text
        val cleanMat = cleanContent.removePrefix("#").uppercase()
        return QrParsedData(cleanContent, null, cleanMat)
    }
}
