import re

filepath = 'app/src/main/java/com/example/ui/SchoolViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Replace setSchoolLogo
logo_old = """            firestore.collection("schools").document(account.schoolName).update(
                "logoBase64", base64
            ).addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error updating logo", e) }"""

logo_new = """            firestore.collection("schools").document(account.schoolName).set(
                mapOf("logoBase64" to base64), com.google.firebase.firestore.SetOptions.merge()
            ).addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error updating logo", e) }"""

content = content.replace(logo_old, logo_new)

with open(filepath, 'w') as f:
    f.write(content)
