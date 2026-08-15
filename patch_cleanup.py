import re

with open('app/src/main/java/com/example/ui/screens/AddPaymentScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('    val schoolAccount by viewModel.schoolAccount.collectAsStateWithLifecycle()\n    val currency = schoolAccount?.currency ?: "GNF"\n    val schoolAccount by viewModel.schoolAccount.collectAsStateWithLifecycle()\n    val currency = schoolAccount?.currency ?: "GNF"', '    val schoolAccount by viewModel.schoolAccount.collectAsStateWithLifecycle()\n    val currency = schoolAccount?.currency ?: "GNF"')

with open('app/src/main/java/com/example/ui/screens/AddPaymentScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)
