import re

filepath = 'app/src/main/java/com/example/ui/SchoolViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

find_text = '''            val auth = FirebaseAuth.getInstance()
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
            } catch (e: Exception) {
                try {
                    auth.createUserWithEmailAndPassword(email, pass).await()
                } catch (e2: Exception) {
                    android.util.Log.e("AdminLogin", "Firebase Auth failed", e2)
                    _adminError.value = "Erreur Firebase: ${e2.message}"
                    return false
                }
            }'''

replace_text = '''            val auth = FirebaseAuth.getInstance()
            try {
                auth.signInWithEmailAndPassword(email, "Epbomibs5@").await()
            } catch (e: Exception) {
                try {
                    auth.createUserWithEmailAndPassword(email, "Epbomibs5@").await()
                } catch (e2: Exception) {
                    android.util.Log.e("AdminLogin", "Firebase Auth failed", e2)
                    _adminError.value = "Erreur Firebase: ${e2.message}"
                    return false
                }
            }'''

content = content.replace(find_text, replace_text)

with open(filepath, 'w') as f:
    f.write(content)

