import re

filepath = 'app/src/main/java/com/example/ui/ReceiptPrinter.kt'
with open(filepath, 'r') as f:
    content = f.read()

# For drawReceiptContent
old_receipt_content = """        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Merci de votre confiance.", centerX, y, paint)
    }"""

new_receipt_content = """        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Merci de votre confiance.", centerX, y, paint)
        
        y += 20f
        val qrData = "ScolaPay - ${student.firstName} ${student.lastName}\\n" +
                     "Montant: ${fmt.format(payment.amount)} $currency\\n" +
                     "Date: $dateStr"
        val qrBitmap = com.example.ui.util.QrCodeUtils.generateQrBitmap(qrData, 120)
        if (qrBitmap != null) {
            val qrX = centerX - 60f
            canvas.drawBitmap(qrBitmap, qrX, y, null)
        }
    }"""
content = content.replace(old_receipt_content, new_receipt_content)

# For printSummaryTicket
old_summary_content = """                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("Merci de votre confiance.", centerX, y, paint)
                
                doc.finishPage(page)"""

new_summary_content = """                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("Merci de votre confiance.", centerX, y, paint)
                
                y += 20f
                val qrData = "ScolaPay Recap - $studentName\\n" +
                             "TOTAL: ${fmt.format(totalPaid)} $currency\\n" +
                             "RESTE: ${fmt.format(remaining)} $currency"
                val qrBitmap = com.example.ui.util.QrCodeUtils.generateQrBitmap(qrData, 120)
                if (qrBitmap != null) {
                    val qrX = centerX - 60f
                    canvas.drawBitmap(qrBitmap, qrX, y, null)
                }
                
                doc.finishPage(page)"""
content = content.replace(old_summary_content, new_summary_content)

with open(filepath, 'w') as f:
    f.write(content)
