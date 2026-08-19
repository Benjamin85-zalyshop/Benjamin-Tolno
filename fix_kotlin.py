import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r') as f:
    content = f.read()

# Replace the fetching code and finalUpdateData logic
old_block = """                val fees = loadClassFees(schoolId)
                val classFeeAmount = fees.find { it.grade == student.grade && it.section == student.section }?.amount ?: 0L
                val totalFee = student.registrationFee + student.reenrollmentFee + classFeeAmount
                
                val finalUpdateData = updateData.toMutableMap()
                finalUpdateData["totalFee"] = totalFee
                finalUpdateData["paidFee"] = totalPaid
                if (student.photoBase64 != null) {
                    finalUpdateData["photoBase64"] = student.photoBase64
                }
                val account = _schoolAccount.value
                if (account != null) {
                    finalUpdateData["schoolName"] = account.displayName.takeIf { it.isNotBlank() } ?: account.schoolName
                    if (account.logoBase64 != null) {
                        finalUpdateData["logoBase64"] = account.logoBase64
                    }
                }"""

new_block = """                val fees = loadClassFees(schoolId)
                val classFeeAmount = fees.find { it.grade == student.grade }?.feeAmount ?: 0L
                val totalFee = student.registrationFee + student.reenrollmentFee + classFeeAmount
                
                val finalUpdateData = mutableMapOf<String, Any>()
                finalUpdateData.putAll(updateData)
                finalUpdateData["totalFee"] = totalFee
                finalUpdateData["paidFee"] = totalPaid
                if (student.photoBase64 != null) {
                    finalUpdateData["photoBase64"] = student.photoBase64!!
                }
                val account = _schoolAccount.value
                if (account != null) {
                    finalUpdateData["schoolName"] = account.displayName.takeIf { it.isNotBlank() } ?: account.schoolName
                    if (account.logoBase64 != null) {
                        finalUpdateData["logoBase64"] = account.logoBase64!!
                    }
                }"""

content = content.replace(old_block, new_block)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w') as f:
    f.write(content)

print("Kotlin patched!")
