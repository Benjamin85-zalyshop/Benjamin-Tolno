import re

filepath = 'app/src/main/java/com/example/ui/SchoolViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Remove the logo auto-upload from listener
old_logo = """                    } else if (remoteLogo == null && updatedAccount.logoBase64 != null) {
                        // The remote document might not have the logo yet, don't erase the local one, upload it!
                        firestore.collection("schools").document(email).set(
                            mapOf("logoBase64" to updatedAccount.logoBase64), com.google.firebase.firestore.SetOptions.merge()
                        )
                    }"""
new_logo = """                    }"""
content = content.replace(old_logo, new_logo)

# Remove the classFees auto-upload from listener
old_fees = """            } else {
                // Upload local class fees if remote is empty
                val localFees = sharedPrefs.getString("class_fees_$schoolId", null)
                if (localFees != null) {
                    firestore.collection("schools").document(email).set(
                        mapOf("classFeesStr" to localFees), com.google.firebase.firestore.SetOptions.merge()
                    )
                }
            }"""
new_fees = """            }"""
content = content.replace(old_fees, new_fees)

with open(filepath, 'w') as f:
    f.write(content)
