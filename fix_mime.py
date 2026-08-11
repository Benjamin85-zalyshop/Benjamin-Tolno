import re

with open('public/app.js', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace("'<img src=\"data:image/jpeg;base64,' + data.schoolLogo", "'<img src=\"data:image/png;base64,' + data.schoolLogo")
content = content.replace("'<img src=\"data:image/jpeg;base64,' + data.photoBase64", "'<img src=\"data:image/jpeg;base64,' + data.photoBase64")

with open('public/app.js', 'w', encoding='utf-8') as f:
    f.write(content)
with open('scolapay-web/public/app.js', 'w', encoding='utf-8') as f:
    f.write(content)

print("Fixed MIME types")
