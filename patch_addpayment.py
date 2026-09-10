import re

with open('app/src/main/java/com/example/ui/screens/AddPaymentScreen.kt', 'r') as f:
    content = f.read()

button_replacement = """                        if (classFee > 0L && amountValue > remainingToPay) {
                            Toast.makeText(context, "Impossible : Le montant dépasse le reste à payer !", Toast.LENGTH_LONG).show()
                        } else {
                            if (selectedMethod == "ChapChapPay") {
                                isLoadingChapChap = true
                                coroutineScope.launch {
                                    val orderId = "PAY_${studentId}_${System.currentTimeMillis()}"
                                    val chapChapUrl = com.example.utils.ChapChapPayApi.createPaymentOperation(amountValue.toDouble(), "Paiement Scolarité - $studentName", orderId)
                                    isLoadingChapChap = false
                                    if (chapChapUrl != null) {
                                        pendingChapChapOrderId = orderId
                                        pendingChapChapAmount = amountValue
                                        pendingChapChapReason = reason.trim()
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(chapChapUrl))
                                        context.startActivity(intent)
                                    } else {
                                        Toast.makeText(context, "Erreur lors de la création du lien ChapChapPay", Toast.LENGTH_LONG).show()
                                    }
                                }
                            } else {
                                viewModel.insertPayment(studentId, amountValue, reason.trim(), selectedMethod)
                                successAmount = amountValue
                                successReason = reason.trim()
                                successMethod = selectedMethod
                                showSuccessDialog = true
                            }
                        }"""

content = re.sub(
    r'                        if \(classFee > 0L && amountValue > remainingToPay\) \{.*?showSuccessDialog = true\n                        \}',
    button_replacement,
    content,
    flags=re.DOTALL
)

dialog_addition = """    if (pendingChapChapOrderId != null) {
        AlertDialog(
            onDismissRequest = { /* Empêcher fermeture sans vérifier ou annuler */ },
            title = { Text("Paiement ChapChapPay en cours", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Veuillez procéder au paiement sur la page web qui s'est ouverte.")
                    Text("Une fois le paiement terminé, cliquez sur 'Vérifier'.")
                    if (isLoadingChapChap) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isLoadingChapChap = true
                        coroutineScope.launch {
                            val status = com.example.utils.ChapChapPayApi.checkOrderStatus(pendingChapChapOrderId!!)
                            isLoadingChapChap = false
                            if (status == "SUCCESS") {
                                viewModel.insertPayment(studentId, pendingChapChapAmount, pendingChapChapReason, "ChapChapPay")
                                successAmount = pendingChapChapAmount
                                successReason = pendingChapChapReason
                                successMethod = "ChapChapPay"
                                pendingChapChapOrderId = null
                                showSuccessDialog = true
                            } else if (status == "FAILED") {
                                Toast.makeText(context, "Le paiement a échoué ou a été annulé.", Toast.LENGTH_LONG).show()
                                pendingChapChapOrderId = null
                            } else {
                                Toast.makeText(context, "Paiement toujours en attente...", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = !isLoadingChapChap
                ) {
                    Text("Vérifier le paiement")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { pendingChapChapOrderId = null },
                    enabled = !isLoadingChapChap
                ) {
                    Text("Annuler")
                }
            }
        )
    }

"""

# Add the dialog at the end, right before the last closing brace.
content = re.sub(
    r'(    if \(showSuccessDialog\) \{)',
    dialog_addition + r'\1',
    content
)

with open('app/src/main/java/com/example/ui/screens/AddPaymentScreen.kt', 'w') as f:
    f.write(content)

