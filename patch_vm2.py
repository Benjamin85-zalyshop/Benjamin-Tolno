with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r') as f:
    code = f.read()

code = code.replace("import com.example.data.models.*", "import com.example.data.models.*\n\ndata class SchoolAdminItem(val email: String, val name: String, val phone: String, val status: String, val amount: Long, val date: String, val transactionId: String, val docId: String)")

# Also, some functions might have Kotlin primitive types mismatch
code = code.replace("function1: Function1<? super String", "onResult: (String) -> Unit")
code = code.replace("Unit>", "")

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w') as f:
    f.write(code)
