package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

    val exportPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf"),
        onResult = { uri ->
            uri?.let {
                if (student != null) {
                    generatePdf(context, student, studentPayments, schoolName ?: "", it)
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
                        onDelete = { viewModel.deletePayment(payment.id) }
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
    uri: android.net.Uri
) {
    val pdfDocument = android.graphics.pdf.PdfDocument()
    val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
    var page = pdfDocument.startPage(pageInfo)
    var canvas = page.canvas
    val paint = android.graphics.Paint()
    
    // Draw text
    paint.textSize = 24f
    paint.isFakeBoldText = true
    canvas.drawText(schoolName.ifBlank { "ScolaPay" }, 50f, 75f, paint)
    
    paint.textSize = 14f
    paint.isFakeBoldText = false
    canvas.drawText("Reçu de paiement officiel - ScolaPay", 50f, 105f, paint)
    
    paint.textSize = 18f
    paint.isFakeBoldText = false
    canvas.drawText("Élève : ${student.firstName} ${student.lastName}", 50f, 145f, paint)
    canvas.drawText("Section : ${student.section}", 50f, 175f, paint)
    canvas.drawText("Classe : ${student.grade}", 50f, 205f, paint)
    
    val numberFormat = java.text.NumberFormat.getNumberInstance(java.util.Locale("fr", "GN"))
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale("fr", "GN"))
    
    var yPosition = 265f
    paint.textSize = 16f
    paint.isFakeBoldText = true
    canvas.drawText("Historique des paiements :", 50f, yPosition, paint)
    yPosition += 30f
    
    paint.isFakeBoldText = false
    var total = 0L
    for (payment in payments) {
        if (yPosition > 800f) {
            pdfDocument.finishPage(page)
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            yPosition = 50f
        }
        
        val dateStr = sdf.format(java.util.Date(payment.date))
        val amountStr = "${numberFormat.format(payment.amount)} GNF"
        
        val line = "- $dateStr : ${payment.reason} [${payment.paymentMethod}] -> $amountStr"
        canvas.drawText(line, 50f, yPosition, paint)
        yPosition += 30f
        total += payment.amount
    }
    
    yPosition += 20f
    if (yPosition > 800f) {
        pdfDocument.finishPage(page)
        page = pdfDocument.startPage(pageInfo)
        canvas = page.canvas
        yPosition = 50f
    }
    paint.isFakeBoldText = true
    canvas.drawText("Total payé : ${numberFormat.format(total)} GNF", 50f, yPosition, paint)
    
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
