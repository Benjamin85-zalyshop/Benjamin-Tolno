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
        canvas.drawText("Version 2.0 • Édition Spéciale ScolaPay", 50f, 200f, paint)
        
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
        drawCheckBullet(canvas, 70f, 555f, "Zéro Oubli : Relances WhatsApp pré-remplies et personnalisables", paint, successColor)
        
        // Bottom Illustration: Flow Diagram
        drawFlowDiagram(canvas, 50f, 630f, primaryColor, accentColor, successColor, paint)
        
        // Page footer
        drawFooter(canvas, 1, paint, textColorLight)
        pdfDocument.finishPage(page)
        
        // --- PAGE 2: INSCRIPTION & CRÉATION DE COMPTE ÉCOLE ---
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
        
        // --- PAGE 3: CONFIGURATION DES ELEVES ---
        page = pdfDocument.startPage(pageInfo)
        canvas = page.canvas
        paint = Paint().apply { isAntiAlias = true }
        
        drawPageHeader(canvas, "2. CONFIGURATION & INSCRIPTION DES ÉLÈVES", primaryColor, paint)
        
        paint.color = textColorDark
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("Définir les frais lors de la création d'un élève", 50f, 110f, paint)
        
        paint.textSize = 12f
        paint.isFakeBoldText = false
        paint.color = textColorLight
        canvas.drawText("Lorsqu'un élève arrive dans votre établissement, configurez ses frais", 50f, 135f, paint)
        canvas.drawText("d'inscription ou de réinscription. Ces montants restent optionnels et", 50f, 155f, paint)
        canvas.drawText("sont rattachés de manière unique à son dossier.", 50f, 175f, paint)
        
        // Illustrated Phone / Form Screen mockup
        drawFormScreenMockup(canvas, 310f, 210f, primaryColor, textColorDark, paint)
        
        // Guide steps (Left Column)
        yPos = 220f
        paint.color = primaryColor
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText("Étapes de configuration :", 50f, yPos, paint)
        yPos += 25f
        
        paint.color = textColorDark
        paint.isFakeBoldText = false
        paint.textSize = 11f
        
        val steps = listOf(
            "1. Rendez-vous dans la section \"Élèves\".",
            "2. Cliquez sur le bouton d'ajout (+).",
            "3. Renseignez le Nom, Prénom, Classe,",
            "   Section et le numéro WhatsApp du parent.",
            "4. Dans les champs optionnels dédiés :",
            "   • Frais d'inscription (ex: 150 000 GNF)",
            "   • Frais de réinscription (ex: 100 000 GNF)",
            "5. Validez l'inscription.",
            "6. L'élève est désormais prêt à payer."
        )
        for (step in steps) {
            canvas.drawText(step, 50f, yPos, paint)
            yPos += 20f
        }
        
        // Advice note card at the bottom
        paint.color = 0xFFFFFBEB.toInt() // Warning amber light
        val adviceCard = RectF(50f, 540f, 545f, 650f)
        canvas.drawRoundRect(adviceCard, 8f, 8f, paint)
        
        paint.color = 0xFFD97706.toInt() // Amber dark
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("💡 CONSEIL DE L'EXPERT :", 70f, 570f, paint)
        paint.color = textColorDark
        paint.isFakeBoldText = false
        paint.textSize = 11f
        canvas.drawText("Configurez toujours les frais d'inscription ou de réinscription dès la", 70f, 595f, paint)
        canvas.drawText("création de l'élève. ScolaPay les mémorisera automatiquement pour", 70f, 615f, paint)
        canvas.drawText("les paiements ultérieurs.", 70f, 635f, paint)
        
        drawFooter(canvas, 3, paint, textColorLight)
        pdfDocument.finishPage(page)
        
        // --- PAGE 4: ENCAISSEMENT & DISTINCTION ---
        page = pdfDocument.startPage(pageInfo)
        canvas = page.canvas
        paint = Paint().apply { isAntiAlias = true }
        
        drawPageHeader(canvas, "3. ENCAISSEMENT DES FRAIS SANS MÉLANGE", primaryColor, paint)
        
        paint.color = textColorDark
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("Le bouton \"Inscription\" dans vos Accès Rapides", 50f, 110f, paint)
        
        paint.textSize = 12f
        paint.isFakeBoldText = false
        paint.color = textColorLight
        canvas.drawText("Pour que vos frais d'inscription et de réinscription ne soient jamais", 50f, 135f, paint)
        canvas.drawText("mélangés aux mensualités ou frais de scolarité standards, ScolaPay", 50f, 155f, paint)
        canvas.drawText("intègre un module dédié, accessible directement depuis le Dashboard.", 50f, 175f, paint)
        
        // Draw Accès Rapides & Inscription Mockup on the right
        drawQuickAccessMockup(canvas, 310f, 210f, primaryColor, accentColor, paint)
        
        // Steps to Cash-In Inscription/Re-inscription (Left Column)
        yPos = 220f
        paint.color = primaryColor
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText("Comment encaisser ces frais :", 50f, yPos, paint)
        yPos += 25f
        
        paint.color = textColorDark
        paint.isFakeBoldText = false
        paint.textSize = 11f
        
        val cashSteps = listOf(
            "1. Allez sur l'Écran d'Accueil.",
            "2. Dans la grille \"Accès Rapides\", cliquez",
            "   sur le bouton rose \"Inscription\".",
            "3. Recherchez l'élève concerné.",
            "4. Sélectionnez le type de versement :",
            "   • Inscription",
            "   • Réinscription",
            "5. ScolaPay remplit automatiquement le",
            "   montant configuré à la création !",
            "6. Sélectionnez le moyen (Espèces, OM, MoMo).",
            "7. Validez. Le solde est mis à jour séparément !"
        )
        for (step in cashSteps) {
            canvas.drawText(step, 50f, yPos, paint)
            yPos += 19f
        }
        
        // Cash-in flow graphics at the bottom
        drawCashInGraphics(canvas, 50f, 520f, accentColor, primaryColor, paint)
        
        drawFooter(canvas, 4, paint, textColorLight)
        pdfDocument.finishPage(page)
        
        // --- PAGE 5: RAPPORTS & WHATSAPP RELANCES ---
        page = pdfDocument.startPage(pageInfo)
        canvas = page.canvas
        paint = Paint().apply { isAntiAlias = true }
        
        drawPageHeader(canvas, "4. RAPPORTS & RELANCES WHATSAPP", primaryColor, paint)
        
        paint.color = textColorDark
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("Trésorerie transparente et Communication directe", 50f, 110f, paint)
        
        paint.textSize = 12f
        paint.isFakeBoldText = false
        paint.color = textColorLight
        canvas.drawText("ScolaPay automatise le calcul de la trésorerie et la communication.", 50f, 135f, paint)
        canvas.drawText("Vous pouvez envoyer des relances professionnelles aux parents d'élèves", 50f, 155f, paint)
        canvas.drawText("et consulter l'état d'avancement global de votre établissement.", 50f, 175f, paint)
        
        // WhatsApp Phone Mockup on the right
        drawWhatsAppMockup(canvas, 310f, 210f, greenLight, textColorDark, paint)
        
        // Detailed Information (Left Column)
        yPos = 220f
        paint.color = primaryColor
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText("Fonctionnalités Clés :", 50f, yPos, paint)
        yPos += 25f
        
        paint.color = textColorDark
        paint.isFakeBoldText = false
        paint.textSize = 11f
        
        val keyFeatures = listOf(
            "📢 Relances de Scolarité :",
            "ScolaPay pré-remplit un message WhatsApp",
            "personnalisé avec le nom de l'élève, sa classe",
            "et le montant exact restant à payer. Il suffit",
            "d'un clic pour l'envoyer directement au parent.",
            "",
            "📊 Synthèse & Historiques :",
            "Consultez l'onglet \"Historique & Synthèse\"",
            "dans le menu Inscription pour voir séparément",
            "le montant total récolté pour les inscriptions",
            "et les réinscriptions en temps réel."
        )
        for (feature in keyFeatures) {
            canvas.drawText(feature, 50f, yPos, paint)
            yPos += 18f
        }
        
        // Support Center Card (Accent colors)
        paint.color = accentLight
        val supportCard = RectF(50f, 510f, 545f, 630f)
        canvas.drawRoundRect(supportCard, 10f, 10f, paint)
        
        paint.color = accentColor
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("📞 SUPPORT TECHNIQUE & DIRECT", 70f, 540f, paint)
        paint.color = textColorDark
        paint.isFakeBoldText = false
        paint.textSize = 11f
        canvas.drawText("Notre équipe technique est disponible pour toute assistance.", 70f, 565f, paint)
        canvas.drawText("Conseiller Technique principal : Benjamin Tolno", 70f, 585f, paint)
        paint.color = primaryColor
        paint.isFakeBoldText = true
        canvas.drawText("Numéro d'appel & WhatsApp : +224 628 37 65 66", 70f, 605f, paint)
        
        drawFooter(canvas, 5, paint, textColorLight)
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
        canvas.drawText("Manuel de l'utilisateur ScolaPay - Page $pageNumber sur 5", 50f, 800f, paint)
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
    
    private fun drawFormScreenMockup(canvas: Canvas, x: Float, y: Float, primaryColor: Int, textColorDark: Int, paint: Paint) {
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
        canvas.drawText("Ajouter un Élève", x + 15f, y + 32f, paint)
        
        // Form Fields
        paint.color = 0xFFD1D5DB.toInt()
        paint.isFakeBoldText = false
        
        // First Name Input
        canvas.drawRoundRect(RectF(x + 15f, y + 55f, x + 195f, y + 75f), 4f, 4f, paint)
        paint.color = textColorDark
        paint.textSize = 7f
        canvas.drawText("Prénom", x + 20f, y + 67f, paint)
        
        // Last Name Input
        paint.color = 0xFFD1D5DB.toInt()
        canvas.drawRoundRect(RectF(x + 15f, y + 85f, x + 195f, y + 105f), 4f, 4f, paint)
        paint.color = textColorDark
        canvas.drawText("Nom de famille", x + 20f, y + 97f, paint)
        
        // Grade Selection
        paint.color = 0xFFD1D5DB.toInt()
        canvas.drawRoundRect(RectF(x + 15f, y + 115f, x + 195f, y + 135f), 4f, 4f, paint)
        paint.color = textColorDark
        canvas.drawText("Classe Sélectionnée", x + 20f, y + 127f, paint)
        
        // Hotspot for custom Registration Fee
        paint.color = 0xFFFDF2F8.toInt() // hot pink background mockup
        canvas.drawRoundRect(RectF(x + 15f, y + 155f, x + 195f, y + 185f), 6f, 6f, paint)
        
        paint.color = 0xFFEC4899.toInt() // hot pink borders
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        canvas.drawRoundRect(RectF(x + 15f, y + 155f, x + 195f, y + 185f), 6f, 6f, paint)
        
        paint.style = Paint.Style.FILL
        paint.textSize = 7f
        paint.isFakeBoldText = true
        canvas.drawText("Frais d'inscription (Optionnel)", x + 20f, y + 167f, paint)
        paint.color = textColorDark
        paint.isFakeBoldText = false
        canvas.drawText("150 000 GNF", x + 20f, y + 178f, paint)
        
        // Hotspot for custom Re-enrollment Fee
        paint.color = 0xFFF3E8FF.toInt() // light purple
        canvas.drawRoundRect(RectF(x + 15f, y + 195f, x + 195f, y + 225f), 6f, 6f, paint)
        
        paint.color = 0xFF8B5CF6.toInt() // purple borders
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(RectF(x + 15f, y + 195f, x + 195f, y + 225f), 6f, 6f, paint)
        
        paint.style = Paint.Style.FILL
        paint.textSize = 7f
        paint.isFakeBoldText = true
        paint.color = 0xFF8B5CF6.toInt()
        canvas.drawText("Frais de réinscription (Optionnel)", x + 20f, y + 207f, paint)
        paint.color = textColorDark
        paint.isFakeBoldText = false
        canvas.drawText("100 000 GNF", x + 20f, y + 218f, paint)
        
        // Save Button Mockup
        paint.color = primaryColor
        canvas.drawRoundRect(RectF(x + 15f, y + 245f, x + 195f, y + 275f), 6f, 6f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        paint.textSize = 9f
        paint.isFakeBoldText = true
        canvas.drawText("Enregistrer l'élève", x + 65f, y + 264f, paint)
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
    
    private fun drawWhatsAppMockup(canvas: Canvas, x: Float, y: Float, greenLight: Int, textColorDark: Int, paint: Paint) {
        // Outer phone
        paint.color = 0xFF374151.toInt()
        val phoneOuter = RectF(x, y, x + 210f, y + 300f)
        canvas.drawRoundRect(phoneOuter, 16f, 16f, paint)
        
        // Screen
        paint.color = 0xFFE5DDD5.toInt() // WhatsApp beige background chat
        val phoneInner = RectF(x + 6f, y + 12f, x + 204f, y + 294f)
        canvas.drawRoundRect(phoneInner, 12f, 12f, paint)
        
        // Header in Phone
        paint.color = 0xFF075E54.toInt() // WhatsApp Teal Dark
        val screenHeader = RectF(x + 6f, y + 12f, x + 204f, y + 45f)
        canvas.drawRect(screenHeader, paint)
        
        paint.color = 0xFFFFFFFF.toInt()
        paint.textSize = 10f
        paint.isFakeBoldText = true
        canvas.drawText("WhatsApp - Parents d'élèves", x + 15f, y + 32f, paint)
        
        // Draw Message Bubble
        paint.color = greenLight
        val bubble = RectF(x + 15f, y + 65f, x + 195f, y + 195f)
        canvas.drawRoundRect(bubble, 8f, 8f, paint)
        
        paint.color = textColorDark
        paint.textSize = 7f
        paint.isFakeBoldText = false
        
        val lines = listOf(
            "📢 Rappel Frais de Scolarité",
            "Bonjour Chers Parents,",
            "Nous vous rappelons que le solde",
            "restant pour les frais de scolarité",
            "de Divine Grâce en classe de 6ème",
            "est de 450 000 GNF.",
            "",
            "Merci de régulariser au plus vite",
            "via ScolaPay.",
            "Cordialement, la Direction."
        )
        
        var messageY = y + 80f
        for (line in lines) {
            if (line.startsWith("📢")) {
                paint.isFakeBoldText = true
            } else {
                paint.isFakeBoldText = false
            }
            canvas.drawText(line, x + 22f, messageY, paint)
            messageY += 11f
        }
        
        // Double Checkmarks
        paint.color = 0xFF34B7F1.toInt() // WhatsApp blue check color
        paint.textSize = 9f
        paint.isFakeBoldText = true
        canvas.drawText("✔✔", x + 175f, y + 190f, paint)
    }
}
