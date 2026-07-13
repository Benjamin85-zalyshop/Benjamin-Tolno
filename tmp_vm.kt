package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.models.Expense
import com.example.data.models.Payment
import com.example.data.models.Student
import com.example.data.repository.SchoolRepository
import com.google.firebase.auth.FirebaseAuth
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

@OptIn(ExperimentalCoroutinesApi::class)
class SchoolViewModel(private val repository: SchoolRepository) : ViewModel() {
    private val _currentSchoolId = MutableStateFlow<Int?>(null)
    private val _schoolName = MutableStateFlow<String?>(null)
    val schoolName: StateFlow<String?> = _schoolName
    
    private val _selectedSection = MutableStateFlow<String>("Toutes les sections")
    val selectedSection: StateFlow<String> = _selectedSection
    
    private val _userRole = MutableStateFlow<String?>("FOUNDER")
    val userRole: StateFlow<String?> = _userRole

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

    val payments: StateFlow<List<Payment>> = combine(_currentSchoolId, _selectedSection, students) { id, section, studentList ->
        Triple(id, section, studentList)
    }.flatMapLatest { (id, section, studentList) ->
        if (id == null) flowOf(emptyList())
        else if (section == "Toutes les sections") repository.getAllPayments(id)
        else repository.getAllPayments(id).map { list -> 
            val studentIds = studentList.map { it.id }.toSet()
            list.filter { it.studentId in studentIds }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCollected: StateFlow<Long?> = payments.map { list -> list.sumOf { it.amount }.takeIf { it > 0 } ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val expenses: StateFlow<List<Expense>> = combine(_currentSchoolId, _selectedSection) { id, section ->
        Pair(id, section)
    }.flatMapLatest { (id, section) ->
        if (id == null) flowOf(emptyList())
        else if (section == "Toutes les sections") repository.getAllExpenses(id)
        else repository.getAllExpenses(id).map { list -> list.filter { it.section == section } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalExpenses: StateFlow<Long?> = expenses.map { list -> list.sumOf { it.amount }.takeIf { it > 0 } ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
        
    val hasActiveSubscription: StateFlow<Boolean> = _currentSchoolId
        .flatMapLatest { id -> if (id != null) repository.getSubscriptionStatus(id) else flowOf(false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
        
    val isPendingValidation: StateFlow<Boolean> = _currentSchoolId
        .flatMapLatest { id -> if (id != null) repository.getPendingValidationStatus(id) else flowOf(false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val balance: StateFlow<Long> = combine(totalCollected, totalExpenses) { collected, expenses ->
        (collected ?: 0L) - (expenses ?: 0L)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun insertStudent(firstName: String, lastName: String, grade: String, section: String) {
        val schoolId = _currentSchoolId.value ?: return
        viewModelScope.launch {
            repository.insertStudent(Student(schoolId = schoolId, firstName = firstName, lastName = lastName, grade = grade, section = section))
        }
    }

    fun insertPayment(studentId: Int, amount: Long, reason: String) {
        val schoolId = _currentSchoolId.value ?: return
        viewModelScope.launch {
            repository.insertPayment(Payment(schoolId = schoolId, studentId = studentId, amount = amount, reason = reason))
        }
    }

    fun deletePayment(paymentId: Int) {
        viewModelScope.launch {
            repository.deletePayment(paymentId)
        }
    }

    fun insertExpense(amount: Long, reason: String, section: String) {
        val schoolId = _currentSchoolId.value ?: return
        viewModelScope.launch {
            repository.insertExpense(Expense(schoolId = schoolId, amount = amount, reason = reason, section = section))
        }
    }
    
    fun deleteExpense(expenseId: Int) {
        viewModelScope.launch {
            repository.deleteExpense(expenseId)
        }
    }

    suspend fun hasAccount(): Boolean {
        return repository.hasAccount()
    }
    
    private suspend fun syncAccount(email: String) {
        var account = repository.getSchoolAccountByName(email)
        if (account == null) {
            repository.registerSchool(email, "dummy", "dummy")
            account = repository.getSchoolAccountByName(email)
        }
        if (account != null) {
            _currentSchoolId.value = account.id
            _schoolName.value = account.schoolName
        }
    }

    suspend fun registerSchool(email: String, password: String): Boolean {
        return try {
            val auth = FirebaseAuth.getInstance()
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            if (result.user != null) {
                _userRole.value = "FOUNDER"
                syncAccount(email)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun login(email: String, password: String): Boolean {
        return try {
            val auth = FirebaseAuth.getInstance()
            val result = auth.signInWithEmailAndPassword(email, password).await()
            if (result.user != null) {
                _userRole.value = "FOUNDER"
                syncAccount(email)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun logout() {
        _currentSchoolId.value = null
        _schoolName.value = null
        _userRole.value = null
    }
    
    fun activateSubscription() {
        val schoolId = _currentSchoolId.value ?: return
        viewModelScope.launch {
            repository.activateSubscription(schoolId)
        }
    }
    
    fun submitSubscriptionRequest(phoneNumber: String, transactionId: String) {
        val schoolId = _currentSchoolId.value ?: return
        viewModelScope.launch {
            repository.submitSubscriptionRequest(schoolId, phoneNumber, transactionId)
        }
    }
}

class SchoolViewModelFactory(private val repository: SchoolRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SchoolViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SchoolViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
