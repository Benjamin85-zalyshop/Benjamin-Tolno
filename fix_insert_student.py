import re

filepath = 'app/src/main/java/com/example/ui/SchoolViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

find_text = '''            firestore.collection("schools").document(email).collection("students").document(remoteId).set(
                mapOf(
                    "firstName" to student.firstName,
                    "lastName" to student.lastName,
                    "grade" to student.grade,
                    "section" to student.section,
                    "parentWhatsApp" to student.parentWhatsApp,
                    "registrationFee" to student.registrationFee,
                    "reenrollmentFee" to student.reenrollmentFee,
                    "photoBase64" to student.photoBase64,
                    "schoolYear" to student.schoolYear
                )
            ).addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e) }'''

replace_text = '''            val studentData = mapOf(
                "firstName" to student.firstName,
                "lastName" to student.lastName,
                "grade" to student.grade,
                "section" to student.section,
                "parentWhatsApp" to student.parentWhatsApp,
                "registrationFee" to student.registrationFee,
                "reenrollmentFee" to student.reenrollmentFee,
                "photoBase64" to student.photoBase64,
                "schoolYear" to student.schoolYear
            )
            firestore.collection("schools").document(email).collection("students").document(remoteId).set(studentData)
                .addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e) }
            
            // Sync to root students collection for parents QR code
            firestore.collection("students").document(remoteId).set(studentData, com.google.firebase.firestore.SetOptions.merge())'''

content = content.replace(find_text, replace_text)

with open(filepath, 'w') as f:
    f.write(content)

