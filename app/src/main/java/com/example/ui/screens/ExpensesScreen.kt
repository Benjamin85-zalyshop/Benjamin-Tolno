package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.SchoolViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    viewModel: SchoolViewModel,
    onNavigateBack: () -> Unit,
    onAddExpense: () -> Unit
) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val userRole by viewModel.userRole.collectAsStateWithLifecycle()
    val schoolAccount by viewModel.schoolAccount.collectAsStateWithLifecycle()
    val currency = schoolAccount?.currency ?: "GNF"

    var selectedMonth by remember { mutableStateOf<String?>(null) }
    var expenseToDelete by remember { mutableStateOf<com.example.data.models.Expense?>(null) }
    
    val sdfMonth = remember { SimpleDateFormat("MM/yyyy", Locale("fr", "GN")) }
    val availableMonths = remember(expenses) {
        expenses.map { sdfMonth.format(Date(it.date)) }.distinct().sortedDescending()
    }
    
    LaunchedEffect(availableMonths) {
        if (selectedMonth == null && availableMonths.isNotEmpty()) {
            selectedMonth = availableMonths.first()
        }
    }
    
    val filteredExpenses = remember(expenses, selectedMonth) {
        if (selectedMonth == null) expenses else expenses.filter { sdfMonth.format(Date(it.date)) == selectedMonth }
    }

    val context = LocalContext.current
    val exportPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf"),
        onResult = { uri ->
            uri?.let {
                generateExpensesPdf(context, filteredExpenses, selectedMonth ?: "Tous", it, currency, schoolAccount)
                Toast.makeText(context, "PDF généré avec succès", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dépenses") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (filteredExpenses.isNotEmpty()) {
                        IconButton(onClick = {
                            val monthStr = selectedMonth?.replace("/", "-") ?: "tous"
                            exportPdfLauncher.launch("Depenses_$monthStr.pdf")
                        }) {
                            Icon(Icons.Filled.Download, contentDescription = "Télécharger PDF")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            val isAuthorized = userRole == "FINANCIER" || userRole == "ADMIN"
            if (isAuthorized) {
                FloatingActionButton(onClick = onAddExpense) {
                    Icon(Icons.Filled.Add, contentDescription = "Ajouter une dépense")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (availableMonths.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedMonth == null,
                            onClick = { selectedMonth = null },
                            label = { Text("Toutes") }
                        )
                    }
                    items(availableMonths) { month ->
                        FilterChip(
                            selected = selectedMonth == month,
                            onClick = { selectedMonth = month },
                            label = { Text(month) }
                        )
                    }
                }
            }

            if (filteredExpenses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Aucune dépense pour cette période.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val numberFormat = NumberFormat.getNumberInstance(Locale("fr", "GN"))
                    val totalExpenses = filteredExpenses.sumOf { it.amount }
                    
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Total Période : ${numberFormat.format(totalExpenses)} $currency",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    items(filteredExpenses) { expense ->
                        ExpenseItem(
                            amount = expense.amount,
                            reason = expense.reason,
                            section = expense.section,
                            date = expense.date,
                            showDeleteAction = (userRole == "FINANCIER" || userRole == "ADMIN"),
                            currency = currency,
                                                        onDelete = { expenseToDelete = expense }
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        if (expenseToDelete != null) {
            AlertDialog(
                onDismissRequest = { expenseToDelete = null },
                title = { Text("Supprimer la dépense", fontWeight = FontWeight.Bold) },
                text = { Text("Êtes-vous sûr de vouloir supprimer cette dépense ? Cette action est irréversible.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteExpense(expenseToDelete!!.id)
                            expenseToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Supprimer")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { expenseToDelete = null }) {
                        Text("Annuler")
                    }
                }
            )
        }
    }
}

@Composable
fun ExpenseItem(
    amount: Long,
    reason: String,
    section: String,
    date: Long,
    showDeleteAction: Boolean,
    currency: String = "GNF",
    onDelete: () -> Unit
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale("fr", "GN"))
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("fr", "GN"))
    
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
                Text(text = section, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                Text(text = sdf.format(Date(date)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "- ${numberFormat.format(amount)} $currency",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                if (showDeleteAction) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Supprimer la dépense",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

fun generateExpensesPdf(
    context: android.content.Context,
    expenses: List<com.example.data.models.Expense>,
    period: String,
    uri: android.net.Uri,
    currency: String = "GNF",
    schoolAccount: com.example.data.models.SchoolAccount? = null
) {
    val pdfDocument = android.graphics.pdf.PdfDocument()
    val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
    var page = pdfDocument.startPage(pageInfo)
    var canvas = page.canvas
    val paint = android.graphics.Paint()

    val numberFormat = java.text.NumberFormat.getNumberInstance(java.util.Locale("fr", "GN"))
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale("fr", "GN"))

    var yPosition = 50f
    
    // --- Header Section ---
    val actualSchoolName = schoolAccount?.displayName?.takeIf { it.isNotBlank() } ?: schoolAccount?.schoolName ?: "ScolaPay"
    val schoolEmail = if (schoolAccount?.displayName?.isNotBlank() == true) schoolAccount.schoolName else ""
    val schoolAddress = schoolAccount?.address ?: ""
    val schoolPhone = schoolAccount?.founderPhone ?: ""
    val logoBase64 = schoolAccount?.logoBase64
    
    // Draw Logo
    if (!logoBase64.isNullOrBlank()) {
        try {
            val decodedString = android.util.Base64.decode(logoBase64, android.util.Base64.DEFAULT)
            val decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            if (decodedByte != null) {
                // Keep aspect ratio
                val aspectRatio = decodedByte.width.toFloat() / decodedByte.height.toFloat()
                val logoHeight = 80f
                val logoWidth = logoHeight * aspectRatio
                val logoRect = android.graphics.RectF(50f, yPosition, 50f + logoWidth, yPosition + logoHeight)
                canvas.drawBitmap(decodedByte, null, logoRect, paint)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Draw School Info
    var textY = yPosition + 20f
    paint.textAlign = android.graphics.Paint.Align.RIGHT
    paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    paint.textSize = 20f
    paint.color = android.graphics.Color.BLACK
    canvas.drawText(actualSchoolName.uppercase(), 545f, textY, paint)
    
    paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
    paint.textSize = 12f
    paint.color = android.graphics.Color.DKGRAY
    
    textY += 20f
    if (schoolEmail.isNotBlank()) {
        canvas.drawText(schoolEmail, 545f, textY, paint)
        textY += 16f
    }
    
    if (schoolAddress.isNotBlank()) {
        canvas.drawText(schoolAddress, 545f, textY, paint)
        textY += 16f
    }
    
    if (schoolPhone.isNotBlank()) {
        canvas.drawText("Tél: " + schoolPhone, 545f, textY, paint)
    }
    
    yPosition += 110f
    
    // Draw Divider
    paint.color = android.graphics.Color.parseColor("#E5E7EB")
    paint.strokeWidth = 2f
    canvas.drawLine(50f, yPosition, 545f, yPosition, paint)
    
    yPosition += 40f
    
    // --- Title Section ---
    paint.textAlign = android.graphics.Paint.Align.CENTER
    paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    paint.textSize = 22f
    paint.color = android.graphics.Color.parseColor("#0F56E3") // ScolaPay blue
    canvas.drawText("RAPPORT DES DÉPENSES", 595f / 2, yPosition, paint)
    
    yPosition += 25f
    paint.textSize = 14f
    paint.color = android.graphics.Color.DKGRAY
    canvas.drawText("Période : $period", 595f / 2, yPosition, paint)
    
    yPosition += 40f
    
    // --- Table Header ---
    val colDateX = 50f
    val colReasonX = 180f
    val colSectionX = 360f
    val colAmountX = 545f // Right aligned
    
    paint.color = android.graphics.Color.parseColor("#F3F4F6")
    canvas.drawRect(50f, yPosition - 15f, 545f, yPosition + 15f, paint)
    
    paint.color = android.graphics.Color.BLACK
    paint.textAlign = android.graphics.Paint.Align.LEFT
    paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    paint.textSize = 12f
    
    canvas.drawText("Date", colDateX + 5f, yPosition + 5f, paint)
    canvas.drawText("Motif", colReasonX + 5f, yPosition + 5f, paint)
    canvas.drawText("Catégorie", colSectionX + 5f, yPosition + 5f, paint)
    
    paint.textAlign = android.graphics.Paint.Align.RIGHT
    canvas.drawText("Montant", colAmountX - 5f, yPosition + 5f, paint)
    
    yPosition += 25f
    
    // --- Table Content ---
    paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
    paint.textSize = 11f
    var total = 0L
    
    for (expense in expenses) {
        if (yPosition > 780f) {
            pdfDocument.finishPage(page)
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            yPosition = 50f
            
            // Re-draw table header on new page
            paint.color = android.graphics.Color.parseColor("#F3F4F6")
            canvas.drawRect(50f, yPosition - 15f, 545f, yPosition + 15f, paint)
            
            paint.color = android.graphics.Color.BLACK
            paint.textAlign = android.graphics.Paint.Align.LEFT
            paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            paint.textSize = 12f
            
            canvas.drawText("Date", colDateX + 5f, yPosition + 5f, paint)
            canvas.drawText("Motif", colReasonX + 5f, yPosition + 5f, paint)
            canvas.drawText("Catégorie", colSectionX + 5f, yPosition + 5f, paint)
            
            paint.textAlign = android.graphics.Paint.Align.RIGHT
            canvas.drawText("Montant", colAmountX - 5f, yPosition + 5f, paint)
            
            yPosition += 25f
            paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
            paint.textSize = 11f
        }
        
        paint.color = android.graphics.Color.BLACK
        paint.textAlign = android.graphics.Paint.Align.LEFT
        
        val dateStr = sdf.format(java.util.Date(expense.date))
        val amountStr = "${numberFormat.format(expense.amount)} $currency"
        
        val maxReasonLen = 30
        val reasonStr = if (expense.reason.length > maxReasonLen) expense.reason.take(maxReasonLen) + "..." else expense.reason
        
        val maxSectionLen = 25
        val sectionStr = if (expense.section.length > maxSectionLen) expense.section.take(maxSectionLen) + "..." else expense.section
        
        canvas.drawText(dateStr, colDateX + 5f, yPosition, paint)
        canvas.drawText(reasonStr, colReasonX + 5f, yPosition, paint)
        canvas.drawText(sectionStr, colSectionX + 5f, yPosition, paint)
        
        paint.textAlign = android.graphics.Paint.Align.RIGHT
        canvas.drawText(amountStr, colAmountX - 5f, yPosition, paint)
        
        // Draw bottom border for the row
        paint.color = android.graphics.Color.parseColor("#E5E7EB")
        paint.strokeWidth = 1f
        canvas.drawLine(50f, yPosition + 10f, 545f, yPosition + 10f, paint)
        
        yPosition += 25f
        total += expense.amount
    }
    
    // --- Table Footer (Total) ---
    if (yPosition > 780f) {
        pdfDocument.finishPage(page)
        page = pdfDocument.startPage(pageInfo)
        canvas = page.canvas
        yPosition = 50f
    }
    
    yPosition += 10f
    paint.color = android.graphics.Color.parseColor("#F9FAFB")
    canvas.drawRect(350f, yPosition - 15f, 545f, yPosition + 15f, paint)
    
    paint.color = android.graphics.Color.BLACK
    paint.textAlign = android.graphics.Paint.Align.LEFT
    paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    paint.textSize = 12f
    canvas.drawText("TOTAL", 360f, yPosition + 5f, paint)
    
    paint.textAlign = android.graphics.Paint.Align.RIGHT
    paint.color = android.graphics.Color.parseColor("#D92D20") // Red for expenses
    canvas.drawText("${numberFormat.format(total)} $currency", 540f, yPosition + 5f, paint)
    
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
