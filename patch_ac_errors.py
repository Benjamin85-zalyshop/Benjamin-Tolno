with open('app/src/main/java/com/example/ui/screens/AcademicScreen.kt', 'r') as f:
    code = f.read()

code = code.replace("schoolYear = selectedSchoolYear,", 'schoolYear = selectedSchoolYear ?: "",')
code = code.replace("examScore = null\n", 'examScore = null,\n                                            comment = null\n')

with open('app/src/main/java/com/example/ui/screens/AcademicScreen.kt', 'w') as f:
    f.write(code)
