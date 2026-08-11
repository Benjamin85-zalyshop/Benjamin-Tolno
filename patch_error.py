import re

with open('public/app.js', 'r', encoding='utf-8') as f:
    content = f.read()

target = """                if (data.academics) {"""
replacement = """                if (data.academics) {"""

target_error = """        });
    }
    // Afficher le contenu"""
replacement_error = """        }, (error) => {
            console.error("Firebase Read Error:", error);
        });
    }
    // Afficher le contenu"""

content = content.replace(target_error, replacement_error)

with open('public/app.js', 'w', encoding='utf-8') as f:
    f.write(content)
with open('scolapay-web/public/app.js', 'w', encoding='utf-8') as f:
    f.write(content)
