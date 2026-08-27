import re

with open('app/src/main/java/com/example/ui/screens/AdminDashboardScreen.kt', 'r') as f:
    content = f.read()

target = """                            onForceExpireClick = {
                                viewModel.forceExpireSchool(item.email)
                            }"""

new_target = """                            onForceExpireClick = {
                                viewModel.forceExpireSchool(item.email)
                                android.widget.Toast.makeText(localContext, "Expiration simulée pour ${item.email}", android.widget.Toast.LENGTH_SHORT).show()
                            }"""

content = content.replace(target, new_target)

with open('app/src/main/java/com/example/ui/screens/AdminDashboardScreen.kt', 'w') as f:
    f.write(content)
