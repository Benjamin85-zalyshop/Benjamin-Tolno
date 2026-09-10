import re

filepath = 'app/src/main/java/com/example/ui/screens/AcademicScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Fix the import
content = content.replace("com.example.data.local.Subject", "com.example.data.models.Subject")

# Remove the duplicate block at the end of the file
# I'll just use regex to remove everything from `    if (subjectToEdit != null) {` to the end if it's after GradesEntryTab.

pattern = r"(\s+if \(subjectToEdit \!= null\) \{\s+AlertDialog\([\s\S]*?Text\(\"Annuler\"\)\n\s+\}\n\s+\}\n\s+\)\n\s+\}\n\})"

# Let's find all matches
matches = list(re.finditer(pattern, content))
if len(matches) > 1:
    last_match = matches[-1]
    # Remove the last match
    content = content[:last_match.start()] + content[last_match.end():]

with open(filepath, 'w') as f:
    f.write(content)
