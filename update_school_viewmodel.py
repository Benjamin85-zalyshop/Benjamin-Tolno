import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

target = """                val updates = mapOf(
                    "totalFee" to totalFee,
                    "paidFee" to paidFee
                )
                studentRef.updateChildren(updates)"""

replacement = """                val updates = mutableMapOf<String, Any>(
                    "totalFee" to totalFee,
                    "paidFee" to paidFee
                )
                if (!student.photoBase64.isNullOrBlank()) {
                    updates["photoBase64"] = student.photoBase64
                }
                val logo = _schoolLogoBase64.value
                if (!logo.isNullOrBlank()) {
                    updates["schoolLogo"] = logo
                }
                studentRef.updateChildren(updates)"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
print("Updated SchoolViewModel.kt")
