import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r') as f:
    content = f.read()

# Replace the stub implementation
stub = """    fun updateStudentFinancialsInRTDB(schoolId: Int, studentId: Int) {
        // TODO: Auto-generated stub
    }"""

impl = """    fun updateStudentFinancialsInRTDB(schoolId: Int, studentId: Int) {
        viewModelScope.launch {
            val student = repository.getStudentById(studentId) ?: return@launch
            if (student.remoteId.isEmpty()) return@launch
            
            val payments = repository.getAllPaymentsDirect(schoolId).filter { it.studentId == studentId }
            val totalPaid = payments.sumOf { it.amount }
            val fees = loadClassFees(schoolId)
            val classFeeAmount = fees.find { it.grade == student.grade }?.feeAmount ?: 0L
            val totalFee = student.registrationFee + student.reenrollmentFee + classFeeAmount
            
            val updateData = mutableMapOf<String, Any>(
                "totalFee" to totalFee,
                "paidFee" to totalPaid
            )
            
            com.google.firebase.database.FirebaseDatabase.getInstance("https://scolapay-b6289-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("students").child(student.remoteId)
                .updateChildren(updateData)
                .addOnSuccessListener {
                    println("Successfully synced financials to RTDB for student ${student.id}")
                }
                .addOnFailureListener { e ->
                    println("Failed to sync financials to RTDB: ${e.message}")
                }
                
            firestore.collection("students").document(student.remoteId)
                .set(updateData, com.google.firebase.firestore.SetOptions.merge())
        }
    }"""

content = content.replace(stub, impl)

# Also patch insertPayment
insert_old = """            firestore.collection("schools").document(email).collection("payments").document(remoteId).set(
                mapOf(
                    "studentRemoteId" to student.remoteId,
                    "amount" to payment.amount,
                    "reason" to payment.reason,
                    "date" to payment.date,
                    "paymentMethod" to payment.paymentMethod
                )
            ).addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e) }
        }
    }"""

insert_new = """            firestore.collection("schools").document(email).collection("payments").document(remoteId).set(
                mapOf(
                    "studentRemoteId" to student.remoteId,
                    "amount" to payment.amount,
                    "reason" to payment.reason,
                    "date" to payment.date,
                    "paymentMethod" to payment.paymentMethod
                )
            ).addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e) }
            
            updateStudentFinancialsInRTDB(schoolId, studentId)
        }
    }"""

content = content.replace(insert_old, insert_new)

# Patch deletePayment
delete_old = """            if (payment.remoteId.isNotEmpty()) {
                firestore.collection("schools").document(email).collection("payments").document(payment.remoteId).delete()
                    .addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e) }
            }
        }
    }"""

delete_new = """            if (payment.remoteId.isNotEmpty()) {
                firestore.collection("schools").document(email).collection("payments").document(payment.remoteId).delete()
                    .addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e) }
            }
            
            updateStudentFinancialsInRTDB(payment.schoolId, payment.studentId)
        }
    }"""

content = content.replace(delete_old, delete_new)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w') as f:
    f.write(content)

print("Financials patched!")
