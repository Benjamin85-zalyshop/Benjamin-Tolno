import os
import glob

def patch_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # Pattern 1: it.reason
    content = content.replace(
        'filter { it.reason != "Inscription" && it.reason != "Réinscription" }',
        'filter { !it.isCancelled && it.reason != "Inscription" && it.reason != "Réinscription" }'
    )
    # If there was already a partial patch from previous, it might become !it.isCancelled && !it.isCancelled... let's fix if so
    content = content.replace('!it.isCancelled && !it.isCancelled', '!it.isCancelled')
    
    # Pattern 2: it.reason == "Inscription"
    content = content.replace(
        'filter { it.reason == "Inscription" }',
        'filter { !it.isCancelled && it.reason == "Inscription" }'
    )
    
    # Pattern 3: it.reason == "Réinscription"
    content = content.replace(
        'filter { it.reason == "Réinscription" }',
        'filter { !it.isCancelled && it.reason == "Réinscription" }'
    )

    # Pattern 4: p -> p.reason != ...
    content = content.replace(
        'filter { p -> p.reason != "Inscription" && p.reason != "Réinscription"',
        'filter { p -> !p.isCancelled && p.reason != "Inscription" && p.reason != "Réinscription"'
    )
    
    content = content.replace(
        'filter { p -> !p.isCancelled && !p.isCancelled',
        'filter { p -> !p.isCancelled'
    )
    
    # Another specific one in AddPaymentScreen.kt:
    content = content.replace(
        'filter { it.studentId == studentId && it.reason != "Inscription" && it.reason != "Réinscription" }',
        'filter { !it.isCancelled && it.studentId == studentId && it.reason != "Inscription" && it.reason != "Réinscription" }'
    )
    
    with open(filepath, 'w') as f:
        f.write(content)

search_dir = "app/src/main/java/com/example/ui"
for root, dirs, files in os.walk(search_dir):
    for file in files:
        if file.endswith(".kt"):
            patch_file(os.path.join(root, file))

print("Patching complete.")
