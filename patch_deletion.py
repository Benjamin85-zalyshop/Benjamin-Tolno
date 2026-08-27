import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r') as f:
    content = f.read()

# 1. Patch the stubs
old_stubs = """    fun createDeletionRequest(student: Student, reason: String) {
        // TODO: Auto-generated stub
    }

    fun approveDeletionRequest(request: DeletionRequest) {
        // TODO: Auto-generated stub
    }

    fun rejectDeletionRequest(request: DeletionRequest, reason: String) {
        // TODO: Auto-generated stub
    }

    fun dismissDeletionRequest(request: DeletionRequest) {
        // TODO: Auto-generated stub
    }"""

new_stubs = """    fun createDeletionRequest(student: Student, reason: String) {
        val email = _schoolAccount.value?.schoolName ?: return
        val remoteId = java.util.UUID.randomUUID().toString()
        val request = DeletionRequest(
            id = remoteId,
            studentRemoteId = student.remoteId,
            studentName = "${student.firstName} ${student.lastName}",
            grade = student.grade,
            section = student.section,
            reason = reason,
            requestedBy = "Financier",
            requestedAt = System.currentTimeMillis(),
            status = "PENDING"
        )
        firestore.collection("schools").document(email).collection("deletionRequests").document(remoteId).set(request)
            .addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error creating deletion request", e) }
    }

    fun approveDeletionRequest(request: DeletionRequest) {
        val email = _schoolAccount.value?.schoolName ?: return
        viewModelScope.launch {
            // Delete the student locally and remotely
            val student = repository.getStudentByRemoteId(request.studentRemoteId)
            if (student != null) {
                repository.deleteStudentById(student.id)
            }
            firestore.collection("schools").document(email).collection("students").document(request.studentRemoteId).delete()
            
            // Mark request as APPROVED
            firestore.collection("schools").document(email).collection("deletionRequests").document(request.id).update("status", "APPROVED")
                .addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error approving deletion request", e) }
        }
    }

    fun rejectDeletionRequest(request: DeletionRequest, reason: String) {
        val email = _schoolAccount.value?.schoolName ?: return
        firestore.collection("schools").document(email).collection("deletionRequests").document(request.id)
            .update(
                mapOf(
                    "status" to "REJECTED",
                    "rejectionReason" to reason
                )
            )
            .addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error rejecting deletion request", e) }
    }

    fun dismissDeletionRequest(request: DeletionRequest) {
        val email = _schoolAccount.value?.schoolName ?: return
        firestore.collection("schools").document(email).collection("deletionRequests").document(request.id).delete()
            .addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error dismissing deletion request", e) }
    }"""

content = content.replace(old_stubs, new_stubs)

# 2. Patch syncSchoolDataFromFirestore to include listener
old_sync_end = """        activeListeners.add(expensesListener)
    }"""

new_sync_end = """        val deletionRequestsListener = firestore.collection("schools").document(email).collection("deletionRequests").addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) return@addSnapshotListener
            val requests = mutableListOf<DeletionRequest>()
            for (doc in snapshot.documents) {
                requests.add(
                    DeletionRequest(
                        id = doc.id,
                        studentRemoteId = doc.getString("studentRemoteId") ?: "",
                        studentName = doc.getString("studentName") ?: "",
                        grade = doc.getString("grade") ?: "",
                        section = doc.getString("section") ?: "",
                        reason = doc.getString("reason") ?: "",
                        requestedBy = doc.getString("requestedBy") ?: "",
                        requestedAt = doc.getLong("requestedAt") ?: 0L,
                        status = doc.getString("status") ?: "PENDING",
                        rejectionReason = doc.getString("rejectionReason") ?: ""
                    )
                )
            }
            _deletionRequests.value = requests.sortedByDescending { it.requestedAt }
        }
        activeListeners.add(expensesListener)
        activeListeners.add(deletionRequestsListener)
    }"""

content = content.replace(old_sync_end, new_sync_end)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w') as f:
    f.write(content)

print("Patch applied")
