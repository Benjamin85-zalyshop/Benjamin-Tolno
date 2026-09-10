import re

filepath = 'app/src/main/java/com/example/ui/SchoolViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

old_block = """            if (email != null) {
                firestore.collection("schools").document(email).set(
                    mapOf("classFeesStr" to jsonStr),
                    com.google.firebase.firestore.SetOptions.merge()
                )
            }"""

new_block = """            if (email != null) {
                firestore.collection("schools").document(email).set(
                    mapOf("classFeesStr" to jsonStr),
                    com.google.firebase.firestore.SetOptions.merge()
                ).addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error syncing class fees", e) }
            }"""

content = content.replace(old_block, new_block)

with open(filepath, 'w') as f:
    f.write(content)
