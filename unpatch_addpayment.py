import re

with open('app/src/main/java/com/example/ui/screens/AddPaymentScreen.kt', 'r') as f:
    content = f.read()

# Revert color for ScolaPay
content = content.replace('"ChapChapPay" to Color(0xFFE11D48)', '"ScolaPay" to Color(0xFF0F56E3)')

# Remove dialog block at end
content = re.sub(
    r'    if \(pendingChapChapOrderId \!= null\) \{.*?    \}\n\n    if \(showSuccessDialog\) \{',
    r'    if (showSuccessDialog) {',
    content,
    flags=re.DOTALL
)

# Revert button block
chapchap_button_block = r"""                        if \(classFee > 0L && amountValue > remainingToPay\) \{
                            Toast\.makeText\(context, "Impossible : Le montant dépasse le reste à payer !", Toast\.LENGTH_LONG\)\.show\(\)
                        \} else \{
                            if \(selectedMethod == "ChapChapPay"\) \{
                                isLoadingChapChap = true
                                coroutineScope\.launch \{
                                    val orderId = "PAY_\$\{studentId\}_\$\{System\.currentTimeMillis\(\)\}"
                                    val chapChapUrl = com\.example\.utils\.ChapChapPayApi\.createPaymentOperation\(amountValue\.toDouble\(\), "Paiement Scolarité - \$studentName", orderId\)
                                    isLoadingChapChap = false
                                    if \(chapChapUrl \!= null\) \{
                                        pendingChapChapOrderId = orderId
                                        pendingChapChapAmount = amountValue
                                        pendingChapChapReason = reason\.trim\(\)
                                        val intent = android\.content\.Intent\(android\.content\.Intent\.ACTION_VIEW, android\.net\.Uri\.parse\(chapChapUrl\)\)
                                        context\.startActivity\(intent\)
                                    \} else \{
                                        Toast\.makeText\(context, "Erreur lors de la création du lien ChapChapPay", Toast\.LENGTH_LONG\)\.show\(\)
                                    \}
                                \}
                            \} else \{
                                viewModel\.insertPayment\(studentId, amountValue, reason\.trim\(\), selectedMethod\)
                                successAmount = amountValue
                                successReason = reason\.trim\(\)
                                successMethod = selectedMethod
                                showSuccessDialog = true
                            \}
                        \}"""

original_button_block = """                        if (classFee > 0L && amountValue > remainingToPay) {
                            Toast.makeText(context, "Impossible : Le montant dépasse le reste à payer !", Toast.LENGTH_LONG).show()
                        } else {
                            viewModel.insertPayment(studentId, amountValue, reason.trim(), selectedMethod)
                            successAmount = amountValue
                            successReason = reason.trim()
                            successMethod = selectedMethod
                            showSuccessDialog = true
                        }"""

content = re.sub(chapchap_button_block, original_button_block, content)

# Remove the vars added:
#     var isLoadingChapChap by remember { mutableStateOf(false) }
#     var pendingChapChapOrderId by remember { mutableStateOf<String?>(null) }
#     var pendingChapChapAmount by remember { mutableStateOf(0L) }
#     var pendingChapChapReason by remember { mutableStateOf("") }
#     val coroutineScope = rememberCoroutineScope()

content = re.sub(r'    var isLoadingChapChap by remember \{ mutableStateOf\(false\) \}\n', '', content)
content = re.sub(r'    var pendingChapChapOrderId by remember \{ mutableStateOf<String\?>\(null\) \}\n', '', content)
content = re.sub(r'    var pendingChapChapAmount by remember \{ mutableStateOf\(0L\) \}\n', '', content)
content = re.sub(r'    var pendingChapChapReason by remember \{ mutableStateOf\(""\) \}\n', '', content)
content = re.sub(r'    val coroutineScope = rememberCoroutineScope\(\)\n', '', content)

with open('app/src/main/java/com/example/ui/screens/AddPaymentScreen.kt', 'w') as f:
    f.write(content)
