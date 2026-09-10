filepath = 'app/src/main/java/com/example/ui/screens/AcademicScreen.kt'
with open(filepath, 'r') as f:
    text = f.read()

open_braces = text.count('{')
close_braces = text.count('}')
print(f"Open: {open_braces}, Close: {close_braces}")
