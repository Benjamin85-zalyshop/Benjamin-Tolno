import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r') as f:
    content = f.read()

# Add import if missing
if 'import kotlinx.coroutines.tasks.await' not in content:
    content = content.replace('import kotlinx.coroutines.launch', 'import kotlinx.coroutines.launch\nimport kotlinx.coroutines.tasks.await')


payment_target = 'val studentId = repository.getStudentIdByRemoteId(studentRemoteId) ?: continue'
payment_replacement = """var studentId = repository.getStudentIdByRemoteId(studentRemoteId)
                    if (studentId == null) {
                        try {
                            val studentDoc = kotlinx.coroutines.tasks.await(firestore.collection("schools").document(email).collection("students").document(studentRemoteId).get())
                            if (studentDoc != null && studentDoc.exists()) {
                                val student = Student(
                                    id = 0, schoolId = schoolId,
                                    firstName = studentDoc.getString("firstName") ?: "", lastName = studentDoc.getString("lastName") ?: "",
                                    grade = studentDoc.getString("grade") ?: "", section = studentDoc.getString("section") ?: "Non défini",
                                    remoteId = studentRemoteId, parentWhatsApp = studentDoc.getString("parentWhatsApp"),
                                    registrationFee = studentDoc.getLong("registrationFee") ?: 0L, reenrollmentFee = studentDoc.getLong("reenrollmentFee") ?: 0L,
                                    photoBase64 = studentDoc.getString("photoBase64"), schoolYear = studentDoc.getString("schoolYear") ?: "2026-2027"
                                )
                                repository.insertStudent(student)
                                studentId = repository.getStudentIdByRemoteId(studentRemoteId)
                            }
                        } catch (e: Exception) {}
                    }
                    if (studentId == null) continue"""

# Since gradesListener also has `val studentId = repository.getStudentIdByRemoteId(studentRemoteId) ?: continue`,
# we need to be careful with replace.

# We'll use regex to replace within paymentsListener specifically
# First, let's find the paymentsListener block.
payment_pattern = r'val studentRemoteId = doc\.getString\("studentRemoteId"\) \?: continue\s*val studentId = repository\.getStudentIdByRemoteId\(studentRemoteId\) \?: continue\s*val payment = Payment\('
payment_repl = r'''val studentRemoteId = doc.getString("studentRemoteId") ?: continue
                    
                    var studentId = repository.getStudentIdByRemoteId(studentRemoteId)
                    if (studentId == null) {
                        try {
                            val studentDoc = kotlinx.coroutines.tasks.await(firestore.collection("schools").document(email).collection("students").document(studentRemoteId).get())
                            if (studentDoc != null && studentDoc.exists()) {
                                val student = Student(
                                    id = 0, schoolId = schoolId,
                                    firstName = studentDoc.getString("firstName") ?: "", lastName = studentDoc.getString("lastName") ?: "",
                                    grade = studentDoc.getString("grade") ?: "", section = studentDoc.getString("section") ?: "Non défini",
                                    remoteId = studentRemoteId, parentWhatsApp = studentDoc.getString("parentWhatsApp"),
                                    registrationFee = studentDoc.getLong("registrationFee") ?: 0L, reenrollmentFee = doc.getLong("reenrollmentFee") ?: 0L,
                                    photoBase64 = studentDoc.getString("photoBase64"), schoolYear = studentDoc.getString("schoolYear") ?: "2026-2027"
                                )
                                repository.insertStudent(student)
                                studentId = repository.getStudentIdByRemoteId(studentRemoteId)
                            }
                        } catch (e: Exception) {}
                    }
                    if (studentId == null) continue
                    
                    val payment = Payment('''

content = re.sub(payment_pattern, payment_repl, content)

grades_pattern = r'val studentId = repository\.getStudentIdByRemoteId\(studentRemoteId\) \?: continue\s*val subjectId = repository\.getSubjectIdByRemoteId\(subjectRemoteId\) \?: continue'
grades_repl = r'''var studentId = repository.getStudentIdByRemoteId(studentRemoteId)
                    if (studentId == null) {
                        try {
                            val studentDoc = kotlinx.coroutines.tasks.await(firestore.collection("schools").document(email).collection("students").document(studentRemoteId).get())
                            if (studentDoc != null && studentDoc.exists()) {
                                val student = Student(
                                    id = 0, schoolId = schoolId,
                                    firstName = studentDoc.getString("firstName") ?: "", lastName = studentDoc.getString("lastName") ?: "",
                                    grade = studentDoc.getString("grade") ?: "", section = studentDoc.getString("section") ?: "Non défini",
                                    remoteId = studentRemoteId, parentWhatsApp = studentDoc.getString("parentWhatsApp"),
                                    registrationFee = studentDoc.getLong("registrationFee") ?: 0L, reenrollmentFee = doc.getLong("reenrollmentFee") ?: 0L,
                                    photoBase64 = studentDoc.getString("photoBase64"), schoolYear = studentDoc.getString("schoolYear") ?: "2026-2027"
                                )
                                repository.insertStudent(student)
                                studentId = repository.getStudentIdByRemoteId(studentRemoteId)
                            }
                        } catch (e: Exception) {}
                    }
                    if (studentId == null) continue

                    var subjectId = repository.getSubjectIdByRemoteId(subjectRemoteId)
                    if (subjectId == null) {
                        try {
                            val subjectDoc = kotlinx.coroutines.tasks.await(firestore.collection("schools").document(email).collection("subjects").document(subjectRemoteId).get())
                            if (subjectDoc != null && subjectDoc.exists()) {
                                val subject = Subject(
                                    id = 0, schoolId = schoolId,
                                    section = subjectDoc.getString("section") ?: "", grade = subjectDoc.getString("grade") ?: "",
                                    name = subjectDoc.getString("name") ?: "", coefficient = subjectDoc.getLong("coefficient")?.toInt() ?: 1,
                                    maxScore = subjectDoc.getDouble("maxScore")?.toFloat() ?: 20f, remoteId = subjectRemoteId
                                )
                                repository.insertSubject(subject)
                                subjectId = repository.getSubjectIdByRemoteId(subjectRemoteId)
                            }
                        } catch (e: Exception) {}
                    }
                    if (subjectId == null) continue'''

content = re.sub(grades_pattern, grades_repl, content)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w') as f:
    f.write(content)
