import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r') as f:
    content = f.read()

# Fix totalPoints calculation for the student in syncStudentAcademicsToRTDB
old_points = """                val grade = studentGrades.find { it.subjectId == subject.id }
                val score = grade?.evaluationScore ?: 0f
                totalCoef += subject.coefficient
                if (score > 0) {
                    totalPoints += (score / subject.maxScore) * 20f * subject.coefficient
                }"""

new_points = """                val grade = studentGrades.find { it.subjectId == subject.id }
                val score = grade?.evaluationScore ?: 0f
                val effectiveMax = subject.maxScore.takeIf { it > 0 } ?: 20f
                totalCoef += subject.coefficient
                if (grade != null && grade.evaluationScore != null) {
                    totalPoints += (score / effectiveMax) * 20f * subject.coefficient
                }"""

content = content.replace(old_points, new_points)

# Remove the incorrect average logic if it was using 10 scale or something
# The score should be normalized to 20 for average calculation, or left as is based on maxScore.
# Let's adjust how the average is formatted based on maxScore. If it's primary, max is 10.
# The calculation `totalPoints += (score / subject.maxScore) * 20f * subject.coefficient`
# scales everything to 20.

# Wait, if we scale everything to 20, but the web UI says "/ 10", then the average will look wrong (e.g., 16.57 / 10).
# Let's check the code that computes average based on section.
old_avg = """            val average = if (totalCoef > 0) totalPoints / totalCoef else 0f"""

new_avg = """            val baseAverage20 = if (totalCoef > 0) totalPoints / totalCoef else 0f
            val isPrimary = student.section.contains("Primaire", ignoreCase = true) || student.section.contains("Maternelle", ignoreCase = true)
            val average = if (isPrimary) baseAverage20 / 2f else baseAverage20"""

content = content.replace(old_avg, new_avg)

# Also fix the `otherStudent` average logic to match
old_other = """                if (otherTotalCoef > 0) otherTotalPoints / otherTotalCoef else 0f
            }.sortedDescending()"""

new_other = """                val otherBase20 = if (otherTotalCoef > 0) otherTotalPoints / otherTotalCoef else 0f
                if (isPrimary) otherBase20 / 2f else otherBase20
            }.sortedDescending()"""

content = content.replace(old_other, new_other)


with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w') as f:
    f.write(content)

print("Average calculation patched!")
