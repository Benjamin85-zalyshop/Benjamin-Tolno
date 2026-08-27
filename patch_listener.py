import re
with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r') as f:
    content = f.read()

target = """                    val payment = Payment(
                        id = existing?.id ?: 0,
                        schoolId = schoolId,
                        studentId = studentId,
                        amount = doc.getLong("amount") ?: 0L,
                        date = doc.getLong("date") ?: System.currentTimeMillis(),
                        reason = doc.getString("reason") ?: "",
                        remoteId = remoteId,
                        paymentMethod = doc.getString("paymentMethod") ?: "Espèces"
                    )"""

replacement = """                    val payment = Payment(
                        id = existing?.id ?: 0,
                        schoolId = schoolId,
                        studentId = studentId,
                        amount = doc.getLong("amount") ?: 0L,
                        date = doc.getLong("date") ?: System.currentTimeMillis(),
                        reason = doc.getString("reason") ?: "",
                        remoteId = remoteId,
                        paymentMethod = doc.getString("paymentMethod") ?: "Espèces",
                        isCancelled = doc.getBoolean("isCancelled") ?: false,
                        cancellationReason = doc.getString("cancellationReason"),
                        cancelledBy = doc.getString("cancelledBy"),
                        cancelledAt = doc.getLong("cancelledAt")
                    )"""

content = content.replace(target, replacement)
with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w') as f:
    f.write(content)
