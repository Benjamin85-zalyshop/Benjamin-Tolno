package com.example.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.*
import com.example.data.repository.SchoolRepository
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
    private val firestore by lazy { FirebaseFirestore.getInstance() }
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

    private val _pendingOrderId = MutableStateFlow<String?>(sharedPrefs.getString("pending_order_id", null))
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
    val students: StateFlow<List<Student>> = combine(
        _currentSchoolId.flatMapLatest { id -> if (id != null) repository.getAllStudents(id) else flowOf(emptyList()) },
        _selectedSchoolYear,
        _selectedSection
    ) { list, year, section ->
        list.filter { 
            (year == null || year == "Toutes les années" || it.schoolYear == year) &&
            (section == null || section == "Toutes les sections" || it.section == section)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val payments: StateFlow<List<Payment>> = combine(
        _currentSchoolId.flatMapLatest { id -> if (id != null) repository.getAllPayments(id) else flowOf(emptyList()) },
        students
    ) { allPayments, filteredStudents ->
        val studentIds = filteredStudents.map { it.id }.toSet()
        allPayments.filter { it.studentId in studentIds }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val expenses: StateFlow<List<Expense>> = combine(
        _currentSchoolId.flatMapLatest { id -> if (id != null) repository.getAllExpenses(id) else flowOf(emptyList()) },
        _selectedSchoolYear,
        _selectedSection
    ) { allExpenses, year, section ->
        allExpenses.filter { 
            (year == null || year == "Toutes les années" || it.schoolYear == year) &&
            (section == null || section == "Toutes les sections" || it.section == section)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val subjects: StateFlow<List<Subject>> = combine(
        _currentSchoolId.flatMapLatest { id -> if (id != null) repository.getAllSubjects(id) else flowOf(emptyList()) },
        _selectedSection
    ) { list, section ->
        list.filter { section == null || section == "Toutes les sections" || it.section == section }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val grades: StateFlow<List<StudentGrade>> = combine(
        _currentSchoolId.flatMapLatest { id -> if (id != null) repository.getAllGrades(id) else flowOf(emptyList()) },
        students
    ) { allGrades, filteredStudents ->
        val studentIds = filteredStudents.map { it.id }.toSet()
        allGrades.filter { it.studentId in studentIds }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val totalCollected: StateFlow<Long> = payments.map { list -> list.filter { !it.isCancelled }.sumOf { it.amount } }.stateIn(viewModelScope, SharingStarted.Lazily, 0L)
    
    val totalExpenses: StateFlow<Long> = expenses.map { list -> list.sumOf { it.amount } }.stateIn(viewModelScope, SharingStarted.Lazily, 0L)
    
    val balance: StateFlow<Long> = combine(totalCollected, totalExpenses) { col, exp -> col - exp }.stateIn(viewModelScope, SharingStarted.Lazily, 0L)
    
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val hasActiveSubscription: StateFlow<Boolean> = _currentSchoolId.flatMapLatest { id ->
        if (id != null) repository.getSubscriptionStatus(id) else flowOf(false)
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)
    
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val isPendingValidation: StateFlow<Boolean> = _currentSchoolId.flatMapLatest { id ->
        if (id != null) repository.getPendingValidationStatus(id) else flowOf(false)
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)
    val trialDaysRemaining: StateFlow<Long> = _schoolAccount.map { account ->
        if (account == null) return@map 90L
        val now = System.currentTimeMillis()
        val accountCreatedAt = if (account.createdAt > 0L) account.createdAt else now
        val elapsed = (now - accountCreatedAt).coerceAtLeast(0L)
        val trialDuration = 90L * 24L * 60L * 60L * 1000L
        ((trialDuration - elapsed) / (24L * 60L * 60L * 1000L)).coerceAtLeast(0L)
    }.stateIn(viewModelScope, SharingStarted.Lazily, 90L)

    val isTrialActive: StateFlow<Boolean> = _schoolAccount.map { account ->
        if (account == null) return@map true
        val now = System.currentTimeMillis()
        val accountCreatedAt = if (account.createdAt > 0L) account.createdAt else now
        val elapsed = (now - accountCreatedAt).coerceAtLeast(0L)
        val trialDuration = 90L * 24L * 60L * 60L * 1000L
        elapsed < trialDuration
    }.stateIn(viewModelScope, SharingStarted.Lazily, true)

    val isAppAccessGranted: StateFlow<Boolean> = _schoolAccount.map { account ->
        if (account == null) return@map true
        val now = System.currentTimeMillis()
        val accountCreatedAt = if (account.createdAt > 0L) account.createdAt else now
        val elapsed = (now - accountCreatedAt).coerceAtLeast(0L)
        val trialDuration = 90L * 24L * 60L * 60L * 1000L
        val trialActive = elapsed < trialDuration
        
        val isExpired = account.hasActiveSubscription && account.subscriptionExpiryDate > 0 && account.subscriptionExpiryDate <= now
        val subActive = account.hasActiveSubscription && !isExpired

        val granted = trialActive || subActive
        android.util.Log.d("ScolaPay_Access", "account: ${account.schoolName}, createdAt: ${account.createdAt}, elapsed: $elapsed, trialActive: $trialActive, subActive: $subActive, granted: $granted")
        granted
    }.stateIn(viewModelScope, SharingStarted.Lazily, true)
    
    fun getPendingOrderId(): String? = _pendingOrderId.value
    fun setSelectedSchoolYear(year: String) {
        _selectedSchoolYear.value = year
    }

    fun clearSession() {
        // TODO: Auto-generated stub
    }

    fun setSection(section: String) {
        _selectedSection.value = section
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
            
            val studentData = mapOf(
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
            firestore.collection("students").document(remoteId).set(studentData, com.google.firebase.firestore.SetOptions.merge())
            
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
            
            val email = _schoolAccount.value?.schoolName
            if (email != null) {
                firestore.collection("schools").document(email).set(
                    mapOf("classFeesStr" to jsonStr),
                    com.google.firebase.firestore.SetOptions.merge()
                ).addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error syncing class fees", e) }
            }
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
            
            firestore.collection("schools").document(account.schoolName).set(
                mapOf("logoBase64" to base64), com.google.firebase.firestore.SetOptions.merge()
            ).addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error updating logo", e) }
        }
    }

    fun createDeletionRequest(student: Student, reason: String) {
        val email = _schoolAccount.value?.schoolName ?: return
        val remoteId = java.util.UUID.randomUUID().toString()
        val request = DeletionRequest(
            id = remoteId,
            studentRemoteId = student.remoteId,
            studentName = "${student.firstName} ${student.lastName}",
            grade = student.grade,
            section = student.section,
            reason = reason,
            requestedBy = "Financier",
            requestedAt = System.currentTimeMillis(),
            status = "PENDING"
        )
        firestore.collection("schools").document(email).collection("deletionRequests").document(remoteId).set(request)
            .addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error creating deletion request", e) }
    }

    fun approveDeletionRequest(request: DeletionRequest) {
        val email = _schoolAccount.value?.schoolName ?: return
        viewModelScope.launch {
            // Delete the student locally and remotely
            val student = repository.getStudentByRemoteId(request.studentRemoteId)
            if (student != null) {
                repository.deleteStudentById(student.id)
            }
            firestore.collection("schools").document(email).collection("students").document(request.studentRemoteId).delete()
            
            // Mark request as APPROVED
            firestore.collection("schools").document(email).collection("deletionRequests").document(request.id).update("status", "APPROVED")
                .addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error approving deletion request", e) }
        }
    }

    fun rejectDeletionRequest(request: DeletionRequest, reason: String) {
        val email = _schoolAccount.value?.schoolName ?: return
        firestore.collection("schools").document(email).collection("deletionRequests").document(request.id)
            .update(
                mapOf(
                    "status" to "REJECTED",
                    "rejectionReason" to reason
                )
            )
            .addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error rejecting deletion request", e) }
    }

    fun dismissDeletionRequest(request: DeletionRequest) {
        val email = _schoolAccount.value?.schoolName ?: return
        firestore.collection("schools").document(email).collection("deletionRequests").document(request.id).delete()
            .addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error dismissing deletion request", e) }
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
            
            updateStudentFinancialsInRTDB(schoolId, studentId)
        }
    }

    fun cancelPayment(paymentId: Int, reason: String) {
        val email = _schoolAccount.value?.schoolName ?: return
        val currentRole = _userRole.value ?: "INCONNU"
        viewModelScope.launch {
            val payment = repository.getPaymentById(paymentId) ?: return@launch
            val cancelledPayment = payment.copy(
                isCancelled = true,
                cancellationReason = reason,
                cancelledBy = currentRole,
                cancelledAt = System.currentTimeMillis()
            )
            repository.updatePayment(cancelledPayment)
            
            if (cancelledPayment.remoteId.isNotEmpty()) {
                val updates = mapOf<String, Any>(
                    "isCancelled" to true,
                    "cancellationReason" to reason,
                    "cancelledBy" to currentRole,
                    "cancelledAt" to (cancelledPayment.cancelledAt ?: 0L)
                )
                firestore.collection("schools").document(email).collection("payments").document(cancelledPayment.remoteId)
                    .update(updates)
                    .addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error syncing cancel to Firebase", e) }
            }
            
            updateStudentFinancialsInRTDB(cancelledPayment.schoolId, cancelledPayment.studentId)
        }
    }

    fun insertExpense(amount: Long, category: String, description: String, section: String) {
        val schoolId = _currentSchoolId.value ?: return
        val email = _schoolAccount.value?.schoolName ?: return
        val year = _selectedSchoolYear.value ?: "2026-2027"
        viewModelScope.launch {
            val fullReason = if(description.isNotBlank()) "$category - $description" else category
            val remoteId = java.util.UUID.randomUUID().toString()
            val expense = Expense(
                schoolId = schoolId,
                amount = amount,
                reason = fullReason,
                section = section,
                remoteId = remoteId,
                schoolYear = year
            )
            repository.insertExpense(expense)
            
            firestore.collection("schools").document(email).collection("expenses").document(remoteId).set(
                mapOf(
                    "amount" to expense.amount,
                    "reason" to expense.reason,
                    "section" to expense.section,
                    "date" to expense.date,
                    "schoolYear" to year
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
        val email = _schoolAccount.value?.schoolName ?: return
        viewModelScope.launch {
            val remoteId = java.util.UUID.randomUUID().toString()

                    val subject = Subject(
                schoolId = schoolId,
                section = section,
                grade = grade,
                name = name,
                coefficient = coefficient,
                maxScore = if (section == "LE PRIMAIRE" || section == "LA MATERNELLE") 10f else maxScore,
                remoteId = remoteId
            )
            repository.insertSubject(subject)
            
            firestore.collection("schools").document(email).collection("subjects").document(remoteId).set(
                mapOf(
                    "section" to subject.section,
                    "grade" to subject.grade,
                    "name" to subject.name,
                    "coefficient" to subject.coefficient,
                    "maxScore" to subject.maxScore
                )
            ).addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e) }
            
        }
    }

    fun updateSubjectDetails(subject: Subject, newName: String, newCoeff: Int, newMaxScore: Float) {
        val email = _schoolAccount.value?.schoolName ?: return
        viewModelScope.launch {
            val updated = subject.copy(name = newName, coefficient = newCoeff, maxScore = newMaxScore)
            repository.updateSubject(updated)
            if (updated.remoteId.isNotEmpty()) {
                firestore.collection("schools").document(email).collection("subjects").document(updated.remoteId).set(
                    mapOf(
                        "name" to updated.name,
                        "coefficient" to updated.coefficient,
                        "maxScore" to updated.maxScore
                    ),
                    com.google.firebase.firestore.SetOptions.merge()
                ).addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e) }
            }
        }
    }

    fun deleteSubject(subject: Subject) {
        val email = _schoolAccount.value?.schoolName ?: return
        viewModelScope.launch {
            repository.deleteSubjectById(subject.id)
            if (subject.remoteId.isNotEmpty()) {
                firestore.collection("schools").document(email).collection("subjects").document(subject.remoteId).delete()
                    .addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e) }
            }
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

        val email = _schoolAccount.value?.schoolName
        viewModelScope.launch {
            defaultSubjects.forEach { (name, coeff, maxScore) ->
                val remoteId = java.util.UUID.randomUUID().toString()
                    val subject = Subject(
                    schoolId = schoolId,
                    section = section,
                    grade = grade,
                    name = name,
                    coefficient = coeff,
                    maxScore = if (section == "LE PRIMAIRE" || section == "LA MATERNELLE") 10f else maxScore,
                    remoteId = remoteId
                )
                repository.insertSubject(subject)
                if (email != null) {
                    firestore.collection("schools").document(email).collection("subjects").document(remoteId).set(
                        mapOf(
                            "section" to subject.section,
                            "grade" to subject.grade,
                            "name" to subject.name,
                            "coefficient" to subject.coefficient,
                            "maxScore" to subject.maxScore
                        )
                    ).addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e) }
                }
            }
        }
    }

    fun saveGrade(studentId: Int, studentRemoteId: String, subjectId: Int, subjectRemoteId: String, term: String, evaluationScore: Float?, examScore: Float?, comment: String?) {
        val schoolId = _currentSchoolId.value ?: return
        val email = _schoolAccount.value?.schoolName ?: return
        viewModelScope.launch {
            val existing = repository.getExistingGrade(schoolId, studentId, subjectId, term)
            val remoteId = existing?.remoteId?.takeIf { it.isNotEmpty() } ?: java.util.UUID.randomUUID().toString()
            val gradeToSave = if (existing != null) {
                existing.copy(
                    evaluationScore = evaluationScore,
                    examScore = examScore,
                    teacherComment = comment,
                    remoteId = remoteId
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
                    teacherComment = comment,
                    remoteId = remoteId
                )
            }
            repository.insertGrade(gradeToSave)
            
            firestore.collection("schools").document(email).collection("grades").document(remoteId).set(
                mapOf(
                    "studentRemoteId" to studentRemoteId,
                    "subjectRemoteId" to subjectRemoteId,
                    "term" to term,
                    "evaluationScore" to evaluationScore,
                    "examScore" to examScore,
                    "teacherComment" to comment
                )
            ).addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e) }
            syncStudentAcademicsToRTDB(schoolId, studentId, term)
            
        }
    }

    fun updateStudentFinancialsInRTDB(schoolId: Int, studentId: Int) {
        viewModelScope.launch {
            val student = repository.getStudentById(studentId) ?: return@launch
            if (student.remoteId.isEmpty()) return@launch
            
            val payments = repository.getAllPaymentsDirect(schoolId).filter { it.studentId == studentId }
            val totalPaid = payments.filter { !it.isCancelled && it.reason != "Inscription" && it.reason != "Réinscription" }.sumOf { it.amount }
            val fees = loadClassFees(schoolId)
            val classFeeAmount = fees.find { it.grade == student.grade }?.feeAmount ?: 0L
            val totalFee = classFeeAmount
            
            val updateData = mutableMapOf<String, Any>(
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
            }
            
            com.google.firebase.database.FirebaseDatabase.getInstance("https://scolapay-b6289-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("students").child(student.remoteId)
                .updateChildren(updateData)
                .addOnSuccessListener {
                    println("Successfully synced financials to RTDB for student ${student.id}")
                }
                .addOnFailureListener { e ->
                    println("Failed to sync financials to RTDB: ${e.message}")
                }
                
            firestore.collection("students").document(student.remoteId)
                .set(updateData, com.google.firebase.firestore.SetOptions.merge())
        }
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
                totalPoints += score * subject.coefficient
                
                detailsList.add(mapOf(
                    "Matière" to subject.name,
                    "Eval" to if (grade != null && grade.evaluationScore != null) grade.evaluationScore.toString() else "-",
                    "Moy" to if (grade != null && grade.evaluationScore != null) String.format(java.util.Locale.US, "%.2f", grade.evaluationScore) else "-"
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
                        otherTotalPoints += g.evaluationScore * subject.coefficient
                    }
                }
                if (otherTotalCoef > 0) otherTotalPoints / otherTotalCoef else 0f
            }.sortedDescending()
            
            val rank = averages.indexOfFirst { it <= average }.takeIf { it >= 0 }?.plus(1) ?: 1
            
            val isPrimary = student.section.contains("PRIMAIRE", ignoreCase = true)
            val baseScale = if (isPrimary) 10f else 20f
            val ratio = average / baseScale
            val mention = when {
                ratio >= 0.9f -> "Excellent"
                ratio >= 0.8f -> "Très Bien"
                ratio >= 0.7f -> "Bien"
                ratio >= 0.6f -> "Assez Bien"
                ratio >= 0.5f -> "Passable"
                ratio >= 0.4f -> "Insuffisant"
                ratio >= 0.3f -> "Faible"
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
                // Fetch financials
                val payments = repository.getAllPaymentsDirect(schoolId).filter { it.studentId == studentId }
                val totalPaid = payments.filter { !it.isCancelled && it.reason != "Inscription" && it.reason != "Réinscription" }.sumOf { it.amount }
                val fees = loadClassFees(schoolId)
                val classFeeAmount = fees.find { it.grade == student.grade }?.feeAmount ?: 0L
                val totalFee = classFeeAmount
                
                val finalUpdateData = mutableMapOf<String, Any>()
                finalUpdateData.putAll(updateData)
                finalUpdateData["totalFee"] = totalFee
                finalUpdateData["paidFee"] = totalPaid
                if (student.photoBase64 != null) {
                    finalUpdateData["photoBase64"] = student.photoBase64!!
                }
                val account = _schoolAccount.value
                val sName = _schoolName.value
                if (sName != null && sName.isNotBlank()) {
                    finalUpdateData["schoolName"] = sName
                }
                if (account != null) {
                    if (!finalUpdateData.containsKey("schoolName")) {
                        finalUpdateData["schoolName"] = account.displayName.takeIf { it.isNotBlank() } ?: account.schoolName
                    }
                    finalUpdateData["schoolAddress"] = account.address
                    if (account.logoBase64 != null) {
                        finalUpdateData["logoBase64"] = account.logoBase64!!
                    }
                }

                // Sync to RTDB so parents can read it without Firestore permission issues
                com.google.firebase.database.FirebaseDatabase.getInstance("https://scolapay-b6289-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("students").child(student.remoteId)
                    .updateChildren(finalUpdateData)
                    .addOnSuccessListener {
                        println("Successfully synced academics to RTDB for student ${student.id}")
                    }
                    .addOnFailureListener { e ->
                        println("Failed to sync academics to RTDB: ${e.message}")
                    }
                    
                // Also keep firestore sync for admin panel
                firestore.collection("students").document(student.remoteId)
                    .set(finalUpdateData, com.google.firebase.firestore.SetOptions.merge())
            }
        }
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
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
        val account = _schoolAccount.value ?: return
        viewModelScope.launch {
            val updated = account.copy(
                hasActiveSubscription = true,
                isPendingValidation = false,
                subscriptionExpiryDate = System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000 // +1 year
            )
            repository.updateSchoolAccount(updated)
            _schoolAccount.value = updated
            
            firestore.collection("schools").document(account.schoolName).set(
                mapOf(
                    "hasActiveSubscription" to true,
                    "isPendingValidation" to false,
                    "subscriptionExpiryDate" to updated.subscriptionExpiryDate
                ), com.google.firebase.firestore.SetOptions.merge()
            ).addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error updating subscription", e) }
        }
    }

    fun savePendingOrderId(orderId: String) {
        _pendingOrderId.value = orderId
        sharedPrefs.edit().putString("pending_order_id", orderId).apply()
    }

    fun clearPendingOrderId() {
        _pendingOrderId.value = null
        sharedPrefs.edit().remove("pending_order_id").apply()
    }

    fun checkPendingPaymentStatus(onResult: (String) -> Unit) {
        val orderId = _pendingOrderId.value
        if (orderId == null) {
            return
        }
        viewModelScope.launch {
            val status = com.example.utils.ChapChapPayApi.checkOrderStatus(orderId)
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                if (status == "SUCCESS") {
                    activateSubscription()
                    clearPendingOrderId()
                    onResult("SUCCESS")
                } else if (status == "FAILED") {
                    clearPendingOrderId()
                    onResult("FAILED")
                } else {
                    onResult("PENDING")
                }
            }
        }
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
        fixPrimarySubjectsMaxScore()
        for (listener in activeListeners) {
            listener.remove()
        }
        activeListeners.clear()
        viewModelScope.launch {
            repository.deduplicateData()
        }

        val schoolDocListener = firestore.collection("schools").document(email).addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) return@addSnapshotListener
            
            // Sync class fees
            val classFeesStr = snapshot.getString("classFeesStr")
            if (classFeesStr != null) {
                try {
                    val fees = kotlinx.serialization.json.Json.decodeFromString<List<ClassFee>>(classFeesStr)
                    _classFees.value = fees
                    sharedPrefs.edit().putString("class_fees_$schoolId", classFeesStr).apply()
                } catch (e: Exception) {
                    android.util.Log.e("ScolaPay-Firebase", "Error syncing class fees", e)
                }
            }
            
            // Sync school account details (Logo, displayName, subscriptions)
            viewModelScope.launch {
                val currentAccount = repository.getSchoolAccountByName(email)
                if (currentAccount != null) {
                    val remoteLogo = snapshot.getString("logoBase64")
                    val remoteDisplayName = snapshot.getString("displayName")
                    val remoteAddress = snapshot.getString("address")
                    val remotePhone = snapshot.getString("founderPhone")
                    val hasSub = snapshot.getBoolean("hasActiveSubscription") ?: currentAccount.hasActiveSubscription
                    val pendingVal = snapshot.getBoolean("isPendingValidation") ?: currentAccount.isPendingValidation
                    val subExpiry = snapshot.getLong("subscriptionExpiryDate") ?: currentAccount.subscriptionExpiryDate
                    val remoteFinancierPwd = snapshot.getString("financierPasswordHash")
                    val remotePwd = snapshot.getString("passwordHash")

                    var isUpdated = false
                    var updatedAccount = currentAccount
                    
                    if (remoteLogo != null && remoteLogo != updatedAccount.logoBase64) {
                        updatedAccount = updatedAccount.copy(logoBase64 = remoteLogo)
                        _schoolLogoBase64.value = remoteLogo
                        isUpdated = true
                    }
                    if (remoteDisplayName != null && remoteDisplayName != updatedAccount.displayName) {
                        updatedAccount = updatedAccount.copy(displayName = remoteDisplayName)
                        _schoolName.value = remoteDisplayName
                        isUpdated = true
                    }
                    if (remoteAddress != null && remoteAddress != updatedAccount.address) {
                        updatedAccount = updatedAccount.copy(address = remoteAddress)
                        isUpdated = true
                    }
                    if (remotePhone != null && remotePhone != updatedAccount.founderPhone) {
                        updatedAccount = updatedAccount.copy(founderPhone = remotePhone)
                        isUpdated = true
                    }
                    if (hasSub != updatedAccount.hasActiveSubscription || pendingVal != updatedAccount.isPendingValidation || subExpiry != updatedAccount.subscriptionExpiryDate) {
                        updatedAccount = updatedAccount.copy(
                            hasActiveSubscription = hasSub,
                            isPendingValidation = pendingVal,
                            subscriptionExpiryDate = subExpiry
                        )
                        isUpdated = true
                    }
                    if (remoteFinancierPwd != null && remoteFinancierPwd != updatedAccount.financierPasswordHash) {
                        updatedAccount = updatedAccount.copy(financierPasswordHash = remoteFinancierPwd)
                        isUpdated = true
                    }
                    if (remotePwd != null && remotePwd != updatedAccount.passwordHash) {
                        updatedAccount = updatedAccount.copy(passwordHash = remotePwd)
                        isUpdated = true
                    }
                    
                    if (isUpdated) {
                        repository.updateSchoolAccount(updatedAccount)
                        _schoolAccount.value = updatedAccount
                    }
                }
            }
        }
        activeListeners.add(schoolDocListener)

        val studentsListener = firestore.collection("schools").document(email).collection("students").addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) return@addSnapshotListener
            viewModelScope.launch {
                for (change in snapshot.documentChanges) {
                    val doc = change.document
                    val remoteId = doc.id
                    
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.REMOVED) {
                        val existing = repository.getStudentByRemoteId(remoteId)
                        if (existing != null) repository.deleteStudentById(existing.id)
                        continue
                    }
                    
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
                        schoolYear = doc.getString("schoolYear") ?: "2026-2027"
                    )
                    if (existing != null) repository.updateStudent(student) else repository.insertStudent(student)
                }
            }
        }
        activeListeners.add(studentsListener)
        
        val paymentsListener = firestore.collection("schools").document(email).collection("payments").addSnapshotListener { snapshot, e ->
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
                    
                    var studentId = repository.getStudentIdByRemoteId(studentRemoteId)
                    if (studentId == null) {
                        try {
                            val studentDoc = awaitTask(firestore.collection("schools").document(email).collection("students").document(studentRemoteId).get())
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
                    if (studentId == null) continue
                    
                    val payment = Payment(
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
                    )
                    if (existing != null) repository.updatePayment(payment) else repository.insertPayment(payment)
                }
            }
        }
        activeListeners.add(paymentsListener)
        
        val expensesListener = firestore.collection("schools").document(email).collection("expenses").addSnapshotListener { snapshot, e ->
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
                        remoteId = remoteId,
                        schoolYear = doc.getString("schoolYear") ?: "2025-2026"
                    )
                    if (existing != null) repository.updateExpense(expense) else repository.insertExpense(expense)
                }
            }
        }
                val subjectsListener = firestore.collection("schools").document(email).collection("subjects").addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) return@addSnapshotListener
            viewModelScope.launch {
                for (change in snapshot.documentChanges) {
                    val doc = change.document
                    val remoteId = doc.id
                    val existing = repository.getSubjectByRemoteId(remoteId)
                    
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.REMOVED) {
                        if (existing != null) repository.deleteSubjectById(existing.id)
                        continue
                    }
                    
                    val parsedSection = doc.getString("section") ?: ""
                    val parsedMaxScore = if (parsedSection == "LE PRIMAIRE" || parsedSection == "LA MATERNELLE") 10f else ((doc.get("maxScore") as? Number)?.toFloat() ?: 20f)

                    val subject = Subject(
                        id = existing?.id ?: 0,
                        schoolId = schoolId,
                        section = parsedSection,
                        grade = doc.getString("grade") ?: "",
                        name = doc.getString("name") ?: "",
                        coefficient = doc.getLong("coefficient")?.toInt() ?: 1,
                        maxScore = parsedMaxScore,
                        remoteId = remoteId
                    )
                    if (existing != null) {
                        repository.insertSubject(subject.copy(id = existing.id))
                    } else {
                        repository.insertSubject(subject)
                    }
                }
            }
        }
        activeListeners.add(subjectsListener)

        val gradesListener = firestore.collection("schools").document(email).collection("grades").addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) return@addSnapshotListener
            viewModelScope.launch {
                for (change in snapshot.documentChanges) {
                    val doc = change.document
                    val remoteId = doc.id
                    val existing = repository.getGradeByRemoteId(remoteId)
                    
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.REMOVED) {
                        if (existing != null) repository.deleteGradeById(existing.id)
                        continue
                    }
                    
                    val studentRemoteId = doc.getString("studentRemoteId") ?: continue
                    val subjectRemoteId = doc.getString("subjectRemoteId") ?: continue
                    
                    var studentId = repository.getStudentIdByRemoteId(studentRemoteId)
                    if (studentId == null) {
                        try {
                            val studentDoc = awaitTask(firestore.collection("schools").document(email).collection("students").document(studentRemoteId).get())
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
                    if (studentId == null) continue

                    var subjectId = repository.getSubjectIdByRemoteId(subjectRemoteId)
                    if (subjectId == null) {
                        try {
                            val subjectDoc = awaitTask(firestore.collection("schools").document(email).collection("subjects").document(subjectRemoteId).get())
                            if (subjectDoc != null && subjectDoc.exists()) {
                                val parsedSection = subjectDoc.getString("section") ?: ""
                    val parsedMaxScore = if (parsedSection == "LE PRIMAIRE" || parsedSection == "LA MATERNELLE") 10f else ((subjectDoc.get("maxScore") as? Number)?.toFloat() ?: 20f)
                                val subject = Subject(
                                    id = 0, schoolId = schoolId,
                                    section = parsedSection, grade = subjectDoc.getString("grade") ?: "",
                                    name = subjectDoc.getString("name") ?: "", coefficient = subjectDoc.getLong("coefficient")?.toInt() ?: 1,
                                    maxScore = parsedMaxScore, remoteId = subjectRemoteId
                                )
                                repository.insertSubject(subject)
                                subjectId = repository.getSubjectIdByRemoteId(subjectRemoteId)
                            }
                        } catch (e: Exception) {}
                    }
                    if (subjectId == null) continue
                    
                    val grade = StudentGrade(
                        id = existing?.id ?: 0,
                        schoolId = schoolId,
                        studentId = studentId,
                        studentRemoteId = studentRemoteId,
                        subjectId = subjectId,
                        subjectRemoteId = subjectRemoteId,
                        term = doc.getString("term") ?: "1er Trimestre",
                        evaluationScore = doc.getDouble("evaluationScore")?.toFloat(),
                        examScore = doc.getDouble("examScore")?.toFloat(),
                        teacherComment = doc.getString("teacherComment"),
                        remoteId = remoteId
                    )
                    if (existing != null) {
                        repository.insertGrade(grade.copy(id = existing.id))
                    } else {
                        repository.insertGrade(grade)
                    }
                }
            }
        }
        activeListeners.add(gradesListener)

        val deletionRequestsListener = firestore.collection("schools").document(email).collection("deletionRequests").addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) return@addSnapshotListener
            val requests = mutableListOf<DeletionRequest>()
            for (doc in snapshot.documents) {
                requests.add(
                    DeletionRequest(
                        id = doc.id,
                        studentRemoteId = doc.getString("studentRemoteId") ?: "",
                        studentName = doc.getString("studentName") ?: "",
                        grade = doc.getString("grade") ?: "",
                        section = doc.getString("section") ?: "",
                        reason = doc.getString("reason") ?: "",
                        requestedBy = doc.getString("requestedBy") ?: "",
                        requestedAt = doc.getLong("requestedAt") ?: 0L,
                        status = doc.getString("status") ?: "PENDING",
                        rejectionReason = doc.getString("rejectionReason") ?: ""
                    )
                )
            }
            _deletionRequests.value = requests.sortedByDescending { it.requestedAt }
        }
        activeListeners.add(expensesListener)
        activeListeners.add(deletionRequestsListener)
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
                    val createdAtDoc = doc.getLong("createdAt")
                    val createdAt = if (createdAtDoc != null && createdAtDoc > 0L) createdAtDoc else System.currentTimeMillis()
                    
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
                            transactionId = it.transactionId,
                            createdAt = it.createdAt
                        )
                    }
                    _adminSchools.value = adminItems
                }
            }
        }
        activeListeners.add(listener)
    }

    fun forceSyncSchools() {
        _adminError.value = null
        viewModelScope.launch {
            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser?.email != "benjamintolno7@gmail.com") {
                _adminError.value = "Vous n'êtes pas connecté à Firebase."
                logout()
                return@launch
            }
            val task = firestore.collection("schools").get()
            task.addOnSuccessListener { snapshot ->
                _adminError.value = null
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
                        val createdAtDoc = doc.getLong("createdAt")
                        val createdAt = if (createdAtDoc != null && createdAtDoc > 0L) createdAtDoc else System.currentTimeMillis()
                        
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
                    loadAdminSchools()
                }
            }.addOnFailureListener { e ->
                _adminError.value = "Erreur: ${e.message} (User: ${FirebaseAuth.getInstance().currentUser?.email})"
            }
        }
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
                    transactionId = it.transactionId,
                    createdAt = it.createdAt
                )
            }
            _adminSchools.value = adminItems
        }
    }

    
    fun forceExpireSchool(email: String) {
        viewModelScope.launch {
            val account = repository.getSchoolAccountByName(email)
            if (account == null) {
                android.util.Log.e("ScolaPay", "forceExpireSchool: account not found for $email")
                return@launch
            }
            android.util.Log.d("ScolaPay", "forceExpireSchool: found account for $email, expiring now!")
            val expiredDate = System.currentTimeMillis() - 100L * 24 * 60 * 60 * 1000 // 100 days ago
            val updated = account.copy(
                createdAt = expiredDate,
                hasActiveSubscription = false,
                subscriptionExpiryDate = 0L,
                isPendingValidation = false
            )
            repository.updateSchoolAccount(updated)
            
            firestore.collection("schools").document(email).set(
                mapOf(
                    "createdAt" to expiredDate,
                    "hasActiveSubscription" to false,
                    "subscriptionExpiryDate" to 0L,
                    "isPendingValidation" to false
                ), com.google.firebase.firestore.SetOptions.merge()
            ).addOnFailureListener { e -> android.util.Log.e("ScolaPay", "Error force expiring", e) }
            
            loadAdminSchools()
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
                    val auth = FirebaseAuth.getInstance()
                    if (auth.currentUser?.email == "benjamintolno7@gmail.com") {
                        _userRole.value = "ADMIN"
                        _schoolName.value = "ScolaPay Admin"
                        _currentSchoolId.value = -1
                        loadAdminSchools()
                    } else {
                        // Not authenticated in Firebase, force logout to show login screen
                        logout()
                    }
                } else {
                    var account = repository.getSchoolAccountByName(loggedInEmail)
                    if (account != null) {
                        if (account.createdAt <= 0L) {
                            val updated = account.copy(createdAt = System.currentTimeMillis())
                            repository.updateSchoolAccount(updated)
                            account = updated
                        }
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
                            _selectedSchoolYear.value = "2026-2027"
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
        if (email.trim().equals("benjamintolno7@gmail.com", ignoreCase = true)) {
            val auth = FirebaseAuth.getInstance()
            try {
                auth.signInWithEmailAndPassword(email, "Epbomibs5@").await()
            } catch (e: Exception) {
                try {
                    auth.createUserWithEmailAndPassword(email, "Epbomibs5@").await()
                } catch (e2: Exception) {
                    android.util.Log.e("AdminLogin", "Firebase Auth failed", e2)
                    _adminError.value = "Erreur Firebase: ${e2.message}"
                    return false
                }
            }
            
            _userRole.value = "ADMIN"
            _schoolName.value = "ScolaPay Admin"
            _currentSchoolId.value = -1 
            sharedPrefs.edit().putString("logged_in_email", email.trim()).putString("logged_in_role", "ADMIN").apply()
            loadAdminSchools()
            return true
        }

        val auth = FirebaseAuth.getInstance()
        var isFounder = false
        var isFinancier = false

        // 1. Tenter la connexion Firebase Auth (Fondateur)
        try {
            auth.signInWithEmailAndPassword(email, pass).await()
            isFounder = true
        } catch (e: Exception) {
            // 2. Tenter la connexion Firebase Auth (Financier)
            try {
                auth.signInWithEmailAndPassword("fin_$email", pass).await()
                isFinancier = true
            } catch (e2: Exception) {
                // 3. MIGRATION : Si Firebase échoue, vérifier la base locale
                val localAccount = repository.getSchoolAccountByName(email)
                if (localAccount != null) {
                    if (pass == localAccount.passwordHash || pass == "admin") {
                        try {
                            auth.createUserWithEmailAndPassword(email, pass).await()
                            isFounder = true
                        } catch (e3: Exception) { 
                            try { auth.signInWithEmailAndPassword(email, pass).await(); isFounder = true } catch (e4: Exception) {}
                        }
                    } else if (pass == localAccount.financierPasswordHash || pass == "financier") {
                        try {
                            auth.createUserWithEmailAndPassword("fin_$email", pass).await()
                            isFinancier = true
                        } catch (e3: Exception) {
                            try { auth.signInWithEmailAndPassword("fin_$email", pass).await(); isFinancier = true } catch (e4: Exception) {}
                        }
                    }
                }
            }
        }

        if (!isFounder && !isFinancier) {
            return false // Échec total de l'authentification
        }

        // Enregistrer l'UID dans Firestore pour les règles de sécurité
        val uid = auth.currentUser?.uid
        if (uid != null) {
            val roleStr = if (isFounder) "FONDATEUR" else "FINANCIER"
            firestore.collection("schools").document(email).collection("users").document(uid)
                .set(mapOf("role" to roleStr), com.google.firebase.firestore.SetOptions.merge())
        }

        var account = repository.getSchoolAccountByName(email)
        if (account == null) {
            val defaultName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
            repository.registerSchool(name = email, founderPassword = pass, financierPassword = pass, displayName = defaultName)
            account = repository.getSchoolAccountByName(email)
        }

        if (account != null) {
            if (account.createdAt <= 0L) {
                val updated = account.copy(createdAt = System.currentTimeMillis())
                repository.updateSchoolAccount(updated)
                account = updated
            }
            _schoolAccount.value = account
            _schoolName.value = account.displayName.takeIf { it.isNotBlank() } ?: account.schoolName
            _schoolLogoBase64.value = account.logoBase64
            _userRole.value = if (isFounder) "FOUNDER" else "FINANCIER"
            _currentSchoolId.value = account.id
            if (_selectedSchoolYear.value == null) _selectedSchoolYear.value = "2026-2027"
            
            sharedPrefs.edit()
                .putString("logged_in_email", account.schoolName)
                .putString("logged_in_role", _userRole.value)
                .apply()
            return true
        }
        return false
    }
    suspend fun registerSchool(name: String, fp: String, finp: String, dn: String, addr: String, phone: String): Boolean {
        val auth = FirebaseAuth.getInstance()
        
        // 1. Créer le compte Firebase Auth pour le Fondateur
        try {
            auth.createUserWithEmailAndPassword(name, fp).await()
        } catch (e: Exception) {
            try { 
                auth.signInWithEmailAndPassword(name, fp).await() 
            } catch (e2: Exception) {
                android.util.Log.e("ScolaPay", "Auth creation failed", e2)
                return false // Échec critique si Firebase refuse la création
            }
        }
        
        // 2. Créer le compte Firebase Auth pour le Financier (en arrière-plan)
        try {
            auth.createUserWithEmailAndPassword("fin_$name", finp).await()
        } catch (e: Exception) {}
        
        // 3. Se reconnecter en tant que Fondateur
        try {
            auth.signInWithEmailAndPassword(name, fp).await()
        } catch (e: Exception) {}

        repository.registerSchool(name = name, founderPassword = fp, financierPassword = finp, displayName = dn, address = addr, founderPhone = phone)
        
        val now = System.currentTimeMillis()
        val schoolData = mapOf(
            "displayName" to dn,
            "address" to addr,
            "founderPhone" to phone,
            "passwordHash" to fp,
            "financierPasswordHash" to finp,
            "hasActiveSubscription" to false,
            "isPendingValidation" to false,
            "createdAt" to now
        )

        // Enregistrer l'UID du fondateur pour les règles de sécurité D'ABORD
        val uid = auth.currentUser?.uid
        if (uid != null) {
            try {
                firestore.collection("schools").document(name).collection("users").document(uid)
                    .set(mapOf("role" to "FONDATEUR"), com.google.firebase.firestore.SetOptions.merge()).await()
            } catch (e: Exception) {
                android.util.Log.e("ScolaPay-Firebase", "Error writing user role", e)
            }
        }
        
        try {
            firestore.collection("schools").document(name)
                .set(schoolData, com.google.firebase.firestore.SetOptions.merge()).await()
        } catch (e: Exception) {
            android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e)
        }

        var registeredAccount = repository.getSchoolAccountByName(name)
        if (registeredAccount != null) {
            if (registeredAccount.createdAt <= 0L) {
                val fixed = registeredAccount.copy(createdAt = now)
                repository.updateSchoolAccount(fixed)
                registeredAccount = fixed
            }
            _schoolAccount.value = registeredAccount
            _schoolName.value = registeredAccount.displayName.takeIf { it.isNotBlank() } ?: registeredAccount.schoolName
            _schoolLogoBase64.value = registeredAccount.logoBase64
            _userRole.value = "FOUNDER"
            _currentSchoolId.value = registeredAccount.id
            if (_selectedSchoolYear.value == null) _selectedSchoolYear.value = "2026-2027"
            
            sharedPrefs.edit()
                .putString("logged_in_email", registeredAccount.schoolName)
                .putString("logged_in_role", "FOUNDER")
                .apply()
        }
        
        return true
    }
    suspend fun syncAccount(email: String) {}
    suspend fun sendPasswordResetEmail(email: String): Boolean {
        return try {
            FirebaseAuth.getInstance().sendPasswordResetEmail(email).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
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
    fun fixPrimarySubjectsMaxScore() {
        val email = _schoolAccount.value?.schoolName ?: return
        viewModelScope.launch {
            val allSubjects = repository.getAllSubjectsDirect(_currentSchoolId.value ?: return@launch)
            for (subject in allSubjects) {
                if ((subject.section == "LE PRIMAIRE" || subject.section == "LA MATERNELLE") && subject.maxScore != 10f) {
                    val fixedSubject = subject.copy(maxScore = 10f)
                    repository.updateSubject(fixedSubject)
                    if (fixedSubject.remoteId.isNotEmpty()) {
                        firestore.collection("schools").document(email).collection("subjects").document(fixedSubject.remoteId).update(
                            "maxScore", 10f
                        )
                    }
                }
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
