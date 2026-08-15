import re
import glob

def patch_file(filepath, replacements):
    with open(filepath, 'r') as f:
        code = f.read()
    
    for k, v in replacements:
        code = code.replace(k, v)
        
    with open(filepath, 'w') as f:
        f.write(code)

# 1. StudentDetailScreen
patch_file('app/src/main/java/com/example/ui/screens/StudentDetailScreen.kt', [
    ("val schoolLogoBase64 by viewModel.schoolLogoBase64.collectAsStateWithLifecycle()", "val schoolLogoBase64 by viewModel.schoolLogoBase64.collectAsStateWithLifecycle()\n    val schoolAccount by viewModel.schoolAccount.collectAsStateWithLifecycle()\n    val currency = schoolAccount?.currency ?: \"GNF\""),
    (' GNF"', ' $currency"'),
    ('GNF)', '$currency)')
])

# 2. StudentsScreen
patch_file('app/src/main/java/com/example/ui/screens/StudentsScreen.kt', [
    ("val userRole by viewModel.userRole.collectAsStateWithLifecycle()", "val userRole by viewModel.userRole.collectAsStateWithLifecycle()\n    val schoolAccount by viewModel.schoolAccount.collectAsStateWithLifecycle()\n    val currency = schoolAccount?.currency ?: \"GNF\""),
    (' GNF"', ' $currency"')
])

# 3. ExpensesScreen
patch_file('app/src/main/java/com/example/ui/screens/ExpensesScreen.kt', [
    ("val userRole by viewModel.userRole.collectAsStateWithLifecycle()", "val userRole by viewModel.userRole.collectAsStateWithLifecycle()\n    val schoolAccount by viewModel.schoolAccount.collectAsStateWithLifecycle()\n    val currency = schoolAccount?.currency ?: \"GNF\""),
    (' GNF"', ' $currency"')
])

# 4. AddStudentScreen
patch_file('app/src/main/java/com/example/ui/screens/AddStudentScreen.kt', [
    ("val context = LocalContext.current", "val context = LocalContext.current\n    val schoolAccount by viewModel.schoolAccount.collectAsStateWithLifecycle()\n    val currency = schoolAccount?.currency ?: \"GNF\""),
    ('GNF"', '$currency"'),
    ('GNF)', '$currency)')
])

# 5. AddExpenseScreen
patch_file('app/src/main/java/com/example/ui/screens/AddExpenseScreen.kt', [
    ("val context = LocalContext.current", "val context = LocalContext.current\n    val schoolAccount by viewModel.schoolAccount.collectAsStateWithLifecycle()\n    val currency = schoolAccount?.currency ?: \"GNF\""),
    ('GNF"', '$currency"'),
    ('GNF)', '$currency)')
])

# 6. QrScannerDialog
patch_file('app/src/main/java/com/example/ui/screens/QrScannerDialog.kt', [
    ("fun QrScannerDialog(", "fun QrScannerDialog(\n    currency: String = \"GNF\","),
    (' GNF"', ' $currency"')
])
patch_file('app/src/main/java/com/example/ui/screens/StudentsScreen.kt', [
    ("QrScannerDialog(", "QrScannerDialog(currency = currency,")
])
patch_file('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', [
    ("QrScannerDialog(", "QrScannerDialog(currency = currency,")
])

# 7. Ticket58mmDialog
patch_file('app/src/main/java/com/example/ui/screens/Ticket58mmDialog.kt', [
    ("fun Ticket58mmDialog(", "fun Ticket58mmDialog(\n    currency: String = \"GNF\","),
    (' GNF"', ' $currency"')
])
patch_file('app/src/main/java/com/example/ui/screens/StudentDetailScreen.kt', [
    ("Ticket58mmDialog(", "Ticket58mmDialog(currency = currency,")
])
patch_file('app/src/main/java/com/example/ui/screens/AddPaymentScreen.kt', [
    ("Ticket58mmDialog(", "Ticket58mmDialog(currency = currency,")
])
patch_file('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', [
    ("Ticket58mmDialog(", "Ticket58mmDialog(currency = currency,")
])

# 8. ReceiptPrinter
patch_file('app/src/main/java/com/example/ui/ReceiptPrinter.kt', [
    ("fun printReceipt(", "fun printReceipt(currency: String = \"GNF\","),
    (' GNF', ' $currency')
])
patch_file('app/src/main/java/com/example/ui/screens/Ticket58mmDialog.kt', [
    ("ReceiptPrinter.printReceipt(", "ReceiptPrinter.printReceipt(currency = currency,")
])
