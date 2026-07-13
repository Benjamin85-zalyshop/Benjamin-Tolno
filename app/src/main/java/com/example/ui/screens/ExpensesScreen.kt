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

    var selectedMonth by remember { mutableStateOf<String?>(null) }
    
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
                generateExpensesPdf(context, filteredExpenses, selectedMonth ?: "Tous", it)
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
            if (userRole == "FINANCIER") {
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
                                    text = "Total Période : ${numberFormat.format(totalExpenses)} GNF",
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
                            showDeleteAction = (userRole == "FINANCIER"),
                            onDelete = { viewModel.deleteExpense(expense.id) }
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseItem(amount: Long, reason: String, section: String, date: Long, showDeleteAction: Boolean, onDelete: () -> Unit) {
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
                    text = "- ${numberFormat.format(amount)} GNF",
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
    canvas.drawText("ScolaPay - Rapport de Dépenses", 50f, 80f, paint)
    
    paint.textSize = 18f
    paint.isFakeBoldText = false
    canvas.drawText("Période : $period", 50f, 130f, paint)
    
    val numberFormat = java.text.NumberFormat.getNumberInstance(java.util.Locale("fr", "GN"))
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale("fr", "GN"))
    
    var yPosition = 180f
    paint.textSize = 16f
    paint.isFakeBoldText = true
    canvas.drawText("Détail des dépenses :", 50f, yPosition, paint)
    yPosition += 30f
    
    paint.isFakeBoldText = false
    var total = 0L
    for (expense in expenses) {
        if (yPosition > 800f) {
            pdfDocument.finishPage(page)
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            yPosition = 50f
        }
        
        val dateStr = sdf.format(java.util.Date(expense.date))
        val amountStr = "${numberFormat.format(expense.amount)} GNF"
        
        val line = "- $dateStr : ${expense.reason} (${expense.section}) -> $amountStr"
        canvas.drawText(line, 50f, yPosition, paint)
        yPosition += 30f
        total += expense.amount
    }
    
    yPosition += 20f
    if (yPosition > 800f) {
        pdfDocument.finishPage(page)
        page = pdfDocument.startPage(pageInfo)
        canvas = page.canvas
        yPosition = 50f
    }
    paint.isFakeBoldText = true
    canvas.drawText("Total dépensé : ${numberFormat.format(total)} GNF", 50f, yPosition, paint)
    
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
