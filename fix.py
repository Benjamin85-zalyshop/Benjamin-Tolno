with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if i >= 1150 and i <= 1160:
        if "val parsedSection =" in line:
            lines[i] = '                    val parsedSection = doc.getString("section") ?: ""\n'
        elif "val parsedMaxScore =" in line:
            lines[i] = '                    val parsedMaxScore = if (parsedSection == "LE PRIMAIRE" || parsedSection == "LA MATERNELLE") 10f else ((doc.get("maxScore") as? Number)?.toFloat() ?: 20f)\n'

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w') as f:
    f.writelines(lines)
