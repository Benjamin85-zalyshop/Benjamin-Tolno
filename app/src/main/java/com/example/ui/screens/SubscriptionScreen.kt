package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.HourglassEmpty
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
    var schoolName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var transactionId by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedPaymentMethod by remember { mutableStateOf("CHAP_CHAP") }
    val localContext = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoadingChapChap by remember { mutableStateOf(false) }
    val pendingOrderId by viewModel.pendingOrderId.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.getPendingOrderId()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Abonnement Requis", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
                Spacer(modifier = Modifier.height(48.dp))
            } else {
                val schoolAcc by viewModel.schoolAccount.collectAsStateWithLifecycle()
                val rejectionReason = schoolAcc?.rejectionReason
                val hasActive = schoolAcc?.hasActiveSubscription == true
                
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
                
                if (!rejectionReason.isNullOrBlank()) {
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

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)), // Light blue brand container
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "OFFRE PROMOTIONNELLE EXCEPTIONNELLE",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "500 000 GNF",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                ),
                                color = Color.Gray
                            )
                            Text(
                                text = "200 000 GNF / an",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981) // Beautiful green for active price
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Bénéficiez du tarif réduit de 200 000 GNF au lieu du prix normal de 500 000 GNF.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF1E3A8A)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Choisissez votre méthode de paiement",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                TabRow(
                    selectedTabIndex = if (selectedPaymentMethod == "MOBILE_MONEY") 0 else 1,
                    containerColor = Color.Transparent
                ) {
                    Tab(
                        selected = selectedPaymentMethod == "CHAP_CHAP",
                        onClick = { selectedPaymentMethod = "CHAP_CHAP" },
                        text = { Text("Chap Chap Pay") }
                    )
                    Tab(
                        selected = selectedPaymentMethod == "MOBILE_MONEY",
                        onClick = { selectedPaymentMethod = "MOBILE_MONEY" },
                        text = { Text("Manuel (Orange/MTN)") }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (selectedPaymentMethod == "MOBILE_MONEY") {
                    Text(
                        text = "Veuillez effectuer le dépôt sur l'un des numéros ci-dessous :",
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

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Soumettre votre paiement",
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
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = {
                            if (schoolName.isBlank() || phoneNumber.isBlank() || transactionId.isBlank()) {
                                errorMessage = "Veuillez remplir tous les champs"
                            } else {
                                errorMessage = null
                                viewModel.submitSubscriptionRequest(phoneNumber, transactionId)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("Envoyer pour validation")
                    }
                } else {
                    // Chap Chap Pay UI
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
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            if (pendingOrderId != null) {
                                var isCheckingStatus by remember { mutableStateOf(false) }
                                Button(
                                    onClick = { 
                                        isCheckingStatus = true
                                        viewModel.checkPendingPaymentStatus { status ->
                                            isCheckingStatus = false
                                            if (status == "SUCCESS") {
                                                android.widget.Toast.makeText(localContext, "Paiement validé avec succès !", android.widget.Toast.LENGTH_SHORT).show()
                                            } else if (status == "FAILED") {
                                                android.widget.Toast.makeText(localContext, "Paiement échoué ou annulé. Vous pouvez réessayer.", android.widget.Toast.LENGTH_SHORT).show()
                                            } else {
                                                android.widget.Toast.makeText(localContext, "Paiement en attente de validation.", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.White),
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    enabled = !isCheckingStatus
                                ) {
                                    if (isCheckingStatus) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                    } else {
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Finaliser le paiement", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = { viewModel.clearPendingOrderId() },
                                    modifier = Modifier.fillMaxWidth().height(56.dp)
                                ) {
                                    Text("Annuler et réessayer", color = Color(0xFFD946EF))
                                }
                            } else {
                                Button(
                                    onClick = {
                                        isLoadingChapChap = true
                                        coroutineScope.launch {
                                            val orderId = "SUB_${System.currentTimeMillis()}"
                                            val chapChapUrl = com.example.utils.ChapChapPayApi.createPaymentOperation(200000.0, "Abonnement Annuel ScolaPay", orderId)
                                            isLoadingChapChap = false
                                            if (chapChapUrl != null) {
                                                viewModel.savePendingOrderId(orderId)
                                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(chapChapUrl))
                                                localContext.startActivity(intent)
                                            } else {
                                                errorMessage = "Erreur lors de la création du lien de paiement Chap Chap Pay."
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD946EF), contentColor = Color.White),
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    enabled = !isLoadingChapChap
                                ) {
                                    if (isLoadingChapChap) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text("Payer avec Chap Chap Pay", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
