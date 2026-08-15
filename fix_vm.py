import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r') as f:
    lines = f.read().split('\n')

out = []
for line in lines:
    if line.startswith('data class SchoolAdminItem'):
        continue
    if "val adminError:" in line or "val adminSchools:" in line:
        continue # we will add them once
    out.append(line)

code = '\n'.join(out)
code = code.replace("import com.example.data.repository.SchoolRepository", "import com.example.data.repository.SchoolRepository\nimport androidx.lifecycle.ViewModelProvider\n\ndata class SchoolAdminItem(val email: String, val name: String, val phone: String, val status: String, val amount: Long, val date: String, val transactionId: String, val docId: String)")

code += """
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
"""

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w') as f:
    f.write(code)
