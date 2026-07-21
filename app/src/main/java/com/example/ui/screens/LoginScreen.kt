package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.SchoolViewModel
import com.example.R
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: SchoolViewModel,
    onNavigateToDashboard: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val currentSchoolId by viewModel.currentSchoolId.collectAsStateWithLifecycle()

    LaunchedEffect(currentSchoolId) {
        if (currentSchoolId != null) {
            onNavigateToDashboard()
        }
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoggingIn by remember { mutableStateOf(false) }
    
    var showResetDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var resetSuccessMessage by remember { mutableStateOf<String?>(null) }
    var resetErrorMessage by remember { mutableStateOf<String?>(null) }
    var isResetting by remember { mutableStateOf(false) }
    var selectedResetRoleTab by remember { mutableStateOf(0) }
    
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF9FAFB),
                            Color(0xFFEBF2FC)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 28.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                
                // Centered 3D ScolaPay Logo
                Image(
                    painter = painterResource(id = R.drawable.scolapay_icon_1783078523543),
                    contentDescription = "Logo ScolaPay",
                    modifier = Modifier
                        .size(220.dp)
                        .padding(bottom = 8.dp),
                    contentScale = ContentScale.Fit
                )
                
                Text(
                    text = "Connexion",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D3E9B),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Connectez-vous à votre compte école",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail") },
                    placeholder = { Text("Ex: ecole@scolapay.com") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Email,
                            contentDescription = null,
                            tint = Color(0xFF9CA3AF)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color(0xFF1F2937),
                        unfocusedTextColor = Color(0xFF1F2937),
                        disabledTextColor = Color(0xFF9CA3AF),
                        focusedBorderColor = Color(0xFF0F56E3),
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedLabelColor = Color(0xFF0F56E3),
                        unfocusedLabelColor = Color(0xFF6B7280)
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    enabled = !isLoggingIn,
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mot de passe") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = Color(0xFF9CA3AF)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color(0xFF1F2937),
                        unfocusedTextColor = Color(0xFF1F2937),
                        disabledTextColor = Color(0xFF9CA3AF),
                        focusedBorderColor = Color(0xFF0F56E3),
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedLabelColor = Color(0xFF0F56E3),
                        unfocusedLabelColor = Color(0xFF6B7280)
                    ),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (passwordVisible) "Masquer le mot de passe" else "Afficher le mot de passe"
                        IconButton(
                            onClick = { passwordVisible = !passwordVisible },
                            enabled = !isLoggingIn
                        ) {
                            Icon(imageVector = image, contentDescription = description, tint = Color(0xFF6B7280))
                        }
                    },
                    enabled = !isLoggingIn,
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            resetEmail = email
                            showResetDialog = true
                        },
                        enabled = !isLoggingIn
                    ) {
                        Text(
                            text = "Mot de passe oublié ?",
                            color = if (isLoggingIn) Color(0xFF9CA3AF) else Color(0xFF0F56E3),
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Login Button
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isLoggingIn = true
                            errorMessage = null
                            val success = viewModel.login(email.trim(), password)
                            if (success) {
                                onNavigateToDashboard()
                            } else {
                                val vmError = viewModel.loginError.value
                                errorMessage = vmError ?: "E-mail ou mot de passe incorrect"
                                isLoggingIn = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(4.dp, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0F56E3),
                        contentColor = Color.White
                    ),
                    enabled = email.isNotBlank() && password.isNotBlank() && !isLoggingIn
                ) {
                    if (isLoggingIn) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = "Se connecter",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // OR Divider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFE5E7EB)
                    )
                    Text(
                        text = "OU",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color(0xFF9CA3AF),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFE5E7EB)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Register Button
                OutlinedButton(
                    onClick = { if (!isLoggingIn) onNavigateToRegister() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(1.dp, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF0F56E3)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF0F56E3)),
                    enabled = !isLoggingIn
                ) {
                    Icon(
                        imageVector = Icons.Filled.School,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF0F56E3)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Créer un compte école",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Contact Support Bottom Text
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showSupportDialog = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Besoin d'aide ? ",
                        color = Color(0xFF6B7280),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Contactez le support",
                        color = Color(0xFF0F56E3),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isResetting) {
                    showResetDialog = false
                    resetSuccessMessage = null
                    resetErrorMessage = null
                }
            },
            title = { Text("Mot de passe oublié ?") },
            text = {
                Column {
                    TabRow(
                        selectedTabIndex = selectedResetRoleTab,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Tab(
                            selected = selectedResetRoleTab == 0,
                            onClick = { selectedResetRoleTab = 0 },
                            text = { Text("Fondateur") }
                        )
                        Tab(
                            selected = selectedResetRoleTab == 1,
                            onClick = { selectedResetRoleTab = 1 },
                            text = { Text("Financier") }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    if (selectedResetRoleTab == 0) {
                        Text(
                            text = "En tant que fondateur, votre compte est relié à l'adresse e-mail de l'école. Saisissez l'adresse e-mail ci-dessous pour recevoir un lien de réinitialisation.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = resetEmail,
                            onValueChange = { resetEmail = it },
                            label = { Text("E-mail de l'école") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            enabled = !isResetting
                        )
                        if (resetSuccessMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = resetSuccessMessage!!,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        if (resetErrorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = resetErrorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Gestion en direct par le fondateur",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Le mot de passe du Financier est géré par le Fondateur de l'école depuis son tableau de bord.\n\nSi vous avez oublié votre mot de passe, veuillez contacter le Fondateur de votre école. Il peut consulter votre mot de passe actuel ou vous en attribuer un nouveau directement en un clic depuis son application.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (selectedResetRoleTab == 0) {
                    TextButton(
                        onClick = {
                            if (resetEmail.isNotBlank()) {
                                coroutineScope.launch {
                                    isResetting = true
                                    resetSuccessMessage = null
                                    resetErrorMessage = null
                                    val success = viewModel.sendPasswordResetEmail(resetEmail.trim())
                                    isResetting = false
                                    if (success) {
                                        resetSuccessMessage = "Un e-mail de réinitialisation a été envoyé à $resetEmail."
                                    } else {
                                        resetErrorMessage = "Erreur lors de l'envoi de l'e-mail. Veuillez vérifier l'adresse."
                                    }
                                }
                            }
                        },
                        enabled = resetEmail.isNotBlank() && !isResetting
                    ) {
                        if (isResetting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Envoyer")
                        }
                    }
                } else {
                    TextButton(
                        onClick = {
                            showResetDialog = false
                        }
                    ) {
                        Text("Compris")
                    }
                }
            },
            dismissButton = {
                if (selectedResetRoleTab == 0) {
                    TextButton(
                        onClick = {
                            showResetDialog = false
                            resetSuccessMessage = null
                            resetErrorMessage = null
                        },
                        enabled = !isResetting
                    ) {
                        Text("Annuler")
                    }
                } else {
                    null
                }
            }
        )
    }

    if (showSupportDialog) {
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = { showSupportDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFEFF6FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Help,
                        contentDescription = null,
                        tint = Color(0xFF0F56E3),
                        modifier = Modifier.size(36.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Support ScolaPay",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Notre équipe d'assistance est à votre disposition pour vous guider ou résoudre vos difficultés.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4B5563),
                        textAlign = TextAlign.Center
                    )

                    // Contact Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Conseiller technique :",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Benjamin Tolno",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF1F2937)
                                    )
                                    Text(
                                        text = "+224 628 37 65 66",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = Color(0xFF0F56E3)
                                    )
                                }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "Disponible",
                                        color = Color(0xFF047857),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Direct Call Button
                    Button(
                        onClick = {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                    data = android.net.Uri.parse("tel:+224628376566")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Impossible de lancer l'appel", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F56E3)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Appel Direct (Téléphone)", fontWeight = FontWeight.Bold)
                    }

                    // WhatsApp Button
                    Button(
                        onClick = {
                            try {
                                val message = "Bonjour ScolaPay, j'utilise votre application de gestion scolaire et j'aurais besoin d'aide."
                                val encodedText = java.net.URLEncoder.encode(message, "UTF-8")
                                val uri = android.net.Uri.parse("https://api.whatsapp.com/send?phone=+224628376566&text=$encodedText")
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Impossible d'ouvrir WhatsApp", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Discuter sur WhatsApp", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSupportDialog = false }) {
                    Text("Fermer", fontWeight = FontWeight.Bold, color = Color(0xFF6B7280))
                }
            }
        )
    }
}
