import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Add _loginError state
pattern_error_state = r"    private val _userRole = MutableStateFlow<String\?>\(null\)\n    val userRole: StateFlow<String\?> = _userRole"
replacement_error_state = """    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError"""

content = re.sub(pattern_error_state, replacement_error_state, content)

# Modify login function
pattern_login = r"""                try \{
                    FirebaseAuth\.getInstance\(\)\.signInWithEmailAndPassword\(cleanEmail, password\)\.await\(\)
                \} catch \(e: Exception\) \{
                    android\.util\.Log\.e\("SchoolViewModel", "signInWithEmailAndPassword failed: \$\{e\.message\}", e\)
                    try \{
                        FirebaseAuth\.getInstance\(\)\.createUserWithEmailAndPassword\(cleanEmail, password\)\.await\(\)
                    \} catch \(e2: Exception\) \{
                        android\.util\.Log\.e\("SchoolViewModel", "createUserWithEmailAndPassword failed: \$\{e2\.message\}", e2\)
                    \}
                \}
                
                if \(FirebaseAuth\.getInstance\(\)\.currentUser == null\) \{
                    android\.util\.Log\.e\("SchoolViewModel", "currentUser is still null"\)
                    return false
                \}"""

replacement_login = """                try {
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(cleanEmail, password).await()
                } catch (e: Exception) {
                    _loginError.value = "Sign in failed: ${e.message}"
                    try {
                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(cleanEmail, password).await()
                        _loginError.value = null // Success
                    } catch (e2: Exception) {
                        _loginError.value = "Create failed: ${e2.message} (Sign in: ${e.message})"
                    }
                }
                
                if (FirebaseAuth.getInstance().currentUser == null) {
                    return false
                }"""

content = content.replace(
    '                try {\n                    FirebaseAuth.getInstance().signInWithEmailAndPassword(cleanEmail, password).await()\n                } catch (e: Exception) {\n                    android.util.Log.e("SchoolViewModel", "signInWithEmailAndPassword failed: ${e.message}", e)\n                    try {\n                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(cleanEmail, password).await()\n                    } catch (e2: Exception) {\n                        android.util.Log.e("SchoolViewModel", "createUserWithEmailAndPassword failed: ${e2.message}", e2)\n                    }\n                }\n                \n                if (FirebaseAuth.getInstance().currentUser == null) {\n                    android.util.Log.e("SchoolViewModel", "currentUser is still null")\n                    return false\n                }', replacement_login)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
