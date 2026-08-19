import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r') as f:
    content = f.read()

# Fix totalPoints math. If it's primary, we don't need to scale it to 20 just to scale it back down to 10.
# The `maxScore` for primary subjects is probably 10. We just do `score * coefficient`.
old_math = """                val grade = studentGrades.find { it.subjectId == subject.id }
                val score = grade?.evaluationScore ?: 0f
                totalCoef += subject.coefficient
                totalPoints += (score / subject.maxScore * 20f) * subject.coefficient"""

new_math = """                val grade = studentGrades.find { it.subjectId == subject.id }
                val score = grade?.evaluationScore ?: 0f
                totalCoef += subject.coefficient
                totalPoints += score * subject.coefficient"""

content = content.replace(old_math, new_math)

old_other_math = """                        otherTotalCoef += subject.coefficient
                        otherTotalPoints += (g.evaluationScore) / subject.maxScore * 20f * subject.coefficient"""

new_other_math = """                        otherTotalCoef += subject.coefficient
                        otherTotalPoints += g.evaluationScore * subject.coefficient"""

content = content.replace(old_other_math, new_other_math)

old_avg = """            val baseAverage20 = if (totalCoef > 0) totalPoints / totalCoef else 0f
            val isPrimary = student.section.contains("Primaire", ignoreCase = true) || student.section.contains("Maternelle", ignoreCase = true)
            val average = if (isPrimary) baseAverage20 / 2f else baseAverage20"""

new_avg = """            val average = if (totalCoef > 0) totalPoints / totalCoef else 0f"""

content = content.replace(old_avg, new_avg)

old_other_avg = """                val otherBase20 = if (otherTotalCoef > 0) otherTotalPoints / otherTotalCoef else 0f
                if (isPrimary) otherBase20 / 2f else otherBase20"""

new_other_avg = """                if (otherTotalCoef > 0) otherTotalPoints / otherTotalCoef else 0f"""

content = content.replace(old_other_avg, new_other_avg)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w') as f:
    f.write(content)

print("Average math patched!")
