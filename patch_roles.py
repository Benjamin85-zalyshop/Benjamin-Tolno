import re

filepath = 'app/src/main/java/com/example/ui/screens/DashboardScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Add isFounder to the top of the dialog
old_dialog_start = """    // F. DIALOG: SCOLARITÉ & RECOUVREMENT
    if (showScolariteDialog) {
        var scolariteTab by remember { mutableIntStateOf(0) }"""
new_dialog_start = """    // F. DIALOG: SCOLARITÉ & RECOUVREMENT
    if (showScolariteDialog) {
        val isFounder = userRole == null || userRole.equals("FOUNDER", ignoreCase = true) || userRole.equals("FONDATEUR", ignoreCase = true) || userRole.equals("ADMIN", ignoreCase = true)
        var scolariteTab by remember { mutableIntStateOf(0) }"""
content = content.replace(old_dialog_start, new_dialog_start)

# Hide Logo buttons
old_logo_buttons = """                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                                                Button(
                                                    onClick = { pickImageLauncher.launch("image/*") },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F56E3)),
                                                    modifier = Modifier.height(32.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                                ) {
                                                    Text(if (schoolLogoBase64 != null) "Changer" else "Importer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                                if (schoolLogoBase64 != null) {
                                                    OutlinedButton(
                                                        onClick = { viewModel.setSchoolLogo(null) },
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                                        border = BorderStroke(1.dp, Color.Red),
                                                        modifier = Modifier.height(32.dp),
                                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                                    ) {
                                                        Text("Supprimer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }"""

new_logo_buttons = """                                            if (isFounder) {
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                                                    Button(
                                                        onClick = { pickImageLauncher.launch("image/*") },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F56E3)),
                                                        modifier = Modifier.height(32.dp),
                                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                                    ) {
                                                        Text(if (schoolLogoBase64 != null) "Changer" else "Importer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    if (schoolLogoBase64 != null) {
                                                        OutlinedButton(
                                                            onClick = { viewModel.setSchoolLogo(null) },
                                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                                            border = BorderStroke(1.dp, Color.Red),
                                                            modifier = Modifier.height(32.dp),
                                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                                        ) {
                                                            Text("Supprimer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }"""
content = content.replace(old_logo_buttons, new_logo_buttons)

# Hide Modifier buttons
old_modifier = """                                            Button(
                                                onClick = {
                                                    editGradeFeeTarget = grade
                                                    editFeeAmountString = if (currentFee > 0) currentFee.toString() else ""
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF), contentColor = Color(0xFF1E3A8A)),
                                                modifier = Modifier.height(32.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                            ) {
                                                Text("Modifier", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }"""

new_modifier = """                                            if (isFounder) {
                                                Button(
                                                    onClick = {
                                                        editGradeFeeTarget = grade
                                                        editFeeAmountString = if (currentFee > 0) currentFee.toString() else ""
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF), contentColor = Color(0xFF1E3A8A)),
                                                    modifier = Modifier.height(32.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                                ) {
                                                    Text("Modifier", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }"""
content = content.replace(old_modifier, new_modifier)

with open(filepath, 'w') as f:
    f.write(content)

