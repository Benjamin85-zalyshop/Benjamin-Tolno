import os

filepath = 'app/src/main/java/com/example/ui/SchoolViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Find the admin login block
admin_start = content.find('if (email.trim().equals("benjamintolno7@gmail.com", ignoreCase = true) && pass == "Epbomibs5@") {')
admin_end = content.find('return true', admin_start)
admin_end = content.find('}', admin_end) + 1

new_admin_block = """if (email.trim().equals("benjamintolno7@gmail.com", ignoreCase = true) && pass == "Epbomibs5@") {
            val auth = FirebaseAuth.getInstance()
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
            } catch (e: Exception) {
                try {
                    auth.createUserWithEmailAndPassword(email, pass).await()
                } catch (e2: Exception) {
                    android.util.Log.e("AdminLogin", "Firebase Auth failed", e2)
                }
            }
            
            _userRole.value = "ADMIN"
            _schoolName.value = "ScolaPay Admin"
            _currentSchoolId.value = -1 
            sharedPrefs.edit().putString("logged_in_email", email.trim()).putString("logged_in_role", "ADMIN").apply()
            loadAdminSchools()
            return true
        }"""

content = content[:admin_start] + new_admin_block + content[admin_end:]

with open(filepath, 'w') as f:
    f.write(content)

