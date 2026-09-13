import re

filepath = 'app/src/main/java/com/example/ui/SchoolViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace(
    '_adminError.value = "Erreur de synchronisation: ${e.message}"',
    '_adminError.value = "Erreur: ${e.message} (User: ${FirebaseAuth.getInstance().currentUser?.email})"'
)

# Also fix the check to be auth.currentUser?.email != "benjamintolno7@gmail.com"
content = content.replace(
    'if (auth.currentUser == null) {',
    'if (auth.currentUser?.email != "benjamintolno7@gmail.com") {'
)

with open(filepath, 'w') as f:
    f.write(content)

