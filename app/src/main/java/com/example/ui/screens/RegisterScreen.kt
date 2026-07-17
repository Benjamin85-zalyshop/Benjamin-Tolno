package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SchoolViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: SchoolViewModel,
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var schoolNameInput by remember { mutableStateOf("") }
    var schoolAddress by remember { mutableStateOf("") }
    var founderPhone by remember { mutableStateOf("") }
    
    // Founder passwords
    var founderPassword by remember { mutableStateOf("") } 
    var founderPasswordVisible by remember { mutableStateOf(false) }
    var confirmFounderPassword by remember { mutableStateOf("") }
    var confirmFounderPasswordVisible by remember { mutableStateOf(false) }
    
    // Financier passwords
    var financierPassword by remember { mutableStateOf("") }
    var financierPasswordVisible by remember { mutableStateOf(false) }
    var confirmFinancierPassword by remember { mutableStateOf("") }
    var confirmFinancierPasswordVisible by remember { mutableStateOf(false) }
    
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inscription École") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Créer un compte école",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Configurez l'email de l'école ainsi que les accès séparés pour le Fondateur et le Financier.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            // School Name Input
            OutlinedTextField(
                value = schoolNameInput,
                onValueChange = { schoolNameInput = it },
                label = { Text("Nom de l'école (Ex: DIVINE GRACE)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Email Input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail de l'école") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // School Address Input
            OutlinedTextField(
                value = schoolAddress,
                onValueChange = { schoolAddress = it },
                label = { Text("Adresse de l'école (Préfecture / Sous-préfecture)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Founder Section Header
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Accès Fondateur (Directeur)",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Founder Phone Number Input
            OutlinedTextField(
                value = founderPhone,
                onValueChange = { founderPhone = it },
                label = { Text("Numéro de téléphone du Fondateur") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            // Founder Password Input
            OutlinedTextField(
                value = founderPassword,
                onValueChange = { founderPassword = it },
                label = { Text("Mot de passe Fondateur") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (founderPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (founderPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (founderPasswordVisible) "Masquer" else "Afficher"
                    IconButton(onClick = { founderPasswordVisible = !founderPasswordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                },
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Confirm Founder Password Input
            OutlinedTextField(
                value = confirmFounderPassword,
                onValueChange = { confirmFounderPassword = it },
                label = { Text("Confirmer le mot de passe Fondateur") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (confirmFounderPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (confirmFounderPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (confirmFounderPasswordVisible) "Masquer" else "Afficher"
                    IconButton(onClick = { confirmFounderPasswordVisible = !confirmFounderPasswordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                },
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Financier Section Header
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Accès Financier (Séparé)",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // Financier Password Input
            OutlinedTextField(
                value = financierPassword,
                onValueChange = { financierPassword = it },
                label = { Text("Mot de passe Financier") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (financierPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (financierPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (financierPasswordVisible) "Masquer" else "Afficher"
                    IconButton(onClick = { financierPasswordVisible = !financierPasswordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                },
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Confirm Financier Password Input
            OutlinedTextField(
                value = confirmFinancierPassword,
                onValueChange = { confirmFinancierPassword = it },
                label = { Text("Confirmer le mot de passe Financier") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (confirmFinancierPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (confirmFinancierPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (confirmFinancierPasswordVisible) "Masquer" else "Afficher"
                    IconButton(onClick = { confirmFinancierPasswordVisible = !confirmFinancierPasswordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                },
                singleLine = true
            )
            
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    // Validations
                    if (schoolNameInput.trim().isBlank()) {
                        errorMessage = "Veuillez entrer le nom de l'école."
                    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                        errorMessage = "Veuillez entrer une adresse e-mail valide."
                    } else if (schoolAddress.trim().isBlank()) {
                        errorMessage = "Veuillez entrer l'adresse de l'école (Préfecture ou Sous-préfecture)."
                    } else if (founderPhone.trim().isBlank()) {
                        errorMessage = "Veuillez entrer le numéro de téléphone du fondateur."
                    } else if (founderPassword.length < 6) {
                        errorMessage = "Le mot de passe Fondateur doit contenir au moins 6 caractères."
                    } else if (founderPassword != confirmFounderPassword) {
                        errorMessage = "Les mots de passe du Fondateur ne correspondent pas."
                    } else if (financierPassword.length < 6) {
                        errorMessage = "Le mot de passe Financier doit contenir au moins 6 caractères."
                    } else if (financierPassword != confirmFinancierPassword) {
                        errorMessage = "Les mots de passe du Financier ne correspondent pas."
                    } else if (founderPassword == financierPassword) {
                        errorMessage = "Les mots de passe Fondateur et Financier doivent être différents."
                    } else {
                        errorMessage = null
                        coroutineScope.launch {
                            val success = viewModel.registerSchool(
                                email = email.trim(), 
                                founderPassword = founderPassword, 
                                financierPassword = financierPassword,
                                displayName = schoolNameInput.trim(),
                                address = schoolAddress.trim(),
                                founderPhone = founderPhone.trim()
                            )
                            if(success) {
                                onRegisterSuccess()
                            } else {
                                errorMessage = "Erreur lors de l'inscription. L'email est peut-être déjà utilisé."
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = schoolNameInput.isNotBlank() && email.isNotBlank() && schoolAddress.isNotBlank() && founderPhone.isNotBlank() && founderPassword.isNotBlank() && confirmFounderPassword.isNotBlank() && financierPassword.isNotBlank() && confirmFinancierPassword.isNotBlank()
            ) {
                Text("S'inscrire")
            }
        }
    }
}
