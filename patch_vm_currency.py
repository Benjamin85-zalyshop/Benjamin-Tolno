with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r') as f:
    code = f.read()

impl = """    fun updateCurrency(currency: String) {
        val account = _schoolAccount.value
        if (account != null) {
            val updated = account.copy(currency = currency)
            _schoolAccount.value = updated
            viewModelScope.launch {
                repository.updateSchoolAccount(updated)
            }
        }
    }"""

code = code.replace("    fun updateCurrency(currency: String) {}", impl)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w') as f:
    f.write(code)
