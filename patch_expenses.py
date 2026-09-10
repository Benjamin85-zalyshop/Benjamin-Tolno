import re

with open('app/src/main/java/com/example/ui/screens/ExpensesScreen.kt', 'r') as f:
    content = f.read()

# Update the call
content = content.replace(
    'generateExpensesPdf(context, filteredExpenses, selectedMonth ?: "Tous", it, currency)',
    'generateExpensesPdf(context, filteredExpenses, selectedMonth ?: "Tous", it, currency, schoolAccount)'
)

# Extract generateExpensesPdf
old_func_pattern = r"fun generateExpensesPdf\([\s\S]*?\}\n\}"

new_func = """fun generateExpensesPdf(
    context: android.content.Context,
    expenses: List<com.example.data.models.Expense>,
    period: String,
    uri: android.net.Uri,
    currency: String = "GNF",
    schoolAccount: com.example.data.models.SchoolAccount? = null
) {
    val pdfDocument = android.graphics.pdf.PdfDocument()
    val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
    var page = pdfDocument.startPage(pageInfo)
    var canvas = page.canvas
    val paint = android.graphics.Paint()

    val numberFormat = java.text.NumberFormat.getNumberInstance(java.util.Locale("fr", "GN"))
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale("fr", "GN"))

    var yPosition = 50f
    
    // --- Header Section ---
    val schoolName = schoolAccount?.schoolName ?: "ScolaPay"
    val schoolAddress = schoolAccount?.address ?: ""
    val schoolPhone = schoolAccount?.founderPhone ?: ""
    val logoBase64 = schoolAccount?.logoBase64
    
    // Draw Logo
    if (!logoBase64.isNullOrBlank()) {
        try {
            val decodedString = android.util.Base64.decode(logoBase64, android.util.Base64.DEFAULT)
            val decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            if (decodedByte != null) {
                // Keep aspect ratio
                val aspectRatio = decodedByte.width.toFloat() / decodedByte.height.toFloat()
                val logoHeight = 80f
                val logoWidth = logoHeight * aspectRatio
                val logoRect = android.graphics.RectF(50f, yPosition, 50f + logoWidth, yPosition + logoHeight)
                canvas.drawBitmap(decodedByte, null, logoRect, paint)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Draw School Info
    paint.textAlign = android.graphics.Paint.Align.RIGHT
    paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    paint.textSize = 20f
    paint.color = android.graphics.Color.BLACK
    canvas.drawText(schoolName, 545f, yPosition + 20f, paint)
    
    paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
    paint.textSize = 12f
    paint.color = android.graphics.Color.DKGRAY
    if (schoolAddress.isNotBlank()) {
        canvas.drawText(schoolAddress, 545f, yPosition + 40f, paint)
    }
    if (schoolPhone.isNotBlank()) {
        canvas.drawText("Tél: " + schoolPhone, 545f, yPosition + 60f, paint)
    }
    
    yPosition += 110f
    
    // Draw Divider
    paint.color = android.graphics.Color.parseColor("#E5E7EB")
    paint.strokeWidth = 2f
    canvas.drawLine(50f, yPosition, 545f, yPosition, paint)
    
    yPosition += 40f
    
    // --- Title Section ---
    paint.textAlign = android.graphics.Paint.Align.CENTER
    paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    paint.textSize = 22f
    paint.color = android.graphics.Color.parseColor("#0F56E3") // ScolaPay blue
    canvas.drawText("RAPPORT DES DÉPENSES", 595f / 2, yPosition, paint)
    
    yPosition += 25f
    paint.textSize = 14f
    paint.color = android.graphics.Color.DKGRAY
    canvas.drawText("Période : $period", 595f / 2, yPosition, paint)
    
    yPosition += 40f
    
    // --- Table Header ---
    val colDateX = 50f
    val colReasonX = 180f
    val colSectionX = 360f
    val colAmountX = 545f // Right aligned
    
    paint.color = android.graphics.Color.parseColor("#F3F4F6")
    canvas.drawRect(50f, yPosition - 15f, 545f, yPosition + 15f, paint)
    
    paint.color = android.graphics.Color.BLACK
    paint.textAlign = android.graphics.Paint.Align.LEFT
    paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    paint.textSize = 12f
    
    canvas.drawText("Date", colDateX + 5f, yPosition + 5f, paint)
    canvas.drawText("Motif", colReasonX + 5f, yPosition + 5f, paint)
    canvas.drawText("Catégorie", colSectionX + 5f, yPosition + 5f, paint)
    
    paint.textAlign = android.graphics.Paint.Align.RIGHT
    canvas.drawText("Montant", colAmountX - 5f, yPosition + 5f, paint)
    
    yPosition += 25f
    
    // --- Table Content ---
    paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
    paint.textSize = 11f
    var total = 0L
    
    for (expense in expenses) {
        if (yPosition > 780f) {
            pdfDocument.finishPage(page)
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            yPosition = 50f
            
            // Re-draw table header on new page
            paint.color = android.graphics.Color.parseColor("#F3F4F6")
            canvas.drawRect(50f, yPosition - 15f, 545f, yPosition + 15f, paint)
            
            paint.color = android.graphics.Color.BLACK
            paint.textAlign = android.graphics.Paint.Align.LEFT
            paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            paint.textSize = 12f
            
            canvas.drawText("Date", colDateX + 5f, yPosition + 5f, paint)
            canvas.drawText("Motif", colReasonX + 5f, yPosition + 5f, paint)
            canvas.drawText("Catégorie", colSectionX + 5f, yPosition + 5f, paint)
            
            paint.textAlign = android.graphics.Paint.Align.RIGHT
            canvas.drawText("Montant", colAmountX - 5f, yPosition + 5f, paint)
            
            yPosition += 25f
            paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
            paint.textSize = 11f
        }
        
        paint.color = android.graphics.Color.BLACK
        paint.textAlign = android.graphics.Paint.Align.LEFT
        
        val dateStr = sdf.format(java.util.Date(expense.date))
        val amountStr = "${numberFormat.format(expense.amount)} $currency"
        
        val maxReasonLen = 30
        val reasonStr = if (expense.reason.length > maxReasonLen) expense.reason.take(maxReasonLen) + "..." else expense.reason
        
        val maxSectionLen = 25
        val sectionStr = if (expense.section.length > maxSectionLen) expense.section.take(maxSectionLen) + "..." else expense.section
        
        canvas.drawText(dateStr, colDateX + 5f, yPosition, paint)
        canvas.drawText(reasonStr, colReasonX + 5f, yPosition, paint)
        canvas.drawText(sectionStr, colSectionX + 5f, yPosition, paint)
        
        paint.textAlign = android.graphics.Paint.Align.RIGHT
        canvas.drawText(amountStr, colAmountX - 5f, yPosition, paint)
        
        // Draw bottom border for the row
        paint.color = android.graphics.Color.parseColor("#E5E7EB")
        paint.strokeWidth = 1f
        canvas.drawLine(50f, yPosition + 10f, 545f, yPosition + 10f, paint)
        
        yPosition += 25f
        total += expense.amount
    }
    
    // --- Table Footer (Total) ---
    if (yPosition > 780f) {
        pdfDocument.finishPage(page)
        page = pdfDocument.startPage(pageInfo)
        canvas = page.canvas
        yPosition = 50f
    }
    
    yPosition += 10f
    paint.color = android.graphics.Color.parseColor("#F9FAFB")
    canvas.drawRect(350f, yPosition - 15f, 545f, yPosition + 15f, paint)
    
    paint.color = android.graphics.Color.BLACK
    paint.textAlign = android.graphics.Paint.Align.LEFT
    paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    paint.textSize = 12f
    canvas.drawText("TOTAL", 360f, yPosition + 5f, paint)
    
    paint.textAlign = android.graphics.Paint.Align.RIGHT
    paint.color = android.graphics.Color.parseColor("#D92D20") // Red for expenses
    canvas.drawText("${numberFormat.format(total)} $currency", 540f, yPosition + 5f, paint)
    
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
}"""

content = re.sub(old_func_pattern, new_func, content)

with open('app/src/main/java/com/example/ui/screens/ExpensesScreen.kt', 'w') as f:
    f.write(content)

