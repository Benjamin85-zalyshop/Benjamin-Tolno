import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

pattern = r"""                try \{
                    FirebaseAuth\.getInstance\(\)\.signInWithEmailAndPassword\(cleanEmail, password\)\.await\(\)
                \} catch \(e: Exception\) \{
                    try \{
                        FirebaseAuth\.getInstance\(\)\.createUserWithEmailAndPassword\(cleanEmail, password\)\.await\(\)
                    \} catch \(e2: Exception\) \{
                        e2\.printStackTrace\(\)
                    \}
                \}
                
                if \(FirebaseAuth\.getInstance\(\)\.currentUser == null\) \{
                    return false
                \}"""

replacement = """                try {
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(cleanEmail, password).await()
                } catch (e: Exception) {
                    android.util.Log.e("SchoolViewModel", "signInWithEmailAndPassword failed: ${e.message}", e)
                    try {
                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(cleanEmail, password).await()
                    } catch (e2: Exception) {
                        android.util.Log.e("SchoolViewModel", "createUserWithEmailAndPassword failed: ${e2.message}", e2)
                    }
                }
                
                if (FirebaseAuth.getInstance().currentUser == null) {
                    android.util.Log.e("SchoolViewModel", "currentUser is still null")
                    return false
                }"""

content = content.replace(
    '                try {\n                    FirebaseAuth.getInstance().signInWithEmailAndPassword(cleanEmail, password).await()\n                } catch (e: Exception) {\n                    try {\n                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(cleanEmail, password).await()\n                    } catch (e2: Exception) {\n                        e2.printStackTrace()\n                    }\n                }\n                \n                if (FirebaseAuth.getInstance().currentUser == null) {\n                    return false\n                }', replacement)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
