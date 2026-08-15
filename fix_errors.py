import re

with open('app/src/main/java/com/example/ui/screens/RegisterScreen.kt', 'r') as f:
    code = f.read()
code = code.replace("email = email.trim(),", "name = email.trim(),")
code = code.replace("founderPassword = founderPassword,", "fp = founderPassword,")
code = code.replace("financierPassword = financierPassword,", "finp = financierPassword,")
code = code.replace("displayName = schoolNameInput.trim(),", "dn = schoolNameInput.trim(),")
code = code.replace("address = schoolAddress.trim(),", "addr = schoolAddress.trim(),")
code = code.replace("founderPhone = founderPhone.trim()", "phone = founderPhone.trim()")
with open('app/src/main/java/com/example/ui/screens/RegisterScreen.kt', 'w') as f:
    f.write(code)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r') as f:
    code = f.read()
code = code.replace("suspend fun syncFinancierAuthAccount(a: String, b: String, c: String) {}", "suspend fun syncFinancierAuthAccount(a: String, b: String, c: String) {}\n    fun updateCurrency(currency: String) {}")
with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w') as f:
    f.write(code)

