import re

with open('app/src/main/java/com/example/ui/screens/LoginScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

pattern = r"""                            val success = viewModel\.login\(email\.trim\(\), password\)
                            if \(success\) \{
                                onNavigateToDashboard\(\)
                            \} else \{
                                errorMessage = "E-mail ou mot de passe incorrect"
                                isLoggingIn = false
                            \}"""

replacement = """                            val success = viewModel.login(email.trim(), password)
                            if (success) {
                                onNavigateToDashboard()
                            } else {
                                val vmError = viewModel.loginError.value
                                errorMessage = vmError ?: "E-mail ou mot de passe incorrect"
                                isLoggingIn = false
                            }"""

content = re.sub(pattern, replacement, content)

with open('app/src/main/java/com/example/ui/screens/LoginScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)
