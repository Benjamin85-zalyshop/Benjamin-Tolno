import re

filepath = 'app/src/main/java/com/example/ui/SchoolViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

old_block = """    suspend fun registerSchool(name: String, fp: String, finp: String, dn: String, addr: String, phone: String): Boolean {
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
        } catch (e: Exception) {}"""

new_block = """    suspend fun registerSchool(name: String, fp: String, finp: String, dn: String, addr: String, phone: String): Boolean {
        val auth = FirebaseAuth.getInstance()
        
        // 1. Créer le compte Firebase Auth pour le Fondateur
        try {
            auth.createUserWithEmailAndPassword(name, fp).await()
        } catch (e: Exception) {
            try { 
                auth.signInWithEmailAndPassword(name, fp).await() 
            } catch (e2: Exception) {
                android.util.Log.e("ScolaPay", "Auth creation failed", e2)
                return false // Échec critique si Firebase refuse la création
            }
        }
        
        // 2. Créer le compte Firebase Auth pour le Financier (en arrière-plan)
        try {
            auth.createUserWithEmailAndPassword("fin_$name", finp).await()
        } catch (e: Exception) {}
        
        // 3. Se reconnecter en tant que Fondateur
        try {
            auth.signInWithEmailAndPassword(name, fp).await()
        } catch (e: Exception) {}"""

content = content.replace(old_block, new_block)

with open(filepath, 'w') as f:
    f.write(content)
