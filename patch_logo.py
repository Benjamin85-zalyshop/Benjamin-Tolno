import re
with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r') as f:
    content = f.read()

replacement = """    fun setSchoolLogo(base64: String?) {
        _schoolLogoBase64.value = base64
        val account = _schoolAccount.value ?: return
        viewModelScope.launch {
            val updated = account.copy(logoBase64 = base64)
            _schoolAccount.value = updated
            repository.updateSchoolAccount(updated)
            
            firestore.collection("schools").document(account.schoolName).update(
                "logoBase64", base64
            ).addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error updating logo", e) }
        }
    }"""

content = re.sub(
    r'    fun setSchoolLogo\(base64: String\?\) \{.*?\n    \}',
    replacement,
    content,
    flags=re.DOTALL
)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w') as f:
    f.write(content)
