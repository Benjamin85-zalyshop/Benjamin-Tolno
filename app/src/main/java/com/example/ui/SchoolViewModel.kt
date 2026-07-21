package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.models.Expense
import com.example.data.models.Payment
import com.example.data.models.Student
import com.example.data.models.DeletionRequest
import com.example.data.models.ClassFee
import com.example.data.repository.SchoolRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.DocumentChange
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map

fun normalizeGradeName(grade: String): String {
    val trimmed = grade.trim()
    return when {
        trimmed.equals("1ere Année", ignoreCase = true) || trimmed.equals("1ere annee", ignoreCase = true) || trimmed.equals("1ère annee", ignoreCase = true) -> "1ère Année"
        trimmed.equals("2eme Année", ignoreCase = true) || trimmed.equals("2eme annee", ignoreCase = true) || trimmed.equals("2ème annee", ignoreCase = true) -> "2ème Année"
        trimmed.equals("3eme Année", ignoreCase = true) || trimmed.equals("3eme annee", ignoreCase = true) || trimmed.equals("3ème annee", ignoreCase = true) -> "3ème Année"
        trimmed.equals("4eme Année", ignoreCase = true) || trimmed.equals("4eme annee", ignoreCase = true) || trimmed.equals("4ème annee", ignoreCase = true) -> "4ème Année"
        trimmed.equals("5eme Année", ignoreCase = true) || trimmed.equals("5eme annee", ignoreCase = true) || trimmed.equals("5ème annee", ignoreCase = true) -> "5ème Année"
        trimmed.equals("6eme Année", ignoreCase = true) || trimmed.equals("6eme annee", ignoreCase = true) || trimmed.equals("6ème annee", ignoreCase = true) -> "6ème Année"
        trimmed.equals("7eme Année", ignoreCase = true) || trimmed.equals("7eme annee", ignoreCase = true) || trimmed.equals("7ème annee", ignoreCase = true) -> "7ème Année"
        trimmed.equals("8eme Année", ignoreCase = true) || trimmed.equals("8eme annee", ignoreCase = true) || trimmed.equals("8ème annee", ignoreCase = true) -> "8ème Année"
        trimmed.equals("9eme Année", ignoreCase = true) || trimmed.equals("9eme annee", ignoreCase = true) || trimmed.equals("9ème annee", ignoreCase = true) -> "9ème Année"
        trimmed.equals("10eme Année", ignoreCase = true) || trimmed.equals("10eme annee", ignoreCase = true) || trimmed.equals("10ème annee", ignoreCase = true) -> "10ème Année"
        trimmed.equals("11eme Année", ignoreCase = true) || trimmed.equals("11eme annee", ignoreCase = true) || trimmed.equals("11ème annee", ignoreCase = true) -> "11ème Année"
        trimmed.equals("12eme Année", ignoreCase = true) || trimmed.equals("12eme annee", ignoreCase = true) || trimmed.equals("12ème annee", ignoreCase = true) -> "12ème Année"
        trimmed.equals("petite section", ignoreCase = true) -> "Petite Section"
        trimmed.equals("moyenne section", ignoreCase = true) -> "Moyenne Section"
        trimmed.equals("grande section", ignoreCase = true) -> "Grande Section"
        else -> trimmed
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SchoolViewModel(
    private val repository: SchoolRepository,
    private val context: android.content.Context
) : ViewModel() {
    private val _currentSchoolId = MutableStateFlow<Int?>(null)
    val currentSchoolId: StateFlow<Int?> = _currentSchoolId
    
    private val _schoolName = MutableStateFlow<String?>(null)
    val schoolName: StateFlow<String?> = _schoolName
    
    private val _selectedSection = MutableStateFlow<String>("Toutes les sections")
    val selectedSection: StateFlow<String> = _selectedSection
    
    private val _userRole = MutableStateFlow<String?>("FOUNDER")
    val userRole: StateFlow<String?> = _userRole
    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError

    private val _schoolAccount = MutableStateFlow<com.example.data.models.SchoolAccount?>(null)
    val schoolAccount: StateFlow<com.example.data.models.SchoolAccount?> = _schoolAccount

    private val sharedPrefs = context.getSharedPreferences("scolapay_prefs", android.content.Context.MODE_PRIVATE)
    
    private val _schoolLogoBase64 = MutableStateFlow<String?>(
        sharedPrefs.getString("school_logo_base64", null)
    )
    val schoolLogoBase64: StateFlow<String?> = _schoolLogoBase64
    
    private val _selectedSchoolYear = MutableStateFlow<String>(
        sharedPrefs.getString("selected_school_year", "2024 - 2025") ?: "2024 - 2025"
    )
    val selectedSchoolYear: StateFlow<String> = _selectedSchoolYear

    private val _deletionRequests = MutableStateFlow<List<DeletionRequest>>(emptyList())
    val deletionRequests: StateFlow<List<DeletionRequest>> = _deletionRequests

    private val _classFees = MutableStateFlow<List<ClassFee>>(emptyList())
    val classFees: StateFlow<List<ClassFee>> = _classFees

    fun setSelectedSchoolYear(year: String) {
        _selectedSchoolYear.value = year
        sharedPrefs.edit().putString("selected_school_year", year).apply()
    }

    private fun isTimestampInSchoolYear(timestamp: Long, schoolYear: String): Boolean {
        if (schoolYear == "Toutes les années") return true
        
        val parts = schoolYear.split("-").map { it.trim() }
        if (parts.size != 2) return true
        val startYear = parts[0].toIntOrNull() ?: return true
        val endYear = parts[1].toIntOrNull() ?: return true
        
        val cal = java.util.Calendar.getInstance()
        
        // Start timestamp: Sept 1st of startYear at 00:00:00.000
        cal.clear()
        cal.set(startYear, java.util.Calendar.SEPTEMBER, 1, 0, 0, 0)
        val startMs = cal.timeInMillis
        
        // End timestamp: Aug 31st of endYear at 23:59:59.999
        cal.clear()
        cal.set(endYear, java.util.Calendar.AUGUST, 31, 23, 59, 59)
        cal.set(java.util.Calendar.MILLISECOND, 999)
        val endMs = cal.timeInMillis
        
        return timestamp in startMs..endMs
    }

    private fun adjustTimestampToSchoolYear(timestamp: Long, schoolYear: String): Long {
        if (schoolYear == "Toutes les années") return timestamp
        val parts = schoolYear.split("-").map { it.trim() }
        if (parts.size != 2) return timestamp
        val startYear = parts[0].toIntOrNull() ?: return timestamp
        val endYear = parts[1].toIntOrNull() ?: return timestamp
        
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = timestamp
        
        val currentMonth = cal.get(java.util.Calendar.MONTH)
        val currentDay = cal.get(java.util.Calendar.DAY_OF_MONTH)
        val currentHour = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val currentMinute = cal.get(java.util.Calendar.MINUTE)
        val currentSecond = cal.get(java.util.Calendar.SECOND)
        val currentMs = cal.get(java.util.Calendar.MILLISECOND)
        
        cal.clear()
        val targetYear = if (currentMonth >= java.util.Calendar.SEPTEMBER) startYear else endYear
        
        cal.set(targetYear, currentMonth, currentDay, currentHour, currentMinute, currentSecond)
        cal.set(java.util.Calendar.MILLISECOND, currentMs)
        
        return cal.timeInMillis
    }

    private val activeListeners = mutableListOf<com.google.firebase.firestore.ListenerRegistration>()
    private var adminListener: com.google.firebase.firestore.ListenerRegistration? = null

    init {
        restoreSession()
    }

    private fun saveSession(email: String, role: String) {
        sharedPrefs.edit()
            .putString("last_email", email)
            .putString("last_role", role)
            .apply()
    }

    private fun clearSession() {
        sharedPrefs.edit()
            .remove("last_email")
            .remove("last_role")
            .apply()
    }

    private fun restoreSession() {
        val lastEmail = sharedPrefs.getString("last_email", null)
        val lastRole = sharedPrefs.getString("last_role", null)
        if (lastEmail != null && lastRole != null) {
            viewModelScope.launch {
                _userRole.value = lastRole
                if (lastRole == "ADMIN") {
                    try {
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(lastEmail, "Epbomibs5@").await()
                    } catch (e: Exception) {
                        android.util.Log.e("SchoolViewModel", "signInWithEmailAndPassword failed: ${e.message}", e)
                        try {
                            FirebaseAuth.getInstance().createUserWithEmailAndPassword(lastEmail, "Epbomibs5@").await()
                        } catch (e2: Exception) {
                            android.util.Log.e("SchoolViewModel", "createUserWithEmailAndPassword failed: ${e2.message}", e2)
                        }
                    }
                    if (FirebaseAuth.getInstance().currentUser == null) {
                        android.util.Log.e("SchoolViewModel", "currentUser is still null in init")
                        clearSession()
                        _userRole.value = null
                        return@launch
                    }
                    _currentSchoolId.value = -1
                    _schoolName.value = "Administrateur ScolaPay"
                    _schoolAccount.value = null
                } else {
                    syncAccount(lastEmail)
                }
            }
        } else {
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            if (currentUser != null && currentUser.email != null) {
                viewModelScope.launch {
                    _userRole.value = "FOUNDER"
                    syncAccount(currentUser.email!!)
                }
            }
        }
    }

    fun setSection(section: String) {
        _selectedSection.value = section
    }

    val students: StateFlow<List<Student>> = combine(_currentSchoolId, _selectedSection) { id, section ->
        Pair(id, section)
    }.flatMapLatest { (id, section) ->
        if (id == null) flowOf(emptyList())
        else if (section == "Toutes les sections") repository.getAllStudents(id)
        else repository.getAllStudents(id).map { list -> list.filter { it.section == section } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<Payment>> = combine(_currentSchoolId, _selectedSection, students, _selectedSchoolYear) { id, section, studentList, schoolYear ->
        if (id == null) flowOf(emptyList())
        else {
            val baseFlow = if (section == "Toutes les sections") repository.getAllPayments(id)
            else repository.getAllPayments(id).map { list -> 
                val studentIds = studentList.map { it.id }.toSet()
                list.filter { it.studentId in studentIds }
            }
            baseFlow.map { list ->
                list.filter { isTimestampInSchoolYear(it.date, schoolYear) }
            }
        }
    }.flatMapLatest { it }
     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCollected: StateFlow<Long?> = payments.map { list -> list.sumOf { it.amount }.takeIf { it > 0 } ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val expenses: StateFlow<List<Expense>> = combine(_currentSchoolId, _selectedSection, _selectedSchoolYear) { id, section, schoolYear ->
        if (id == null) flowOf(emptyList())
        else {
            val baseFlow = if (section == "Toutes les sections") repository.getAllExpenses(id)
            else repository.getAllExpenses(id).map { list -> list.filter { it.section == section } }
            baseFlow.map { list ->
                list.filter { isTimestampInSchoolYear(it.date, schoolYear) }
            }
        }
    }.flatMapLatest { it }
     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalExpenses: StateFlow<Long?> = expenses.map { list -> list.sumOf { it.amount }.takeIf { it > 0 } ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
        
    val hasActiveSubscription: StateFlow<Boolean> = _currentSchoolId
        .flatMapLatest { id -> if (id != null) repository.getSubscriptionStatus(id) else flowOf(false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
        
    val isPendingValidation: StateFlow<Boolean> = _currentSchoolId
        .flatMapLatest { id -> if (id != null) repository.getPendingValidationStatus(id) else flowOf(false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isTrialActive: StateFlow<Boolean> = schoolAccount.map { account ->
        if (account == null) false
        else {
            val elapsed = System.currentTimeMillis() - account.createdAt
            val trialDuration = 90L * 24L * 60L * 60L * 1000L // 3 months (90 days)
            elapsed < trialDuration
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val trialDaysRemaining: StateFlow<Long> = schoolAccount.map { account ->
        if (account == null) 0L
        else {
            val elapsed = System.currentTimeMillis() - account.createdAt
            val trialDuration = 90L * 24L * 60L * 60L * 1000L // 3 months (90 days)
            val remainingMs = trialDuration - elapsed
            (remainingMs / (24L * 60L * 60L * 1000L)).coerceAtLeast(0L)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val isAppAccessGranted: StateFlow<Boolean> = combine(hasActiveSubscription, isTrialActive) { hasSub, trialActive ->
        hasSub || trialActive
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val balance: StateFlow<Long> = combine(totalCollected, totalExpenses) { collected, expenses ->
        (collected ?: 0L) - (expenses ?: 0L)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun insertStudent(
        firstName: String,
        lastName: String,
        grade: String,
        section: String,
        parentWhatsApp: String? = null,
        registrationFee: Long = 0L,
        reenrollmentFee: Long = 0L
    ) {
        val schoolId = _currentSchoolId.value ?: return
        val email = sharedPrefs.getString("last_email", null) ?: return
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("schools").document(email).collection("students").document()
            val remoteId = docRef.id
            
            val studentData = hashMapOf(
                "firstName" to firstName,
                "lastName" to lastName,
                "grade" to grade,
                "section" to section,
                "parentWhatsApp" to (parentWhatsApp ?: ""),
                "registrationFee" to registrationFee,
                "reenrollmentFee" to reenrollmentFee
            )
            try {
                docRef.set(studentData)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            repository.insertStudent(
                Student(
                    schoolId = schoolId,
                    firstName = firstName,
                    lastName = lastName,
                    grade = grade,
                    section = section,
                    remoteId = remoteId,
                    parentWhatsApp = parentWhatsApp,
                    registrationFee = registrationFee,
                    reenrollmentFee = reenrollmentFee
                )
            )
        }
    }

    fun setClassFee(grade: String, amount: Long) {
        val normalizedGrade = normalizeGradeName(grade)
        val email = sharedPrefs.getString("last_email", null) ?: return
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            try {
                db.collection("schools").document(email)
                    .collection("classFees").document(normalizedGrade)
                    .set(hashMapOf("grade" to normalizedGrade, "feeAmount" to amount))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setSchoolLogo(base64: String?) {
        val email = sharedPrefs.getString("last_email", null) ?: return
        _schoolLogoBase64.value = base64
        sharedPrefs.edit().putString("school_logo_base64", base64).apply()
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            try {
                db.collection("schools").document(email)
                    .update("logoBase64", base64)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun createDeletionRequest(student: Student, reason: String) {
        val email = sharedPrefs.getString("last_email", null) ?: return
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("schools").document(email)
                .collection("deletionRequests").document()
            
            val requestData = hashMapOf(
                "studentRemoteId" to student.remoteId,
                "studentName" to "${student.firstName} ${student.lastName}",
                "grade" to student.grade,
                "section" to student.section,
                "reason" to reason,
                "requestedBy" to "Financier",
                "requestedAt" to System.currentTimeMillis()
            )
            try {
                docRef.set(requestData)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun approveDeletionRequest(request: DeletionRequest) {
        val email = sharedPrefs.getString("last_email", null) ?: return
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            val schoolRef = db.collection("schools").document(email)
            
            try {
                // 1. Delete student from Firestore
                schoolRef.collection("students").document(request.studentRemoteId).delete()
                
                // 2. Find and delete all payments for this student from Firestore
                val studentId = repository.getStudentIdByRemoteId(request.studentRemoteId)
                if (studentId != null) {
                    val schoolId = _currentSchoolId.value ?: -1
                    val allLocalPayments = repository.getAllPaymentsDirect(schoolId)
                    val studentPayments = allLocalPayments.filter { it.studentId == studentId }
                    for (payment in studentPayments) {
                        if (payment.remoteId.isNotEmpty()) {
                            schoolRef.collection("payments").document(payment.remoteId).delete()
                        }
                    }
                }
                
                // 3. Delete the request itself
                schoolRef.collection("deletionRequests").document(request.id).delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun rejectDeletionRequest(request: DeletionRequest) {
        val email = sharedPrefs.getString("last_email", null) ?: return
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            try {
                db.collection("schools").document(email)
                    .collection("deletionRequests").document(request.id).delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteStudentDirectly(student: Student) {
        val email = sharedPrefs.getString("last_email", null) ?: return
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            val schoolRef = db.collection("schools").document(email)
            
            try {
                // 1. Delete student from Firestore
                schoolRef.collection("students").document(student.remoteId).delete()
                
                // 2. Find and delete all payments for this student from Firestore
                val schoolId = _currentSchoolId.value ?: -1
                val allLocalPayments = repository.getAllPaymentsDirect(schoolId)
                val studentPayments = allLocalPayments.filter { it.studentId == student.id }
                for (payment in studentPayments) {
                    if (payment.remoteId.isNotEmpty()) {
                        schoolRef.collection("payments").document(payment.remoteId).delete()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun insertPayment(studentId: Int, amount: Long, reason: String, paymentMethod: String = "Espèces") {
        val schoolId = _currentSchoolId.value ?: return
        val email = sharedPrefs.getString("last_email", null) ?: return
        viewModelScope.launch {
            val student = repository.getAllStudentsDirect(schoolId).find { it.id == studentId }
            val studentRemoteId = student?.remoteId ?: ""
            
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("schools").document(email).collection("payments").document()
            val remoteId = docRef.id
            val date = adjustTimestampToSchoolYear(System.currentTimeMillis(), _selectedSchoolYear.value)
            
            val paymentData = hashMapOf(
                "studentRemoteId" to studentRemoteId,
                "amount" to amount,
                "reason" to reason,
                "date" to date,
                "paymentMethod" to paymentMethod
            )
            try {
                docRef.set(paymentData)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            repository.insertPayment(
                Payment(
                    schoolId = schoolId,
                    studentId = studentId,
                    amount = amount,
                    reason = reason,
                    date = date,
                    remoteId = remoteId,
                    paymentMethod = paymentMethod
                )
            )
        }
    }

    fun deletePayment(paymentId: Int) {
        val email = sharedPrefs.getString("last_email", null) ?: return
        viewModelScope.launch {
            val paymentsList = repository.getAllPaymentsDirect(_currentSchoolId.value ?: -1)
            val payment = paymentsList.find { it.id == paymentId }
            if (payment != null && payment.remoteId.isNotEmpty()) {
                val db = FirebaseFirestore.getInstance()
                try {
                    db.collection("schools").document(email)
                        .collection("payments").document(payment.remoteId).delete()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            repository.deletePayment(paymentId)
        }
    }

    fun insertExpense(amount: Long, category: String, description: String, section: String) {
        val schoolId = _currentSchoolId.value ?: return
        val email = sharedPrefs.getString("last_email", null) ?: return
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("schools").document(email).collection("expenses").document()
            val remoteId = docRef.id
            val date = adjustTimestampToSchoolYear(System.currentTimeMillis(), _selectedSchoolYear.value)
            
            val expenseData = hashMapOf(
                "amount" to amount,
                "section" to section,
                "reason" to description,
                "date" to date
            )
            try {
                docRef.set(expenseData)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            repository.insertExpense(
                Expense(
                    schoolId = schoolId,
                    amount = amount,
                    reason = description,
                    section = section,
                    date = date,
                    remoteId = remoteId
                )
            )
        }
    }
    
    fun deleteExpense(expenseId: Int) {
        val email = sharedPrefs.getString("last_email", null) ?: return
        viewModelScope.launch {
            val expensesList = repository.getAllExpensesDirect(_currentSchoolId.value ?: -1)
            val expense = expensesList.find { it.id == expenseId }
            if (expense != null && expense.remoteId.isNotEmpty()) {
                val db = FirebaseFirestore.getInstance()
                try {
                    db.collection("schools").document(email)
                        .collection("expenses").document(expense.remoteId).delete()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            repository.deleteExpense(expenseId)
        }
    }

    suspend fun hasAccount(): Boolean {
        return repository.hasAccount()
    }
    
    private fun startRealtimeSync(email: String, schoolId: Int) {
        android.util.Log.d("SchoolViewModel", "startRealtimeSync called for email: $email, schoolId: $schoolId")
        activeListeners.forEach { it.remove() }
        activeListeners.clear()

        val db = FirebaseFirestore.getInstance()
        val schoolDocRef = db.collection("schools").document(email)

        val schoolListener = schoolDocRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                android.util.Log.e("SchoolViewModel", "schoolListener error: ${error.message}", error)
                error.printStackTrace()
                return@addSnapshotListener
            }
            android.util.Log.d("SchoolViewModel", "schoolListener received snapshot")
            if (snapshot != null && snapshot.exists()) {
                val displayName = snapshot.getString("displayName") ?: ""
                val schoolName = snapshot.getString("schoolName") ?: email
                val hasActiveSubscription = snapshot.getBoolean("hasActiveSubscription") ?: false
                val isPendingValidation = snapshot.getBoolean("isPendingValidation") ?: false
                val paymentPhoneNumber = snapshot.getString("paymentPhoneNumber")
                val transactionId = snapshot.getString("transactionId")
                val rejectionReason = snapshot.getString("rejectionReason")
                val createdAt = snapshot.getLong("createdAt")
                val logoBase64 = snapshot.getString("logoBase64")
                
                _schoolLogoBase64.value = logoBase64
                sharedPrefs.edit().putString("school_logo_base64", logoBase64).apply()
                
                viewModelScope.launch {
                    val currentLocalAcc = repository.getSchoolAccountByName(email)
                    if (currentLocalAcc != null) {
                        val updated = currentLocalAcc.copy(
                            displayName = displayName,
                            schoolName = schoolName,
                            hasActiveSubscription = hasActiveSubscription,
                            isPendingValidation = isPendingValidation,
                            paymentPhoneNumber = paymentPhoneNumber,
                            transactionId = transactionId,
                            rejectionReason = rejectionReason,
                            createdAt = createdAt ?: currentLocalAcc.createdAt
                        )
                        repository.insertSchoolAccountDirect(updated)
                        _schoolAccount.value = updated
                        _schoolName.value = displayName.ifEmpty { schoolName }
                    }
                }
            }
        }
        activeListeners.add(schoolListener)

        val studentsListener = schoolDocRef.collection("students")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    error.printStackTrace()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    viewModelScope.launch {
                        for (change in snapshot.documentChanges) {
                            val doc = change.document
                            val remoteId = doc.id
                            
                            when (change.type) {
                                DocumentChange.Type.ADDED,
                                DocumentChange.Type.MODIFIED -> {
                                    val firstName = doc.getString("firstName") ?: ""
                                    val lastName = doc.getString("lastName") ?: ""
                                    var grade = doc.getString("grade") ?: ""
                                    var section = doc.getString("section") ?: "Non défini"
                                    val parentWhatsApp = doc.getString("parentWhatsApp")
                                    val regFee = doc.getLong("registrationFee") ?: 0L
                                    val reenrFee = doc.getLong("reenrollmentFee") ?: 0L
                                    
                                    var needsUpdate = false
                                    val normalizedGrade = normalizeGradeName(grade)
                                    if (normalizedGrade != grade) {
                                        grade = normalizedGrade
                                        needsUpdate = true
                                    }
                                    
                                    val primaryGrades = listOf("1ère Année", "2ème Année", "3ème Année", "4ème Année", "5ème Année", "6ème Année")
                                    if (section == "LA MATERNELLE" && primaryGrades.contains(grade)) {
                                        section = "LE PRIMAIRE"
                                        needsUpdate = true
                                    }
                                    
                                    if (needsUpdate) {
                                        val finalGrade = grade
                                        val finalSection = section
                                        viewModelScope.launch {
                                            try {
                                                doc.reference.update(
                                                    mapOf(
                                                        "grade" to finalGrade,
                                                        "section" to finalSection
                                                    )
                                                )
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                    
                                    val existingStudent = repository.getStudentByRemoteId(remoteId)
                                    if (existingStudent == null) {
                                        repository.insertStudent(
                                            Student(
                                                schoolId = schoolId,
                                                firstName = firstName,
                                                lastName = lastName,
                                                grade = grade,
                                                section = section,
                                                remoteId = remoteId,
                                                parentWhatsApp = parentWhatsApp,
                                                registrationFee = regFee,
                                                reenrollmentFee = reenrFee
                                            )
                                        )
                                    } else {
                                        if (existingStudent.firstName != firstName ||
                                            existingStudent.lastName != lastName ||
                                            existingStudent.grade != grade ||
                                            existingStudent.section != section ||
                                            existingStudent.parentWhatsApp != parentWhatsApp ||
                                            existingStudent.registrationFee != regFee ||
                                            existingStudent.reenrollmentFee != reenrFee) {
                                            repository.insertStudent(
                                                existingStudent.copy(
                                                    firstName = firstName,
                                                    lastName = lastName,
                                                    grade = grade,
                                                    section = section,
                                                    parentWhatsApp = parentWhatsApp,
                                                    registrationFee = regFee,
                                                    reenrollmentFee = reenrFee
                                                )
                                            )
                                        }
                                    }
                                }
                                DocumentChange.Type.REMOVED -> {
                                    repository.deleteStudentByRemoteId(remoteId)
                                }
                            }
                        }
                    }
                }
            }
        activeListeners.add(studentsListener)

        val paymentsListener = schoolDocRef.collection("payments")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    error.printStackTrace()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    viewModelScope.launch {
                        for (change in snapshot.documentChanges) {
                            val doc = change.document
                            val remoteId = doc.id
                            
                            when (change.type) {
                                DocumentChange.Type.ADDED,
                                DocumentChange.Type.MODIFIED -> {
                                    val studentRemoteId = doc.getString("studentRemoteId") ?: ""
                                    val amount = doc.getLong("amount") ?: 0L
                                    val date = doc.getLong("date") ?: System.currentTimeMillis()
                                    val reason = doc.getString("reason") ?: ""
                                    val paymentMethod = doc.getString("paymentMethod") ?: "Espèces"
                                    
                                    var localStudentId = repository.getStudentIdByRemoteId(studentRemoteId)
                                    if (localStudentId == null && studentRemoteId.isNotEmpty()) {
                                        try {
                                            val stuDoc = schoolDocRef.collection("students").document(studentRemoteId).get().await()
                                            if (stuDoc.exists()) {
                                                val firstName = stuDoc.getString("firstName") ?: ""
                                                val lastName = stuDoc.getString("lastName") ?: ""
                                                val grade = stuDoc.getString("grade") ?: ""
                                                val section = stuDoc.getString("section") ?: "Matin"
                                                val parentWhatsApp = stuDoc.getString("parentWhatsApp") ?: ""
                                                val regFee = stuDoc.getLong("registrationFee") ?: 0L
                                                val reenrFee = stuDoc.getLong("reenrollmentFee") ?: 0L
                                                repository.insertStudent(
                                                    com.example.data.models.Student(
                                                        schoolId = schoolId,
                                                        firstName = firstName,
                                                        lastName = lastName,
                                                        grade = grade,
                                                        section = section,
                                                        remoteId = studentRemoteId,
                                                        parentWhatsApp = parentWhatsApp,
                                                        registrationFee = regFee,
                                                        reenrollmentFee = reenrFee
                                                    )
                                                )
                                                localStudentId = repository.getStudentIdByRemoteId(studentRemoteId)
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                    if (localStudentId != null) {
                                        val existingPayment = repository.getPaymentByRemoteId(remoteId)
                                        if (existingPayment == null) {
                                            repository.insertPayment(
                                                Payment(
                                                    schoolId = schoolId,
                                                    studentId = localStudentId,
                                                    amount = amount,
                                                    date = date,
                                                    reason = reason,
                                                    remoteId = remoteId,
                                                    paymentMethod = paymentMethod
                                                )
                                            )
                                        } else {
                                            if (existingPayment.amount != amount ||
                                                existingPayment.reason != reason ||
                                                existingPayment.date != date ||
                                                existingPayment.studentId != localStudentId ||
                                                existingPayment.paymentMethod != paymentMethod) {
                                                repository.insertPayment(
                                                    existingPayment.copy(
                                                        amount = amount,
                                                        reason = reason,
                                                        date = date,
                                                        studentId = localStudentId,
                                                        paymentMethod = paymentMethod
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                                DocumentChange.Type.REMOVED -> {
                                    repository.deletePaymentByRemoteId(remoteId)
                                }
                            }
                        }
                    }
                }
            }
        activeListeners.add(paymentsListener)

        val expensesListener = schoolDocRef.collection("expenses")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    error.printStackTrace()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    viewModelScope.launch {
                        for (change in snapshot.documentChanges) {
                            val doc = change.document
                            val remoteId = doc.id
                            
                            when (change.type) {
                                DocumentChange.Type.ADDED,
                                DocumentChange.Type.MODIFIED -> {
                                    val amount = doc.getLong("amount") ?: 0L
                                    val date = doc.getLong("date") ?: System.currentTimeMillis()
                                    val reason = doc.getString("reason") ?: ""
                                    val section = doc.getString("section") ?: "Général"
                                    
                                    val existingExpense = repository.getExpenseByRemoteId(remoteId)
                                    if (existingExpense == null) {
                                        repository.insertExpense(
                                            Expense(
                                                schoolId = schoolId,
                                                amount = amount,
                                                date = date,
                                                reason = reason,
                                                section = section,
                                                remoteId = remoteId
                                            )
                                        )
                                    } else {
                                        if (existingExpense.amount != amount ||
                                            existingExpense.reason != reason ||
                                            existingExpense.date != date ||
                                            existingExpense.section != section) {
                                            repository.insertExpense(
                                                existingExpense.copy(
                                                    amount = amount,
                                                    reason = reason,
                                                    date = date,
                                                    section = section
                                                )
                                            )
                                        }
                                    }
                                }
                                DocumentChange.Type.REMOVED -> {
                                    repository.deleteExpenseByRemoteId(remoteId)
                                }
                            }
                        }
                    }
                }
            }
        activeListeners.add(expensesListener)

        val deletionRequestsListener = schoolDocRef.collection("deletionRequests")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    error.printStackTrace()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val requestsList = snapshot.documents.mapNotNull { doc ->
                        val studentRemoteId = doc.getString("studentRemoteId") ?: return@mapNotNull null
                        val studentName = doc.getString("studentName") ?: ""
                        val grade = doc.getString("grade") ?: ""
                        val section = doc.getString("section") ?: ""
                        val reason = doc.getString("reason") ?: ""
                        val requestedBy = doc.getString("requestedBy") ?: ""
                        val requestedAt = doc.getLong("requestedAt") ?: 0L
                        DeletionRequest(
                            id = doc.id,
                            studentRemoteId = studentRemoteId,
                            studentName = studentName,
                            grade = grade,
                            section = section,
                            reason = reason,
                            requestedBy = requestedBy,
                            requestedAt = requestedAt
                        )
                    }
                    _deletionRequests.value = requestsList
                }
            }
        activeListeners.add(deletionRequestsListener)

        val classFeesListener = schoolDocRef.collection("classFees")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    error.printStackTrace()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val feesList = snapshot.documents.mapNotNull { doc ->
                        var grade = doc.getString("grade") ?: doc.id
                        val feeAmount = doc.getLong("feeAmount") ?: 0L
                        
                        val normalizedGrade = normalizeGradeName(grade)
                        if (normalizedGrade != grade) {
                            val finalGrade = normalizedGrade
                            viewModelScope.launch {
                                try {
                                    schoolDocRef.collection("classFees").document(finalGrade)
                                        .set(hashMapOf("grade" to finalGrade, "feeAmount" to feeAmount))
                                    doc.reference.delete()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            grade = normalizedGrade
                        }
                        
                        ClassFee(grade = grade, feeAmount = feeAmount)
                    }
                    _classFees.value = feesList.distinctBy { it.grade }
                }
            }
        activeListeners.add(classFeesListener)
    }

    private suspend fun syncAccount(email: String) {
        var account = repository.getSchoolAccountByName(email)
        if (account == null) {
            try {
                val db = FirebaseFirestore.getInstance()
                val doc = db.collection("schools").document(email).get().await()
                if (doc.exists()) {
                    val schoolName = doc.getString("schoolName") ?: email
                    val passwordHash = doc.getString("passwordHash") ?: "dummy"
                    val financierPasswordHash = doc.getString("financierPasswordHash") ?: ""
                    val displayName = doc.getString("displayName") ?: ""
                    val hasActiveSubscription = doc.getBoolean("hasActiveSubscription") ?: false
                    val isPendingValidation = doc.getBoolean("isPendingValidation") ?: false
                    val paymentPhoneNumber = doc.getString("paymentPhoneNumber")
                    val transactionId = doc.getString("transactionId")
                    val rejectionReason = doc.getString("rejectionReason")
                    val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    val logoBase64 = doc.getString("logoBase64")
                    val address = doc.getString("address") ?: ""
                    val founderPhone = doc.getString("founderPhone") ?: ""
                    
                    _schoolLogoBase64.value = logoBase64
                    sharedPrefs.edit().putString("school_logo_base64", logoBase64).apply()
 
                    val newAcc = com.example.data.models.SchoolAccount(
                        schoolName = schoolName,
                        passwordHash = passwordHash,
                        financierPasswordHash = financierPasswordHash,
                        displayName = displayName,
                        hasActiveSubscription = hasActiveSubscription,
                        isPendingValidation = isPendingValidation,
                        paymentPhoneNumber = paymentPhoneNumber,
                        transactionId = transactionId,
                        rejectionReason = rejectionReason,
                        createdAt = createdAt,
                        address = address,
                        founderPhone = founderPhone
                    )
                    repository.insertSchoolAccountDirect(newAcc)
                    account = repository.getSchoolAccountByName(email)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        if (account == null) {
            repository.registerSchool(email, "dummy", "dummy")
            account = repository.getSchoolAccountByName(email)
        }
        
        if (account != null) {
            val auth = FirebaseAuth.getInstance()
            val resolvedRole = _userRole.value ?: "FOUNDER"
            val targetEmail = if (resolvedRole == "FINANCIER") email.replace("@", "+financier@") else email
            val targetPassword = if (resolvedRole == "FINANCIER") account.financierPasswordHash else account.passwordHash
            val currentEmail = auth.currentUser?.email
            if (currentEmail == null || currentEmail != targetEmail) {
                try {
                    auth.signInWithEmailAndPassword(targetEmail, targetPassword).await()
                } catch (e: Exception) {
                    try {
                        if (resolvedRole != "FINANCIER") {
                            auth.createUserWithEmailAndPassword(targetEmail, account.passwordHash).await()
                        } else {
                            auth.signInWithEmailAndPassword(email, account.passwordHash).await()
                        }
                    } catch (e2: Exception) {
                        e2.printStackTrace()
                    }
                }
            }
            
            _currentSchoolId.value = account.id
            _schoolName.value = account.displayName.ifEmpty { account.schoolName }
            _schoolAccount.value = account
            saveSession(email, resolvedRole)
            startRealtimeSync(email, account.id)
        }
    }

    suspend fun registerSchool(email: String, founderPassword: String, financierPassword: String, displayName: String, address: String = "", founderPhone: String = ""): Boolean {
        val cleanEmail = email.trim().lowercase()
        return try {
            val auth = FirebaseAuth.getInstance()
            val result = auth.createUserWithEmailAndPassword(cleanEmail, founderPassword).await()
            if (result.user != null) {
                _userRole.value = "FOUNDER"
                
                // Create financier sub-account in Firebase Auth using the +financier notation
                try {
                    val financierEmail = cleanEmail.replace("@", "+financier@")
                    auth.createUserWithEmailAndPassword(financierEmail, financierPassword).await()
                    // After creating the financier user, we are logged in as them, so sign out and sign back in as Founder
                    auth.signOut()
                    auth.signInWithEmailAndPassword(cleanEmail, founderPassword).await()
                } catch (fe: Exception) {
                    android.util.Log.e("SchoolViewModel", "Failed to register financier sub-account in Auth: ${fe.message}", fe)
                }

                val db = FirebaseFirestore.getInstance()
                val schoolData = hashMapOf(
                    "schoolName" to cleanEmail,
                    "passwordHash" to founderPassword,
                    "financierPasswordHash" to financierPassword,
                    "displayName" to displayName,
                    "hasActiveSubscription" to false,
                    "isPendingValidation" to false,
                    "createdAt" to System.currentTimeMillis(),
                    "address" to address,
                    "founderPhone" to founderPhone
                )
                db.collection("schools").document(cleanEmail).set(schoolData).await()
                
                repository.registerSchool(cleanEmail, founderPassword, financierPassword, displayName, address, founderPhone)
                val account = repository.getSchoolAccountByName(cleanEmail)
                if (account != null) {
                    _currentSchoolId.value = account.id
                    _schoolName.value = account.displayName.ifEmpty { account.schoolName }
                    saveSession(cleanEmail, "FOUNDER")
                    startRealtimeSync(cleanEmail, account.id)
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private suspend fun syncFinancierAuthAccount(schoolEmail: String, founderPassword: String, financierPassword: String) {
        val auth = FirebaseAuth.getInstance()
        val currentAuthUser = auth.currentUser
        val currentEmail = currentAuthUser?.email
        
        val cleanEmail = schoolEmail.trim().lowercase()
        val financierEmail = cleanEmail.replace("@", "+financier@")
        
        try {
            // Try to sign in as financier to see if credentials need updating
            try {
                auth.signOut()
                auth.signInWithEmailAndPassword(financierEmail, financierPassword).await()
                // Sign in succeeded, so credentials are correct! We can optionally update it to be safe
                auth.currentUser?.updatePassword(financierPassword)?.await()
            } catch (signInEx: Exception) {
                // Sign in failed, user might not exist, let's create them!
                try {
                    auth.createUserWithEmailAndPassword(financierEmail, financierPassword).await()
                } catch (createEx: Exception) {
                    android.util.Log.e("SchoolViewModel", "syncFinancierAuthAccount: failed to create financier auth: ${createEx.message}", createEx)
                }
            }
            
            // Sign back in as the original user (Founder)
            auth.signOut()
            if (currentEmail != null && currentEmail == cleanEmail) {
                auth.signInWithEmailAndPassword(cleanEmail, founderPassword).await()
            }
        } catch (e: Exception) {
            android.util.Log.e("SchoolViewModel", "syncFinancierAuthAccount error: ${e.message}", e)
            // Restore founder login if possible
            try {
                auth.signOut()
                if (currentEmail != null && currentEmail == cleanEmail) {
                    auth.signInWithEmailAndPassword(cleanEmail, founderPassword).await()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    suspend fun login(email: String, rawPassword: String): Boolean {
        _loginError.value = null
        android.util.Log.d("LOGIN_ATTEMPT", "Email: '${email.trim()}' | Password: '${rawPassword.trim()}'")
        val password = rawPassword.trim()
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail == "benjamintolno7@gmail.com") {
            try {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(cleanEmail, password).await()
            } catch (e: Exception) {
                _loginError.value = "Sign in failed: ${e.message}"
                try {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(cleanEmail, password).await()
                    _loginError.value = null // Success
                } catch (e2: Exception) {
                    _loginError.value = "Create failed: ${e2.message} (Sign in: ${e.message})"
                    return false
                }
            }
            
            if (FirebaseAuth.getInstance().currentUser == null) {
                return false
            }
            
            _userRole.value = "ADMIN"
            _currentSchoolId.value = -1
            _schoolName.value = "Administrateur ScolaPay"
            _schoolAccount.value = null
            saveSession(cleanEmail, "ADMIN")
            return true
        }

        return try {
            val auth = FirebaseAuth.getInstance()
            
            // 1. Try direct Firebase Auth sign-in with cleanEmail & password (works for Founder)
            var signedInAsFounder = false
            try {
                auth.signOut()
                val res = auth.signInWithEmailAndPassword(cleanEmail, password).await()
                if (res.user != null) {
                    signedInAsFounder = true
                }
            } catch (e: Exception) {
                // Not the founder, or invalid founder password, or network issue
            }

            // 2. Try direct Firebase Auth sign-in as Financier (cleanEmail with +financier & password)
            var signedInAsFinancier = false
            val financierEmail = cleanEmail.replace("@", "+financier@")
            if (!signedInAsFounder) {
                try {
                    auth.signOut()
                    val res = auth.signInWithEmailAndPassword(financierEmail, password).await()
                    if (res.user != null) {
                        signedInAsFinancier = true
                    }
                } catch (e: Exception) {
                    // Not the financier, or invalid financier password, or user doesn't exist
                }
            }

            // If we successfully logged in using either role, we are authenticated online!
            if (signedInAsFounder || signedInAsFinancier) {
                val resolvedRole = if (signedInAsFinancier) "FINANCIER" else "FOUNDER"
                
                // Read the Firestore document securely to sync details
                val db = FirebaseFirestore.getInstance()
                val doc = db.collection("schools").document(cleanEmail).get().await()
                if (doc.exists()) {
                    val dbSchoolName = doc.getString("schoolName") ?: cleanEmail
                    val dbFounderPassword = doc.getString("passwordHash") ?: ""
                    val dbFinancierPassword = doc.getString("financierPasswordHash") ?: ""
                    val dbDisplayName = doc.getString("displayName") ?: ""
                    val dbHasActiveSubscription = doc.getBoolean("hasActiveSubscription") ?: false
                    val dbIsPendingValidation = doc.getBoolean("isPendingValidation") ?: false
                    val dbPaymentPhoneNumber = doc.getString("paymentPhoneNumber")
                    val dbTransactionId = doc.getString("transactionId")
                    val dbRejectionReason = doc.getString("rejectionReason")
                    val dbCreatedAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    val dbAddress = doc.getString("address") ?: ""
                    val dbFounderPhone = doc.getString("founderPhone") ?: ""

                    // Save/update school account locally in Room
                    val newAcc = com.example.data.models.SchoolAccount(
                        schoolName = dbSchoolName,
                        passwordHash = dbFounderPassword,
                        financierPasswordHash = dbFinancierPassword,
                        displayName = dbDisplayName,
                        hasActiveSubscription = dbHasActiveSubscription,
                        isPendingValidation = dbIsPendingValidation,
                        paymentPhoneNumber = dbPaymentPhoneNumber,
                        transactionId = dbTransactionId,
                        rejectionReason = dbRejectionReason,
                        createdAt = dbCreatedAt,
                        address = dbAddress,
                        founderPhone = dbFounderPhone
                    )
                    repository.insertSchoolAccountDirect(newAcc)
                    
                    // If we logged in as Founder, let's make sure the financier Auth sub-account is synced
                    if (signedInAsFounder && dbFinancierPassword.isNotEmpty()) {
                        syncFinancierAuthAccount(cleanEmail, dbFounderPassword, dbFinancierPassword)
                    }
                }

                // Retrieve account from Room to set StateFlow values
                val account = repository.getSchoolAccountByName(cleanEmail)
                if (account != null) {
                    _userRole.value = resolvedRole
                    _currentSchoolId.value = account.id
                    _schoolName.value = account.displayName.ifEmpty { account.schoolName }
                    _schoolAccount.value = account
                    saveSession(cleanEmail, resolvedRole)
                    startRealtimeSync(cleanEmail, account.id)
                    return true
                }
            }

            // 3. Fallback: If both direct Firebase logins failed, let's see if we can do an online fetch
            // using anonymous sign-in or unauthenticated read.
            var firestoreAccountFound = false
            var dbFounderPassword = ""
            var dbFinancierPassword = ""
            var dbSchoolName = ""
            var dbDisplayName = ""
            var dbHasActiveSubscription = false
            var dbIsPendingValidation = false
            var dbPaymentPhoneNumber: String? = null
            var dbTransactionId: String? = null
            var dbRejectionReason: String? = null
            var dbCreatedAt: Long? = null
            var dbAddress = ""
            var dbFounderPhone = ""

            try {
                if (auth.currentUser == null) {
                    try {
                        auth.signInAnonymously().await()
                    } catch (e: Exception) {
                        android.util.Log.e("SchoolViewModel", "signInAnonymously failed: ${e.message}")
                    }
                }
                val db = FirebaseFirestore.getInstance()
                val doc = db.collection("schools").document(cleanEmail).get().await()
                if (doc.exists()) {
                    firestoreAccountFound = true
                    dbFounderPassword = doc.getString("passwordHash") ?: ""
                    dbFinancierPassword = doc.getString("financierPasswordHash") ?: ""
                    dbSchoolName = doc.getString("schoolName") ?: cleanEmail
                    dbDisplayName = doc.getString("displayName") ?: ""
                    dbHasActiveSubscription = doc.getBoolean("hasActiveSubscription") ?: false
                    dbIsPendingValidation = doc.getBoolean("isPendingValidation") ?: false
                    dbPaymentPhoneNumber = doc.getString("paymentPhoneNumber")
                    dbTransactionId = doc.getString("transactionId")
                    dbRejectionReason = doc.getString("rejectionReason")
                    dbCreatedAt = doc.getLong("createdAt")
                    dbAddress = doc.getString("address") ?: ""
                    dbFounderPhone = doc.getString("founderPhone") ?: ""
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            if (firestoreAccountFound) {
                if (password == dbFounderPassword || (dbFinancierPassword.isNotEmpty() && password == dbFinancierPassword)) {
                    val resolvedRole = if (password == dbFinancierPassword) "FINANCIER" else "FOUNDER"
                    
                    auth.signOut()
                    // If they are Financier, first try to sign them in directly using financierEmail.
                    // If that fails, we sign in as Founder and sync the Financier account.
                    val loginSuccess = if (resolvedRole == "FINANCIER") {
                        try {
                            auth.signInWithEmailAndPassword(financierEmail, password).await()
                            true
                        } catch (e: Exception) {
                            try {
                                auth.signInWithEmailAndPassword(cleanEmail, dbFounderPassword).await()
                                syncFinancierAuthAccount(cleanEmail, dbFounderPassword, dbFinancierPassword)
                                true
                            } catch (e2: Exception) {
                                false
                            }
                        }
                    } else {
                        try {
                            auth.signInWithEmailAndPassword(cleanEmail, dbFounderPassword).await()
                            if (dbFinancierPassword.isNotEmpty()) {
                                syncFinancierAuthAccount(cleanEmail, dbFounderPassword, dbFinancierPassword)
                            }
                            true
                        } catch (e: Exception) {
                            false
                        }
                    }

                    if (loginSuccess && auth.currentUser != null) {
                        val newAcc = com.example.data.models.SchoolAccount(
                            schoolName = dbSchoolName,
                            passwordHash = dbFounderPassword,
                            financierPasswordHash = dbFinancierPassword,
                            displayName = dbDisplayName,
                            hasActiveSubscription = dbHasActiveSubscription,
                            isPendingValidation = dbIsPendingValidation,
                            paymentPhoneNumber = dbPaymentPhoneNumber,
                            transactionId = dbTransactionId,
                            rejectionReason = dbRejectionReason,
                            createdAt = dbCreatedAt ?: System.currentTimeMillis(),
                            address = dbAddress,
                            founderPhone = dbFounderPhone
                        )
                        repository.insertSchoolAccountDirect(newAcc)
                        val account = repository.getSchoolAccountByName(cleanEmail)

                        if (account != null) {
                            _userRole.value = resolvedRole
                            _currentSchoolId.value = account.id
                            _schoolName.value = account.displayName.ifEmpty { account.schoolName }
                            _schoolAccount.value = account
                            saveSession(cleanEmail, resolvedRole)
                            startRealtimeSync(cleanEmail, account.id)
                            return true
                        }
                    }
                }
            }

            // 4. Local database check (perfect for Offline/cached accounts)
            var account = repository.getSchoolAccountByName(cleanEmail)
            if (account != null) {
                if (password == account.passwordHash) {
                    try {
                        auth.signOut()
                        auth.signInWithEmailAndPassword(cleanEmail, account.passwordHash).await()
                    } catch (e: Exception) { e.printStackTrace() }
                    _userRole.value = "FOUNDER"
                    _currentSchoolId.value = account.id
                    _schoolName.value = account.displayName.ifEmpty { account.schoolName }
                    _schoolAccount.value = account
                    saveSession(cleanEmail, "FOUNDER")
                    startRealtimeSync(cleanEmail, account.id)
                    return true
                } else if (password == account.financierPasswordHash) {
                    try {
                        auth.signOut()
                        auth.signInWithEmailAndPassword(financierEmail, account.financierPasswordHash).await()
                    } catch (e: Exception) {
                        try {
                            auth.signInWithEmailAndPassword(cleanEmail, account.passwordHash).await()
                        } catch (e2: Exception) { e2.printStackTrace() }
                    }
                    _userRole.value = "FINANCIER"
                    _currentSchoolId.value = account.id
                    _schoolName.value = account.displayName.ifEmpty { account.schoolName }
                    _schoolAccount.value = account
                    saveSession(cleanEmail, "FINANCIER")
                    startRealtimeSync(cleanEmail, account.id)
                    return true
                }
            }

            _loginError.value = "Identifiants incorrects ou problème de connexion"
            false
        } catch (e: Exception) {
            e.printStackTrace()
            _loginError.value = "Erreur: ${e.message}"
            
            // 5. Offline Fallback in Catch Block
            val account = repository.getSchoolAccountByName(cleanEmail)
            if (account != null) {
                if (password == account.passwordHash) {
                    _userRole.value = "FOUNDER"
                    _currentSchoolId.value = account.id
                    _schoolName.value = account.displayName.ifEmpty { account.schoolName }
                    _schoolAccount.value = account
                    saveSession(cleanEmail, "FOUNDER")
                    startRealtimeSync(cleanEmail, account.id)
                    return true
                } else if (password == account.financierPasswordHash) {
                    _userRole.value = "FINANCIER"
                    _currentSchoolId.value = account.id
                    _schoolName.value = account.displayName.ifEmpty { account.schoolName }
                    _schoolAccount.value = account
                    saveSession(cleanEmail, "FINANCIER")
                    startRealtimeSync(cleanEmail, account.id)
                    return true
                }
            }
            false
        }
    }

    fun logout() {
        activeListeners.forEach { it.remove() }
        activeListeners.clear()
        adminListener?.remove()
        adminListener = null
        _currentSchoolId.value = null
        _schoolName.value = null
        _schoolLogoBase64.value = null
        _userRole.value = null
        sharedPrefs.edit().remove("school_logo_base64").apply()
        clearSession()
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun activateSubscription() {
        val schoolId = _currentSchoolId.value ?: return
        val email = sharedPrefs.getString("last_email", null) ?: return
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            try {
                db.collection("schools").document(email).update(
                    mapOf(
                        "hasActiveSubscription" to true,
                        "isPendingValidation" to false
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            repository.activateSubscription(schoolId)
        }
    }
    
    fun submitSubscriptionRequest(phoneNumber: String, transactionId: String) {
        val schoolId = _currentSchoolId.value ?: return
        val email = sharedPrefs.getString("last_email", null) ?: return
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            try {
                db.collection("schools").document(email).update(
                    mapOf(
                        "isPendingValidation" to true,
                        "paymentPhoneNumber" to phoneNumber,
                        "transactionId" to transactionId,
                        "rejectionReason" to null
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            repository.submitSubscriptionRequest(schoolId, phoneNumber, transactionId)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Boolean {
        return try {
            val auth = FirebaseAuth.getInstance()
            auth.sendPasswordResetEmail(email).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun updateFinancierPassword(newPassword: String) {
        val email = sharedPrefs.getString("last_email", null) ?: return
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            try {
                db.collection("schools").document(email).update("financierPasswordHash", newPassword).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val currentAcc = repository.getSchoolAccountByName(email)
            if (currentAcc != null) {
                val updated = currentAcc.copy(financierPasswordHash = newPassword)
                repository.insertSchoolAccountDirect(updated)
                _schoolAccount.value = updated
                
                // Sync Firebase Auth Financier Account!
                syncFinancierAuthAccount(email, currentAcc.passwordHash, newPassword)
            }
        }
    }

    private val _adminSchools = MutableStateFlow<List<SchoolAdminItem>>(emptyList())
    val adminSchools: StateFlow<List<SchoolAdminItem>> = _adminSchools

    private val _adminError = MutableStateFlow<String?>(null)
    val adminError: StateFlow<String?> = _adminError

    fun loadAdminSchools() {
        adminListener?.remove()
        _adminError.value = null
        val db = FirebaseFirestore.getInstance()
        adminListener = db.collection("schools").addSnapshotListener { snapshot, error ->
            if (error != null) {
                error.printStackTrace()
                _adminError.value = "Erreur de chargement: ${error.message}"
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    val email = doc.id
                    if (email.trim().lowercase() == "benjamintolno7@gmail.com") return@mapNotNull null
                    
                    val schoolName = doc.getString("schoolName") ?: email
                    val displayName = doc.getString("displayName") ?: ""
                    val hasActiveSubscription = doc.getBoolean("hasActiveSubscription") ?: false
                    val isPendingValidation = doc.getBoolean("isPendingValidation") ?: false
                    val paymentPhoneNumber = doc.getString("paymentPhoneNumber")
                    val transactionId = doc.getString("transactionId")
                    val rejectionReason = doc.getString("rejectionReason")
                    val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    val address = doc.getString("address") ?: ""
                    val founderPhone = doc.getString("founderPhone") ?: ""
                    
                    SchoolAdminItem(
                        email = email,
                        schoolName = schoolName,
                        displayName = displayName,
                        hasActiveSubscription = hasActiveSubscription,
                        isPendingValidation = isPendingValidation,
                        paymentPhoneNumber = paymentPhoneNumber,
                        transactionId = transactionId,
                        rejectionReason = rejectionReason,
                        createdAt = createdAt,
                        address = address,
                        founderPhone = founderPhone
                    )
                }
                val sortedList = list.sortedByDescending { it.createdAt }
                _adminSchools.value = sortedList
            }
        }
    }

    fun approveSchoolSubscription(email: String) {
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            try {
                db.collection("schools").document(email).update(
                    mapOf(
                        "hasActiveSubscription" to true,
                        "isPendingValidation" to false,
                        "rejectionReason" to null
                    )
                ).await()
                loadAdminSchools()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteSchoolAccount(email: String) {
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            try {
                db.collection("schools").document(email).delete().await()
                loadAdminSchools()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun rejectSchoolSubscription(email: String, reason: String) {
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            try {
                db.collection("schools").document(email).update(
                    mapOf(
                        "hasActiveSubscription" to false,
                        "isPendingValidation" to false,
                        "rejectionReason" to reason
                    )
                ).await()
                loadAdminSchools()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

class SchoolViewModelFactory(
    private val repository: SchoolRepository,
    private val context: android.content.Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SchoolViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SchoolViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class SchoolAdminItem(
    val email: String,
    val schoolName: String,
    val displayName: String,
    val hasActiveSubscription: Boolean,
    val isPendingValidation: Boolean,
    val paymentPhoneNumber: String?,
    val transactionId: String?,
    val rejectionReason: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val address: String = "",
    val founderPhone: String = ""
)
