import re

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

settings_dialog = """
    if (showSettingsDialog) {
        val currencies = listOf("GNF", "XOF", "XAF", "EUR", "USD")
        var expandedCurrency by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Paramètres de l'école", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Text("Devise principale", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    ExposedDropdownMenuBox(
                        expanded = expandedCurrency,
                        onExpandedChange = { expandedCurrency = !expandedCurrency }
                    ) {
                        OutlinedTextField(
                            value = selectedCurrency,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Devise") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCurrency) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCurrency,
                            onDismissRequest = { expandedCurrency = false }
                        ) {
                            currencies.forEach { currency ->
                                DropdownMenuItem(
                                    text = { Text(currency) },
                                    onClick = {
                                        selectedCurrency = currency
                                        expandedCurrency = false
                                        viewModel.updateCurrency(currency)
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Sécurité", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    OutlinedButton(
                        onClick = { 
                            showSettingsDialog = false
                            newFinancierPassword = ""
                            showFinancierMgmtDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Gérer le mot de passe Financier")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) { Text("Fermer") }
            }
        )
    }
"""

content = content.replace("if (showFinancierMgmtDialog) {", settings_dialog + "\n    if (showFinancierMgmtDialog) {")

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)
