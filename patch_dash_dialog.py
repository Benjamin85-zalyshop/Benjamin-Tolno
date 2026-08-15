import re

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace(
    'var showSupportDialog by remember { mutableStateOf(false) }',
    'var showSupportDialog by remember { mutableStateOf(false) }\n    var showSettingsDialog by remember { mutableStateOf(false) }\n    var selectedCurrency by remember { mutableStateOf(schoolAccount?.currency ?: "GNF") }'
)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)
