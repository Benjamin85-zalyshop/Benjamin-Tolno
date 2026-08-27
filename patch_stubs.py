import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r') as f:
    content = f.read()

old_year = """    fun setSelectedSchoolYear(year: String) {
        // TODO: Auto-generated stub
    }"""

new_year = """    fun setSelectedSchoolYear(year: String) {
        _selectedSchoolYear.value = year
    }"""

content = content.replace(old_year, new_year)

old_sec = """    fun setSection(section: String) {
        // TODO: Auto-generated stub
    }"""

new_sec = """    fun setSection(section: String) {
        _selectedSection.value = section
    }"""

content = content.replace(old_sec, new_sec)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w') as f:
    f.write(content)
