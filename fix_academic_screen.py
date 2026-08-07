with open("app/src/main/java/com/example/ui/screens/AcademicScreen.kt", "r") as f:
    content = f.read()

insert_point = "ReportCardPdfUtils.generateReportCardPdf("
insertion = "viewModel.syncStudentAcademicsToRTDB(student.schoolId, student.id, selectedTerm)\n            "

if "viewModel.syncStudentAcademicsToRTDB(student.schoolId, student.id, selectedTerm)" not in content:
    content = content.replace(insert_point, insertion + insert_point)

with open("app/src/main/java/com/example/ui/screens/AcademicScreen.kt", "w") as f:
    f.write(content)

