import re

file_path = "app/src/main/java/com/example/ui/SchoolViewModel.kt"
with open(file_path, "r") as f:
    content = f.read()

new_content = re.sub(
    r"fun insertExpense.*",
    r"fun insertExpense(amount: Long, reason: String, section: String) {",
    content
)

new_content = re.sub(
    r"repository\.insertExpense\(Expense.*",
    r"repository.insertExpense(Expense(schoolId = schoolId, amount = amount, reason = reason, section = section))",
    new_content
)

if content != new_content:
    with open(file_path, "w") as f:
        f.write(new_content)
    print("REPLACED")
else:
    print("NOT REPLACED")

