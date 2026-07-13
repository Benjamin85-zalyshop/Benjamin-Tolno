import re
file_path = "app/src/main/java/com/example/ui/SchoolViewModel.kt"
with open(file_path, "r") as f:
    content = f.read()

for i, line in enumerate(content.split("\n")):
    if "insertExpense" in line:
        print(f"{i}: {repr(line)}")
