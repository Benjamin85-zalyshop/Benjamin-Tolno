import re

with open('public/app.js', 'r', encoding='utf-8') as f:
    content = f.read()

target = """                        document.getElementById('academicAvg').textContent = termData.avg + ' / ' + (document.getElementById('academicAvg').textContent.split('/')[1] || '20').trim();"""

replacement = """                        let maxScore = 20;
                        if (studentSection.toLowerCase().includes('primaire') || studentSection.toLowerCase().includes('maternelle')) {
                            maxScore = 10;
                        }
                        document.getElementById('academicAvg').textContent = termData.avg + ' / ' + maxScore;"""

content = content.replace(target, replacement)

with open('public/app.js', 'w', encoding='utf-8') as f:
    f.write(content)
with open('scolapay-web/public/app.js', 'w', encoding='utf-8') as f:
    f.write(content)
