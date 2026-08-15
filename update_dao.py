with open('app/src/main/java/com/example/data/local/SchoolDao.kt', 'r') as f:
    code = f.read()

code = code.replace("import androidx.room.Insert", "import androidx.room.Insert\nimport androidx.room.Update")
code = code.replace("suspend fun insertSchoolAccount(account: com.example.data.models.SchoolAccount)", "suspend fun insertSchoolAccount(account: com.example.data.models.SchoolAccount)\n\n    @Update\n    suspend fun updateSchoolAccount(account: com.example.data.models.SchoolAccount)")

with open('app/src/main/java/com/example/data/local/SchoolDao.kt', 'w') as f:
    f.write(code)
