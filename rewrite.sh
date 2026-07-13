head -n 103 app/src/main/java/com/example/ui/SchoolViewModel.kt > tmp.kt
cat << 'INNER_EOF' >> tmp.kt
    fun insertExpense(amount: Long, reason: String, section: String) {
        val schoolId = _currentSchoolId.value ?: return
        viewModelScope.launch {
            repository.insertExpense(Expense(schoolId = schoolId, amount = amount, reason = reason, section = section))
        }
    }
INNER_EOF
tail -n +110 app/src/main/java/com/example/ui/SchoolViewModel.kt >> tmp.kt
mv tmp.kt app/src/main/java/com/example/ui/SchoolViewModel.kt
