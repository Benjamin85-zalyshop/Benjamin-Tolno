import re

filepath = 'app/src/main/java/com/example/ui/SchoolViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

print("Checking SchoolViewModel Firebase initialization...")
if "val firestore =" in content:
    print("Found direct initialization:")
    lines = [l for l in content.split('\n') if "firestore" in l]
    for l in lines[:5]:
        print(l.strip())
elif "val firestore by lazy" in content:
    print("Found lazy initialization:")
    lines = [l for l in content.split('\n') if "firestore" in l]
    for l in lines[:5]:
        print(l.strip())
else:
    print("Could not find firestore initialization")

