package com.example.data.repository

import com.example.data.local.SchoolDao
import com.example.data.models.Expense
import com.example.data.models.Payment
import com.example.data.models.Student
import kotlinx.coroutines.flow.Flow

class SchoolRepository(private val schoolDao: SchoolDao) {
    fun getAllStudents(schoolId: Int): Flow<List<Student>> = schoolDao.getAllStudents(schoolId)
    fun getAllPayments(schoolId: Int): Flow<List<Payment>> = schoolDao.getAllPayments(schoolId)
    fun getTotalCollected(schoolId: Int): Flow<Long?> = schoolDao.getTotalCollected(schoolId)
    
    fun getAllExpenses(schoolId: Int): Flow<List<Expense>> = schoolDao.getAllExpenses(schoolId)
    fun getTotalExpenses(schoolId: Int): Flow<Long?> = schoolDao.getTotalExpenses(schoolId)

    fun getPaymentsForStudent(studentId: Int): Flow<List<Payment>> = schoolDao.getPaymentsForStudent(studentId)

    fun getSubscriptionStatus(schoolId: Int): Flow<Boolean> = schoolDao.getSubscriptionStatus(schoolId)
    fun getPendingValidationStatus(schoolId: Int): Flow<Boolean> = schoolDao.getPendingValidationStatus(schoolId)

    suspend fun submitSubscriptionRequest(schoolId: Int, phoneNumber: String, transactionId: String) {
        schoolDao.submitSubscriptionRequest(schoolId, phoneNumber, transactionId)
    }

    suspend fun activateSubscription(schoolId: Int) {
        schoolDao.activateSubscription(schoolId)
    }

    suspend fun insertStudent(student: Student) {
        schoolDao.insertStudent(student)
    }

    suspend fun insertPayment(payment: Payment) {
        schoolDao.insertPayment(payment)
    }

    suspend fun deletePayment(paymentId: Int) {
        schoolDao.deletePaymentById(paymentId)
    }
    
    suspend fun insertExpense(expense: Expense) {
        schoolDao.insertExpense(expense)
    }

    suspend fun deleteExpense(expenseId: Int) {
        schoolDao.deleteExpenseById(expenseId)
    }

    suspend fun registerSchool(name: String, founderPassword: String, financierPassword: String, displayName: String = "", address: String = "", founderPhone: String = "") {
        schoolDao.insertSchoolAccount(com.example.data.models.SchoolAccount(schoolName = name, passwordHash = founderPassword, financierPasswordHash = financierPassword, displayName = displayName, address = address, founderPhone = founderPhone))
    }

    suspend fun getSchoolAccountByName(name: String): com.example.data.models.SchoolAccount? {
        return schoolDao.getSchoolAccountByName(name)
    }

    suspend fun hasAccount(): Boolean {
        return schoolDao.getAccountCount() > 0
    }

    suspend fun getStudentByRemoteId(remoteId: String): Student? = schoolDao.getStudentByRemoteId(remoteId)
    suspend fun getPaymentByRemoteId(remoteId: String): Payment? = schoolDao.getPaymentByRemoteId(remoteId)
    suspend fun getExpenseByRemoteId(remoteId: String): Expense? = schoolDao.getExpenseByRemoteId(remoteId)
    suspend fun getStudentIdByRemoteId(remoteId: String): Int? = schoolDao.getStudentIdByRemoteId(remoteId)
    suspend fun deleteStudentById(studentId: Int) = schoolDao.deleteStudentById(studentId)
    suspend fun deleteStudentByRemoteId(remoteId: String) = schoolDao.deleteStudentByRemoteId(remoteId)
    suspend fun deletePaymentByRemoteId(remoteId: String) = schoolDao.deletePaymentByRemoteId(remoteId)
    suspend fun deleteExpenseByRemoteId(remoteId: String) = schoolDao.deleteExpenseByRemoteId(remoteId)
    suspend fun insertSchoolAccountDirect(account: com.example.data.models.SchoolAccount) = schoolDao.insertSchoolAccount(account)

    suspend fun getAllStudentsDirect(schoolId: Int): List<Student> = schoolDao.getAllStudentsDirect(schoolId)
    suspend fun getAllPaymentsDirect(schoolId: Int): List<Payment> = schoolDao.getAllPaymentsDirect(schoolId)
    suspend fun getAllExpensesDirect(schoolId: Int): List<Expense> = schoolDao.getAllExpensesDirect(schoolId)
}
