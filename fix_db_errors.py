with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    code = f.read()

code = code.replace('else selectedSchoolYear,', 'else (selectedSchoolYear ?: ""),')
code = code.replace('text = "${numberFormat.format(expenses)} $currency"', 'text = "${numberFormat.format(expenses)} GNF"')
code = code.replace('text = "${numberFormat.format(expense.amount)} $currency"', 'text = "${numberFormat.format(expense.amount)} GNF"')
code = code.replace('text = "${numberFormat.format(totalAmount)} $currency"', 'text = "${numberFormat.format(totalAmount)} GNF"')

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(code)
