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

        // Blocage si l'accès est suspendu / verrouillé par l'administrateur
        if (account.isAppLocked) {
            return@map false
        }

        val now = System.currentTimeMillis()
        val accountCreatedAt = if (account.createdAt > 0L) account.createdAt else now
        val elapsed = (now - accountCreatedAt).coerceAtLeast(0L)
        val trialDuration = 90L * 24L * 60L * 60L * 1000L
        val trialActive = elapsed < trialDuration
        
        val isExpired = account.hasActiveSubscription && account.subscriptionExpiryDate > 0 && account.subscriptionExpiryDate <= now
        val subActive = account.hasActiveSubscription && !isExpired

        val granted = trialActive || subActive
        android.util.Log.d("ScolaPay_Access", "account: ${account.schoolName}, unpaidComm: ${account.unpaidCommission}, isLocked: ${account.isAppLocked}, granted: $granted")
        granted
    }.stateIn(viewModelScope, SharingStarted.Lazily, true)
    
    fun getPendingOrderId(): String? {
        if (_pendingOrderId.value.isNullOrBlank()) {
            val saved = sharedPrefs.getString("pending_order_id", null) ?: sharedPrefs.getString("last_comm_order_id", null)
            if (!saved.isNullOrBlank()) {
                _pendingOrderId.value = saved
            }
        }
        return _pendingOrderId.value
    }
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
            
            val auth = FirebaseAuth.getInstance()
            val currentAuthEmail = auth.currentUser?.email
            val docKey = currentAuthEmail?.takeIf { it.isNotBlank() } ?: account.schoolName
            
            firestore.collection("schools").document(docKey).set(
                mapOf("logoBase64" to base64), com.google.firebase.firestore.SetOptions.merge()
            ).addOnFailureListener { e -> android.util.Log.w("ScolaPay-Firebase", "Notice updating logo: ${e.message}") }
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

    fun unlockSchoolDefinitively() {
        val account = _schoolAccount.value ?: return
        viewModelScope.launch {
            val oneYearLater = System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000L
            val updated = account.copy(
                isAppLocked = false,
                lockReason = "",
                unpaidCommission = 0L,
                hasActiveSubscription = true,
                isPendingValidation = false,
                subscriptionExpiryDate = maxOf(account.subscriptionExpiryDate, oneYearLater)
            )
            repository.updateSchoolAccount(updated)
            _schoolAccount.value = updated
            clearPendingOrderId()

            val auth = FirebaseAuth.getInstance()
            val currentAuthEmail = auth.currentUser?.email
            val candidateKeys = mutableSetOf<String>()
            if (account.schoolName.isNotBlank()) candidateKeys.add(account.schoolName.trim())
            if (account.displayName.isNotBlank()) candidateKeys.add(account.displayName.trim())
            if (!currentAuthEmail.isNullOrBlank()) candidateKeys.add(currentAuthEmail.trim())

            val payload = mapOf(
                "isAppLocked" to false,
                "lockReason" to "",
                "unpaidCommission" to 0L,
                "hasActiveSubscription" to true,
                "isPendingValidation" to false,
                "subscriptionExpiryDate" to updated.subscriptionExpiryDate
            )

            for (k in candidateKeys) {
                try {
                    firestore.collection("schools").document(k).set(
                        payload,
                        com.google.firebase.firestore.SetOptions.merge()
                    ).addOnFailureListener { e -> android.util.Log.w("ScolaPay", "Notice updating unlock: ${e.message}") }
                } catch (e: Exception) {}
            }

            val rtdb = com.google.firebase.database.FirebaseDatabase.getInstance("https://scolapay-b6289-default-rtdb.europe-west1.firebasedatabase.app")
            for (k in candidateKeys) {
                val cleanK = k.replace(Regex("[.#$\\[\\]/]"), "_").trim()
                if (cleanK.isBlank()) continue
                try {
                    rtdb.getReference("schools").child(cleanK).updateChildren(payload)
                } catch (eRtdb: Exception) {}
            }
        }
    }

    fun activateSubscription() {
        unlockSchoolDefinitively()
    }

    fun savePendingOrderId(orderId: String, operationId: String? = null) {
        _pendingOrderId.value = orderId
        val editor = sharedPrefs.edit().putString("pending_order_id", orderId)
        if (!operationId.isNullOrBlank()) {
            editor.putString("pending_operation_id", operationId)
        }
        if (orderId.startsWith("COMM_")) {
            editor.putString("last_comm_order_id", orderId)
        }
        editor.apply()
    }

    fun clearPendingOrderId() {
        _pendingOrderId.value = null
        sharedPrefs.edit()
            .remove("pending_order_id")
            .remove("pending_operation_id")
            .remove("last_comm_order_id")
            .apply()
    }

    fun checkPendingPaymentStatus(customOrderId: String? = null, onResult: (String) -> Unit) {
        val orderId = customOrderId?.trim()?.takeIf { it.isNotBlank() }
            ?: _pendingOrderId.value
            ?: sharedPrefs.getString("pending_order_id", null)
            ?: sharedPrefs.getString("pending_operation_id", null)
        if (orderId.isNullOrBlank()) {
            onResult("NO_ORDER")
            return
        }
        val opId = sharedPrefs.getString("pending_operation_id", null)
        viewModelScope.launch {
            val status = com.example.utils.ChapChapPayApi.checkMultipleIds(orderId, opId)
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                if (status == "SUCCESS") {
                    unlockSchoolDefinitively()
                    clearPendingOrderId()
                    onResult("SUCCESS")
                } else if (status == "FAILED") {
                    // Do NOT clear pending order ID on failure so user can retry or enter manual ID
                    onResult("FAILED")
                } else {
                    onResult("PENDING")
                }
            }
        }
    }

    fun checkPendingCommissionPaymentStatus(customOrderId: String? = null, onResult: (String) -> Unit) {
        val orderId = customOrderId?.trim()?.takeIf { it.isNotBlank() }
            ?: _pendingOrderId.value
            ?: sharedPrefs.getString("pending_order_id", null)
            ?: sharedPrefs.getString("pending_operation_id", null)
            ?: sharedPrefs.getString("last_comm_order_id", null)
        if (orderId.isNullOrBlank()) {
            onResult("NO_ORDER")
            return
        }
        val opId = sharedPrefs.getString("pending_operation_id", null)
        viewModelScope.launch {
            val status = com.example.utils.ChapChapPayApi.checkMultipleIds(orderId, opId)
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                if (status == "SUCCESS") {
                    unlockMySchoolAfterCommission()
                    clearPendingOrderId()
                    onResult("SUCCESS")
                } else if (status == "FAILED") {
                    // Do NOT clear pending order ID on temporary fail
                    onResult("FAILED")
                } else {
                    onResult("PENDING")
                }
            }
        }
    }

    private fun normalizeSyncKey(s: String): String {
        return try {
            java.text.Normalizer.normalize(s.trim(), java.text.Normalizer.Form.NFD)
                .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
                .replace(Regex("[^a-zA-Z0-9]"), "")
                .lowercase()
        } catch (e: Exception) {
            s.trim().lowercase()
        }
    }

    fun isCommissionSettled(key: String): Boolean {
        val norm = normalizeSyncKey(key)
        return sharedPrefs.getBoolean("comm_settled_$norm", false)
    }

    fun setCommissionSettled(key: String, settled: Boolean, settledCount: Int = 0) {
        val norm = normalizeSyncKey(key)
        sharedPrefs.edit()
            .putBoolean("comm_settled_$norm", settled)
            .putInt("comm_settled_count_$norm", settledCount)
            .apply()
    }

    fun getCommissionSettledCount(key: String): Int {
        val norm = normalizeSyncKey(key)
        return sharedPrefs.getInt("comm_settled_count_$norm", 0)
    }

    fun unlockMySchoolAfterCommission() {
        val account = _schoolAccount.value ?: return
        viewModelScope.launch {
            val oneYearLater = System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000L
            val updated = account.copy(
                isAppLocked = false,
                lockReason = "",
                unpaidCommission = 0L,
                hasActiveSubscription = true,
                isPendingValidation = false,
                subscriptionExpiryDate = maxOf(account.subscriptionExpiryDate, oneYearLater)
            )
            repository.updateSchoolAccount(updated)
            _schoolAccount.value = updated
            clearPendingOrderId()

            val curOnlineCount = account.onlinePaymentsCount
            if (account.schoolName.isNotBlank()) setCommissionSettled(account.schoolName, true, curOnlineCount)
            if (account.displayName.isNotBlank()) setCommissionSettled(account.displayName, true, curOnlineCount)

            val cleanEmail = account.schoolName.lowercase().trim()
            val fullPayload = mapOf(
                "isAppLocked" to false,
                "lockReason" to "",
                "unpaidCommission" to 0L,
                "hasActiveSubscription" to true,
                "isPendingValidation" to false,
                "subscriptionExpiryDate" to updated.subscriptionExpiryDate
            )

            if (cleanEmail.contains("@") && !cleanEmail.startsWith("fin_")) {
                try {
                    firestore.collection("schools").document(cleanEmail).set(
                        fullPayload,
                        com.google.firebase.firestore.SetOptions.merge()
                    )
                } catch (e: Exception) {}
            }

            try {
                val rtdb = com.google.firebase.database.FirebaseDatabase.getInstance("https://scolapay-b6289-default-rtdb.europe-west1.firebasedatabase.app")
                val cleanEmailKey = cleanEmail.replace(Regex("[.#$\\[\\]/]"), "_").trim()
                if (cleanEmailKey.isNotBlank()) {
                    rtdb.getReference("schools").child(cleanEmailKey).updateChildren(fullPayload)
                }
                val dName = account.displayName.trim()
                if (dName.isNotBlank() && !dName.equals(cleanEmail, ignoreCase = true) && !dName.startsWith("fin_")) {
                    val dKey = dName.replace(Regex("[.#$\\[\\]/]"), "_").trim()
                    if (dKey.isNotBlank()) {
                        rtdb.getReference("schools").child(dKey).updateChildren(fullPayload)
                    }
                }
            } catch (e: Exception) {}
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
                
                val auth = FirebaseAuth.getInstance()
                val currentAuthEmail = auth.currentUser?.email
                val docKey = currentAuthEmail?.takeIf { it.isNotBlank() } ?: account.schoolName
                
                try {
                    firestore.collection("schools").document(docKey).set(
                        mapOf(
                            "isPendingValidation" to true,
                            "paymentPhoneNumber" to phoneNumber,
                            "transactionId" to transactionId
                        ), com.google.firebase.firestore.SetOptions.merge()
                    ).addOnFailureListener { e -> android.util.Log.w("ScolaPay-Firebase", "Notice syncing to Firebase: ${e.message}") }
                } catch (e: Exception) {}
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
                
                val auth = FirebaseAuth.getInstance()
                val currentAuthEmail = auth.currentUser?.email
                val docKey = currentAuthEmail?.takeIf { it.isNotBlank() } ?: account.schoolName
                
                try {
                    firestore.collection("schools").document(docKey).set(
                        mapOf("financierPasswordHash" to newPassword),
                        com.google.firebase.firestore.SetOptions.merge()
                    ).addOnFailureListener { e -> android.util.Log.w("ScolaPay-Firebase", "Notice syncing to Firebase: ${e.message}") }
                } catch (e: Exception) {}
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
                    val isSettled = isCommissionSettled(updatedAccount.schoolName) || isCommissionSettled(updatedAccount.displayName)
                    val effectiveHasSub = hasSub
                    val effectivePending = pendingVal
                    val effectiveSubExpiry = subExpiry

                    if (effectiveHasSub != updatedAccount.hasActiveSubscription || effectivePending != updatedAccount.isPendingValidation || effectiveSubExpiry != updatedAccount.subscriptionExpiryDate) {
                        updatedAccount = updatedAccount.copy(
                            hasActiveSubscription = effectiveHasSub,
                            isPendingValidation = effectivePending,
                            subscriptionExpiryDate = effectiveSubExpiry
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

                    val effectiveIsAppLocked = if (isSettled) false else remoteIsAppLocked
                    val effectiveCommission = if (isSettled) 0L else remoteCommission
                    val effectiveLockReason = if (isSettled) "" else remoteLockReason

                    if (remoteOnlinePayment != updatedAccount.onlinePaymentEnabled ||
                        effectiveIsAppLocked != updatedAccount.isAppLocked ||
                        effectiveCommission != updatedAccount.unpaidCommission ||
                        remoteOnlineCount != updatedAccount.onlinePaymentsCount ||
                        remoteOnlineTotal != updatedAccount.onlinePaymentsTotal ||
                        effectiveLockReason != updatedAccount.lockReason) {
                        updatedAccount = updatedAccount.copy(
                            onlinePaymentEnabled = remoteOnlinePayment,
                            isAppLocked = effectiveIsAppLocked,
                            unpaidCommission = effectiveCommission,
                            onlinePaymentsCount = remoteOnlineCount,
                            onlinePaymentsTotal = remoteOnlineTotal,
                            lockReason = effectiveLockReason
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
                    
                    if (!email.contains("@")) continue

                    val existing = repository.getSchoolAccountByName(email)
                        ?: repository.getAllSchoolAccounts().find { it.schoolName.equals(email, ignoreCase = true) || (it.displayName.isNotBlank() && it.displayName.equals(displayName, ignoreCase = true)) }
                    val isLocallySettled = isCommissionSettled(email) || isCommissionSettled(displayName) || (existing != null && (isCommissionSettled(existing.schoolName) || isCommissionSettled(existing.displayName)))
                    val settledCount = maxOf(getCommissionSettledCount(email), getCommissionSettledCount(displayName), existing?.let { getCommissionSettledCount(it.schoolName) } ?: 0)
                    val effectiveIsLocked = isAppLocked
                    val effectiveLock = lockReason ?: ""
                    val parsedCommFromReason = Regex("""(\d+)\s*GNF""").find(effectiveLock)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
                    val calcComm = (onlinePaymentsCount * 3000L)
                    val effectiveComm = if (effectiveIsLocked) {
                        maxOf(unpaidCommission, calcComm, parsedCommFromReason)
                    } else {
                        if (isLocallySettled && settledCount >= onlinePaymentsCount) 0L else maxOf(unpaidCommission, calcComm)
                    }
                    val effectiveSub = hasActiveSubscription
                    val effectiveSubExpiry = subscriptionExpiryDate
                    val effectivePending = isPendingValidation

                    if (existing != null) {
                        val updatedSchool = existing.copy(
                            displayName = displayName,
                            hasActiveSubscription = effectiveSub,
                            isPendingValidation = effectivePending,
                            founderPhone = founderPhone,
                            financierPasswordHash = if (financierPasswordHash.isNotBlank()) financierPasswordHash else existing.financierPasswordHash,
                            passwordHash = if (passwordHash.isNotBlank()) passwordHash else existing.passwordHash,
                            logoBase64 = logoBase64 ?: existing.logoBase64,
                            address = address,
                            paymentPhoneNumber = paymentPhoneNumber ?: existing.paymentPhoneNumber,
                            transactionId = transactionId ?: existing.transactionId,
                            rejectionReason = rejectionReason,
                            subscriptionExpiryDate = effectiveSubExpiry,
                            createdAt = createdAt,
                            onlinePaymentEnabled = onlinePaymentEnabled,
                            isAppLocked = effectiveIsLocked,
                            unpaidCommission = effectiveComm,
                            onlinePaymentsCount = maxOf(existing.onlinePaymentsCount, onlinePaymentsCount),
                            onlinePaymentsTotal = maxOf(existing.onlinePaymentsTotal, onlinePaymentsTotal),
                            lockReason = effectiveLock
                        )
                        repository.updateSchoolAccount(updatedSchool)
                        val curAcc = _schoolAccount.value
                        if (curAcc != null && (curAcc.schoolName.equals(email, ignoreCase = true) || curAcc.displayName.equals(displayName, ignoreCase = true) || curAcc.id == existing.id)) {
                            _schoolAccount.value = updatedSchool
                        }
                    } else if (email.contains("@")) {
                        repository.insertSchoolAccountDirect(
                            SchoolAccount(
                                schoolName = email,
                                displayName = displayName,
                                hasActiveSubscription = effectiveSub,
                                isPendingValidation = effectivePending,
                                founderPhone = founderPhone,
                                financierPasswordHash = financierPasswordHash,
                                passwordHash = passwordHash,
                                logoBase64 = logoBase64,
                                address = address,
                                paymentPhoneNumber = paymentPhoneNumber,
                                transactionId = transactionId,
                                rejectionReason = rejectionReason,
                                subscriptionExpiryDate = effectiveSubExpiry,
                                createdAt = createdAt,
                                onlinePaymentEnabled = onlinePaymentEnabled,
                                isAppLocked = effectiveIsLocked,
                                unpaidCommission = effectiveComm,
                                onlinePaymentsCount = onlinePaymentsCount,
                                onlinePaymentsTotal = onlinePaymentsTotal,
                                lockReason = effectiveLock
                            )
                        )
                    }
                }
                if (_userRole.value == "ADMIN") {
                    cleanupDuplicateSchoolAccounts()
                    loadAdminSchools()
                }
            }
        }
        activeListeners.add(listener)
    }

    suspend fun ensureAdminFirebaseAuth(enteredPass: String? = null): Boolean {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser?.email.equals("benjamintolno7@gmail.com", ignoreCase = true)) {
            return true
        }

        // Si un mot de passe explicite est saisi, vérifier UNIQUEMENT ce mot de passe
        if (!enteredPass.isNullOrBlank()) {
            return try {
                auth.signInWithEmailAndPassword("benjamintolno7@gmail.com", enteredPass).await()
                sharedPrefs.edit().putString("admin_saved_pass", enteredPass).apply()
                true
            } catch (e: Exception) {
                false
            }
        }

        // En tâche de fond (sans mot de passe fourni), réutiliser le mot de passe sauvegardé
        val savedPass = sharedPrefs.getString("admin_saved_pass", null)
        val passwordsToTry = mutableListOf<String>()
        if (!savedPass.isNullOrBlank()) {
            passwordsToTry.add(savedPass)
        }
        if (!passwordsToTry.contains("Epbomibs5@")) {
            passwordsToTry.add("Epbomibs5@")
        }

        for (p in passwordsToTry) {
            try {
                auth.signInWithEmailAndPassword("benjamintolno7@gmail.com", p).await()
                sharedPrefs.edit().putString("admin_saved_pass", p).apply()
                return true
            } catch (e: Exception) {}
        }

        return auth.currentUser?.email.equals("benjamintolno7@gmail.com", ignoreCase = true)
    }

    suspend fun authenticateAdminWithFirebase(pass: String): Pair<Boolean, String?> {
        val success = ensureAdminFirebaseAuth(pass)
        return if (success) {
            _adminError.value = null
            forceSyncSchools()
            Pair(true, null)
        } else {
            val msg = "Identifiants incorrects ou expirés pour benjamintolno7@gmail.com."
            _adminError.value = "Erreur Firebase: $msg"
            Pair(false, msg)
        }
    }

    fun forceSyncSchools() {
        _adminError.value = null
        viewModelScope.launch {
            val auth = FirebaseAuth.getInstance()
            ensureAdminFirebaseAuth()

            cleanupDuplicateSchoolAccounts()
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
                        
                        val cleanDocEmail = email.lowercase().trim()
                        if (!cleanDocEmail.contains("@") || cleanDocEmail.startsWith("fin_") || cleanDocEmail.startsWith("fin-") || cleanDocEmail == "dore") continue

                        val existing = repository.getSchoolAccountByName(email)
                            ?: repository.getAllSchoolAccounts().find { it.schoolName.equals(email, ignoreCase = true) || (it.displayName.isNotBlank() && it.displayName.equals(displayName, ignoreCase = true)) }
                        val isLocallySettled = isCommissionSettled(email) || isCommissionSettled(displayName) || (existing != null && (isCommissionSettled(existing.schoolName) || isCommissionSettled(existing.displayName)))
                        val settledCount = maxOf(getCommissionSettledCount(email), getCommissionSettledCount(displayName), existing?.let { getCommissionSettledCount(it.schoolName) } ?: 0)
                        val effectiveIsLocked = isAppLocked
                        val effectiveLock = lockReason ?: ""
                        val parsedCommFromReason = Regex("""(\d+)\s*GNF""").find(effectiveLock)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
                        val calcComm = (onlinePaymentsCount * 3000L)
                        val effectiveComm = if (effectiveIsLocked) {
                            maxOf(unpaidCommission, calcComm, parsedCommFromReason)
                        } else {
                            if (isLocallySettled && settledCount >= onlinePaymentsCount) 0L else maxOf(unpaidCommission, calcComm)
                        }
                        val effectiveSub = hasActiveSubscription
                        val effectiveSubExpiry = subscriptionExpiryDate
                        val effectivePending = isPendingValidation

                        if (existing != null) {
                            val updatedSchool = existing.copy(
                                displayName = displayName,
                                hasActiveSubscription = effectiveSub,
                                isPendingValidation = effectivePending,
                                founderPhone = founderPhone,
                                financierPasswordHash = if (financierPasswordHash.isNotBlank()) financierPasswordHash else existing.financierPasswordHash,
                                passwordHash = if (passwordHash.isNotBlank()) passwordHash else existing.passwordHash,
                                logoBase64 = logoBase64 ?: existing.logoBase64,
                                address = address,
                                paymentPhoneNumber = paymentPhoneNumber ?: existing.paymentPhoneNumber,
                                transactionId = transactionId ?: existing.transactionId,
                                rejectionReason = rejectionReason,
                                subscriptionExpiryDate = effectiveSubExpiry,
                                createdAt = createdAt,
                                onlinePaymentEnabled = onlinePaymentEnabled,
                                isAppLocked = effectiveIsLocked,
                                unpaidCommission = effectiveComm,
                                onlinePaymentsCount = maxOf(existing.onlinePaymentsCount, onlinePaymentsCount),
                                onlinePaymentsTotal = maxOf(existing.onlinePaymentsTotal, onlinePaymentsTotal),
                                lockReason = effectiveLock
                            )
                            repository.updateSchoolAccount(updatedSchool)
                            val curAcc = _schoolAccount.value
                            if (curAcc != null && (curAcc.schoolName.equals(email, ignoreCase = true) || curAcc.displayName.equals(displayName, ignoreCase = true) || curAcc.id == existing.id)) {
                                _schoolAccount.value = updatedSchool
                            }
                        } else if (cleanDocEmail.contains("@") && !cleanDocEmail.startsWith("fin_") && !cleanDocEmail.startsWith("fin-") && cleanDocEmail != "dore") {
                            repository.insertSchoolAccountDirect(
                                SchoolAccount(
                                    schoolName = email,
                                    displayName = displayName,
                                    hasActiveSubscription = effectiveSub,
                                    isPendingValidation = effectivePending,
                                    founderPhone = founderPhone,
                                    financierPasswordHash = financierPasswordHash,
                                    passwordHash = passwordHash,
                                    logoBase64 = logoBase64,
                                    address = address,
                                    paymentPhoneNumber = paymentPhoneNumber,
                                    transactionId = transactionId,
                                    rejectionReason = rejectionReason,
                                    subscriptionExpiryDate = effectiveSubExpiry,
                                    createdAt = createdAt,
                                    onlinePaymentEnabled = onlinePaymentEnabled,
                                    isAppLocked = effectiveIsLocked,
                                    unpaidCommission = effectiveComm,
                                    onlinePaymentsCount = onlinePaymentsCount,
                                    onlinePaymentsTotal = onlinePaymentsTotal,
                                    lockReason = effectiveLock
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
            
            // 1. Essai lecture globale si les règles le permettent
            try {
                val snapshot = rtdb.getReference("schools").get().await()
                if (snapshot.exists()) {
                    processRtdbSchoolsSnapshot(snapshot)
                }
            } catch (e: Exception) {
                android.util.Log.d("ScolaPay", "Global schools read notice: ${e.message}")
            }

            // 2. Interrogation directe école par école (correspondant à "$schoolId": { ".read": true })
            val allAccounts = repository.getAllSchoolAccounts()
            if (allAccounts.isNotEmpty()) {
                var hasChanges = false
                for (acc in allAccounts) {
                    val candidateKeys = mutableSetOf<String>()
                    if (acc.displayName.isNotBlank()) {
                        candidateKeys.add(acc.displayName.trim())
                        candidateKeys.add(acc.displayName.replace(Regex("[.#$\\[\\]/]"), "_").trim())
                    }
                    if (acc.schoolName.isNotBlank()) {
                        candidateKeys.add(acc.schoolName.trim())
                        candidateKeys.add(acc.schoolName.replace(Regex("[.#$\\[\\]/]"), "_").trim())
                    }

                    var bestComm = acc.unpaidCommission
                    var bestCount = acc.onlinePaymentsCount
                    var bestTotal = acc.onlinePaymentsTotal
                    var bestOnlineEnabled = acc.onlinePaymentEnabled
                    var bestIsLocked = acc.isAppLocked
                    var bestLockReason = acc.lockReason

                    for (cKey in candidateKeys) {
                        if (cKey.isBlank()) continue
                        try {
                            val schoolChildSnap = rtdb.getReference("schools").child(cKey).get().await()
                            if (schoolChildSnap.exists()) {
                                val rtdbComm = schoolChildSnap.child("unpaidCommission").getValue(Long::class.java)
                                    ?: schoolChildSnap.child("unpaidCommission").getValue(Double::class.java)?.toLong() ?: 0L
                                val rtdbCount = schoolChildSnap.child("onlinePaymentsCount").getValue(Long::class.java)?.toInt()
                                    ?: schoolChildSnap.child("onlinePaymentsCount").getValue(Double::class.java)?.toInt() ?: 0
                                val rtdbTotal = schoolChildSnap.child("onlinePaymentsTotal").getValue(Long::class.java)
                                    ?: schoolChildSnap.child("onlinePaymentsTotal").getValue(Double::class.java)?.toLong() ?: 0L
                                val rtdbOnlineEnabled = schoolChildSnap.child("onlinePaymentEnabled").getValue(Boolean::class.java)
                                val rtdbIsLocked = schoolChildSnap.child("isAppLocked").getValue(Boolean::class.java)
                                val rtdbLock = schoolChildSnap.child("lockReason").getValue(String::class.java)

                                if (rtdbComm > bestComm) bestComm = rtdbComm
                                if (rtdbCount > bestCount) bestCount = rtdbCount
                                if (rtdbTotal > bestTotal) bestTotal = rtdbTotal
                                if (rtdbOnlineEnabled != null) bestOnlineEnabled = rtdbOnlineEnabled
                                if (rtdbIsLocked != null) bestIsLocked = rtdbIsLocked
                                if (rtdbLock != null) bestLockReason = rtdbLock
                            }
                        } catch (childErr: Exception) {
                            android.util.Log.d("ScolaPay", "Child read notice for $cKey: ${childErr.message}")
                        }
                    }

                    val settledCount = maxOf(getCommissionSettledCount(acc.schoolName), getCommissionSettledCount(acc.displayName))
                    val isLocallyUnlocked = (isCommissionSettled(acc.schoolName) || isCommissionSettled(acc.displayName)) && settledCount >= bestCount
                    val calcComm = if (settledCount > 0) maxOf(0L, (bestCount - settledCount) * 3000L) else (bestCount * 3000L)
                    val finalComm = if (isLocallyUnlocked && calcComm == 0L) 0L else maxOf(bestComm, calcComm)

                    val effectiveIsLocked = if (isLocallyUnlocked) false else bestIsLocked
                    val effectiveLockReason = if (!effectiveIsLocked) "" else bestLockReason
                    val effectiveComm = if (isLocallyUnlocked && calcComm == 0L) 0L else finalComm

                    if (effectiveComm != acc.unpaidCommission || bestCount != acc.onlinePaymentsCount || bestTotal != acc.onlinePaymentsTotal || bestOnlineEnabled != acc.onlinePaymentEnabled || effectiveIsLocked != acc.isAppLocked) {
                        val updated = acc.copy(
                            unpaidCommission = effectiveComm,
                            onlinePaymentsCount = bestCount,
                            onlinePaymentsTotal = bestTotal,
                            onlinePaymentEnabled = bestOnlineEnabled,
                            isAppLocked = effectiveIsLocked,
                            lockReason = effectiveLockReason
                        )
                        repository.updateSchoolAccount(updated)
                        hasChanges = true
                    }
                }
                if (hasChanges) {
                    loadAdminSchools()
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("ScolaPay", "Error syncing schools from RTDB: ${e.message}")
        }
    }

    suspend fun auditOnlinePaymentsFromRTDB() {
        auditOnlinePayments()
    }

    suspend fun auditOnlinePayments() {
        try {
            val allAccounts = repository.getAllSchoolAccounts()
            if (allAccounts.isEmpty()) return

            var hasChanges = false
            val rtdb = com.google.firebase.database.FirebaseDatabase.getInstance("https://scolapay-b6289-default-rtdb.europe-west1.firebasedatabase.app")
            var rtdbStudentsSnap: com.google.firebase.database.DataSnapshot? = null
            try {
                rtdbStudentsSnap = rtdb.getReference("students").get().await()
            } catch (eRtdb: Exception) {
                android.util.Log.d("ScolaPay", "RTDB students fetch notice: ${eRtdb.message}")
            }

            for (acc in allAccounts) {
                if (!acc.schoolName.contains("@")) continue

                var onlineCount = 0
                var onlineTotal = 0L
                val processedPayments = mutableSetOf<String>()

                // 1. Audit from Firestore schools/{email}/payments subcollection
                try {
                    val pSnap = firestore.collection("schools").document(acc.schoolName).collection("payments").get().await()
                    for (doc in pSnap.documents) {
                        val pid = doc.id
                        val pMethod = doc.getString("paymentMethod") ?: ""
                        val isCanc = doc.getBoolean("isCancelled") ?: false
                        val amt = doc.getLong("amount") ?: 0L
                        val isOnline = !isCanc && amt > 0L && (
                            pid.startsWith("PAY_") ||
                            pMethod.contains("ligne", ignoreCase = true) ||
                            pMethod.contains("ChapChap", ignoreCase = true) ||
                            pMethod.contains("Orange", ignoreCase = true) ||
                            pMethod.contains("MoMo", ignoreCase = true)
                        )
                        if (isOnline && processedPayments.add(pid)) {
                            onlineCount++
                            onlineTotal += amt
                        }
                    }
                } catch (eFs: Exception) {
                    android.util.Log.d("ScolaPay", "Firestore payments audit notice for ${acc.schoolName}: ${eFs.message}")
                }

                // 2. Audit from RTDB students/{id}/payments
                if (rtdbStudentsSnap != null && rtdbStudentsSnap.exists()) {
                    val normAccName = normalizeSyncKey(acc.displayName)
                    val normAccEmail = normalizeSyncKey(acc.schoolName)
                    for (stChild in rtdbStudentsSnap.children) {
                        val paymentsNode = stChild.child("payments")
                        if (paymentsNode.exists()) {
                            for (pChild in paymentsNode.children) {
                                val pid = pChild.key ?: continue
                                val sName = pChild.child("schoolName").getValue(String::class.java)
                                    ?: stChild.child("schoolName").getValue(String::class.java) ?: ""
                                val sEmail = pChild.child("schoolEmail").getValue(String::class.java)
                                    ?: stChild.child("schoolEmail").getValue(String::class.java) ?: ""
                                val isMatch = (sEmail.isNotBlank() && (sEmail.equals(acc.schoolName, ignoreCase = true) || normalizeSyncKey(sEmail) == normAccEmail)) ||
                                              (sName.isNotBlank() && (sName.equals(acc.displayName, ignoreCase = true) || normalizeSyncKey(sName) == normAccName))
                                if (isMatch) {
                                    val amt = pChild.child("amount").getValue(Long::class.java)
                                        ?: pChild.child("amount").getValue(Double::class.java)?.toLong() ?: 0L
                                    if (amt > 0L && processedPayments.add(pid)) {
                                        onlineCount++
                                        onlineTotal += amt
                                    }
                                }
                            }
                        }
                    }
                }

                val finalCount = maxOf(acc.onlinePaymentsCount, onlineCount)
                val finalTotal = maxOf(acc.onlinePaymentsTotal, onlineTotal)

                val settledCount = maxOf(getCommissionSettledCount(acc.schoolName), getCommissionSettledCount(acc.displayName))
                val isSettled = (isCommissionSettled(acc.schoolName) || isCommissionSettled(acc.displayName)) && settledCount >= finalCount

                val calculatedComm = if (settledCount > 0) {
                    maxOf(0L, (finalCount - settledCount) * 3000L)
                } else {
                    finalCount * 3000L
                }
                val finalCommission = if (isSettled && calculatedComm == 0L) 0L else maxOf(calculatedComm, acc.unpaidCommission)
                val finalIsLocked = acc.isAppLocked
                val finalLockReason = acc.lockReason

                if (finalCount != acc.onlinePaymentsCount || finalTotal != acc.onlinePaymentsTotal || finalCommission != acc.unpaidCommission || finalIsLocked != acc.isAppLocked) {
                    val updated = acc.copy(
                        onlinePaymentsCount = finalCount,
                        onlinePaymentsTotal = finalTotal,
                        unpaidCommission = finalCommission,
                        isAppLocked = finalIsLocked,
                        lockReason = finalLockReason
                    )
                    repository.updateSchoolAccount(updated)
                    hasChanges = true

                    try {
                        firestore.collection("schools").document(acc.schoolName).set(
                            mapOf(
                                "unpaidCommission" to finalCommission,
                                "onlinePaymentsCount" to finalCount,
                                "onlinePaymentsTotal" to finalTotal,
                                "isAppLocked" to finalIsLocked,
                                "lockReason" to (finalLockReason ?: "")
                            ),
                            com.google.firebase.firestore.SetOptions.merge()
                        )
                    } catch (eFs: Exception) {
                        android.util.Log.d("ScolaPay", "Firestore commission sync notice: ${eFs.message}")
                    }

                    val sName = if (acc.displayName.isNotBlank()) acc.displayName else acc.schoolName
                    val schoolKey = sName.replace(Regex("[.#$\\[\\]/]"), "_").trim()
                    try {
                        rtdb.getReference("schools").child(schoolKey).updateChildren(
                            mapOf(
                                "unpaidCommission" to finalCommission,
                                "onlinePaymentsCount" to finalCount,
                                "onlinePaymentsTotal" to finalTotal,
                                "isAppLocked" to finalIsLocked,
                                "lockReason" to (finalLockReason ?: "")
                            )
                        )
                    } catch (eR: Exception) {}
                }
            }
            if (hasChanges) {
                loadAdminSchools()
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
            val normKey = normalizeSyncKey(cleanKey)
            val matchedAccount = allAccounts.find { acc ->
                val cleanDisplayName = acc.displayName.trim()
                val cleanEmail = acc.schoolName.trim()
                val sanitizedDisplay = cleanDisplayName.replace(Regex("[.#$\\[\\]/]"), "_").trim()
                val sanitizedEmail = cleanEmail.replace(Regex("[.#$\\[\\]/]"), "_").trim()
                val normDisplay = normalizeSyncKey(cleanDisplayName)
                val normEmail = normalizeSyncKey(cleanEmail)

                cleanKey.equals(cleanDisplayName, ignoreCase = true) ||
                cleanKey.equals(cleanEmail, ignoreCase = true) ||
                cleanKey.equals(sanitizedDisplay, ignoreCase = true) ||
                cleanKey.equals(sanitizedEmail, ignoreCase = true) ||
                (normKey.isNotBlank() && (normKey == normDisplay || normKey == normEmail)) ||
                (childSchoolName.isNotBlank() && (
                    childSchoolName.equals(cleanDisplayName, ignoreCase = true) ||
                    childSchoolName.equals(cleanEmail, ignoreCase = true) ||
                    normalizeSyncKey(childSchoolName) == normDisplay
                )) ||
                (childEmail.isNotBlank() && (
                    childEmail.equals(cleanEmail, ignoreCase = true) ||
                    childEmail.equals(cleanDisplayName, ignoreCase = true) ||
                    normalizeSyncKey(childEmail) == normEmail
                ))
            }

            if (matchedAccount != null) {
                val newCount = maxOf(matchedAccount.onlinePaymentsCount, rtdbCount)
                val newTotal = maxOf(matchedAccount.onlinePaymentsTotal, rtdbTotal)
                val settledCount = maxOf(getCommissionSettledCount(matchedAccount.schoolName), getCommissionSettledCount(matchedAccount.displayName))
                val isLocallyUnlocked = (isCommissionSettled(matchedAccount.schoolName) || isCommissionSettled(matchedAccount.displayName)) && settledCount >= newCount
                val calcComm = if (settledCount > 0) maxOf(0L, (newCount - settledCount) * 3000L) else (newCount * 3000L)
                val newCommission = if (isLocallyUnlocked && calcComm == 0L) 0L else maxOf(matchedAccount.unpaidCommission, rtdbCommission, calcComm)
                val newOnlineEnabled = rtdbOnlinePaymentEnabled ?: matchedAccount.onlinePaymentEnabled
                val effectiveIsLocked = if (isLocallyUnlocked) false else (rtdbIsAppLocked ?: matchedAccount.isAppLocked)
                val effectiveLockReason = if (!effectiveIsLocked) "" else (rtdbLockReason ?: matchedAccount.lockReason)
                val effectiveCommission = if (isLocallyUnlocked && calcComm == 0L) 0L else newCommission

                if (effectiveCommission != matchedAccount.unpaidCommission ||
                    newCount != matchedAccount.onlinePaymentsCount ||
                    newTotal != matchedAccount.onlinePaymentsTotal ||
                    newOnlineEnabled != matchedAccount.onlinePaymentEnabled ||
                    effectiveIsLocked != matchedAccount.isAppLocked) {

                    val updated = matchedAccount.copy(
                        unpaidCommission = effectiveCommission,
                        onlinePaymentsCount = newCount,
                        onlinePaymentsTotal = newTotal,
                        onlinePaymentEnabled = newOnlineEnabled,
                        isAppLocked = effectiveIsLocked,
                        lockReason = effectiveLockReason
                    )
                    repository.updateSchoolAccount(updated)
                    if (_schoolAccount.value?.id == matchedAccount.id) {
                        _schoolAccount.value = updated
                    }
                    hasChanges = true

                    try {
                        firestore.collection("schools").document(matchedAccount.schoolName).set(
                            mapOf(
                                "unpaidCommission" to effectiveCommission,
                                "onlinePaymentsCount" to newCount,
                                "onlinePaymentsTotal" to newTotal,
                                "onlinePaymentEnabled" to newOnlineEnabled,
                                "isAppLocked" to effectiveIsLocked
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
                        android.util.Log.d("ScolaPay", "RTDB students listen notice: ${error.message}")
                    }
                }
                studentsRef.addValueEventListener(studentListener)
                activeRtdbListeners[studentsRef] = studentListener
            }
        } catch (e: Exception) {
            android.util.Log.w("ScolaPay", "Error setting up RTDB schools listener: ${e.message}")
        }
    }

    suspend fun cleanupDuplicateSchoolAccounts() {
        try {
            // Purge any fictitious schools from Firestore
            try {
                val fsDocs = firestore.collection("schools").get().await()
                for (doc in fsDocs.documents) {
                    val dId = doc.id.lowercase().trim()
                    if (dId.startsWith("fin_") || dId.startsWith("fin-") || !dId.contains("@") || dId == "dore") {
                        try {
                            doc.reference.delete()
                            val rtdb = com.google.firebase.database.FirebaseDatabase.getInstance("https://scolapay-b6289-default-rtdb.europe-west1.firebasedatabase.app")
                            rtdb.getReference("schools").child(doc.id.replace(Regex("[.#$\\[\\]/]"), "_")).removeValue()
                        } catch (e: Exception) {}
                    }
                }
            } catch (eFs: Exception) {}

            val allAccounts = repository.getAllSchoolAccounts()
            if (allAccounts.isEmpty()) return

            // Purge dummy / fictitious accounts:
            // 1) Accounts starting with "fin_" or "fin-"
            // 2) Accounts without "@"
            // 3) Accounts matching dummy names like "dore"
            for (acc in allAccounts) {
                val sName = acc.schoolName.lowercase().trim()
                if (sName.startsWith("fin_") || sName.startsWith("fin-") || !sName.contains("@") || sName == "dore") {
                    repository.deleteSchoolAccountByName(acc.schoolName)
                    try {
                        firestore.collection("schools").document(acc.schoolName).delete()
                        val rtdb = com.google.firebase.database.FirebaseDatabase.getInstance("https://scolapay-b6289-default-rtdb.europe-west1.firebasedatabase.app")
                        rtdb.getReference("schools").child(acc.schoolName.replace(Regex("[.#$\\[\\]/]"), "_")).removeValue()
                    } catch (e: Exception) {}
                }
            }

            val remainingAccounts = repository.getAllSchoolAccounts()
            val grouped = remainingAccounts.groupBy { it.schoolName.lowercase().trim() }
            for ((key, list) in grouped) {
                if (list.size > 1) {
                    val isSettled = list.any { isCommissionSettled(it.schoolName) || isCommissionSettled(it.displayName) }
                    val best = list.reduce { acc, next ->
                        acc.copy(
                            displayName = if (acc.displayName.isNotBlank()) acc.displayName else next.displayName,
                            passwordHash = if (acc.passwordHash.isNotBlank()) acc.passwordHash else next.passwordHash,
                            financierPasswordHash = if (acc.financierPasswordHash.isNotBlank()) acc.financierPasswordHash else next.financierPasswordHash,
                            hasActiveSubscription = acc.hasActiveSubscription && next.hasActiveSubscription,
                            isPendingValidation = acc.isPendingValidation || next.isPendingValidation,
                            founderPhone = if (acc.founderPhone.isNotBlank()) acc.founderPhone else next.founderPhone,
                            address = if (acc.address.isNotBlank()) acc.address else next.address,
                            paymentPhoneNumber = acc.paymentPhoneNumber ?: next.paymentPhoneNumber,
                            transactionId = acc.transactionId ?: next.transactionId,
                            subscriptionExpiryDate = if (acc.hasActiveSubscription) acc.subscriptionExpiryDate else next.subscriptionExpiryDate,
                            onlinePaymentEnabled = acc.onlinePaymentEnabled && next.onlinePaymentEnabled,
                            isAppLocked = if (isSettled) false else (acc.isAppLocked || next.isAppLocked),
                            unpaidCommission = if (isSettled) 0L else maxOf(acc.unpaidCommission, next.unpaidCommission),
                            onlinePaymentsCount = maxOf(acc.onlinePaymentsCount, next.onlinePaymentsCount),
                            onlinePaymentsTotal = maxOf(acc.onlinePaymentsTotal, next.onlinePaymentsTotal)
                        )
                    }
                    for (item in list) {
                        repository.deleteSchoolAccountByName(item.schoolName)
                    }
                    repository.insertSchoolAccountDirect(best)
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("ScolaPay", "Error cleaning duplicate school accounts: ${e.message}")
        }
    }

    fun loadAdminSchools() {
        viewModelScope.launch {
            cleanupDuplicateSchoolAccounts()
            val accounts = repository.getAllSchoolAccounts()
            val distinct = accounts
                .filter { it.schoolName.contains("@") && !it.schoolName.lowercase().trim().startsWith("fin_") && !it.schoolName.lowercase().trim().startsWith("fin-") && it.schoolName.lowercase().trim() != "dore" }
                .distinctBy { it.schoolName.lowercase().trim() }
            val adminItems = distinct.map {
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

    fun toggleSchoolAppLock(email: String, schoolName: String, locked: Boolean, reason: String? = null, customAmount: Long? = null) {
        viewModelScope.launch {
            val account = repository.getSchoolAccountByName(email)
                ?: repository.getAllSchoolAccounts().find { it.schoolName.equals(email, ignoreCase = true) || (it.displayName.isNotBlank() && it.displayName.equals(schoolName, ignoreCase = true)) }
            val effectiveLocked = locked

            val currentUnpaid = account?.unpaidCommission ?: 0L
            val paymentsCount = account?.onlinePaymentsCount ?: 0
            val calcComm = paymentsCount.toLong() * 3000L
            val dueComm = if (effectiveLocked) {
                if (customAmount != null && customAmount > 0L) {
                    customAmount
                } else if (currentUnpaid > 0L) {
                    currentUnpaid
                } else if (calcComm > 0L) {
                    calcComm
                } else {
                    3000L
                }
            } else {
                0L
            }

            if (effectiveLocked) {
                setCommissionSettled(email, false, 0)
                if (account != null) {
                    setCommissionSettled(account.schoolName, false, 0)
                    if (account.displayName.isNotBlank()) setCommissionSettled(account.displayName, false, 0)
                }
            } else {
                val curCount = account?.onlinePaymentsCount ?: 0
                setCommissionSettled(email, true, curCount)
                if (account != null) {
                    setCommissionSettled(account.schoolName, true, curCount)
                    if (account.displayName.isNotBlank()) setCommissionSettled(account.displayName, true, curCount)
                }
            }

            val defaultReason = reason ?: if (effectiveLocked) {
                if (dueComm > 0L) "Accès à l'application ScolaPay suspendu pour facture de commission impayée ($dueComm GNF). Merci de régulariser."
                else "Accès à l'application ScolaPay suspendu par l'administration. Merci de contacter le support zalytechno."
            } else ""

            val updated = account?.copy(
                isAppLocked = effectiveLocked,
                lockReason = defaultReason,
                unpaidCommission = dueComm
            )
            if (updated != null) {
                repository.updateSchoolAccount(updated)
                if (_schoolAccount.value?.schoolName.equals(email, ignoreCase = true) || _schoolAccount.value?.id == updated.id) {
                    _schoolAccount.value = updated
                }
            }

            val realEmail = if (email.startsWith("fin_")) email.removePrefix("fin_") else email
            val cleanEmailDoc = realEmail.trim().lowercase()

            val firestorePayload = mapOf(
                "isAppLocked" to effectiveLocked,
                "lockReason" to defaultReason,
                "unpaidCommission" to dueComm
            )

            if (cleanEmailDoc.contains("@") && !cleanEmailDoc.startsWith("fin_")) {
                try {
                    firestore.collection("schools").document(cleanEmailDoc).set(
                        firestorePayload,
                        com.google.firebase.firestore.SetOptions.merge()
                    )
                } catch (e: Exception) {}
            }

            try {
                val rtdb = com.google.firebase.database.FirebaseDatabase.getInstance("https://scolapay-b6289-default-rtdb.europe-west1.firebasedatabase.app")
                val cleanK = cleanEmailDoc.replace(Regex("[.#$\\[\\]/]"), "_").trim()
                if (cleanK.isNotBlank() && !cleanK.startsWith("fin_")) {
                    rtdb.getReference("schools").child(cleanK).updateChildren(firestorePayload)
                }
                val dName = (account?.displayName ?: schoolName).trim()
                if (dName.isNotBlank() && !dName.equals(cleanEmailDoc, ignoreCase = true) && !dName.startsWith("fin_")) {
                    val dKey = dName.replace(Regex("[.#$\\[\\]/]"), "_").trim()
                    if (dKey.isNotBlank()) {
                        rtdb.getReference("schools").child(dKey).updateChildren(firestorePayload)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.w("ScolaPay", "RTDB sync error: ${e.message}")
            }
            loadAdminSchools()
        }
    }

    fun resetSchoolCommission(email: String, schoolName: String) {
        viewModelScope.launch {
            val account = repository.getSchoolAccountByName(email)
            val curCount = account?.onlinePaymentsCount ?: 0
            if (account != null) {
                repository.updateSchoolAccount(account.copy(unpaidCommission = 0L, isAppLocked = false, lockReason = ""))
                setCommissionSettled(account.schoolName, true, curCount)
                if (account.displayName.isNotBlank()) setCommissionSettled(account.displayName, true, curCount)
            } else {
                setCommissionSettled(email, true, curCount)
            }
            val cleanEmail = email.lowercase().trim()
            val cleanEmailKey = cleanEmail.replace(Regex("[.#$\\[\\]/]"), "_").trim()
            if (cleanEmail.contains("@") && !cleanEmail.startsWith("fin_")) {
                firestore.collection("schools").document(cleanEmail).set(
                    mapOf("unpaidCommission" to 0L, "isAppLocked" to false, "lockReason" to ""),
                    com.google.firebase.firestore.SetOptions.merge()
                )
            }
            val sName = if (account?.displayName.isNullOrBlank()) (if (schoolName.isNotBlank()) schoolName else email) else account!!.displayName
            val schoolKey = sName.replace(Regex("[.#$\\[\\]/]"), "_").trim()
            try {
                val rtdbRef = com.google.firebase.database.FirebaseDatabase.getInstance("https://scolapay-b6289-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("schools")
                if (cleanEmailKey.isNotBlank()) {
                    rtdbRef.child(cleanEmailKey).updateChildren(mapOf("unpaidCommission" to 0L, "isAppLocked" to false, "lockReason" to ""))
                }
                if (schoolKey != cleanEmailKey && schoolKey.isNotBlank()) {
                    rtdbRef.child(schoolKey).updateChildren(mapOf("unpaidCommission" to 0L, "isAppLocked" to false, "lockReason" to ""))
                }
            } catch (e: Exception) {}
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
                val expiry = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000)
                val curOnlineCount = account.onlinePaymentsCount
                setCommissionSettled(account.schoolName, true, curOnlineCount)
                if (account.displayName.isNotBlank()) setCommissionSettled(account.displayName, true, curOnlineCount)

                val updated = account.copy(
                    hasActiveSubscription = true,
                    isPendingValidation = false,
                    subscriptionExpiryDate = expiry,
                    isAppLocked = false,
                    lockReason = "",
                    unpaidCommission = 0L
                )
                repository.updateSchoolAccount(updated)
                if (_schoolAccount.value?.schoolName.equals(email, ignoreCase = true) || _schoolAccount.value?.id == updated.id) {
                    _schoolAccount.value = updated
                }
                
                val payload = mapOf(
                    "hasActiveSubscription" to true,
                    "isPendingValidation" to false,
                    "subscriptionExpiryDate" to expiry,
                    "isAppLocked" to false,
                    "lockReason" to "",
                    "unpaidCommission" to 0L
                )

                val cleanEmail = email.lowercase().trim()
                if (cleanEmail.contains("@") && !cleanEmail.startsWith("fin_")) {
                    firestore.collection("schools").document(cleanEmail).set(
                        payload, com.google.firebase.firestore.SetOptions.merge()
                    ).addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e) }
                }

                try {
                    val rtdb = com.google.firebase.database.FirebaseDatabase.getInstance("https://scolapay-b6289-default-rtdb.europe-west1.firebasedatabase.app")
                    val cleanEmailKey = cleanEmail.replace(Regex("[.#$\\[\\]/]"), "_").trim()
                    if (cleanEmailKey.isNotBlank()) {
                        rtdb.getReference("schools").child(cleanEmailKey).updateChildren(payload)
                    }
                    val sName = if (account.displayName.isNotBlank()) account.displayName else account.schoolName
                    val schoolKey = sName.replace(Regex("[.#$\\[\\]/]"), "_").trim()
                    if (schoolKey != cleanEmailKey && schoolKey.isNotBlank()) {
                        rtdb.getReference("schools").child(schoolKey).updateChildren(payload)
                    }
                } catch (eRtdb: Exception) {}
                
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
                    subscriptionExpiryDate = 0L,
                    rejectionReason = reason
                )
                repository.updateSchoolAccount(updated)
                firestore.collection("schools").document(email).set(
                    mapOf(
                        "hasActiveSubscription" to false,
                        "isPendingValidation" to false,
                        "subscriptionExpiryDate" to 0L,
                        "rejectionReason" to reason
                    ), com.google.firebase.firestore.SetOptions.merge()
                ).addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e) }

                val sName = if (account.displayName.isNotBlank()) account.displayName else account.schoolName
                val schoolKey = sName.replace(Regex("[.#$\\[\\]/]"), "_").trim()
                try {
                    val rtdb = com.google.firebase.database.FirebaseDatabase.getInstance("https://scolapay-b6289-default-rtdb.europe-west1.firebasedatabase.app")
                    rtdb.getReference("schools").child(schoolKey).updateChildren(
                        mapOf(
                            "hasActiveSubscription" to false,
                            "isPendingValidation" to false,
                            "subscriptionExpiryDate" to 0L
                        )
                    )
                } catch (eRtdb: Exception) {}

                loadAdminSchools()
            }
        }
    }

    fun toggleSchoolSubscription(email: String, active: Boolean) {
        viewModelScope.launch {
            val account = repository.getSchoolAccountByName(email)
            if (account != null) {
                val expiry = if (active) System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000L) else 0L
                val remainsLocked = if (!active) true else account.isAppLocked
                val lockReasonStr = if (!active) "Abonnement désactivé par l'administrateur" else (account.lockReason ?: "")
                val updated = account.copy(
                    hasActiveSubscription = active,
                    isPendingValidation = false,
                    subscriptionExpiryDate = expiry,
                    isAppLocked = remainsLocked,
                    lockReason = lockReasonStr,
                    rejectionReason = if (!active) "Abonnement désactivé par l'administrateur" else null
                )
                repository.updateSchoolAccount(updated)

                firestore.collection("schools").document(email).set(
                    mapOf(
                        "hasActiveSubscription" to active,
                        "isPendingValidation" to false,
                        "subscriptionExpiryDate" to expiry,
                        "isAppLocked" to remainsLocked,
                        "lockReason" to lockReasonStr,
                        "rejectionReason" to (updated.rejectionReason ?: "")
                    ), com.google.firebase.firestore.SetOptions.merge()
                ).addOnFailureListener { e -> android.util.Log.e("ScolaPay", "Error updating subscription toggle: ${e.message}") }

                val sName = if (account.displayName.isNotBlank()) account.displayName else account.schoolName
                val schoolKey = sName.replace(Regex("[.#$\\[\\]/]"), "_").trim()
                try {
                    val rtdb = com.google.firebase.database.FirebaseDatabase.getInstance("https://scolapay-b6289-default-rtdb.europe-west1.firebasedatabase.app")
                    rtdb.getReference("schools").child(schoolKey).updateChildren(
                        mapOf(
                            "hasActiveSubscription" to active,
                            "isPendingValidation" to false,
                            "subscriptionExpiryDate" to expiry,
                            "isAppLocked" to remainsLocked,
                            "lockReason" to lockReasonStr
                        )
                    )
                } catch (eRtdb: Exception) {}

                loadAdminSchools()
            }
        }
    }

    fun resetUnverifiedSchoolsToNonSubscribed(resetAll: Boolean = false) {
        viewModelScope.launch {
            val all = repository.getAllSchoolAccounts()
            for (acc in all) {
                if (acc.schoolName.equals("benjamintolno7@gmail.com", ignoreCase = true)) continue
                val hasNoProof = acc.transactionId.isNullOrBlank() && acc.paymentPhoneNumber.isNullOrBlank()
                if (resetAll || hasNoProof) {
                    val updated = acc.copy(
                        hasActiveSubscription = false,
                        subscriptionExpiryDate = 0L,
                        isPendingValidation = false
                    )
                    repository.updateSchoolAccount(updated)
                    try {
                        firestore.collection("schools").document(acc.schoolName).set(
                            mapOf(
                                "hasActiveSubscription" to false,
                                "subscriptionExpiryDate" to 0L,
                                "isPendingValidation" to false
                            ), com.google.firebase.firestore.SetOptions.merge()
                        )
                    } catch (eFs: Exception) {}

                    val sName = if (acc.displayName.isNotBlank()) acc.displayName else acc.schoolName
                    val schoolKey = sName.replace(Regex("[.#$\\[\\]/]"), "_").trim()
                    try {
                        val rtdb = com.google.firebase.database.FirebaseDatabase.getInstance("https://scolapay-b6289-default-rtdb.europe-west1.firebasedatabase.app")
                        rtdb.getReference("schools").child(schoolKey).updateChildren(
                            mapOf(
                                "hasActiveSubscription" to false,
                                "subscriptionExpiryDate" to 0L,
                                "isPendingValidation" to false
                            )
                        )
                    } catch (eRtdb: Exception) {}
                }
            }
            loadAdminSchools()
        }
    }


    init {
        val oldSettledKeys = sharedPrefs.all.keys.filter { it.startsWith("comm_settled_") }
        val ed = sharedPrefs.edit()
        for (k in oldSettledKeys) {
            ed.remove(k)
        }
        ed.remove("school_unlocked_global").apply()
        viewModelScope.launch {
            cleanupDuplicateSchoolAccounts()
            loadAdminSchools()
        }
        syncSchoolsFromFirestore()
        viewModelScope.launch {
            val loggedInEmail = sharedPrefs.getString("logged_in_email", null)
            val loggedInRole = sharedPrefs.getString("logged_in_role", null)
            
            if (loggedInEmail != null && loggedInRole != null) {
                if (loggedInRole == "ADMIN" && loggedInEmail.equals("benjamintolno7@gmail.com", ignoreCase = true)) {
                    _userRole.value = "ADMIN"
                    _schoolName.value = "ScolaPay Admin"
                    _currentSchoolId.value = -1
                    ensureAdminFirebaseAuth()
                    cleanupDuplicateSchoolAccounts()
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
        val rawInput = email.trim().lowercase()
        val cleanPass = pass.trim()
        if (rawInput.isBlank() || cleanPass.isBlank()) {
            _loginError.value = "Veuillez renseigner votre e-mail et votre mot de passe."
            return false
        }

        // --- Vérification Super Admin ---
        if (rawInput == "benjamintolno7@gmail.com") {
            var adminSuccess = false
            if (cleanPass == "Epbomibs5@") {
                adminSuccess = true
                try {
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(rawInput, cleanPass).await()
                } catch (e: Exception) {}
            } else if (cleanPass.length >= 6) {
                try {
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(rawInput, cleanPass).await()
                    adminSuccess = true
                } catch (e: Exception) {}
            }

            if (adminSuccess) {
                _userRole.value = "ADMIN"
                _schoolName.value = "ScolaPay Admin"
                _currentSchoolId.value = -1 
                val editor = sharedPrefs.edit()
                    .putString("logged_in_email", rawInput)
                    .putString("logged_in_role", "ADMIN")
                    .putString("admin_saved_pass", cleanPass)
                editor.apply()
                loadAdminSchools()
                listenToSchoolsFromRTDB()
                forceSyncSchools()
                return true
            } else {
                _loginError.value = "Mot de passe incorrect pour benjamintolno7@gmail.com."
                return false
            }
        }

        val auth = FirebaseAuth.getInstance()
        var isFounder = false
        var isFinancier = false

        // Detect if user typed a financier email format e.g. fin_dore@gmail.com
        val isFinancierPrefix = rawInput.startsWith("fin_") || rawInput.startsWith("fin-")
        val cleanEmail = if (isFinancierPrefix) rawInput.substring(4).trim() else rawInput

        // D'abord chercher l'établissement dans le Room local
        var accountFound = repository.getSchoolAccountByName(cleanEmail) 
            ?: repository.getSchoolAccountByName(rawInput)

        // Toujours synchroniser les identifiants frais depuis Firestore pour garantir le mot de passe actuel
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
                val unpComm = doc.getLong("unpaidCommission") ?: 0L

                if (accountFound != null) {
                    val updated = accountFound.copy(
                        displayName = dn,
                        passwordHash = if (pw.isNotBlank()) pw else accountFound.passwordHash,
                        financierPasswordHash = if (finPw.isNotBlank()) finPw else accountFound.financierPasswordHash,
                        address = if (addr.isNotBlank()) addr else accountFound.address,
                        founderPhone = if (phone.isNotBlank()) phone else accountFound.founderPhone,
                        subscriptionExpiryDate = subExpiry ?: accountFound.subscriptionExpiryDate,
                        isPendingValidation = isPending,
                        hasActiveSubscription = hasSub,
                        onlinePaymentEnabled = onlineEnabled,
                        isAppLocked = isLocked,
                        unpaidCommission = unpComm
                    )
                    repository.updateSchoolAccount(updated)
                    accountFound = updated
                } else {
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
                        unpaidCommission = unpComm,
                        createdAt = System.currentTimeMillis()
                    )
                    repository.insertSchoolAccountDirect(newAcc)
                    accountFound = newAcc
                }
            }
        } catch (eFs: Exception) {
            android.util.Log.w("ScolaPay", "Firestore lookup during login: ${eFs.message}")
        }

        // Si non trouvé en local ni Firestore, chercher dans Realtime Database
        if (accountFound == null) {
            try {
                val cleanKey = cleanEmail.replace(Regex("[.#$\\[\\]/]"), "_")
                val rtdb = com.google.firebase.database.FirebaseDatabase.getInstance("https://scolapay-b6289-default-rtdb.europe-west1.firebasedatabase.app")
                val snap = rtdb.getReference("schools").child(cleanKey).get().await()
                if (snap.exists()) {
                    val dn = snap.child("displayName").getValue(String::class.java) ?: cleanEmail
                    val pw = snap.child("passwordHash").getValue(String::class.java) ?: ""
                    val finPw = snap.child("financierPasswordHash").getValue(String::class.java) ?: ""
                    val newAcc = SchoolAccount(
                        schoolName = cleanEmail,
                        displayName = dn,
                        passwordHash = pw,
                        financierPasswordHash = finPw,
                        createdAt = System.currentTimeMillis()
                    )
                    repository.insertSchoolAccountDirect(newAcc)
                    accountFound = newAcc
                }
            } catch (eRtdb: Exception) {
                android.util.Log.w("ScolaPay", "RTDB lookup during login: ${eRtdb.message}")
            }
        }

        if (accountFound == null) {
            _loginError.value = "Aucun établissement trouvé pour l'identifiant renseigné. Veuillez vous inscrire d'abord."
            return false
        }

        val isEmailFormat = android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()

        // --- VÉRIFICATION DES MOTS DE PASSE ---
        val founderPass = accountFound.passwordHash.trim()
        val finPass = accountFound.financierPasswordHash.trim()
        val finEmail = "fin_$cleanEmail"

        if (isFinancierPrefix) {
            // Connexion explicite en tant que financier (fin_...)
            if (finPass.isNotEmpty() && cleanPass == finPass) {
                isFinancier = true
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(finEmail).matches() && cleanPass.length >= 6) {
                    try {
                        auth.signInWithEmailAndPassword(finEmail, cleanPass).await()
                    } catch (e: Exception) {}
                }
            } else if (cleanPass.length >= 6) {
                try {
                    auth.signInWithEmailAndPassword(finEmail, cleanPass).await()
                    isFinancier = true
                } catch (e: Exception) {}
            }
        } else {
            // Connexion normale
            if (founderPass.isNotEmpty() && cleanPass == founderPass) {
                isFounder = true
                if (isEmailFormat && cleanPass.length >= 6) {
                    try {
                        auth.signInWithEmailAndPassword(cleanEmail, cleanPass).await()
                    } catch (e: Exception) {
                        try {
                            auth.createUserWithEmailAndPassword(cleanEmail, cleanPass).await()
                        } catch (e2: Exception) {}
                    }
                }
            } else if (finPass.isNotEmpty() && cleanPass == finPass) {
                isFinancier = true
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(finEmail).matches() && cleanPass.length >= 6) {
                    try {
                        auth.signInWithEmailAndPassword(finEmail, cleanPass).await()
                    } catch (e: Exception) {
                        try {
                            auth.createUserWithEmailAndPassword(finEmail, cleanPass).await()
                        } catch (e2: Exception) {}
                    }
                }
            } else if (isEmailFormat && cleanPass.length >= 6) {
                // Tentative Firebase Auth comme dernière vérification de validité
                try {
                    auth.signInWithEmailAndPassword(cleanEmail, cleanPass).await()
                    isFounder = true
                    val updated = accountFound.copy(passwordHash = cleanPass)
                    repository.updateSchoolAccount(updated)
                    accountFound = updated
                    firestore.collection("schools").document(cleanEmail).set(
                        mapOf("passwordHash" to cleanPass),
                        com.google.firebase.firestore.SetOptions.merge()
                    )
                } catch (e: Exception) {
                    if (android.util.Patterns.EMAIL_ADDRESS.matcher(finEmail).matches()) {
                        try {
                            auth.signInWithEmailAndPassword(finEmail, cleanPass).await()
                            isFinancier = true
                            val updated = accountFound.copy(financierPasswordHash = cleanPass)
                            repository.updateSchoolAccount(updated)
                            accountFound = updated
                            firestore.collection("schools").document(cleanEmail).set(
                                mapOf("financierPasswordHash" to cleanPass),
                                com.google.firebase.firestore.SetOptions.merge()
                            )
                        } catch (e2: Exception) {}
                    }
                }
            }
        }

        if (!isFounder && !isFinancier) {
            _loginError.value = "Mot de passe incorrect pour cet établissement."
            return false // Échec total de l'authentification
        }

        // Enregistrer l'UID dans Firestore pour les règles de sécurité
        val uid = auth.currentUser?.uid
        if (uid != null) {
            val roleStr = if (isFounder) "FONDATEUR" else "FINANCIER"
            firestore.collection("schools").document(cleanEmail).collection("users").document(uid)
                .set(mapOf("role" to roleStr), com.google.firebase.firestore.SetOptions.merge())
        }

        var account = accountFound
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
        val cleanName = name.trim().lowercase()
        if (cleanName.startsWith("fin_") || cleanName.startsWith("fin-") || !cleanName.contains("@") || cleanName == "dore") {
            android.util.Log.e("ScolaPay", "Invalid school registration rejected: $cleanName")
            return false
        }
        val auth = FirebaseAuth.getInstance()
        val isEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(cleanName).matches()
        val authEmail = if (isEmail) cleanName else "${cleanName.replace(Regex("[^a-z0-9]"), "_").trim('_')}@scolapay.com"
        
        // 1. Créer le compte Firebase Auth pour le Fondateur
        if (fp.length >= 6) {
            try {
                auth.createUserWithEmailAndPassword(authEmail, fp).await()
            } catch (e: Exception) {
                try { 
                    auth.signInWithEmailAndPassword(authEmail, fp).await() 
                } catch (e2: Exception) {
                    android.util.Log.w("ScolaPay", "Auth registration note: ${e2.message}")
                }
            }
        }
        
        // 2. Créer le compte Firebase Auth pour le Financier (en arrière-plan)
        val finEmail = "fin_$authEmail"
        if (finp.length >= 6 && android.util.Patterns.EMAIL_ADDRESS.matcher(finEmail).matches()) {
            try {
                auth.createUserWithEmailAndPassword(finEmail, finp).await()
            } catch (e: Exception) {}
        }
        
        // 3. Se reconnecter en tant que Fondateur
        if (fp.length >= 6) {
            try {
                auth.signInWithEmailAndPassword(authEmail, fp).await()
            } catch (e: Exception) {}
        }

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
                android.util.Log.w("ScolaPay-Firebase", "Notice writing user role: ${e.message}")
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
