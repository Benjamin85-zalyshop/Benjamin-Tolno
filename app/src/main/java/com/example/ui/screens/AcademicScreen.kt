package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.DEFAULT_CLASSES_BY_SECTION
import com.example.data.models.Student
import com.example.data.models.StudentGrade
import com.example.data.models.Subject
import com.example.ui.SchoolViewModel
import com.example.ui.util.ReportCardPdfUtils
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicScreen(
    viewModel: SchoolViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val students by viewModel.students.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val grades by viewModel.grades.collectAsStateWithLifecycle()
    val schoolName by viewModel.schoolName.collectAsStateWithLifecycle()
    val schoolLogoBase64 by viewModel.schoolLogoBase64.collectAsStateWithLifecycle()
    val selectedSchoolYear by viewModel.selectedSchoolYear.collectAsStateWithLifecycle()
    val schoolAccount by viewModel.schoolAccount.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val classFees by viewModel.classFees.collectAsStateWithLifecycle()

    val sections = listOf("LA MATERNELLE", "LE PRIMAIRE", "LE COLLÈGE", "LE LYCÉE", "L'UNIVERSITÉ", "L'ÉCOLE PROFESSIONNELLE")
    var selectedSection by remember { mutableStateOf("LE PRIMAIRE") }

    val gradeSuggestions = remember(selectedSection) {
        DEFAULT_CLASSES_BY_SECTION[selectedSection] ?: emptyList()
    }
    var selectedGrade by remember { mutableStateOf(gradeSuggestions.firstOrNull() ?: "1ère Année") }

    // Update selectedGrade when section changes
    LaunchedEffect(selectedSection) {
        val newGrades = DEFAULT_CLASSES_BY_SECTION[selectedSection] ?: emptyList()
        if (selectedGrade !in newGrades && newGrades.isNotEmpty()) {
            selectedGrade = newGrades.first()
        }
    }

    val terms = listOf("Mensuel", "1er Trimestre", "2ème Trimestre", "3ème Trimestre", "Fin d'année")
    var selectedTerm by remember { mutableStateOf("1er Trimestre") }

    var activeTabIndex by remember { mutableStateOf(0) } // 0: Matières, 1: Saisie Notes, 2: Bulletins

    // Dialog for Adding/Editing Subject
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var subjectNameInput by remember { mutableStateOf("") }
    var subjectCoeffInput by remember { mutableStateOf("1") }
    var subjectMaxInput by remember { mutableStateOf("20") }

    // Student selection for Bulletin PDF
    var selectedStudentForBulletin by remember { mutableStateOf<Student?>(null) }
    val filteredStudentsForGrade = remember(students, selectedSection, selectedGrade) {
        students.filter { it.grade == selectedGrade }
    }

    // Auto-select first student in grade for Bulletin tab
    LaunchedEffect(filteredStudentsForGrade) {
        if (selectedStudentForBulletin == null || selectedStudentForBulletin !in filteredStudentsForGrade) {
            selectedStudentForBulletin = filteredStudentsForGrade.firstOrNull()
        }
    }

    // PDF Launcher for Bulletin
    val exportPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let {
            val student = selectedStudentForBulletin ?: return@rememberLauncherForActivityResult
            val classSubjects = subjects.filter { it.section == student.section && it.grade == student.grade }
            val classGrades = grades.filter { it.term == selectedTerm }
            val allClassStudents = students.filter { it.grade == student.grade && it.section == student.section }
            val summary = ReportCardPdfUtils.calculateSummary(
                student = student,
                term = selectedTerm,
                allStudentsInClass = allClassStudents,
                subjects = classSubjects,
                allGradesForClassAndTerm = classGrades
            )

            val studentGrades = classGrades.filter { it.studentId == student.id }
            
            // Financial data for QR
            val studentPayments = payments.filter { it.studentId == student.id }
            val studentClassFee = classFees.find { it.grade == student.grade }?.feeAmount ?: 0L
            val totalPaid = studentPayments.sumOf { it.amount } + student.registrationFee
            val totalToPay = studentClassFee + student.registrationFee
            val due = (totalToPay - totalPaid).coerceAtLeast(0L)
            val percent = if (totalToPay > 0) (totalPaid.toDouble() / totalToPay.toDouble() * 100).toInt() else 100
            
            val numberFormat = java.text.NumberFormat.getNumberInstance(java.util.Locale("fr", "GN"))
            val formattedTotal = numberFormat.format(totalToPay)
            val formattedPaid = numberFormat.format(totalPaid)
            val formattedDue = numberFormat.format(due)

            viewModel.syncStudentAcademicsToRTDB(student.schoolId, student.id, selectedTerm)
            ReportCardPdfUtils.generateReportCardPdf(
                context = context,
                student = student,
                term = selectedTerm,
                schoolYear = selectedSchoolYear,
                schoolName = schoolName ?: "",
                schoolLogoBase64 = schoolLogoBase64,
                schoolAddress = schoolAccount?.address ?: "",
                schoolPhone = schoolAccount?.founderPhone ?: "",
                subjects = classSubjects,
                studentGrades = studentGrades,
                summary = summary,
                uri = it,
                totalFee = formattedTotal,
                paidFee = formattedPaid,
                dueFee = formattedDue,
                percent = percent.toString()
            )
            Toast.makeText(context, "Bulletin PDF généré avec succès !", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Gestion Académique & Notes", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("$schoolName • $selectedSchoolYear", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filters Section (Section & Class)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Filtrer par Section & Classe", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Section Selector Dropdown
                        var sectionExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = sectionExpanded,
                            onExpandedChange = { sectionExpanded = !sectionExpanded },
                            modifier = Modifier.weight(1.2f)
                        ) {
                            OutlinedTextField(
                                value = selectedSection,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Section") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sectionExpanded) },
                                modifier = Modifier.menuAnchor(),
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = sectionExpanded,
                                onDismissRequest = { sectionExpanded = false }
                            ) {
                                sections.forEach { sec ->
                                    DropdownMenuItem(
                                        text = { Text(sec) },
                                        onClick = {
                                            selectedSection = sec
                                            sectionExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Grade Selector Dropdown
                        var gradeExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = gradeExpanded,
                            onExpandedChange = { gradeExpanded = !gradeExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedGrade,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Classe") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gradeExpanded) },
                                modifier = Modifier.menuAnchor(),
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = gradeExpanded,
                                onDismissRequest = { gradeExpanded = false }
                            ) {
                                gradeSuggestions.forEach { g ->
                                    DropdownMenuItem(
                                        text = { Text(g) },
                                        onClick = {
                                            selectedGrade = g
                                            gradeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Tabs Header
            TabRow(
                selectedTabIndex = activeTabIndex,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = activeTabIndex == 0,
                    onClick = { activeTabIndex = 0 },
                    text = { Text("Matières", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeTabIndex == 1,
                    onClick = { activeTabIndex = 1 },
                    text = { Text("Saisie Notes", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeTabIndex == 2,
                    onClick = { activeTabIndex = 2 },
                    text = { Text("Bulletins PDF", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tab Content
            when (activeTabIndex) {
                0 -> SubjectsTab(
                    viewModel = viewModel,
                    selectedSection = selectedSection,
                    selectedGrade = selectedGrade,
                    subjects = subjects.filter { it.section == selectedSection && it.grade == selectedGrade },
                    onOpenAddDialog = {
                        subjectNameInput = ""
                        subjectCoeffInput = "1"
                        subjectMaxInput = if (selectedSection.equals("LE PRIMAIRE", ignoreCase = true)) "10" else "20"
                        showAddSubjectDialog = true
                    }
                )
                1 -> GradesEntryTab(
                    viewModel = viewModel,
                    selectedSection = selectedSection,
                    selectedGrade = selectedGrade,
                    selectedTerm = selectedTerm,
                    onTermSelected = { selectedTerm = it },
                    terms = terms,
                    students = filteredStudentsForGrade,
                    subjects = subjects.filter { it.section == selectedSection && it.grade == selectedGrade },
                    allGrades = grades
                )
                2 -> BulletinPdfTab(
                    selectedSection = selectedSection,
                    selectedGrade = selectedGrade,
                    selectedTerm = selectedTerm,
                    onTermSelected = { selectedTerm = it },
                    terms = terms,
                    students = filteredStudentsForGrade,
                    subjects = subjects.filter { it.section == selectedSection && it.grade == selectedGrade },
                    allGrades = grades,
                    selectedStudent = selectedStudentForBulletin,
                    onStudentSelected = { selectedStudentForBulletin = it },
                    onGeneratePdf = {
                        if (selectedStudentForBulletin != null) {
                            val fileName = "Bulletin_${selectedTerm.replace(" ", "_")}_${selectedStudentForBulletin?.firstName}_${selectedStudentForBulletin?.lastName}.pdf"
                            exportPdfLauncher.launch(fileName)
                        } else {
                            Toast.makeText(context, "Veuillez sélectionner un élève", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }

        // Add Subject Dialog
        if (showAddSubjectDialog) {
            AlertDialog(
                onDismissRequest = { showAddSubjectDialog = false },
                title = { Text("Ajouter une Matière ($selectedGrade)", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = subjectNameInput,
                            onValueChange = { subjectNameInput = it },
                            label = { Text("Nom de la Matière (ex: Mathématiques)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = subjectCoeffInput,
                                onValueChange = { subjectCoeffInput = it },
                                label = { Text("Coefficient") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = subjectMaxInput,
                                onValueChange = { subjectMaxInput = it },
                                label = { Text("Barème (ex: 20)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val name = subjectNameInput.trim()
                            val coeff = subjectCoeffInput.toIntOrNull() ?: 1
                            val defaultMax = if (selectedSection.equals("LE PRIMAIRE", ignoreCase = true)) 10f else 20f
                            val maxScore = subjectMaxInput.toFloatOrNull() ?: defaultMax
                            if (name.isNotEmpty()) {
                                viewModel.insertSubject(selectedSection, selectedGrade, name, coeff, maxScore)
                                Toast.makeText(context, "Matière '$name' ajoutée", Toast.LENGTH_SHORT).show()
                                showAddSubjectDialog = false
                            }
                        }
                    ) {
                        Text("Ajouter")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddSubjectDialog = false }) {
                        Text("Annuler")
                    }
                }
            )
        }
    }
}

@Composable
private fun SubjectsTab(
    viewModel: SchoolViewModel,
    selectedSection: String,
    selectedGrade: String,
    subjects: List<Subject>,
    onOpenAddDialog: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Matières configurées (${subjects.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (subjects.isEmpty()) {
                    OutlinedButton(
                        onClick = {
                            viewModel.seedDefaultSubjects(selectedSection, selectedGrade)
                            Toast.makeText(context, "Matières par défaut ajoutées pour $selectedGrade", Toast.LENGTH_SHORT).show()
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Par Défaut", style = MaterialTheme.typography.labelMedium)
                    }
                }
                Button(
                    onClick = onOpenAddDialog,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ajouter", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (subjects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Aucune matière pour $selectedGrade",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            viewModel.seedDefaultSubjects(selectedSection, selectedGrade)
                            Toast.makeText(context, "Matières par défaut ajoutées", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Charger le programme type ($selectedSection)")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(subjects, key = { it.id }) { sub ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            sub.name.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                                Column {
                                    Text(sub.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                    Text("Coefficient : ${sub.coefficient}  |  Barème : /${sub.maxScore.toInt()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            IconButton(
                                onClick = {
                                    viewModel.deleteSubject(sub)
                                    Toast.makeText(context, "Matière supprimée", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GradesEntryTab(
    viewModel: SchoolViewModel,
    selectedSection: String,
    selectedGrade: String,
    selectedTerm: String,
    onTermSelected: (String) -> Unit,
    terms: List<String>,
    students: List<Student>,
    subjects: List<Subject>,
    allGrades: List<StudentGrade>
) {
    val context = LocalContext.current
    var selectedSubject by remember(subjects) { mutableStateOf(subjects.firstOrNull()) }

    LaunchedEffect(subjects) {
        if (selectedSubject == null || selectedSubject !in subjects) {
            selectedSubject = subjects.firstOrNull()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Term & Subject Selectors Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Term Selector
            var termExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = termExpanded,
                onExpandedChange = { termExpanded = !termExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = selectedTerm,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Nature des résultats") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = termExpanded) },
                    modifier = Modifier.menuAnchor(),
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = termExpanded,
                    onDismissRequest = { termExpanded = false }
                ) {
                    terms.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(t) },
                            onClick = {
                                onTermSelected(t)
                                termExpanded = false
                            }
                        )
                    }
                }
            }

            // Subject Selector
            var subjectExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = subjectExpanded,
                onExpandedChange = { subjectExpanded = !subjectExpanded },
                modifier = Modifier.weight(1.2f)
            ) {
                OutlinedTextField(
                    value = selectedSubject?.name ?: "Aucune matière",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Matière") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                    modifier = Modifier.menuAnchor(),
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = subjectExpanded,
                    onDismissRequest = { subjectExpanded = false }
                ) {
                    subjects.forEach { s ->
                        DropdownMenuItem(
                            text = { Text(s.name) },
                            onClick = {
                                selectedSubject = s
                                subjectExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (subjects.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚠️ Veuillez d'abord ajouter des matières dans l'onglet 'Matières' pour la classe $selectedGrade.", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }
            }
        } else if (students.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aucun élève inscrit dans la classe $selectedGrade", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            val sub = selectedSubject ?: return

            Text(
                "Notes pour ${sub.name} (Coeff ${sub.coefficient}) • $selectedTerm",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(students, key = { it.id }) { student ->
                    val studentGradeObj = allGrades.find { it.studentId == student.id && it.subjectId == sub.id && it.term == selectedTerm }

                    var evalText by remember(studentGradeObj) {
                        mutableStateOf(studentGradeObj?.evaluationScore?.let { String.format(Locale.US, "%.1f", it) } ?: "")
                    }
                    var examText by remember(studentGradeObj) {
                        mutableStateOf(studentGradeObj?.examScore?.let { String.format(Locale.US, "%.1f", it) } ?: "")
                    }

                    val evalVal = evalText.toFloatOrNull()
                    val examVal = examText.toFloatOrNull()
                    val averageVal = evalVal

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    val photoBmp = remember(student.photoBase64) {
                                        if (!student.photoBase64.isNullOrBlank()) {
                                            try {
                                                val bytes = android.util.Base64.decode(student.photoBase64, android.util.Base64.DEFAULT)
                                                android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                                            } catch (e: Exception) { null }
                                        } else null
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (photoBmp != null) {
                                            Image(bitmap = photoBmp, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                        } else {
                                            Text("${student.firstName.take(1)}${student.lastName.take(1)}", fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Column {
                                        Text("${student.firstName} ${student.lastName}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        val mat = if (student.remoteId.length >= 5) student.remoteId.take(5).uppercase() else student.id.toString()
                                        Text("Matricule : #$mat", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                if (averageVal != null) {
                                    val isPassing = averageVal >= (sub.maxScore / 2f)
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = if (isPassing) Color(0xFF1B5E20) else MaterialTheme.colorScheme.errorContainer
                                    ) {
                                        Text(
                                            "Moy. ${String.format(Locale.US, "%.2f", averageVal)} / ${sub.maxScore.toInt()}",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isPassing) Color.White else MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = evalText,
                                    onValueChange = { evalText = it },
                                    label = { Text("Note (/${sub.maxScore.toInt()})") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )

                                IconButton(
                                    onClick = {
                                        val eScore = evalText.toFloatOrNull()
                                        viewModel.saveGrade(
                                            studentId = student.id,
                                            studentRemoteId = student.remoteId,
                                            subjectId = sub.id,
                                            subjectRemoteId = sub.remoteId,
                                            term = selectedTerm,
                                            evaluationScore = eScore,
                                            examScore = null
                                        )
                                        Toast.makeText(context, "Note enregistrée pour ${student.firstName}", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = "Enregistrer", tint = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BulletinPdfTab(
    selectedSection: String,
    selectedGrade: String,
    selectedTerm: String,
    onTermSelected: (String) -> Unit,
    terms: List<String>,
    students: List<Student>,
    subjects: List<Subject>,
    allGrades: List<StudentGrade>,
    selectedStudent: Student?,
    onStudentSelected: (Student) -> Unit,
    onGeneratePdf: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Term & Student Selectors
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Term Selector
            var termExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = termExpanded,
                onExpandedChange = { termExpanded = !termExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = selectedTerm,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Nature des résultats") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = termExpanded) },
                    modifier = Modifier.menuAnchor(),
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = termExpanded,
                    onDismissRequest = { termExpanded = false }
                ) {
                    terms.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(t) },
                            onClick = {
                                onTermSelected(t)
                                termExpanded = false
                            }
                        )
                    }
                }
            }

            // Student Selector
            var studentExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = studentExpanded,
                onExpandedChange = { studentExpanded = !studentExpanded },
                modifier = Modifier.weight(1.3f)
            ) {
                OutlinedTextField(
                    value = selectedStudent?.let { "${it.firstName} ${it.lastName}" } ?: "Choisir un élève",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Élève") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = studentExpanded) },
                    modifier = Modifier.menuAnchor(),
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = studentExpanded,
                    onDismissRequest = { studentExpanded = false }
                ) {
                    students.forEach { st ->
                        DropdownMenuItem(
                            text = { Text("${st.firstName} ${st.lastName}") },
                            onClick = {
                                onStudentSelected(st)
                                studentExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedStudent == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Veuillez sélectionner un élève dans la classe $selectedGrade", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            val st = selectedStudent
            val classGrades = allGrades.filter { it.term == selectedTerm }
            val summary = remember(st, selectedTerm, students, subjects, classGrades) {
                ReportCardPdfUtils.calculateSummary(
                    student = st,
                    term = selectedTerm,
                    allStudentsInClass = students.filter { it.grade == st.grade && it.section == st.section },
                    subjects = subjects.filter { it.grade == st.grade && it.section == st.section },
                    allGradesForClassAndTerm = classGrades
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Aperçu des Résultats • $selectedTerm", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Moyenne Générale", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            val base = if (selectedSection.equals("LE PRIMAIRE", ignoreCase = true)) 10 else 20
                            Text("${String.format(Locale.US, "%.2f", summary.average)} / $base", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Rang dans la classe", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            val suffix = if (summary.rank == 1) "er" else "ème"
                            Text("${summary.rank}$suffix sur ${summary.classSize}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Points : ${String.format(Locale.US, "%.2f", summary.totalPoints)}", style = MaterialTheme.typography.bodySmall)
                        Text("Mention : ${summary.mention}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onGeneratePdf,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("GÉNÉRER LE BULLETIN PDF INFALSIFIABLE", fontWeight = FontWeight.Bold)
            }
        }
    }
}
