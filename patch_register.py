import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Fix registerSchool fallback
pattern = r"        \} catch \(e: Exception\) \{\n            e\.printStackTrace\(\)\n            try \{\n                _userRole\.value = \"FOUNDER\"\n                repository\.registerSchool\(cleanEmail, founderPassword, financierPassword, displayName, address, founderPhone\)\n                val account = repository\.getSchoolAccountByName\(cleanEmail\)\n                if \(account != null\) \{\n                    _currentSchoolId\.value = account\.id\n                    _schoolName\.value = account\.displayName\.ifEmpty \{ account\.schoolName \}\n                    saveSession\(cleanEmail, \"FOUNDER\"\)\n                    startRealtimeSync\(cleanEmail, account\.id\)\n                    return true\n                \}\n            \} catch \(localEx: Exception\) \{\n                false\n            \}\n            false\n        \}"

replacement = """        } catch (e: Exception) {
            e.printStackTrace()
            false
        }"""

content = re.sub(pattern, replacement, content)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
