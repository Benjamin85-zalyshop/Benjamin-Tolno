package com.example.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import java.io.OutputStream

object UserManualGenerator {
    private val textColorDark = 0xFF1F2937.toInt()     // Dark Gray

    fun generateUserManualPdf(context: Context, uri: Uri) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        var yPos = 0f
        
        // Colors
        val primaryColor = 0xFF1E3A8A.toInt()      // Royal Blue
        val primaryLight = 0xFFEFF6FF.toInt()     // Light Blue
        val accentColor = 0xFFEC4899.toInt()       // Hot Pink
        val accentLight = 0xFFFDF2F8.toInt()      // Pink Light
        val successColor = 0xFF10B981.toInt()      // Success Green
        val textColorLight = 0xFF6B7280.toInt()    // Light Gray
        val white = 0xFFFFFFFF.toInt()
        val greenLight = 0xFFDCF8C6.toInt()       // WhatsApp Green Light
        val warningColor = 0xFFEF4444.toInt()      // Safety Red
        val warningLight = 0xFFFEF2F2.toInt()     // Safety Red Light
        
        // --- PAGE 1: COVER & INTRODUCTION ---
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        var paint = Paint().apply { isAntiAlias = true }
        
        // Draw Royal Blue Header block
        paint.color = primaryColor
        canvas.drawRect(0f, 0f, 595f, 280f, paint)
        
        // Header Text
        paint.color = white
        paint.textSize = 36f
        paint.isFakeBoldText = true
        canvas.drawText("ScolaPay", 50f, 110f, paint)
        
        paint.textSize = 18f
        paint.isFakeBoldText = false
        canvas.drawText("MANUEL D'UTILISATION OFFICIEL", 50f, 150f, paint)
        
        paint.textSize = 12f
        paint.color = 0xFF93C5FD.toInt() // Soft light blue
        canvas.drawText("La solution moderne pour la gestion financière des écoles", 50f, 180f, paint)
        canvas.drawText("Version 3.0 • Édition Spéciale ScolaPay", 50f, 200f, paint)
        
        // Draw Introduction block
        paint.color = textColorDark
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("Bienvenue sur ScolaPay", 50f, 330f, paint)
        
        paint.textSize = 13f
        paint.isFakeBoldText = false
        paint.color = textColorLight
        canvas.drawText("ScolaPay est une application complète conçue pour simplifier la vie des", 50f, 360f, paint)
        canvas.drawText("établissements scolaires. Elle permet une traçabilité totale des paiements,", 50f, 380f, paint)
        canvas.drawText("les relances WhatsApp instantanées et une comptabilité en temps réel.", 50f, 400f, paint)
        
        // Draw Features Card (Light Blue Card)
        paint.color = primaryLight
        val introCard = RectF(50f, 430f, 545f, 590f)
        canvas.drawRoundRect(introCard, 12f, 12f, paint)
        
        // Inside Features Card
        paint.color = primaryColor
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("Pourquoi choisir ScolaPay ?", 70f, 465f, paint)
        
        paint.isFakeBoldText = false
        paint.textSize = 12f
        paint.color = textColorDark
        
        // Draw bullets
        drawCheckBullet(canvas, 70f, 495f, "Séparation claire : Frais de Scolarité vs Frais d'Inscription", paint, successColor)
        drawCheckBullet(canvas, 70f, 525f, "Rapports & Statistiques : Suivi précis du recouvrement par classe", paint, successColor)
        drawCheckBullet(canvas, 70f, 555f, "Zéro Oubli : Relances WhatsApp pré-remplies et appels directs", paint, successColor)
        
        // Bottom Illustration: Flow Diagram
        drawFlowDiagram(canvas, 50f, 630f, primaryColor, accentColor, successColor, paint)
        
        // Page footer
        drawFooter(canvas, 1, paint, textColorLight)
        pdfDocument.finishPage(page)
        
        // --- PAGE 2: INSCRIPTION & SÉCURITÉ DU COMPTE ---
        page = pdfDocument.startPage(pageInfo)
        canvas = page.canvas
        paint = Paint().apply { isAntiAlias = true }
        
        drawPageHeader(canvas, "1. INSCRIPTION & CRÉATION DE COMPTE ÉCOLE", primaryColor, paint)
        
        paint.color = textColorDark
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("Créer et sécuriser le compte de votre école", 50f, 110f, paint)
        
        paint.textSize = 12f
        paint.isFakeBoldText = false
        paint.color = textColorLight
        canvas.drawText("Pour commencer à utiliser ScolaPay, chaque école doit d'abord s'enregistrer", 50f, 135f, paint)
        canvas.drawText("en créant un compte sécurisé avec deux rôles d'accès distincts.", 50f, 155f, paint)
        
        // Draw registration form mockup on the right side
        drawRegistrationFormMockup(canvas, 310f, 210f, primaryColor, accentColor, paint)
        
        // Steps to register (Left Column)
        yPos = 190f
        paint.color = primaryColor
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText("Procédure d'inscription :", 50f, yPos, paint)
        yPos += 25f
        
        paint.color = textColorDark
        paint.isFakeBoldText = false
        paint.textSize = 11f
        
        val regSteps = listOf(
            "1. Saisissez l'adresse email officielle",
            "   de votre établissement scolaire.",
            "2. Définissez le mot de passe du FONDATEUR,",
            "   puis confirmez-le une seconde fois.",
            "3. Définissez le mot de passe du FINANCIER,",
            "   puis confirmez-le également deux fois.",
            "4. Validez l'inscription de l'école.",
            "",
            "🎁 PÉRIODE D'ESSAI GRATUITE :",
            "Afin de vous permettre de tester toutes",
            "les fonctionnalités de ScolaPay en toute",
            "confiance, une période d'essai gratuite",
            "est automatiquement accordée à votre école.",
            "Vous pourrez configurer vos classes, ajouter",
            "vos élèves et tester les relances WhatsApp.",
            "Le paiement ne sera requis qu'à l'issue",
            "de cette période d'évaluation !"
        )
        for (step in regSteps) {
            if (step.startsWith("🎁") || step.startsWith("Afin") || step.startsWith("Le paiement")) {
                paint.color = primaryColor
                if (step.startsWith("🎁")) paint.isFakeBoldText = true
            } else {
                paint.color = textColorDark
                paint.isFakeBoldText = false
            }
            canvas.drawText(step, 50f, yPos, paint)
            yPos += 19f
        }
        
        // Info Banner at the bottom
        paint.color = 0xFFECFDF5.toInt() // Success green light
        val infoBanner = RectF(50f, 540f, 545f, 650f)
        canvas.drawRoundRect(infoBanner, 8f, 8f, paint)
        
        paint.color = 0xFF059669.toInt() // Emerald Green dark
        paint.textSize = 11f
        paint.isFakeBoldText = true
        canvas.drawText("🔒 SÉCURITÉ DOUBLE ACCÈS :", 70f, 570f, paint)
        paint.color = textColorDark
        paint.isFakeBoldText = false
        paint.textSize = 10.5f
        canvas.drawText("ScolaPay sépare les privilèges du Fondateur (accès total, configuration,", 70f, 595f, paint)
        canvas.drawText("tarifs) et du Financier (uniquement encaissement et rapports journaliers)", 70f, 615f, paint)
        canvas.drawText("pour garantir l'intégrité et la sécurité de vos fonds scolaires.", 70f, 635f, paint)
        
        drawFooter(canvas, 2, paint, textColorLight)
        pdfDocument.finishPage(page)
        
        // --- PAGE 3: CONFIGURATION DES CLASSES & LOGO DE L'ÉCOLE ---
        page = pdfDocument.startPage(pageInfo)
        canvas = page.canvas
        paint = Paint().apply { isAntiAlias = true }
        
        drawPageHeader(canvas, "2. CONFIGURATION & IMPORTATION DU LOGO", primaryColor, paint)
        
        paint.color = textColorDark
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("Personnaliser ScolaPay à l'image de votre école", 50f, 110f, paint)
        
        paint.textSize = 12f
        paint.isFakeBoldText = false
        paint.color = textColorLight
        canvas.drawText("Pour que vos documents de facturation soient officiels et professionnels,", 50f, 135f, paint)
        canvas.drawText("vous pouvez importer le logo de votre école directement dans ScolaPay.", 50f, 155f, paint)
        canvas.drawText("Il sera automatiquement inséré sur toutes les factures générées.", 50f, 175f, paint)
        
        // Draw Logo Upload block mockup on the right side
        drawLogoUploadMockup(canvas, 310f, 210f, primaryColor, paint)
        
        // Guide steps (Left Column)
        yPos = 220f
        paint.color = primaryColor
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText("Comment importer votre logo :", 50f, yPos, paint)
        yPos += 25f
        
        paint.color = textColorDark
        paint.isFakeBoldText = false
        paint.textSize = 11f
        
        val logoSteps = listOf(
            "1. Rendez-vous dans l'onglet d'accueil ScolaPay.",
            "2. Faites défiler vers le bas jusqu'au bloc",
            "   \"Configuration\" (accessible par le Fondateur).",
            "3. Dans l'encadré \"Logo de l'école\", cliquez",
            "   sur le bouton bleu \"Importer\".",
            "4. Choisissez le fichier d'image (.png ou .jpg)",
            "   de votre logo dans la galerie de votre appareil.",
            "5. ScolaPay compresse automatiquement l'image",
            "   et met à jour la base de données cloud.",
            "6. Pour remplacer ou effacer le logo,",
            "   cliquez sur \"Changer\" ou \"Supprimer\"."
        )
        for (step in logoSteps) {
            canvas.drawText(step, 50f, yPos, paint)
            yPos += 21f
        }
        
        // Advice note card at the bottom
        paint.color = 0xFFFFFBEB.toInt() // Warning amber light
        val logoAdviceCard = RectF(50f, 540f, 545f, 650f)
        canvas.drawRoundRect(logoAdviceCard, 8f, 8f, paint)
        
        paint.color = 0xFFD97706.toInt() // Amber dark
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("💡 FACTURATION PROFESSIONNELLE :", 70f, 570f, paint)
        paint.color = textColorDark
        paint.isFakeBoldText = false
        paint.textSize = 11f
        canvas.drawText("Une fois configuré, le logo de l'école est intégré de manière dynamique", 70f, 595f, paint)
        canvas.drawText("en haut à droite de l'en-tête de chaque reçu de paiement et de chaque", 70f, 615f, paint)
        canvas.drawText("facture PDF de scolarité que vous partagez avec les parents.", 70f, 635f, paint)
        
        drawFooter(canvas, 3, paint, textColorLight)
        pdfDocument.finishPage(page)
        
        // --- PAGE 4: CONFIGURATION ET ENCAISSEMENT DES FRAIS ---
        page = pdfDocument.startPage(pageInfo)
        canvas = page.canvas
        paint = Paint().apply { isAntiAlias = true }
        
        drawPageHeader(canvas, "3. INSCRIPTION & ENCAISSEMENT DES FRAIS", primaryColor, paint)
        
        paint.color = textColorDark
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("Définir les frais et les encaisser sans mélange", 50f, 110f, paint)
        
        paint.textSize = 12f
        paint.isFakeBoldText = false
        paint.color = textColorLight
        canvas.drawText("Configurez les frais d'inscription ou de réinscription lors de la création", 50f, 135f, paint)
        canvas.drawText("de l'élève. Encaissez-les via le raccourci dédié pour qu'ils ne soient", 50f, 155f, paint)
        canvas.drawText("jamais mélangés aux mensualités scolaires ordinaires.", 50f, 175f, paint)
        
        // Draw Accès Rapides & Inscription Mockup on the right
        drawQuickAccessMockup(canvas, 310f, 210f, primaryColor, accentColor, paint)
        
        // Steps to Cash-In Inscription/Re-inscription (Left Column)
        yPos = 220f
        paint.color = primaryColor
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText("Procédure étape par étape :", 50f, yPos, paint)
        yPos += 25f
        
        paint.color = textColorDark
        paint.isFakeBoldText = false
        paint.textSize = 11f
        
        val cashSteps = listOf(
            "1. Créez l'élève et renseignez ses frais",
            "   optionnels d'inscription ou de réinscription.",
            "2. Dans la grille \"Accès Rapides\" du Dashboard,",
            "   cliquez sur le bouton rose \"Inscription\".",
            "3. Recherchez et sélectionnez l'élève concerné.",
            "4. Cochez le type de paiement souhaité.",
            "5. ScolaPay pré-remplit le montant exact configuré !",
            "6. Sélectionnez le mode (Espèces, OM, MoMo).",
            "7. Validez. Le reçu PDF avec votre logo est prêt."
        )
        for (step in cashSteps) {
            canvas.drawText(step, 50f, yPos, paint)
            yPos += 20f
        }
        
        // Cash-in flow graphics at the bottom
        drawCashInGraphics(canvas, 50f, 520f, accentColor, primaryColor, paint)
        
        drawFooter(canvas, 4, paint, textColorLight)
        pdfDocument.finishPage(page)
        
        // --- PAGE 5: SÉCURITÉ DE SUPPRESSION ---
        page = pdfDocument.startPage(pageInfo)
        canvas = page.canvas
        paint = Paint().apply { isAntiAlias = true }
        
        drawPageHeader(canvas, "4. SÉCURITÉ DES PAIEMENTS & SUPPRESSIONS", primaryColor, paint)
        
        paint.color = textColorDark
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("Double validation contre les erreurs de comptabilité", 50f, 110f, paint)
        
        paint.textSize = 12f
        paint.isFakeBoldText = false
        paint.color = textColorLight
        canvas.drawText("Pour garantir une comptabilité infaillible, ScolaPay sécurise les actions de", 50f, 135f, paint)
        canvas.drawText("suppression de paiements. Seuls les comptes autorisés (Financier, Fondateur)", 50f, 155f, paint)
        canvas.drawText("peuvent initier cette action, qui fait l'objet d'un avertissement strict.", 50f, 175f, paint)
        
        // Draw Alert Dialog Mockup on the right
        drawAlertDialogMockup(canvas, 310f, 210f, primaryColor, paint)
        
        // Deletion instructions (Left Column)
        yPos = 220f
        paint.color = primaryColor
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText("Comment supprimer un paiement :", 50f, yPos, paint)
        yPos += 25f
        
        paint.color = textColorDark
        paint.isFakeBoldText = false
        paint.textSize = 11f
        
        val deleteSteps = listOf(
            "1. Rendez-vous dans la fiche de l'élève ou",
            "   dans l'historique général des paiements.",
            "2. Cliquez sur l'icône de corbeille rouge (🗑).",
            "3. Une boîte d'alerte de sécurité apparaît.",
            "4. L'avertissement affiche les détails du paiement",
            "   (élève, montant exact, mode, date).",
            "5. Il rappelle explicitement que cette action",
            "   est définitive et impactera le solde dû.",
            "6. Cliquez sur \"Supprimer définitivement\" pour",
            "   confirmer, ou sur \"Annuler\" pour renoncer."
        )
        for (step in deleteSteps) {
            canvas.drawText(step, 50f, yPos, paint)
            yPos += 20f
        }
        
        // Warning Banner at the bottom
        paint.color = warningLight
        val warnBanner = RectF(50f, 540f, 545f, 650f)
        canvas.drawRoundRect(warnBanner, 8f, 8f, paint)
        
        paint.color = warningColor
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("⚠️ SÉCURITÉ STRICTE DE TRÉSORERIE :", 70f, 570f, paint)
        paint.color = textColorDark
        paint.isFakeBoldText = false
        paint.textSize = 11f
        canvas.drawText("Ne supprimez un versement qu'en cas d'erreur de saisie flagrante.", 70f, 595f, paint)
        canvas.drawText("Chaque suppression recalculera immédiatement en temps réel le solde", 70f, 615f, paint)
        canvas.drawText("restant à payer de l'élève ainsi que les totaux des bilans de l'école.", 70f, 635f, paint)
        
        drawFooter(canvas, 5, paint, textColorLight)
        pdfDocument.finishPage(page)
        
        // --- PAGE 6: APPELS DIRECTS, RELANCES & SUPPORT ---
        page = pdfDocument.startPage(pageInfo)
        canvas = page.canvas
        paint = Paint().apply { isAntiAlias = true }
        
        drawPageHeader(canvas, "5. APPELS PARENTS, RELANCES & SUPPORT", primaryColor, paint)
        
        paint.color = textColorDark
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("Communication instantanée et assistance", 50f, 110f, paint)
        
        paint.textSize = 12f
        paint.isFakeBoldText = false
        paint.color = textColorLight
        canvas.drawText("Accélérez le recouvrement des frais scolaires en combinant les", 50f, 135f, paint)
        canvas.drawText("appels directs téléphoniques et les relances WhatsApp pré-remplies.", 50f, 155f, paint)
        canvas.drawText("Restez également en contact avec notre support pour toute question.", 50f, 175f, paint)
        
        // Draw parent card mockup on the right
        drawCallParentMockup(canvas, 310f, 210f, primaryColor, paint)
        
        // Communication and Support details (Left Column)
        yPos = 220f
        paint.color = primaryColor
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText("Outils de communication :", 50f, yPos, paint)
        yPos += 25f
        
        paint.color = textColorDark
        paint.isFakeBoldText = false
        paint.textSize = 11f
        
        val commsFeatures = listOf(
            "📞 APPEL DIRECT DES PARENTS (Nouveauté) :",
            "Plus besoin de copier ou saisir manuellement",
            "le numéro de téléphone ! Depuis le dossier de",
            "l'élève, cliquez sur le bouton d'appel bleu.",
            "L'application compose directement le numéro",
            "du parent associé sur votre téléphone.",
            "",
            "💬 RELANCE INDIVIDUELLE WHATSAPP :",
            "ScolaPay génère un message personnalisé",
            "contenant le nom, la classe et le montant exact",
            "dû par l'élève, envoyé en un clic via WhatsApp."
        )
        for (feature in commsFeatures) {
            if (feature.startsWith("📞") || feature.startsWith("💬")) {
                paint.color = primaryColor
                paint.isFakeBoldText = true
            } else {
                paint.color = textColorDark
                paint.isFakeBoldText = false
            }
            canvas.drawText(feature, 50f, yPos, paint)
            yPos += 19f
        }
        
        // Support Center Card (Accent colors)
        paint.color = accentLight
        val supportCard = RectF(50f, 510f, 545f, 640f)
        canvas.drawRoundRect(supportCard, 10f, 10f, paint)
        
        paint.color = accentColor
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("📞 SUPPORT TECHNIQUE & DIRECT SCOLAPAY", 70f, 535f, paint)
        paint.color = textColorDark
        paint.isFakeBoldText = false
        paint.textSize = 11f
        canvas.drawText("Notre équipe technique est disponible pour toute assistance.", 70f, 560f, paint)
        canvas.drawText("Conseiller Technique principal : Benjamin Tolno", 70f, 580f, paint)
        paint.color = primaryColor
        paint.isFakeBoldText = true
        canvas.drawText("Numéro d'appel & WhatsApp : +224 628 37 65 66", 70f, 600f, paint)
        paint.color = textColorLight
        paint.textSize = 9.5f
        paint.isFakeBoldText = false
        canvas.drawText("Disponible du Lundi au Samedi de 08h00 à 18h00.", 70f, 622f, paint)
        
        drawFooter(canvas, 6, paint, textColorLight)
        pdfDocument.finishPage(page)
        
        // --- PAGE 7: CARTES D'ÉLÈVE, BADGES QR & IMPRESSION TICKETS ---
        page = pdfDocument.startPage(pageInfo)
        canvas = page.canvas
        paint = Paint().apply { isAntiAlias = true }
        
        drawPageHeader(canvas, "6. CARTES D'ÉLÈVE, BADGES QR & TICKETS", primaryColor, paint)
        
        paint.color = textColorDark
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("Identification rapide et encaissement optimisé", 50f, 110f, paint)
        
        paint.textSize = 12f
        paint.isFakeBoldText = false
        paint.color = textColorLight
        canvas.drawText("Accélérez l'encaissement et fiabilisez l'identification de vos élèves avec", 50f, 135f, paint)
        canvas.drawText("les cartes scolaires avec QR code et l'impression sur terminaux Android.", 50f, 155f, paint)
        
        yPos = 200f
        paint.color = primaryColor
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText("📇 Cartes d'Élève & Badges QR Imprimables :", 50f, yPos, paint)
        yPos += 25f
        
        paint.color = textColorDark
        paint.isFakeBoldText = false
        paint.textSize = 11f
        val studentCardFeatures = listOf(
            "• Concept : Générer automatiquement une Carte Scolaire PDF pour chaque",
            "  élève avec sa photo (caméra ou galerie), son matricule, sa classe",
            "  et son QR Code permanent.",
            "• Avantage : L'élève ou le parent présente simplement sa carte à la caisse.",
            "  Le caissier scanne le QR Code et la fiche de l'élève s'ouvre instantanément",
            "  pour effectuer un nouveau paiement sans aucune recherche ni erreur."
        )
        for (feature in studentCardFeatures) {
            canvas.drawText(feature, 50f, yPos, paint)
            yPos += 19f
        }
        
        yPos += 20f
        paint.color = primaryColor
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText("🖨️ Impression des tickets (Terminal Android) :", 50f, yPos, paint)
        yPos += 25f
        
        paint.color = textColorDark
        paint.isFakeBoldText = false
        paint.textSize = 11f
        val printFeatures = listOf(
            "• Connectez ScolaPay à un terminal de paiement Android avec imprimante",
            "  thermique intégrée.",
            "• Imprimez immédiatement le ticket de caisse lors du paiement avec le",
            "  logo de votre école pour un rendu professionnel."
        )
        for (feature in printFeatures) {
            canvas.drawText(feature, 50f, yPos, paint)
            yPos += 19f
        }
        
        drawFooter(canvas, 7, paint, textColorLight)
        pdfDocument.finishPage(page)
        
        // --- PAGE 8: BULLETINS & FICHE RÉCAPITULATIVE PARENT ---
        page = pdfDocument.startPage(pageInfo)
        canvas = page.canvas
        paint = Paint().apply { isAntiAlias = true }
        
        drawPageHeader(canvas, "7. BULLETINS & FICHE PARENT (ACCÈS DIRECT)", primaryColor, paint)
        
        paint.color = textColorDark
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("Suivi académique et financier transparent", 50f, 110f, paint)
        
        paint.textSize = 12f
        paint.isFakeBoldText = false
        paint.color = textColorLight
        canvas.drawText("Partagez facilement les résultats scolaires et offrez une vision claire", 50f, 135f, paint)
        canvas.drawText("des finances aux parents grâce à la fiche récapitulative via QR Code.", 50f, 155f, paint)
        
        yPos = 200f
        paint.color = primaryColor
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText("📊 Bulletins : Calcul des notes par période :", 50f, yPos, paint)
        yPos += 25f
        
        paint.color = textColorDark
        paint.isFakeBoldText = false
        paint.textSize = 11f
        val bulletinFeatures = listOf(
            "• Générez des bulletins détaillés pour chaque élève selon la période :",
            "  Mensuel, 1er Trimestre, 2ème Trimestre, etc.",
            "• Le système gère les matières, notes, coefficients et calcule la moyenne.",
            "• Le bulletin est infalsifiable et porte le logo de l'établissement."
        )
        for (feature in bulletinFeatures) {
            canvas.drawText(feature, 50f, yPos, paint)
            yPos += 19f
        }
        
        yPos += 20f
        paint.color = primaryColor
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText("📲 Fiche Récapitulative Parent (Accès Direct) :", 50f, yPos, paint)
        yPos += 25f
        
        paint.color = textColorDark
        paint.isFakeBoldText = false
        paint.textSize = 11f
        val recapFeatures = listOf(
            "• Concept : Générer un QR Code sur les bulletins ou relevés de compte",
            "  que les parents peuvent scanner avec l'appareil photo de leur téléphone.",
            "• Avantage : Le QR Code redirige directement vers un relevé financier",
            "  en ligne sécurisé affichant l'historique complet des versements et le",
            "  reste à payer en temps réel."
        )
        for (feature in recapFeatures) {
            canvas.drawText(feature, 50f, yPos, paint)
            yPos += 19f
        }
        
        drawFooter(canvas, 8, paint, textColorLight)
        pdfDocument.finishPage(page)
        
        // --- PAGE 9: ABONNEMENT ÉCOLE & PAIEMENT DIGITAL ---
        page = pdfDocument.startPage(pageInfo)
        canvas = page.canvas
        paint = Paint().apply { isAntiAlias = true }
        
        drawPageHeader(canvas, "8. ABONNEMENT ÉCOLE & PAIEMENT DIGITAL", primaryColor, paint)
        
        paint.color = textColorDark
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("Paiement en ligne sécurisé via Chap Chap Pay", 50f, 110f, paint)
        
        paint.textSize = 12f
        paint.isFakeBoldText = false
        paint.color = textColorLight
        canvas.drawText("Renouvelez facilement votre licence ScolaPay grâce à notre intégration", 50f, 135f, paint)
        canvas.drawText("avec les services de paiement mobile les plus utilisés en Guinée.", 50f, 155f, paint)
        
        yPos = 200f
        paint.color = primaryColor
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText("💳 Système d'abonnement ScolaPay :", 50f, yPos, paint)
        yPos += 25f
        
        paint.color = textColorDark
        paint.isFakeBoldText = false
        paint.textSize = 11f
        val subFeatures = listOf(
            "• Intégration de Chap Chap Pay : Réglez votre abonnement directement",
            "  depuis l'application ScolaPay.",
            "• Moyens de paiement : Payez facilement avec Orange Money, MTN MoMo,",
            "  Kulu, Soutra Money, Akiba, PayCard ou VISA/Mastercard.",
            "• Activation Immédiate : Une fois le paiement validé sur votre mobile,",
            "  l'application se met à jour automatiquement et débloque votre accès."
        )
        for (feature in subFeatures) {
            canvas.drawText(feature, 50f, yPos, paint)
            yPos += 19f
        }
        
        drawFooter(canvas, 9, paint, textColorLight)
        pdfDocument.finishPage(page)
        
        // Write PDF to output stream
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }
    }
    
    // --- DRAWING HELPERS ---
    
    private fun drawCheckBullet(canvas: Canvas, x: Float, y: Float, text: String, paint: Paint, successColor: Int) {
        val originalColor = paint.color
        val originalBold = paint.isFakeBoldText
        
        // Draw green checkmark
        paint.color = successColor
        paint.isFakeBoldText = true
        canvas.drawText("✔", x, y, paint)
        
        // Draw text
        paint.color = originalColor
        paint.isFakeBoldText = originalBold
        canvas.drawText(text, x + 20f, y, paint)
    }
    
    private fun drawFooter(canvas: Canvas, pageNumber: Int, paint: Paint, textColor: Int) {
        paint.color = textColor
        paint.textSize = 9f
        paint.isFakeBoldText = false
        canvas.drawText("Manuel de l'utilisateur ScolaPay - Page $pageNumber sur 9", 50f, 800f, paint)
        canvas.drawText("Propriété Exclusive de ScolaPay • Tous droits réservés", 330f, 800f, paint)
    }
    
    private fun drawRegistrationFormMockup(canvas: Canvas, x: Float, y: Float, primaryColor: Int, accentColor: Int, paint: Paint) {
        // Draw Phone/Card Outline
        paint.color = 0xFF374151.toInt()
        val phoneOuter = RectF(x, y, x + 210f, y + 300f)
        canvas.drawRoundRect(phoneOuter, 16f, 16f, paint)
        
        paint.color = 0xFFF9FAFB.toInt()
        val phoneInner = RectF(x + 6f, y + 12f, x + 204f, y + 294f)
        canvas.drawRoundRect(phoneInner, 12f, 12f, paint)
        
        // Header in Phone
        paint.color = primaryColor
        val screenHeader = RectF(x + 6f, y + 12f, x + 204f, y + 45f)
        canvas.drawRect(screenHeader, paint)
        
        paint.color = 0xFFFFFFFF.toInt()
        paint.textSize = 10f
        paint.isFakeBoldText = true
        canvas.drawText("S'inscrire sur ScolaPay", x + 15f, y + 32f, paint)
        
        // Fields in phone mockup
        paint.color = 0xFFD1D5DB.toInt()
        paint.isFakeBoldText = false
        
        // 1. Email field
        canvas.drawRoundRect(RectF(x + 15f, y + 55f, x + 195f, y + 75f), 4f, 4f, paint)
        paint.color = textColorDark
        paint.textSize = 7f
        canvas.drawText("Email de l'école (ex: ecole@mail.com)", x + 20f, y + 67f, paint)
        
        // 2. Founder Password Field
        paint.color = 0xFFD1D5DB.toInt()
        canvas.drawRoundRect(RectF(x + 15f, y + 85f, x + 195f, y + 105f), 4f, 4f, paint)
        paint.color = textColorDark
        canvas.drawText("Mot de passe Fondateur", x + 20f, y + 97f, paint)
        
        // 3. Confirm Founder Password
        paint.color = 0xFFD1D5DB.toInt()
        canvas.drawRoundRect(RectF(x + 15f, y + 110f, x + 195f, y + 130f), 4f, 4f, paint)
        paint.color = textColorDark
        canvas.drawText("Confirmer Mot de passe Fondateur", x + 20f, y + 122f, paint)
        
        // 4. Financial Password
        paint.color = 0xFFD1D5DB.toInt()
        canvas.drawRoundRect(RectF(x + 15f, y + 140f, x + 195f, y + 160f), 4f, 4f, paint)
        paint.color = textColorDark
        canvas.drawText("Mot de passe Financier", x + 20f, y + 152f, paint)
        
        // 5. Confirm Financial Password
        paint.color = 0xFFD1D5DB.toInt()
        canvas.drawRoundRect(RectF(x + 15f, y + 165f, x + 195f, y + 185f), 4f, 4f, paint)
        paint.color = textColorDark
        canvas.drawText("Confirmer Mot de passe Financier", x + 20f, y + 177f, paint)
        
        // Trial Period badge
        paint.color = 0xFFFEF3C7.toInt() // Amber light
        canvas.drawRoundRect(RectF(x + 15f, y + 200f, x + 195f, y + 235f), 6f, 6f, paint)
        
        paint.color = 0xFFD97706.toInt() // Amber dark text
        paint.textSize = 7.5f
        paint.isFakeBoldText = true
        canvas.drawText("🎁 Période d'évaluation offerte !", x + 25f, y + 215f, paint)
        paint.textSize = 6.5f
        paint.isFakeBoldText = false
        canvas.drawText("Test complet de ScolaPay sans engagement", x + 25f, y + 227f, paint)
        
        // Register Button Mockup
        paint.color = primaryColor
        canvas.drawRoundRect(RectF(x + 15f, y + 248f, x + 195f, y + 278f), 6f, 6f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        paint.textSize = 9f
        paint.isFakeBoldText = true
        canvas.drawText("Créer le compte de l'école", x + 40f, y + 267f, paint)
    }
    
    private fun drawPageHeader(canvas: Canvas, title: String, primaryColor: Int, paint: Paint) {
        paint.color = primaryColor
        paint.textSize = 16f
        paint.isFakeBoldText = true
        canvas.drawText(title, 50f, 55f, paint)
        
        // Draw decorative thin line
        paint.strokeWidth = 1f
        paint.color = 0xFFD1D5DB.toInt()
        canvas.drawLine(50f, 65f, 545f, 65f, paint)
    }
    
    private fun drawFlowDiagram(canvas: Canvas, x: Float, y: Float, primaryColor: Int, accentColor: Int, successColor: Int, paint: Paint) {
        // Draw light background container
        paint.color = 0xFFF3F4F6.toInt()
        val container = RectF(x, y, x + 495f, y + 120f)
        canvas.drawRoundRect(container, 10f, 10f, paint)
        
        // Draw step 1 circle
        paint.color = primaryColor
        canvas.drawCircle(x + 70f, y + 60f, 30f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("1. ÉLÈVE", x + 48f, y + 64f, paint)
        
        // Draw Step 2 circle
        paint.color = accentColor
        canvas.drawCircle(x + 247f, y + 60f, 30f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawText("2. PAYE", x + 225f, y + 64f, paint)
        
        // Draw Step 3 circle
        paint.color = successColor
        canvas.drawCircle(x + 425f, y + 60f, 30f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawText("3. STATS", x + 402f, y + 64f, paint)
        
        // Connectors (Arrows)
        paint.color = 0xFF9CA3AF.toInt()
        paint.strokeWidth = 3f
        canvas.drawLine(x + 110f, y + 60f, x + 200f, y + 60f, paint)
        canvas.drawLine(x + 287f, y + 60f, x + 380f, y + 60f, paint)
        
        // Arrow heads
        val path1 = Path().apply {
            moveTo(x + 205f, y + 60f)
            lineTo(x + 195f, y + 55f)
            lineTo(x + 195f, y + 65f)
            close()
        }
        canvas.drawPath(path1, paint)
        
        val path2 = Path().apply {
            moveTo(x + 385f, y + 60f)
            lineTo(x + 375f, y + 55f)
            lineTo(x + 375f, y + 65f)
            close()
        }
        canvas.drawPath(path2, paint)
        
        // Text under diagrams
        paint.color = 0xFF4B5563.toInt()
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText("Configuration de l'élève", x + 20f, y + 105f, paint)
        canvas.drawText("Enregistrement des Frais", x + 195f, y + 105f, paint)
        canvas.drawText("Rapports Trésorerie", x + 380f, y + 105f, paint)
    }
    
    private fun drawLogoUploadMockup(canvas: Canvas, x: Float, y: Float, primaryColor: Int, paint: Paint) {
        // Draw Phone Outline
        paint.color = 0xFF374151.toInt()
        val phoneOuter = RectF(x, y, x + 210f, y + 300f)
        canvas.drawRoundRect(phoneOuter, 16f, 16f, paint)
        
        paint.color = 0xFFF9FAFB.toInt()
        val phoneInner = RectF(x + 6f, y + 12f, x + 204f, y + 294f)
        canvas.drawRoundRect(phoneInner, 12f, 12f, paint)
        
        // Header in Phone
        paint.color = primaryColor
        val screenHeader = RectF(x + 6f, y + 12f, x + 204f, y + 45f)
        canvas.drawRect(screenHeader, paint)
        
        paint.color = 0xFFFFFFFF.toInt()
        paint.textSize = 10f
        paint.isFakeBoldText = true
        canvas.drawText("Configuration Générale", x + 15f, y + 32f, paint)
        
        // Configuration Card
        paint.color = 0xFFF3F4F6.toInt()
        val configCard = RectF(x + 12f, y + 60f, x + 198f, y + 150f)
        canvas.drawRoundRect(configCard, 8f, 8f, paint)
        
        // Inside Card: Logo block
        paint.color = 0xFFFFFFFF.toInt()
        val logoBox = RectF(x + 20f, y + 75f, x + 60f, y + 115f)
        canvas.drawRoundRect(logoBox, 4f, 4f, paint)
        
        // Logo symbol
        paint.color = primaryColor
        paint.textSize = 12f
        canvas.drawText("🏫", x + 30f, y + 100f, paint)
        
        // Text inside config card
        paint.color = textColorDark
        paint.textSize = 8f
        paint.isFakeBoldText = true
        canvas.drawText("Logo de l'école", x + 70f, y + 85f, paint)
        paint.color = 0xFF6B7280.toInt()
        paint.isFakeBoldText = false
        paint.textSize = 6f
        canvas.drawText("S'affiche sur vos factures PDF", x + 70f, y + 95f, paint)
        
        // Buttons: Importer / Supprimer
        paint.color = primaryColor
        val importBtn = RectF(x + 70f, y + 105f, x + 120f, y + 122f)
        canvas.drawRoundRect(importBtn, 4f, 4f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        paint.textSize = 6.5f
        paint.isFakeBoldText = true
        canvas.drawText("Changer", x + 80f, y + 116f, paint)
        
        paint.color = 0xFFEF4444.toInt() // Red delete button
        val deleteBtn = RectF(x + 125f, y + 105f, x + 175f, y + 122f)
        canvas.drawRoundRect(deleteBtn, 4f, 4f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawText("Supprimer", x + 133f, y + 116f, paint)
        
        // Classes List preview below logo section
        paint.color = 0xFFFFFFFF.toInt()
        val classesCard = RectF(x + 12f, y + 165f, x + 198f, y + 280f)
        canvas.drawRoundRect(classesCard, 8f, 8f, paint)
        
        paint.color = textColorDark
        paint.textSize = 8f
        paint.isFakeBoldText = true
        canvas.drawText("Classes de l'école", x + 20f, y + 180f, paint)
        
        // Fake classes rows
        paint.color = 0xFFE5E7EB.toInt()
        canvas.drawRect(x + 20f, y + 192f, x + 190f, y + 193f, paint)
        paint.color = textColorDark
        paint.isFakeBoldText = false
        paint.textSize = 7f
        canvas.drawText("7ème Année (Tarif: 400 000 GNF)", x + 20f, y + 207f, paint)
        
        paint.color = 0xFFE5E7EB.toInt()
        canvas.drawRect(x + 20f, y + 217f, x + 190f, y + 218f, paint)
        paint.color = textColorDark
        canvas.drawText("8ème Année (Tarif: 450 000 GNF)", x + 20f, y + 232f, paint)
        
        paint.color = 0xFFE5E7EB.toInt()
        canvas.drawRect(x + 20f, y + 242f, x + 190f, y + 243f, paint)
        paint.color = textColorDark
        canvas.drawText("9ème Année (Tarif: 500 000 GNF)", x + 20f, y + 257f, paint)
    }
    
    private fun drawQuickAccessMockup(canvas: Canvas, x: Float, y: Float, primaryColor: Int, accentColor: Int, paint: Paint) {
        // Draw Phone Outline
        paint.color = 0xFF374151.toInt()
        val phoneOuter = RectF(x, y, x + 210f, y + 300f)
        canvas.drawRoundRect(phoneOuter, 16f, 16f, paint)
        
        paint.color = 0xFFF9FAFB.toInt()
        val phoneInner = RectF(x + 6f, y + 12f, x + 204f, y + 294f)
        canvas.drawRoundRect(phoneInner, 12f, 12f, paint)
        
        // Title Accès Rapides in phone
        paint.color = textColorDark
        paint.textSize = 10f
        paint.isFakeBoldText = true
        canvas.drawText("Accès rapides", x + 15f, y + 40f, paint)
        
        // Create 2x2 Grid mockup
        val items = listOf(
            Pair("Élèves", 0xFFEFF6FF.toInt()),
            Pair("Paiements", 0xFFECFDF5.toInt()),
            Pair("Dépenses", 0xFFFFFBEB.toInt()),
            Pair("Inscription", 0xFFFDF2F8.toInt()) // Highlighted Pink button!
        )
        
        val colorsText = listOf(
            0xFF3B82F6.toInt(),
            0xFF10B981.toInt(),
            0xFFF59E0B.toInt(),
            0xFFEC4899.toInt() // Pink text/icon
        )
        
        var cellX = x + 15f
        var cellY = y + 60f
        
        for (i in 0 until 4) {
            paint.color = items[i].second
            val cellRect = RectF(cellX, cellY, cellX + 80f, cellY + 70f)
            
            // Draw special neon stroke for 'Inscription' button!
            if (i == 3) {
                canvas.drawRoundRect(cellRect, 8f, 8f, paint)
                paint.color = accentColor
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2f
                canvas.drawRoundRect(cellRect, 8f, 8f, paint)
                paint.style = Paint.Style.FILL
            } else {
                canvas.drawRoundRect(cellRect, 8f, 8f, paint)
            }
            
            // Draw fake icon circle
            paint.color = colorsText[i]
            canvas.drawCircle(cellX + 40f, cellY + 25f, 12f, paint)
            paint.color = 0xFFFFFFFF.toInt()
            paint.textSize = 10f
            paint.isFakeBoldText = true
            if (i == 3) {
                canvas.drawText("+", cellX + 37f, cellY + 29f, paint)
            } else {
                canvas.drawText("●", cellX + 37f, cellY + 29f, paint)
            }
            
            // Draw text
            paint.color = textColorDark
            paint.textSize = 8f
            paint.isFakeBoldText = i == 3
            canvas.drawText(items[i].first, cellX + 22f, cellY + 52f, paint)
            
            // Adjust coordinates for grid
            if (i == 1) {
                cellX = x + 15f
                cellY += 85f
            } else {
                cellX += 98f
            }
        }
        
        // Pointing Arrow Illustration
        paint.color = accentColor
        paint.strokeWidth = 2f
        paint.style = Paint.Style.STROKE
        
        // Draw decorative arrow pointing from left to the 'Inscription' button
        canvas.drawCircle(x + 138f, cellY + 35f, 22f, paint)
        paint.style = Paint.Style.FILL
        canvas.drawText("CLIQUEZ ICI !", x + 50f, cellY + 80f, paint)
    }
    
    private fun drawCashInGraphics(canvas: Canvas, x: Float, y: Float, accentColor: Int, primaryColor: Int, paint: Paint) {
        // Container
        paint.color = 0xFFF9FAFB.toInt()
        val container = RectF(x, y, x + 495f, y + 100f)
        canvas.drawRoundRect(container, 10f, 10f, paint)
        
        // Borders
        paint.style = Paint.Style.STROKE
        paint.color = 0xFFE5E7EB.toInt()
        paint.strokeWidth = 1.5f
        canvas.drawRoundRect(container, 10f, 10f, paint)
        
        paint.style = Paint.Style.FILL
        
        // Text inside graphics
        paint.color = accentColor
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("💸 FLUX DE COMPTABILITÉ SÉPARÉE", x + 20f, y + 30f, paint)
        
        paint.color = textColorDark
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText("• Les encaissements d'Inscription & Réinscription vont au registre : INSCRIPTIONS", x + 20f, y + 55f, paint)
        canvas.drawText("• Les encaissements mensuels ou annuels vont au registre : SCOLARITÉ STANDARD", x + 20f, y + 75f, paint)
    }
    
    private fun drawAlertDialogMockup(canvas: Canvas, x: Float, y: Float, primaryColor: Int, paint: Paint) {
        // Draw Phone Outline
        paint.color = 0xFF374151.toInt()
        val phoneOuter = RectF(x, y, x + 210f, y + 300f)
        canvas.drawRoundRect(phoneOuter, 16f, 16f, paint)
        
        paint.color = 0xFF9CA3AF.toInt() // Grayed-out background during dialog
        val phoneInner = RectF(x + 6f, y + 12f, x + 204f, y + 294f)
        canvas.drawRoundRect(phoneInner, 12f, 12f, paint)
        
        // Dialog Container
        paint.color = 0xFFFFFFFF.toInt()
        val dialogRect = RectF(x + 20f, y + 80f, x + 190f, y + 230f)
        canvas.drawRoundRect(dialogRect, 12f, 12f, paint)
        
        // Warning Icon or indicator
        paint.color = 0xFFEF4444.toInt() // Red warning color
        canvas.drawCircle(x + 105f, y + 110f, 16f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("⚠️", x + 97f, y + 115f, paint)
        
        // Title
        paint.color = textColorDark
        paint.textSize = 8.5f
        paint.isFakeBoldText = true
        canvas.drawText("Avertissement de Sécurité", x + 48f, y + 142f, paint)
        
        // Body text
        paint.color = 0xFF4B5563.toInt()
        paint.textSize = 6.5f
        paint.isFakeBoldText = false
        canvas.drawText("Êtes-vous sûr de vouloir supprimer", x + 35f, y + 160f, paint)
        canvas.drawText("définitivement ce versement de", x + 40f, y + 172f, paint)
        paint.color = 0xFFEF4444.toInt()
        paint.isFakeBoldText = true
        canvas.drawText("300 000 GNF de Divine Grâce ?", x + 37f, y + 184f, paint)
        
        // Buttons
        // Confirm Button
        paint.color = 0xFFEF4444.toInt()
        val confirmBtn = RectF(x + 110f, y + 198f, x + 180f, y + 218f)
        canvas.drawRoundRect(confirmBtn, 4f, 4f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        paint.textSize = 6.5f
        paint.isFakeBoldText = true
        canvas.drawText("Supprimer", x + 128f, y + 211f, paint)
        
        // Cancel Button
        paint.color = 0xFFE5E7EB.toInt()
        val cancelBtn = RectF(x + 30f, y + 198f, x + 100f, y + 218f)
        canvas.drawRoundRect(cancelBtn, 4f, 4f, paint)
        paint.color = textColorDark
        paint.isFakeBoldText = false
        canvas.drawText("Annuler", x + 53f, y + 211f, paint)
    }

    private fun drawCallParentMockup(canvas: Canvas, x: Float, y: Float, primaryColor: Int, paint: Paint) {
        // Draw Phone Outline
        paint.color = 0xFF374151.toInt()
        val phoneOuter = RectF(x, y, x + 210f, y + 300f)
        canvas.drawRoundRect(phoneOuter, 16f, 16f, paint)
        
        paint.color = 0xFFF9FAFB.toInt()
        val phoneInner = RectF(x + 6f, y + 12f, x + 204f, y + 294f)
        canvas.drawRoundRect(phoneInner, 12f, 12f, paint)
        
        // Header in Phone
        paint.color = primaryColor
        val screenHeader = RectF(x + 6f, y + 12f, x + 204f, y + 45f)
        canvas.drawRect(screenHeader, paint)
        
        paint.color = 0xFFFFFFFF.toInt()
        paint.textSize = 10f
        paint.isFakeBoldText = true
        canvas.drawText("Fiche Élève : Divine", x + 15f, y + 32f, paint)
        
        // Student Info Card
        paint.color = 0xFFFFFFFF.toInt()
        val studentCard = RectF(x + 12f, y + 60f, x + 198f, y + 160f)
        canvas.drawRoundRect(studentCard, 8f, 8f, paint)
        
        paint.color = textColorDark
        paint.textSize = 9f
        paint.isFakeBoldText = true
        canvas.drawText("Divine Grâce TOLNO", x + 20f, y + 80f, paint)
        paint.color = 0xFF6B7280.toInt()
        paint.textSize = 7.5f
        paint.isFakeBoldText = false
        canvas.drawText("Classe: 6ème Année  |  Sec: Primaire", x + 20f, y + 95f, paint)
        canvas.drawText("Solde Dû : 450 000 GNF", x + 20f, y + 110f, paint)
        canvas.drawText("Parent: +224 628 37 65 66", x + 20f, y + 125f, paint)
        
        // Buttons row
        // Call Parent Button (Primary)
        paint.color = primaryColor
        val callBtn = RectF(x + 20f, y + 135f, x + 102f, y + 152f)
        canvas.drawRoundRect(callBtn, 4f, 4f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        paint.textSize = 6.5f
        paint.isFakeBoldText = true
        canvas.drawText("📞 Appeler", x + 38f, y + 146f, paint)
        
        // WhatsApp Button
        paint.color = 0xFF25D366.toInt() // WhatsApp Green
        val waBtn = RectF(x + 108f, y + 135f, x + 190f, y + 152f)
        canvas.drawRoundRect(waBtn, 4f, 4f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawText("💬 Relancer", x + 125f, y + 146f, paint)
        
        // Activity/Payments list below
        paint.color = 0xFFFFFFFF.toInt()
        val listCard = RectF(x + 12f, y + 170f, x + 198f, y + 280f)
        canvas.drawRoundRect(listCard, 8f, 8f, paint)
        
        paint.color = textColorDark
        paint.textSize = 8f
        paint.isFakeBoldText = true
        canvas.drawText("Derniers Versements", x + 20f, y + 185f, paint)
        
        paint.color = 0xFF10B981.toInt() // Success color
        paint.textSize = 7f
        paint.isFakeBoldText = true
        canvas.drawText("+ 300 000 GNF", x + 20f, y + 205f, paint)
        paint.color = 0xFF6B7280.toInt()
        paint.isFakeBoldText = false
        canvas.drawText("15 Juil 2026 - Scolarité", x + 20f, y + 215f, paint)
        
        paint.color = 0xFF10B981.toInt()
        paint.isFakeBoldText = true
        canvas.drawText("+ 150 000 GNF", x + 20f, y + 240f, paint)
        paint.color = 0xFF6B7280.toInt()
        paint.isFakeBoldText = false
        canvas.drawText("01 Juil 2026 - Inscription", x + 20f, y + 255f, paint)
    }
}
