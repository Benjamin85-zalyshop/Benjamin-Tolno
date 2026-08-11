import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

target = """                val termData = mapOf(
                    "avg" to String.format(java.util.Locale.US, "%.2f", summary.average),
                    "rank" to summary.rank,
                    "size" to summary.classSize,
                    "mention" to summary.mention,
                    "subjects" to subjectsMap
                )"""

replacement = """                val termData = mutableMapOf<String, Any>(
                    "avg" to String.format(java.util.Locale.US, "%.2f", summary.average),
                    "rank" to summary.rank,
                    "size" to summary.classSize,
                    "mention" to summary.mention,
                    "subjects" to subjectsMap
                )"""

target2 = """                val studentRef = database.getReference("students").child(matricule).child("academics").child(term)
                studentRef.setValue(termData)"""

replacement2 = """                val studentRef = database.getReference("students").child(matricule).child("academics").child(term)
                studentRef.setValue(termData)
                
                // Sync photo and logo too
                val rootStudentRef = database.getReference("students").child(matricule)
                val profileUpdates = mutableMapOf<String, Any>()
                if (!student.photoBase64.isNullOrBlank()) {
                    profileUpdates["photoBase64"] = student.photoBase64
                }
                val logo = _schoolLogoBase64.value
                if (!logo.isNullOrBlank()) {
                    profileUpdates["schoolLogo"] = logo
                }
                if (profileUpdates.isNotEmpty()) {
                    rootStudentRef.updateChildren(profileUpdates)
                }"""

content = content.replace(target, replacement).replace(target2, replacement2)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
print("Updated Academics Sync")
