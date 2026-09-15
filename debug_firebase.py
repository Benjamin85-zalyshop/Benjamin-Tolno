import re

filepath = 'app/src/main/java/com/example/ui/SchoolViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Let's add some debugging to the studentsListener to see if Android is receiving the snapshot event but rejecting the data
find_text = '''        val studentsListener = firestore.collection("schools").document(email).collection("students").addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) return@addSnapshotListener
            viewModelScope.launch {
                for (doc in snapshot.documents) {
                    val remoteId = doc.id
                    val existing = repository.getStudentByRemoteId(remoteId)
                    val student = Student(
                        id = existing?.id ?: 0,
                        schoolId = schoolId,
                        firstName = doc.getString("firstName") ?: "",
                        lastName = doc.getString("lastName") ?: "",
                        grade = doc.getString("grade") ?: "",
                        section = doc.getString("section") ?: "Non défini",
                        remoteId = remoteId,
                        parentWhatsApp = doc.getString("parentWhatsApp"),
                        registrationFee = doc.getLong("registrationFee") ?: 0L,
                        reenrollmentFee = doc.getLong("reenrollmentFee") ?: 0L,
                        photoBase64 = doc.getString("photoBase64"),
                        schoolYear = doc.getString("schoolYear") ?: "2026-2027"
                    )
                    if (existing != null) repository.updateStudent(student) else repository.insertStudent(student)
                }
            }
        }'''

replace_text = '''        val studentsListener = firestore.collection("schools").document(email).collection("students").addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) return@addSnapshotListener
            viewModelScope.launch {
                for (change in snapshot.documentChanges) {
                    val doc = change.document
                    val remoteId = doc.id
                    
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.REMOVED) {
                        val existing = repository.getStudentByRemoteId(remoteId)
                        if (existing != null) repository.deleteStudentById(existing.id)
                        continue
                    }
                    
                    val existing = repository.getStudentByRemoteId(remoteId)
                    val student = Student(
                        id = existing?.id ?: 0,
                        schoolId = schoolId,
                        firstName = doc.getString("firstName") ?: "",
                        lastName = doc.getString("lastName") ?: "",
                        grade = doc.getString("grade") ?: "",
                        section = doc.getString("section") ?: "Non défini",
                        remoteId = remoteId,
                        parentWhatsApp = doc.getString("parentWhatsApp"),
                        registrationFee = doc.getLong("registrationFee") ?: 0L,
                        reenrollmentFee = doc.getLong("reenrollmentFee") ?: 0L,
                        photoBase64 = doc.getString("photoBase64"),
                        schoolYear = doc.getString("schoolYear") ?: "2026-2027"
                    )
                    if (existing != null) repository.updateStudent(student) else repository.insertStudent(student)
                }
            }
        }'''

content = content.replace(find_text, replace_text)

with open(filepath, 'w') as f:
    f.write(content)

