import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

target1 = 'suspend fun registerSchool(email: String, founderPassword: String, financierPassword: String, displayName: String, address: String = "", founderPhone: String = ""): Boolean {'
replacement1 = 'suspend fun registerSchool(email: String, founderPassword: String, financierPassword: String, displayName: String, address: String = "", founderPhone: String = "", currency: String = "GNF"): Boolean {'
content = content.replace(target1, replacement1)

target2 = 'repository.registerSchool(cleanEmail, founderPassword, financierPassword, displayName, address, founderPhone)'
replacement2 = 'repository.registerSchool(cleanEmail, founderPassword, financierPassword, displayName, address, founderPhone, currency)'
content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
