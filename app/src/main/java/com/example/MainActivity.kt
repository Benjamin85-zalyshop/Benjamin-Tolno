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
import com.example.ui.navigation.DashboardRoute
import com.example.ui.navigation.ExpensesRoute
import com.example.ui.navigation.LoginRoute
import com.example.ui.navigation.RegisterRoute
import com.example.ui.navigation.StudentDetailRoute
import com.example.ui.navigation.StudentsRoute
import com.example.ui.screens.AddExpenseScreen
import com.example.ui.screens.AddPaymentScreen
import com.example.ui.screens.AddStudentScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExpensesScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.RegisterScreen
import com.example.ui.screens.StudentDetailScreen
import com.example.ui.screens.StudentsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
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
                                }
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
