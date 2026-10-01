package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Refresh
import kotlinx.coroutines.launch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.SchoolViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    viewModel: SchoolViewModel,
    isPendingValidation: Boolean,
    onLogout: () -> Unit
) {
    val schoolAcc by viewModel.schoolAccount.collectAsStateWithLifecycle()
    var schoolName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var transactionId by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val selectedPaymentMethod = "MOBILE_MONEY"
    val localContext = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoadingChapChap by remember { mutableStateOf(false) }
    var isCheckingPayment by remember { mutableStateOf(false) }
    var showManualIdPrompt by remember { mutableStateOf(false) }
    var manualOrderIdInput by remember { mutableStateOf("") }
    val pendingOrderId by viewModel.pendingOrderId.collectAsStateWithLifecycle()
    val isLocked = schoolAcc?.isAppLocked == true
    val rejectionReason = schoolAcc?.rejectionReason
    val hasActive = schoolAcc?.hasActiveSubscription == true

    val parsedCommFromReason = remember(schoolAcc?.lockReason) {
        val r = schoolAcc?.lockReason ?: ""
        val match = Regex("""(\d+)\s*GNF""").find(r)
        match?.groupValues?.get(1)?.toLongOrNull() ?: 0L
    }
    val actualDueCommission = remember(schoolAcc, parsedCommFromReason) {
        val fromAcc = schoolAcc?.unpaidCommission ?: 0L
        val fromCount = (schoolAcc?.onlinePaymentsCount ?: 0).toLong() * 3000L
        maxOf(fromAcc, fromCount, parsedCommFromReason)
    }
    val targetAmount = if (isLocked) {
        if (actualDueCommission > 0L) actualDueCommission.toDouble() else 3000.0
    } else {
        230000.0
    }
    val targetAmountFormatted = remember(targetAmount) {
        java.text.NumberFormat.getInstance(java.util.Locale.FRANCE).format(targetAmount.toLong())
    }
    
    LaunchedEffect(schoolAcc) {
        if (schoolName.isBlank() && schoolAcc != null) {
            schoolName = schoolAcc?.displayName?.ifBlank { schoolAcc?.schoolName } ?: ""
        }
        if (phoneNumber.isBlank() && !schoolAcc?.paymentPhoneNumber.isNullOrBlank()) {
            phoneNumber = schoolAcc?.paymentPhoneNumber ?: ""
        }
        if (transactionId.isBlank() && !schoolAcc?.transactionId.isNullOrBlank()) {
            transactionId = schoolAcc?.transactionId ?: ""
        }
    }

    LaunchedEffect(Unit) {
        viewModel.getPendingOrderId()
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                val pId = viewModel.getPendingOrderId()
                if (!pId.isNullOrBlank()) {
                    if (isLocked) {
                        viewModel.checkPendingCommissionPaymentStatus { res ->
                            if (res == "SUCCESS") {
                                android.widget.Toast.makeText(localContext, "Paiement ChapChapPay validé ! Votre accès est débloqué.", android.widget.Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        viewModel.checkPendingPaymentStatus { res ->
                            if (res == "SUCCESS") {
                                android.widget.Toast.makeText(localContext, "Abonnement activé avec succès !", android.widget.Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isLocked) "Accès Suspendu" else "Abonnement Requis", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isLocked) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = if (isLocked) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Déconnexion"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isPendingValidation) {
                Icon(
                    imageVector = Icons.Filled.HourglassEmpty,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "En attente de validation",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Votre paiement est en cours de vérification par l'administrateur. L'accès à l'application sera débloqué une fois la transaction validée.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.forceSyncSchools()
                        val currentAcc = schoolAcc
                        if (currentAcc != null && currentAcc.isAppLocked) {
                            val commMsg = if (currentAcc.unpaidCommission > 0L) " (Commissions dues : ${currentAcc.unpaidCommission} GNF)" else ""
                            android.widget.Toast.makeText(localContext, "Accès suspendu par l'administration$commMsg. En attente de déblocage par l'admin.", android.widget.Toast.LENGTH_LONG).show()
                        } else if (currentAcc != null && !currentAcc.isAppLocked && !currentAcc.isPendingValidation) {
                            android.widget.Toast.makeText(localContext, "Votre accès a été validé et débloqué !", android.widget.Toast.LENGTH_LONG).show()
                        } else {
                            android.widget.Toast.makeText(localContext, "Votre demande est en cours de vérification par l'administrateur.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Refresh, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isLocked) "Vérifier la validation de ma commission" else "Vérifier la validation de mon paiement", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        if (isLocked) {
                            viewModel.checkPendingCommissionPaymentStatus { res ->
                                if (res == "SUCCESS") {
                                    android.widget.Toast.makeText(localContext, "Commission validée avec succès ! Accès débloqué.", android.widget.Toast.LENGTH_LONG).show()
                                } else {
                                    android.widget.Toast.makeText(localContext, "Paiement de commission en cours de vérification.", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            viewModel.checkPendingPaymentStatus { res ->
                                if (res == "SUCCESS") {
                                    android.widget.Toast.makeText(localContext, "Paiement validé avec succès ! Accès débloqué.", android.widget.Toast.LENGTH_LONG).show()
                                } else {
                                    android.widget.Toast.makeText(localContext, "Paiement en cours de vérification.", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Vérifier le statut du paiement")
                }

                Spacer(modifier = Modifier.height(32.dp))
            } else {
                if (isLocked) {
                    Text(
                        text = "Accès Suspendu",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "L'accès à l'application ScolaPay pour cet établissement est momentanément suspendu par l'administration.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Motif de la suspension",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = schoolAcc?.lockReason?.ifBlank { "Régularisation de commission requise." } ?: "Régularisation de commission requise.",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (isLocked) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Montant des commissions dues : $targetAmountFormatted GNF",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = if (hasActive) "Abonnement expiré" else "Expiration de l'essai gratuit",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (hasActive) "Votre abonnement annuel a expiré. Pour continuer à utiliser tous les services de ScolaPay pour les 12 prochains mois, veuillez renouveler votre abonnement." else "Votre période d'essai gratuite de 3 mois a expiré. Pour continuer à bénéficier de tous les services de ScolaPay, activez votre abonnement annuel.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                if (!isLocked && !rejectionReason.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Demande refusée par l'administrateur",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Motif : $rejectionReason",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // OPTION 1 : PAIEMENT EN LIGNE AUTOMATIQUE AVEC CHAPCHAPPAY
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isLocked) Color(0xFF7F1D1D).copy(alpha = 0.15f) else Color(0xFFD946EF).copy(alpha = 0.12f)
                    ),
                    border = BorderStroke(1.5.dp, if (isLocked) Color(0xFFDC2626) else Color(0xFFD946EF))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚡", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isLocked) "Option 1 : Déblocage Immédiat (ChapChapPay)" else "Option 1 : Paiement Instantané (ChapChapPay)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isLocked) Color(0xFFDC2626) else Color(0xFFD946EF)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isLocked)
                                "Réglez vos commissions en ligne par Orange Money ou MTN MoMo. Dès la confirmation, votre application est débloquée automatiquement sans délai d'attente !"
                            else
                                "Payez votre abonnement en toute sécurité par Orange Money, MTN MoMo ou carte bancaire avec activation immédiate de votre compte.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                isLoadingChapChap = true
                                coroutineScope.launch {
                                    val prefix = if (isLocked) "COMM_" else "SUB_"
                                    val orderId = "$prefix${System.currentTimeMillis()}"
                                    val desc = if (isLocked) "Régularisation Commissions ScolaPay" else "Abonnement Annuel ScolaPay"
                                    val result = com.example.utils.ChapChapPayApi.createPayment(targetAmount, desc, orderId)
                                    isLoadingChapChap = false
                                    if (result != null) {
                                        viewModel.savePendingOrderId(orderId, result.operationId)
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(result.paymentUrl))
                                        localContext.startActivity(intent)
                                    } else {
                                        android.widget.Toast.makeText(localContext, "Erreur lors de la création du lien ChapChapPay. Veuillez réessayer ou utiliser le paiement manuel.", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isLocked) Color(0xFFDC2626) else Color(0xFFD946EF),
                                contentColor = Color.White
                            ),
                            enabled = !isLoadingChapChap,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isLoadingChapChap) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                Text(
                                    text = if (isLocked) "Payer $targetAmountFormatted GNF (ChapChapPay)" else "Payer 230 000 GNF (ChapChapPay)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                isCheckingPayment = true
                                if (isLocked) {
                                    viewModel.checkPendingCommissionPaymentStatus { res ->
                                        isCheckingPayment = false
                                        when (res) {
                                            "SUCCESS" -> {
                                                android.widget.Toast.makeText(localContext, "Paiement validé avec succès ! Votre accès est débloqué.", android.widget.Toast.LENGTH_LONG).show()
                                            }
                                            "PENDING" -> {
                                                android.widget.Toast.makeText(localContext, "Paiement en cours de traitement par l'opérateur. Réessayez dans un instant.", android.widget.Toast.LENGTH_LONG).show()
                                            }
                                            "FAILED" -> {
                                                android.widget.Toast.makeText(localContext, "Paiement non confirmé. Vous pouvez vérifier avec votre référence.", android.widget.Toast.LENGTH_SHORT).show()
                                                showManualIdPrompt = true
                                            }
                                            "NO_ORDER" -> {
                                                showManualIdPrompt = true
                                            }
                                            else -> {
                                                showManualIdPrompt = true
                                            }
                                        }
                                    }
                                } else {
                                    viewModel.checkPendingPaymentStatus { res ->
                                        isCheckingPayment = false
                                        when (res) {
                                            "SUCCESS" -> {
                                                android.widget.Toast.makeText(localContext, "Abonnement activé avec succès !", android.widget.Toast.LENGTH_LONG).show()
                                            }
                                            "PENDING" -> {
                                                android.widget.Toast.makeText(localContext, "Paiement en cours de traitement par l'opérateur. Réessayez dans quelques secondes.", android.widget.Toast.LENGTH_LONG).show()
                                            }
                                            "FAILED" -> {
                                                android.widget.Toast.makeText(localContext, "Paiement non confirmé.", android.widget.Toast.LENGTH_SHORT).show()
                                                showManualIdPrompt = true
                                            }
                                            "NO_ORDER" -> {
                                                showManualIdPrompt = true
                                            }
                                        }
                                    }
                                }
                            },
                            enabled = !isCheckingPayment,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isLocked) Color(0xFFDC2626) else Color(0xFFD946EF))
                        ) {
                            if (isCheckingPayment) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp), tint = if (isLocked) Color(0xFFDC2626) else Color(0xFFD946EF))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (isLocked) "Vérifier mon paiement & débloquer" else "Vérifier mon paiement & activer",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLocked) Color(0xFFDC2626) else Color(0xFFD946EF)
                                )
                            }
                        }

                        if (isLocked) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = {
                                    val pendingId = viewModel.getPendingOrderId()
                                    if (!pendingId.isNullOrBlank()) {
                                        manualOrderIdInput = pendingId
                                    }
                                    showManualIdPrompt = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Déjà payé les commissions ($targetAmountFormatted GNF) par ChapChapPay ? Vérifier ici",
                                    color = Color(0xFF16A34A),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // OPTION 2 : PAIEMENT MANUEL PAR TRANSFERT DIRECT
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                    Text("  OU PAIEMENT MANUEL  ", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isLocked) "Option 2 : Transfert direct Orange Money / MTN MoMo" else "Option 2 : Dépôt direct",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isLocked) "Veuillez effectuer le dépôt sur l'un des numéros ci-dessous, puis renseigner l'identifiant de la transaction (ID) reçu par SMS :" else "Veuillez effectuer le dépôt sur l'un des numéros ci-dessous, puis renseigner l'identifiant reçu par SMS :",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(10.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Orange Money : 628 37 65 66", fontWeight = FontWeight.Bold, color = Color(0xFFFF6600), fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("MTN MoMo : 660 37 78 87", fontWeight = FontWeight.Bold, color = Color(0xFFCC9900), fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    text = if (isLocked) "Soumettre votre justificatif de régularisation" else "Soumettre votre paiement",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    OutlinedTextField(
                        value = schoolName,
                        onValueChange = { schoolName = it },
                        label = { Text("Nom de l'école") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Numéro de téléphone de paiement") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = transactionId,
                        onValueChange = { transactionId = it },
                        label = { Text("Identifiant de transaction (ID)") },
                        placeholder = { Text("Ex: CI260924.1432.A12345 ou MP2409...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            if (schoolName.isBlank() || phoneNumber.isBlank() || transactionId.isBlank()) {
                                errorMessage = "Veuillez remplir tous les champs."
                            } else {
                                errorMessage = null
                                viewModel.submitSubscriptionRequest(phoneNumber, transactionId)
                                if (isLocked) {
                                    android.widget.Toast.makeText(localContext, "Justificatif de règlement envoyé ! L'administrateur débloquera votre accès après vérification.", android.widget.Toast.LENGTH_LONG).show()
                                } else {
                                    android.widget.Toast.makeText(localContext, "Demande d'abonnement envoyée pour validation par l'administrateur !", android.widget.Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text(if (isLocked) "Envoyer le justificatif pour validation" else "Envoyer pour validation")
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

    if (showManualIdPrompt) {
        var isVerifyingManual by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { if (!isVerifyingManual) showManualIdPrompt = false },
            title = { Text("Vérification de la commande", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Veuillez saisir la référence de commande ChapChapPay générée lors de votre paiement (ou le numéro de transaction) pour vérifier l'encaissement :",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = manualOrderIdInput,
                        onValueChange = { manualOrderIdInput = it },
                        label = { Text("ID de commande / Référence") },
                        placeholder = { Text("Ex: COMM_179... ou PAY_...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Le déblocage automatique intervient dès confirmation effective du paiement par le serveur sécurisé.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val inputId = manualOrderIdInput.trim()
                        if (inputId.isBlank()) {
                            android.widget.Toast.makeText(localContext, "Veuillez saisir votre référence de commande.", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isVerifyingManual = true
                        if (isLocked) {
                            viewModel.checkPendingCommissionPaymentStatus(inputId) { res ->
                                isVerifyingManual = false
                                when (res) {
                                    "SUCCESS" -> {
                                        showManualIdPrompt = false
                                        android.widget.Toast.makeText(localContext, "Paiement confirmé ! Votre accès est débloqué.", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                    "PENDING" -> {
                                        android.widget.Toast.makeText(localContext, "Paiement en cours de validation par l'opérateur.", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                    else -> {
                                        android.widget.Toast.makeText(localContext, "Aucun paiement validé trouvé pour cette référence. Vérifiez la saisie ou utilisez l'option 2.", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        } else {
                            viewModel.checkPendingPaymentStatus(inputId) { res ->
                                isVerifyingManual = false
                                when (res) {
                                    "SUCCESS" -> {
                                        showManualIdPrompt = false
                                        android.widget.Toast.makeText(localContext, "Paiement confirmé ! Abonnement activé.", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                    "PENDING" -> {
                                        android.widget.Toast.makeText(localContext, "Paiement en cours de validation par l'opérateur.", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                    else -> {
                                        android.widget.Toast.makeText(localContext, "Aucun paiement validé trouvé pour cette référence.", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isVerifyingManual,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) {
                    if (isVerifyingManual) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Vérifier et Débloquer", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualIdPrompt = false }, enabled = !isVerifyingManual) {
                    Text("Annuler")
                }
            }
        )
    }
}
