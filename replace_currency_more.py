import re
import glob

def patch_file(filepath, replacements):
    with open(filepath, 'r') as f:
        code = f.read()
    
    for k, v in replacements:
        code = code.replace(k, v)
        
    with open(filepath, 'w') as f:
        f.write(code)


# DashboardScreen
patch_file('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', [
    ('500 000 GNF', '500 000 $currency'),
    ('200 000 GNF', '200 000 $currency'),
    ('(en GNF)', '(en $currency)'),
    ('Text("GNF")', 'Text(currency)'),
    ('format(unpaidBalance) + " GNF"', 'format(unpaidBalance) + " $currency"'),
    ('format(totalAmount)} GNF', 'format(totalAmount)} $currency'),
    ('formattedAmount GNF', 'formattedAmount $currency'),
])

# SubscriptionScreen
try:
    patch_file('app/src/main/java/com/example/ui/screens/SubscriptionScreen.kt', [
        ('500 000 GNF', '500 000 GNF'), # Just leave Subscription screen as is, since we don't have currency here maybe
    ])
except: pass

# AdminDashboardScreen
try:
    patch_file('app/src/main/java/com/example/ui/screens/AdminDashboardScreen.kt', [
        ('500 000 GNF', '500 000 GNF'),
    ])
except: pass

# StudentDetailScreen
patch_file('app/src/main/java/com/example/ui/screens/StudentDetailScreen.kt', [
    ('} GNF', '} $currency'),
    ('Amount GNF', 'Amount $currency')
])

# AddPaymentScreen
patch_file('app/src/main/java/com/example/ui/screens/AddPaymentScreen.kt', [
    ('} GNF', '} $currency')
])

# AddStudentScreen
patch_file('app/src/main/java/com/example/ui/screens/AddStudentScreen.kt', [
    ('Text("GNF")', 'Text(currency)')
])

