package com.example.ui.screens

import android.widget.Toast
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import android.util.Base64
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ui.UserManualGenerator
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.SchoolViewModel
import com.example.data.models.DeletionRequest
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

val SECTIONS = listOf("Toutes les sections", "LA MATERNELLE", "LE PRIMAIRE", "LE COLLEGE", "LE LYCÉE", "L'UNIVERSITE", "L'ECOLE PROFESSIONNELLE")

val DEFAULT_CLASSES_BY_SECTION = mapOf(
    "LA MATERNELLE" to listOf("Petite Section", "Moyenne Section", "Grande Section"),
    "LE PRIMAIRE" to listOf("1ère Année", "2ème Année", "3ème Année", "4ème Année", "5ème Année", "6ème Année"),
    "LE COLLEGE" to listOf("7ème Année", "8ème Année", "9ème Année", "10ème Année"),
    "LE LYCÉE" to listOf("11ème Année", "12ème Année", "Terminale"),
    "L'UNIVERSITE" to listOf("Licence 1", "Licence 2", "Licence 3", "Licence 4"),
    "L'ECOLE PROFESSIONNELLE" to listOf("1ère Année Pro", "2ème Année Pro", "3ème Année Pro")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: SchoolViewModel,
    onNavigateToStudents: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val totalCollected by viewModel.totalCollected.collectAsStateWithLifecycle()
    val totalExpenses by viewModel.totalExpenses.collectAsStateWithLifecycle()
    val balance by viewModel.balance.collectAsStateWithLifecycle()
    val students by viewModel.students.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val schoolName by viewModel.schoolName.collectAsStateWithLifecycle()
    val schoolLogoBase64 by viewModel.schoolLogoBase64.collectAsStateWithLifecycle()
    val selectedSection by viewModel.selectedSection.collectAsStateWithLifecycle()
    val userRole by viewModel.userRole.collectAsStateWithLifecycle()
    val selectedSchoolYear by viewModel.selectedSchoolYear.collectAsStateWithLifecycle()
    
    val schoolAccount by viewModel.schoolAccount.collectAsStateWithLifecycle()
    val deletionRequests by viewModel.deletionRequests.collectAsStateWithLifecycle()
    val classFees by viewModel.classFees.collectAsStateWithLifecycle()

    val totalTheorique = remember(students, classFees) {
        students.sumOf { s ->
            classFees.find { it.grade == s.grade }?.feeAmount ?: 0L
        }
    }
    
    // States for various dialogs/features
    var showFinancierMgmtDialog by remember { mutableStateOf(false) }
    var showSchoolYearDialog by remember { mutableStateOf(false) }
    var newFinancierPassword by remember { mutableStateOf("") }
    var financierPasswordVisible by remember { mutableStateOf(false) }
    var isNewPasswordVisible by remember { mutableStateOf(false) }
    
    var isBalanceVisible by remember { mutableStateOf(true) }
    var paymentToDelete by remember { mutableStateOf<com.example.data.models.Payment?>(null) }
    
    // Quick access dialogs
    var showFacturesDialog by remember { mutableStateOf(false) }
    var showCaissesDialog by remember { mutableStateOf(false) }
    var showScolariteDialog by remember { mutableStateOf(false) }
    var showRapportsDialog by remember { mutableStateOf(false) }
    var showCommuniquesDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }
    var showInscriptionDialog by remember { mutableStateOf(false) }

    val exportManualPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf"),
        onResult = { uri ->
            uri?.let {
                UserManualGenerator.generateUserManualPdf(context, it)
                Toast.makeText(context, "Manuel d'utilisation généré avec succès !", Toast.LENGTH_SHORT).show()
            }
        }
    )

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                if (bytes != null) {
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    if (bitmap != null) {
                        val maxLength = 300
                        val resizedBitmap = if (bitmap.width > maxLength || bitmap.height > maxLength) {
                            val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                            val targetWidth = if (bitmap.width > bitmap.height) maxLength else (maxLength * aspectRatio).toInt()
                            val targetHeight = if (bitmap.width > bitmap.height) (maxLength / aspectRatio).toInt() else maxLength
                            Bitmap.createScaledBitmap(bitmap, targetWidth.coerceAtLeast(1), targetHeight.coerceAtLeast(1), true)
                        } else {
                            bitmap
                        }
                        val outputStream = ByteArrayOutputStream()
                        resizedBitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
                        val compressedBytes = outputStream.toByteArray()
                        val base64String = Base64.encodeToString(compressedBytes, Base64.DEFAULT)
                        viewModel.setSchoolLogo(base64String)
                        Toast.makeText(context, "Logo importé avec succès !", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Impossible de charger l'image.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Erreur lors de l'importation: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Professional WhatsApp communication center state
    var commsMessageText by remember { mutableStateOf("Bonjour Chers Parents, nous vous rappelons que le solde restant pour les frais de scolarité de {élève} en classe de {classe} est de {solde_dû}. Merci de régulariser au plus vite via ScolaPay. Cordialement, la Direction.") }
    var commsRecipientType by remember { mutableStateOf("STUDENT") } // "STUDENT", "CLASS", "ALL"
    var commsSearchQuery by remember { mutableStateOf("") }
    var commsSelectedStudent by remember { mutableStateOf<com.example.data.models.Student?>(null) }
    var commsSelectedClass by remember { mutableStateOf("1ère Année") }
    var commsActiveTab by remember { mutableStateOf(0) } // 0 = Composer, 1 = Modèles de message


    val numberFormat = NumberFormat.getNumberInstance(Locale("fr", "GN"))
    
    val currentCollected = totalCollected ?: 0L
    val currentExpenses = totalExpenses ?: 0L
    val currentBalance = balance ?: 0L

    val formattedCollected = numberFormat.format(currentCollected) + " GNF"
    val formattedExpenses = numberFormat.format(currentExpenses) + " GNF"
    val formattedBalance = numberFormat.format(currentBalance) + " GNF"
    
    val isAppAccessGranted by viewModel.isAppAccessGranted.collectAsStateWithLifecycle()
    val isTrialActive by viewModel.isTrialActive.collectAsStateWithLifecycle()
    val trialDaysRemaining by viewModel.trialDaysRemaining.collectAsStateWithLifecycle()
    val hasActiveSubscription by viewModel.hasActiveSubscription.collectAsStateWithLifecycle()
    val isPendingValidation by viewModel.isPendingValidation.collectAsStateWithLifecycle()

    var showDirectSubscriptionDialog by remember { mutableStateOf(false) }

    if (userRole == "ADMIN") {
        AdminDashboardScreen(viewModel = viewModel, onLogout = onLogout)
        return
    }

    if (!isAppAccessGranted) {
        SubscriptionScreen(viewModel = viewModel, isPendingValidation = isPendingValidation, onLogout = onLogout)
        return
    }

    Scaffold(
        containerColor = Color(0xFF0F56E3) // Deep blue background matching top bar of the screenshot
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. STYLISH TOP APP BAR AREA (Integrated into blue background)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = Color.White,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable {
                                Toast.makeText(context, "ScolaPay - Menu principal", Toast.LENGTH_SHORT).show()
                            }
                    )
                    
                    // Logo text style: "Scola" in white, "Pay" in beautiful green
                    Row {
                        Text(
                            text = "Scola",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Pay",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981) // High-quality green
                        )
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Share icon button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable {
                                try {
                                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(android.content.Intent.EXTRA_SUBJECT, "ScolaPay")
                                        putExtra(
                                            android.content.Intent.EXTRA_TEXT,
                                            "Découvrez ScolaPay, l'application moderne de gestion financière scolaire ! Elle permet de gérer facilement la scolarité et les inscriptions, d'envoyer des reçus PDF professionnels avec logo, de faire des appels directs aux parents et d'envoyer des relances automatiques par WhatsApp.\n\n👉 Téléchargez et installez l'application immédiatement depuis Google Play Store :\nhttps://play.google.com/store/apps/details?id=com.aistudio.scolapay.gnf"
                                        )
                                    }
                                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Partager ScolaPay via"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Impossible de lancer le partage", Toast.LENGTH_SHORT).show()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Partager l'application",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Notification bell icon
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable { showCommuniquesDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    // Profile button / Logout
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Déconnexion",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            // 2. MAIN BODY CONTAINER with rounded top corners
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(Color(0xFFF9FAFB)) // Sleek off-white/light gray background
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // BIENVENUE & ACADEMIC YEAR
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Bienvenue,",
                                    fontSize = 14.sp,
                                    color = Color(0xFF6B7280),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = schoolName ?: "École ScolaPay",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F2937),
                                    maxLines = 1
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        showSchoolYearDialog = true
                                    }
                                ) {
                                    Text(
                                        text = if (selectedSchoolYear == "Toutes les années") "Toutes les années scolaires" else "Année scolaire $selectedSchoolYear",
                                        fontSize = 12.sp,
                                        color = Color(0xFF4B5563)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = Color(0xFF4B5563),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            
                            // Current year badge card on the right
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                                modifier = Modifier
                                    .shadow(1.dp, RoundedCornerShape(12.dp))
                                    .clickable { showSchoolYearDialog = true },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = Color(0xFF0F56E3),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Exercice actuel",
                                            fontSize = 9.sp,
                                            color = Color(0xFF6B7280)
                                        )
                                        Text(
                                            text = if (selectedSchoolYear == "Toutes les années") "Toutes" else selectedSchoolYear,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1F2937)
                                        )
                                    }
                                }
                            }
                        }

                        if (isTrialActive && !hasActiveSubscription) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFF7ED) // Warm alert orange tint
                                ),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color(0xFFFED7AA)) // Soft orange border
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "Info d'essai",
                                            tint = Color(0xFFEA580C)
                                        )
                                        Text(
                                            text = "Période d'essai gratuite active",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFEA580C),
                                            fontSize = 15.sp
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        SuggestionChip(
                                            onClick = {},
                                            label = {
                                                Text(
                                                    text = "$trialDaysRemaining jours restants",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFFEA580C)
                                                )
                                            },
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = Color(0xFFFFEDD5)
                                            ),
                                            border = BorderStroke(1.dp, Color(0xFFFED7AA))
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Vous bénéficiez de 3 mois d'essai gratuit. Profitez de notre offre spéciale de lancement : abonnez-vous maintenant pour seulement 200 000 GNF/an au lieu de 500 000 GNF !",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF4F46E5), // Elegant indigo/blue text for promotional info
                                        fontSize = 13.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { showDirectSubscriptionDialog = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFEA580C)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Payment,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("S'abonner maintenant (200 000 GNF)", fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                    
                    // SECTION TABS AS MODERN FILTER PILLS
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(SECTIONS) { section ->
                                val isSelected = selectedSection == section
                                val containerColor = if (isSelected) Color(0xFF0F56E3) else Color.White
                                val contentColor = if (isSelected) Color.White else Color(0xFF4B5563)
                                val borderStroke = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE5E7EB))
                                
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(containerColor)
                                        .then(if (borderStroke != null) Modifier.border(borderStroke, RoundedCornerShape(20.dp)) else Modifier)
                                        .clickable { viewModel.setSection(section) }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = section,
                                        color = contentColor,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                    
                    // 3. GRADIENT BALANCE BANNER CARD WITH COIN ANIMATION
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(6.dp, RoundedCornerShape(24.dp)),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF0F56E3),
                                                Color(0xFF2563EB)
                                            )
                                        )
                                    )
                                    .padding(24.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Solde Actuel",
                                                color = Color.White.copy(alpha = 0.9f),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Icon(
                                                imageVector = if (isBalanceVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                                contentDescription = "Toggle Balance",
                                                tint = Color.White.copy(alpha = 0.9f),
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clickable { isBalanceVisible = !isBalanceVisible }
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = if (isBalanceVisible) formattedBalance else "•••••••• GNF",
                                            color = Color.White,
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "Solde disponible dans toutes les caisses",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 11.sp
                                        )
                                    }
                                    
                                    // Custom 3D Wallet Representation
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.AccountBalanceWallet,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(40.dp)
                                        )
                                        // Float gold coins visual
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFBBF24))
                                                .align(Alignment.TopEnd)
                                                .offset(x = (-4).dp, y = 4.dp)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFBBF24))
                                                .align(Alignment.BottomStart)
                                                .offset(x = 4.dp, y = (-4).dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // 4. METRICS ROW: ENCAISSEMENTS & DEPENSES CARDS
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Encaissements Card (Green theme)
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .shadow(2.dp, RoundedCornerShape(20.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFDCFCE7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = "Arrow Down",
                                            tint = Color(0xFF15803D),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    
                                    Column {
                                        Text(
                                            text = "Encaissements",
                                            fontSize = 12.sp,
                                            color = Color(0xFF15803D),
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = formattedCollected,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF166534)
                                        )
                                    }
                                    
                                    // Progress indicator pill
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFDCFCE7))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "Ce mois-ci",
                                                fontSize = 9.sp,
                                                color = Color(0xFF15803D)
                                            )
                                            Text(
                                                text = "+18.5%",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF166534)
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // Dépenses Card (Red theme)
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .shadow(2.dp, RoundedCornerShape(20.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFEE2E2)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowUpward,
                                            contentDescription = "Arrow Up",
                                            tint = Color(0xFFB91C1C),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    
                                    Column {
                                        Text(
                                            text = "Dépenses",
                                            fontSize = 12.sp,
                                            color = Color(0xFFB91C1C),
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = formattedExpenses,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF991B1B)
                                        )
                                    }
                                    
                                    // Progress indicator pill
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFFEE2E2))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "Ce mois-ci",
                                                fontSize = 9.sp,
                                                color = Color(0xFFB91C1C)
                                            )
                                            Text(
                                                text = "+9.3%",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF991B1B)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    if (userRole == "FOUNDER" && deletionRequests.isNotEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(2.dp, RoundedCornerShape(24.dp))
                                    .background(Color(0xFFFEF2F2))
                                    .border(BorderStroke(1.dp, Color(0xFFFCA5A5)), RoundedCornerShape(24.dp))
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Attention",
                                        tint = Color(0xFFDC2626)
                                    )
                                    Text(
                                        text = "Demandes de suppression d'élèves (${deletionRequests.size})",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF991B1B)
                                    )
                                }
                                
                                Text(
                                    text = "Le financier a demandé la suppression de ces élèves. Veuillez valider ou rejeter pour maintenir la crédibilité de votre base de données.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF7F1D1D)
                                )
                                
                                deletionRequests.forEach { request ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, Color(0xFFFEE2E2)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        text = request.studentName,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF111827),
                                                        fontSize = 14.sp
                                                    )
                                                    Text(
                                                        text = "${request.section} - ${request.grade}",
                                                        fontSize = 12.sp,
                                                        color = Color(0xFF4B5563)
                                                    )
                                                }
                                                
                                                val dateStr = remember(request.requestedAt) {
                                                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("fr", "GN"))
                                                    sdf.format(Date(request.requestedAt))
                                                }
                                                Text(
                                                    text = dateStr,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF6B7280)
                                                )
                                            }
                                            
                                            HorizontalDivider(color = Color(0xFFF3F4F6))
                                            
                                            Text(
                                                text = "Motif : \"${request.reason}\"",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFFB91C1C)
                                            )
                                            
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Button(
                                                    onClick = { viewModel.rejectDeletionRequest(request) },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color(0xFFF3F4F6),
                                                        contentColor = Color(0xFF374151)
                                                    ),
                                                    modifier = Modifier.weight(1f),
                                                    contentPadding = PaddingValues(vertical = 8.dp)
                                                ) {
                                                    Text("Rejeter", fontSize = 12.sp)
                                                }
                                                
                                                Button(
                                                    onClick = { 
                                                        viewModel.approveDeletionRequest(request)
                                                        Toast.makeText(context, "Élève supprimé de la base de données", Toast.LENGTH_SHORT).show()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color(0xFFDC2626),
                                                        contentColor = Color.White
                                                    ),
                                                    modifier = Modifier.weight(1f),
                                                    contentPadding = PaddingValues(vertical = 8.dp)
                                                ) {
                                                    Text("Approuver", fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // 5. ACCÈS RAPIDES GRID SECTION
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(24.dp))
                                .background(Color.White)
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Accès rapides",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E3A8A)
                            )
                            
                            // Row 1
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                QuickAccessButton(
                                    title = "Élèves",
                                    icon = Icons.Default.People,
                                    iconColor = Color(0xFF3B82F6),
                                    bgColor = Color(0xFFEFF6FF),
                                    onClick = onNavigateToStudents
                                )
                                QuickAccessButton(
                                    title = "Paiements",
                                    icon = Icons.Default.AccountBalanceWallet,
                                    iconColor = Color(0xFF10B981),
                                    bgColor = Color(0xFFECFDF5),
                                    onClick = onNavigateToStudents
                                )
                                QuickAccessButton(
                                    title = "Dépenses",
                                    icon = Icons.Default.MoneyOff,
                                    iconColor = Color(0xFFF59E0B),
                                    bgColor = Color(0xFFFEF3C7),
                                    onClick = onNavigateToExpenses
                                )
                                QuickAccessButton(
                                    title = "Factures",
                                    icon = Icons.Default.Description,
                                    iconColor = Color(0xFF8B5CF6),
                                    bgColor = Color(0xFFF5F3FF),
                                    onClick = { showFacturesDialog = true }
                                )
                            }
                            
                            // Row 2
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                QuickAccessButton(
                                    title = "Caisses",
                                    icon = Icons.Default.Savings,
                                    iconColor = Color(0xFF06B6D4),
                                    bgColor = Color(0xFFECFEFF),
                                    onClick = { showCaissesDialog = true }
                                )
                                QuickAccessButton(
                                    title = "Scolarité",
                                    icon = Icons.Default.School,
                                    iconColor = Color(0xFFEA580C),
                                    bgColor = Color(0xFFFFF7ED),
                                    onClick = { showScolariteDialog = true }
                                )
                                QuickAccessButton(
                                    title = "Rapports",
                                    icon = Icons.Default.BarChart,
                                    iconColor = Color(0xFF6366F1),
                                    bgColor = Color(0xFFEEF2FF),
                                    onClick = { showRapportsDialog = true }
                                )
                                QuickAccessButton(
                                    title = "Messages",
                                    icon = Icons.Default.Notifications,
                                    iconColor = Color(0xFFF59E0B),
                                    bgColor = Color(0xFFFFFBEB),
                                    onClick = { showCommuniquesDialog = true }
                                )
                            }

                            // Row 3
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                QuickAccessButton(
                                    title = "Paramètres",
                                    icon = Icons.Default.Settings,
                                    iconColor = Color(0xFF6B7280),
                                    bgColor = Color(0xFFF3F4F6),
                                    onClick = {
                                        if (userRole == "FOUNDER") {
                                            newFinancierPassword = ""
                                            showFinancierMgmtDialog = true
                                        } else {
                                            Toast.makeText(context, "Réservé au fondateur de l'école", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                                QuickAccessButton(
                                    title = "Support",
                                    icon = Icons.Default.Help,
                                    iconColor = Color(0xFF0F56E3),
                                    bgColor = Color(0xFFEFF6FF),
                                    onClick = { showSupportDialog = true }
                                )
                                QuickAccessButton(
                                    title = "Inscription",
                                    icon = Icons.Default.AppRegistration,
                                    iconColor = Color(0xFFEC4899),
                                    bgColor = Color(0xFFFDF2F8),
                                    onClick = { showInscriptionDialog = true }
                                )
                                Spacer(modifier = Modifier.width(68.dp))
                            }
                        }
                    }
                    
                    // 6. ENCAISSÉ PAR CLASSE LIST
                    item {
                        Text(
                            text = "Encaissé par Classe",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    
                    item {
                        val paymentsByClass = payments.groupBy { payment ->
                            students.find { it.id == payment.studentId }?.grade ?: "Inconnu"
                        }.mapValues { (_, classPayments) ->
                            classPayments.sumOf { it.amount }
                        }.toList().sortedByDescending { it.second }

                        if (paymentsByClass.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Aucun paiement enregistré pour le moment.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF6B7280)
                                    )
                                }
                            }
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(paymentsByClass) { (className, totalAmount) ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                                        modifier = Modifier
                                            .width(150.dp)
                                            .shadow(1.dp, RoundedCornerShape(16.dp)),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = className,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1F2937),
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "${numberFormat.format(totalAmount)} GNF",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF10B981)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // 7. PAIEMENTS RÉCENTS LIST
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Paiements récents",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E3A8A)
                            )
                            Text(
                                text = "Voir tout",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F56E3),
                                modifier = Modifier.clickable { onNavigateToStudents() }
                            )
                        }
                    }
                    
                    if (payments.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Aucun paiement récent enregistré.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF6B7280)
                                    )
                                }
                            }
                        }
                    } else {
                        items(payments.take(10)) { payment ->
                            val student = students.find { it.id == payment.studentId }
                            val studentName = student?.let { "${it.firstName} ${it.lastName}" } ?: "Inconnu"
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(1.dp, RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFEFF6FF)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.People,
                                                contentDescription = null,
                                                tint = Color(0xFF3B82F6),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        
                                        Column {
                                            Text(
                                                text = studentName,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1F2937)
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = "${payment.reason} (${student?.grade ?: ""})",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF6B7280),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f, fill = false)
                                                )
                                                
                                                val methodColor = when (payment.paymentMethod) {
                                                    "Espèces" -> Color(0xFF10B981)
                                                    "Orange Money" -> Color(0xFFF97316)
                                                    "Mobile Money" -> Color(0xFFEAB308)
                                                    "ScolaPay" -> Color(0xFF0F56E3)
                                                    "Virement" -> Color(0xFF6B7280)
                                                    "Chèque" -> Color(0xFF8B5CF6)
                                                    else -> Color(0xFF6B7280)
                                                }
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .background(methodColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = payment.paymentMethod,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = methodColor
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "+${numberFormat.format(payment.amount)} GNF",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF10B981),
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                        
                                        if (userRole == "FINANCIER" || userRole == "FOUNDER") {
                                            IconButton(
                                                onClick = { paymentToDelete = payment },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Supprimer",
                                                    tint = Color(0xFFEF4444),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }

    // A. DIALOG: GERER LE COMPTE FINANCIER
    if (showFinancierMgmtDialog) {
        val currentFinancierPassword = schoolAccount?.financierPasswordHash ?: "Non défini"
        AlertDialog(
            onDismissRequest = { showFinancierMgmtDialog = false },
            title = { Text("Gérer le compte Financier", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Le Financier utilise l'e-mail de l'école pour se connecter, mais avec son propre mot de passe.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Informations de connexion",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Identifiant (E-mail) : ${schoolAccount?.schoolName ?: ""}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Mot de passe actuel : " + if (financierPasswordVisible) currentFinancierPassword else "••••••••",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { financierPasswordVisible = !financierPasswordVisible }) {
                                    Icon(
                                        imageVector = if (financierPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = if (financierPasswordVisible) "Masquer" else "Afficher"
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Attribuer un nouveau mot de passe",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    OutlinedTextField(
                        value = newFinancierPassword,
                        onValueChange = { newFinancierPassword = it },
                        label = { Text("Nouveau mot de passe") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { isNewPasswordVisible = !isNewPasswordVisible }) {
                                Icon(
                                    imageVector = if (isNewPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (isNewPasswordVisible) "Masquer" else "Afficher"
                                )
                            }
                        },
                        visualTransformation = if (isNewPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newFinancierPassword.isNotBlank()) {
                            viewModel.updateFinancierPassword(newFinancierPassword.trim())
                            showFinancierMgmtDialog = false
                            Toast.makeText(context, "Mot de passe financier mis à jour !", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = newFinancierPassword.isNotBlank() && newFinancierPassword.length >= 6
                ) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinancierMgmtDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }

    // B. DIALOG: FACTURES (INVOICES)
    if (showFacturesDialog) {
        AlertDialog(
            onDismissRequest = { showFacturesDialog = false },
            title = { Text("Factures & Facturation", fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (selectedSchoolYear == "Toutes les années") "Rapport de facturation globale pour toutes les années scolaires." else "Rapport de facturation globale pour l'année scolaire $selectedSchoolYear.",
                        fontSize = 13.sp,
                        color = Color(0xFF4B5563)
                    )
                    
                    HorizontalDivider(color = Color(0xFFE5E7EB))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total Scolarisé (Théorique) :", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "${numberFormat.format(totalTheorique)} GNF", color = Color(0xFF1F2937), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total Déjà Recouvré :", color = Color(0xFF10B981), fontSize = 13.sp)
                        Text(text = "$formattedCollected GNF", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    
                    val currentTuitionCollected = payments.filter { it.reason != "Inscription" && it.reason != "Réinscription" }.sumOf { it.amount }
                    val restToPay = totalTheorique - currentTuitionCollected
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Reste à Recouvrer :", color = Color(0xFFEF4444), fontSize = 13.sp)
                        Text(text = "${numberFormat.format(restToPay.coerceAtLeast(0L))} GNF", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    
                    HorizontalDivider(color = Color(0xFFE5E7EB))
                    
                    Text(
                        text = "Note: ScolaPay génère et met à jour automatiquement les reçus de paiement WhatsApp des élèves.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showFacturesDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F56E3))
                ) {
                    Text("D'accord")
                }
            }
        )
    }

    // C. DIALOG: CAISSES
    if (showCaissesDialog) {
        AlertDialog(
            onDismissRequest = { showCaissesDialog = false },
            title = { Text("Gestion des Caisses ScolaPay", fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Répartition du solde disponible actuel ($formattedBalance) :",
                        fontSize = 13.sp,
                        color = Color(0xFF4B5563)
                    )
                    
                    HorizontalDivider(color = Color(0xFFE5E7EB))
                    
                    // Caisse Principale (70%)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "1. Caisse Principale (70%)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "${numberFormat.format((currentBalance * 0.7).toLong())} GNF", color = Color(0xFF1F2937), fontSize = 13.sp)
                        }
                        LinearProgressIndicator(progress = { 0.7f }, modifier = Modifier.fillMaxWidth(), color = Color(0xFF0F56E3))
                    }
                    
                    // Caisse Mobile Money (20%)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "2. Mobile Money / Orange (20%)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "${numberFormat.format((currentBalance * 0.2).toLong())} GNF", color = Color(0xFF1F2937), fontSize = 13.sp)
                        }
                        LinearProgressIndicator(progress = { 0.2f }, modifier = Modifier.fillMaxWidth(), color = Color(0xFF10B981))
                    }
                    
                    // Caisse Banque (10%)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "3. Banque / Chèques (10%)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "${numberFormat.format((currentBalance * 0.1).toLong())} GNF", color = Color(0xFF1F2937), fontSize = 13.sp)
                        }
                        LinearProgressIndicator(progress = { 0.1f }, modifier = Modifier.fillMaxWidth(), color = Color(0xFFF59E0B))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCaissesDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F56E3))
                ) {
                    Text("Fermer")
                }
            }
        )
    }

    // D. DIALOG: RAPPORTS
    if (showRapportsDialog) {
        val totalSchoolIncomes = currentCollected
        AlertDialog(
            onDismissRequest = { showRapportsDialog = false },
            title = { Text("Rapports des Flux Financiers", fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Synthèse visuelle de l'année scolaire en cours :",
                        fontSize = 13.sp,
                        color = Color(0xFF4B5563)
                    )
                    
                    HorizontalDivider(color = Color(0xFFE5E7EB))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Élèves inscrits :", fontSize = 13.sp)
                        Text(text = "${students.size} élèves", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Revenus encaissés :", fontSize = 13.sp)
                        Text(text = "$formattedCollected", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Dépenses d'exploitation :", fontSize = 13.sp)
                        Text(text = "$formattedExpenses", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    
                    HorizontalDivider(color = Color(0xFFE5E7EB))
                    
                    // Mini Visual Bar Representation
                    Text(text = "Utilisation des fonds encaissés :", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    val expensesRatio = if (totalSchoolIncomes > 0L) (currentExpenses.toFloat() / totalSchoolIncomes.toFloat()) else 0f
                    val savingsRatio = (1f - expensesRatio).coerceIn(0f, 1f)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .clip(RoundedCornerShape(9.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(savingsRatio.coerceAtLeast(0.01f))
                                .fillMaxHeight()
                                .background(Color(0xFF10B981))
                        )
                        Box(
                            modifier = Modifier
                                .weight(expensesRatio.coerceAtLeast(0.01f))
                                .fillMaxHeight()
                                .background(Color(0xFFEF4444))
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Solde Epargné : ${(savingsRatio * 100).toInt()}%", fontSize = 11.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        Text(text = "Dépensé : ${(expensesRatio * 100).toInt()}%", fontSize = 11.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showRapportsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F56E3))
                ) {
                    Text("D'accord")
                }
            }
        )
    }

    // E. DIALOG: COMMUNICATION & MESSAGES
    if (showCommuniquesDialog) {
        AlertDialog(
            onDismissRequest = { showCommuniquesDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Portail de Communication WhatsApp",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF1E3A8A)
                            )
                            Text(
                                text = "Envoyer des messages personnalisés aux parents",
                                fontSize = 11.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }
                    IconButton(onClick = { showCommuniquesDialog = false }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer")
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // TAB SWITCHER
                    TabRow(
                        selectedTabIndex = commsActiveTab,
                        containerColor = Color(0xFFF3F4F6),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        Tab(
                            selected = commsActiveTab == 0,
                            onClick = { commsActiveTab = 0 },
                            text = { Text("Composer un message", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                            selectedContentColor = Color(0xFF0F56E3),
                            unselectedContentColor = Color(0xFF6B7280)
                        )
                        Tab(
                            selected = commsActiveTab == 1,
                            onClick = { commsActiveTab = 1 },
                            text = { Text("Modèles professionnels", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                            selectedContentColor = Color(0xFF0F56E3),
                            unselectedContentColor = Color(0xFF6B7280)
                        )
                    }

                    if (commsActiveTab == 1) {
                        // MODÈLES PREDEFINIS TAB
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            item {
                                Text(
                                    text = "Sélectionnez un modèle de message ci-dessous pour l'appliquer à l'éditeur :",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF4B5563),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }

                            val templates = listOf(
                                Triple(
                                    "📢 Rappel Frais de Scolarité",
                                    "Bonjour Chers Parents, nous vous rappelons que le solde restant pour les frais de scolarité de {élève} en classe de {classe} est de {solde_dû}. Merci de régulariser au plus vite via ScolaPay. Cordialement, la Direction.",
                                    Color(0xFFEFF6FF) to Color(0xFF1D4ED8)
                                ),
                                Triple(
                                    "🏫 Invitation Réunion Générale",
                                    "Bonjour Chers Parents, vous êtes cordialement invités à la réunion des parents d'élèves ce samedi à 10h concernant l'évolution scolaire et les outils financiers de l'établissement. Votre présence est très importante. Cordialement, la Direction.",
                                    Color(0xFFFEF3C7) to Color(0xFFB45309)
                                ),
                                Triple(
                                    "📌 Avis d'Absence",
                                    "Bonjour, nous vous informons que votre enfant {élève} en classe de {classe} a été signalé absent aujourd'hui. Veuillez contacter l'administration de l'établissement pour plus de détails. Cordialement.",
                                    Color(0xFFFEE2E2) to Color(0xFFB91C1C)
                                ),
                                Triple(
                                    "📅 Note d'Information Administrative",
                                    "Chers parents, l'établissement informe que les cours seront exceptionnellement suspendus ce [Date] pour cause de réunion pédagogique. Reprise des cours le [Date]. Cordialement, la Direction.",
                                    Color(0xFFECFDF5) to Color(0xFF047857)
                                )
                            )

                            items(templates) { (title, text, colors) ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = colors.first),
                                    border = BorderStroke(1.dp, colors.second.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            commsMessageText = text
                                            commsActiveTab = 0
                                            Toast.makeText(context, "Modèle appliqué !", Toast.LENGTH_SHORT).show()
                                        }
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = colors.second)
                                        Text(text = text, fontSize = 11.sp, color = colors.second.copy(alpha = 0.9f))
                                    }
                                }
                            }
                        }
                    } else {
                        // COMPOSER TAB
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            // 1. RECIPIENT TYPE SELECTOR
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("1. Type de destinataire", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF374151))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf(
                                            "STUDENT" to "Par Élève",
                                            "CLASS" to "Par Classe",
                                            "ALL" to "Tous les Parents"
                                        ).forEach { (type, label) ->
                                            val isSelected = commsRecipientType == type
                                            Card(
                                                shape = RoundedCornerShape(20.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isSelected) Color(0xFF0F56E3) else Color(0xFFF3F4F6)
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        commsRecipientType = type
                                                        if (type != "STUDENT") commsSelectedStudent = null
                                                    }
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 8.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = label,
                                                        color = if (isSelected) Color.White else Color(0xFF4B5563),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 2. RECIPIENT DETAIL FIELDS
                            if (commsRecipientType == "STUDENT") {
                                item {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("2. Sélectionner l'élève", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF374151))
                                        
                                        OutlinedTextField(
                                            value = commsSearchQuery,
                                            onValueChange = { commsSearchQuery = it },
                                            placeholder = { Text("Rechercher un élève par nom...") },
                                            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF0F56E3),
                                                unfocusedBorderColor = Color(0xFFD1D5DB)
                                            )
                                        )

                                        if (commsSelectedStudent != null) {
                                            // Show selected student badge
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                                                border = BorderStroke(1.dp, Color(0xFF10B981)),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text(
                                                            text = "${commsSelectedStudent?.firstName} ${commsSelectedStudent?.lastName}",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp,
                                                            color = Color(0xFF065F46)
                                                        )
                                                        Text(
                                                            text = "Classe : ${commsSelectedStudent?.grade} • WhatsApp : ${commsSelectedStudent?.parentWhatsApp ?: "Aucun"}",
                                                            fontSize = 11.sp,
                                                            color = Color(0xFF047857)
                                                        )
                                                    }
                                                    IconButton(onClick = { commsSelectedStudent = null }) {
                                                        Icon(imageVector = Icons.Default.Close, contentDescription = "Désélectionner", tint = Color(0xFF065F46))
                                                    }
                                                }
                                            }
                                        } else if (commsSearchQuery.isNotBlank()) {
                                            // Search Results List
                                            val filteredStudents = students.filter {
                                                it.firstName.contains(commsSearchQuery, ignoreCase = true) ||
                                                it.lastName.contains(commsSearchQuery, ignoreCase = true)
                                            }.take(5)

                                            if (filteredStudents.isEmpty()) {
                                                Text("Aucun élève trouvé", fontSize = 12.sp, color = Color.Red, modifier = Modifier.padding(4.dp))
                                            } else {
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                                                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column {
                                                        filteredStudents.forEach { student ->
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .clickable {
                                                                        commsSelectedStudent = student
                                                                        commsSearchQuery = ""
                                                                    }
                                                                    .padding(12.dp),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Column {
                                                                    Text("${student.firstName} ${student.lastName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                                    Text("Classe : ${student.grade}", fontSize = 11.sp, color = Color(0xFF6B7280))
                                                                }
                                                                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(16.dp))
                                                            }
                                                            HorizontalDivider(color = Color(0xFFE5E7EB))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else if (commsRecipientType == "CLASS") {
                                item {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("2. Sélectionner la Classe / Niveau", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF374151))
                                        
                                        // Simple horizontal scrollable chips for classes
                                        val allClasses = DEFAULT_CLASSES_BY_SECTION.values.flatten()
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            items(allClasses) { grade ->
                                                val isSelected = commsSelectedClass == grade
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = { commsSelectedClass = grade },
                                                    label = { Text(grade, fontSize = 11.sp) }
                                                )
                                            }
                                        }
                                        val countInClass = students.count { it.grade == commsSelectedClass }
                                        Text(
                                            text = "$countInClass élève(s) trouvé(s) en classe de $commsSelectedClass.",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF4B5563)
                                        )
                                    }
                                }
                            } else {
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color(0xFFD97706))
                                            Text(
                                                text = "Vous allez préparer le message pour l'ensemble des parents d'élèves (${students.size} élèves au total).",
                                                fontSize = 11.sp,
                                                color = Color(0xFFB45309)
                                            )
                                        }
                                    }
                                }
                            }

                            // 3. MESSAGE COMPOSER
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("3. Rédiger le message", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF374151))
                                        Text(
                                            text = "${commsMessageText.length} caractères",
                                            fontSize = 10.sp,
                                            color = Color(0xFF6B7280)
                                        )
                                    }
                                    
                                    OutlinedTextField(
                                        value = commsMessageText,
                                        onValueChange = { commsMessageText = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(110.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        maxLines = 6,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF0F56E3),
                                            unfocusedBorderColor = Color(0xFFD1D5DB)
                                        )
                                    )

                                    // Dynamic variables insertion tips
                                    Text(
                                        text = "Astuce : Cliquez pour insérer des balises de personnalisation automatique :",
                                        fontSize = 10.sp,
                                        color = Color(0xFF6B7280),
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                    
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        listOf(
                                            "{élève}" to "Nom élève",
                                            "{classe}" to "Classe",
                                            "{solde_dû}" to "Solde dû"
                                        ).forEach { (code, label) ->
                                            InputChip(
                                                selected = false,
                                                onClick = {
                                                    commsMessageText += "$code"
                                                },
                                                label = { Text("$code ($label)", fontSize = 10.sp) }
                                            )
                                        }
                                    }
                                }
                            }

                            // 4. GENERATED RECIPIENT PREVIEW & SEND LIST
                            item {
                                Text(
                                    text = "4. Aperçu et Envoi Individualisé via WhatsApp",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF374151),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            // Compute list of targets
                            val targetStudents = when (commsRecipientType) {
                                "STUDENT" -> if (commsSelectedStudent != null) listOf(commsSelectedStudent!!) else emptyList()
                                "CLASS" -> students.filter { it.grade == commsSelectedClass }
                                "ALL" -> students
                                else -> emptyList()
                            }

                            if (targetStudents.isEmpty()) {
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Sélectionnez un ou plusieurs destinataires pour générer les aperçus de messages.",
                                                fontSize = 11.sp,
                                                color = Color(0xFF6B7280),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            } else {
                                items(targetStudents) { student ->
                                    // Calculate due balance for student
                                    val studentPayments = payments.filter { it.studentId == student.id }
                                    val totalPaid = studentPayments.filter { it.reason != "Inscription" && it.reason != "Réinscription" }.sumOf { it.amount }
                                    val classFee = classFees.find { it.grade == student.grade }?.feeAmount ?: 0L
                                    val unpaidBalance = maxOf(0L, classFee - totalPaid)

                                    val finalMessageText = getPersonalizedMessage(commsMessageText, student, unpaidBalance)
                                    val phone = student.parentWhatsApp ?: ""

                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                                        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        text = "${student.firstName} ${student.lastName}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = Color(0xFF1F2937)
                                                    )
                                                    Text(
                                                        text = "Classe : ${student.grade} • Frais restants : ${numberFormat.format(unpaidBalance)} GNF",
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF4B5563)
                                                    )
                                                }
                                                
                                                if (phone.isNotBlank()) {
                                                    Button(
                                                        onClick = {
                                                            try {
                                                                val cleanPhone = phone.trim()
                                                                val formattedPhone = when {
                                                                    cleanPhone.startsWith("+") -> cleanPhone.replace(" ", "").replace("-", "")
                                                                    cleanPhone.startsWith("224") -> "+$cleanPhone".replace(" ", "").replace("-", "")
                                                                    else -> "+224${cleanPhone.replace(" ", "").replace("-", "")}"
                                                                }
                                                                val encodedText = java.net.URLEncoder.encode(finalMessageText, "UTF-8")
                                                                val uri = android.net.Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=$encodedText")
                                                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                                                context.startActivity(intent)
                                                            } catch (e: Exception) {
                                                                Toast.makeText(context, "Erreur d'ouverture de WhatsApp", Toast.LENGTH_SHORT).show()
                                                            }
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                                        shape = RoundedCornerShape(18.dp),
                                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                        modifier = Modifier.height(32.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Chat,
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Envoyer", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                    }
                                                } else {
                                                    Card(
                                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "WhatsApp non configuré",
                                                            color = Color(0xFF991B1B),
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            // Bubble with personalized preview
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp))
                                                    .padding(10.dp)
                                            ) {
                                                Text(
                                                    text = finalMessageText,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF2E7D32),
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCommuniquesDialog = false }) {
                    Text("Fermer", fontWeight = FontWeight.Bold, color = Color(0xFF6B7280))
                }
            }
        )
    }

    if (showDirectSubscriptionDialog) {
        var subSchoolName by remember { mutableStateOf(schoolName ?: "") }
        var subPhoneNumber by remember { mutableStateOf("") }
        var subTransactionId by remember { mutableStateOf("") }
        var subErrorMessage by remember { mutableStateOf<String?>(null) }

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
                        text = "Profitez de notre offre spéciale à 200 000 GNF/an au lieu de 500 000 GNF.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4B5563)
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Instructions de paiement :", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF374151))
                            Text("Orange Money : 628 37 65 66", fontWeight = FontWeight.Bold, color = Color(0xFFFF6600), fontSize = 14.sp)
                            Text("MTN MoMo : 660 37 78 87", fontWeight = FontWeight.Bold, color = Color(0xFFCC9900), fontSize = 14.sp)
                        }
                    }

                    if (subErrorMessage != null) {
                        Text(
                            text = subErrorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    OutlinedTextField(
                        value = subSchoolName,
                        onValueChange = { subSchoolName = it },
                        label = { Text("Nom de l'école") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = subPhoneNumber,
                        onValueChange = { subPhoneNumber = it },
                        label = { Text("Numéro de téléphone de paiement") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = subTransactionId,
                        onValueChange = { subTransactionId = it },
                        label = { Text("Identifiant de transaction (ID)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (subSchoolName.isBlank() || subPhoneNumber.isBlank() || subTransactionId.isBlank()) {
                            subErrorMessage = "Veuillez remplir tous les champs"
                        } else {
                            subErrorMessage = null
                            viewModel.submitSubscriptionRequest(subPhoneNumber, subTransactionId)
                            showDirectSubscriptionDialog = false
                            Toast.makeText(context, "Demande d'abonnement soumise avec succès", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Soumettre", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDirectSubscriptionDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showSchoolYearDialog) {
        AlertDialog(
            onDismissRequest = { showSchoolYearDialog = false },
            title = {
                Text(
                    text = "Sélectionner l'exercice scolaire",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1F2937)
                )
            },
            text = {
                val currentYear = remember { java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) }
                val startYear = 2024
                val endYear = remember(currentYear) { maxOf(currentYear + 10, 2035) }
                val schoolYears = remember(endYear) {
                    mutableListOf("Toutes les années").apply {
                        for (y in startYear..endYear) {
                            add("$y - ${y + 1}")
                        }
                    }
                }
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(schoolYears) { year ->
                        val isSelected = year == selectedSchoolYear
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFF0F56E3).copy(alpha = 0.1f) else Color.Transparent)
                                .clickable {
                                    viewModel.setSelectedSchoolYear(year)
                                    showSchoolYearDialog = false
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = year,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF0F56E3) else Color(0xFF4B5563)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Sélectionné",
                                    tint = Color(0xFF0F56E3),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSchoolYearDialog = false }) {
                    Text("Fermer", color = Color(0xFF0F56E3))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // F. DIALOG: SCOLARITÉ & RECOUVREMENT
    if (showScolariteDialog) {
        var scolariteTab by remember { mutableIntStateOf(0) }
        
        // Find all unique classes from students AND configured classFees
        val activeGrades = students.map { it.grade }.distinct()
        val configuredGrades = classFees.map { it.grade }
        val allGrades = (activeGrades + configuredGrades).distinct().sorted()

        val classesBySection = remember(students, classFees) {
            val map = mutableMapOf<String, List<String>>()
            val validSections = SECTIONS.filter { it != "Toutes les sections" }
            for (sec in validSections) {
                val defaults = DEFAULT_CLASSES_BY_SECTION[sec] ?: emptyList()
                val customActiveInSec = if (sec == "LA MATERNELLE") {
                    emptyList()
                } else {
                    students
                        .filter { it.section == sec && it.grade.isNotBlank() && !defaults.contains(it.grade) }
                        .map { it.grade }
                        .distinct()
                }
                map[sec] = (defaults + customActiveInSec).distinct()
            }
            // Check for other grades with no matching section in defaults
            val allStandardClasses = DEFAULT_CLASSES_BY_SECTION.values.flatten()
            val otherActiveGrades = (students.map { it.grade } + classFees.map { it.grade })
                .distinct()
                .filter { it.isNotBlank() && !allStandardClasses.contains(it) && !students.any { s -> s.grade == it && validSections.contains(s.section) } }
            if (otherActiveGrades.isNotEmpty()) {
                map["AUTRES"] = otherActiveGrades
            }
            map
        }

        var editGradeFeeTarget by remember { mutableStateOf<String?>(null) }
        var editFeeAmountString by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showScolariteDialog = false },
            title = {
                Text(
                    text = "Scolarité & Recouvrement",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tab header
                    TabRow(
                        selectedTabIndex = scolariteTab,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Tab(
                            selected = scolariteTab == 0,
                            onClick = { scolariteTab = 0 },
                            text = { Text("Recouvrement", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = scolariteTab == 1,
                            onClick = { scolariteTab = 1 },
                            text = { Text("Configuration", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }

                    if (scolariteTab == 0) {
                        // RECOUVREMENT TAB CONTENT
                        // 1. Overall stats
                        val tuitionCollected = payments.filter { it.reason != "Inscription" && it.reason != "Réinscription" }.sumOf { it.amount }
                        val remainingToCollect = (totalTheorique - tuitionCollected).coerceAtLeast(0L)
                        val totalProgress = if (totalTheorique > 0) tuitionCollected.toFloat() / totalTheorique else 0f

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4F8)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Synthèse Générale",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF1E3A8A)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Attendu :", fontSize = 11.sp, color = Color(0xFF4B5563))
                                    Text("${numberFormat.format(totalTheorique)} GNF", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Recouvré :", fontSize = 11.sp, color = Color(0xFF10B981))
                                    Text("${numberFormat.format(tuitionCollected)} GNF", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Reste :", fontSize = 11.sp, color = Color(0xFFEF4444))
                                    Text("${numberFormat.format(remainingToCollect)} GNF", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Taux de recouvrement :", fontSize = 11.sp, color = Color(0xFF4B5563))
                                    Text("${(totalProgress * 100).toInt()}%", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF1E3A8A))
                                }
                                LinearProgressIndicator(
                                    progress = { totalProgress },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = Color(0xFF0F56E3),
                                    trackColor = Color(0xFFE5E7EB)
                                )
                            }
                        }

                        // 2. Class-by-class detail list
                        if (allGrades.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                Text("Aucune classe disponible", fontSize = 12.sp, color = Color.Gray)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(allGrades) { grade ->
                                    val gradeStudents = students.filter { it.grade == grade }
                                    val count = gradeStudents.size
                                    val fee = classFees.find { it.grade == grade }?.feeAmount ?: 0L
                                    val expected = count * fee
                                    val collected = payments.filter { p -> p.reason != "Inscription" && p.reason != "Réinscription" && gradeStudents.any { s -> s.id == p.studentId } }.sumOf { it.amount }
                                    val remaining = (expected - collected).coerceAtLeast(0L)
                                    val progress = if (expected > 0) collected.toFloat() / expected else 0f

                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(text = grade, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Text(text = "$count élève(s) • Frais: ${numberFormat.format(fee)} GNF", fontSize = 11.sp, color = Color.Gray)
                                                }
                                                Text(
                                                    text = "${(progress * 100).toInt()}%",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF10B981)
                                                )
                                            }
                                            
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Attendu: ${numberFormat.format(expected)} GNF", fontSize = 11.sp, color = Color.Gray)
                                                Text("Recouvré: ${numberFormat.format(collected)} GNF", fontSize = 11.sp, color = Color(0xFF10B981))
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Spacer(modifier = Modifier.weight(1f))
                                                Text("Reste: ${numberFormat.format(remaining)} GNF", fontSize = 11.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold)
                                            }

                                            LinearProgressIndicator(
                                                progress = { progress },
                                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                                color = Color(0xFF10B981),
                                                trackColor = Color(0xFFE5E7EB)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // CONFIGURATION TAB CONTENT
                        Text(
                            text = "Définissez les frais de scolarité annuels pour chaque classe.",
                            fontSize = 12.sp,
                            color = Color(0xFF4B5563)
                        )

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        // Logo preview
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .background(Color(0xFFF3F4F6), shape = RoundedCornerShape(8.dp))
                                                .border(1.dp, Color(0xFFD1D5DB), shape = RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            val logoBase64 = schoolLogoBase64
                                            if (!logoBase64.isNullOrBlank()) {
                                                val decodedBytes = remember(logoBase64) {
                                                    try {
                                                        Base64.decode(logoBase64, Base64.DEFAULT)
                                                    } catch (e: Exception) {
                                                        null
                                                    }
                                                }
                                                val bitmap = remember(decodedBytes) {
                                                    decodedBytes?.let {
                                                        BitmapFactory.decodeByteArray(it, 0, it.size)
                                                    }
                                                }
                                                if (bitmap != null) {
                                                    Image(
                                                        bitmap = bitmap.asImageBitmap(),
                                                        contentDescription = "Logo de l'école",
                                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                                                    )
                                                } else {
                                                    Icon(Icons.Default.School, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                                                }
                                            } else {
                                                Icon(Icons.Default.School, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                                            }
                                        }

                                        // Action buttons
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Logo de l'école", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E3A8A))
                                            Text("S'affiche sur toutes vos factures et reçus.", fontSize = 11.sp, color = Color.Gray)
                                            
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                                                Button(
                                                    onClick = { pickImageLauncher.launch("image/*") },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F56E3)),
                                                    modifier = Modifier.height(32.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                                ) {
                                                    Text(if (schoolLogoBase64 != null) "Changer" else "Importer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                                if (schoolLogoBase64 != null) {
                                                    OutlinedButton(
                                                        onClick = { viewModel.setSchoolLogo(null) },
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                                        border = BorderStroke(1.dp, Color.Red),
                                                        modifier = Modifier.height(32.dp),
                                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                                    ) {
                                                        Text("Supprimer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            classesBySection.forEach { (section, grades) ->
                                if (grades.isNotEmpty()) {
                                    item {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp, bottom = 4.dp)
                                        ) {
                                            Text(
                                                text = section,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color(0xFF0F56E3),
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                    items(grades) { grade ->
                                        val currentFee = classFees.find { it.grade == grade }?.feeAmount ?: 0L
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = grade, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text(
                                                    text = if (currentFee > 0L) "${numberFormat.format(currentFee)} GNF" else "Non configuré",
                                                    color = if (currentFee > 0L) Color(0xFF10B981) else Color(0xFFEF4444),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                            Button(
                                                onClick = {
                                                    editGradeFeeTarget = grade
                                                    editFeeAmountString = if (currentFee > 0) currentFee.toString() else ""
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF), contentColor = Color(0xFF1E3A8A)),
                                                modifier = Modifier.height(32.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                            ) {
                                                Text("Modifier", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        HorizontalDivider(color = Color(0xFFF3F4F6))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showScolariteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F56E3))
                ) {
                    Text("Fermer")
                }
            }
        )

        // Sub-dialog: Edit Fee Amount
        if (editGradeFeeTarget != null) {
            AlertDialog(
                onDismissRequest = { editGradeFeeTarget = null },
                title = { Text("Modifier Frais - ${editGradeFeeTarget}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Saisissez le montant des frais scolaires pour cette classe (en GNF) :", fontSize = 13.sp, color = Color.Gray)
                        OutlinedTextField(
                            value = editFeeAmountString,
                            onValueChange = { editFeeAmountString = it.filter { char -> char.isDigit() } },
                            label = { Text("Frais (GNF)") },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amount = editFeeAmountString.toLongOrNull() ?: 0L
                            viewModel.setClassFee(editGradeFeeTarget!!, amount)
                            Toast.makeText(context, "Frais mis à jour pour ${editGradeFeeTarget}", Toast.LENGTH_SHORT).show()
                            editGradeFeeTarget = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("Enregistrer")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editGradeFeeTarget = null }) {
                        Text("Annuler")
                    }
                }
            )
        }
    }

    if (showInscriptionDialog) {
        var dialogTab by remember { mutableIntStateOf(0) }
        var selectedStudentForFee by remember { mutableStateOf<com.example.data.models.Student?>(null) }
        var studentSearchQuery by remember { mutableStateOf("") }
        var isStudentDropdownExpanded by remember { mutableStateOf(false) }
        
        var selectedFeeType by remember { mutableStateOf("Inscription") } // "Inscription" or "Réinscription"
        var feeAmountInput by remember { mutableStateOf("") }
        var selectedFeeMethod by remember { mutableStateOf("Espèces") }
        
        var searchHistoryQuery by remember { mutableStateOf("") }

        LaunchedEffect(selectedStudentForFee, selectedFeeType) {
            selectedStudentForFee?.let { student ->
                val amount = if (selectedFeeType == "Inscription") student.registrationFee else student.reenrollmentFee
                feeAmountInput = if (amount > 0L) amount.toString() else ""
            } ?: run {
                feeAmountInput = ""
            }
        }

        AlertDialog(
            onDismissRequest = { showInscriptionDialog = false },
            title = {
                Text(
                    text = "Gestion des Inscriptions & Réinscriptions",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A)
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 520.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TabRow(
                        selectedTabIndex = dialogTab,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Tab(
                            selected = dialogTab == 0,
                            onClick = { dialogTab = 0 },
                            text = { Text("Nouveau Paiement", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = dialogTab == 1,
                            onClick = { dialogTab = 1 },
                            text = { Text("Historique & Synthèse", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }

                    if (dialogTab == 0) {
                        // NOUVEAU PAIEMENT
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Enregistrer un paiement d'inscription ou de réinscription d'un élève.",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )

                            // Search / Dropdown student selection
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = studentSearchQuery,
                                    onValueChange = {
                                        studentSearchQuery = it
                                        isStudentDropdownExpanded = true
                                        selectedStudentForFee = null
                                    },
                                    label = { Text("Rechercher un élève") },
                                    placeholder = { Text(selectedStudentForFee?.let { "${it.firstName} ${it.lastName}" } ?: "Saisir un nom...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    trailingIcon = {
                                        IconButton(onClick = { isStudentDropdownExpanded = !isStudentDropdownExpanded }) {
                                            Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Liste")
                                        }
                                    }
                                )

                                val filteredStudents = remember(students, studentSearchQuery) {
                                    if (studentSearchQuery.isBlank()) {
                                        students.take(10)
                                    } else {
                                        students.filter {
                                            "${it.firstName} ${it.lastName}".contains(studentSearchQuery, ignoreCase = true)
                                        }
                                    }
                                }

                                DropdownMenu(
                                    expanded = isStudentDropdownExpanded,
                                    onDismissRequest = { isStudentDropdownExpanded = false },
                                    modifier = Modifier.fillMaxWidth(0.9f).heightIn(max = 200.dp)
                                ) {
                                    if (filteredStudents.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("Aucun élève trouvé") },
                                            onClick = {}
                                        )
                                    } else {
                                        filteredStudents.forEach { s ->
                                            DropdownMenuItem(
                                                text = {
                                                    Column {
                                                        Text("${s.lastName} ${s.firstName}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                                        Text("Classe: ${s.grade} • Section: ${s.section}", fontSize = 11.sp, color = Color.Gray)
                                                    }
                                                },
                                                onClick = {
                                                    selectedStudentForFee = s
                                                    studentSearchQuery = "${s.lastName} ${s.firstName}"
                                                    isStudentDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            if (selectedStudentForFee != null) {
                                val s = selectedStudentForFee!!
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Élève sélectionné : ${s.firstName} ${s.lastName}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("Frais d'inscription configurés : ${numberFormat.format(s.registrationFee)} GNF", fontSize = 11.sp)
                                        Text("Frais de réinscription configurés : ${numberFormat.format(s.reenrollmentFee)} GNF", fontSize = 11.sp)
                                    }
                                }

                                // Selection between Inscription and Re-inscription
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { selectedFeeType = "Inscription" }
                                    ) {
                                        RadioButton(
                                            selected = selectedFeeType == "Inscription",
                                            onClick = { selectedFeeType = "Inscription" }
                                        )
                                        Text("Inscription", fontSize = 12.sp)
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { selectedFeeType = "Réinscription" }
                                    ) {
                                        RadioButton(
                                            selected = selectedFeeType == "Réinscription",
                                            onClick = { selectedFeeType = "Réinscription" }
                                        )
                                        Text("Réinscription", fontSize = 12.sp)
                                    }
                                }

                                // Amount Field
                                OutlinedTextField(
                                    value = feeAmountInput,
                                    onValueChange = { feeAmountInput = it },
                                    label = { Text("Montant Payé") },
                                    suffix = { Text("GNF") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                // Payment Method Selection
                                Text("Mode de Paiement", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("Espèces", "Orange Money", "Mobile Money").forEach { method ->
                                        val isSelected = selectedFeeMethod == method
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isSelected) Color(0xFFEFF6FF) else Color.White
                                            ),
                                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF3B82F6) else Color(0xFFD1D5DB)),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { selectedFeeMethod = method }
                                        ) {
                                            Box(modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                                Text(method, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        val amount = feeAmountInput.toLongOrNull() ?: 0L
                                        if (amount > 0L) {
                                            viewModel.insertPayment(
                                                studentId = s.id,
                                                amount = amount,
                                                reason = selectedFeeType,
                                                paymentMethod = selectedFeeMethod
                                            )
                                            Toast.makeText(context, "Paiement de $selectedFeeType enregistré avec succès", Toast.LENGTH_SHORT).show()
                                            // Reset inputs
                                            selectedStudentForFee = null
                                            studentSearchQuery = ""
                                            feeAmountInput = ""
                                        } else {
                                            Toast.makeText(context, "Veuillez saisir un montant valide", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899))
                                ) {
                                    Text("Enregistrer le paiement")
                                }
                            }
                        }
                    } else {
                        // HISTORIQUE & SYNTHÈSE
                        val inscriptionPayments = remember(payments) {
                            payments.filter { it.reason == "Inscription" || it.reason == "Réinscription" }
                        }
                        
                        val totalInsc = remember(inscriptionPayments) {
                            inscriptionPayments.filter { it.reason == "Inscription" }.sumOf { it.amount }
                        }
                        
                        val totalReinsc = remember(inscriptionPayments) {
                            inscriptionPayments.filter { it.reason == "Réinscription" }.sumOf { it.amount }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Stats Cards
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE7F3)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("Inscriptions", fontSize = 10.sp, color = Color(0xFF9D174D))
                                        Text("${numberFormat.format(totalInsc)} GNF", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF9D174D))
                                    }
                                }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("Réinscriptions", fontSize = 10.sp, color = Color(0xFF5B21B6))
                                        Text("${numberFormat.format(totalReinsc)} GNF", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF5B21B6))
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = searchHistoryQuery,
                                onValueChange = { searchHistoryQuery = it },
                                label = { Text("Rechercher dans l'historique") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) }
                            )

                            val displayPayments = remember(inscriptionPayments, searchHistoryQuery, students) {
                                inscriptionPayments.filter { p ->
                                    val s = students.find { it.id == p.studentId }
                                    val name = s?.let { "${it.firstName} ${it.lastName}" } ?: ""
                                    name.contains(searchHistoryQuery, ignoreCase = true) || p.reason.contains(searchHistoryQuery, ignoreCase = true)
                                }
                            }

                            if (displayPayments.isEmpty()) {
                                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text("Aucun paiement trouvé", fontSize = 12.sp, color = Color.Gray)
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.weight(1f).heightIn(max = 220.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(displayPayments) { p ->
                                        val s = students.find { it.id == p.studentId }
                                        val name = s?.let { "${it.lastName} ${it.firstName}" } ?: "Élève inconnu"
                                        val grade = s?.grade ?: ""
                                        
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    Text("$grade • ${p.paymentMethod}", fontSize = 10.sp, color = Color.Gray)
                                                }
                                                Column(horizontalAlignment = Alignment.End) {
                                                    val isInsc = p.reason == "Inscription"
                                                    Card(
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = if (isInsc) Color(0xFFFCE7F3) else Color(0xFFF3E8FF)
                                                        ),
                                                        modifier = Modifier.padding(bottom = 2.dp)
                                                    ) {
                                                        Text(
                                                            p.reason,
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isInsc) Color(0xFF9D174D) else Color(0xFF5B21B6),
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                    Text("${numberFormat.format(p.amount)} GNF", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showInscriptionDialog = false }) {
                    Text("Fermer", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showSupportDialog) {
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

                        // User Manual PDF Button
                        Button(
                            onClick = {
                                try {
                                    exportManualPdfLauncher.launch("Manuel_Utilisation_ScolaPay.pdf")
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Impossible de lancer l'export PDF", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Télécharger le Manuel PDF", fontWeight = FontWeight.Bold, color = Color.White)
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

                        // Share Application Button
                        Button(
                            onClick = {
                                try {
                                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(android.content.Intent.EXTRA_SUBJECT, "ScolaPay")
                                        putExtra(
                                            android.content.Intent.EXTRA_TEXT,
                                            "Découvrez ScolaPay, l'application moderne de gestion financière scolaire ! Elle permet de gérer facilement la scolarité et les inscriptions, d'envoyer des reçus PDF professionnels avec logo, de faire des appels directs aux parents et d'envoyer des relances automatiques par WhatsApp.\n\n👉 Téléchargez et installez l'application immédiatement depuis Google Play Store :\nhttps://play.google.com/store/apps/details?id=com.aistudio.scolapay.gnf"
                                        )
                                    }
                                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Recommander ScolaPay via"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Impossible de lancer le partage", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Partager & Recommander l'application", fontWeight = FontWeight.Bold, color = Color.White)
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

    if (paymentToDelete != null) {
        val numberFormat = remember { NumberFormat.getNumberInstance(Locale("fr", "GN")) }
        AlertDialog(
            onDismissRequest = { paymentToDelete = null },
            title = { Text("Avertissement : Supprimer le paiement") },
            text = {
                val formattedAmount = numberFormat.format(paymentToDelete?.amount ?: 0L)
                Text("Attention ! Êtes-vous sûr de vouloir supprimer définitivement ce paiement de $formattedAmount GNF (${paymentToDelete?.reason ?: ""}) ? Cette action est irréversible et affectera le solde de l'élève.")
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        paymentToDelete?.let {
                            viewModel.deletePayment(it.id)
                            Toast.makeText(context, "Paiement supprimé", Toast.LENGTH_SHORT).show()
                        }
                        paymentToDelete = null
                    }
                ) {
                    Text("Supprimer définitivement")
                }
            },
            dismissButton = {
                TextButton(onClick = { paymentToDelete = null }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun QuickAccessButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    bgColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(68.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4B5563),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

fun getPersonalizedMessage(
    template: String,
    student: com.example.data.models.Student,
    unpaidBalance: Long
): String {
    val formattedBalance = NumberFormat.getNumberInstance(Locale("fr", "GN")).format(unpaidBalance) + " GNF"
    return template
        .replace("{élève}", "${student.firstName} ${student.lastName}")
        .replace("{prénom}", student.firstName)
        .replace("{nom}", student.lastName)
        .replace("{classe}", student.grade)
        .replace("{solde_dû}", formattedBalance)
}

