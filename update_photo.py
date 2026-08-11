import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

target = """    fun updateStudentPhoto(student: Student, photoBase64: String?) {
        val email = sharedPrefs.getString("last_email", null)
        viewModelScope.launch {
            val updated = student.copy(photoBase64 = photoBase64)
            repository.insertStudent(updated)
            if (!email.isNullOrBlank() && student.remoteId.isNotBlank()) {
                try {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("schools").document(email)
                        .collection("students").document(student.remoteId)
                        .update("photoBase64", photoBase64 ?: "")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }"""

replacement = """    fun updateStudentPhoto(student: Student, photoBase64: String?) {
        val email = sharedPrefs.getString("last_email", null)
        viewModelScope.launch {
            val updated = student.copy(photoBase64 = photoBase64)
            repository.insertStudent(updated)
            
            // Sync to RTDB
            try {
                val matricule = if (!student.remoteId.isNullOrEmpty() && student.remoteId.length >= 5) student.remoteId.take(5).uppercase() else student.id.toString()
                val database = FirebaseDatabase.getInstance("https://scolapay-b6289-default-rtdb.europe-west1.firebasedatabase.app")
                val studentRef = database.getReference("students").child(matricule)
                val updates = mutableMapOf<String, Any>()
                updates["photoBase64"] = photoBase64 ?: ""
                val logo = _schoolLogoBase64.value
                if (!logo.isNullOrBlank()) {
                    updates["schoolLogo"] = logo
                }
                studentRef.updateChildren(updates)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            if (!email.isNullOrBlank() && student.remoteId.isNotBlank()) {
                try {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("schools").document(email)
                        .collection("students").document(student.remoteId)
                        .update("photoBase64", photoBase64 ?: "")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
print("Updated updateStudentPhoto")
