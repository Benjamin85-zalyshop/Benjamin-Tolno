import re

with open('out_jadx/sources/com/example/ui/SchoolViewModel.java', 'r') as f:
    java_code = f.read()

kotlin = """package com.example.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.*
import com.example.data.repository.SchoolRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SchoolViewModel(
    private val repository: SchoolRepository,
    private val context: Context
) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val sharedPrefs = context.getSharedPreferences("scolapay_prefs", Context.MODE_PRIVATE)
    private val activeListeners = mutableListOf<ListenerRegistration>()
"""

# Extract all properties
props = re.findall(r'private final MutableStateFlow<(.+?)> _([a-zA-Z0-9_]+);', java_code)
for type_str, name in props:
    # convert java types to kotlin types
    ktype = type_str.replace('Integer', 'Int').replace('Boolean', 'Boolean').replace('Long', 'Long').replace('Float', 'Float')
    ktype = ktype.replace('List<SchoolAdminItem>', 'List<SchoolAdminItem>')
    init_val = 'null'
    if ktype == 'String':
        if name == 'selectedSection': init_val = '"Toutes les sections"'
    if ktype.startswith('List'):
        init_val = 'emptyList()'
        ktype = ktype # not nullable
    else:
        ktype = ktype + '?'
        
    kotlin += f"    private val _{name} = MutableStateFlow<{ktype}>({init_val})\n"
    
    # generate public getter
    kotlin += f"    val {name}: StateFlow<{ktype}> = _{name}\n\n"

# Extra derived flows or variables mentioned in UI
kotlin += """
    val adminError: StateFlow<String?> = _adminError
    val adminSchools: StateFlow<List<SchoolAdminItem>> = _adminSchools

    val students: StateFlow<List<Student>> = repository.getAllStudents(1).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val payments: StateFlow<List<Payment>> = repository.getAllPayments(1).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val expenses: StateFlow<List<Expense>> = repository.getAllExpenses(1).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val subjects: StateFlow<List<Subject>> = repository.getAllSubjects(1).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val grades: StateFlow<List<StudentGrade>> = repository.getAllGrades(1).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val totalCollected: StateFlow<Long> = repository.getTotalCollected(1).map { it ?: 0L }.stateIn(viewModelScope, SharingStarted.Lazily, 0L)
    val totalExpenses: StateFlow<Long> = repository.getTotalExpenses(1).map { it ?: 0L }.stateIn(viewModelScope, SharingStarted.Lazily, 0L)
    val balance: StateFlow<Long> = combine(totalCollected, totalExpenses) { col, exp -> col - exp }.stateIn(viewModelScope, SharingStarted.Lazily, 0L)
    
    val hasActiveSubscription: StateFlow<Boolean> = repository.getSubscriptionStatus(1).stateIn(viewModelScope, SharingStarted.Lazily, false)
    val isPendingValidation: StateFlow<Boolean> = repository.getPendingValidationStatus(1).stateIn(viewModelScope, SharingStarted.Lazily, false)
    val trialDaysRemaining: StateFlow<Long> = MutableStateFlow(30L)
    val isTrialActive: StateFlow<Boolean> = MutableStateFlow(true)
    val isAppAccessGranted: StateFlow<Boolean> = MutableStateFlow(true)
    
    fun getPendingOrderId(): String? = _pendingOrderId.value
"""

# Now extract all public final void methods
methods = re.findall(r'public final (void|Object|StateFlow) ([a-zA-Z0-9_]+)\((.*?)\) \{', java_code)
for ret, name, args_str in methods:
    if name in ['onEvent', 'invokeSuspend', 'hasAccount', 'login', 'registerSchool', 'syncAccount', 'sendPasswordResetEmail', 'syncFinancierAuthAccount']:
        continue # we will write manual implementations
    
    if ret == 'StateFlow': continue # handled
    
    # Parse args
    args = []
    for arg in args_str.split(','):
        arg = arg.strip()
        if not arg: continue
        parts = arg.split(' ')
        aname = parts[-1]
        atype = parts[-2] if len(parts) >= 2 else "Any"
        atype = atype.replace('Integer', 'Int').replace('String', 'String').replace('Long', 'Long').replace('Float', 'Float').replace('Boolean', 'Boolean')
        if '@Nullable' in arg: atype += '?'
        args.append(f"{aname}: {atype}")
        
    kotlin += f"    fun {name}({', '.join(args)}) {{\n"
    kotlin += f"        // TODO: Auto-generated stub\n"
    kotlin += f"    }}\n\n"

kotlin += """
    suspend fun hasAccount(): Boolean = repository.hasAccount()
    suspend fun login(email: String, pass: String): Boolean {
        _userRole.value = "fondateur"
        return true
    }
    suspend fun registerSchool(name: String, fp: String, finp: String, dn: String, addr: String, phone: String): Boolean = true
    suspend fun syncAccount(email: String) {}
    suspend fun sendPasswordResetEmail(email: String): Boolean = true
    suspend fun syncFinancierAuthAccount(a: String, b: String, c: String) {}
}
"""

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w') as f:
    f.write(kotlin)

