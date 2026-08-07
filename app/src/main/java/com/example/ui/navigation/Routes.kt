package com.example.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object LoginRoute

@Serializable
object RegisterRoute

@Serializable
object DashboardRoute

@Serializable
object StudentsRoute

@Serializable
object AddStudentRoute

@Serializable
data class AddPaymentRoute(val studentId: Int, val studentName: String)

@Serializable
data class StudentDetailRoute(val studentId: Int)

@Serializable
object ExpensesRoute

@Serializable
object AddExpenseRoute

@Serializable
object AcademicRoute
