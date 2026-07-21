import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

replacement = """                    try {
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(lastEmail, "Epbomibs5@").await()
                    } catch (e: Exception) {
                        android.util.Log.e("SchoolViewModel", "signInWithEmailAndPassword failed: ${e.message}", e)
                        try {
                            FirebaseAuth.getInstance().createUserWithEmailAndPassword(lastEmail, "Epbomibs5@").await()
                        } catch (e2: Exception) {
                            android.util.Log.e("SchoolViewModel", "createUserWithEmailAndPassword failed: ${e2.message}", e2)
                        }
                    }
                    if (FirebaseAuth.getInstance().currentUser == null) {
                        android.util.Log.e("SchoolViewModel", "currentUser is still null in init")
                        clearSession()
                        _userRole.value = null
                        return@launch
                    }"""

content = content.replace(
    '                    try {\n                        FirebaseAuth.getInstance().signInWithEmailAndPassword(lastEmail, "Epbomibs5@").await()\n                    } catch (e: Exception) {\n                        try {\n                            FirebaseAuth.getInstance().createUserWithEmailAndPassword(lastEmail, "Epbomibs5@").await()\n                        } catch (e2: Exception) { e2.printStackTrace() }\n                    }\n                    if (FirebaseAuth.getInstance().currentUser == null) {\n                        clearSession()\n                        _userRole.value = null\n                        return@launch\n                    }', replacement)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
