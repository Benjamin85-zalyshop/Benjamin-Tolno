import re

with open('public/app.js', 'r') as f:
    content = f.read()

# Fix how address is displayed on web PDF
old_pdf_contact = """                if (data.schoolAddress) {
                    document.getElementById('pdfSchoolContact').textContent = data.schoolAddress;
                }"""

new_pdf_contact = """                if (data.schoolAddress) {
                    document.getElementById('pdfSchoolContact').textContent = data.schoolAddress;
                } else if (data.schoolName) {
                    // Fallback to old contact string from Android if needed, but Android now sends schoolAddress
                }"""

content = content.replace(old_pdf_contact, new_pdf_contact)

with open('public/app.js', 'w') as f:
    f.write(content)
