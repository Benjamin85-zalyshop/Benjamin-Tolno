import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

target1 = """                val logo = _schoolLogoBase64.value
                if (!logo.isNullOrBlank()) {
                    updates["schoolLogo"] = logo
                }
                studentRef.updateChildren(updates)"""

replacement1 = """                val logo = _schoolLogoBase64.value
                if (!logo.isNullOrBlank()) {
                    updates["schoolLogo"] = logo
                }
                _schoolAccount.value?.let { acc ->
                    updates["schoolName"] = acc.displayName.ifEmpty { acc.schoolName }
                    updates["schoolAddress"] = acc.address
                    updates["schoolPhone"] = acc.founderPhone
                    updates["schoolYear"] = _selectedSchoolYear.value
                }
                studentRef.updateChildren(updates)"""

content = content.replace(target1, replacement1)

target2 = """                val logo = _schoolLogoBase64.value
                if (!logo.isNullOrBlank()) {
                    profileUpdates["schoolLogo"] = logo
                }
                if (profileUpdates.isNotEmpty()) {"""

replacement2 = """                val logo = _schoolLogoBase64.value
                if (!logo.isNullOrBlank()) {
                    profileUpdates["schoolLogo"] = logo
                }
                _schoolAccount.value?.let { acc ->
                    profileUpdates["schoolName"] = acc.displayName.ifEmpty { acc.schoolName }
                    profileUpdates["schoolAddress"] = acc.address
                    profileUpdates["schoolPhone"] = acc.founderPhone
                    profileUpdates["schoolYear"] = _selectedSchoolYear.value
                }
                if (profileUpdates.isNotEmpty()) {"""

content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)

