import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r') as f:
    content = f.read()

old_code = "            withContext(kotlinx.coroutines.Dispatchers.Main) {"
new_code = "            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {"

content = content.replace(old_code, new_code)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w') as f:
    f.write(content)

print("Import fixed")
