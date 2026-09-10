import re

filepath = 'app/src/main/java/com/example/ui/SchoolViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

old_block = """    suspend fun login(email: String, pass: String): Boolean {
        val auth = FirebaseAuth.getInstance()
        var isFounder = false
        var isFinancier = false
        var financierName = email
        
        // 1. Tenter la connexion Fondateur sur Firebase
        try {
            auth.signInWithEmailAndPassword(email, pass).await()
            isFounder = true
        } catch (e: Exception) {
            // 2. Tenter la connexion Financier sur Firebase
            try {
                auth.signInWithEmailAndPassword("fin_$email", pass).await()
                isFinancier = true
                financierName = "fin_$email"
            } catch (e2: Exception) {
                // 3. MIGRATION : Si Firebase échoue, vérifier la base locale
                val localAccount = repository.getSchoolAccountByName(email)
                if (localAccount != null) {
                    if (pass == localAccount.passwordHash || pass == "admin") {
                        try {
                            auth.createUserWithEmailAndPassword(email, pass).await()
                            isFounder = true
                        } catch (e3: Exception) {
                             try { auth.signInWithEmailAndPassword(email, pass).await(); isFounder = true } catch (e4: Exception) {}
                        }
                    } else if (pass == localAccount.financierPasswordHash) {
                        try {
                            auth.createUserWithEmailAndPassword("fin_$email", pass).await()
                            isFinancier = true
                            financierName = "fin_$email"
                        } catch (e3: Exception) {
                             try { auth.signInWithEmailAndPassword("fin_$email", pass).await(); isFinancier = true; financierName = "fin_$email" } catch (e4: Exception) {}
                        }
                    }
                }
            }
        }"""

new_block = """    val loginError = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)

    suspend fun login(email: String, pass: String): Boolean {
        val auth = FirebaseAuth.getInstance()
        var isFounder = false
        var isFinancier = false
        var financierName = email
        loginError.value = null
        
        // 1. Tenter la connexion Fondateur sur Firebase
        try {
            auth.signInWithEmailAndPassword(email, pass).await()
            isFounder = true
        } catch (e: Exception) {
            // 2. Tenter la connexion Financier sur Firebase
            try {
                auth.signInWithEmailAndPassword("fin_$email", pass).await()
                isFinancier = true
                financierName = "fin_$email"
            } catch (e2: Exception) {
                // 3. MIGRATION : Si Firebase échoue, vérifier la base locale
                val localAccount = repository.getSchoolAccountByName(email)
                if (localAccount != null) {
                    if (pass == localAccount.passwordHash || pass == "admin") {
                        try {
                            auth.createUserWithEmailAndPassword(email, pass).await()
                            isFounder = true
                        } catch (e3: Exception) {
                             try { 
                                 auth.signInWithEmailAndPassword(email, pass).await()
                                 isFounder = true 
                             } catch (e4: Exception) {
                                 loginError.value = "Le mot de passe ou l'email est invalide pour Firebase."
                             }
                        }
                    } else if (pass == localAccount.financierPasswordHash) {
                        try {
                            auth.createUserWithEmailAndPassword("fin_$email", pass).await()
                            isFinancier = true
                            financierName = "fin_$email"
                        } catch (e3: Exception) {
                             try { auth.signInWithEmailAndPassword("fin_$email", pass).await(); isFinancier = true; financierName = "fin_$email" } catch (e4: Exception) {}
                        }
                    } else {
                        loginError.value = "E-mail ou mot de passe incorrect."
                    }
                } else {
                    loginError.value = "E-mail ou mot de passe incorrect, ou compte introuvable."
                }
            }
        }"""

content = content.replace(old_block, new_block)

with open(filepath, 'w') as f:
    f.write(content)
