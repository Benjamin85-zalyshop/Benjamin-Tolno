import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

target = """    fun setSchoolLogo(base64: String?) {
        val email = sharedPrefs.getString("last_email", null) ?: return
        _schoolLogoBase64.value = base64
        sharedPrefs.edit().putString("school_logo_base64", base64).apply()
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            try {
                db.collection("schools").document(email)
                    .update("logoBase64", base64)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }"""

replacement = """    fun setSchoolLogo(base64: String?) {
        val email = sharedPrefs.getString("last_email", null) ?: return
        _schoolLogoBase64.value = base64
        sharedPrefs.edit().putString("school_logo_base64", base64).apply()
        viewModelScope.launch {
            // Also update all students in RTDB (this is a bit heavy, but ensures it's instantly available)
            try {
                val database = FirebaseDatabase.getInstance("https://scolapay-b6289-default-rtdb.europe-west1.firebasedatabase.app")
                val students = repository.getAllStudentsDirect(_currentSchoolId.value)
                for (s in students) {
                    val matricule = if (!s.remoteId.isNullOrEmpty() && s.remoteId.length >= 5) s.remoteId.take(5).uppercase() else s.id.toString()
                    database.getReference("students").child(matricule).child("schoolLogo").setValue(base64 ?: "")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            val db = FirebaseFirestore.getInstance()
            try {
                db.collection("schools").document(email)
                    .update("logoBase64", base64)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
print("Updated setSchoolLogo")
