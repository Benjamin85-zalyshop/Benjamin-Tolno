import re

filepath = 'app/src/main/java/com/example/ui/screens/DashboardScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

find_text = '''val DEFAULT_CLASSES_BY_SECTION = mapOf(
    "LA MATERNELLE" to listOf("Petite Section", "Moyenne Section", "Grande Section"),
    "LE PRIMAIRE" to listOf("1ère Année", "2ème Année", "3ème Année", "4ème Année", "5ème Année", "6ème Année"),
    "LE COLLÈGE" to listOf("7ème", "8ème", "9ème", "10ème"),
    "LE LYCÉE" to listOf("11ème Sciences Mathématiques", "11ème Sciences Expérimentales", "11ème Sciences Sociales", "12ème Sciences Mathématiques", "12ème Sciences Expérimentales", "12ème Sciences Sociales", "Terminale Sciences Mathématiques", "Terminale Sciences Expérimentales", "Terminale Sciences Sociales")
)'''

replace_text = '''val DEFAULT_CLASSES_BY_SECTION = mapOf(
    "LA MATERNELLE" to listOf("Petite Section", "Moyenne Section", "Grande Section"),
    "LE PRIMAIRE" to listOf("1ère Année", "2ème Année", "3ème Année", "4ème Année", "5ème Année", "6ème Année"),
    "LE COLLÈGE" to listOf("7ème", "8ème", "9ème", "10ème"),
    "LE LYCÉE" to listOf("11ème Sciences Mathématiques", "11ème Sciences Expérimentales", "11ème Sciences Sociales", "12ème Sciences Mathématiques", "12ème Sciences Expérimentales", "12ème Sciences Sociales", "Terminale Sciences Mathématiques", "Terminale Sciences Expérimentales", "Terminale Sciences Sociales"),
    "L'UNIVERSITÉ" to listOf("Licence 1", "Licence 2", "Licence 3", "Licence 4", "Master 1", "Master 2"),
    "L'ÉCOLE PROFESSIONNELLE" to listOf("1ère Année", "2ème Année", "3ème Année")
)'''

content = content.replace(find_text, replace_text)

with open(filepath, 'w') as f:
    f.write(content)

