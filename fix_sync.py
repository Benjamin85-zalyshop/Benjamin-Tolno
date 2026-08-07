with open("app/src/main/java/com/example/ui/SchoolViewModel.kt", "r") as f:
    content = f.read()

bad1 = "val subjectsList = subjects.value.filter { it.grade == student.grade && it.section == student.section }"
good1 = "val subjectsList = repository.getAllSubjectsDirect(schoolId).filter { it.grade == student.grade && it.section == student.section }"
content = content.replace(bad1, good1)

bad2 = "val allGradesForClass = grades.value.filter { g -> classStudents.any { it.id == g.studentId } && g.term == term }"
good2 = "val allGradesForClass = repository.getAllGradesDirect(schoolId).filter { g -> classStudents.any { it.id == g.studentId } && g.term == term }"
content = content.replace(bad2, good2)

bad3 = "private fun syncStudentAcademicsToRTDB"
good3 = "fun syncStudentAcademicsToRTDB"
content = content.replace(bad3, good3)

with open("app/src/main/java/com/example/ui/SchoolViewModel.kt", "w") as f:
    f.write(content)

