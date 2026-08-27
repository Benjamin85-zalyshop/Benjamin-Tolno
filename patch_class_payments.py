import re
with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'classPayments.sumOf { it.amount }',
    'classPayments.filter { !it.isCancelled }.sumOf { it.amount }'
)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)
