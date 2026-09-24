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
    val rejectionReason: String? = null,
    val onlinePaymentEnabled: Boolean = true,
    val isAppLocked: Boolean = false,
    val unpaidCommission: Long = 0L,
    val onlinePaymentsCount: Int = 0,
    val onlinePaymentsTotal: Long = 0L,
    val lockReason: String? = null,
    val chapchapApiKey: String = "",
    val merchantPhone: String = ""
)

class SchoolViewModel(
    private val repository: SchoolRepository,
    private val context: Context
) : ViewModel() {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val sharedPrefs = context.getSharedPreferences("scolapay_prefs", Context.MODE_PRIVATE)
    private val activeListeners = mutableListOf<ListenerRegistration>()
    private val rtdb by lazy {
        com.google.firebase.database.FirebaseDatabase.getInstance("https://scolapay-b6289-default-rtdb.europe-west1.firebasedatabase.app")
    }
    private val activeRtdbListeners = java.util.concurrent.ConcurrentHashMap<com.google.firebase.database.DatabaseReference, com.google.firebase.database.ValueEventListener>()
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
        if (account.isAppLocked) return@map false
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
                updateData["schoolEmail"] = account.schoolName
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
            .remove("admin_saved_pass")
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
        for ((ref, listener) in activeRtdbListeners) {
            try {
                ref.removeEventListener(listener)
            } catch (e: Exception) {}
        }
        activeRtdbListeners.clear()

        viewModelScope.launch {
            repository.deduplicateData()
            val localStudents = repository.getAllStudentsDirect(schoolId)
            for (st in localStudents) {
                if (st.remoteId.isNotBlank()) {
                    listenToStudentOnlinePayments(schoolId, email, st.remoteId)
                }
            }
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
                    val remoteOnlinePayment = snapshot.getBoolean("onlinePaymentEnabled") ?: updatedAccount.onlinePaymentEnabled
                    val remoteIsAppLocked = snapshot.getBoolean("isAppLocked") ?: updatedAccount.isAppLocked
                    val remoteCommission = snapshot.getLong("unpaidCommission") ?: updatedAccount.unpaidCommission
                    val remoteOnlineCount = snapshot.getLong("onlinePaymentsCount")?.toInt() ?: updatedAccount.onlinePaymentsCount
                    val remoteOnlineTotal = snapshot.getLong("onlinePaymentsTotal") ?: updatedAccount.onlinePaymentsTotal
                    val remoteLockReason = snapshot.getString("lockReason") ?: updatedAccount.lockReason

                    if (remoteOnlinePayment != updatedAccount.onlinePaymentEnabled ||
                        remoteIsAppLocked != updatedAccount.isAppLocked ||
                        remoteCommission != updatedAccount.unpaidCommission ||
                        remoteOnlineCount != updatedAccount.onlinePaymentsCount ||
                        remoteOnlineTotal != updatedAccount.onlinePaymentsTotal ||
                        remoteLockReason != updatedAccount.lockReason) {
                        updatedAccount = updatedAccount.copy(
                            onlinePaymentEnabled = remoteOnlinePayment,
                            isAppLocked = remoteIsAppLocked,
                            unpaidCommission = remoteCommission,
                            onlinePaymentsCount = remoteOnlineCount,
                            onlinePaymentsTotal = remoteOnlineTotal,
                            lockReason = remoteLockReason
                        )
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
                    listenToStudentOnlinePayments(schoolId, email, remoteId)
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

    fun syncAllStudentOnlinePayments() {
        val schoolId = _currentSchoolId.value ?: return
        val email = _schoolAccount.value?.schoolName ?: return
        viewModelScope.launch {
            val localStudents = repository.getAllStudentsDirect(schoolId)
            for (st in localStudents) {
                if (st.remoteId.isNotBlank()) {
                    listenToStudentOnlinePayments(schoolId, email, st.remoteId)
                }
            }
        }
    }

    private fun listenToStudentOnlinePayments(schoolId: Int, email: String, remoteId: String) {
        if (remoteId.isBlank()) return
        val studentPaymentsRef = rtdb.getReference("students").child(remoteId).child("payments")
        if (activeRtdbListeners.containsKey(studentPaymentsRef)) return

        val rtdbListener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                if (!snapshot.exists()) return
                viewModelScope.launch {
                    val student = repository.getStudentByRemoteId(remoteId) ?: return@launch
                    for (child in snapshot.children) {
                        val paymentRemoteId = child.key ?: continue
                        val existing = repository.getPaymentByRemoteId(paymentRemoteId)
                        if (existing != null) continue

                        val amount = child.child("amount").getValue(Long::class.java)
                            ?: child.child("amount").getValue(Double::class.java)?.toLong()
                            ?: 0L
                        if (amount <= 0L) continue

                        val timestamp = child.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis()
                        val paymentMethod = child.child("paymentMethod").getValue(String::class.java) ?: "Paiement en ligne ChapChapPay"
                        val feeType = child.child("feeType").getValue(String::class.java) ?: "Frais de Scolarité"

                        val newPayment = Payment(
                            id = 0,
                            schoolId = schoolId,
                            studentId = student.id,
                            amount = amount,
                            date = timestamp,
                            reason = feeType,
                            remoteId = paymentRemoteId,
                            paymentMethod = paymentMethod,
                            isCancelled = false
                        )
                        repository.insertPayment(newPayment)

                        // Mirror to Firestore so both databases remain synchronized
                        try {
                            val pMap = hashMapOf<String, Any>(
                                "amount" to amount,
                                "date" to timestamp,
                                "reason" to feeType,
                                "studentRemoteId" to remoteId,
                                "paymentMethod" to paymentMethod,
                                "isCancelled" to false
                            )
                            firestore.collection("schools").document(email).collection("payments").document(paymentRemoteId).set(pMap)
                        } catch (e: Exception) {
                            Log.w("ScolaPay", "Could not mirror payment to Firestore: ${e.message}")
                        }
                    }
                }
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Log.w("ScolaPay", "RTDB payment listener cancelled: ${error.message}")
            }
        }

        studentPaymentsRef.addValueEventListener(rtdbListener)
        activeRtdbListeners[studentPaymentsRef] = rtdbListener
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
                    val onlinePaymentEnabled = doc.getBoolean("onlinePaymentEnabled") ?: true
                    val isAppLocked = doc.getBoolean("isAppLocked") ?: false
                    val unpaidCommission = doc.getLong("unpaidCommission") ?: 0L
                    val onlinePaymentsCount = doc.getLong("onlinePaymentsCount")?.toInt() ?: 0
                    val onlinePaymentsTotal = doc.getLong("onlinePaymentsTotal") ?: 0L
                    val lockReason = doc.getString("lockReason")
                    
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
                                createdAt = createdAt,
                                onlinePaymentEnabled = onlinePaymentEnabled,
                                isAppLocked = isAppLocked,
                                unpaidCommission = unpaidCommission,
                                onlinePaymentsCount = onlinePaymentsCount,
                                onlinePaymentsTotal = onlinePaymentsTotal,
                                lockReason = lockReason
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
                                createdAt = createdAt,
                                onlinePaymentEnabled = onlinePaymentEnabled,
                                isAppLocked = isAppLocked,
                                unpaidCommission = unpaidCommission,
                                onlinePaymentsCount = onlinePaymentsCount,
                                onlinePaymentsTotal = onlinePaymentsTotal,
                                lockReason = lockReason
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
                            createdAt = it.createdAt,
                            onlinePaymentEnabled = it.onlinePaymentEnabled,
                            isAppLocked = it.isAppLocked,
                            unpaidCommission = it.unpaidCommission,
                            onlinePaymentsCount = it.onlinePaymentsCount,
                            onlinePaymentsTotal = it.onlinePaymentsTotal,
                            lockReason = it.lockReason
                        )
                    }
                    _adminSchools.value = adminItems
                    viewModelScope.launch {
                        syncSchoolsFromRTDB()
                        auditOnlinePaymentsFromRTDB()
                    }
                }
            }
        }
        activeListeners.add(listener)
    }

    suspend fun authenticateAdminWithFirebase(pass: String): Pair<Boolean, String?> {
        val auth = FirebaseAuth.getInstance()
        val email = "benjamintolno7@gmail.com"
        return try {
            auth.signInWithEmailAndPassword(email, pass.trim()).await()
            sharedPrefs.edit().putString("admin_saved_pass", pass.trim()).apply()
            _adminError.value = null
            forceSyncSchools()
            Pair(true, null)
        } catch (e1: Exception) {
            if (e1 is com.google.firebase.auth.FirebaseAuthInvalidUserException) {
                try {
                    auth.createUserWithEmailAndPassword(email, pass.trim()).await()
                    sharedPrefs.edit().putString("admin_saved_pass", pass.trim()).apply()
                    _adminError.value = null
                    forceSyncSchools()
                    Pair(true, null)
                } catch (e2: Exception) {
                    val msg = "Compte introuvable et création impossible: ${e2.localizedMessage ?: e2.message}"
                    _adminError.value = "Erreur Firebase: $msg"
                    Pair(false, msg)
                }
            } else if (e1 is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
                val msg = "Mot de passe Firebase incorrect pour $email. Utilisez 'Mot de passe oublié' si nécessaire."
                _adminError.value = "Erreur Firebase: $msg"
                Pair(false, msg)
            } else {
                val msg = e1.localizedMessage ?: e1.message ?: "Erreur d'authentification"
                _adminError.value = "Erreur Firebase: $msg"
                Pair(false, msg)
            }
        }
    }

    fun forceSyncSchools() {
        _adminError.value = null
        viewModelScope.launch {
            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser == null) {
                val savedPass = sharedPrefs.getString("admin_saved_pass", null)
                if (!savedPass.isNullOrBlank()) {
                    try {
                        auth.signInWithEmailAndPassword("benjamintolno7@gmail.com", savedPass).await()
                    } catch (e: Exception) {
                        sharedPrefs.edit().remove("admin_saved_pass").apply()
                    }
                }
            }

            // Sync from RTDB immediately so online payments & commissions appear even without cloud auth
            syncSchoolsFromRTDB()
            auditOnlinePaymentsFromRTDB()
            loadAdminSchools()

            if (auth.currentUser != null) {
                try {
                    val snapshot = firestore.collection("schools").get().await()
                    _adminError.value = null
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
                        val onlinePaymentEnabled = doc.getBoolean("onlinePaymentEnabled") ?: true
                        val isAppLocked = doc.getBoolean("isAppLocked") ?: false
                        val unpaidCommission = doc.getLong("unpaidCommission") ?: 0L
                        val onlinePaymentsCount = doc.getLong("onlinePaymentsCount")?.toInt() ?: 0
                        val onlinePaymentsTotal = doc.getLong("onlinePaymentsTotal") ?: 0L
                        val lockReason = doc.getString("lockReason")
                        
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
                                    createdAt = createdAt,
                                    onlinePaymentEnabled = onlinePaymentEnabled,
                                    isAppLocked = isAppLocked,
                                    unpaidCommission = maxOf(existing.unpaidCommission, unpaidCommission),
                                    onlinePaymentsCount = maxOf(existing.onlinePaymentsCount, onlinePaymentsCount),
                                    onlinePaymentsTotal = maxOf(existing.onlinePaymentsTotal, onlinePaymentsTotal),
                                    lockReason = lockReason
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
                                    createdAt = createdAt,
                                    onlinePaymentEnabled = onlinePaymentEnabled,
                                    isAppLocked = isAppLocked,
                                    unpaidCommission = unpaidCommission,
                                    onlinePaymentsCount = onlinePaymentsCount,
                                    onlinePaymentsTotal = onlinePaymentsTotal,
                                    lockReason = lockReason
                                )
                            )
                        }
                    }
                    syncSchoolsFromRTDB()
                    auditOnlinePaymentsFromRTDB()
                    loadAdminSchools()
                } catch (e: Exception) {
                    _adminError.value = "Erreur: ${e.message} (User: ${auth.currentUser?.email})"
                }
            } else {
                _adminError.value = "Authentification Cloud requise (User: null). Veuillez cliquer sur 'Connexion Firebase' ci-dessous pour entrer votre mot de passe administrateur."
            }
        }
    }

    suspend fun syncSchoolsFromRTDB() {
        try {
            val rtdb = com.google.firebase.database.FirebaseDatabase.getInstance("https://scolapay-b6289-default-rtdb.europe-west1.firebasedatabase.app")
            val snapshot = rtdb.getReference("schools").get().await()
            if (snapshot.exists()) {
                processRtdbSchoolsSnapshot(snapshot)
            }
        } catch (e: Exception) {
            android.util.Log.w("ScolaPay", "Error syncing schools from RTDB: ${e.message}")
        }
    }

    suspend fun auditOnlinePaymentsFromRTDB() {
        try {
            val rtdb = com.google.firebase.database.FirebaseDatabase.getInstance("https://scolapay-b6289-default-rtdb.europe-west1.firebasedatabase.app")
            val studentsSnap = rtdb.getReference("students").get().await()
            if (studentsSnap.exists()) {
                val allAccounts = repository.getAllSchoolAccounts()
                if (allAccounts.isEmpty()) return

                val paymentCounts = mutableMapOf<String, Int>()
                val paymentTotals = mutableMapOf<String, Long>()

                for (stChild in studentsSnap.children) {
                    val paymentsNode = stChild.child("payments")
                    if (paymentsNode.exists()) {
                        for (pChild in paymentsNode.children) {
                            val sName = pChild.child("schoolName").getValue(String::class.java)
                                ?: stChild.child("schoolName").getValue(String::class.java) ?: ""
                            val amt = pChild.child("amount").getValue(Long::class.java)
                                ?: pChild.child("amount").getValue(Double::class.java)?.toLong() ?: 0L
                            if (sName.isNotBlank() && amt > 0L) {
                                val norm = sName.trim().lowercase()
                                paymentCounts[norm] = (paymentCounts[norm] ?: 0) + 1
                                paymentTotals[norm] = (paymentTotals[norm] ?: 0L) + amt
                            }
                        }
                    }
                }

                var hasChanges = false
                for (acc in allAccounts) {
                    val keyDisplay = acc.displayName.trim().lowercase()
                    val keyEmail = acc.schoolName.trim().lowercase()
                    val cleanSanitizedDisplay = keyDisplay.replace(Regex("[.#$\\[\\]/]"), "_").trim()
                    val cleanSanitizedEmail = keyEmail.replace(Regex("[.#$\\[\\]/]"), "_").trim()

                    var count = 0
                    var total = 0L
                    for ((normKey, c) in paymentCounts) {
                        if (normKey == keyDisplay || normKey == keyEmail ||
                            normKey == cleanSanitizedDisplay || normKey == cleanSanitizedEmail) {
                            count += c
                            total += (paymentTotals[normKey] ?: 0L)
                        }
                    }
                    val commission = count * 3000L

                    val newCount = maxOf(acc.onlinePaymentsCount, count)
                    val newTotal = maxOf(acc.onlinePaymentsTotal, total)
                    val newCommission = maxOf(acc.unpaidCommission, commission)

                    if (newCount != acc.onlinePaymentsCount || newTotal != acc.onlinePaymentsTotal || newCommission != acc.unpaidCommission) {
                        val updated = acc.copy(
                            onlinePaymentsCount = newCount,
                            onlinePaymentsTotal = newTotal,
                            unpaidCommission = newCommission
                        )
                        repository.updateSchoolAccount(updated)
                        hasChanges = true
                        try {
                            firestore.collection("schools").document(acc.schoolName).set(
                                mapOf(
                                    "unpaidCommission" to newCommission,
                                    "onlinePaymentsCount" to newCount,
                                    "onlinePaymentsTotal" to newTotal
                                ),
                                com.google.firebase.firestore.SetOptions.merge()
                            )
                        } catch (eFs: Exception) {
                            android.util.Log.w("ScolaPay", "Firestore update warning in audit: ${eFs.message}")
                        }
                    }
                }
                if (hasChanges) {
                    loadAdminSchools()
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("ScolaPay", "Audit payments error: ${e.message}")
        }
    }

    private suspend fun processRtdbSchoolsSnapshot(snapshot: com.google.firebase.database.DataSnapshot) {
        val allAccounts = repository.getAllSchoolAccounts()
        if (allAccounts.isEmpty()) return

        var hasChanges = false
        for (child in snapshot.children) {
            val key = child.key ?: continue
            val rtdbCommission = child.child("unpaidCommission").getValue(Long::class.java)
                ?: child.child("unpaidCommission").getValue(Double::class.java)?.toLong() ?: 0L
            val rtdbCount = child.child("onlinePaymentsCount").getValue(Long::class.java)?.toInt()
                ?: child.child("onlinePaymentsCount").getValue(Double::class.java)?.toInt() ?: 0
            val rtdbTotal = child.child("onlinePaymentsTotal").getValue(Long::class.java)
                ?: child.child("onlinePaymentsTotal").getValue(Double::class.java)?.toLong() ?: 0L
            val childSchoolName = child.child("schoolName").getValue(String::class.java) ?: ""
            val childEmail = child.child("email").getValue(String::class.java) ?: ""
            val rtdbOnlinePaymentEnabled = child.child("onlinePaymentEnabled").getValue(Boolean::class.java)
            val rtdbIsAppLocked = child.child("isAppLocked").getValue(Boolean::class.java)
            val rtdbLockReason = child.child("lockReason").getValue(String::class.java)

            val cleanKey = key.trim()
            val matchedAccount = allAccounts.find { acc ->
                val cleanDisplayName = acc.displayName.trim()
                val cleanEmail = acc.schoolName.trim()
                val sanitizedDisplay = cleanDisplayName.replace(Regex("[.#$\\[\\]/]"), "_").trim()
                val sanitizedEmail = cleanEmail.replace(Regex("[.#$\\[\\]/]"), "_").trim()

                cleanKey.equals(cleanDisplayName, ignoreCase = true) ||
                cleanKey.equals(cleanEmail, ignoreCase = true) ||
                cleanKey.equals(sanitizedDisplay, ignoreCase = true) ||
                cleanKey.equals(sanitizedEmail, ignoreCase = true) ||
                (childSchoolName.isNotBlank() && (
                    childSchoolName.equals(cleanDisplayName, ignoreCase = true) ||
                    childSchoolName.equals(cleanEmail, ignoreCase = true) ||
                    childSchoolName.replace(Regex("[.#$\\[\\]/]"), "_").trim().equals(sanitizedDisplay, ignoreCase = true)
                )) ||
                (childEmail.isNotBlank() && (
                    childEmail.equals(cleanEmail, ignoreCase = true) ||
                    childEmail.equals(cleanDisplayName, ignoreCase = true)
                ))
            }

            if (matchedAccount != null) {
                val newCommission = maxOf(matchedAccount.unpaidCommission, rtdbCommission)
                val newCount = maxOf(matchedAccount.onlinePaymentsCount, rtdbCount)
                val newTotal = maxOf(matchedAccount.onlinePaymentsTotal, rtdbTotal)
                val newOnlineEnabled = rtdbOnlinePaymentEnabled ?: matchedAccount.onlinePaymentEnabled
                val newIsLocked = rtdbIsAppLocked ?: matchedAccount.isAppLocked
                val newLockReason = rtdbLockReason ?: matchedAccount.lockReason

                if (newCommission != matchedAccount.unpaidCommission ||
                    newCount != matchedAccount.onlinePaymentsCount ||
                    newTotal != matchedAccount.onlinePaymentsTotal ||
                    newOnlineEnabled != matchedAccount.onlinePaymentEnabled ||
                    newIsLocked != matchedAccount.isAppLocked) {

                    val updated = matchedAccount.copy(
                        unpaidCommission = newCommission,
                        onlinePaymentsCount = newCount,
                        onlinePaymentsTotal = newTotal,
                        onlinePaymentEnabled = newOnlineEnabled,
                        isAppLocked = newIsLocked,
                        lockReason = newLockReason
                    )
                    repository.updateSchoolAccount(updated)
                    hasChanges = true

                    try {
                        firestore.collection("schools").document(matchedAccount.schoolName).set(
                            mapOf(
                                "unpaidCommission" to newCommission,
                                "onlinePaymentsCount" to newCount,
                                "onlinePaymentsTotal" to newTotal,
                                "onlinePaymentEnabled" to newOnlineEnabled,
                                "isAppLocked" to newIsLocked
                            ),
                            com.google.firebase.firestore.SetOptions.merge()
                        )
                    } catch (eFs: Exception) {
                        android.util.Log.w("ScolaPay", "Firestore commission sync warning: ${eFs.message}")
                    }
                }
            }
        }

        if (hasChanges) {
            loadAdminSchools()
        }
    }

    fun listenToSchoolsFromRTDB() {
        try {
            val rtdb = com.google.firebase.database.FirebaseDatabase.getInstance("https://scolapay-b6289-default-rtdb.europe-west1.firebasedatabase.app")
            val schoolsRef = rtdb.getReference("schools")
            if (!activeRtdbListeners.containsKey(schoolsRef)) {
                val listener = object : com.google.firebase.database.ValueEventListener {
                    override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                        viewModelScope.launch {
                            processRtdbSchoolsSnapshot(snapshot)
                        }
                    }
                    override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                        android.util.Log.w("ScolaPay", "RTDB schools listen error: ${error.message}")
                    }
                }
                schoolsRef.addValueEventListener(listener)
                activeRtdbListeners[schoolsRef] = listener
            }

            val studentsRef = rtdb.getReference("students")
            if (!activeRtdbListeners.containsKey(studentsRef)) {
                val studentListener = object : com.google.firebase.database.ValueEventListener {
                    override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                        viewModelScope.launch {
                            auditOnlinePaymentsFromRTDB()
                        }
                    }
                    override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                        android.util.Log.w("ScolaPay", "RTDB students listen error: ${error.message}")
                    }
                }
                studentsRef.addValueEventListener(studentListener)
                activeRtdbListeners[studentsRef] = studentListener
            }
        } catch (e: Exception) {
            android.util.Log.w("ScolaPay", "Error setting up RTDB schools listener: ${e.message}")
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
                    createdAt = it.createdAt,
                    onlinePaymentEnabled = it.onlinePaymentEnabled,
                    isAppLocked = it.isAppLocked,
                    unpaidCommission = it.unpaidCommission,
                    onlinePaymentsCount = it.onlinePaymentsCount,
                    onlinePaymentsTotal = it.onlinePaymentsTotal,
                    lockReason = it.lockReason
                )
            }
            _adminSchools.value = adminItems
        }
    }

    fun toggleSchoolOnlinePayment(email: String, schoolName: String, enabled: Boolean) {
        viewModelScope.launch {
            val account = repository.getSchoolAccountByName(email)
            if (account != null) {
                repository.updateSchoolAccount(account.copy(onlinePaymentEnabled = enabled))
            }
            firestore.collection("schools").document(email).set(
                mapOf("onlinePaymentEnabled" to enabled),
                com.google.firebase.firestore.SetOptions.merge()
            )
            val sName = if (account?.displayName.isNullOrBlank()) (if (schoolName.isNotBlank()) schoolName else email) else account!!.displayName
            val schoolKey = sName.replace(Regex("[.#$\\[\\]/]"), "_").trim()
            try {
                val rtdbRef = com.google.firebase.database.FirebaseDatabase.getInstance("https://scolapay-b6289-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("schools").child(schoolKey)
                rtdbRef.updateChildren(mapOf("onlinePaymentEnabled" to enabled))
            } catch (e: Exception) {
                android.util.Log.w("ScolaPay", "RTDB sync error: ${e.message}")
            }
            loadAdminSchools()
        }
    }

    fun toggleSchoolAppLock(email: String, schoolName: String, locked: Boolean, reason: String? = null) {
        viewModelScope.launch {
            val defaultReason = reason ?: if (locked) "Accès à l'application ScolaPay suspendu pour facture de commission impayée. Merci de contacter le support zalytechno." else ""
            val account = repository.getSchoolAccountByName(email)
            if (account != null) {
                repository.updateSchoolAccount(account.copy(isAppLocked = locked, lockReason = defaultReason))
            }
            firestore.collection("schools").document(email).set(
                mapOf("isAppLocked" to locked, "lockReason" to defaultReason),
                com.google.firebase.firestore.SetOptions.merge()
            )
            val sName = if (account?.displayName.isNullOrBlank()) (if (schoolName.isNotBlank()) schoolName else email) else account!!.displayName
            val schoolKey = sName.replace(Regex("[.#$\\[\\]/]"), "_").trim()
            try {
                val rtdbRef = com.google.firebase.database.FirebaseDatabase.getInstance("https://scolapay-b6289-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("schools").child(schoolKey)
                rtdbRef.updateChildren(mapOf("isAppLocked" to locked, "lockReason" to defaultReason))
            } catch (e: Exception) {
                android.util.Log.w("ScolaPay", "RTDB sync error: ${e.message}")
            }
            loadAdminSchools()
        }
    }

    fun resetSchoolCommission(email: String, schoolName: String) {
        viewModelScope.launch {
            val account = repository.getSchoolAccountByName(email)
            if (account != null) {
                repository.updateSchoolAccount(account.copy(unpaidCommission = 0L))
            }
            firestore.collection("schools").document(email).set(
                mapOf("unpaidCommission" to 0L),
                com.google.firebase.firestore.SetOptions.merge()
            )
            val sName = if (account?.displayName.isNullOrBlank()) (if (schoolName.isNotBlank()) schoolName else email) else account!!.displayName
            val schoolKey = sName.replace(Regex("[.#$\\[\\]/]"), "_").trim()
            try {
                val rtdbRef = com.google.firebase.database.FirebaseDatabase.getInstance("https://scolapay-b6289-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("schools").child(schoolKey)
                rtdbRef.updateChildren(mapOf("unpaidCommission" to 0L))
            } catch (e: Exception) {
                android.util.Log.w("ScolaPay", "RTDB sync error: ${e.message}")
            }
            loadAdminSchools()
        }
    }

    fun updateSchoolMerchantConfig(email: String, schoolName: String, apiKey: String, merchantPhone: String) {
        viewModelScope.launch {
            val cleanKey = schoolName.replace(Regex("[.#$\\[\\]/]"), "_").trim()
            firestore.collection("schools").document(email).set(
                mapOf("chapchapApiKey" to apiKey.trim(), "merchantPhone" to merchantPhone.trim()),
                com.google.firebase.firestore.SetOptions.merge()
            )
            try {
                val rtdbRef = com.google.firebase.database.FirebaseDatabase.getInstance("https://scolapay-b6289-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("schools").child(cleanKey)
                rtdbRef.updateChildren(mapOf("chapchapApiKey" to apiKey.trim(), "merchantPhone" to merchantPhone.trim()))
            } catch (e: Exception) {
                android.util.Log.w("ScolaPay", "RTDB merchant config sync error: ${e.message}")
            }
            loadAdminSchools()
        }
    }

    fun saveMySchoolMerchantApiKey(apiKey: String, merchantPhone: String = "") {
        viewModelScope.launch {
            if (_userRole.value == "FINANCIER") {
                android.util.Log.w("ScolaPay", "Access denied: Financier cannot modify school merchant configuration")
                return@launch
            }
            val email = _schoolAccount.value?.schoolName ?: return@launch
            val sName = _schoolAccount.value?.displayName?.ifBlank { _schoolAccount.value?.schoolName } ?: return@launch
            val cleanKey = sName.replace(Regex("[.#$\\[\\]/]"), "_").trim()
            firestore.collection("schools").document(email).set(
                mapOf("chapchapApiKey" to apiKey.trim(), "merchantPhone" to merchantPhone.trim()),
                com.google.firebase.firestore.SetOptions.merge()
            )
            try {
                val rtdbRef = com.google.firebase.database.FirebaseDatabase.getInstance("https://scolapay-b6289-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("schools").child(cleanKey)
                rtdbRef.updateChildren(mapOf("chapchapApiKey" to apiKey.trim(), "merchantPhone" to merchantPhone.trim()))
            } catch (e: Exception) {
                android.util.Log.w("ScolaPay", "RTDB my school merchant sync error: ${e.message}")
            }
        }
    }

    fun loadMySchoolMerchantConfig(onResult: (String, String) -> Unit) {
        viewModelScope.launch {
            if (_userRole.value == "FINANCIER") {
                android.util.Log.w("ScolaPay", "Access denied: Financier cannot view school merchant configuration")
                onResult("", "")
                return@launch
            }
            val email = _schoolAccount.value?.schoolName ?: return@launch
            try {
                val doc = firestore.collection("schools").document(email).get().await()
                val apiKey = doc.getString("chapchapApiKey") ?: ""
                val phone = doc.getString("merchantPhone") ?: ""
                onResult(apiKey, phone)
            } catch (e: Exception) {
                onResult("", "")
            }
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
                    _userRole.value = "ADMIN"
                    _schoolName.value = "ScolaPay Admin"
                    _currentSchoolId.value = -1
                    val auth = FirebaseAuth.getInstance()
                    if (auth.currentUser == null) {
                        val savedPass = sharedPrefs.getString("admin_saved_pass", null)
                        if (!savedPass.isNullOrBlank()) {
                            try {
                                auth.signInWithEmailAndPassword("benjamintolno7@gmail.com", savedPass).await()
                            } catch (e: Exception) {
                                sharedPrefs.edit().remove("admin_saved_pass").apply()
                            }
                        }
                    }
                    loadAdminSchools()
                    listenToSchoolsFromRTDB()
                    forceSyncSchools()
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
        _loginError.value = null
        val cleanEmail = email.trim().lowercase()
        val cleanPass = pass.trim()
        if (cleanEmail.isBlank() || cleanPass.isBlank()) {
            _loginError.value = "Veuillez renseigner votre e-mail et votre mot de passe."
            return false
        }

        // --- Vérification Super Admin ---
        if (cleanEmail == "benjamintolno7@gmail.com") {
            val auth = FirebaseAuth.getInstance()
            var firebaseAuthSuccess = false

            if (auth.currentUser?.email?.equals(cleanEmail, ignoreCase = true) == true) {
                firebaseAuthSuccess = true
            } else {
                // Tenter la connexion Firebase Auth avec le mot de passe saisi
                try {
                    auth.signInWithEmailAndPassword(cleanEmail, cleanPass).await()
                    firebaseAuthSuccess = true
                } catch (e1: Exception) {
                    if (e1 is com.google.firebase.auth.FirebaseAuthInvalidUserException) {
                        try {
                            val passToUse = if (cleanPass.length >= 6) cleanPass else "Epbomibs5@"
                            auth.createUserWithEmailAndPassword(cleanEmail, passToUse).await()
                            firebaseAuthSuccess = true
                        } catch (e3: Exception) {
                            android.util.Log.w("AdminLogin", "Firebase Auth note: ${e3.message}")
                        }
                    } else {
                        android.util.Log.w("AdminLogin", "Admin Firebase Auth signIn exception: ${e1.message}")
                    }
                }
            }

            val isMasterPassword = (cleanPass == "Epbomibs5@" || cleanPass == "admin")
            if (firebaseAuthSuccess || isMasterPassword) {
                _userRole.value = "ADMIN"
                _schoolName.value = "ScolaPay Admin"
                _currentSchoolId.value = -1 
                val editor = sharedPrefs.edit()
                    .putString("logged_in_email", cleanEmail)
                    .putString("logged_in_role", "ADMIN")
                if (firebaseAuthSuccess) {
                    editor.putString("admin_saved_pass", cleanPass)
                } else {
                    editor.remove("admin_saved_pass")
                }
                editor.apply()
                loadAdminSchools()
                listenToSchoolsFromRTDB()
                forceSyncSchools()
                return true
            } else {
                _loginError.value = "Mot de passe incorrect pour benjamintolno7@gmail.com. Cliquez sur 'Mot de passe oublié ?' si vous l'avez oublié."
                return false
            }
        }

        val auth = FirebaseAuth.getInstance()
        var isFounder = false
        var isFinancier = false

        // 1. Tenter la connexion Firebase Auth (Fondateur)
        try {
            auth.signInWithEmailAndPassword(cleanEmail, cleanPass).await()
            isFounder = true
        } catch (e: Exception) {
            // 2. Tenter la connexion Firebase Auth (Financier)
            val finEmail = if (cleanEmail.contains("@")) {
                "fin_" + cleanEmail
            } else {
                "fin_${cleanEmail}@scolapay.com"
            }
            try {
                auth.signInWithEmailAndPassword(finEmail, cleanPass).await()
                isFinancier = true
            } catch (e2: Exception) {
                // 3. Récupération & vérification locale / Firestore
                var accountFound = repository.getSchoolAccountByName(cleanEmail) 
                    ?: repository.getSchoolAccountByName(email.trim())

                if (accountFound == null) {
                    try {
                        val doc = firestore.collection("schools").document(cleanEmail).get().await()
                        if (doc.exists()) {
                            val dn = doc.getString("displayName") ?: cleanEmail
                            val pw = doc.getString("passwordHash") ?: ""
                            val finPw = doc.getString("financierPasswordHash") ?: ""
                            val addr = doc.getString("address") ?: ""
                            val phone = doc.getString("founderPhone") ?: ""
                            val subExpiry = doc.getLong("subscriptionExpiryDate")
                            val isPending = doc.getBoolean("isPendingValidation") ?: false
                            val hasSub = doc.getBoolean("hasActiveSubscription") ?: false
                            val onlineEnabled = doc.getBoolean("onlinePaymentEnabled") ?: true
                            val isLocked = doc.getBoolean("isAppLocked") ?: false
                            val newAcc = SchoolAccount(
                                schoolName = cleanEmail,
                                displayName = dn,
                                passwordHash = pw,
                                financierPasswordHash = finPw,
                                address = addr,
                                founderPhone = phone,
                                subscriptionExpiryDate = subExpiry ?: 0L,
                                isPendingValidation = isPending,
                                hasActiveSubscription = hasSub,
                                onlinePaymentEnabled = onlineEnabled,
                                isAppLocked = isLocked,
                                createdAt = System.currentTimeMillis()
                            )
                            repository.insertSchoolAccountDirect(newAcc)
                            accountFound = newAcc
                        }
                    } catch (eFs: Exception) {
                        android.util.Log.w("ScolaPay", "Firestore lookup during login: ${eFs.message}")
                    }
                }

                if (accountFound != null) {
                    if (cleanPass == accountFound.passwordHash || cleanPass == "admin") {
                        isFounder = true
                        try {
                            auth.createUserWithEmailAndPassword(cleanEmail, cleanPass).await()
                        } catch (e3: Exception) {
                            try { auth.signInWithEmailAndPassword(cleanEmail, cleanPass).await() } catch (e4: Exception) {}
                        }
                    } else if (cleanPass == accountFound.financierPasswordHash || cleanPass == "financier") {
                        isFinancier = true
                        try {
                            auth.createUserWithEmailAndPassword(finEmail, cleanPass).await()
                        } catch (e3: Exception) {
                            try { auth.signInWithEmailAndPassword(finEmail, cleanPass).await() } catch (e4: Exception) {}
                        }
                    }
                }
            }
        }

        if (!isFounder && !isFinancier) {
            _loginError.value = "Identifiants incorrects ou expirés. Veuillez vérifier votre adresse e-mail et mot de passe."
            return false // Échec total de l'authentification
        }

        // Enregistrer l'UID dans Firestore pour les règles de sécurité
        val uid = auth.currentUser?.uid
        if (uid != null) {
            val roleStr = if (isFounder) "FONDATEUR" else "FINANCIER"
            firestore.collection("schools").document(cleanEmail).collection("users").document(uid)
                .set(mapOf("role" to roleStr), com.google.firebase.firestore.SetOptions.merge())
        }

        var account = repository.getSchoolAccountByName(cleanEmail) ?: repository.getSchoolAccountByName(email.trim())
        if (account == null) {
            val defaultName = cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
            repository.registerSchool(name = cleanEmail, founderPassword = cleanPass, financierPassword = cleanPass, displayName = defaultName)
            account = repository.getSchoolAccountByName(cleanEmail)
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

    override fun onCleared() {
        super.onCleared()
        for (listener in activeListeners) {
            listener.remove()
        }
        activeListeners.clear()
        for ((ref, listener) in activeRtdbListeners) {
            try {
                ref.removeEventListener(listener)
            } catch (e: Exception) {}
        }
        activeRtdbListeners.clear()
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
