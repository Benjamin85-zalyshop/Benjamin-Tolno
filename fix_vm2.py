with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r') as f:
    code = f.read()

code = code.replace("    private val _adminError = MutableStateFlow<String?>(null)", "    private val _adminError = MutableStateFlow<String?>(null)\n    val adminError: StateFlow<String?> = _adminError")
code = code.replace("    private val _adminSchools = MutableStateFlow<List<SchoolAdminItem>>(emptyList())", "    private val _adminSchools = MutableStateFlow<List<SchoolAdminItem>>(emptyList())\n    val adminSchools: StateFlow<List<SchoolAdminItem>> = _adminSchools")

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w') as f:
    f.write(code)
