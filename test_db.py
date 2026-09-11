import sqlite3

# wait, we can't test db directly easily.
# But wait, look at syncStudentAcademicsToRTDB
#             val allGrades = repository.getAllGradesDirect(schoolId).filter { it.term == term }
#             val studentGrades = allGrades.filter { it.studentId == studentId }
# If `term` doesn't exactly match `grade.term`, it will be empty!
# In AcademicScreen.kt:
# val terms = listOf("1er Trimestre", "2ème Trimestre", "3ème Trimestre")
# var selectedTerm by remember { mutableStateOf(terms[0]) }
# So term is "1er Trimestre"
# How is grade.term saved?
# In AcademicScreen.kt, when saving:
#     term = selectedTerm,
# So they match!
