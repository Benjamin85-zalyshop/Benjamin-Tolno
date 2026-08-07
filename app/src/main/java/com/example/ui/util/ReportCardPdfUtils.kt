package com.example.ui.util

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Base64
import com.example.data.models.Student
import com.example.data.models.StudentGrade
import com.example.data.models.Subject
import java.util.Locale

object ReportCardPdfUtils {

    data class StudentReportSummary(
        val totalCoeff: Int,
        val totalPoints: Float,
        val average: Float,
        val rank: Int,
        val classSize: Int,
        val classAverage: Float,
        val mention: String
    )

    fun calculateSummary(
        student: Student,
        term: String,
        allStudentsInClass: List<Student>,
        subjects: List<Subject>,
        allGradesForClassAndTerm: List<StudentGrade>
    ): StudentReportSummary {

        // Calculate averages for all students in class to get rank & class average
        val studentAverages = mutableMapOf<Int, Float>()

        for (st in allStudentsInClass) {
            val stGrades = allGradesForClassAndTerm.filter { it.studentId == st.id }
            var points = 0f
            var coeffSum = 0

            for (sub in subjects) {
                val g = stGrades.find { it.subjectId == sub.id }
                if (g != null) {
                    val subAvg = g.evaluationScore
                    if (subAvg != null) {
                        points += subAvg * sub.coefficient
                        coeffSum += sub.coefficient
                    }
                }
            }

            val avg = if (coeffSum > 0) points / coeffSum else 0f
            studentAverages[st.id] = avg
        }

        val myAvg = studentAverages[student.id] ?: 0f
        val classSize = allStudentsInClass.size
        
        // Sort descending to get rank
        val sortedAverages = studentAverages.values.sortedDescending()
        val rank = (sortedAverages.indexOf(myAvg) + 1).coerceAtLeast(1)
        val classAvg = if (studentAverages.isNotEmpty()) studentAverages.values.average().toFloat() else 0f

        val scaledAvg = if (student.section.equals("LE PRIMAIRE", ignoreCase = true)) myAvg * 2f else myAvg
        val mention = when {
            scaledAvg >= 16f -> "Félicitations du Conseil"
            scaledAvg >= 14f -> "Encouragements"
            scaledAvg >= 12f -> "Tableau d'Honneur"
            scaledAvg >= 10f -> "Passable"
            scaledAvg >= 8f -> "Travail Insuffisant"
            else -> "Avertissement"
        }

        val myGrades = allGradesForClassAndTerm.filter { it.studentId == student.id }
        var myTotalPoints = 0f
        var myTotalCoeff = 0
        for (sub in subjects) {
            val g = myGrades.find { it.subjectId == sub.id }
            if (g != null) {
                val subAvg = g.evaluationScore
                if (subAvg != null) {
                    myTotalPoints += subAvg * sub.coefficient
                    myTotalCoeff += sub.coefficient
                }
            }
        }

        return StudentReportSummary(
            totalCoeff = myTotalCoeff,
            totalPoints = myTotalPoints,
            average = myAvg,
            rank = rank,
            classSize = classSize,
            classAverage = classAvg,
            mention = mention
        )
    }

    fun generateReportCardPdf(
        context: Context,
        student: Student,
        term: String,
        schoolYear: String,
        schoolName: String,
        schoolLogoBase64: String?,
        schoolAddress: String = "",
        schoolPhone: String = "",
        subjects: List<Subject>,
        studentGrades: List<StudentGrade>,
        summary: StudentReportSummary,
        uri: Uri,
        totalFee: String = "",
        paidFee: String = "",
        dueFee: String = "",
        percent: String = ""
    ) {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4 standard width in points (72 dpi)
        val pageHeight = 842 // A4 standard height
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }

        // 1. Background
        paint.color = Color.WHITE
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), paint)

        // 2. Header Box
        val primaryColor = Color.parseColor("#0F56E3")
        paint.color = primaryColor
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 70f, paint)

        // School Logo
        if (!schoolLogoBase64.isNullOrBlank()) {
            try {
                val bytes = Base64.decode(schoolLogoBase64, Base64.DEFAULT)
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bmp != null) {
                    val rect = RectF(16f, 10f, 66f, 60f)
                    canvas.drawBitmap(bmp, null, rect, paint)
                }
            } catch (e: Exception) { e.printStackTrace() }
        }

        // Header Text
        paint.color = Color.WHITE
        paint.isFakeBoldText = true
        paint.textSize = 15f
        val headerTitle = if (schoolName.isNotBlank()) schoolName.uppercase() else "ÉTABLISSEMENT SCOLAIRE"
        canvas.drawText(headerTitle, 80f, 30f, paint)

        paint.textSize = 10f
        paint.isFakeBoldText = false
        val contactLine = listOf(schoolAddress, schoolPhone).filter { it.isNotBlank() }.joinToString(" • ")
        val subtitle = if (contactLine.isNotEmpty()) contactLine else "ScolaPay - Gestion Scolaire & Pédagogique"
        canvas.drawText(subtitle, 80f, 48f, paint)

        paint.textAlign = Paint.Align.RIGHT
        paint.isFakeBoldText = true
        paint.textSize = 11f
        canvas.drawText("BULLETIN DE NOTES", pageWidth - 20f, 30f, paint)
        paint.textSize = 9.5f
        paint.isFakeBoldText = false
        canvas.drawText("$term • Année : $schoolYear", pageWidth - 20f, 48f, paint)
        paint.textAlign = Paint.Align.LEFT

        // 3. Student Identity Card Box
        var startY = 88f
        paint.color = Color.parseColor("#F8FAFC")
        val studentBox = RectF(20f, startY, pageWidth - 20f, startY + 80f)
        canvas.drawRoundRect(studentBox, 6f, 6f, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = Color.parseColor("#CBD5E1")
        canvas.drawRoundRect(studentBox, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        // Student Photo
        val photoRect = RectF(30f, startY + 10f, 85f, startY + 70f)
        var photoDrawn = false
        if (!student.photoBase64.isNullOrBlank()) {
            try {
                val bytes = Base64.decode(student.photoBase64, Base64.DEFAULT)
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bmp != null) {
                    canvas.drawBitmap(bmp, null, photoRect, paint)
                    photoDrawn = true
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
        if (!photoDrawn) {
            paint.color = Color.parseColor("#E2E8F0")
            canvas.drawRoundRect(photoRect, 4f, 4f, paint)
            paint.color = Color.parseColor("#64748B")
            paint.textSize = 8f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("PHOTO", photoRect.centerX(), photoRect.centerY() + 3f, paint)
            paint.textAlign = Paint.Align.LEFT
        }

        // Student Info Text
        val infoX = 100f
        paint.color = Color.parseColor("#0F172A")
        paint.isFakeBoldText = true
        paint.textSize = 12f
        canvas.drawText("${student.firstName} ${student.lastName}".uppercase(), infoX, startY + 26f, paint)

        val matricule = if (student.remoteId.length >= 5) student.remoteId.take(5).uppercase() else student.id.toString()
        paint.textSize = 9.5f
        paint.color = primaryColor
        canvas.drawText("Matricule : #$matricule", infoX, startY + 44f, paint)

        paint.color = Color.parseColor("#475569")
        paint.isFakeBoldText = false
        canvas.drawText("Section : ${student.section}   |   Classe : ${student.grade}", infoX, startY + 62f, paint)

        // QR Code top right in student box
        try {
            val qrData = QrCodeUtils.buildReportCardQrData(
                studentId = student.id,
                remoteId = matricule,
                name = "${student.firstName} ${student.lastName}",
                grade = student.grade,
                section = student.section,
                term = term,
                average = summary.average,
                rank = summary.rank,
                classSize = summary.classSize,
                mention = summary.mention,
                schoolName = schoolName,
                schoolYear = schoolYear,
                totalFee = totalFee,
                paidFee = paidFee,
                dueFee = dueFee,
                percent = percent
            )
            val qrBmp = QrCodeUtils.generateQrBitmap(qrData, 150)
            if (qrBmp != null) {
                val qrRect = RectF(pageWidth - 85f, startY + 10f, pageWidth - 30f, startY + 65f)
                canvas.drawBitmap(qrBmp, null, qrRect, paint)
                paint.color = Color.parseColor("#64748B")
                paint.textSize = 5.5f
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("VERIF. SCANNABLE", qrRect.centerX(), qrRect.bottom + 8f, paint)
                paint.textAlign = Paint.Align.LEFT
            }
        } catch (e: Exception) { e.printStackTrace() }

        // 4. Grades Table Header
        startY += 100f
        val tableLeft = 20f
        val tableRight = pageWidth - 20f
        
        paint.color = primaryColor
        canvas.drawRect(tableLeft, startY, tableRight, startY + 24f, paint)

        paint.color = Color.WHITE
        paint.isFakeBoldText = true
        paint.textSize = 9f
        val isPrimary = student.section.contains("PRIMAIRE", ignoreCase = true)
        val maxBase = if (isPrimary) 10 else 20

        canvas.drawText("MATIÈRE", tableLeft + 10f, startY + 16f, paint)
        canvas.drawText("COEFF", tableLeft + 200f, startY + 16f, paint)
        canvas.drawText("NOTE (/$maxBase)", tableLeft + 280f, startY + 16f, paint)
        canvas.drawText("APPRÉCIATION / REMARQUE", tableLeft + 380f, startY + 16f, paint)

        // 5. Grades Table Rows
        startY += 24f
        paint.isFakeBoldText = false
        val rowHeight = 22f

        var isAltRow = false
        for (sub in subjects) {
            val grade = studentGrades.find { it.subjectId == sub.id }
            val subAvg = grade?.evaluationScore

            paint.color = if (isAltRow) Color.parseColor("#F8FAFC") else Color.WHITE
            canvas.drawRect(tableLeft, startY, tableRight, startY + rowHeight, paint)

            // Row Bottom Border
            paint.color = Color.parseColor("#E2E8F0")
            canvas.drawLine(tableLeft, startY + rowHeight, tableRight, startY + rowHeight, paint)

            // Text
            paint.color = Color.parseColor("#1E293B")
            paint.textSize = 9f
            paint.isFakeBoldText = true
            val nameTruncated = if (sub.name.length > 25) sub.name.take(23) + "..." else sub.name
            canvas.drawText(nameTruncated, tableLeft + 10f, startY + 15f, paint)

            paint.isFakeBoldText = false
            canvas.drawText("${sub.coefficient}", tableLeft + 210f, startY + 15f, paint)

            val avgStr = if (subAvg != null) String.format(Locale.US, "%.2f", subAvg) else "-"

            val scaledSubAvg = if (isPrimary && subAvg != null) subAvg * 2f else subAvg
            paint.isFakeBoldText = true
            paint.color = if (subAvg != null && subAvg < (sub.maxScore / 2f)) Color.parseColor("#DC2626") else Color.parseColor("#0F56E3")
            canvas.drawText(avgStr, tableLeft + 290f, startY + 15f, paint)

            paint.isFakeBoldText = false
            paint.color = Color.parseColor("#475569")
            val comment = grade?.teacherComment?.ifBlank { null } ?: when {
                subAvg == null -> "Non évalué"
                scaledSubAvg!! >= 16f -> "Très bien"
                scaledSubAvg >= 14f -> "Bien"
                scaledSubAvg >= 12f -> "Assez bien"
                scaledSubAvg >= 10f -> "Passable"
                else -> "Insuffisant"
            }
            val commTruncated = if (comment.length > 22) comment.take(20) + "..." else comment
            canvas.drawText(commTruncated, tableLeft + 380f, startY + 15f, paint)

            startY += rowHeight
            isAltRow = !isAltRow
        }

        // Table Outer Border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = Color.parseColor("#CBD5E1")
        canvas.drawRect(tableLeft, 88f + 100f, tableRight, startY, paint)
        paint.style = Paint.Style.FILL

        // 6. Final Performance & Decision Summary Box
        startY += 20f
        val summaryBoxHeight = 110f
        paint.color = Color.parseColor("#F1F5F9")
        val summaryRect = RectF(tableLeft, startY, tableRight, startY + summaryBoxHeight)
        canvas.drawRoundRect(summaryRect, 6f, 6f, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = Color.parseColor("#94A3B8")
        canvas.drawRoundRect(summaryRect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        // Column 1: Averages & Points
        paint.color = Color.parseColor("#0F172A")
        paint.isFakeBoldText = true
        paint.textSize = 10f
        canvas.drawText("RÉCAPITULATIF DES RÉSULTATS", tableLeft + 15f, startY + 22f, paint)

        val isPrimaire = student.section.contains("PRIMAIRE", true)
        val denom = if (isPrimaire) "10" else "20"
        
        paint.isFakeBoldText = false
        paint.textSize = 9f
        paint.color = Color.parseColor("#334155")
        canvas.drawText("Total Coefficients : ${summary.totalCoeff}", tableLeft + 15f, startY + 42f, paint)
        canvas.drawText("Total Points : ${String.format(Locale.US, "%.2f", summary.totalPoints)}", tableLeft + 15f, startY + 58f, paint)
        canvas.drawText("Moyenne de la Classe : ${String.format(Locale.US, "%.2f", summary.classAverage)} / $denom", tableLeft + 15f, startY + 74f, paint)

        // Column 2: Big Highlight - General Average & Rank
        val midX = tableLeft + 250f
        paint.color = primaryColor
        paint.isFakeBoldText = true
        paint.textSize = 11f
        canvas.drawText("MOYENNE GÉNÉRALE", midX, startY + 22f, paint)

        paint.textSize = 22f
        val avgFormatted = String.format(Locale.US, "%.2f", summary.average)
        canvas.drawText("$avgFormatted / $denom", midX, startY + 52f, paint)

        paint.textSize = 10f
        paint.color = Color.parseColor("#0F172A")
        val rankSuffix = if (summary.rank == 1) "er" else "ème"
        canvas.drawText("Rang : ${summary.rank}$rankSuffix sur ${summary.classSize} élèves", midX, startY + 74f, paint)

        // Column 3: Mention / Appréciation
        val rightX = tableLeft + 410f
        paint.color = Color.parseColor("#0F172A")
        paint.isFakeBoldText = true
        paint.textSize = 10f
        canvas.drawText("MENTION DU CONSEIL", rightX, startY + 22f, paint)

        paint.textSize = 11f
        paint.color = if (summary.average >= 10f) Color.parseColor("#166534") else Color.parseColor("#991B1B")
        canvas.drawText(summary.mention, rightX, startY + 48f, paint)

        // 7. Signatures Section
        startY += summaryBoxHeight + 35f
        paint.color = Color.parseColor("#1E293B")
        paint.isFakeBoldText = true
        paint.textSize = 9.5f
        canvas.drawText("Le Parent d'Élève", tableLeft + 30f, startY, paint)
        canvas.drawText("Le Chef d'Établissement & Cachet", pageWidth - 200f, startY, paint)

        paint.isFakeBoldText = false
        paint.color = Color.parseColor("#94A3B8")
        paint.textSize = 7.5f
        canvas.drawText("(Signature)", tableLeft + 30f, startY + 14f, paint)
        canvas.drawText("(Signature et Sceau Officiel)", pageWidth - 200f, startY + 14f, paint)

        // Footer Bar
        paint.color = Color.parseColor("#94A3B8")
        paint.textSize = 7f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Bulletin généré par ScolaPay • Solution certifiée de gestion scolaire et financière", pageWidth / 2f, pageHeight - 18f, paint)

        pdfDocument.finishPage(page)

        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }
    }
}
