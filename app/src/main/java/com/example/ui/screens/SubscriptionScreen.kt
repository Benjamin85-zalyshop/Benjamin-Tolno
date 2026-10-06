package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.SchoolViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    viewModel: SchoolViewModel,
    isPendingValidation: Boolean,
    onLogout: () -> Unit
) {
    val schoolAcc by viewModel.schoolAccount.collectAsStateWithLifecycle()
    val localContext = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoadingChapChap by remember { mutableStateOf(false) }
    var isCheckingStatus by remember { mutableStateOf(false) }
    var showManualPrompt by remember { mutableStateOf(false) }
    var manualRefInput by remember { mutableStateOf("") }

    val pendingOrderId = viewModel.getPendingOrderId()

    val isLocked = schoolAcc?.isAppLocked == true && (!schoolAcc?.lockReason.isNullOrBlank() || (schoolAcc?.unpaidCommission ?: 0L) > 0L)
    val rejectionReason = schoolAcc?.rejectionReason
    val hasActive = schoolAcc?.hasActiveSubscription == true

    val displayReason = remember(schoolAcc?.lockReason) {
        val r = schoolAcc?.lockReason?.trim() ?: ""
        if (r.isBlank()) {
            "Accès à l'application ScolaPay suspendu par l'administration."
        } else {
            r
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        viewModel.forceSyncSchools()
        val pId = viewModel.getPendingOrderId()
        if (!pId.isNullOrBlank()) {
            viewModel.checkPendingPaymentStatus { res ->
                if (res == "SUCCESS") {
                    Toast.makeText(localContext, "Abonnement activé avec succès ! Accès débloqué.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        var pollingJob: Job? = null
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val pId = viewModel.getPendingOrderId()
                if (!pId.isNullOrBlank()) {
                    pollingJob?.cancel()
                    pollingJob = coroutineScope.launch {
                        for (i in 1..6) {
                            var done = false
                            viewModel.checkPendingPaymentStatus { res ->
                                if (res == "SUCCESS") {
                                    done = true
                                    Toast.makeText(localContext, "Abonnement activé avec succès ! Accès débloqué.", Toast.LENGTH_LONG).show()
                                }
                            }
                            if (done) break
                            delay(3000L)
                        }
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            pollingJob?.cancel()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isLocked) "Accès Verrouillé" else "Abonnement ScolaPay", fontWeight = FontWeight.Bold) },
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
            Spacer(modifier = Modifier.height(20.dp))

            if (isLocked) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Accès Établissement Verrouillé",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "L'accès à l'interface de gestion de l'école est actuellement bloqué par l'administration ScolaPay.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Motif du verrouillage :",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = displayReason,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Pour régulariser la situation de votre établissement ou demander le déverrouillage, veuillez contacter le service administratif ScolaPay.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = {
                                val schoolTitle = schoolAcc?.displayName?.ifBlank { schoolAcc?.schoolName } ?: "notre école"
                                val waMsg = "Bonjour Administration ScolaPay,\n\nL'accès à notre établissement *$schoolTitle* est verrouillé dans l'application.\n\n*Motif indiqué :*\n$displayReason\n\nMerci de nous indiquer la démarche pour régulariser notre situation et débloquer l'accès."
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse("https://api.whatsapp.com/send?phone=224628376566&text=${Uri.encode(waMsg)}")
                                    }
                                    localContext.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(localContext, "Contactez le support au 628 37 65 66", Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("💬 Contacter l'administrateur (WhatsApp)", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        var isSyncing by remember { mutableStateOf(false) }
                        OutlinedButton(
                            onClick = {
                                isSyncing = true
                                coroutineScope.launch {
                                    viewModel.forceSyncSchools()
                                    delay(1200L)
                                    isSyncing = false
                                    Toast.makeText(localContext, "Statut vérifié auprès du serveur.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isSyncing
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Vérifier le statut / Actualiser")
                            }
                        }
                    }
                }
            } else {
                Icon(
                    imageVector = Icons.Filled.Payment,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (hasActive) "Abonnement expiré" else "Abonnement ScolaPay",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (hasActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (hasActive) "Votre abonnement annuel a expiré. Renouvelez-le via chapchappay.com pour débloquer l'application." else "Pour accéder à l'ensemble des fonctionnalités de ScolaPay, activez votre abonnement annuel.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!rejectionReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Remarque précédente :",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = rejectionReason,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // CARTE DE CONFIRMATION DE PAIEMENT EN COURS
            if (!pendingOrderId.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)), // Warm amber
                    border = BorderStroke(1.5.dp, Color(0xFFF59E0B))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.HourglassEmpty, contentDescription = null, tint = Color(0xFFD97706))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Paiement en attente de validation",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E),
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Si vous venez d'effectuer votre règlement de 300 000 GNF sur Chap Chap Pay, cliquez ci-dessous pour confirmer et débloquer immédiatement l'école.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF78350F),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                isCheckingStatus = true
                                coroutineScope.launch {
                                    viewModel.checkPendingPaymentStatus { res ->
                                        isCheckingStatus = false
                                        if (res == "SUCCESS") {
                                            Toast.makeText(localContext, "Abonnement activé avec succès ! Accès débloqué.", Toast.LENGTH_LONG).show()
                                        } else if (res == "PENDING") {
                                            Toast.makeText(localContext, "Paiement en cours de traitement par l'opérateur (Orange/MTN). Réessayez dans un instant.", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(localContext, "Paiement non confirmé. Si vous avez été débité, cliquez sur 'Saisir ma référence ChapChap'.", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            enabled = !isCheckingStatus,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isCheckingStatus) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Filled.Refresh, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Vérifier et activer mon abonnement", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showManualPrompt = true }) {
                                Text("Saisir ID / Référence", color = Color(0xFFB45309), fontSize = 12.sp)
                            }
                            TextButton(onClick = { viewModel.clearPendingOrderId() }) {
                                Text("Nouveau paiement", color = Color(0xFF6B7280), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // UNIQUE MOYEN DE PAIEMENT : CHAPCHAPPAY.COM
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFDF4FF) // Soft fuchsia tint matching ChapChapPay
                ),
                border = BorderStroke(1.5.dp, Color(0xFFD946EF))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Paiement Rapide avec Chap Chap Pay",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC026D3)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Réglez directement et en toute sécurité via le portail chapchappay.com par Orange Money, MTN MoMo ou carte bancaire.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF475569)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFF0ABFC)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Montant de l'abonnement :",
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF334155),
                                fontSize = 14.sp
                            )
                            Text(
                                "300 000 GNF",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFC026D3),
                                fontSize = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            isLoadingChapChap = true
                            coroutineScope.launch {
                                val orderId = "SUB_${System.currentTimeMillis()}"
                                val desc = "Abonnement ScolaPay"
                                val result = com.example.utils.ChapChapPayApi.createPayment(300000.0, desc, orderId)
                                isLoadingChapChap = false
                                if (result != null) {
                                    viewModel.savePendingOrderId(result.orderId ?: orderId, result.operationId)
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(result.paymentUrl))
                                    localContext.startActivity(intent)
                                } else {
                                    Toast.makeText(localContext, "Erreur lors de la création du lien de paiement Chap Chap Pay.", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD946EF),
                            contentColor = Color.White
                        ),
                        enabled = !isLoadingChapChap,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoadingChapChap) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = "Payer avec Chap Chap Pay (300 000 GNF)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ASSISTANCE WHATSAPP
            Button(
                onClick = {
                    val schoolTitle = schoolAcc?.displayName?.ifBlank { schoolAcc?.schoolName } ?: "notre école"
                    val waMsg = "Bonjour Administration ScolaPay,\n\nJe vous contacte concernant l'abonnement de notre établissement *$schoolTitle*.\n\nMerci de nous assister."
                    try {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://api.whatsapp.com/send?phone=224628376566&text=${Uri.encode(waMsg)}")
                        }
                        localContext.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(localContext, "Impossible d'ouvrir WhatsApp. Contactez le 628 37 65 66.", Toast.LENGTH_LONG).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("💬 Contacter l'administrateur par WhatsApp", fontWeight = FontWeight.Bold, color = Color.White)
            }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Se déconnecter", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showManualPrompt) {
        AlertDialog(
            onDismissRequest = { showManualPrompt = false },
            title = { Text("Vérifier une référence ChapChapPay", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Si vous avez payé sur Chap Chap Pay mais que l'écran ne s'est pas débloqué, collez ici votre numéro de commande (SUB_...) ou l'ID d'opération ChapChapPay.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = manualRefInput,
                        onValueChange = { manualRefInput = it },
                        label = { Text("ID ou Réf de transaction") },
                        placeholder = { Text("Ex: SUB_178... ou UUID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ref = manualRefInput.trim()
                        if (ref.isNotBlank()) {
                            showManualPrompt = false
                            isCheckingStatus = true
                            viewModel.savePendingOrderId(ref)
                            coroutineScope.launch {
                                viewModel.checkPendingPaymentStatus(ref) { res ->
                                    isCheckingStatus = false
                                    if (res == "SUCCESS") {
                                        Toast.makeText(localContext, "Paiement confirmé ! Abonnement activé.", Toast.LENGTH_LONG).show()
                                    } else if (res == "PENDING") {
                                        Toast.makeText(localContext, "Paiement toujours en cours chez l'opérateur.", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(localContext, "Référence non trouvée ou non confirmée.", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Text("Vérifier")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualPrompt = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}
