import re

with open('app/src/main/java/com/example/ui/screens/SubscriptionScreen.kt', 'r') as f:
    lines = f.readlines()

# let's remove the last brace
lines.pop()

with open('app/src/main/java/com/example/ui/screens/SubscriptionScreen.kt', 'w') as f:
    f.writelines(lines)
