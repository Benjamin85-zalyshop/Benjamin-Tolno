import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r') as f:
    content = f.read()

# Replace the firestore sync in syncStudentAcademicsToRTDB
old_sync = """            if (student.remoteId.isNotEmpty()) {
                firestore.collection("students").document(student.remoteId)
                    .set(updateData, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener {
                        println("Successfully synced academics to Firestore for student ${student.id}")
                    }
                    .addOnFailureListener { e ->
                        println("Failed to sync academics to Firestore: ${e.message}")
                    }
            }"""

new_sync = """            if (student.remoteId.isNotEmpty()) {
                // Fetch financials
                val payments = repository.getAllPaymentsDirect(schoolId).filter { it.studentId == studentId }
                val totalPaid = payments.sumOf { it.amount }
                val fees = loadClassFees(schoolId)
                val classFeeAmount = fees.find { it.grade == student.grade && it.section == student.section }?.amount ?: 0L
                val totalFee = student.registrationFee + student.reenrollmentFee + classFeeAmount
                
                val finalUpdateData = updateData.toMutableMap()
                finalUpdateData["totalFee"] = totalFee
                finalUpdateData["paidFee"] = totalPaid
                if (student.photoBase64 != null) {
                    finalUpdateData["photoBase64"] = student.photoBase64
                }
                val account = _schoolAccount.value
                if (account != null) {
                    finalUpdateData["schoolName"] = account.displayName.takeIf { it.isNotBlank() } ?: account.schoolName
                    if (account.logoBase64 != null) {
                        finalUpdateData["logoBase64"] = account.logoBase64
                    }
                }

                // Sync to RTDB so parents can read it without Firestore permission issues
                com.google.firebase.database.FirebaseDatabase.getInstance("https://scolapay-b6289-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("students").child(student.remoteId)
                    .updateChildren(finalUpdateData)
                    .addOnSuccessListener {
                        println("Successfully synced academics to RTDB for student ${student.id}")
                    }
                    .addOnFailureListener { e ->
                        println("Failed to sync academics to RTDB: ${e.message}")
                    }
                    
                // Also keep firestore sync for admin panel
                firestore.collection("students").document(student.remoteId)
                    .set(finalUpdateData, com.google.firebase.firestore.SetOptions.merge())
            }"""

content = content.replace(old_sync, new_sync)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w') as f:
    f.write(content)

print("Android patched!")
