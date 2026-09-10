import re

filepath = 'app/src/main/java/com/example/ui/SchoolViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Replace the duplicated loginError that I added
old_block = """    val loginError = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)

    suspend fun login(email: String, pass: String): Boolean {"""

new_block = """    suspend fun login(email: String, pass: String): Boolean {"""

content = content.replace(old_block, new_block)

# Replace loginError.value = with _loginError.value = 
content = content.replace("loginError.value = ", "_loginError.value = ")

with open(filepath, 'w') as f:
    f.write(content)
