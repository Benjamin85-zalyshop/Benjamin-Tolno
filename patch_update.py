import re

filepath = 'app/src/main/java/com/example/ui/SchoolViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

old_block = """            firestore.collection("schools").document(account.schoolName).update(
                mapOf(
                    "hasActiveSubscription" to true,
                    "isPendingValidation" to false,
                    "subscriptionExpiryDate" to updated.subscriptionExpiryDate
                )
            ).addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error updating subscription", e) }"""

new_block = """            firestore.collection("schools").document(account.schoolName).set(
                mapOf(
                    "hasActiveSubscription" to true,
                    "isPendingValidation" to false,
                    "subscriptionExpiryDate" to updated.subscriptionExpiryDate
                ), com.google.firebase.firestore.SetOptions.merge()
            ).addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error updating subscription", e) }"""

content = content.replace(old_block, new_block)

with open(filepath, 'w') as f:
    f.write(content)
