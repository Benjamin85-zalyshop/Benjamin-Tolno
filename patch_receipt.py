import re

filepath = 'app/src/main/java/com/example/ui/ReceiptPrinter.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Fix printSummaryTicket signature
old_sig = """    fun printSummaryTicket(
        context: Context,
        schoolName: String,
        matricule: String,
        studentName: String,
        studentGrade: String,
        totalPaid: Long,
        remaining: Long,
        currency: String = "GNF"
    )"""

new_sig = """    fun printSummaryTicket(
        context: Context,
        schoolName: String,
        student: Student,
        totalPaid: Long,
        remaining: Long,
        currency: String = "GNF"
    )"""
content = content.replace(old_sig, new_sig)

# Fix printSummaryTicket internals
old_matricule = """                canvas.drawText("Matricule: #$matricule", 10f, y, paint)
                y += 20f
                
                canvas.drawText("Élève: $studentName", 10f, y, paint)
                y += 20f
                
                canvas.drawText("Classe: $studentGrade", 10f, y, paint)"""

new_matricule = """                val matricule = if (student.remoteId.length >= 5) student.remoteId.take(5).uppercase() else student.id.toString()
                canvas.drawText("Matricule: #$matricule", 10f, y, paint)
                y += 20f
                
                canvas.drawText("Élève: ${student.firstName} ${student.lastName}", 10f, y, paint)
                y += 20f
                
                canvas.drawText("Classe: ${student.grade}", 10f, y, paint)"""
content = content.replace(old_matricule, new_matricule)

# Fix QR Data for printReceipt
old_receipt_qr = """        val qrData = "ScolaPay - ${student.firstName} ${student.lastName}\\n" +
                     "Montant: ${fmt.format(payment.amount)} $currency\\n" +
                     "Date: $dateStr"
        val qrBitmap = com.example.ui.util.QrCodeUtils.generateQrBitmap(qrData, 120)
        if (qrBitmap != null) {
            val qrX = centerX - 60f
            canvas.drawBitmap(qrBitmap, qrX, y, null)
        }"""

new_receipt_qr = """        val qrData = com.example.ui.util.QrCodeUtils.buildStudentQrData(
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
        }"""
content = content.replace(old_receipt_qr, new_receipt_qr)

# Fix QR Data for printSummaryTicket
old_summary_qr = """                val qrData = "ScolaPay Recap - $studentName\\n" +
                             "TOTAL: ${fmt.format(totalPaid)} $currency\\n" +
                             "RESTE: ${fmt.format(remaining)} $currency"
                val qrBitmap = com.example.ui.util.QrCodeUtils.generateQrBitmap(qrData, 120)
                if (qrBitmap != null) {
                    val qrX = centerX - 60f
                    canvas.drawBitmap(qrBitmap, qrX, y, null)
                }"""

new_summary_qr = """                val qrData = com.example.ui.util.QrCodeUtils.buildStudentQrData(
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
                }"""
content = content.replace(old_summary_qr, new_summary_qr)

# Let's also adjust the page height from 600 to something larger so it doesn't get cut off if we increase QR size. 120 to 200 adds 80px.
content = content.replace("private val pageHeight = 600", "private val pageHeight = 750")

with open(filepath, 'w') as f:
    f.write(content)
