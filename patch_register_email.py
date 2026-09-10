import re

filepath = 'app/src/main/java/com/example/ui/screens/RegisterScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

old_block = """                    if (founderPassword.length < 6) {
                        errorMessage = "Le mot de passe Fondateur doit contenir au moins 6 caractères."
                    }"""

new_block = """                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                        errorMessage = "Veuillez entrer une adresse e-mail valide."
                    } else if (founderPassword.length < 6) {
                        errorMessage = "Le mot de passe Fondateur doit contenir au moins 6 caractères."
                    }"""

content = content.replace(old_block, new_block)

with open(filepath, 'w') as f:
    f.write(content)
