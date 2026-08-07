package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ReceiptLong
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
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.example.ui.components.PhotoSourceDialog

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
    val grades by viewModel.grades.collectAsStateWithLifecycle(emptyList())
    val subjects by viewModel.subjects.collectAsStateWithLifecycle(emptyList())
    
    val student = students.find { it.id == studentId }
    val studentPayments = allPayments.filter { it.studentId == studentId }.sortedByDescending { it.date }
    
    val numberFormat = NumberFormat.getNumberInstance(Locale("fr", "GN"))
    val totalPaid = studentPayments.filter { it.reason != "Inscription" && it.reason != "Réinscription" }.sumOf { it.amount }

    val context = LocalContext.current
    
    val pendingRequest = remember(deletionRequests, student?.remoteId) {
        student?.let { s -> deletionRequests.find { it.studentRemoteId == s.remoteId && it.status == "PENDING" } }
    }
    val hasPendingRequest = pendingRequest != null
    
    val rejectedRequest = remember(deletionRequests, student?.remoteId) {
        student?.let { s -> deletionRequests.find { it.studentRemoteId == s.remoteId && it.status == "REJECTED" } }
    }
    
    var showDirectDeleteDialog by remember { mutableStateOf(false) }
    var showTicketDialog by remember { mutableStateOf(false) }
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

    val exportStudentCardLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf"),
        onResult = { uri ->
            uri?.let {
                if (student != null) {
                    val matricule = if (student.remoteId.length >= 5) student.remoteId.take(5).uppercase() else student.id.toString()
                    val studentClassFee = classFees.find { it.grade == student.grade }?.feeAmount ?: 0L
                    generateStudentIdCardPdf(
                        context, 
                        student, 
                        matricule, 
                        schoolName ?: "ÉCOLE", 
                        schoolLogoBase64, 
                        studentPayments,
                        studentClassFee,
                        grades,
                        subjects,
                        students,
                        it
                    )
                    Toast.makeText(context, "Carte Scolaire PDF générée avec succès", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    var showPhotoDialog by remember { mutableStateOf(false) }

    if (showPhotoDialog && student != null) {
        PhotoSourceDialog(
            onDismiss = { showPhotoDialog = false },
            onPhotoCaptured = { base64 ->
                viewModel.updateStudentPhoto(student, base64)
                val msg = if (base64 != null) "Photo de l'élève mise à jour" else "Photo de l'élève supprimée"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            },
            hasCurrentPhoto = !student.photoBase64.isNullOrBlank()
        )
    }

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
                        val fileName = "Carte_Scolaire_${student.firstName}_${student.lastName}.pdf"
                        exportStudentCardLauncher.launch(fileName)
                    }) {
                        Icon(Icons.Filled.Badge, contentDescription = "Carte Scolaire PDF")
                    }
                    IconButton(onClick = {
                        val fileName = "Paiements_${student.firstName}_${student.lastName}.pdf"
                        exportPdfLauncher.launch(fileName)
                    }) {
                        Icon(Icons.Filled.Download, contentDescription = "Télécharger Relevé PDF")
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
            if (rejectedRequest != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Demande de suppression rejetée",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = { viewModel.dismissDeletionRequest(rejectedRequest) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Fermer",
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Motif du fondateur : ${rejectedRequest.rejectionReason}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
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
                val matricule = if (student.remoteId.length >= 5) student.remoteId.take(5).uppercase() else student.id.toString()

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            val decodedBytes = remember(student.photoBase64) {
                                if (!student.photoBase64.isNullOrBlank()) {
                                    try { android.util.Base64.decode(student.photoBase64, android.util.Base64.DEFAULT) } catch (e: Exception) { null }
                                } else null
                            }
                            val photoBitmap = remember(decodedBytes) {
                                decodedBytes?.let { android.graphics.BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap() }
                            }

                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable { showPhotoDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                if (photoBitmap != null) {
                                    Image(
                                        bitmap = photoBitmap,
                                        contentDescription = "Photo de l'élève",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "Changer la photo",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text("+Photo", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Informations Élève", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text("Matricule : #$matricule", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                                Text("Section : ${student.section}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text("Classe : ${student.grade}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val fileName = "Carte_Scolaire_${student.firstName}_${student.lastName}.pdf"
                                exportStudentCardLauncher.launch(fileName)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Filled.Badge, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Générer Carte Scolaire PDF", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

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

                        val totalToPay = studentClassFee + student.registrationFee + student.reenrollmentFee
                        val currentTotalPaid = studentPayments.sumOf { it.amount } + student.registrationFee + student.reenrollmentFee
                        val remainingToPay = (totalToPay - currentTotalPaid).coerceAtLeast(0L)

                        Text(
                            text = "Frais de scolarité total : " + if (studentClassFee > 0L) "${numberFormat.format(totalToPay)} GNF" else "Non défini",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Total payé : ${numberFormat.format(currentTotalPaid)} GNF",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (studentClassFee > 0L) {
                            Spacer(modifier = Modifier.height(4.dp))
                            if (currentTotalPaid >= totalToPay) {
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
                                    color = Color(0xFFE11D48),
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
                        
                        Button(
                            onClick = { showTicketDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ReceiptLong,
                                contentDescription = "Ticket 58mm",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onTertiary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Voir ticket 58mm", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onTertiary)
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
                        onDelete = { paymentToDelete = payment },
                        onPrint = {
                            if (student != null) {
                                val classFee = classFees.find { it.grade == student.grade }?.feeAmount ?: 0L
                                com.example.ui.ReceiptPrinter.printReceipt(
                                    context,
                                    student,
                                    payment,
                                    schoolName ?: "",
                                    classFee
                                )
                            }
                        }
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showTicketDialog) {
        val studentClassFee = classFees.find { it.grade == student.grade }?.feeAmount ?: 0L
        val ticketTotalToPay = studentClassFee + student.registrationFee + student.reenrollmentFee
        val ticketTotalPaid = studentPayments.sumOf { it.amount } + student.registrationFee + student.reenrollmentFee
        val ticketRemaining = (ticketTotalToPay - ticketTotalPaid).coerceAtLeast(0L)
        val matricule = if (student.remoteId.length >= 5) student.remoteId.take(5).uppercase() else student.id.toString()
        
        Ticket58mmDialog(
            schoolName = schoolName ?: "",
            matricule = matricule,
            studentName = "${student.firstName} ${student.lastName}",
            studentGrade = student.grade,
            totalPaid = ticketTotalPaid,
            remaining = ticketRemaining,
            onDismiss = { showTicketDialog = false },
            onPrint = {
                com.example.ui.ReceiptPrinter.printSummaryTicket(
                    context,
                    schoolName ?: "",
                    matricule,
                    "${student.firstName} ${student.lastName}",
                    student.grade,
                    ticketTotalPaid,
                    ticketRemaining
                )
                showTicketDialog = false
            }
        )
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
fun PaymentHistoryItem(amount: Long, reason: String, date: Long, paymentMethod: String, showDeleteAction: Boolean, onDelete: () -> Unit, onPrint: () -> Unit = {}) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPrint) {
                        Icon(imageVector = Icons.Filled.Print, contentDescription = "Imprimer", tint = MaterialTheme.colorScheme.secondary)
                    }
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
    
    // Financial data for QR
    val qrTotalPaid = payments.sumOf { it.amount } + student.registrationFee
    val qrTotalToPay = studentClassFee + student.registrationFee
    val qrDue = (qrTotalToPay - qrTotalPaid).coerceAtLeast(0L)
    val qrPercent = if (qrTotalToPay > 0) (qrTotalPaid.toDouble() / qrTotalToPay.toDouble() * 100).toInt() else 100
    
    val formattedTotal = numberFormat.format(qrTotalToPay)
    val formattedPaid = numberFormat.format(qrTotalPaid)
    val formattedDue = numberFormat.format(qrDue)
    
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
                val destRect = android.graphics.RectF(395f, 45f, 465f, 115f)
                canvas.drawBitmap(bitmap, null, destRect, paint)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Draw QR Code on PDF Header
    try {
        val mat = if (student.remoteId.length >= 5) student.remoteId.take(5).uppercase() else student.id.toString()
        val qrData = com.example.ui.util.QrCodeUtils.buildStudentQrData(
            studentId = student.id,
            remoteId = mat,
            name = "${student.firstName} ${student.lastName}",
            grade = student.grade,
            section = student.section,
            totalFee = formattedTotal,
            paidFee = formattedPaid,
            dueFee = formattedDue,
            percent = qrPercent.toString()
        )
        val qrBmp = com.example.ui.util.QrCodeUtils.generateQrBitmap(qrData, 200)
        if (qrBmp != null) {
            val qrRect = android.graphics.RectF(475f, 45f, 545f, 115f)
            canvas.drawBitmap(qrBmp, null, qrRect, paint)
        }
    } catch (e: Exception) {
        e.printStackTrace()
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
    val matricule = if (student.remoteId.length >= 5) student.remoteId.take(5).uppercase() else student.id.toString()
    canvas.drawText("Matricule : #$matricule", 50f, 205f, paint)
    canvas.drawText("Classe : ${student.grade}", 50f, 230f, paint)
    canvas.drawText("Section : ${student.section}", 50f, 255f, paint)
    
    // Right Column: Financial Details
    paint.isFakeBoldText = true
    canvas.drawText("DÉTAILS DES FRAIS", 320f, 155f, paint)
    
    paint.isFakeBoldText = false
    var financialY = 180f
    
    if (student.registrationFee > 0L) {
        canvas.drawText("Frais d'inscription : ${numberFormat.format(student.registrationFee)} GNF", 320f, financialY, paint)
        financialY += 25f
    }
    if (student.reenrollmentFee > 0L) {
        canvas.drawText("Frais de réinscription : ${numberFormat.format(student.reenrollmentFee)} GNF", 320f, financialY, paint)
        financialY += 25f
    }
    
    canvas.drawText("Frais de classe : ${numberFormat.format(studentClassFee)} GNF", 320f, financialY, paint)
    financialY += 25f
    
    val paymentsSum = payments.sumOf { it.amount }
    val pdfTotalPaid = paymentsSum + student.registrationFee + student.reenrollmentFee
    canvas.drawText("Total payé : ${numberFormat.format(pdfTotalPaid)} GNF", 320f, financialY, paint)
    
    // Remaining balance Box
    val totalToPay = studentClassFee + student.registrationFee + student.reenrollmentFee
    val remaining = (totalToPay - pdfTotalPaid).coerceAtLeast(0L)
    val isFullyPaid = remaining <= 0L
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
        canvas.drawText("${numberFormat.format(pdfTotalPaid)} GNF", 470f, currentY + 17f, paint)
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

fun generateStudentIdCardPdf(
    context: android.content.Context,
    student: com.example.data.models.Student,
    matricule: String,
    schoolName: String,
    schoolLogoBase64: String?,
    payments: List<com.example.data.models.Payment>,
    studentClassFee: Long,
    grades: List<com.example.data.models.StudentGrade>,
    subjects: List<com.example.data.models.Subject>,
    studentsList: List<com.example.data.models.Student>,
    uri: android.net.Uri
) {
    val pdfDocument = android.graphics.pdf.PdfDocument()
    val cardWidth = 340
    val cardHeight = 215
    val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(cardWidth, cardHeight, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    val paint = android.graphics.Paint()
    paint.isAntiAlias = true

    // Background Card
    paint.color = android.graphics.Color.WHITE
    canvas.drawRect(0f, 0f, cardWidth.toFloat(), cardHeight.toFloat(), paint)

    // Header Bar Background
    val primaryColor = android.graphics.Color.parseColor("#0F56E3")
    paint.color = primaryColor
    canvas.drawRect(0f, 0f, cardWidth.toFloat(), 48f, paint)

    // School Name & Header Title
    paint.color = android.graphics.Color.WHITE
    paint.isFakeBoldText = true
    paint.textSize = 11f
    val schoolTitle = if (schoolName.isNotBlank()) schoolName.uppercase() else "ÉTABLISSEMENT SCOLAIRE"
    canvas.drawText(schoolTitle, 52f, 22f, paint)

    paint.textSize = 7.5f
    paint.isFakeBoldText = false
    canvas.drawText("CARTE D'ÉLÈVE OFFICIELLE", 52f, 36f, paint)

    // Draw School Logo on top-left if present
    if (!schoolLogoBase64.isNullOrBlank()) {
        try {
            val logoBytes = android.util.Base64.decode(schoolLogoBase64, android.util.Base64.DEFAULT)
            val logoBmp = android.graphics.BitmapFactory.decodeByteArray(logoBytes, 0, logoBytes.size)
            if (logoBmp != null) {
                val logoRect = android.graphics.RectF(8f, 6f, 44f, 42f)
                canvas.drawBitmap(logoBmp, null, logoRect, paint)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Student Photo Frame (Left Side)
    val photoLeft = 14f
    val photoTop = 56f
    val photoRight = 84f
    val photoBottom = 144f
    val photoRect = android.graphics.RectF(photoLeft, photoTop, photoRight, photoBottom)

    var hasPhoto = false
    if (!student.photoBase64.isNullOrBlank()) {
        try {
            val photoBytes = android.util.Base64.decode(student.photoBase64, android.util.Base64.DEFAULT)
            val studentBmp = android.graphics.BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.size)
            if (studentBmp != null) {
                canvas.drawBitmap(studentBmp, null, photoRect, paint)
                hasPhoto = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    if (!hasPhoto) {
        // Placeholder Box
        paint.color = android.graphics.Color.parseColor("#E2E8F0")
        canvas.drawRoundRect(photoRect, 4f, 4f, paint)
        paint.color = android.graphics.Color.parseColor("#64748B")
        paint.textSize = 8f
        paint.textAlign = android.graphics.Paint.Align.CENTER
        canvas.drawText("PHOTO", photoLeft + 35f, photoTop + 48f, paint)
        paint.textAlign = android.graphics.Paint.Align.LEFT
    }

    // Photo Border
    paint.style = android.graphics.Paint.Style.STROKE
    paint.strokeWidth = 1f
    paint.color = android.graphics.Color.parseColor("#CBD5E1")
    canvas.drawRoundRect(photoRect, 4f, 4f, paint)
    paint.style = android.graphics.Paint.Style.FILL

    // Student Details (Center Column)
    val detailX = 94f
    var startY = 70f
    
    // Full Name
    paint.color = android.graphics.Color.parseColor("#1E293B")
    paint.textSize = 10.5f
    paint.isFakeBoldText = true
    val nameText = "${student.firstName} ${student.lastName}".uppercase()
    canvas.drawText(nameText, detailX, startY, paint)

    // Matricule Badge
    startY += 18f
    val matText = "MATRICULE : #$matricule"
    paint.textSize = 9f
    paint.color = android.graphics.Color.parseColor("#0F56E3")
    canvas.drawText(matText, detailX, startY, paint)

    // Class & Section
    startY += 15f
    paint.color = android.graphics.Color.parseColor("#334155")
    paint.isFakeBoldText = false
    paint.textSize = 8.5f
    canvas.drawText("Classe : ${student.grade}", detailX, startY, paint)

    startY += 13f
    canvas.drawText("Section : ${student.section}", detailX, startY, paint)

    // Parent Phone
    startY += 13f
    val phone = student.parentWhatsApp?.ifBlank { "Non renseigné" } ?: "Non renseigné"
    canvas.drawText("Tél. Parent : $phone", detailX, startY, paint)

    // Permanent QR Code (Right Side)
    try {
        val idCardNumberFormat = java.text.NumberFormat.getNumberInstance(java.util.Locale("fr", "GN"))
        val idCardTotalPaid = payments.sumOf { it.amount } + student.registrationFee + student.reenrollmentFee
        val idCardTotalToPay = studentClassFee + student.registrationFee + student.reenrollmentFee
        val idCardDue = (idCardTotalToPay - idCardTotalPaid).coerceAtLeast(0L)
        val idCardPercent = if (idCardTotalToPay > 0) (idCardTotalPaid.toDouble() / idCardTotalToPay.toDouble() * 100).toInt() else 100

        val formattedTotal = idCardNumberFormat.format(idCardTotalToPay)
        val formattedPaid = idCardNumberFormat.format(idCardTotalPaid)
        val formattedDue = idCardNumberFormat.format(idCardDue)
        
        val studentGrades = grades.filter { it.studentId == student.id }
        val latestTerm = studentGrades.firstOrNull()?.term ?: ""
        
        var termStr = ""
        var avgStr = ""
        var rankStr = ""
        var sizeStr = ""
        var mentionStr = ""
        
        if (latestTerm.isNotEmpty()) {
            termStr = latestTerm
            val classStudents = studentsList.filter { it.grade == student.grade && it.section == student.section }
            val classGrades = grades.filter { it.term == latestTerm }
            
            val studentAverages = classStudents.mapNotNull { s ->
                val sGrades = classGrades.filter { it.studentId == s.id }
                if (sGrades.isEmpty()) return@mapNotNull null
                
                var totalPoints = 0.0
                var totalCoeffs = 0
                sGrades.forEach { g ->
                    val subj = subjects.find { it.id == g.subjectId }
                    if (subj != null) {
                        val eval = g.evaluationScore
                        val exam = g.examScore
                        val subAvg = when {
                            eval != null && exam != null -> (eval + exam * 2f) / 3f
                            eval != null -> eval
                            exam != null -> exam
                            else -> null
                        }
                        if (subAvg != null) {
                            totalPoints += subAvg * subj.coefficient
                            totalCoeffs += subj.coefficient
                        }
                    }
                }
                if (totalCoeffs > 0) s.id to (totalPoints / totalCoeffs) else null
            }.sortedByDescending { it.second }
            
            val myAvgIndex = studentAverages.indexOfFirst { it.first == student.id }
            if (myAvgIndex != -1) {
                val myAvg = studentAverages[myAvgIndex].second
                avgStr = String.format(java.util.Locale.US, "%.2f", myAvg)
                rankStr = (myAvgIndex + 1).toString()
                sizeStr = studentAverages.size.toString()
                
                val baseScale = if (student.section.contains("PRIMAIRE", true)) 10.0 else 20.0
                val ratio = myAvg / baseScale
                mentionStr = when {
                    ratio >= 0.8 -> "Félicitations"
                    ratio >= 0.7 -> "Tableau d'Honneur"
                    ratio >= 0.6 -> "Encouragements"
                    ratio >= 0.5 -> "Passable"
                    else -> "Insuffisant"
                }
            }
        }

        val qrData = com.example.ui.util.QrCodeUtils.buildStudentQrData(
            studentId = student.id,
            remoteId = matricule,
            name = "${student.firstName} ${student.lastName}",
            grade = student.grade,
            section = student.section,
            totalFee = formattedTotal,
            paidFee = formattedPaid,
            dueFee = formattedDue,
            percent = idCardPercent.toString(),
            term = termStr,
            avg = avgStr,
            rank = rankStr,
            size = sizeStr,
            mention = mentionStr
        )
        val qrBmp = com.example.ui.util.QrCodeUtils.generateQrBitmap(qrData, 200)
        if (qrBmp != null) {
            val qrRect = android.graphics.RectF(238f, 56f, 326f, 144f)
            canvas.drawBitmap(qrBmp, null, qrRect, paint)
            
            // QR Border
            paint.style = android.graphics.Paint.Style.STROKE
            paint.strokeWidth = 1f
            paint.color = android.graphics.Color.parseColor("#E2E8F0")
            canvas.drawRoundRect(qrRect, 4f, 4f, paint)
            paint.style = android.graphics.Paint.Style.FILL

            paint.color = android.graphics.Color.parseColor("#64748B")
            paint.textSize = 6f
            paint.textAlign = android.graphics.Paint.Align.CENTER
            canvas.drawText("SCANNER PERMANENT", 282f, 154f, paint)
            paint.textAlign = android.graphics.Paint.Align.LEFT
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    // Divider Line
    paint.color = android.graphics.Color.parseColor("#E2E8F0")
    canvas.drawLine(14f, 184f, 326f, 184f, paint)

    // Footer Text
    paint.color = android.graphics.Color.parseColor("#94A3B8")
    paint.textSize = 6.5f
    canvas.drawText("ScolaPay • Carte scolaire infalsifiable avec QR Code permanent", 14f, 198f, paint)

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
