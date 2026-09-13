import re

filepath = 'app/src/main/java/com/example/ui/SchoolViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Let's fix the login flow at init time as well
content = content.replace(
    '''            if (loggedInEmail != null && loggedInRole != null) {
                if (loggedInRole == "ADMIN" && loggedInEmail.equals("benjamintolno7@gmail.com", ignoreCase = true)) {
                    _userRole.value = "ADMIN"
                    _schoolName.value = "ScolaPay Admin"
                    _currentSchoolId.value = -1
                    val auth = FirebaseAuth.getInstance()
                    if (auth.currentUser?.email != "benjamintolno7@gmail.com") {
                        try {
                            auth.signInWithEmailAndPassword("benjamintolno7@gmail.com", "Epbomibs5@").await()
                        } catch (e: Exception) {
                            try {
                                auth.createUserWithEmailAndPassword("benjamintolno7@gmail.com", "Epbomibs5@").await()
                            } catch (e2: Exception) {}
                        }
                    }
                    loadAdminSchools()
                } else {''',
    '''            if (loggedInEmail != null && loggedInRole != null) {
                if (loggedInRole == "ADMIN" && loggedInEmail.equals("benjamintolno7@gmail.com", ignoreCase = true)) {
                    _userRole.value = "ADMIN"
                    _schoolName.value = "ScolaPay Admin"
                    _currentSchoolId.value = -1
                    val auth = FirebaseAuth.getInstance()
                    if (auth.currentUser?.email != "benjamintolno7@gmail.com") {
                        try {
                            auth.signInWithEmailAndPassword("benjamintolno7@gmail.com", "Epbomibs5@").await()
                        } catch (e: Exception) {
                            try {
                                auth.createUserWithEmailAndPassword("benjamintolno7@gmail.com", "Epbomibs5@").await()
                            } catch (e2: Exception) {}
                        }
                    }
                    loadAdminSchools()
                } else {'''
)

with open(filepath, 'w') as f:
    f.write(content)

