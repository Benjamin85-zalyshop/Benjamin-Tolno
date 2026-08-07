package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.SchoolViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(
    viewModel: SchoolViewModel,
    onNavigateBack: () -> Unit,
    onAddStudent: () -> Unit,
    onAddPayment: (Int, String) -> Unit,
    onStudentClick: (Int) -> Unit
) {
    val students by viewModel.students.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val classFees by viewModel.classFees.collectAsStateWithLifecycle()
    val userRole by viewModel.userRole.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedGrade by remember { mutableStateOf<String?>(null) }
    var showQrScannerDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Liste des Élèves") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { showQrScannerDialog = true }) {
                        Icon(Icons.Filled.QrCodeScanner, contentDescription = "Scanner QR Code")
                    }
                }
            )
        },
        floatingActionButton = {
            if (userRole == "FINANCIER") {
                FloatingActionButton(onClick = onAddStudent) {
                    Icon(Icons.Filled.Add, contentDescription = "Ajouter un élève")
                }
            }
        }
    ) { padding ->
        if (students.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    text = "Aucun élève enregistré.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val grades = students.map { it.grade }.distinct().sorted()
            val filteredStudents = students.filter {
                (selectedGrade == null || it.grade == selectedGrade) &&
                (searchQuery.isBlank() ||
                 it.firstName.contains(searchQuery, ignoreCase = true) ||
                 it.lastName.contains(searchQuery, ignoreCase = true))
            }

            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Rechercher un élève") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Recherche") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Effacer")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    singleLine = true
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedGrade == null,
                            onClick = { selectedGrade = null },
                            label = { Text("Toutes") }
                        )
                    }
                    items(grades) { grade ->
                        FilterChip(
                            selected = selectedGrade == grade,
                            onClick = { selectedGrade = grade },
                            label = { Text(grade) }
                        )
                    }
                }

                if (filteredStudents.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Aucun élève trouvé.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp, top = 8.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val numberFormat = NumberFormat.getNumberInstance(Locale("fr", "GN"))
                        items(filteredStudents) { student ->
                            val fullName = "${student.firstName} ${student.lastName}"
                            val studentPayments = payments.filter { it.studentId == student.id }
                            val totalPaid = studentPayments.filter { it.reason != "Inscription" && it.reason != "Réinscription" }.sumOf { it.amount }
                            val formattedTotal = "${numberFormat.format(totalPaid)} GNF"
                                                        val matricule = if (student.remoteId.length >= 5) student.remoteId.take(5).uppercase() else student.id.toString()
                            StudentCard(
                                name = fullName,
                                matricule = matricule,
                                grade = student.grade,
                                section = student.section,
                                totalPaid = formattedTotal,
                                showPaymentAction = (userRole == "FINANCIER"),
                                onAddPaymentClick = { onAddPayment(student.id, fullName) },
                                onClick = { onStudentClick(student.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showQrScannerDialog) {
        QrScannerDialog(
            students = students,
            payments = payments,
            classFees = classFees,
            onDismiss = { showQrScannerDialog = false },
            onStudentSelected = { studentId ->
                onStudentClick(studentId)
            }
        )
    }
}

@Composable
fun StudentCard(name: String, matricule: String, grade: String, section: String, totalPaid: String, showPaymentAction: Boolean, onAddPaymentClick: () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = "ID: #$matricule • $section - $grade", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "Payé: $totalPaid",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            if (showPaymentAction) {
                IconButton(
                    onClick = onAddPaymentClick,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Payment,
                        contentDescription = "Payer",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}
