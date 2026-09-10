import re

with open('app/src/main/java/com/example/ui/UserManualGenerator.kt', 'r') as f:
    content = f.read()

old_footer_call = """        for (feature in printFeatures) {
            canvas.drawText(feature, 50f, yPos, paint)
            yPos += 19f
        }
        
        drawFooter(canvas, 7, paint, textColorLight)
        pdfDocument.finishPage(page)"""

new_footer_call = """        for (feature in printFeatures) {
            canvas.drawText(feature, 50f, yPos, paint)
            yPos += 19f
        }
        
        // --- PROMOTIONAL BANNER FOR POS TERMINAL ---
        val bannerY = yPos + 30f
        
        // Banner Background (Soft Blue)
        paint.color = 0xFFEFF6FF.toInt()
        val bannerRect = android.graphics.RectF(50f, bannerY, 545f, bannerY + 160f)
        canvas.drawRoundRect(bannerRect, 12f, 12f, paint)
        
        // Banner Border
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = 0xFF93C5FD.toInt()
        canvas.drawRoundRect(bannerRect, 12f, 12f, paint)
        paint.style = android.graphics.Paint.Style.FILL
        
        // Draw POS Terminal Mockup
        val posX = 430f
        val posY = bannerY + 20f
        
        // Receipt paper
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawRect(posX + 15f, posY - 15f, posX + 65f, posY + 20f, paint)
        // Receipt lines
        paint.color = 0xFFD1D5DB.toInt()
        paint.strokeWidth = 1f
        canvas.drawLine(posX + 20f, posY - 5f, posX + 60f, posY - 5f, paint)
        canvas.drawLine(posX + 20f, posY, posX + 60f, posY, paint)
        canvas.drawLine(posX + 20f, posY + 5f, posX + 50f, posY + 5f, paint)
        
        // Terminal Body
        paint.color = 0xFF374151.toInt() // Dark Gray
        val posBody = android.graphics.RectF(posX, posY + 10f, posX + 80f, posY + 130f)
        canvas.drawRoundRect(posBody, 10f, 10f, paint)
        
        // Printer head (top bump)
        val printerHead = android.graphics.RectF(posX - 5f, posY + 5f, posX + 85f, posY + 30f)
        canvas.drawRoundRect(printerHead, 8f, 8f, paint)
        
        // Screen
        paint.color = 0xFFE5E7EB.toInt() // Light gray screen
        val posScreen = android.graphics.RectF(posX + 8f, posY + 35f, posX + 72f, posY + 105f)
        canvas.drawRoundRect(posScreen, 4f, 4f, paint)
        
        // ScolaPay logo on screen
        paint.color = primaryColor
        paint.textSize = 9f
        paint.isFakeBoldText = true
        canvas.drawText("ScolaPay", posX + 16f, posY + 65f, paint)
        
        // Text Content
        paint.color = primaryColor
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("Équipez votre école avec nos Terminaux Android !", 70f, bannerY + 35f, paint)
        
        paint.color = textColorDark
        paint.textSize = 11f
        paint.isFakeBoldText = false
        canvas.drawText("ScolaPay vend des terminaux Android professionnels", 70f, bannerY + 65f, paint)
        canvas.drawText("avec imprimante thermique intégrée pour l'impression", 70f, bannerY + 85f, paint)
        canvas.drawText("directe et instantanée des tickets de caisse.", 70f, bannerY + 105f, paint)
        
        paint.color = 0xFF059669.toInt() // Success Green
        paint.textSize = 11.5f
        paint.isFakeBoldText = true
        canvas.drawText("Contactez-nous pour commander votre terminal :", 70f, bannerY + 135f, paint)
        
        paint.color = primaryColor
        canvas.drawText("+224 628 37 65 66", 70f, bannerY + 152f, paint)
        
        drawFooter(canvas, 7, paint, textColorLight)
        pdfDocument.finishPage(page)"""

content = content.replace(old_footer_call, new_footer_call)

with open('app/src/main/java/com/example/ui/UserManualGenerator.kt', 'w') as f:
    f.write(content)

