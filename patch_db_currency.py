import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace(
    'var dbFounderPhone = ""\n            try {',
    'var dbFounderPhone = ""\n            var dbCurrency = "GNF"\n            try {'
)
content = content.replace(
    'dbCurrency = doc.getString("currency") ?: "GNF"\n                    dbCurrency = doc.getString("currency") ?: "GNF"',
    'dbCurrency = doc.getString("currency") ?: "GNF"'
)

# And in AddPaymentScreen.kt:
with open('app/src/main/java/com/example/ui/screens/AddPaymentScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace(
    'val context = LocalContext.current\n    val schoolAccount by viewModel.schoolAccount.collectAsStateWithLifecycle()\n    val currency = schoolAccount?.currency ?: "GNF"\n    val schoolAccount by viewModel.schoolAccount.collectAsStateWithLifecycle()\n    val currency = schoolAccount?.currency ?: "GNF"',
    'val context = LocalContext.current\n    val schoolAccount by viewModel.schoolAccount.collectAsStateWithLifecycle()\n    val currency = schoolAccount?.currency ?: "GNF"'
)
content = content.replace(
    'val schoolAccount by viewModel.schoolAccount.collectAsStateWithLifecycle()\n    val currency = schoolAccount?.currency ?: "GNF"\n    \n    val students',
    'val schoolAccount by viewModel.schoolAccount.collectAsStateWithLifecycle()\n    val currency = schoolAccount?.currency ?: "GNF"\n    \n    val students'
)
# Make sure AddPaymentScreen only has ONE declaration of schoolAccount and currency.

with open('app/src/main/java/com/example/ui/screens/AddPaymentScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
