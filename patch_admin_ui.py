with open('app/src/main/java/com/example/ui/screens/AdminDashboardScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("viewModel.loadAdminSchools()", "viewModel.forceSyncSchools()")

with open('app/src/main/java/com/example/ui/screens/AdminDashboardScreen.kt', 'w') as f:
    f.write(content)
