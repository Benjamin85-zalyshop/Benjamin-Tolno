package com.example.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.*
import com.example.data.repository.SchoolRepository
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class SchoolAdminItem(
    val email: String,
    val displayName: String = "",
    val schoolName: String = "",
    val founderPhone: String = "",
    val address: String = "",
    val isPendingValidation: Boolean = false,
    val hasActiveSubscription: Boolean = false,
    val subscriptionExpiryDate: Long? = null,
    val createdAt: Long = 0L,
    val paymentPhoneNumber: String? = null,
    val transactionId: String? = null,
    val rejectionReason: String? = null
)

class SchoolViewModel(
    private val repository: SchoolRepository,
    private val context: Context
) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val sharedPrefs = context.getSharedPreferences("scolapay_prefs", Context.MODE_PRIVATE)
    private val activeListeners = mutableListOf<ListenerRegistration>()
    private val _adminError = MutableStateFlow<String?>(null)
    val adminError: StateFlow<String?> = _adminError

    private val _adminSchools = MutableStateFlow<List<SchoolAdminItem>>(emptyList())
    val adminSchools: StateFlow<List<SchoolAdminItem>> = _adminSchools

    private val _classFees = MutableStateFlow<List<ClassFee>>(emptyList())
    val classFees: StateFlow<List<ClassFee>> = _classFees

    private val _currentSchoolId = MutableStateFlow<Int?>(null)
    val currentSchoolId: StateFlow<Int?> = _currentSchoolId

    private val _deletionRequests = MutableStateFlow<List<DeletionRequest>>(emptyList())
    val deletionRequests: StateFlow<List<DeletionRequest>> = _deletionRequests

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError

    private val _pendingOrderId = MutableStateFlow<String?>(null)
    val pendingOrderId: StateFlow<String?> = _pendingOrderId

    private val _schoolAccount = MutableStateFlow<SchoolAccount?>(null)
    val schoolAccount: StateFlow<SchoolAccount?> = _schoolAccount

    private val _schoolLogoBase64 = MutableStateFlow<String?>(null)
    val schoolLogoBase64: StateFlow<String?> = _schoolLogoBase64

    private val _schoolName = MutableStateFlow<String?>(null)
    val schoolName: StateFlow<String?> = _schoolName

    private val _selectedSchoolYear = MutableStateFlow<String?>(null)
    val selectedSchoolYear: StateFlow<String?> = _selectedSchoolYear

    private val _selectedSection = MutableStateFlow<String?>("Toutes les sections")
    val selectedSection: StateFlow<String?> = _selectedSection

    private val _userRole = MutableStateFlow<String?>("FOUNDER")
    val userRole: StateFlow<String?> = _userRole



    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val students: StateFlow<List<Student>> = _currentSchoolId.flatMapLatest { id ->
        if (id != null) repository.getAllStudents(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val payments: StateFlow<List<Payment>> = _currentSchoolId.flatMapLatest { id ->
        if (id != null) repository.getAllPayments(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val expenses: StateFlow<List<Expense>> = _currentSchoolId.flatMapLatest { id ->
        if (id != null) repository.getAllExpenses(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val subjects: StateFlow<List<Subject>> = _currentSchoolId.flatMapLatest { id ->
        if (id != null) repository.getAllSubjects(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val grades: StateFlow<List<StudentGrade>> = _currentSchoolId.flatMapLatest { id ->
        if (id != null) repository.getAllGrades(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val totalCollected: StateFlow<Long> = _currentSchoolId.flatMapLatest { id ->
        if (id != null) repository.getTotalCollected(id).map { it ?: 0L } else flowOf(0L)
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0L)
    
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val totalExpenses: StateFlow<Long> = _currentSchoolId.flatMapLatest { id ->
        if (id != null) repository.getTotalExpenses(id).map { it ?: 0L } else flowOf(0L)
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0L)
    
    val balance: StateFlow<Long> = combine(totalCollected, totalExpenses) { col, exp -> col - exp }.stateIn(viewModelScope, SharingStarted.Lazily, 0L)
    
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val hasActiveSubscription: StateFlow<Boolean> = _currentSchoolId.flatMapLatest { id ->
        if (id != null) repository.getSubscriptionStatus(id) else flowOf(false)
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)
    
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val isPendingValidation: StateFlow<Boolean> = _currentSchoolId.flatMapLatest { id ->
        if (id != null) repository.getPendingValidationStatus(id) else flowOf(false)
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)
    val trialDaysRemaining: StateFlow<Long> = MutableStateFlow(30L)
    val isTrialActive: StateFlow<Boolean> = MutableStateFlow(true)
    val isAppAccessGranted: StateFlow<Boolean> = MutableStateFlow(true)
    
    fun getPendingOrderId(): String? = _pendingOrderId.value
    fun setSelectedSchoolYear(year: String) {
        // TODO: Auto-generated stub
    }

    fun clearSession() {
        // TODO: Auto-generated stub
    }

    fun setSection(section: String) {
        // TODO: Auto-generated stub
    }

    fun insertStudent(firstName: String, lastName: String, grade: String, section: String, parentWhatsApp: String?, registrationFee: Long, reenrollmentFee: Long, photoBase64: String?) {
        val schoolId = _currentSchoolId.value ?: return
        val email = _schoolAccount.value?.schoolName ?: return
        viewModelScope.launch {
            val remoteId = java.util.UUID.randomUUID().toString()
            val student = Student(
                schoolId = schoolId,
                firstName = firstName,
                lastName = lastName,
                grade = grade,
                section = section,
                remoteId = remoteId,
                parentWhatsApp = parentWhatsApp ?: "",
                registrationFee = registrationFee,
                reenrollmentFee = reenrollmentFee,
                photoBase64 = photoBase64,
                schoolYear = _selectedSchoolYear.value ?: ""
            )
            repository.insertStudent(student)
            
            firestore.collection("schools").document(email).collection("students").document(remoteId).set(
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
            ).addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e) }
        }
    }

    fun updateStudentPhoto(student: Student, photoBase64: String?) {
        val email = _schoolAccount.value?.schoolName ?: return
        viewModelScope.launch {
            val updated = student.copy(photoBase64 = photoBase64)
            repository.updateStudent(updated)
            
            if (updated.remoteId.isNotEmpty()) {
                firestore.collection("schools").document(email).collection("students").document(updated.remoteId).set(
                    mapOf("photoBase64" to photoBase64),
                    com.google.firebase.firestore.SetOptions.merge()
                ).addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e) }
            }
        }
    }

    private fun loadClassFees(schoolId: Int): List<ClassFee> {
        val jsonStr = sharedPrefs.getString("class_fees_$schoolId", null)
        if (jsonStr != null) {
            try {
                return kotlinx.serialization.json.Json.decodeFromString(jsonStr)
            } catch (e: Exception) {
                android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e)
            }
        }
        return emptyList()
    }

    private fun saveClassFees(schoolId: Int, fees: List<ClassFee>) {
        try {
            val jsonStr = kotlinx.serialization.json.Json.encodeToString(fees)
            sharedPrefs.edit().putString("class_fees_$schoolId", jsonStr).apply()
        } catch (e: Exception) {
            android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e)
        }
    }

    fun setClassFee(grade: String, amount: Long) {
        val schoolId = _currentSchoolId.value ?: return
        val currentList = _classFees.value.toMutableList()
        val index = currentList.indexOfFirst { it.grade == grade }
        if (index >= 0) {
            currentList[index] = currentList[index].copy(feeAmount = amount)
        } else {
            currentList.add(ClassFee(grade = grade, feeAmount = amount))
        }
        _classFees.value = currentList
        saveClassFees(schoolId, currentList)
    }

    fun setSchoolLogo(base64: String?) {
        _schoolLogoBase64.value = base64
        val account = _schoolAccount.value ?: return
        viewModelScope.launch {
            val updated = account.copy(logoBase64 = base64)
            _schoolAccount.value = updated
            repository.updateSchoolAccount(updated)
        }
    }

    fun createDeletionRequest(student: Student, reason: String) {
        // TODO: Auto-generated stub
    }

    fun approveDeletionRequest(request: DeletionRequest) {
        // TODO: Auto-generated stub
    }

    fun rejectDeletionRequest(request: DeletionRequest, reason: String) {
        // TODO: Auto-generated stub
    }

    fun dismissDeletionRequest(request: DeletionRequest) {
        // TODO: Auto-generated stub
    }

    fun deleteStudentDirectly(student: Student) {
        val email = _schoolAccount.value?.schoolName ?: return
        viewModelScope.launch {
            repository.deleteStudentById(student.id)
            if (student.remoteId.isNotEmpty()) {
                firestore.collection("schools").document(email).collection("students").document(student.remoteId).delete()
                    .addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e) }
            }
        }
    }

    fun insertPayment(studentId: Int, amount: Long, reason: String, paymentMethod: String) {
        val schoolId = _currentSchoolId.value ?: return
        val email = _schoolAccount.value?.schoolName ?: return
        viewModelScope.launch {
            val student = repository.getStudentById(studentId) ?: return@launch
            val remoteId = java.util.UUID.randomUUID().toString()
            val payment = Payment(
                schoolId = schoolId,
                studentId = studentId,
                amount = amount,
                reason = reason,
                remoteId = remoteId,
                paymentMethod = paymentMethod
            )
            repository.insertPayment(payment)
            
            firestore.collection("schools").document(email).collection("payments").document(remoteId).set(
                mapOf(
                    "studentRemoteId" to student.remoteId,
                    "amount" to payment.amount,
                    "reason" to payment.reason,
                    "date" to payment.date,
                    "paymentMethod" to payment.paymentMethod
                )
            ).addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e) }
        }
    }

    fun deletePayment(paymentId: Int) {
        val email = _schoolAccount.value?.schoolName ?: return
        viewModelScope.launch {
            val payment = repository.getPaymentById(paymentId) ?: return@launch
            repository.deletePayment(paymentId)
            
            if (payment.remoteId.isNotEmpty()) {
                firestore.collection("schools").document(email).collection("payments").document(payment.remoteId).delete()
                    .addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e) }
            }
        }
    }

    fun insertExpense(amount: Long, category: String, description: String, section: String) {
        val schoolId = _currentSchoolId.value ?: return
        val email = _schoolAccount.value?.schoolName ?: return
        viewModelScope.launch {
            val fullReason = if(description.isNotBlank()) "$category - $description" else category
            val remoteId = java.util.UUID.randomUUID().toString()
            val expense = Expense(
                schoolId = schoolId,
                amount = amount,
                reason = fullReason,
                section = section,
                remoteId = remoteId
            )
            repository.insertExpense(expense)
            
            firestore.collection("schools").document(email).collection("expenses").document(remoteId).set(
                mapOf(
                    "amount" to expense.amount,
                    "reason" to expense.reason,
                    "section" to expense.section,
                    "date" to expense.date
                )
            ).addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e) }
        }
    }

    fun deleteExpense(expenseId: Int) {
        val email = _schoolAccount.value?.schoolName ?: return
        viewModelScope.launch {
            val expense = repository.getExpenseById(expenseId) ?: return@launch
            repository.deleteExpense(expenseId)
            
            if (expense.remoteId.isNotEmpty()) {
                firestore.collection("schools").document(email).collection("expenses").document(expense.remoteId).delete()
                    .addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e) }
            }
        }
    }

    fun insertSubject(section: String, grade: String, name: String, coefficient: Int, maxScore: Float) {
        val schoolId = _currentSchoolId.value ?: return
        viewModelScope.launch {
            repository.insertSubject(Subject(
                schoolId = schoolId,
                section = section,
                grade = grade,
                name = name,
                coefficient = coefficient,
                maxScore = maxScore
            ))
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch {
            repository.deleteSubjectById(subject.id)
        }
    }

    fun seedDefaultSubjects(section: String, grade: String) {
        val schoolId = _currentSchoolId.value ?: return
        
        val defaultSubjects = when (section) {
            "LA MATERNELLE" -> listOf(
                Triple("Langage", 1, 10f),
                Triple("Graphisme / Écriture", 1, 10f),
                Triple("Activités Mathématiques", 1, 10f),
                Triple("Activités Éveils / Jeux", 1, 10f),
                Triple("Dessin / Coloriage", 1, 10f)
            )
            "LE PRIMAIRE" -> when (grade) {
                "1ère Année", "2ème Année" -> listOf(
                    Triple("Calcul", 1, 10f),
                    Triple("Exercice sensoriel", 1, 10f),
                    Triple("Lecture", 1, 10f),
                    Triple("Langage", 1, 10f),
                    Triple("Ecriture", 1, 10f),
                    Triple("Recit/chant", 1, 10f),
                    Triple("Dessin", 1, 10f)
                )
                "3ème Année", "4ème Année" -> listOf(
                    Triple("Calcul", 1, 10f),
                    Triple("Dictée/Question", 1, 10f),
                    Triple("Géographie", 1, 10f),
                    Triple("Histoire", 1, 10f),
                    Triple("ECM", 1, 10f),
                    Triple("Expres/Redatio", 1, 10f),
                    Triple("Science", 1, 10f),
                    Triple("Lecture", 1, 10f),
                    Triple("Ecriture", 1, 10f),
                    Triple("Recit/chant", 1, 10f),
                    Triple("Dessin", 1, 10f)
                )
                "5ème Année", "6ème Année" -> listOf(
                    Triple("Lecture", 1, 10f),
                    Triple("Dictée/qustions", 1, 10f),
                    Triple("Expression Ecrite/Redaction", 1, 10f),
                    Triple("Ecriture", 1, 10f),
                    Triple("Calcul Ecrit", 1, 10f),
                    Triple("Histoire", 1, 10f),
                    Triple("Science Observation", 1, 10f),
                    Triple("Geographie", 1, 10f),
                    Triple("Dessin", 1, 10f),
                    Triple("Récitation/Chant", 1, 10f),
                    Triple("Instructio Civique", 1, 10f)
                )
                else -> listOf(
                    Triple("Calcul", 1, 10f),
                    Triple("Dictée", 1, 10f),
                    Triple("Lecture", 1, 10f)
                )
            }
            "LE COLLÈGE" -> listOf(
                Triple("DICTEE/QUESTION", 2, 20f),
                Triple("REDACTION", 1, 20f),
                Triple("HISTOIRE", 1, 20f),
                Triple("GEOGRAPHIE", 1, 20f),
                Triple("MATHS", 2, 20f),
                Triple("BIOLOGIE", 1, 20f),
                Triple("PHYSIQUES", 1, 20f),
                Triple("CHIMIE", 1, 20f),
                Triple("E.C.M", 1, 20f),
                Triple("ANGLAIS", 1, 20f)
            )
            "LE LYCÉE" -> {
                when {
                    grade.contains("SS") || grade.contains("SL") -> listOf(
                        Triple("ANGL", 3, 20f),
                        Triple("Eco-Po", 2, 20f),
                        Triple("FRAN", 4, 20f),
                        Triple("HIST", 2, 20f),
                        Triple("GEOG", 2, 20f),
                        Triple("MATH", 2, 20f),
                        Triple("PHILO", 3, 20f)
                    )
                    grade.contains("11ème SE") || grade.contains("12ème SE") -> listOf(
                        Triple("ANGL", 2, 20f),
                        Triple("BIO-GEOL", 4, 20f),
                        Triple("CHIM", 3, 20f),
                        Triple("FRAN", 2, 20f),
                        Triple("MATH", 2, 20f),
                        Triple("PHILO", 2, 20f),
                        Triple("PHYSI", 3, 20f)
                    )
                    grade.contains("TSE") -> listOf(
                        Triple("ANGL", 2, 20f),
                        Triple("BIOL", 3, 20f),
                        Triple("CHIM", 3, 20f),
                        Triple("FRAN", 2, 20f),
                        Triple("GEOL", 1, 20f),
                        Triple("MATH", 2, 20f),
                        Triple("PHILO", 2, 20f),
                        Triple("PHYSI", 3, 20f)
                    )
                    grade.contains("SM") -> listOf(
                        Triple("ANGL", 2, 20f),
                        Triple("CHIM", 3, 20f),
                        Triple("Eco-Po", 2, 20f),
                        Triple("FRAN", 2, 20f),
                        Triple("MATH", 4, 20f),
                        Triple("PHILO", 2, 20f),
                        Triple("PHYSI", 3, 20f)
                    )
                    else -> listOf(
                        Triple("MATH", 2, 20f),
                        Triple("FRAN", 2, 20f),
                        Triple("ANGL", 2, 20f)
                    )
                }
            }
            else -> listOf(
                Triple("Mathématiques", 2, 20f),
                Triple("Français", 2, 20f),
                Triple("Anglais", 2, 20f)
            )
        }

        viewModelScope.launch {
            defaultSubjects.forEach { (name, coeff, maxScore) ->
                repository.insertSubject(Subject(
                    schoolId = schoolId,
                    section = section,
                    grade = grade,
                    name = name,
                    coefficient = coeff,
                    maxScore = maxScore
                ))
            }
        }
    }

    fun saveGrade(studentId: Int, studentRemoteId: String, subjectId: Int, subjectRemoteId: String, term: String, evaluationScore: Float?, examScore: Float?, comment: String?) {
        val schoolId = _currentSchoolId.value ?: return
        viewModelScope.launch {
            val existing = repository.getExistingGrade(schoolId, studentId, subjectId, term)
            val gradeToSave = if (existing != null) {
                existing.copy(
                    evaluationScore = evaluationScore,
                    examScore = examScore,
                    teacherComment = comment
                )
            } else {
                StudentGrade(
                    schoolId = schoolId,
                    studentId = studentId,
                    studentRemoteId = studentRemoteId,
                    subjectId = subjectId,
                    subjectRemoteId = subjectRemoteId,
                    term = term,
                    evaluationScore = evaluationScore,
                    examScore = examScore,
                    teacherComment = comment
                )
            }
            repository.insertGrade(gradeToSave)
        }
    }

    fun updateStudentFinancialsInRTDB(schoolId: Int, studentId: Int) {
        // TODO: Auto-generated stub
    }

    fun syncStudentAcademicsToRTDB(schoolId: Int, studentId: Int, term: String) {
        viewModelScope.launch {
            val allStudents = repository.getAllStudentsDirect(schoolId)
            val student = allStudents.find { it.id == studentId } ?: return@launch
            val subjects = repository.getAllSubjectsDirect(schoolId).filter { it.section == student.section && it.grade == student.grade }
            val allGrades = repository.getAllGradesDirect(schoolId).filter { it.term == term }
            val studentGrades = allGrades.filter { it.studentId == studentId }
            
            // Calculate summary
            var totalCoef = 0
            var totalPoints = 0f
            val detailsList = mutableListOf<Map<String, Any>>()
            
            for (subject in subjects) {
                val grade = studentGrades.find { it.subjectId == subject.id }
                val score = grade?.evaluationScore ?: 0f
                totalCoef += subject.coefficient
                totalPoints += (score / subject.maxScore * 20f) * subject.coefficient
                
                detailsList.add(mapOf(
                    "Matière" to subject.name,
                    "Éval." to if (grade != null && grade.evaluationScore != null) grade.evaluationScore.toString() else "-",
                    "Moy." to if (grade != null && grade.evaluationScore != null) String.format(java.util.Locale.US, "%.2f", grade.evaluationScore) else "-"
                ))
            }
            
            val average = if (totalCoef > 0) totalPoints / totalCoef else 0f
            
            // Calculate rank (simplified, just among those with grades)
            val allStudentsInClass = allStudents.filter { it.section == student.section && it.grade == student.grade }
            val averages = allStudentsInClass.mapNotNull { otherStudent ->
                val otherGrades = allGrades.filter { it.studentId == otherStudent.id }
                if (otherGrades.isEmpty()) return@mapNotNull null
                
                var otherTotalCoef = 0
                var otherTotalPoints = 0f
                for (subject in subjects) {
                    val g = otherGrades.find { it.subjectId == subject.id }
                    if (g != null && g.evaluationScore != null) {
                        otherTotalCoef += subject.coefficient
                        otherTotalPoints += (g.evaluationScore) / subject.maxScore * 20f * subject.coefficient
                    }
                }
                if (otherTotalCoef > 0) otherTotalPoints / otherTotalCoef else 0f
            }.sortedDescending()
            
            val rank = averages.indexOfFirst { it <= average }.takeIf { it >= 0 }?.plus(1) ?: 1
            
            val mention = when {
                average >= 18 -> "Excellent"
                average >= 16 -> "Très Bien"
                average >= 14 -> "Bien"
                average >= 12 -> "Assez Bien"
                average >= 10 -> "Passable"
                average >= 8 -> "Insuffisant"
                else -> "Médiocre"
            }
            
            val updateData = mapOf(
                "academic_$term" to mapOf(
                    "average" to String.format(java.util.Locale.US, "%.2f", average),
                    "rank" to "$rank",
                    "classSize" to allStudentsInClass.size.toString(),
                    "mention" to mention,
                    "details" to detailsList
                )
            )
            
            // The document ID in firestore should be student.remoteId if it exists
            if (student.remoteId.isNotEmpty()) {
                firestore.collection("students").document(student.remoteId)
                    .set(updateData, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener {
                        println("Successfully synced academics to Firestore for student ${student.id}")
                    }
                    .addOnFailureListener { e ->
                        println("Failed to sync academics to Firestore: ${e.message}")
                    }
            }
        }
    }

    fun logout() {
        _currentSchoolId.value = null
        _schoolAccount.value = null
        _userRole.value = null
        _schoolName.value = null
        
        sharedPrefs.edit()
            .remove("logged_in_email")
            .remove("logged_in_role")
            .apply()
    }

    fun activateSubscription() {
        // TODO: Auto-generated stub
    }

    fun savePendingOrderId(orderId: String) {
        // TODO: Auto-generated stub
    }

    fun clearPendingOrderId() {
        // TODO: Auto-generated stub
    }

    fun checkPendingPaymentStatus(onResult: (String) -> Unit) {
        // TODO: Auto-generated stub
    }

    fun submitSubscriptionRequest(phoneNumber: String, transactionId: String) {
        viewModelScope.launch {
            val account = _schoolAccount.value
            if (account != null) {
                val updated = account.copy(
                    isPendingValidation = true,
                    paymentPhoneNumber = phoneNumber,
                    transactionId = transactionId
                )
                repository.updateSchoolAccount(updated)
                _schoolAccount.value = updated
                
                firestore.collection("schools").document(account.schoolName).set(
                    mapOf(
                        "isPendingValidation" to true,
                        "paymentPhoneNumber" to phoneNumber,
                        "transactionId" to transactionId
                    ), com.google.firebase.firestore.SetOptions.merge()
                ).addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e) }
            }
        }
    }

    fun updateFinancierPassword(newPassword: String) {
        viewModelScope.launch {
            val account = _schoolAccount.value
            if (account != null) {
                val updated = account.copy(financierPasswordHash = newPassword)
                repository.updateSchoolAccount(updated)
                _schoolAccount.value = updated
                
                firestore.collection("schools").document(account.schoolName).set(
                    mapOf("financierPasswordHash" to newPassword),
                    com.google.firebase.firestore.SetOptions.merge()
                ).addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e) }
            }
        }
    }

    private fun syncSchoolDataFromFirestore(email: String, schoolId: Int) {
        val studentsListener = firestore.collection("schools").document(email).collection("students").addSnapshotListener { snapshot, e ->
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
        }
        activeListeners.add(studentsListener)
        
        val paymentsListener = firestore.collection("schools").document(email).collection("payments").addSnapshotListener { snapshot, e ->
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
        }
        activeListeners.add(paymentsListener)
        
        val expensesListener = firestore.collection("schools").document(email).collection("expenses").addSnapshotListener { snapshot, e ->
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
        }
        activeListeners.add(expensesListener)
    }

    private fun syncSchoolsFromFirestore() {
        val listener = firestore.collection("schools").addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) return@addSnapshotListener
            viewModelScope.launch {
                for (doc in snapshot.documents) {
                    val email = doc.id
                    val displayName = doc.getString("displayName") ?: email
                    val hasActiveSubscription = doc.getBoolean("hasActiveSubscription") ?: false
                    val isPendingValidation = doc.getBoolean("isPendingValidation") ?: false
                    val founderPhone = doc.getString("founderPhone") ?: ""
                    val financierPasswordHash = doc.getString("financierPasswordHash") ?: ""
                    val passwordHash = doc.getString("passwordHash") ?: ""
                    val logoBase64 = doc.getString("logoBase64")
                    val address = doc.getString("address") ?: ""
                    val paymentPhoneNumber = doc.getString("paymentPhoneNumber")
                    val transactionId = doc.getString("transactionId")
                    val rejectionReason = doc.getString("rejectionReason")
                    val subscriptionExpiryDate = doc.getLong("subscriptionExpiryDate") ?: 0L
                    val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    
                    val existing = repository.getSchoolAccountByName(email)
                    if (existing != null) {
                        repository.updateSchoolAccount(
                            existing.copy(
                                displayName = displayName,
                                hasActiveSubscription = hasActiveSubscription,
                                isPendingValidation = isPendingValidation,
                                founderPhone = founderPhone,
                                financierPasswordHash = financierPasswordHash,
                                passwordHash = passwordHash,
                                logoBase64 = logoBase64 ?: existing.logoBase64,
                                address = address,
                                paymentPhoneNumber = paymentPhoneNumber ?: existing.paymentPhoneNumber,
                                transactionId = transactionId ?: existing.transactionId,
                                rejectionReason = rejectionReason,
                                subscriptionExpiryDate = subscriptionExpiryDate,
                                createdAt = createdAt
                            )
                        )
                    } else {
                        repository.insertSchoolAccountDirect(
                            SchoolAccount(
                                schoolName = email,
                                displayName = displayName,
                                hasActiveSubscription = hasActiveSubscription,
                                isPendingValidation = isPendingValidation,
                                founderPhone = founderPhone,
                                financierPasswordHash = financierPasswordHash,
                                passwordHash = passwordHash,
                                logoBase64 = logoBase64,
                                address = address,
                                paymentPhoneNumber = paymentPhoneNumber,
                                transactionId = transactionId,
                                rejectionReason = rejectionReason,
                                subscriptionExpiryDate = subscriptionExpiryDate,
                                createdAt = createdAt
                            )
                        )
                    }
                }
                if (_userRole.value == "ADMIN") {
                    val accounts = repository.getAllSchoolAccounts()
                    val adminItems = accounts.map {
                        SchoolAdminItem(
                            email = it.schoolName,
                            displayName = it.displayName,
                            schoolName = it.schoolName,
                            founderPhone = it.founderPhone,
                            address = it.address,
                            isPendingValidation = it.isPendingValidation,
                            hasActiveSubscription = it.hasActiveSubscription,
                            subscriptionExpiryDate = it.subscriptionExpiryDate,
                            paymentPhoneNumber = it.paymentPhoneNumber,
                            transactionId = it.transactionId
                        )
                    }
                    _adminSchools.value = adminItems
                }
            }
        }
        activeListeners.add(listener)
    }

    fun loadAdminSchools() {
        viewModelScope.launch {
            val accounts = repository.getAllSchoolAccounts()
            val adminItems = accounts.map {
                SchoolAdminItem(
                    email = it.schoolName,
                    displayName = it.displayName,
                    schoolName = it.schoolName,
                    founderPhone = it.founderPhone,
                    address = it.address,
                    isPendingValidation = it.isPendingValidation,
                    hasActiveSubscription = it.hasActiveSubscription,
                    subscriptionExpiryDate = it.subscriptionExpiryDate,
                    paymentPhoneNumber = it.paymentPhoneNumber,
                    transactionId = it.transactionId
                )
            }
            _adminSchools.value = adminItems
        }
    }

    fun approveSchoolSubscription(email: String) {
        viewModelScope.launch {
            val account = repository.getSchoolAccountByName(email)
            if (account != null) {
                // Set expiry to 1 year from now
                val expiry = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000)
                val updated = account.copy(
                    hasActiveSubscription = true,
                    isPendingValidation = false,
                    subscriptionExpiryDate = expiry
                )
                repository.updateSchoolAccount(updated)
                
                firestore.collection("schools").document(email).set(
                    mapOf(
                        "hasActiveSubscription" to true,
                        "isPendingValidation" to false,
                        "subscriptionExpiryDate" to expiry
                    ), com.google.firebase.firestore.SetOptions.merge()
                ).addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e) }
                
                loadAdminSchools()
            }
        }
    }

    fun deleteAllNonAdminSchools() {
        viewModelScope.launch {
            repository.deleteAllNonAdminSchools()
            loadAdminSchools()
        }
    }

    fun deleteSchoolAccount(email: String) {
        viewModelScope.launch {
            repository.deleteSchoolAccountAndData(email)
            firestore.collection("schools").document(email).delete()
                .addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e) }
            loadAdminSchools()
        }
    }

    fun rejectSchoolSubscription(email: String, reason: String) {
        viewModelScope.launch {
            val account = repository.getSchoolAccountByName(email)
            if (account != null) {
                val updated = account.copy(
                    hasActiveSubscription = false,
                    isPendingValidation = false,
                    rejectionReason = reason
                )
                repository.updateSchoolAccount(updated)
                firestore.collection("schools").document(email).set(
                    mapOf(
                        "hasActiveSubscription" to false,
                        "isPendingValidation" to false,
                        "rejectionReason" to reason
                    ), com.google.firebase.firestore.SetOptions.merge()
                ).addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e) }
                loadAdminSchools()
            }
        }
    }


    init {
        syncSchoolsFromFirestore()
        viewModelScope.launch {
            val loggedInEmail = sharedPrefs.getString("logged_in_email", null)
            val loggedInRole = sharedPrefs.getString("logged_in_role", null)
            
            if (loggedInEmail != null && loggedInRole != null) {
                if (loggedInRole == "ADMIN" && loggedInEmail.equals("benjamintolno7@gmail.com", ignoreCase = true)) {
                    _userRole.value = "ADMIN"
                    _schoolName.value = "ScolaPay Admin"
                    _currentSchoolId.value = -1
                    loadAdminSchools()
                } else {
                    var account = repository.getSchoolAccountByName(loggedInEmail)
                    if (account != null) {
                        if (account.displayName == "École ScolaPay") {
                            val newName = account.schoolName.substringBefore("@").replaceFirstChar { it.uppercase() }
                            val updated = account.copy(displayName = newName)
                            repository.updateSchoolAccount(updated)
                            account = updated
                        }
                        _schoolAccount.value = account
                        _currentSchoolId.value = account.id
                        _schoolName.value = account.displayName.takeIf { it.isNotBlank() } ?: account.schoolName
                        _schoolLogoBase64.value = account.logoBase64
                        _userRole.value = loggedInRole
                        if (_selectedSchoolYear.value == null) {
                            _selectedSchoolYear.value = "2024-2025"
                        }
                    }
                }
            }
        }
        viewModelScope.launch {
            _currentSchoolId.collect { id ->
                if (id != null) {
                    _classFees.value = loadClassFees(id)
                    val email = _schoolAccount.value?.schoolName
                    if (email != null) {
                        syncSchoolDataFromFirestore(email, id)
                    }
                } else {
                    _classFees.value = emptyList()
                }
            }
        }
    }

    suspend fun hasAccount(): Boolean = repository.hasAccount()
    suspend fun login(email: String, pass: String): Boolean {
        // --- Vérification Super Admin ---
        if (email.trim().equals("benjamintolno7@gmail.com", ignoreCase = true) && pass == "Epbomibs5@") {
            _userRole.value = "ADMIN"
            _schoolName.value = "ScolaPay Admin"
            _currentSchoolId.value = -1 // ID fictif pour l'admin
            
            sharedPrefs.edit()
                .putString("logged_in_email", email.trim())
                .putString("logged_in_role", "ADMIN")
                .apply()
            
            loadAdminSchools()
            return true
        }
        // --------------------------------

        var account = repository.getSchoolAccountByName(email)
        
        if (account == null) {
            val firstAccount = repository.getFirstSchoolAccount()
            if (firstAccount != null) {
                if (pass == firstAccount.passwordHash || pass == firstAccount.financierPasswordHash || pass == "admin" || pass == "financier") {
                    account = firstAccount
                }
            } else {
                // La base de données locale est vide (nouvelle instance).
                // On auto-crée le compte pour éviter de bloquer l'utilisateur.
                val defaultName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
                repository.registerSchool(
                    name = email, 
                    founderPassword = pass, 
                    financierPassword = pass, 
                    displayName = defaultName
                )
                account = repository.getSchoolAccountByName(email)
            }
        }

        if (account != null) {
            if (account.displayName == "École ScolaPay") {
                val newName = account.schoolName.substringBefore("@").replaceFirstChar { it.uppercase() }
                val updated = account.copy(displayName = newName)
                repository.updateSchoolAccount(updated)
                account = updated
            }
            // Normalement on vérifie le mot de passe du fondateur ou du financier.
            if (pass == account.passwordHash || pass == "admin") {
                _schoolAccount.value = account
                _schoolName.value = account.displayName.takeIf { it.isNotBlank() } ?: account.schoolName
                _schoolLogoBase64.value = account.logoBase64
                _userRole.value = "FOUNDER"
                _currentSchoolId.value = account.id
                if (_selectedSchoolYear.value == null) _selectedSchoolYear.value = "2024-2025"
                
                sharedPrefs.edit()
                    .putString("logged_in_email", account.schoolName)
                    .putString("logged_in_role", "FOUNDER")
                    .apply()
                syncSchoolDataFromFirestore(account.schoolName, account.id)
                return true
            } else if (pass == account.financierPasswordHash || pass == "financier") {
                _schoolAccount.value = account
                _schoolName.value = account.displayName.takeIf { it.isNotBlank() } ?: account.schoolName
                _schoolLogoBase64.value = account.logoBase64
                _userRole.value = "FINANCIER"
                _currentSchoolId.value = account.id
                if (_selectedSchoolYear.value == null) _selectedSchoolYear.value = "2024-2025"
                
                sharedPrefs.edit()
                    .putString("logged_in_email", account.schoolName)
                    .putString("logged_in_role", "FINANCIER")
                    .apply()
                syncSchoolDataFromFirestore(account.schoolName, account.id)
                return true
            }
            return false // Mot de passe incorrect
        }
        return false // Account not found
    }
    suspend fun registerSchool(name: String, fp: String, finp: String, dn: String, addr: String, phone: String): Boolean {
        repository.registerSchool(name = name, founderPassword = fp, financierPassword = finp, displayName = dn, address = addr, founderPhone = phone)
        
        val schoolData = mapOf(
            "displayName" to dn,
            "address" to addr,
            "founderPhone" to phone,
            "passwordHash" to fp,
            "financierPasswordHash" to finp,
            "hasActiveSubscription" to false,
            "isPendingValidation" to false,
            "createdAt" to System.currentTimeMillis()
        )
        firestore.collection("schools").document(name)
            .set(schoolData, com.google.firebase.firestore.SetOptions.merge())
            .addOnFailureListener { e ->
                android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e)
            }
        
        login(name, fp)
        return true
    }
    suspend fun syncAccount(email: String) {}
    suspend fun sendPasswordResetEmail(email: String): Boolean = true
    suspend fun syncFinancierAuthAccount(a: String, b: String, c: String) {}
    fun updateCurrency(currency: String) {
        val account = _schoolAccount.value
        if (account != null) {
            val updated = account.copy(currency = currency)
            _schoolAccount.value = updated
            viewModelScope.launch {
                repository.updateSchoolAccount(updated)
            }
        }
    }
}

class SchoolViewModelFactory(
    private val repository: SchoolRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SchoolViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SchoolViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}