import re

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# I see I added `val currency = schoolAccount?.currency ?: "GNF"` right inside fun DashboardScreen.
# BUT I probably added it BEFORE `schoolAccount` was collected!
# Let's remove it from the top and put it AFTER schoolAccount is collected.

content = content.replace('    val currency = schoolAccount?.currency ?: "GNF"\n', '')

target = '    val schoolAccount by viewModel.schoolAccount.collectAsStateWithLifecycle()'
replacement = '    val schoolAccount by viewModel.schoolAccount.collectAsStateWithLifecycle()\n    val currency = schoolAccount?.currency ?: "GNF"'
content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)

with open('app/src/main/java/com/example/ui/screens/AddPaymentScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('    val schoolAccount by viewModel.schoolAccount.collectAsState()\n    val currency = schoolAccount?.currency ?: "GNF"\n', '')
target2 = '    val context = LocalContext.current'
replacement2 = '    val context = LocalContext.current\n    val schoolAccount by viewModel.schoolAccount.collectAsStateWithLifecycle()\n    val currency = schoolAccount?.currency ?: "GNF"'
content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/ui/screens/AddPaymentScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)
