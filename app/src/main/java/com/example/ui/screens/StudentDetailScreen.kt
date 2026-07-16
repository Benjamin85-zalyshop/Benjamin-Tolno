package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.SchoolViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.material.icons.filled.Download

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailScreen(
    studentId: Int,
    viewModel: SchoolViewModel,
    onNavigateBack: () -> Unit,
    onAddPayment: (Int, String) -> Unit
) {
    val students by viewModel.students.collectAsStateWithLifecycle()
    val allPayments by viewModel.payments.collectAsStateWithLifecycle()
    val userRole by viewModel.userRole.collectAsStateWithLifecycle()
    val schoolName by viewModel.schoolName.collectAsStateWithLifecycle()
    val schoolLogoBase64 by viewModel.schoolLogoBase64.collectAsStateWithLifecycle()
    val deletionRequests by viewModel.deletionRequests.collectAsStateWithLifecycle()
    val classFees by viewModel.classFees.collectAsStateWithLifecycle()
    
    val student = students.find { it.id == studentId }
    val studentPayments = allPayments.filter { it.studentId == studentId }.sortedByDescending { it.date }
    
    val numberFormat = NumberFormat.getNumberInstance(Locale("fr", "GN"))
    val totalPaid = studentPayments.filter { it.reason != "Inscription" && it.reason != "Réinscription" }.sumOf { it.amount }

    val context = LocalContext.current
    
    val hasPendingRequest = remember(deletionRequests, student?.remoteId) {
        student != null && deletionRequests.any { it.studentRemoteId == student.remoteId }
    }
    
    var showDirectDeleteDialog by remember { mutableStateOf(false) }
    var showRequestDeleteDialog by remember { mutableStateOf(false) }
    var deletionReason by remember { mutableStateOf("") }
    var paymentToDelete by remember { mutableStateOf<com.example.data.models.Payment?>(null) }

    val exportPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf"),
        onResult = { uri ->
            uri?.let {
                if (student != null) {
                    val studentClassFee = classFees.find { it.grade == student.grade }?.feeAmount ?: 0L
                    generatePdf(context, student, studentPayments, schoolName ?: "", studentClassFee, schoolLogoBase64, it)
                    Toast.makeText(context, "PDF généré avec succès", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    if (student == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Détails") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Élève introuvable.")
            }
        }
        return
    }

    val fullName = "${student.firstName} ${student.lastName}"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(fullName) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val fileName = "Paiements_${student.firstName}_${student.lastName}.pdf"
                        exportPdfLauncher.launch(fileName)
                    }) {
                        Icon(Icons.Filled.Download, contentDescription = "Télécharger PDF")
                    }
                    if (userRole == "FOUNDER") {
                        IconButton(onClick = { showDirectDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Supprimer l'élève", tint = MaterialTheme.colorScheme.error)
                        }
                    } else if (userRole == "FINANCIER") {
                        IconButton(
                            onClick = { 
                                if (!hasPendingRequest) {
                                    showRequestDeleteDialog = true 
                                } else {
                                    Toast.makeText(context, "Une demande de suppression est déjà en cours de validation.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(
                                Icons.Filled.Delete, 
                                contentDescription = "Demander la suppression",
                                tint = if (hasPendingRequest) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (userRole == "FINANCIER") {
                ExtendedFloatingActionButton(
                    onClick = { onAddPayment(studentId, fullName) },
                    icon = { Icon(Icons.Filled.Payment, contentDescription = "Payer") },
                    text = { Text("Nouveau Paiement") }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (hasPendingRequest) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "⚠️ Demande de suppression en cours",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val req = deletionRequests.find { it.studentRemoteId == student.remoteId }
                            val reasonText = req?.reason ?: ""
                            Text(
                                text = "Une demande de suppression a été envoyée au fondateur pour cet élève. " +
                                        if (reasonText.isNotEmpty()) "Motif : \"$reasonText\"" else "",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            item {
                val studentClassFee = classFees.find { it.grade == student.grade }?.feeAmount ?: 0L
                val remainingToPay = (studentClassFee - totalPaid).coerceAtLeast(0L)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Informations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Section : ${student.section}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("Classe : ${student.grade}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        if (student.registrationFee > 0L) {
                            Text("Frais d'inscription : ${numberFormat.format(student.registrationFee)} GNF", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        if (student.reenrollmentFee > 0L) {
                            Text("Frais de réinscription : ${numberFormat.format(student.reenrollmentFee)} GNF", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                        )

                        Text(
                            text = "Frais de classe : " + if (studentClassFee > 0L) "${numberFormat.format(studentClassFee)} GNF" else "Non défini",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Total payé : ${numberFormat.format(totalPaid)} GNF",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (studentClassFee > 0L) {
                            Spacer(modifier = Modifier.height(4.dp))
                            if (totalPaid >= studentClassFee) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD1FAE5)),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Text(
                                        text = "✓ Scolarité Réglée (Payé à 100%)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF065F46),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = "Reste à payer : ${numberFormat.format(remainingToPay)} GNF",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (!student.parentWhatsApp.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("WhatsApp parents : ${student.parentWhatsApp}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }

            if (!student.parentWhatsApp.isNullOrBlank()) {
                item {
                    val formattedWhatsApp = remember(student.parentWhatsApp) {
                        val clean = student.parentWhatsApp.replace(Regex("[^0-9+]"), "")
                        if (clean.startsWith("+")) {
                            clean
                        } else if (clean.startsWith("224")) {
                            "+$clean"
                        } else {
                            "+224$clean"
                        }
                    }
                    
                    val studentClassFee = classFees.find { it.grade == student.grade }?.feeAmount ?: 0L
                    val remainingToPay = (studentClassFee - totalPaid).coerceAtLeast(0L)
                    
                    val invoiceText = remember(student, studentPayments, totalPaid, schoolName, studentClassFee, remainingToPay) {
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("fr", "GN"))
                        val pList = if (studentPayments.isEmpty()) {
                            "Aucun paiement enregistré."
                        } else {
                            studentPayments.joinToString("\n") { p ->
                                "- ${sdf.format(Date(p.date))} : ${p.reason} -> ${numberFormat.format(p.amount)} GNF"
                            }
                        }
                        val finalSchoolName = schoolName?.ifBlank { "ScolaPay" } ?: "ScolaPay"
                        
                        val feesSection = if (studentClassFee > 0L) {
                            """
                            *Frais Scolaires Totaux :* ${numberFormat.format(studentClassFee)} GNF
                            *Total Payé :* ${numberFormat.format(totalPaid)} GNF
                            *Reste à Payer :* ${numberFormat.format(remainingToPay)} GNF ${if (remainingToPay == 0L) "✓ (Scolarité réglée)" else ""}
                            """.trimIndent()
                        } else {
                            "*Total Payé :* ${numberFormat.format(totalPaid)} GNF"
                        }

                        """
                        *$finalSchoolName - Reçu de Scolarité*
                        
                        *Élève :* ${student.firstName} ${student.lastName}
                        *Section :* ${student.section}
                        *Classe :* ${student.grade}
                        
                        *Historique des paiements :*
                        $pList
                        
                        ----------------------------------
                        $feesSection
                        ----------------------------------
                        
                        _Généré par ScolaPay pour ${schoolName?.ifBlank { "votre école" } ?: "votre école"}_
                        """.trimIndent()
                    }
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    try {
                                        val uri = android.net.Uri.parse("https://api.whatsapp.com/send?phone=$formattedWhatsApp")
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Impossible d'ouvrir WhatsApp", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF25D366))
                            ) {
                                Text("Contacter", maxLines = 1)
                            }
                            
                            Button(
                                onClick = {
                                    try {
                                        val encodedText = java.net.URLEncoder.encode(invoiceText, "UTF-8")
                                        val uri = android.net.Uri.parse("https://api.whatsapp.com/send?phone=$formattedWhatsApp&text=$encodedText")
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Impossible d'ouvrir WhatsApp", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("Envoyer facture", maxLines = 1)
                            }
                        }
                        
                        Button(
                            onClick = {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                        data = android.net.Uri.parse("tel:$formattedWhatsApp")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Impossible d'ouvrir le téléphone", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Call,
                                contentDescription = "Appeler",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Appeler directement", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            item {
                Text("Historique des paiements", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            if (studentPayments.isEmpty()) {
                item {
                    Text("Aucun paiement enregistré.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(studentPayments) { payment ->
                    PaymentHistoryItem(
                        amount = payment.amount,
                        reason = payment.reason,
                        date = payment.date,
                        paymentMethod = payment.paymentMethod,
                        showDeleteAction = (userRole == "FINANCIER"),
                        onDelete = { paymentToDelete = payment }
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showDirectDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDirectDeleteDialog = false },
            title = { Text("Supprimer l'élève ?") },
            text = { Text("Voulez-vous supprimer définitivement ${student.firstName} ${student.lastName} ? Cette action supprimera également tout l'historique de ses paiements et reçus.") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        showDirectDeleteDialog = false
                        viewModel.deleteStudentDirectly(student)
                        Toast.makeText(context, "Élève supprimé définitivement", Toast.LENGTH_SHORT).show()
                        onNavigateBack()
                    }
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDirectDeleteDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showRequestDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showRequestDeleteDialog = false },
            title = { Text("Demander la suppression") },
            text = {
                Column {
                    Text("En tant que financier, vous ne pouvez pas supprimer un élève directement pour garantir la crédibilité du nombre d'élèves.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Veuillez saisir le motif de votre demande de suppression afin que le fondateur puisse la valider :", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = deletionReason,
                        onValueChange = { deletionReason = it },
                        label = { Text("Motif de la demande") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = deletionReason.isNotBlank(),
                    onClick = {
                        viewModel.createDeletionRequest(student, deletionReason)
                        showRequestDeleteDialog = false
                        deletionReason = ""
                        Toast.makeText(context, "Demande de suppression envoyée au fondateur", Toast.LENGTH_LONG).show()
                    }
                ) {
                    Text("Envoyer la demande")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRequestDeleteDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (paymentToDelete != null) {
        AlertDialog(
            onDismissRequest = { paymentToDelete = null },
            title = { Text("Avertissement : Supprimer le paiement") },
            text = {
                val formattedAmount = numberFormat.format(paymentToDelete?.amount ?: 0L)
                Text("Attention ! Êtes-vous sûr de vouloir supprimer définitivement ce paiement de $formattedAmount GNF (${paymentToDelete?.reason ?: ""}) ? Cette action est irréversible et affectera le solde de l'élève.")
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        paymentToDelete?.let {
                            viewModel.deletePayment(it.id)
                            Toast.makeText(context, "Paiement supprimé", Toast.LENGTH_SHORT).show()
                        }
                        paymentToDelete = null
                    }
                ) {
                    Text("Supprimer définitivement")
                }
            },
            dismissButton = {
                TextButton(onClick = { paymentToDelete = null }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun PaymentHistoryItem(amount: Long, reason: String, date: Long, paymentMethod: String, showDeleteAction: Boolean, onDelete: () -> Unit) {
    val numberFormat = NumberFormat.getNumberInstance(Locale("fr", "GN"))
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("fr", "GN"))
    
    val methodColor = when (paymentMethod) {
        "Espèces" -> Color(0xFF10B981)
        "Orange Money" -> Color(0xFFF97316)
        "Mobile Money" -> Color(0xFFEAB308)
        "ScolaPay" -> Color(0xFF0F56E3)
        "Virement" -> Color(0xFF6B7280)
        "Chèque" -> Color(0xFF8B5CF6)
        else -> Color(0xFF6B7280)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = reason, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = sdf.format(Date(date)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    Box(
                        modifier = Modifier
                            .background(methodColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = paymentMethod,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = methodColor
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${numberFormat.format(amount)} GNF",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (showDeleteAction) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Supprimer le paiement",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

fun generatePdf(
    context: android.content.Context,
    student: com.example.data.models.Student,
    payments: List<com.example.data.models.Payment>,
    schoolName: String,
    studentClassFee: Long,
    schoolLogoBase64: String?,
    uri: android.net.Uri
) {
    val pdfDocument = android.graphics.pdf.PdfDocument()
    val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
    var page = pdfDocument.startPage(pageInfo)
    var canvas = page.canvas
    val paint = android.graphics.Paint()
    
    val numberFormat = java.text.NumberFormat.getNumberInstance(java.util.Locale("fr", "GN"))
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale("fr", "GN"))
    
    // Draw Border on the page
    paint.style = android.graphics.Paint.Style.STROKE
    paint.strokeWidth = 2f
    paint.color = android.graphics.Color.DKGRAY
    canvas.drawRect(25f, 25f, 570f, 817f, paint)
    
    // Reset paint style for filled elements
    paint.style = android.graphics.Paint.Style.FILL
    
    // Draw School Logo if present
    if (!schoolLogoBase64.isNullOrBlank()) {
        try {
            val decodedBytes = android.util.Base64.decode(schoolLogoBase64, android.util.Base64.DEFAULT)
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            if (bitmap != null) {
                val destRect = android.graphics.RectF(465f, 40f, 545f, 120f)
                canvas.drawBitmap(bitmap, null, destRect, paint)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // School / Application Title
    paint.textSize = 24f
    paint.isFakeBoldText = true
    paint.color = android.graphics.Color.parseColor("#0F56E3") // Primary Blue
    canvas.drawText(schoolName.ifBlank { "ScolaPay" }, 50f, 65f, paint)
    
    // Document Title
    paint.textSize = 14f
    paint.isFakeBoldText = true
    paint.color = android.graphics.Color.BLACK
    canvas.drawText("FACTURE ET SITUATION DES PAIEMENTS", 50f, 90f, paint)
    
    // Subtitle / Date
    paint.textSize = 10f
    paint.isFakeBoldText = true
    paint.color = android.graphics.Color.BLACK
    canvas.drawText("Généré le : ${sdf.format(java.util.Date())}", 50f, 110f, paint)
    
    // Divider line below header
    paint.strokeWidth = 1f
    paint.color = android.graphics.Color.LTGRAY
    canvas.drawLine(50f, 125f, 545f, 125f, paint)
    
    // Left Column: Student Information
    paint.textSize = 12f
    paint.color = android.graphics.Color.BLACK
    paint.isFakeBoldText = true
    canvas.drawText("INFORMATIONS ÉLÈVE", 50f, 155f, paint)
    
    paint.isFakeBoldText = false
    canvas.drawText("Nom complet : ${student.firstName} ${student.lastName}", 50f, 180f, paint)
    canvas.drawText("Classe : ${student.grade}", 50f, 205f, paint)
    canvas.drawText("Section : ${student.section}", 50f, 230f, paint)
    
    // Right Column: Financial Details
    paint.isFakeBoldText = true
    canvas.drawText("DÉTAILS DES FRAIS", 320f, 155f, paint)
    
    paint.isFakeBoldText = false
    canvas.drawText("Frais de classe : ${numberFormat.format(studentClassFee)} GNF", 320f, 180f, paint)
    
    val totalPaid = payments.filter { it.reason != "Inscription" && it.reason != "Réinscription" }.sumOf { it.amount }
    canvas.drawText("Total payé : ${numberFormat.format(totalPaid)} GNF", 320f, 205f, paint)
    
    // Remaining balance Box
    val remaining = studentClassFee - totalPaid
    val isFullyPaid = remaining <= 0
    val boxColor = if (isFullyPaid) "#10B981" else "#E11D48" // Green or Red-rose
    val boxBgColor = if (isFullyPaid) "#ECFDF5" else "#FFF1F2"
    
    paint.style = android.graphics.Paint.Style.STROKE
    paint.strokeWidth = 1.5f
    paint.color = android.graphics.Color.parseColor(boxColor)
    canvas.drawRect(320f, 220f, 545f, 275f, paint)
    
    paint.style = android.graphics.Paint.Style.FILL
    paint.color = android.graphics.Color.parseColor(boxBgColor)
    canvas.drawRect(321f, 221f, 544f, 274f, paint)
    
    paint.color = android.graphics.Color.parseColor(boxColor)
    paint.textSize = 11f
    paint.isFakeBoldText = true
    canvas.drawText(if (isFullyPaid) "SITUATION : EN RÈGLE" else "RESTE À PAYER", 335f, 242f, paint)
    
    paint.textSize = 14f
    canvas.drawText("${numberFormat.format(remaining)} GNF", 335f, 263f, paint)
    
    // Reset paint properties
    paint.color = android.graphics.Color.BLACK
    
    // Divider before history
    paint.strokeWidth = 1f
    paint.color = android.graphics.Color.LTGRAY
    canvas.drawLine(50f, 295f, 545f, 295f, paint)
    
    // Payment History Table Title
    paint.style = android.graphics.Paint.Style.FILL
    paint.color = android.graphics.Color.BLACK
    paint.textSize = 13f
    paint.isFakeBoldText = true
    canvas.drawText("HISTORIQUE DES PAIEMENTS ENREGISTRÉS", 50f, 320f, paint)
    
    // Table Header setup
    val tableHeaderY = 335f
    val rowHeight = 25f
    
    // Header background
    paint.style = android.graphics.Paint.Style.FILL
    paint.color = android.graphics.Color.parseColor("#F3F4F6")
    canvas.drawRect(50f, tableHeaderY, 545f, tableHeaderY + rowHeight, paint)
    
    // Header borders
    paint.style = android.graphics.Paint.Style.STROKE
    paint.strokeWidth = 1f
    paint.color = android.graphics.Color.LTGRAY
    canvas.drawRect(50f, tableHeaderY, 545f, tableHeaderY + rowHeight, paint)
    
    // Header text
    paint.style = android.graphics.Paint.Style.FILL
    paint.color = android.graphics.Color.BLACK
    paint.textSize = 10f
    paint.isFakeBoldText = true
    canvas.drawText("Date", 60f, tableHeaderY + 17f, paint)
    canvas.drawText("Motif / Description", 180f, tableHeaderY + 17f, paint)
    canvas.drawText("Mode", 380f, tableHeaderY + 17f, paint)
    canvas.drawText("Montant", 470f, tableHeaderY + 17f, paint)
    
    var currentY = tableHeaderY + rowHeight
    paint.isFakeBoldText = false
    
    for (payment in payments) {
        if (currentY > 730f) {
            // If running out of space, draw a continuation indicator and open a new page
            paint.isFakeBoldText = true
            paint.color = android.graphics.Color.BLACK
            canvas.drawText("... Suite des paiements sur la page suivante ...", 50f, currentY + 17f, paint)
            
            pdfDocument.finishPage(page)
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            
            // Draw page border on the new page
            paint.style = android.graphics.Paint.Style.STROKE
            paint.strokeWidth = 2f
            paint.color = android.graphics.Color.DKGRAY
            canvas.drawRect(25f, 25f, 570f, 817f, paint)
            
            currentY = 50f
            
            // Draw Table Header on new page
            paint.style = android.graphics.Paint.Style.FILL
            paint.color = android.graphics.Color.parseColor("#F3F4F6")
            canvas.drawRect(50f, currentY, 545f, currentY + rowHeight, paint)
            
            paint.style = android.graphics.Paint.Style.STROKE
            paint.strokeWidth = 1f
            paint.color = android.graphics.Color.LTGRAY
            canvas.drawRect(50f, currentY, 545f, currentY + rowHeight, paint)
            
            paint.style = android.graphics.Paint.Style.FILL
            paint.color = android.graphics.Color.BLACK
            paint.textSize = 10f
            paint.isFakeBoldText = true
            canvas.drawText("Date", 60f, currentY + 17f, paint)
            canvas.drawText("Motif / Description", 180f, currentY + 17f, paint)
            canvas.drawText("Mode", 380f, currentY + 17f, paint)
            canvas.drawText("Montant", 470f, currentY + 17f, paint)
            
            currentY += rowHeight
            paint.isFakeBoldText = false
        }
        
        // Row background and borders
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 0.5f
        paint.color = android.graphics.Color.LTGRAY
        canvas.drawRect(50f, currentY, 545f, currentY + rowHeight, paint)
        
        paint.style = android.graphics.Paint.Style.FILL
        paint.color = android.graphics.Color.BLACK
        
        val dateStr = sdf.format(java.util.Date(payment.date))
        val amountStr = "${numberFormat.format(payment.amount)} GNF"
        
        canvas.drawText(dateStr, 60f, currentY + 17f, paint)
        
        val reasonStr = if (payment.reason.length > 25) payment.reason.substring(0, 22) + "..." else payment.reason
        canvas.drawText(reasonStr, 180f, currentY + 17f, paint)
        
        val methodColorHex = when (payment.paymentMethod) {
            "Espèces" -> "#10B981"
            "Orange Money" -> "#F97316"
            "Mobile Money" -> "#EAB308"
            "ScolaPay" -> "#0F56E3"
            "Virement" -> "#6B7280"
            "Chèque" -> "#8B5CF6"
            else -> "#6B7280"
        }
        paint.color = android.graphics.Color.parseColor(methodColorHex)
        paint.isFakeBoldText = true
        canvas.drawText(payment.paymentMethod, 380f, currentY + 17f, paint)
        
        paint.color = android.graphics.Color.BLACK
        paint.isFakeBoldText = false
        canvas.drawText(amountStr, 470f, currentY + 17f, paint)
        
        currentY += rowHeight
    }
    
    if (payments.isEmpty()) {
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 0.5f
        paint.color = android.graphics.Color.LTGRAY
        canvas.drawRect(50f, currentY, 545f, currentY + rowHeight * 2, paint)
        
        paint.style = android.graphics.Paint.Style.FILL
        paint.color = android.graphics.Color.BLACK
        paint.textSize = 10f
        paint.isFakeBoldText = true
        canvas.drawText("Aucun paiement enregistré pour le moment.", 180f, currentY + rowHeight + 5f, paint)
        currentY += rowHeight * 2
    } else {
        // Draw total summary row
        paint.style = android.graphics.Paint.Style.FILL
        paint.color = android.graphics.Color.parseColor("#F9FAFB")
        canvas.drawRect(50f, currentY, 545f, currentY + rowHeight, paint)
        
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = android.graphics.Color.LTGRAY
        canvas.drawRect(50f, currentY, 545f, currentY + rowHeight, paint)
        
        paint.style = android.graphics.Paint.Style.FILL
        paint.color = android.graphics.Color.BLACK
        paint.isFakeBoldText = true
        canvas.drawText("TOTAL PAYÉ", 60f, currentY + 17f, paint)
        canvas.drawText("${numberFormat.format(totalPaid)} GNF", 470f, currentY + 17f, paint)
    }
    
    // Always draw footer at the very bottom of the page
    paint.style = android.graphics.Paint.Style.STROKE
    paint.strokeWidth = 1f
    paint.color = android.graphics.Color.LTGRAY
    canvas.drawLine(50f, 755f, 545f, 755f, paint)
    
    paint.style = android.graphics.Paint.Style.FILL
    paint.textSize = 9.5f
    paint.color = android.graphics.Color.BLACK
    paint.isFakeBoldText = true
    canvas.drawText("Merci pour votre confiance. Reçu généré électroniquement par ScolaPay.", 50f, 785f, paint)
    canvas.drawText("Signature & Cachet de l'établissement", 355f, 785f, paint)
    
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
}
