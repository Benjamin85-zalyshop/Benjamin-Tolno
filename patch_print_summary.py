import re

filepath = 'app/src/main/java/com/example/ui/screens/StudentDetailScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

old_call = """                com.example.ui.ReceiptPrinter.printSummaryTicket(
                    context,
                    schoolName ?: "",
                    matricule,
                    "${student.firstName} ${student.lastName}",
                    student.grade,
                    ticketTotalPaid,
                    ticketRemaining,
                    currency
                )"""

new_call = """                com.example.ui.ReceiptPrinter.printSummaryTicket(
                    context,
                    schoolName ?: "",
                    student,
                    ticketTotalPaid,
                    ticketRemaining,
                    currency
                )"""

if old_call in content:
    content = content.replace(old_call, new_call)
    print("Replaced in StudentDetailScreen")
else:
    print("Not found in StudentDetailScreen")

with open(filepath, 'w') as f:
    f.write(content)
