import re

with open('app/src/main/java/com/example/ui/UserManualGenerator.kt', 'r') as f:
    content = f.read()

old_advice = """        canvas.drawText("Une fois configuré, le logo de l'école est intégré de manière dynamique", 70f, 595f, paint)
        canvas.drawText("en haut à droite de l'en-tête de chaque reçu de paiement et de chaque", 70f, 615f, paint)
        canvas.drawText("facture PDF de scolarité que vous partagez avec les parents.", 70f, 635f, paint)"""

new_advice = """        canvas.drawText("Une fois configurés, le logo et la devise de l'école sont intégrés", 70f, 595f, paint)
        canvas.drawText("dynamiquement sur chaque reçu de paiement, rapport de dépenses", 70f, 615f, paint)
        canvas.drawText("et facture PDF de scolarité que vous partagez avec les parents.", 70f, 635f, paint)"""

content = content.replace(old_advice, new_advice)

with open('app/src/main/java/com/example/ui/UserManualGenerator.kt', 'w') as f:
    f.write(content)
