import re

with open('public/app.js', 'r', encoding='utf-8') as f:
    content = f.read()

# In the onValue(studentRef, ...) block, around line 275
# we can inject logic to capture photo and logo and update global variables or DOM directly.

target = "updateFinancialUI(formatGNF(dbTotal), formatGNF(dbPaid), formatGNF(dbDue), dbPercent);"
replacement = """updateFinancialUI(formatGNF(dbTotal), formatGNF(dbPaid), formatGNF(dbDue), dbPercent);

                if (data.photoBase64) {
                    window.studentPhotoBase64 = data.photoBase64;
                    const photoContainer = document.getElementById('pdfStudentPhotoPlaceholder').parentElement;
                    photoContainer.innerHTML = '<img src="data:image/jpeg;base64,' + data.photoBase64 + '" style="width:100%;height:100%;object-fit:cover;" crossorigin="anonymous">';
                }
                if (data.schoolLogo) {
                    window.schoolLogoBase64 = data.schoolLogo;
                    const logoContainer = document.getElementById('pdfSchoolLogoInitial').parentElement;
                    logoContainer.innerHTML = '<img src="data:image/jpeg;base64,' + data.schoolLogo + '" style="width:100%;height:100%;object-fit:contain;border-radius:50%;" crossorigin="anonymous">';
                }
"""
content = content.replace(target, replacement)

with open('public/app.js', 'w', encoding='utf-8') as f:
    f.write(content)

with open('scolapay-web/public/app.js', 'w', encoding='utf-8') as f:
    f.write(content)

print("Updated app.js")
