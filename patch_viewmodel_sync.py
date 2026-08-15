import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Register School
content = content.replace(
    'val account = hashMapOf(',
    'val account = hashMapOf(\n                "currency" to "GNF",'
)

content = content.replace(
    'founderPhone = founderPhone\n            )',
    'founderPhone = founderPhone,\n                currency = "GNF"\n            )'
)

# syncAccount
content = content.replace(
    'val founderPhone = doc.getString("founderPhone") ?: ""',
    'val founderPhone = doc.getString("founderPhone") ?: ""\n                    val currency = doc.getString("currency") ?: "GNF"'
)

content = content.replace(
    'founderPhone = founderPhone\n                    )',
    'founderPhone = founderPhone,\n                        currency = currency\n                    )'
)

# updateAccount
content = content.replace(
    'val founderPhone = doc.getString("founderPhone") ?: account.founderPhone',
    'val founderPhone = doc.getString("founderPhone") ?: account.founderPhone\n                        val currency = doc.getString("currency") ?: account.currency'
)

content = content.replace(
    'founderPhone = founderPhone\n                        )',
    'founderPhone = founderPhone,\n                            currency = currency\n                        )'
)

# saveSchoolSettings
target_settings = """    fun saveSchoolSettings(displayName: String, address: String, founderPhone: String) {"""
replacement_settings = """    fun saveSchoolSettings(displayName: String, address: String, founderPhone: String, currency: String = "GNF") {"""
content = content.replace(target_settings, replacement_settings)

content = content.replace(
    '"address" to address,',
    '"address" to address,\n                "currency" to currency,'
)

content = content.replace(
    'founderPhone = founderPhone\n                )',
    'founderPhone = founderPhone,\n                    currency = currency\n                )'
)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
