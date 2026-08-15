import re

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

target = """                                QuickAccessButton(
                                    title = "Paramètres",
                                    icon = Icons.Default.Settings,
                                    iconColor = Color(0xFF6B7280),
                                    bgColor = Color(0xFFF3F4F6),
                                    onClick = {
                                        if (userRole == "FOUNDER") {
                                            newFinancierPassword = ""
                                            showFinancierMgmtDialog = true
                                        } else {
                                            Toast.makeText(context, "Réservé au fondateur de l'école", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )"""

replacement = """                                QuickAccessButton(
                                    title = "Paramètres",
                                    icon = Icons.Default.Settings,
                                    iconColor = Color(0xFF6B7280),
                                    bgColor = Color(0xFFF3F4F6),
                                    onClick = {
                                        if (userRole == "FOUNDER") {
                                            selectedCurrency = schoolAccount?.currency ?: "GNF"
                                            showSettingsDialog = true
                                        } else {
                                            Toast.makeText(context, "Réservé au fondateur de l'école", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)
