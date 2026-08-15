import re

with open('public/app.js', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('if (data.schoolYear) {', 'if (data.currency) {\n                    window.schoolCurrency = data.currency;\n                }\n                if (data.schoolYear) {')
content = content.replace('formatGNF(dbTotal)', 'formatCurrency(dbTotal)')
content = content.replace('formatGNF(dbPaid)', 'formatCurrency(dbPaid)')
content = content.replace('formatGNF(dbDue)', 'formatCurrency(dbDue)')

format_currency = """                const formatCurrency = (num) => {
                    if (num === null || num === undefined) return "0 " + (window.schoolCurrency || "GNF");
                    return Number(num).toLocaleString('fr-FR').replace(/,/g, ' ') + " " + (window.schoolCurrency || "GNF");
                };"""

content = re.sub(r'const formatGNF = \(num\) => \{.*?\};', format_currency, content, flags=re.DOTALL)

with open('public/app.js', 'w', encoding='utf-8') as f:
    f.write(content)
with open('scolapay-web/public/app.js', 'w', encoding='utf-8') as f:
    f.write(content)

