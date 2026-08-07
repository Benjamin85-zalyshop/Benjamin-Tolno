package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import com.example.ui.SchoolViewModel
import com.example.ui.components.PhotoSourceDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentScreen(
    viewModel: SchoolViewModel,
    onNavigateBack: () -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("") }
    var section by remember { mutableStateOf(SECTIONS[1]) }
    var parentWhatsApp by remember { mutableStateOf("") }
    var registrationFee by remember { mutableStateOf("") }
    var reenrollmentFee by remember { mutableStateOf("") }
    var photoBase64 by remember { mutableStateOf<String?>(null) }
    var showPhotoDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var gradeExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val gradeSuggestions = remember(section) {
        DEFAULT_CLASSES_BY_SECTION[section] ?: emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajouter un Élève") },
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
            // Photo Selection Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val decodedBytes = remember(photoBase64) {
                    if (!photoBase64.isNullOrBlank()) {
                        try { android.util.Base64.decode(photoBase64, android.util.Base64.DEFAULT) } catch (e: Exception) { null }
                    } else null
                }
                val photoBitmap = remember(decodedBytes) {
                    decodedBytes?.let { android.graphics.BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap() }
                }

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
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
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Photo",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Column {
                    Text("Photo de l'élève", fontWeight = FontWeight.Bold)
                    Text("Pour la carte scolaire PDF", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { showPhotoDialog = true }
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (photoBase64 != null) "Changer photo" else "Ajouter photo")
                    }
                }
            }

            if (showPhotoDialog) {
                PhotoSourceDialog(
                    onDismiss = { showPhotoDialog = false },
                    onPhotoCaptured = { photoBase64 = it },
                    hasCurrentPhoto = photoBase64 != null
                )
            }
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("Prénom") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                singleLine = true
            )
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Nom") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                singleLine = true
            )
            
            // Section dropdown first
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
            ) {
                OutlinedTextField(
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    readOnly = true,
                    value = section,
                    onValueChange = {},
                    label = { Text("Section") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    SECTIONS.drop(1).forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                section = selectionOption
                                expanded = false
                                // Clear grade or auto-select first of section if empty
                                grade = ""
                            }
                        )
                    }
                }
            }

            // Grade dropdown / autocomplete second
            ExposedDropdownMenuBox(
                expanded = gradeExpanded,
                onExpandedChange = { gradeExpanded = !gradeExpanded },
            ) {
                OutlinedTextField(
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    value = grade,
                    onValueChange = { 
                        grade = it
                        gradeExpanded = true
                    },
                    label = { Text("Classe (ex: 10ème Année)") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    singleLine = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gradeExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                )
                
                if (gradeSuggestions.isNotEmpty()) {
                    val filteredSuggestions = gradeSuggestions.filter { 
                        it.contains(grade, ignoreCase = true) 
                    }
                    if (filteredSuggestions.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = gradeExpanded,
                            onDismissRequest = { gradeExpanded = false },
                        ) {
                            filteredSuggestions.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        grade = selectionOption
                                        gradeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = parentWhatsApp,
                onValueChange = { parentWhatsApp = it },
                label = { Text("Numéro WhatsApp des parents (Optionnel)") },
                placeholder = { Text("Ex: +224621123456") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "WhatsApp"
                    )
                }
            )

            OutlinedTextField(
                value = registrationFee,
                onValueChange = { registrationFee = it },
                label = { Text("Frais d'inscription (Optionnel)") },
                placeholder = { Text("Ex: 150000") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                suffix = { Text("GNF") }
            )

            OutlinedTextField(
                value = reenrollmentFee,
                onValueChange = { reenrollmentFee = it },
                label = { Text("Frais de réinscription (Optionnel)") },
                placeholder = { Text("Ex: 100000") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                suffix = { Text("GNF") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (firstName.isNotBlank() && lastName.isNotBlank() && grade.isNotBlank()) {
                        viewModel.insertStudent(
                            firstName = firstName.trim(),
                            lastName = lastName.trim(),
                            grade = grade.trim(),
                            section = section,
                            parentWhatsApp = parentWhatsApp.trim().ifBlank { null },
                            registrationFee = registrationFee.toLongOrNull() ?: 0L,
                            reenrollmentFee = reenrollmentFee.toLongOrNull() ?: 0L,
                            photoBase64 = photoBase64
                        )
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = firstName.isNotBlank() && lastName.isNotBlank() && grade.isNotBlank()
            ) {
                Text("Enregistrer")
            }
        }
    }
}
