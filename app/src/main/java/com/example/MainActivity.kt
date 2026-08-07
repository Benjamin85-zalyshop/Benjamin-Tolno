package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.data.local.AppDatabase
import com.example.data.repository.SchoolRepository
import com.example.ui.SchoolViewModel
import com.example.ui.SchoolViewModelFactory
import com.example.ui.navigation.AddExpenseRoute
import com.example.ui.navigation.AddPaymentRoute
import com.example.ui.navigation.AddStudentRoute
import com.example.ui.navigation.AcademicRoute
import com.example.ui.navigation.DashboardRoute
import com.example.ui.navigation.ExpensesRoute
import com.example.ui.navigation.LoginRoute
import com.example.ui.navigation.RegisterRoute
import com.example.ui.navigation.StudentDetailRoute
import com.example.ui.navigation.StudentsRoute
import com.example.ui.screens.AddExpenseScreen
import com.example.ui.screens.AddPaymentScreen
import com.example.ui.screens.AddStudentScreen
import com.example.ui.screens.AcademicScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExpensesScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.RegisterScreen
import com.example.ui.screens.StudentDetailScreen
import com.example.ui.screens.StudentsScreen
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var inactivityJob: Job? = null
    private var onUserInteractionCallback: (() -> Unit)? = null
    private val inactivityTimeoutMs = 5 * 60 * 1000L // 5 minutes
    private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private fun resetInactivityTimer(viewModel: SchoolViewModel, onTimeout: () -> Unit) {
        val isLoggedIn = viewModel.userRole.value != null
        if (!isLoggedIn) {
            inactivityJob?.cancel()
            return
        }
        inactivityJob?.cancel()
        inactivityJob = mainScope.launch {
            delay(inactivityTimeoutMs)
            onTimeout()
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        onUserInteractionCallback?.invoke()
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        inactivityJob?.cancel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(this)
        val repository = SchoolRepository(database.schoolDao())
        val factory = SchoolViewModelFactory(repository, applicationContext)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val viewModel: SchoolViewModel = viewModel(factory = factory)

                    val userRoleState = viewModel.userRole.collectAsStateWithLifecycle()
                    
                    val activity = androidx.compose.ui.platform.LocalContext.current as? androidx.activity.ComponentActivity
                    
                    androidx.compose.runtime.DisposableEffect(activity) {
                        val listener = androidx.core.util.Consumer<android.content.Intent> { newIntent ->
                            val uri = newIntent.data
                            if (uri != null && (uri.scheme == "scolapay" || uri.scheme == "https") && (uri.host == "paiement" || uri.host == "scolapay.gn" || uri.host == "scolapay-b6289.web.app")) {
                                if (uri.path?.contains("success") == true || uri.path?.contains("return") == true) {
                                    viewModel.checkPendingPaymentStatus { status ->
                                        if (status == "SUCCESS") {
                                            android.widget.Toast.makeText(applicationContext, "Paiement Chap Chap Pay réussi, abonnement activé !", android.widget.Toast.LENGTH_LONG).show()
                                        } else if (status == "FAILED") {
                                            android.widget.Toast.makeText(applicationContext, "Paiement échoué ou annulé. Vous pouvez réessayer.", android.widget.Toast.LENGTH_LONG).show()
                                        } else {
                                            android.widget.Toast.makeText(applicationContext, "Paiement en attente de validation.", android.widget.Toast.LENGTH_LONG).show()
                                        }
                                    }
                                    newIntent.data = null // Clear intent
                                    activity?.intent?.data = null
                                } else if (uri.path?.contains("cancel") == true) {
                                    android.widget.Toast.makeText(applicationContext, "Paiement annulé.", android.widget.Toast.LENGTH_LONG).show()
                                    viewModel.clearPendingOrderId()
                                    newIntent.data = null // Clear intent
                                    activity?.intent?.data = null
                                }
                            }
                        }
                        activity?.addOnNewIntentListener(listener)
                        
                        // Handle initial intent
                        activity?.intent?.let { listener.accept(it) }
                        
                        onDispose {
                            activity?.removeOnNewIntentListener(listener)
                        }
                    }
                    
                    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
                    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
                        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                                viewModel.checkPendingPaymentStatus { status ->
                                    if (status == "SUCCESS") {
                                        android.widget.Toast.makeText(applicationContext, "Paiement Chap Chap Pay réussi !", android.widget.Toast.LENGTH_LONG).show()
                                    } else if (status == "FAILED") {
                                        android.widget.Toast.makeText(applicationContext, "Le paiement a échoué. Vous pouvez réessayer.", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose {
                            lifecycleOwner.lifecycle.removeObserver(observer)
                        }
                    }

                    LaunchedEffect(userRoleState.value) {
                        val role = userRoleState.value
                        if (role != null) {
                            resetInactivityTimer(viewModel) {
                                viewModel.logout()
                                navController.navigate(LoginRoute) {
                                    popUpTo(0) { inclusive = true }
                                }
                                android.widget.Toast.makeText(
                                    applicationContext,
                                    "Déconnecté pour inactivité après 5 minutes",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            inactivityJob?.cancel()
                        }
                    }

                    onUserInteractionCallback = {
                        resetInactivityTimer(viewModel) {
                            viewModel.logout()
                            navController.navigate(LoginRoute) {
                                popUpTo(0) { inclusive = true }
                            }
                            android.widget.Toast.makeText(
                                applicationContext,
                                "Déconnecté pour inactivité après 5 minutes",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = LoginRoute
                    ) {
                        composable<LoginRoute> {
                            LoginScreen(
                                viewModel = viewModel,
                                onNavigateToDashboard = {
                                    navController.navigate(DashboardRoute) {
                                        popUpTo(LoginRoute) { inclusive = true }
                                    }
                                },
                                onNavigateToRegister = {
                                    navController.navigate(RegisterRoute)
                                }
                            )
                        }
                        composable<RegisterRoute> {
                            RegisterScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onRegisterSuccess = {
                                    navController.navigate(DashboardRoute) {
                                        popUpTo(LoginRoute) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable<DashboardRoute> {
                            DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToStudents = {
                                    navController.navigate(StudentsRoute)
                                },
                                onNavigateToExpenses = {
                                    navController.navigate(ExpensesRoute)
                                },
                                onLogout = {
                                    viewModel.logout()
                                    navController.navigate(LoginRoute) {
                                        popUpTo(DashboardRoute) { inclusive = true }
                                    }
                                },
                                onNavigateToStudentDetail = { id ->
                                    navController.navigate(StudentDetailRoute(id))
                                },
                                onNavigateToAcademic = {
                                    navController.navigate(AcademicRoute)
                                }
                            )
                        }
                        composable<AcademicRoute> {
                            AcademicScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable<StudentsRoute> {
                            StudentsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onAddStudent = { navController.navigate(AddStudentRoute) },
                                onAddPayment = { studentId, studentName -> 
                                    navController.navigate(AddPaymentRoute(studentId, studentName))
                                },
                                onStudentClick = { studentId ->
                                    navController.navigate(StudentDetailRoute(studentId))
                                }
                            )
                        }
                        composable<AddStudentRoute> {
                            AddStudentScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable<AddPaymentRoute> { backStackEntry ->
                            val route: AddPaymentRoute = backStackEntry.toRoute()
                            AddPaymentScreen(
                                studentId = route.studentId,
                                studentName = route.studentName,
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable<StudentDetailRoute> { backStackEntry ->
                            val route: StudentDetailRoute = backStackEntry.toRoute()
                            StudentDetailScreen(
                                studentId = route.studentId,
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onAddPayment = { studentId, studentName -> 
                                    navController.navigate(AddPaymentRoute(studentId, studentName))
                                }
                            )
                        }
                        composable<ExpensesRoute> {
                            ExpensesScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onAddExpense = { navController.navigate(AddExpenseRoute) }
                            )
                        }
                        composable<AddExpenseRoute> {
                            AddExpenseScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
