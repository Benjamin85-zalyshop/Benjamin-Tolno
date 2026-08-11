import re

with open('public/app.js', 'r', encoding='utf-8') as f:
    content = f.read()

target = "document.getElementById('pdfSchoolLogoInitial').textContent = schoolName.charAt(0).toUpperCase();"
replacement = "if (document.getElementById('pdfSchoolLogoInitial')) { document.getElementById('pdfSchoolLogoInitial').textContent = schoolName.charAt(0).toUpperCase(); }"

content = content.replace(target, replacement)

with open('public/app.js', 'w', encoding='utf-8') as f:
    f.write(content)
with open('scolapay-web/public/app.js', 'w', encoding='utf-8') as f:
    f.write(content)
