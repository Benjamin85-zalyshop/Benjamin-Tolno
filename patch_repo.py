import re

with open('app/src/main/java/com/example/data/repository/SchoolRepository.kt', 'r', encoding='utf-8') as f:
    content = f.read()

target = 'suspend fun registerSchool(name: String, founderPassword: String, financierPassword: String, displayName: String = "", address: String = "", founderPhone: String = "") {'
replacement = 'suspend fun registerSchool(name: String, founderPassword: String, financierPassword: String, displayName: String = "", address: String = "", founderPhone: String = "", currency: String = "GNF") {'
content = content.replace(target, replacement)

target2 = 'founderPhone = founderPhone\n        )'
replacement2 = 'founderPhone = founderPhone,\n            currency = currency\n        )'
content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/data/repository/SchoolRepository.kt', 'w', encoding='utf-8') as f:
    f.write(content)
