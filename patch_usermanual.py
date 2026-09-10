import re

with open('app/src/main/java/com/example/ui/UserManualGenerator.kt', 'r') as f:
    content = f.read()

# Replace chapter title
content = content.replace(
    'drawPageHeader(canvas, "2. CONFIGURATION & IMPORTATION DU LOGO", primaryColor, paint)',
    'drawPageHeader(canvas, "2. CONFIGURATION : LOGO ET DEVISE", primaryColor, paint)'
)

# Update the intro text
content = content.replace(
    'canvas.drawText("vous pouvez importer le logo de votre école directement dans ScolaPay.", 50f, 155f, paint)\n        canvas.drawText("Il sera automatiquement inséré sur toutes les factures générées.", 50f, 175f, paint)',
    'canvas.drawText("vous pouvez importer le logo de votre école directement dans ScolaPay.", 50f, 155f, paint)\n        canvas.drawText("Vous pouvez également y définir la devise officielle de votre école.", 50f, 175f, paint)'
)

# Add currency configuration step
old_logo_steps = """        val logoSteps = listOf(
            "1. Rendez-vous dans l'onglet d'accueil ScolaPay.",
            "2. Faites défiler vers le bas jusqu'au bloc",
            "   \\"Configuration\\" (accessible par le Fondateur).",
            "3. Dans l'encadré \\"Logo de l'école\\", cliquez",
            "   sur le bouton bleu \\"Importer\\".",
            "4. Choisissez le fichier d'image (.png ou .jpg)",
            "   de votre logo dans la galerie de votre appareil.",
            "5. ScolaPay compresse automatiquement l'image",
            "   et met à jour la base de données cloud.",
            "6. Pour remplacer ou effacer le logo,",
            "   cliquez sur \\"Changer\\" ou \\"Supprimer\\"."
        )"""

new_logo_steps = """        val logoSteps = listOf(
            "1. Rendez-vous dans l'onglet d'accueil ScolaPay.",
            "2. Faites défiler vers le bas jusqu'au bloc",
            "   \\"Configuration\\" (accessible par le Fondateur).",
            "3. Logo : Cliquez sur le bouton bleu \\"Importer\\",",
            "   choisissez votre fichier (.png ou .jpg).",
            "4. Devise : Cliquez sur le menu de devise",
            "   pour choisir l'unité monétaire de votre école",
            "   (ex: GNF, FCFA, USD, EUR, etc.).",
            "5. La devise s'appliquera instantanément à",
            "   tous les montants, reçus et statistiques.",
            "6. Les modifications sont enregistrées",
            "   automatiquement et partagées sur le réseau."
        )"""

content = content.replace(old_logo_steps, new_logo_steps)

with open('app/src/main/java/com/example/ui/UserManualGenerator.kt', 'w') as f:
    f.write(content)
