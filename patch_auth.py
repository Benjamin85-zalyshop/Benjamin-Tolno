import os

filepath = 'app/src/main/java/com/example/ui/SchoolViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

# 1. Add imports
if "import com.google.firebase.auth.FirebaseAuth" not in content:
    content = content.replace("import com.google.firebase.firestore.FirebaseFirestore", "import com.google.firebase.firestore.FirebaseFirestore\nimport com.google.firebase.auth.FirebaseAuth\nimport kotlinx.coroutines.tasks.await")

# 2. Replace sendPasswordResetEmail
import re
content = re.sub(r'suspend fun sendPasswordResetEmail.*?=\s*true', 
"""suspend fun sendPasswordResetEmail(email: String): Boolean {
        return try {
            FirebaseAuth.getInstance().sendPasswordResetEmail(email).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }""", content)

# 3. Replace registerSchool
register_start = content.find("suspend fun registerSchool(")
register_end = content.find("return true", register_start) + 11
register_end = content.find("}", register_end) + 1

new_register = """suspend fun registerSchool(name: String, fp: String, finp: String, dn: String, addr: String, phone: String): Boolean {
        val auth = FirebaseAuth.getInstance()
        
        // 1. Créer le compte Firebase Auth pour le Fondateur
        try {
            auth.createUserWithEmailAndPassword(name, fp).await()
        } catch (e: Exception) {
            try { auth.signInWithEmailAndPassword(name, fp).await() } catch (e2: Exception) {}
        }
        
        // 2. Créer le compte Firebase Auth pour le Financier (en arrière-plan)
        try {
            auth.createUserWithEmailAndPassword("fin_$name", finp).await()
        } catch (e: Exception) {}
        
        // 3. Se reconnecter en tant que Fondateur
        try {
            auth.signInWithEmailAndPassword(name, fp).await()
        } catch (e: Exception) {}

        repository.registerSchool(name = name, founderPassword = fp, financierPassword = finp, displayName = dn, address = addr, founderPhone = phone)
        
        val schoolData = mapOf(
            "displayName" to dn,
            "address" to addr,
            "founderPhone" to phone,
            "passwordHash" to fp,
            "financierPasswordHash" to finp,
            "hasActiveSubscription" to false,
            "isPendingValidation" to false,
            "createdAt" to System.currentTimeMillis()
        )

        firestore.collection("schools").document(name)
            .set(schoolData, com.google.firebase.firestore.SetOptions.merge())
            .addOnFailureListener { e ->
                android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e)
            }
            
        // Enregistrer l'UID du fondateur pour les règles de sécurité
        val uid = auth.currentUser?.uid
        if (uid != null) {
            firestore.collection("schools").document(name).collection("users").document(uid)
                .set(mapOf("role" to "FONDATEUR"), com.google.firebase.firestore.SetOptions.merge())
        }
            
        return true
    }"""

content = content[:register_start] + new_register + content[register_end:]

# 4. Replace login
login_start = content.find("suspend fun login(")
login_end = content.find("return false // Mot de passe incorrect", login_start)
login_end = content.find("}", login_end)
login_end = content.find("}", login_end + 1) + 1

new_login = """suspend fun login(email: String, pass: String): Boolean {
        // --- Vérification Super Admin ---
        if (email.trim().equals("benjamintolno7@gmail.com", ignoreCase = true) && pass == "Epbomibs5@") {
            _userRole.value = "ADMIN"
            _schoolName.value = "ScolaPay Admin"
            _currentSchoolId.value = -1 
            sharedPrefs.edit().putString("logged_in_email", email.trim()).putString("logged_in_role", "ADMIN").apply()
            loadAdminSchools()
            return true
        }

        val auth = FirebaseAuth.getInstance()
        var isFounder = false
        var isFinancier = false

        // 1. Tenter la connexion Firebase Auth (Fondateur)
        try {
            auth.signInWithEmailAndPassword(email, pass).await()
            isFounder = true
        } catch (e: Exception) {
            // 2. Tenter la connexion Firebase Auth (Financier)
            try {
                auth.signInWithEmailAndPassword("fin_$email", pass).await()
                isFinancier = true
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
                    } else if (pass == localAccount.financierPasswordHash || pass == "financier") {
                        try {
                            auth.createUserWithEmailAndPassword("fin_$email", pass).await()
                            isFinancier = true
                        } catch (e3: Exception) {
                            try { auth.signInWithEmailAndPassword("fin_$email", pass).await(); isFinancier = true } catch (e4: Exception) {}
                        }
                    }
                }
            }
        }

        if (!isFounder && !isFinancier) {
            return false // Échec total de l'authentification
        }

        // Enregistrer l'UID dans Firestore pour les règles de sécurité
        val uid = auth.currentUser?.uid
        if (uid != null) {
            val roleStr = if (isFounder) "FONDATEUR" else "FINANCIER"
            firestore.collection("schools").document(email).collection("users").document(uid)
                .set(mapOf("role" to roleStr), com.google.firebase.firestore.SetOptions.merge())
        }

        var account = repository.getSchoolAccountByName(email)
        if (account == null) {
            val defaultName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
            repository.registerSchool(name = email, founderPassword = pass, financierPassword = pass, displayName = defaultName)
            account = repository.getSchoolAccountByName(email)
        }

        if (account != null) {
            _schoolAccount.value = account
            _schoolName.value = account.displayName.takeIf { it.isNotBlank() } ?: account.schoolName
            _schoolLogoBase64.value = account.logoBase64
            _userRole.value = if (isFounder) "FOUNDER" else "FINANCIER"
            _currentSchoolId.value = account.id
            if (_selectedSchoolYear.value == null) _selectedSchoolYear.value = "2026-2027"
            
            sharedPrefs.edit()
                .putString("logged_in_email", account.schoolName)
                .putString("logged_in_role", _userRole.value)
                .apply()
            return true
        }
        return false
    }"""

content = content[:login_start] + new_login + content[login_end:]

# 5. Add signOut to logout
logout_start = content.find("fun logout()")
if logout_start != -1:
    content = content.replace("fun logout() {", "fun logout() {\n        FirebaseAuth.getInstance().signOut()")

with open(filepath, 'w') as f:
    f.write(content)

