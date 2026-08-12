import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

target = """                for (s in students) {
                    val matricule = if (!s.remoteId.isNullOrEmpty() && s.remoteId.length >= 5) s.remoteId.take(5).uppercase() else s.id.toString()
                    database.getReference("students").child(matricule).child("schoolLogo").setValue(base64 ?: "")
                }"""

replacement = """                for (s in students) {
                    val matricule = if (!s.remoteId.isNullOrEmpty() && s.remoteId.length >= 5) s.remoteId.take(5).uppercase() else s.id.toString()
                    val ref = database.getReference("students").child(matricule)
                    ref.child("schoolLogo").setValue(base64 ?: "")
                    _schoolAccount.value?.let { acc ->
                        ref.child("schoolName").setValue(acc.displayName.ifEmpty { acc.schoolName })
                        ref.child("schoolAddress").setValue(acc.address)
                        ref.child("schoolPhone").setValue(acc.founderPhone)
                        ref.child("schoolYear").setValue(_selectedSchoolYear.value)
                    }
                }"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
