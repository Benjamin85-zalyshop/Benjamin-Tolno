import re

filepath = 'app/src/main/java/com/example/ui/SchoolViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

start_marker = "        firestore.collection(\"schools\").document(name)\n            .set(schoolData"
end_marker = "return true\n    }"

start_idx = content.find(start_marker)
end_idx = content.find(end_marker)

if start_idx != -1 and end_idx != -1:
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
        }
        
        """
    content = content[:start_idx] + new_block + content[end_idx:]

with open(filepath, 'w') as f:
    f.write(content)

