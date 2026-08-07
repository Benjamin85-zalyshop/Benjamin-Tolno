import re

# Fix DAO
with open("app/src/main/java/com/example/data/local/SchoolDao.kt", "r") as f:
    content = f.read()

dao_insert = """
    @Query("SELECT * FROM students WHERE schoolId = :schoolId")
    suspend fun getAllStudentsDirect(schoolId: Int): List<Student>

    @Query("SELECT * FROM subjects WHERE schoolId = :schoolId")
    suspend fun getAllSubjectsDirect(schoolId: Int): List<Subject>

    @Query("SELECT * FROM student_grades WHERE schoolId = :schoolId")
    suspend fun getAllGradesDirect(schoolId: Int): List<StudentGrade>
"""

content = re.sub(r'@Query\("SELECT \* FROM students WHERE schoolId = :schoolId"\)\s+suspend fun getAllStudentsDirect\(schoolId: Int\): List<Student>', dao_insert, content)

with open("app/src/main/java/com/example/data/local/SchoolDao.kt", "w") as f:
    f.write(content)


# Fix Repo
with open("app/src/main/java/com/example/data/repository/SchoolRepository.kt", "r") as f:
    content = f.read()

repo_insert = """
    suspend fun getAllStudentsDirect(schoolId: Int): List<Student> = schoolDao.getAllStudentsDirect(schoolId)
    suspend fun getAllSubjectsDirect(schoolId: Int): List<Subject> = schoolDao.getAllSubjectsDirect(schoolId)
    suspend fun getAllGradesDirect(schoolId: Int): List<StudentGrade> = schoolDao.getAllGradesDirect(schoolId)
"""

content = re.sub(r'suspend fun getAllStudentsDirect\(schoolId: Int\): List<Student> = schoolDao\.getAllStudentsDirect\(schoolId\)', repo_insert, content)

with open("app/src/main/java/com/example/data/repository/SchoolRepository.kt", "w") as f:
    f.write(content)

