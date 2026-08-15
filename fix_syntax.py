with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r') as f:
    code = f.read()

code = code.replace("checkPendingPaymentStatus(String: super?, function1: )", "checkPendingPaymentStatus(onResult: (String) -> Unit)")
code = code.replace(": int", ": Int")
code = code.replace(": long", ": Long")
code = code.replace(": float", ": Float")
code = code.replace("<int>", "<Int>")
code = code.replace("<long>", "<Long>")
code = code.replace("<float>", "<Float>")
code = code.replace("<Boolean>", "<Boolean>")

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w') as f:
    f.write(code)
