with open('app/src/main/java/com/example/data/repository/SchoolRepository.kt', 'r') as f:
    code = f.read()

code = code.replace("suspend fun getSchoolAccountByName", "suspend fun updateSchoolAccount(account: com.example.data.models.SchoolAccount) {\n        schoolDao.updateSchoolAccount(account)\n    }\n\n    suspend fun getSchoolAccountByName")

with open('app/src/main/java/com/example/data/repository/SchoolRepository.kt', 'w') as f:
    f.write(code)
