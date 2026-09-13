import re

filepath = 'app/src/main/java/com/example/ui/SchoolViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

find_text = '''                    android.util.Log.e("AdminLogin", "Firebase Auth failed", e2)
                    _adminError.value = "Erreur: Firebase a bloqué l'accès ou mot de passe incorrect."
                    return false'''

replace_text = '''                    android.util.Log.e("AdminLogin", "Firebase Auth failed", e2)
                    _adminError.value = "Erreur Firebase: ${e2.message}"
                    return false'''

content = content.replace(find_text, replace_text)

with open(filepath, 'w') as f:
    f.write(content)

