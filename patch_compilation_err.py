import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace(
    'var dbFounderPhone = ""',
    'var dbFounderPhone = ""\n            var dbCurrency = "GNF"'
)

content = content.replace(
    'dbFounderPhone = doc.getString("founderPhone") ?: ""',
    'dbFounderPhone = doc.getString("founderPhone") ?: ""\n                    dbCurrency = doc.getString("currency") ?: "GNF"'
)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
