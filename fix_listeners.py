import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r') as f:
    content = f.read()

# Fix studentsListener
old_students = """        val studentsListener = firestore.collection("schools").document(email).collection("students").addSnapshotListener { snapshot, e ->
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
                        schoolYear = doc.getString("schoolYear") ?: "2024-2025"
                    )
                    
                    if (existing != null) repository.updateStudent(student) else repository.insertStudent(student)
                }
            }
        }"""

new_students = """        val studentsListener = firestore.collection("schools").document(email).collection("students").addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) return@addSnapshotListener
            viewModelScope.launch {
                for (change in snapshot.documentChanges) {
                    val doc = change.document
                    val remoteId = doc.id
                    val existing = repository.getStudentByRemoteId(remoteId)
                    
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.REMOVED) {
                        if (existing != null) repository.deleteStudentById(existing.id)
                        continue
                    }
                    
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
                        schoolYear = doc.getString("schoolYear") ?: "2024-2025"
                    )
                    
                    if (existing != null) repository.updateStudent(student) else repository.insertStudent(student)
                }
            }
        }"""

content = content.replace(old_students, new_students)

# Fix paymentsListener
old_payments = """        val paymentsListener = firestore.collection("schools").document(email).collection("payments").addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) return@addSnapshotListener
            viewModelScope.launch {
                for (doc in snapshot.documents) {
                    val remoteId = doc.id
                    val existing = repository.getPaymentByRemoteId(remoteId)
                    val studentRemoteId = doc.getString("studentRemoteId") ?: continue
                    val studentId = repository.getStudentIdByRemoteId(studentRemoteId) ?: continue
                    
                    val payment = Payment(
                        id = existing?.id ?: 0,
                        schoolId = schoolId,
                        studentId = studentId,
                        amount = doc.getLong("amount") ?: 0L,
                        date = doc.getLong("date") ?: System.currentTimeMillis(),
                        reason = doc.getString("reason") ?: "",
                        remoteId = remoteId,
                        paymentMethod = doc.getString("paymentMethod") ?: "Espèces"
                    )
                    if (existing != null) repository.updatePayment(payment) else repository.insertPayment(payment)
                }
            }
        }"""

new_payments = """        val paymentsListener = firestore.collection("schools").document(email).collection("payments").addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) return@addSnapshotListener
            viewModelScope.launch {
                for (change in snapshot.documentChanges) {
                    val doc = change.document
                    val remoteId = doc.id
                    val existing = repository.getPaymentByRemoteId(remoteId)
                    
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.REMOVED) {
                        if (existing != null) repository.deletePayment(existing.id)
                        continue
                    }
                    
                    val studentRemoteId = doc.getString("studentRemoteId") ?: continue
                    val studentId = repository.getStudentIdByRemoteId(studentRemoteId) ?: continue
                    
                    val payment = Payment(
                        id = existing?.id ?: 0,
                        schoolId = schoolId,
                        studentId = studentId,
                        amount = doc.getLong("amount") ?: 0L,
                        date = doc.getLong("date") ?: System.currentTimeMillis(),
                        reason = doc.getString("reason") ?: "",
                        remoteId = remoteId,
                        paymentMethod = doc.getString("paymentMethod") ?: "Espèces"
                    )
                    if (existing != null) repository.updatePayment(payment) else repository.insertPayment(payment)
                }
            }
        }"""

content = content.replace(old_payments, new_payments)

# Fix expensesListener
old_expenses = """        val expensesListener = firestore.collection("schools").document(email).collection("expenses").addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) return@addSnapshotListener
            viewModelScope.launch {
                for (doc in snapshot.documents) {
                    val remoteId = doc.id
                    val existing = repository.getExpenseByRemoteId(remoteId)
                    val expense = Expense(
                        id = existing?.id ?: 0,
                        schoolId = schoolId,
                        amount = doc.getLong("amount") ?: 0L,
                        date = doc.getLong("date") ?: System.currentTimeMillis(),
                        reason = doc.getString("reason") ?: "",
                        section = doc.getString("section") ?: "Général",
                        remoteId = remoteId
                    )
                    if (existing != null) repository.updateExpense(expense) else repository.insertExpense(expense)
                }
            }
        }"""

new_expenses = """        val expensesListener = firestore.collection("schools").document(email).collection("expenses").addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) return@addSnapshotListener
            viewModelScope.launch {
                for (change in snapshot.documentChanges) {
                    val doc = change.document
                    val remoteId = doc.id
                    val existing = repository.getExpenseByRemoteId(remoteId)
                    
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.REMOVED) {
                        if (existing != null) repository.deleteExpense(existing.id)
                        continue
                    }
                    
                    val expense = Expense(
                        id = existing?.id ?: 0,
                        schoolId = schoolId,
                        amount = doc.getLong("amount") ?: 0L,
                        date = doc.getLong("date") ?: System.currentTimeMillis(),
                        reason = doc.getString("reason") ?: "",
                        section = doc.getString("section") ?: "Général",
                        remoteId = remoteId
                    )
                    if (existing != null) repository.updateExpense(expense) else repository.insertExpense(expense)
                }
            }
        }"""

content = content.replace(old_expenses, new_expenses)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w') as f:
    f.write(content)

print("Listeners patched!")
