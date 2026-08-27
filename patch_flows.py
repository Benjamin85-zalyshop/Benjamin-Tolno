import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r') as f:
    content = f.read()

old_flows = """    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
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
    
    val balance: StateFlow<Long> = combine(totalCollected, totalExpenses) { col, exp -> col - exp }.stateIn(viewModelScope, SharingStarted.Lazily, 0L)"""

new_flows = """    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
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
        _selectedSection
    ) { allExpenses, section ->
        allExpenses.filter { section == null || section == "Toutes les sections" || it.section == section }
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
    
    val totalCollected: StateFlow<Long> = payments.map { list -> list.sumOf { it.amount } }.stateIn(viewModelScope, SharingStarted.Lazily, 0L)
    
    val totalExpenses: StateFlow<Long> = expenses.map { list -> list.sumOf { it.amount } }.stateIn(viewModelScope, SharingStarted.Lazily, 0L)
    
    val balance: StateFlow<Long> = combine(totalCollected, totalExpenses) { col, exp -> col - exp }.stateIn(viewModelScope, SharingStarted.Lazily, 0L)"""

content = content.replace(old_flows, new_flows)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w') as f:
    f.write(content)

print("Flows patched!")
