import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r') as f:
    content = f.read()

# Add schoolAddress to the sync block in syncStudentAcademicsToRTDB
old_block = """                val account = _schoolAccount.value
                if (account != null) {
                    finalUpdateData["schoolName"] = account.displayName.takeIf { it.isNotBlank() } ?: account.schoolName
                    if (account.logoBase64 != null) {
                        finalUpdateData["logoBase64"] = account.logoBase64!!
                    }
                }"""

new_block = """                val account = _schoolAccount.value
                if (account != null) {
                    finalUpdateData["schoolName"] = account.displayName.takeIf { it.isNotBlank() } ?: account.schoolName
                    finalUpdateData["schoolAddress"] = account.address
                    if (account.logoBase64 != null) {
                        finalUpdateData["logoBase64"] = account.logoBase64!!
                    }
                }"""

content = content.replace(old_block, new_block)

# Also add to updateStudentFinancialsInRTDB
old_fin = """            val updateData = mutableMapOf<String, Any>(
                "totalFee" to totalFee,
                "paidFee" to totalPaid
            )"""

new_fin = """            val updateData = mutableMapOf<String, Any>(
                "totalFee" to totalFee,
                "paidFee" to totalPaid
            )
            val account = _schoolAccount.value
            if (account != null) {
                updateData["schoolName"] = account.displayName.takeIf { it.isNotBlank() } ?: account.schoolName
                updateData["schoolAddress"] = account.address
                if (account.logoBase64 != null) {
                    updateData["logoBase64"] = account.logoBase64!!
                }
            }"""

content = content.replace(old_fin, new_fin)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w') as f:
    f.write(content)

print("Android Address Patched!")
