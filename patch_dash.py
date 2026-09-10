import re

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

# Replace 200 000 with 230 000 in text
content = content.replace("200 000 $currency", "230 000 $currency")
content = content.replace("200000.0", "230000.0")

# Rewrite the dialog
old_dialog = r"""    if \(showDirectSubscriptionDialog\) \{.*?    if \(showSchoolYearDialog\) \{"""

new_dialog = """    if (showDirectSubscriptionDialog) {
        var isLoadingChapChap by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showDirectSubscriptionDialog = false },
            title = {
                Text(
                    text = "S'abonner à ScolaPay",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF1F2937)
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Profitez de notre offre spéciale à 230 000 $currency/an au lieu de 500 000 $currency.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4B5563)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Paiement Rapide avec Chap Chap Pay",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD946EF)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Vous serez redirigé vers Chap Chap Pay pour payer en toute sécurité via Orange Money, MTN MoMo ou carte bancaire.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isLoadingChapChap = true
                        coroutineScope.launch {
                            val orderId = "SUB_${System.currentTimeMillis()}"
                            val chapChapUrl = com.example.utils.ChapChapPayApi.createPaymentOperation(230000.0, "Abonnement Annuel ScolaPay", orderId)
                            isLoadingChapChap = false
                            if (chapChapUrl != null) {
                                viewModel.savePendingOrderId(orderId)
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(chapChapUrl))
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(context, "Erreur lors de la création du lien de paiement Chap Chap Pay.", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD946EF), contentColor = Color.White),
                    enabled = !isLoadingChapChap
                ) {
                    if (isLoadingChapChap) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Payer avec Chap Chap Pay", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDirectSubscriptionDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showSchoolYearDialog) {"""

content = re.sub(old_dialog, new_dialog, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)
