import re

filepath = 'app/src/main/java/com/example/ui/screens/AcademicScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

old_block = """                            IconButton(
                                onClick = {
                                    viewModel.deleteSubject(sub)
                                    Toast.makeText(context, "Matière supprimée", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error)
                            }"""

new_block = """                            Row {
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
                            }"""

if old_block in content:
    content = content.replace(old_block, new_block)
    print("Replaced successfully!")
else:
    print("Block not found!")
    
with open(filepath, 'w') as f:
    f.write(content)
