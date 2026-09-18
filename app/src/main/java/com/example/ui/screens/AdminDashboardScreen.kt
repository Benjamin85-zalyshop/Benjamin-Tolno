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
    var rejectionReason by remember { mutableStateOf("") }
    
    val coroutineScope = rememberCoroutineScope()
    var showFirebaseAuthDialog by remember { mutableStateOf(false) }
    var firebasePasswordInput by remember { mutableStateOf("") }
    var isAuthenticatingFirebase by remember { mutableStateOf(false) }
    var firebaseAuthErrorMessage by remember { mutableStateOf<String?>(null) }
    var firebasePasswordVisible by remember { mutableStateOf(false) }
    var firebaseResetSentMessage by remember { mutableStateOf<String?>(null) }
    
    val localContext = androidx.compose.ui.platform.LocalContext.current
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
        "Montant insuffisant (requis: 500 000 GNF)",
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
                selectedTab = 2 // Basculer automatiquement sur l'onglet "Tous" s'il n'y a pas d'écoles en attente
            }
        }
    }

    val filteredSchools = remember(schools, selectedTab) {
        when (selectedTab) {
            0 -> schools.filter { it.isPendingValidation }
            1 -> schools.filter { it.hasActiveSubscription }
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
            PrimaryTabRow(selectedTabIndex = selectedTab) {
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
                                onClick = { selectedTab = 2 }
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
                            }
                        )
                    }
                }
            }
        }
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
}

@Composable
fun SchoolRequestCard(
    item: SchoolAdminItem,
    onApprove: () -> Unit,
    onRejectClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onForceExpireClick: () -> Unit = {}
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

            if (item.isPendingValidation || item.hasActiveSubscription) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Filled.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error)
                    }
                    
                    Row {
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
                                onClick = onRejectClick,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.testTag("revoke_btn")
                            ) {
                                Icon(Icons.Filled.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Révoquer / Refuser")
                            }
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Filled.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error)
                    }
                }
                

            }
        }
    }
}
