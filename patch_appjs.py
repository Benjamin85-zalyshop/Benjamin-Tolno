import re

with open('public/app.js', 'r', encoding='utf-8') as f:
    content = f.read()

target = """                if (data.schoolLogo) {
                    window.schoolLogoBase64 = data.schoolLogo;
                    const logoContainer = document.getElementById('pdfSchoolLogoInitial').parentElement;
                    logoContainer.innerHTML = '<img src="data:image/png;base64,' + data.schoolLogo + '" style="width:100%;height:100%;object-fit:contain;border-radius:50%;" crossorigin="anonymous">';
                }"""

replacement = """                if (data.schoolLogo) {
                    window.schoolLogoBase64 = data.schoolLogo;
                    const logoContainer = document.getElementById('pdfSchoolLogoInitial').parentElement;
                    logoContainer.innerHTML = '<img src="data:image/png;base64,' + data.schoolLogo + '" style="width:100%;height:100%;object-fit:contain;border-radius:50%;" crossorigin="anonymous">';
                }
                if (data.schoolName) {
                    document.getElementById('schoolName').textContent = data.schoolName;
                    document.getElementById('pdfSchoolName').textContent = data.schoolName;
                }
                if (data.schoolAddress) {
                    document.getElementById('pdfSchoolContact').textContent = data.schoolAddress;
                }
                if (data.schoolYear) {
                    document.getElementById('schoolYear').textContent = data.schoolYear;
                }"""

content = content.replace(target, replacement)

with open('public/app.js', 'w', encoding='utf-8') as f:
    f.write(content)
with open('scolapay-web/public/app.js', 'w', encoding='utf-8') as f:
    f.write(content)

