with open('app/src/main/java/com/example/ui/screens/AdminDashboardScreen.kt', 'r') as f:
    code = f.read()

code = code.replace("item.subscriptionExpiryDate > 0", "(item.subscriptionExpiryDate ?: 0L) > 0")
code = code.replace("item.subscriptionExpiryDate <=", "(item.subscriptionExpiryDate ?: 0L) <=")
code = code.replace("java.util.Date(item.subscriptionExpiryDate)", "java.util.Date(item.subscriptionExpiryDate ?: 0L)")

with open('app/src/main/java/com/example/ui/screens/AdminDashboardScreen.kt', 'w') as f:
    f.write(code)

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    code = f.read()

code = code.replace("import com.example.ui.SchoolViewModel", "import com.example.ui.SchoolViewModel\nimport com.example.ui.SchoolViewModelFactory")

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(code)
