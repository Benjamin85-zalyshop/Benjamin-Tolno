import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace('"Éval." to', '"Eval" to')
content = content.replace('"Moy." to', '"Moy" to')

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w') as f:
    f.write(content)

with open('public/app.js', 'r') as f:
    appjs = f.read()

appjs = appjs.replace("subjData['Éval.']", "subjData['Eval']")
appjs = appjs.replace("subjData['Moy.']", "subjData['Moy']")

with open('public/app.js', 'w') as f:
    f.write(appjs)

print("Keys fixed!")
