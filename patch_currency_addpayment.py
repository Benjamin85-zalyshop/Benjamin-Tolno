import re

with open('app/src/main/java/com/example/ui/screens/AddPaymentScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

target = '    val context = LocalContext.current'
replacement = '    val context = LocalContext.current\n    val schoolAccount by viewModel.schoolAccount.collectAsStateWithLifecycle()\n    val currency = schoolAccount?.currency ?: "GNF"'

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/screens/AddPaymentScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)
