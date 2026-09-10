import re

filepath = 'app/src/main/java/com/example/ui/SchoolViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

old_block = """    fun deleteSubject(subject: Subject) {"""

new_block = """    fun updateSubjectDetails(subject: Subject, newName: String, newCoeff: Int, newMaxScore: Float) {
        val email = _schoolAccount.value?.schoolName ?: return
        viewModelScope.launch {
            val updated = subject.copy(name = newName, coefficient = newCoeff, maxScore = newMaxScore)
            repository.updateSubject(updated)
            if (updated.remoteId.isNotEmpty()) {
                firestore.collection("schools").document(email).collection("subjects").document(updated.remoteId).set(
                    mapOf(
                        "name" to updated.name,
                        "coefficient" to updated.coefficient,
                        "maxScore" to updated.maxScore
                    ),
                    com.google.firebase.firestore.SetOptions.merge()
                ).addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e) }
            }
        }
    }

    fun deleteSubject(subject: Subject) {"""

content = content.replace(old_block, new_block)

with open(filepath, 'w') as f:
    f.write(content)
