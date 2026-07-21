package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    var rejectionReason by remember { mutableStateOf("") }
    
    // Quick template suggestions for rejection
    val rejectionSuggestions = listOf(
        "Identifiant de transaction incorrect",
        "Paiement non reçu",
        "Montant insuffisant (requis: 500 000 GNF)",
        "Numéro de téléphone invalide",
        "Transaction déjà validée"
    )

    LaunchedEffect(Unit) {
        viewModel.loadAdminSchools()
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
                        onClick = {
                            viewModel.loadAdminSchools()
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
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = adminError ?: "",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
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
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredSchools, key = { it.email }) { item ->
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
}

@Composable
fun SchoolRequestCard(
    item: SchoolAdminItem,
    onApprove: () -> Unit,
    onRejectClick: () -> Unit,
    onDeleteClick: () -> Unit
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
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .clickable {
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
                    }
                    
                    if (!item.hasActiveSubscription) {
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

                val badgeColor = when {
                    item.isPendingValidation -> Color(0xFFFF9800) // Orange
                    item.hasActiveSubscription -> Color(0xFF4CAF50) // Green
                    else -> {
                        val elapsed = System.currentTimeMillis() - item.createdAt
                        val isTrialActive = elapsed < (90L * 24L * 60L * 60L * 1000L)
                        if (isTrialActive) Color(0xFF2563EB) else Color(0xFF9E9E9E)
                    }
                }

                val badgeText = when {
                    item.isPendingValidation -> "En attente"
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
