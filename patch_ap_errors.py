with open('app/src/main/java/com/example/ui/screens/AddPaymentScreen.kt', 'r') as f:
    code = f.read()

code = code.replace("schoolLogoBase64: String?,", "schoolLogoBase64: String?,\n    currency: String,")
code = code.replace("schoolLogoBase64 = schoolLogoBase64", "schoolLogoBase64 = schoolLogoBase64,\n                                    currency = currency")

with open('app/src/main/java/com/example/ui/screens/AddPaymentScreen.kt', 'w') as f:
    f.write(code)
