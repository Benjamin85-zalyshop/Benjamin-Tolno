import re

filepath = 'app/src/main/java/com/example/ui/screens/AcademicScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

pattern = r"(\s+\}\n\n)(@OptIn\(ExperimentalMaterial3Api::class\)\n@Composable\nprivate fun BulletinPdfTab\()"
content = re.sub(pattern, r"\1}\n\2", content)

with open(filepath, 'w') as f:
    f.write(content)
