import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace(
    'var dbFounderPhone = ""\n            var dbCurrency = "GNF"',
    'var dbFounderPhone = ""\n            var dbCurrency = "GNF"'
)
# Wait, let me grep for dbCurrency first.
