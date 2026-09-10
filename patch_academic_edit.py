import re

filepath = 'app/src/main/java/com/example/ui/screens/AcademicScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

# 1. Add state vars inside SubjectsTab
old_signature = """@Composable
private fun SubjectsTab(
    viewModel: SchoolViewModel,
    selectedSection: String,
    selectedGrade: String,
    subjects: List<Subject>,
    onOpenAddDialog: () -> Unit
) {
    val context = LocalContext.current"""

new_signature = """@Composable
private fun SubjectsTab(
    viewModel: SchoolViewModel,
    selectedSection: String,
    selectedGrade: String,
    subjects: List<Subject>,
    onOpenAddDialog: () -> Unit
) {
    val context = LocalContext.current
    var subjectToEdit by remember { mutableStateOf<com.example.data.local.Subject?>(null) }
    var editNameInput by remember(subjectToEdit) { mutableStateOf(subjectToEdit?.name ?: "") }
    var editCoeffInput by remember(subjectToEdit) { mutableStateOf(subjectToEdit?.coefficient?.toString() ?: "") }
    var editMaxInput by remember(subjectToEdit) { mutableStateOf(subjectToEdit?.maxScore?.toInt()?.toString() ?: "") }"""

content = content.replace(old_signature, new_signature)

# 2. Update Row to have Edit button next to Delete button
old_row = """                                Column {
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
                        }"""

new_row = """                                Column {
                                    Text(sub.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                    Text("Coefficient : ${sub.coefficient}  |  Barème : /${sub.maxScore.toInt()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Row {
                                IconButton(
                                    onClick = { subjectToEdit = sub }
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = MaterialTheme.colorScheme.primary)
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
                        }"""

content = content.replace(old_row, new_row)

# 3. Add the Dialog at the end of the SubjectsTab function
old_end = """                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)"""

new_end = """                }
            }
        }
    }
    
    if (subjectToEdit != null) {
        AlertDialog(
            onDismissRequest = { subjectToEdit = null },
            title = { Text("Modifier la Matière", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editNameInput,
                        onValueChange = { editNameInput = it },
                        label = { Text("Nom de la Matière") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editCoeffInput,
                            onValueChange = { editCoeffInput = it },
                            label = { Text("Coefficient") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editMaxInput,
                            onValueChange = { editMaxInput = it },
                            label = { Text("Barème") },
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
                        val name = editNameInput.trim()
                        val coeff = editCoeffInput.toIntOrNull() ?: 1
                        val maxScore = editMaxInput.toFloatOrNull() ?: 20f
                        if (name.isNotEmpty()) {
                            viewModel.updateSubjectDetails(subjectToEdit!!, name, coeff, maxScore)
                            Toast.makeText(context, "Matière mise à jour", Toast.LENGTH_SHORT).show()
                            subjectToEdit = null
                        }
                    }
                ) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = { subjectToEdit = null }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)"""

content = content.replace(old_end, new_end)

with open(filepath, 'w') as f:
    f.write(content)
