import re

with open('public/app.js', 'r', encoding='utf-8') as f:
    content = f.read()

target1 = """                if (data.photoBase64) {
                    window.studentPhotoBase64 = data.photoBase64;
                    const photoContainer = document.getElementById('pdfStudentPhotoPlaceholder').parentElement;
                    photoContainer.innerHTML = '<img src="data:image/jpeg;base64,' + data.photoBase64 + '" style="width:100%;height:100%;object-fit:cover;" crossorigin="anonymous">';
                }"""

replacement1 = """                if (data.photoBase64) {
                    window.studentPhotoBase64 = data.photoBase64;
                    let photoContainer = document.getElementById('pdfStudentPhotoContainer');
                    if (!photoContainer) {
                        const placeholder = document.getElementById('pdfStudentPhotoPlaceholder');
                        if (placeholder) {
                            photoContainer = placeholder.parentElement;
                            photoContainer.id = 'pdfStudentPhotoContainer';
                        }
                    }
                    if (photoContainer) {
                        photoContainer.innerHTML = '<img src="data:image/jpeg;base64,' + data.photoBase64 + '" style="width:100%;height:100%;object-fit:cover;border-radius:8px;" crossorigin="anonymous">';
                    }
                }"""

target2 = """                if (data.schoolLogo) {
                    window.schoolLogoBase64 = data.schoolLogo;
                    const logoContainer = document.getElementById('pdfSchoolLogoInitial').parentElement;
                    logoContainer.innerHTML = '<img src="data:image/png;base64,' + data.schoolLogo + '" style="width:100%;height:100%;object-fit:contain;border-radius:50%;" crossorigin="anonymous">';
                }"""

replacement2 = """                if (data.schoolLogo) {
                    window.schoolLogoBase64 = data.schoolLogo;
                    let logoContainer = document.getElementById('pdfSchoolLogoContainer');
                    if (!logoContainer) {
                        const placeholder = document.getElementById('pdfSchoolLogoInitial');
                        if (placeholder) {
                            logoContainer = placeholder.parentElement;
                            logoContainer.id = 'pdfSchoolLogoContainer';
                        }
                    }
                    if (logoContainer) {
                        logoContainer.innerHTML = '<img src="data:image/png;base64,' + data.schoolLogo + '" style="width:100%;height:100%;object-fit:contain;border-radius:50%;" crossorigin="anonymous">';
                    }
                }"""

content = content.replace(target1, replacement1)
content = content.replace(target2, replacement2)

with open('public/app.js', 'w', encoding='utf-8') as f:
    f.write(content)
with open('scolapay-web/public/app.js', 'w', encoding='utf-8') as f:
    f.write(content)
