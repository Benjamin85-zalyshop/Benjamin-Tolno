import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('dbCurrency = doc.getString("currency") ?: "GNF"\n                    val dbCurrency = doc.getString("currency") ?: "GNF"', 'dbCurrency = doc.getString("currency") ?: "GNF"')

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
