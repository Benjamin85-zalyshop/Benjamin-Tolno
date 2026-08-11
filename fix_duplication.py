import re

with open('public/app.js', 'r', encoding='utf-8') as f:
    content = f.read()

target1 = "document.getElementById('pdfAvg').textContent = avg + ' / 10';"
repl1 = "document.getElementById('pdfAvg').textContent = avg;"

target2 = "document.getElementById('pdfClassAvg').textContent = avg + ' / 10';"
repl2 = "document.getElementById('pdfClassAvg').textContent = avg;"

content = content.replace(target1, repl1)
content = content.replace(target2, repl2)

with open('public/app.js', 'w', encoding='utf-8') as f:
    f.write(content)
with open('scolapay-web/public/app.js', 'w', encoding='utf-8') as f:
    f.write(content)

print("Fixed duplication")
