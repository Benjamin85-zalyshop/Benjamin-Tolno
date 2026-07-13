import sys
with open("app/src/main/java/com/example/ui/SchoolViewModel.kt") as f:
    text = f.read()

new_text = text.replace("category: String, description: String", "reason: String")
new_text = new_text.replace("category = category, description = description", "reason = reason")

with open("app/src/main/java/com/example/ui/SchoolViewModel.kt", "w") as f:
    f.write(new_text)

if text != new_text:
    print("Replaced!")
else:
    print("No replacement occurred!")
