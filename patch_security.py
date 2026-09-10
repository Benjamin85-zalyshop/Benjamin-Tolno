import re

with open('app/src/main/java/com/example/ui/UserManualGenerator.kt', 'r') as f:
    content = f.read()

old_intro = """        canvas.drawText("Pour garantir une comptabilité infaillible, ScolaPay sécurise les actions de", 50f, 135f, paint)
        canvas.drawText("suppression de paiements. Seuls les comptes autorisés (Financier, Fondateur)", 50f, 155f, paint)
        canvas.drawText("peuvent initier cette action, qui fait l'objet d'un avertissement strict.", 50f, 175f, paint)"""

new_intro = """        canvas.drawText("Pour une comptabilité infaillible, ScolaPay trace strictement chaque suppression.", 50f, 135f, paint)
        canvas.drawText("Lorsqu'un Financier supprime un paiement, il doit insérer un motif. Ce montant", 50f, 155f, paint)
        canvas.drawText("restera visible mais barré en rouge, et le Fondateur en sera alerté.", 50f, 175f, paint)"""

content = content.replace(old_intro, new_intro)

old_steps = """        val deleteSteps = listOf(
            "1. Rendez-vous dans la fiche de l'élève ou",
            "   dans l'historique général des paiements.",
            "2. Cliquez sur l'icône de corbeille rouge (🗑).",
            "3. Une boîte d'alerte de sécurité apparaît.",
            "4. L'avertissement affiche les détails du paiement",
            "   (élève, montant exact, mode, date).",
            "5. Il rappelle explicitement que cette action",
            "   est définitive et impactera le solde dû.",
            "6. Cliquez sur \\"Supprimer définitivement\\" pour",
            "   confirmer, ou sur \\"Annuler\\" pour renoncer."
        )"""

new_steps = """        val deleteSteps = listOf(
            "1. Rendez-vous dans la fiche de l'élève ou",
            "   dans l'historique général des paiements.",
            "2. Cliquez sur l'icône de corbeille rouge (🗑).",
            "3. Saisissez obligatoirement le motif de la",
            "   suppression pour justifier l'action.",
            "4. Confirmez l'alerte de sécurité.",
            "5. Le paiement supprimé restera visible mais",
            "   barré en rouge dans l'historique.",
            "6. L'action et le motif sont transmis au",
            "   Fondateur qui recevra un avertissement."
        )"""

content = content.replace(old_steps, new_steps)

old_warning = """        canvas.drawText("Ne supprimez un versement qu'en cas d'erreur de saisie flagrante.", 70f, 595f, paint)
        canvas.drawText("Chaque suppression recalculera immédiatement en temps réel le solde", 70f, 615f, paint)
        canvas.drawText("restant à payer de l'élève ainsi que les totaux des bilans de l'école.", 70f, 635f, paint)"""

new_warning = """        canvas.drawText("Ne supprimez un versement qu'en cas d'erreur de saisie flagrante.", 70f, 595f, paint)
        canvas.drawText("Chaque suppression est notifiée au Fondateur. Le solde est recalculé", 70f, 615f, paint)
        canvas.drawText("et le montant reste tracé (barré en rouge) pour une transparence totale.", 70f, 635f, paint)"""

content = content.replace(old_warning, new_warning)

with open('app/src/main/java/com/example/ui/UserManualGenerator.kt', 'w') as f:
    f.write(content)
