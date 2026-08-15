import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# in login function:
content = content.replace(
    'val dbFounderPhone = doc.getString("founderPhone") ?: ""',
    'val dbFounderPhone = doc.getString("founderPhone") ?: ""\n                    val dbCurrency = doc.getString("currency") ?: "GNF"'
)

content = content.replace(
    'founderPhone = dbFounderPhone\n                    )',
    'founderPhone = dbFounderPhone,\n                        currency = dbCurrency\n                    )'
)

content = content.replace(
    'founderPhone = dbFounderPhone\n                        )',
    'founderPhone = dbFounderPhone,\n                            currency = dbCurrency\n                        )'
)


with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
