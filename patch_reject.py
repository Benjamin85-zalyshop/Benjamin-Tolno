import re

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

old_click = """                                                Button(
                                                    onClick = { 
                                                        requestToReject = request
                                                        rejectDialogReason = ""
                                                    },"""

new_click = """                                                Button(
                                                    onClick = { 
                                                        requestToReject = request
                                                        rejectDialogReason = ""
                                                        showRejectDialog = true
                                                    },"""

content = content.replace(old_click, new_click)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)

print("Patch applied")
