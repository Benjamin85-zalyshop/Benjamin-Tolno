package com.example.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import com.example.data.models.Payment
import com.example.data.models.Student
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.ui.printer.BluetoothPrinterManager
import com.example.ui.printer.PrinterTerminalType

object ReceiptPrinter {

    fun printReceipt(
        context: Context,
        student: Student,
        payment: Payment,
        schoolName: String,
        classFee: Long,
        currency: String = "GNF"
    ) {
        val terminalType = BluetoothPrinterManager.getTerminalType(context)
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "Reçu_${student.firstName}_${student.lastName}"
        
        // 58mm -> ~250 points, 80mm -> ~360 points (1/72 inch points)
        val pageWidth = if (terminalType == PrinterTerminalType.POS_58MM) 250 else 360
        val pageHeight = if (terminalType == PrinterTerminalType.POS_58MM) 750 else 850
        
        printManager.print(jobName, object : PrintDocumentAdapter() {
            private var pdfDocument: PdfDocument? = null

            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback,
                extras: Bundle?
            ) {
                pdfDocument = PdfDocument()
                val info = PrintDocumentInfo.Builder(jobName)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()
                callback.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                val doc = pdfDocument ?: return
                
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
                val page = doc.startPage(pageInfo)
                val canvas = page.canvas
                
                drawReceiptContent(canvas, student, payment, schoolName, currency, pageWidth)
                
                doc.finishPage(page)
                
                try {
                    doc.writeTo(FileOutputStream(destination.fileDescriptor))
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.toString())
                } finally {
                    doc.close()
                    pdfDocument = null
                }
            }
        }, null)
    }

    private fun drawReceiptContent(
        canvas: Canvas,
        student: Student,
        payment: Payment,
        schoolName: String,
        currency: String,
        pageWidth: Int = 250
    ) {
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }
        
        var y = 30f
        val centerX = pageWidth / 2f
        
        // School Name
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 16f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(schoolName, centerX, y, paint)
        y += 25f
        
        // Title
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 14f
        canvas.drawText("REÇU DE PAIEMENT", centerX, y, paint)
        y += 30f
        
        // Left aligned content
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 12f
        
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateStr = dateFormat.format(Date(payment.date))
        
        canvas.drawText("Date: $dateStr", 10f, y, paint)
        y += 20f
        
        val matricule = if (student.remoteId.length >= 5) student.remoteId.take(5).uppercase() else student.id.toString()
        canvas.drawText("Matricule: #$matricule", 10f, y, paint)
        y += 20f
        
        canvas.drawText("Élève: ${student.firstName} ${student.lastName}", 10f, y, paint)
        y += 20f
        
        canvas.drawText("Classe: ${student.grade}", 10f, y, paint)
        y += 30f
        
        val fmt = java.text.NumberFormat.getInstance(java.util.Locale("fr", "GN"))
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Montant: ${fmt.format(payment.amount)} $currency", 10f, y, paint)
        y += 20f
        
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Motif: ${payment.reason}", 10f, y, paint)
        y += 20f
        
        canvas.drawText("Mode: ${payment.paymentMethod}", 10f, y, paint)
        y += 40f
        
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Merci de votre confiance.", centerX, y, paint)
        
        y += 20f
        val qrData = com.example.ui.util.QrCodeUtils.buildStudentQrData(
            studentId = student.id,
            remoteId = student.remoteId,
            name = "${student.firstName} ${student.lastName}",
            grade = student.grade,
            section = student.section,
            paidFee = fmt.format(payment.amount),
            schoolName = schoolName
        )
        // Increase QR code size from 120 to 200 for better scannability
        val qrBitmap = com.example.ui.util.QrCodeUtils.generateQrBitmap(qrData, 200)
        if (qrBitmap != null) {
            val qrX = centerX - 100f
            canvas.drawBitmap(qrBitmap, qrX, y, null)
        }
    }

    fun printSummaryTicket(
        context: Context,
        schoolName: String,
        student: Student,
        totalPaid: Long,
        remaining: Long,
        currency: String = "GNF"
    ) {
        val terminalType = BluetoothPrinterManager.getTerminalType(context)
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "Recap_${student.firstName}_${student.lastName}"
        
        val pageWidth = if (terminalType == PrinterTerminalType.POS_58MM) 250 else 360
        val pageHeight = if (terminalType == PrinterTerminalType.POS_58MM) 750 else 850

        printManager.print(jobName, object : PrintDocumentAdapter() {
            private var pdfDocument: PdfDocument? = null

            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback,
                extras: Bundle?
            ) {
                pdfDocument = PdfDocument()
                val info = PrintDocumentInfo.Builder(jobName)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()
                callback.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                val doc = pdfDocument ?: return
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
                val page = doc.startPage(pageInfo)
                val canvas = page.canvas
                
                // Draw content
                val paint = Paint().apply {
                    color = Color.BLACK
                    textSize = 12f
                    isAntiAlias = true
                }
                
                var y = 30f
                val centerX = pageWidth / 2f
                
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 16f
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText(schoolName, centerX, y, paint)
                y += 25f
                
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 14f
                canvas.drawText("RÉCAPITULATIF FINANCIER", centerX, y, paint)
                y += 30f
                
                paint.textAlign = Paint.Align.LEFT
                paint.textSize = 12f
                
                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale("fr", "GN"))
                val dateStr = sdf.format(java.util.Date())
                
                canvas.drawText("Date: $dateStr", 10f, y, paint)
                y += 20f
                
                val matricule = if (student.remoteId.length >= 5) student.remoteId.take(5).uppercase() else student.id.toString()
                canvas.drawText("Matricule: #$matricule", 10f, y, paint)
                y += 20f
                
                canvas.drawText("Élève: ${student.firstName} ${student.lastName}", 10f, y, paint)
                y += 20f
                
                canvas.drawText("Classe: ${student.grade}", 10f, y, paint)
                y += 30f
                
                val fmt = java.text.NumberFormat.getInstance(java.util.Locale("fr", "GN"))
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("TOTAL PAYÉ: ${fmt.format(totalPaid)} $currency", 10f, y, paint)
                y += 20f
                
                canvas.drawText("RESTE: ${fmt.format(remaining)} $currency", 10f, y, paint)
                y += 40f
                
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("Merci de votre confiance.", centerX, y, paint)
                
                y += 20f
                val qrData = com.example.ui.util.QrCodeUtils.buildStudentQrData(
                    studentId = student.id,
                    remoteId = student.remoteId,
                    name = "${student.firstName} ${student.lastName}",
                    grade = student.grade,
                    section = student.section,
                    totalFee = fmt.format(totalPaid + remaining),
                    paidFee = fmt.format(totalPaid),
                    dueFee = fmt.format(remaining),
                    schoolName = schoolName
                )
                // Increase QR code size from 120 to 200 for better scannability
                val qrBitmap = com.example.ui.util.QrCodeUtils.generateQrBitmap(qrData, 200)
                if (qrBitmap != null) {
                    val qrX = centerX - 100f
                    canvas.drawBitmap(qrBitmap, qrX, y, null)
                }
                
                doc.finishPage(page)
                
                try {
                    doc.writeTo(FileOutputStream(destination.fileDescriptor))
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.toString())
                } finally {
                    doc.close()
                    pdfDocument = null
                }
            }
        }, null)
    }
}
