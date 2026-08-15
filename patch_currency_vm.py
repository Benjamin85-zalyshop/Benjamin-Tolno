import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

func = """
    fun updateCurrency(newCurrency: String) {
        val email = sharedPrefs.getString("last_email", null) ?: return
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            try {
                db.collection("schools").document(email).update("currency", newCurrency).await()
                val currentAcc = _schoolAccount.value
                if (currentAcc != null) {
                    val updated = currentAcc.copy(currency = newCurrency)
                    repository.insertSchoolAccountDirect(updated)
                    _schoolAccount.value = updated
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
"""

content = content.replace("fun updateFinancierPassword", func + "\n    fun updateFinancierPassword")

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
