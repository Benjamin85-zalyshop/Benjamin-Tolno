import re

filepath = 'app/src/main/java/com/example/ui/SchoolViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Fix init block
init_find = '''            if (loggedInEmail != null && loggedInRole != null) {
                if (loggedInRole == "ADMIN" && loggedInEmail.equals("benjamintolno7@gmail.com", ignoreCase = true)) {
                    _userRole.value = "ADMIN"
                    _schoolName.value = "ScolaPay Admin"
                    _currentSchoolId.value = -1
                    val auth = FirebaseAuth.getInstance()
                    if (auth.currentUser?.email != "benjamintolno7@gmail.com") {
                        try {
                            auth.signInWithEmailAndPassword("benjamintolno7@gmail.com", "Epbomibs5@").await()
                        } catch (e: Exception) {
                            try {
                                auth.createUserWithEmailAndPassword("benjamintolno7@gmail.com", "Epbomibs5@").await()
                            } catch (e2: Exception) {}
                        }
                    }
                    loadAdminSchools()
                } else {'''

init_replace = '''            if (loggedInEmail != null && loggedInRole != null) {
                if (loggedInRole == "ADMIN" && loggedInEmail.equals("benjamintolno7@gmail.com", ignoreCase = true)) {
                    val auth = FirebaseAuth.getInstance()
                    if (auth.currentUser?.email == "benjamintolno7@gmail.com") {
                        _userRole.value = "ADMIN"
                        _schoolName.value = "ScolaPay Admin"
                        _currentSchoolId.value = -1
                        loadAdminSchools()
                    } else {
                        // Not authenticated in Firebase, force logout to show login screen
                        logout()
                    }
                } else {'''
content = content.replace(init_find, init_replace)

# Fix forceSyncSchools
sync_find = '''        viewModelScope.launch {
            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser?.email != "benjamintolno7@gmail.com") {
                try {
                    auth.signInWithEmailAndPassword("benjamintolno7@gmail.com", "Epbomibs5@").await()
                } catch (e: Exception) {
                    try {
                        auth.createUserWithEmailAndPassword("benjamintolno7@gmail.com", "Epbomibs5@").await()
                    } catch (e2: Exception) {
                        _adminError.value = "Erreur de connexion Firebase: ${e2.message}"
                        return@launch
                    }
                }
            }
            val task = firestore.collection("schools").get()'''

sync_replace = '''        viewModelScope.launch {
            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser?.email != "benjamintolno7@gmail.com") {
                _adminError.value = "Vous n'êtes pas connecté à Firebase."
                logout()
                return@launch
            }
            val task = firestore.collection("schools").get()'''
content = content.replace(sync_find, sync_replace)

# Fix login function
login_find = '''        // --- Vérification Super Admin ---
        if (email.trim().equals("benjamintolno7@gmail.com", ignoreCase = true) && pass == "Epbomibs5@") {
            val auth = FirebaseAuth.getInstance()
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
            } catch (e: Exception) {
                try {
                    auth.createUserWithEmailAndPassword(email, pass).await()
                } catch (e2: Exception) {
                    android.util.Log.e("AdminLogin", "Firebase Auth failed", e2)
                }
            }
            
            _userRole.value = "ADMIN"
            _schoolName.value = "ScolaPay Admin"
            _currentSchoolId.value = -1 
            sharedPrefs.edit().putString("logged_in_email", email.trim()).putString("logged_in_role", "ADMIN").apply()
            loadAdminSchools()
            return true
        }'''

login_replace = '''        // --- Vérification Super Admin ---
        if (email.trim().equals("benjamintolno7@gmail.com", ignoreCase = true)) {
            val auth = FirebaseAuth.getInstance()
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
            } catch (e: Exception) {
                try {
                    auth.createUserWithEmailAndPassword(email, pass).await()
                } catch (e2: Exception) {
                    android.util.Log.e("AdminLogin", "Firebase Auth failed", e2)
                    _adminError.value = "Erreur: Firebase a bloqué l'accès ou mot de passe incorrect."
                    return false
                }
            }
            
            _userRole.value = "ADMIN"
            _schoolName.value = "ScolaPay Admin"
            _currentSchoolId.value = -1 
            sharedPrefs.edit().putString("logged_in_email", email.trim()).putString("logged_in_role", "ADMIN").apply()
            loadAdminSchools()
            return true
        }'''
content = content.replace(login_find, login_replace)

with open(filepath, 'w') as f:
    f.write(content)

