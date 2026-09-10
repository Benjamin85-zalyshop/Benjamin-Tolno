import re

filepath = 'app/src/main/java/com/example/ui/SchoolViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

old_block = """        firestore.collection("schools").document(name)
            .set(schoolData, com.google.firebase.firestore.SetOptions.merge())
            .addOnFailureListener { e ->
                android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e)
            }
        
        // Enregistrer l'UID du fondateur pour les règles de sécurité
        val uid = auth.currentUser?.uid
        if (uid != null) {
            firestore.collection("schools").document(name).collection("users").document(uid)
                .set(mapOf("role" to "FONDATEUR"), com.google.firebase.firestore.SetOptions.merge())
        }"""

new_block = """        // Enregistrer l'UID du fondateur pour les règles de sécurité D'ABORD
        val uid = auth.currentUser?.uid
        if (uid != null) {
            try {
                firestore.collection("schools").document(name).collection("users").document(uid)
                    .set(mapOf("role" to "FONDATEUR"), com.google.firebase.firestore.SetOptions.merge()).await()
            } catch (e: Exception) {
                android.util.Log.e("ScolaPay-Firebase", "Error writing user role", e)
            }
        }
        
        try {
            firestore.collection("schools").document(name)
                .set(schoolData, com.google.firebase.firestore.SetOptions.merge()).await()
        } catch (e: Exception) {
            android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e)
        }"""

content = content.replace(old_block, new_block)

with open(filepath, 'w') as f:
    f.write(content)
