import re
with open('app/src/main/java/com/example/ui/screens/ExpensesScreen.kt', 'r') as f:
    content = f.read()

replacement = """    var selectedMonth by remember { mutableStateOf<String?>(null) }
    var expenseToDelete by remember { mutableStateOf<com.example.data.models.Expense?>(null) }"""

content = content.replace("    var selectedMonth by remember { mutableStateOf<String?>(null) }", replacement)

with open('app/src/main/java/com/example/ui/screens/ExpensesScreen.kt', 'w') as f:
    f.write(content)
