sed -i 's/suspend fun registerSchool(name: String, founderPassword: String, financierPassword: String) {/suspend fun registerSchool(email: String, password: String): Boolean {/g' app/src/main/java/com/example/ui/SchoolViewModel.kt

sed -i 's/        repository.registerSchool(name, founderPassword, financierPassword)/        return try {\n            val auth = FirebaseAuth.getInstance()\n            val result = auth.createUserWithEmailAndPassword(email, password).await()\n            if (result.user != null) {\n                _userRole.value = "FOUNDER"\n                _currentSchoolId.value = 1\n                _schoolName.value = "ScolaPay (Firebase)"\n                true\n            } else {\n                false\n            }\n        } catch (e: Exception) {\n            e.printStackTrace()\n            false\n        }/g' app/src/main/java/com/example/ui/SchoolViewModel.kt

sed -i '/\/\/ Automatically login after registration as founder/d' app/src/main/java/com/example/ui/SchoolViewModel.kt
sed -i '/login(name, founderPassword)/d' app/src/main/java/com/example/ui/SchoolViewModel.kt

