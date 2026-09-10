import re

with open('app/src/main/java/com/example/ui/UserManualGenerator.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'canvas.drawText("Comment importer votre logo :", 50f, yPos, paint)',
    'canvas.drawText("Comment configurer le logo et la devise :", 50f, yPos, paint)'
)

with open('app/src/main/java/com/example/ui/UserManualGenerator.kt', 'w') as f:
    f.write(content)
