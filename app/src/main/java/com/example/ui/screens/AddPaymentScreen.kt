package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.SchoolViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentScreen(
    studentId: Int,
    studentName: String,
    viewModel: SchoolViewModel,
    onNavigateBack: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("Frais de scolarité") }
    var selectedMethod by remember { mutableStateOf("Espèces") }
    val context = LocalContext.current

    val students by viewModel.students.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val classFees by viewModel.classFees.collectAsStateWithLifecycle()

    val student = remember(students, studentId) { students.find { it.id == studentId } }
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale("fr", "GN")) }
    val schoolName by viewModel.schoolName.collectAsStateWithLifecycle()
    val schoolLogoBase64 by viewModel.schoolLogoBase64.collectAsStateWithLifecycle()

    var showSuccessDialog by remember { mutableStateOf(false) }
    var successAmount by remember { mutableStateOf(0L) }
    var successReason by remember { mutableStateOf("") }
    var successMethod by remember { mutableStateOf("Espèces") }

    val exportReceiptPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf"),
        onResult = { uri ->
            uri?.let {
                if (student != null) {
                    generateReceiptPdf(
                        context = context,
                        studentName = "${student.firstName} ${student.lastName}",
                        grade = student.grade,
                        section = student.section,
                        amount = successAmount,
                        reason = successReason,
                        paymentMethod = successMethod,
                        date = System.currentTimeMillis(),
                        schoolName = schoolName ?: "",
                        schoolLogoBase64 = schoolLogoBase64,
                        uri = it
                    )
                    Toast.makeText(context, "Reçu PDF généré avec succès", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    val classFee = remember(student, classFees) {
        student?.let { s -> classFees.find { it.grade == s.grade }?.feeAmount } ?: 0L
    }

    val totalPaid = remember(payments, studentId) {
        payments.filter { it.studentId == studentId }.sumOf { it.amount }
    }

    val remainingToPay = remember(classFee, totalPaid) {
        (classFee - totalPaid).coerceAtLeast(0L)
    }

    val enteredAmount = amount.toLongOrNull() ?: 0L
    val isOverpaid = classFee > 0L && enteredAmount > remainingToPay

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paiement - $studentName") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Student fee summary card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Statut Financier de l'Élève",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    if (student != null) {
                        Text(
                            text = "Classe : ${student.grade} • Section : ${student.section}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Frais Scolaires Prévus :", fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(
                            text = if (classFee > 0L) "${numberFormat.format(classFee)} GNF" else "Non défini",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Déjà Payé :", fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(
                            text = "${numberFormat.format(totalPaid)} GNF",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF10B981)
                        )
                    }

                    if (classFee > 0L) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Reste à Payer :", fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text = "${numberFormat.format(remainingToPay)} GNF",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (remainingToPay == 0L) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                            )
                        }

                        if (remainingToPay == 0L) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFD1FAE5)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "✓ Cet élève a entièrement réglé sa scolarité.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF065F46),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Note : Les frais de scolarité de cette classe ne sont pas configurés. Veuillez les définir dans l'onglet \"Scolarité\" de l'écran d'accueil.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                        amount = newValue
                    }
                },
                label = { Text("Montant du paiement (GNF)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = isOverpaid,
                enabled = classFee == 0L || remainingToPay > 0L
            )

            if (isOverpaid) {
                Text(
                    text = "Erreur : Le montant ne peut pas dépasser le reste à payer de l'élève (${numberFormat.format(remainingToPay)} GNF).",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Motif du paiement") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                enabled = classFee == 0L || remainingToPay > 0L
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Mode de règlement",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            val methods = listOf(
                "Espèces" to Color(0xFF10B981),
                "Orange Money" to Color(0xFFF97316),
                "Mobile Money" to Color(0xFFEAB308),
                "ScolaPay" to Color(0xFF0F56E3),
                "Virement" to Color(0xFF6B7280),
                "Chèque" to Color(0xFF8B5CF6)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    methods.take(3).forEach { (method, color) ->
                        val isSelected = selectedMethod == method
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .clickable { selectedMethod = method },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                                contentColor = if (isSelected) color else MaterialTheme.colorScheme.onSurface
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) color else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = method,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    methods.drop(3).forEach { (method, color) ->
                        val isSelected = selectedMethod == method
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .clickable { selectedMethod = method },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                                contentColor = if (isSelected) color else MaterialTheme.colorScheme.onSurface
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) color else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = method,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val amountValue = amount.toLongOrNull()
                    if (amountValue != null && amountValue > 0 && reason.isNotBlank()) {
                        if (classFee > 0L && amountValue > remainingToPay) {
                            Toast.makeText(context, "Impossible : Le montant dépasse le reste à payer !", Toast.LENGTH_LONG).show()
                        } else {
                            viewModel.insertPayment(studentId, amountValue, reason.trim(), selectedMethod)
                            successAmount = amountValue
                            successReason = reason.trim()
                            successMethod = selectedMethod
                            showSuccessDialog = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isOverpaid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                ),
                enabled = amount.isNotBlank() && reason.isNotBlank() && !isOverpaid && (classFee == 0L || remainingToPay > 0L)
            ) {
                Text("Valider le paiement")
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { /* Interdire la fermeture par clic externe */ },
            icon = {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Paiement enregistré !",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Le paiement a été validé avec succès pour l'élève.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Élève :", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(studentName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Montant :", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${numberFormat.format(successAmount)} GNF", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Motif :", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(successReason, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Règlement :", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                
                                val methodColor = when (successMethod) {
                                    "Espèces" -> Color(0xFF10B981)
                                    "Orange Money" -> Color(0xFFF97316)
                                    "Mobile Money" -> Color(0xFFEAB308)
                                    "ScolaPay" -> Color(0xFF0F56E3)
                                    "Virement" -> Color(0xFF6B7280)
                                    "Chèque" -> Color(0xFF8B5CF6)
                                    else -> Color(0xFF6B7280)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(methodColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = successMethod,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = methodColor
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanName = studentName.replace(" ", "_")
                        val fileName = "Recu_${cleanName}_${successAmount}.pdf"
                        exportReceiptPdfLauncher.launch(fileName)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Télécharger Reçu")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Retourner", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}

fun generateReceiptPdf(
    context: android.content.Context,
    studentName: String,
    grade: String,
    section: String,
    amount: Long,
    reason: String,
    paymentMethod: String,
    date: Long,
    schoolName: String,
    schoolLogoBase64: String?,
    uri: android.net.Uri
) {
    val pdfDocument = android.graphics.pdf.PdfDocument()
    val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 420, 1).create() // A5 horizontal layout
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = android.graphics.Paint()
    
    val numberFormat = java.text.NumberFormat.getNumberInstance(java.util.Locale("fr", "GN"))
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale("fr", "GN"))
    
    // Draw Border
    paint.style = android.graphics.Paint.Style.STROKE
    paint.strokeWidth = 2f
    paint.color = android.graphics.Color.DKGRAY
    canvas.drawRect(15f, 15f, 580f, 405f, paint)
    
    // Reset paint for text
    paint.style = android.graphics.Paint.Style.FILL
    
    // Draw School Logo if present
    if (!schoolLogoBase64.isNullOrBlank()) {
        try {
            val decodedBytes = android.util.Base64.decode(schoolLogoBase64, android.util.Base64.DEFAULT)
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            if (bitmap != null) {
                val destRect = android.graphics.RectF(475f, 30f, 555f, 100f)
                canvas.drawBitmap(bitmap, null, destRect, paint)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Header
    paint.textSize = 22f
    paint.isFakeBoldText = true
    paint.color = android.graphics.Color.parseColor("#0F56E3") // Primary Blue
    canvas.drawText(schoolName.ifBlank { "ScolaPay" }, 40f, 50f, paint)
    
    paint.textSize = 14f
    paint.isFakeBoldText = true
    paint.color = android.graphics.Color.DKGRAY
    canvas.drawText("REÇU DE PAIEMENT ÉLÈVE", 40f, 75f, paint)
    
    paint.textSize = 10f
    paint.isFakeBoldText = false
    paint.color = android.graphics.Color.GRAY
    canvas.drawText("Généré le : ${sdf.format(java.util.Date(date))}", 40f, 95f, paint)
    
    // Divider line
    paint.strokeWidth = 1f
    paint.color = android.graphics.Color.LTGRAY
    canvas.drawLine(40f, 110f, 555f, 110f, paint)
    
    // Student info (Left Column)
    paint.textSize = 12f
    paint.color = android.graphics.Color.BLACK
    paint.isFakeBoldText = true
    canvas.drawText("INFORMATIONS ÉLÈVE", 40f, 140f, paint)
    
    paint.isFakeBoldText = false
    canvas.drawText("Nom complet : $studentName", 40f, 165f, paint)
    canvas.drawText("Classe : $grade", 40f, 190f, paint)
    canvas.drawText("Section : $section", 40f, 215f, paint)
    
    // Payment info (Right Column)
    paint.isFakeBoldText = true
    canvas.drawText("DÉTAILS DU RÈGLEMENT", 320f, 140f, paint)
    
    paint.isFakeBoldText = false
    canvas.drawText("Motif : $reason", 320f, 165f, paint)
    
    // Payment Method - Highlighted
    canvas.drawText("Mode de règlement :", 320f, 190f, paint)
    paint.isFakeBoldText = true
    paint.color = when (paymentMethod) {
        "Espèces" -> android.graphics.Color.parseColor("#10B981")
        "Orange Money" -> android.graphics.Color.parseColor("#F97316")
        "Mobile Money" -> android.graphics.Color.parseColor("#EAB308")
        "ScolaPay" -> android.graphics.Color.parseColor("#0F56E3")
        "Virement" -> android.graphics.Color.parseColor("#6B7280")
        "Chèque" -> android.graphics.Color.parseColor("#8B5CF6")
        else -> android.graphics.Color.BLACK
    }
    canvas.drawText(paymentMethod, 450f, 190f, paint)
    
    // Reset color to black
    paint.color = android.graphics.Color.BLACK
    
    // Total Amount Box
    paint.style = android.graphics.Paint.Style.STROKE
    paint.strokeWidth = 1.5f
    paint.color = android.graphics.Color.parseColor("#0F56E3")
    canvas.drawRect(320f, 215f, 555f, 265f, paint)
    
    paint.style = android.graphics.Paint.Style.FILL
    paint.color = android.graphics.Color.parseColor("#EEF2F6")
    canvas.drawRect(321f, 216f, 554f, 264f, paint)
    
    paint.color = android.graphics.Color.parseColor("#0F56E3")
    paint.textSize = 14f
    paint.isFakeBoldText = true
    canvas.drawText("MONTANT PAYÉ", 335f, 235f, paint)
    
    paint.textSize = 16f
    canvas.drawText("${numberFormat.format(amount)} GNF", 335f, 255f, paint)
    
    // Divider before footer
    paint.strokeWidth = 1f
    paint.color = android.graphics.Color.LTGRAY
    canvas.drawLine(40f, 290f, 555f, 290f, paint)
    
    // Footer / Signatures
    paint.textSize = 10f
    paint.color = android.graphics.Color.GRAY
    paint.isFakeBoldText = false
    canvas.drawText("Merci pour votre confiance. Reçu généré électroniquement par ScolaPay.", 40f, 320f, paint)
    canvas.drawText("Signature & Cachet de l'établissement", 360f, 320f, paint)
    
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
