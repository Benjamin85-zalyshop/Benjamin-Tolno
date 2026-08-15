import re

with open('app/src/main/java/com/example/ui/screens/AdminDashboardScreen.kt', 'r') as f:
    code = f.read()

code = code.replace("item.subscriptionExpiryDate < currentTime", "(item.subscriptionExpiryDate ?: 0L) < currentTime")
code = code.replace("item.subscriptionExpiryDate > currentTime", "(item.subscriptionExpiryDate ?: 0L) > currentTime")
code = code.replace("formatDate(item.subscriptionExpiryDate)", "formatDate(item.subscriptionExpiryDate ?: 0L)")

with open('app/src/main/java/com/example/ui/screens/AdminDashboardScreen.kt', 'w') as f:
    f.write(code)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    code = f.read()
code = code.replace('NumberFormat.getNumberInstance(Locale("fr", "GN")).format(unpaidBalance) + " $currency"', 'NumberFormat.getNumberInstance(Locale("fr", "GN")).format(unpaidBalance) + " GNF"')
with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(code)
