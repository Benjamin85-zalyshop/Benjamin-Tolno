import re

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Replace hardcoded GNF with dynamic currency from schoolAccount
content = content.replace('val currency = schoolAccount?.currency ?: "GNF"', '') # Clean up if I added it before
content = re.sub(r'fun DashboardScreen\(.*?\)\s*{', r'\g<0>\n    val currency = schoolAccount?.currency ?: "GNF"', content, count=1)

# Now replace all literal " GNF" or "GNF" with the dynamic currency.
# E.g. "$formattedCollected GNF" -> "$formattedCollected $currency"
content = content.replace(' GNF"', ' $currency"')
content = content.replace(' GNF/', ' $currency/')
content = content.replace(' (GNF)', ' ($currency)')
content = content.replace('Frais (GNF)', 'Frais ($currency)')

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)

with open('app/src/main/java/com/example/ui/screens/AddPaymentScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

content = re.sub(r'fun AddPaymentScreen\(.*?\)\s*{', r'\g<0>\n    val schoolAccount by viewModel.schoolAccount.collectAsState()\n    val currency = schoolAccount?.currency ?: "GNF"', content, count=1)
content = content.replace(' GNF"', ' $currency"')
content = content.replace(' (GNF)', ' ($currency)')
content = content.replace('Frais (GNF)', 'Frais ($currency)')

with open('app/src/main/java/com/example/ui/screens/AddPaymentScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)

