import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace(
    'updates["schoolPhone"] = acc.founderPhone',
    'updates["schoolPhone"] = acc.founderPhone\n                    updates["currency"] = acc.currency'
)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
