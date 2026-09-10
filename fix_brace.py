import re

filepath = 'app/src/main/java/com/example/ui/screens/AcademicScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace("    }\n@OptIn(ExperimentalMaterial3Api::class)\n@Composable\nprivate fun BulletinPdfTab(", "    }\n}\n@OptIn(ExperimentalMaterial3Api::class)\n@Composable\nprivate fun BulletinPdfTab(")

with open(filepath, 'w') as f:
    f.write(content)
