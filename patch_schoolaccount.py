import re

with open('app/src/main/java/com/example/data/models/SchoolAccount.kt', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace("val subscriptionExpiryDate: Long = 0L", "val subscriptionExpiryDate: Long = 0L,\n    val currency: String = \"GNF\"")

with open('app/src/main/java/com/example/data/models/SchoolAccount.kt', 'w', encoding='utf-8') as f:
    f.write(content)
