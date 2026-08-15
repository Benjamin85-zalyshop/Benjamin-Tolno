import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

target = 'val founderPhone: String = ""\n)'
replacement = 'val founderPhone: String = "",\n    val currency: String = "GNF"\n)'
content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
