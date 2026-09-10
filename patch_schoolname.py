import re

with open('app/src/main/java/com/example/ui/screens/ExpensesScreen.kt', 'r') as f:
    content = f.read()

old_vars = """    val schoolName = schoolAccount?.schoolName ?: "ScolaPay"
    val schoolAddress = schoolAccount?.address ?: ""
    val schoolPhone = schoolAccount?.founderPhone ?: ""
    val logoBase64 = schoolAccount?.logoBase64"""

new_vars = """    val actualSchoolName = schoolAccount?.displayName?.takeIf { it.isNotBlank() } ?: schoolAccount?.schoolName ?: "ScolaPay"
    val schoolEmail = if (schoolAccount?.displayName?.isNotBlank() == true) schoolAccount.schoolName else ""
    val schoolAddress = schoolAccount?.address ?: ""
    val schoolPhone = schoolAccount?.founderPhone ?: ""
    val logoBase64 = schoolAccount?.logoBase64"""

content = content.replace(old_vars, new_vars)

old_draw = """    // Draw School Info
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
    
    yPosition += 110f"""

new_draw = """    // Draw School Info
    var textY = yPosition + 20f
    paint.textAlign = android.graphics.Paint.Align.RIGHT
    paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    paint.textSize = 20f
    paint.color = android.graphics.Color.BLACK
    canvas.drawText(actualSchoolName.uppercase(), 545f, textY, paint)
    
    paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
    paint.textSize = 12f
    paint.color = android.graphics.Color.DKGRAY
    
    textY += 20f
    if (schoolEmail.isNotBlank()) {
        canvas.drawText(schoolEmail, 545f, textY, paint)
        textY += 16f
    }
    
    if (schoolAddress.isNotBlank()) {
        canvas.drawText(schoolAddress, 545f, textY, paint)
        textY += 16f
    }
    
    if (schoolPhone.isNotBlank()) {
        canvas.drawText("Tél: " + schoolPhone, 545f, textY, paint)
    }
    
    yPosition += 110f"""

content = content.replace(old_draw, new_draw)

with open('app/src/main/java/com/example/ui/screens/ExpensesScreen.kt', 'w') as f:
    f.write(content)
