import re

filepath = 'app/src/main/java/com/example/ui/ReceiptPrinter.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace('val jobName = "Recap_$studentName"', 'val jobName = "Recap_${student.firstName}_${student.lastName}"')

with open(filepath, 'w') as f:
    f.write(content)
