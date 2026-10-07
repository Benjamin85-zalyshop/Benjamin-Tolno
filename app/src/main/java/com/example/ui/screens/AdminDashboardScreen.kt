package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.example.ui.SchoolViewModel
import com.example.ui.SchoolAdminItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: SchoolViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val schools by viewModel.adminSchools.collectAsStateWithLifecycle()
    val adminError by viewModel.adminError.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var schoolToReject by remember { mutableStateOf<SchoolAdminItem?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var schoolToDelete by remember { mutableStateOf<SchoolAdminItem?>(null) }
    var showCleanDialog by remember { mutableStateOf(false) }
    var showManageSubsDialog by remember { mutableStateOf(false) }
    var rejectionReason by remember { mutableStateOf("") }
    
    val coroutineScope = rememberCoroutineScope()
    var showFirebaseAuthDialog by remember { mutableStateOf(false) }
    var firebasePasswordInput by remember { mutableStateOf("") }
    var isAuthenticatingFirebase by remember { mutableStateOf(false) }
    var firebaseAuthErrorMessage by remember { mutableStateOf<String?>(null) }
    var firebasePasswordVisible by remember { mutableStateOf(false) }
    var firebaseResetSentMessage by remember { mutableStateOf<String?>(null) }
    
    val localContext = androidx.compose.ui.platform.LocalContext.current
    var showLockDialog by remember { mutableStateOf(false) }
    var schoolToLock by remember { mutableStateOf<SchoolAdminItem?>(null) }
    var lockReasonInput by remember { mutableStateOf("") }
    var showWhatsAppDialog by remember { mutableStateOf(false) }
    var schoolForWhatsApp by remember { mutableStateOf<SchoolAdminItem?>(null) }
    var whatsappMessage by remember { mutableStateOf("") }
    val whatsappSuggestions = listOf(
        "Bonjour, ceci est un rappel que votre abonnement à ScolaPay arrive à expiration. Veuillez le renouveler.",
        "Bonjour, votre demande d'abonnement à ScolaPay est en cours de traitement.",
        "Bonjour, nous avons une information importante concernant l'application ScolaPay.",
        "Bonjour, merci pour votre abonnement à ScolaPay ! Votre compte est maintenant activé pour un an. Nous restons à votre entière disposition."
    )
    
    // Quick template suggestions for rejection
    val rejectionSuggestions = listOf(
        "Identifiant de transaction incorrect",
        "Paiement non reçu",
        "Montant insuffisant (requis: 300 000 GNF)",
        "Numéro de téléphone invalide",
        "Transaction déjà validée"
    )

    LaunchedEffect(Unit) {
        viewModel.forceSyncSchools()
    }

    var hasAutoSwitchedTab by remember { mutableStateOf(false) }
    LaunchedEffect(schools) {
        if (!hasAutoSwitchedTab && schools.isNotEmpty()) {
            hasAutoSwitchedTab = true
            if (schools.none { it.isPendingValidation } && selectedTab == 0) {
                selectedTab = 3 // Basculer automatiquement sur l'onglet "Tous" s'il n'y a pas d'écoles en attente
            }
        }
    }

    val filteredSchools = remember(schools, selectedTab) {
        when (selectedTab) {
            0 -> schools.filter { it.isPendingValidation }
            1 -> schools.filter { it.hasActiveSubscription }
            2 -> schools.filter { !it.hasActiveSubscription }
            else -> schools
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("ScolaPay Admin", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text("Gestion des abonnements", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(
                        onClick = { showManageSubsDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Gérer les abonnements"
                        )
                    }
                    IconButton(
                        onClick = { showCleanDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Tout nettoyer"
                        )
                    }
                    IconButton(
                        onClick = {
                            viewModel.forceSyncSchools()
                            Toast.makeText(context, "Mise à jour des écoles...", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualiser"
                        )
                    }
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("admin_logout_btn")
                    ) {
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
        ) {
            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 8.dp
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.PendingActions, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("En attente (${schools.count { it.isPendingValidation }})")
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Abonnés (${schools.count { it.hasActiveSubscription }})")
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.PersonOff, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Non abonnés (${schools.count { !it.hasActiveSubscription }})")
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.List, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tous (${schools.size})")
                        }
                    }
                )
            }
            
            if (adminError != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Synchronisation Cloud Firestore",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = adminError ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    firebaseAuthErrorMessage = null
                                    firebaseResetSentMessage = null
                                    firebasePasswordInput = ""
                                    showFirebaseAuthDialog = true
                                },
                                modifier = Modifier.weight(1.1f)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Connexion Firebase")
                            }
                            FilledTonalButton(
                                onClick = {
                                    viewModel.forceSyncSchools()
                                    Toast.makeText(context, "Nouvelle tentative de synchronisation...", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(0.9f)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Réessayer")
                            }
                        }
                    }
                }
            }

            if (filteredSchools.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = when (selectedTab) {
                                0 -> Icons.Filled.CheckCircleOutline
                                1 -> Icons.Filled.ErrorOutline
                                else -> Icons.Filled.School
                            },
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = when (selectedTab) {
                                0 -> "Aucune demande d'abonnement en attente !"
                                1 -> "Aucune école n'a d'abonnement actif."
                                2 -> "Aucune école non abonnée."
                                else -> "Aucune école enregistrée."
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        )
                        if (selectedTab == 0 && schools.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { selectedTab = 3 }
                            ) {
                                Icon(Icons.Filled.List, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Voir toutes les écoles (${schools.size})")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredSchools) { item ->
                        SchoolRequestCard(
                            item = item,
                            onApprove = { viewModel.approveSchoolSubscription(item.email) },
                            onRejectClick = {
                                schoolToReject = item
                                rejectionReason = ""
                                showRejectDialog = true
                            },
                            onToggleSubscription = { active ->
                                viewModel.toggleSchoolSubscription(item.email, active)
                                android.widget.Toast.makeText(
                                    localContext,
                                    if (active) "Abonnement activé pour 1 an !" else "École passée en non-abonnée.",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            },
                            onDeleteClick = {
                                schoolToDelete = item
                                showDeleteDialog = true
                            },
                            onWhatsAppClick = {
                                schoolForWhatsApp = item
                                whatsappMessage = whatsappSuggestions.first()
                                showWhatsAppDialog = true
                            },
                            onForceExpireClick = {
                                viewModel.forceExpireSchool(item.email)
                                android.widget.Toast.makeText(localContext, "Expiration simulée pour ${item.email}", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            onToggleOnlinePayment = { enabled ->
                                viewModel.toggleSchoolOnlinePayment(item.email, item.displayName.ifEmpty { item.schoolName }, enabled)
                                android.widget.Toast.makeText(localContext, if (enabled) "Paiement en ligne activé pour ${item.displayName.ifEmpty { item.schoolName }}" else "Paiement en ligne bloqué pour ${item.displayName.ifEmpty { item.schoolName }}", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            onToggleAppLock = { locked ->
                                if (locked) {
                                    schoolToLock = item
                                    lockReasonInput = ""
                                    showLockDialog = true
                                } else {
                                    viewModel.toggleSchoolAppLock(item.email, item.displayName.ifEmpty { item.schoolName }, false, reason = "")
                                    android.widget.Toast.makeText(localContext, "Application déverrouillée pour ${item.displayName.ifEmpty { item.schoolName }}", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            onResetCommission = {
                                viewModel.resetSchoolCommission(item.email, item.displayName.ifEmpty { item.schoolName })
                                android.widget.Toast.makeText(localContext, "Commission réinitialisée à 0 pour ${item.displayName.ifEmpty { item.schoolName }}", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            onSendCommissionInvoice = {
                                val cleanPhone = item.founderPhone.trim().replace(" ", "").replace("+", "")
                                val schoolTitle = item.displayName.ifEmpty { item.schoolName }
                                val invoiceMsg = "Bonjour Direction de *$schoolTitle*,\n\nVoici le point de vos paiements de scolarité perçus en ligne via ScolaPay :\n• Nombre de paiements : ${item.onlinePaymentsCount}\n• Total des règlements : ${item.onlinePaymentsTotal} GNF\n• Commission due à ScolaPay : *${item.unpaidCommission} GNF*\n\nMerci d'effectuer le reversement de la commission par Orange Money / Mobile Money pour maintenir le service actif.\n\nCordialement,\nService Financier ScolaPay / zalytechno"
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(invoiceMsg)}")
                                    }
                                    localContext.startActivity(intent)
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(localContext, "Impossible d'ouvrir WhatsApp", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showManageSubsDialog) {
        AlertDialog(
            onDismissRequest = { showManageSubsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gestion des abonnements", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Régularisez facilement les abonnements de vos écoles. Si des écoles ont été marquées comme abonnées alors qu'elles ne l'étaient pas, vous pouvez corriger leur statut ici :",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Actions automatiques :", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                            
                            Button(
                                onClick = {
                                    viewModel.resetUnverifiedSchoolsToNonSubscribed(resetAll = false)
                                    showManageSubsDialog = false
                                    Toast.makeText(context, "Écoles sans preuve de paiement repassées en non-abonnées !", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                            ) {
                                Text("Rétablir les écoles sans justificatif", fontSize = 13.sp)
                            }
                            
                            OutlinedButton(
                                onClick = {
                                    viewModel.resetUnverifiedSchoolsToNonSubscribed(resetAll = true)
                                    showManageSubsDialog = false
                                    Toast.makeText(context, "Toutes les écoles repassées en non-abonnées !", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Rétablir TOUTES les écoles en non-abonnées", fontSize = 13.sp)
                            }
                        }
                    }
                    Text(
                        "Astuce : Vous pouvez aussi activer ou désactiver l'abonnement de chaque école individuellement via le bouton sur sa carte.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showManageSubsDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }

    if (showRejectDialog && schoolToReject != null) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Cancel,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Refuser le paiement")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Veuillez indiquer le motif de refus pour l'école ${schoolToReject?.displayName ?: schoolToReject?.schoolName} :",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        label = { Text("Motif de refus") },
                        modifier = Modifier.fillMaxWidth().testTag("rejection_reason_input"),
                        minLines = 2,
                        maxLines = 4
                    )

                    Text(
                        text = "Suggestions rapides :",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        rejectionSuggestions.forEach { suggestion ->
                            SuggestionChip(
                                onClick = { rejectionReason = suggestion },
                                label = { Text(suggestion, style = MaterialTheme.typography.bodySmall) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (rejectionReason.isNotBlank()) {
                            viewModel.rejectSchoolSubscription(schoolToReject!!.email, rejectionReason)
                            showRejectDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    enabled = rejectionReason.isNotBlank(),
                    modifier = Modifier.testTag("submit_rejection_btn")
                ) {
                    Text("Confirmer le refus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showCleanDialog) {
        AlertDialog(
            onDismissRequest = { showCleanDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tout nettoyer")
                }
            },
            text = { Text("Voulez-vous supprimer toutes les écoles sauf le compte Administrateur ? Cette action est irréversible.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAllNonAdminSchools()
                        showCleanDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Oui, nettoyer")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCleanDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showFirebaseAuthDialog) {
        AlertDialog(
            onDismissRequest = { 
                if (!isAuthenticatingFirebase) showFirebaseAuthDialog = false 
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Connexion Cloud Firebase")
                }
            },
            text = {
                Column {
                    Text(
                        "Pour autoriser la synchronisation Firestore des abonnements, connectez-vous avec votre compte Firebase :",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = "benjamintolno7@gmail.com",
                        onValueChange = {},
                        label = { Text("E-mail administrateur") },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = firebasePasswordInput,
                        onValueChange = { 
                            firebasePasswordInput = it
                            firebaseAuthErrorMessage = null
                        },
                        label = { Text("Mot de passe Firebase") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = if (firebasePasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { firebasePasswordVisible = !firebasePasswordVisible }) {
                                Icon(
                                    if (firebasePasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                    if (firebaseAuthErrorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            firebaseAuthErrorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (firebaseResetSentMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            firebaseResetSentMessage!!,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                val sent = viewModel.sendPasswordResetEmail("benjamintolno7@gmail.com")
                                if (sent) {
                                    firebaseResetSentMessage = "E-mail de réinitialisation envoyé à benjamintolno7@gmail.com. Vérifiez vos spams."
                                } else {
                                    firebaseAuthErrorMessage = "Impossible d'envoyer l'e-mail de réinitialisation."
                                }
                            }
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Mot de passe oublié ?")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (firebasePasswordInput.isNotBlank()) {
                            coroutineScope.launch {
                                isAuthenticatingFirebase = true
                                firebaseAuthErrorMessage = null
                                val (ok, errorMsg) = viewModel.authenticateAdminWithFirebase(firebasePasswordInput)
                                isAuthenticatingFirebase = false
                                if (ok) {
                                    showFirebaseAuthDialog = false
                                    Toast.makeText(context, "Connecté à Firebase ! Synchronisation réussie.", Toast.LENGTH_LONG).show()
                                } else {
                                    firebaseAuthErrorMessage = errorMsg ?: "Échec : mot de passe incorrect."
                                }
                            }
                        }
                    },
                    enabled = firebasePasswordInput.isNotBlank() && !isAuthenticatingFirebase
                ) {
                    if (isAuthenticatingFirebase) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Connexion...")
                    } else {
                        Text("Se connecter")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showFirebaseAuthDialog = false },
                    enabled = !isAuthenticatingFirebase
                ) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showDeleteDialog && schoolToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.DeleteForever,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Supprimer l'école")
                }
            },
            text = {
                Text("Voulez-vous vraiment supprimer définitivement le compte de l'école ${schoolToDelete?.displayName ?: schoolToDelete?.schoolName} ? Cette action est irréversible.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSchoolAccount(schoolToDelete!!.email)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Oui, supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
    
    if (showWhatsAppDialog && schoolForWhatsApp != null) {
        val school = schoolForWhatsApp!!
        AlertDialog(
            onDismissRequest = { showWhatsAppDialog = false },
            title = { Text("Message WhatsApp") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "À : ${school.schoolName}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Modèles de message :",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(whatsappSuggestions, key = { it }) { suggestion ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { whatsappMessage = suggestion }
                            ) {
                                Text(
                                    text = suggestion,
                                    modifier = Modifier.padding(8.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = whatsappMessage,
                        onValueChange = { whatsappMessage = it },
                        label = { Text("Message") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            var phone = school.founderPhone.replace(" ", "").replace("+", "")
                            if (!phone.startsWith("224") && phone.length == 9) {
                                phone = "224$phone"
                            }
                            val url = "https://wa.me/$phone?text=${android.net.Uri.encode(whatsappMessage)}"
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                data = android.net.Uri.parse(url)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        showWhatsAppDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Icon(imageVector = Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ouvrir WhatsApp")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWhatsAppDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showLockDialog && schoolToLock != null) {
        val s = schoolToLock!!
        AlertDialog(
            onDismissRequest = { showLockDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFDC2626))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verrouiller l'accès de l'école", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Établissement concerné :",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = s.displayName.ifEmpty { s.schoolName },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Indiquez le motif du verrouillage (ce message sera affiché à la direction de l'école) :",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    OutlinedTextField(
                        value = lockReasonInput,
                        onValueChange = { lockReasonInput = it },
                        label = { Text("Motif du verrouillage") },
                        placeholder = { Text("Ex: Abonnement annuel expiré, facture non régularisée...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                    Text("Suggestions rapides :", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            "Abonnement annuel expiré. Merci de renouveler pour réactiver vos accès.",
                            "Suspension administrative du compte ScolaPay.",
                            "Facture de prestation de service impayée. Merci de régulariser."
                        ).forEach { suggestion ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { lockReasonInput = suggestion }
                            ) {
                                Text(
                                    text = suggestion,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val enteredReason = lockReasonInput.trim().ifEmpty {
                            "Accès à l'application ScolaPay suspendu par l'administration."
                        }
                        viewModel.toggleSchoolAppLock(
                            email = s.email,
                            schoolName = s.displayName.ifEmpty { s.schoolName },
                            locked = true,
                            reason = enteredReason
                        )
                        showLockDialog = false
                        android.widget.Toast.makeText(
                            localContext,
                            "Application verrouillée pour ${s.displayName.ifEmpty { s.schoolName }}",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    enabled = lockReasonInput.isNotBlank()
                ) {
                    Text("Confirmer le verrouillage")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLockDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun SchoolRequestCard(
    item: SchoolAdminItem,
    onApprove: () -> Unit,
    onRejectClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onForceExpireClick: () -> Unit = {},
    onToggleOnlinePayment: (Boolean) -> Unit = {},
    onToggleAppLock: (Boolean) -> Unit = {},
    onResetCommission: () -> Unit = {},
    onSendCommissionInvoice: () -> Unit = {},
    onToggleSubscription: (Boolean) -> Unit = {}
) {
    val localContext = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.displayName.ifEmpty { item.schoolName },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    
                    if (item.address.isNotEmpty()) {
                        Text(
                            text = "📍 Adr : ${item.address}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    
                    if (item.founderPhone.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:${item.founderPhone}")
                                        }
                                        localContext.startActivity(intent)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            ) {
                                Text(
                                    text = "📞 Tél Fondateur : ${item.founderPhone}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A),
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF25D366).copy(alpha = 0.15f),
                                contentColor = Color(0xFF128C7E),
                                modifier = Modifier
                                    .clickable { onWhatsAppClick() }
                                    .padding(vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Send,
                                        contentDescription = "WhatsApp",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "WhatsApp",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    if (item.hasActiveSubscription) {
                        val isExpired = (item.subscriptionExpiryDate ?: 0L) > 0 && (item.subscriptionExpiryDate ?: 0L) <= System.currentTimeMillis()
                        if (isExpired) {
                            Text(
                                text = "Abonnement expiré",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFDC2626),
                                fontWeight = FontWeight.Medium
                            )
                        } else if ((item.subscriptionExpiryDate ?: 0L) > 0) {
                            val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                            val expiryStr = dateFormat.format(java.util.Date(item.subscriptionExpiryDate ?: 0L))
                            Text(
                                text = "Expire le : $expiryStr",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF16A34A),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        val elapsed = System.currentTimeMillis() - item.createdAt
                        val trialDuration = 90L * 24L * 60L * 60L * 1000L
                        val isTrialActive = elapsed < trialDuration
                        val daysRemaining = ((trialDuration - elapsed) / (24L * 60L * 60L * 1000L)).coerceAtLeast(0L)
                        
                        Text(
                            text = if (isTrialActive) "Essai : $daysRemaining j restants" else "Essai expiré",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isTrialActive) Color(0xFF2563EB) else Color(0xFFDC2626),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                val isExpired = item.hasActiveSubscription && (item.subscriptionExpiryDate ?: 0L) > 0 && (item.subscriptionExpiryDate ?: 0L) <= System.currentTimeMillis()

                val badgeColor = when {
                    item.isPendingValidation -> Color(0xFFFF9800) // Orange
                    isExpired -> Color(0xFFDC2626) // Red
                    item.hasActiveSubscription -> Color(0xFF4CAF50) // Green
                    else -> {
                        val elapsed = System.currentTimeMillis() - item.createdAt
                        val isTrialActive = elapsed < (90L * 24L * 60L * 60L * 1000L)
                        if (isTrialActive) Color(0xFF2563EB) else Color(0xFF9E9E9E)
                    }
                }

                val badgeText = when {
                    item.isPendingValidation -> "En attente"
                    isExpired -> "Abon. expiré"
                    item.hasActiveSubscription -> "Abonné"
                    else -> {
                        val elapsed = System.currentTimeMillis() - item.createdAt
                        val isTrialActive = elapsed < (90L * 24L * 60L * 60L * 1000L)
                        if (isTrialActive) "Essai actif" else "Expiré"
                    }
                }

                Surface(
                    color = badgeColor.copy(alpha = 0.15f),
                    contentColor = badgeColor,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = badgeText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            if (item.transactionId != null || item.paymentPhoneNumber != null) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Icon(
                            imageVector = Icons.Filled.PhoneAndroid,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Téléphone de paiement : ",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = item.paymentPhoneNumber ?: "Non spécifié",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Icon(
                            imageVector = Icons.Filled.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Identifiant transaction : ",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = item.transactionId ?: "Non spécifié",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                Text(
                    text = "Aucune information de transaction soumise.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            if (!item.rejectionReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Dernier motif de refus : ${item.rejectionReason}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            val isDark = androidx.compose.foundation.isSystemInDarkTheme()
            val lockCardBg = if (isDark) {
                if (item.isAppLocked) Color(0xFF3B1212) else Color(0xFF1E293B)
            } else {
                if (item.isAppLocked) Color(0xFFFEF2F2) else Color(0xFFF8FAFC)
            }
            val lockCardBorder = if (item.isAppLocked) Color(0xFFFCA5A5) else Color(0xFFE2E8F0)
            val lockTitleColor = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = lockCardBg,
                    contentColor = lockTitleColor
                ),
                border = BorderStroke(1.dp, lockCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (item.isAppLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = if (item.isAppLocked) Color(0xFFDC2626) else Color(0xFF16A34A),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Accès à l'application",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = lockTitleColor
                            )
                        }
                        Surface(
                            color = if (item.isAppLocked) Color(0xFFDC2626).copy(alpha = 0.15f) else Color(0xFF16A34A).copy(alpha = 0.15f),
                            contentColor = if (item.isAppLocked) Color(0xFFDC2626) else Color(0xFF16A34A),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (item.isAppLocked) "VERROUILLÉ" else "AUTORISÉ",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (item.isAppLocked) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Motif : ${item.lockReason?.ifBlank { "Accès suspendu par l'administration" } ?: "Accès suspendu par l'administration"}",
                            color = Color(0xFFDC2626),
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (item.isAppLocked) {
                        Button(
                            onClick = { onToggleAppLock(false) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Déverrouiller l'application",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onToggleAppLock(true) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                            border = BorderStroke(1.dp, Color(0xFFDC2626)),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Verrouiller l'application",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Filled.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error)
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.isPendingValidation) {
                        OutlinedButton(
                            onClick = onRejectClick,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(Icons.Filled.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Refuser")
                        }

                        Button(
                            onClick = onApprove,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            modifier = Modifier.testTag("approve_btn")
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Valider")
                        }
                    } else if (item.hasActiveSubscription) {
                        OutlinedButton(
                            onClick = { onToggleSubscription(false) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.testTag("revoke_btn")
                        ) {
                            Icon(Icons.Filled.PersonOff, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Passer en non-abonné", fontSize = 12.sp)
                        }
                    } else {
                        Button(
                            onClick = { onToggleSubscription(true) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            modifier = Modifier.testTag("activate_btn")
                        ) {
                            Icon(Icons.Filled.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Activer abonnement (1 an)", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
