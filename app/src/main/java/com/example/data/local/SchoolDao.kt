package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.models.Expense
import com.example.data.models.Payment
import com.example.data.models.Student
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolDao {
    @Query("SELECT * FROM students WHERE schoolId = :schoolId ORDER BY lastName ASC, firstName ASC")
    fun getAllStudents(schoolId: Int): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE schoolId = :schoolId")
    suspend fun getAllStudentsDirect(schoolId: Int): List<Student>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student)

    @Query("SELECT * FROM payments WHERE studentId = :studentId ORDER BY date DESC")
    fun getPaymentsForStudent(studentId: Int): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE schoolId = :schoolId ORDER BY date DESC")
    fun getAllPayments(schoolId: Int): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE schoolId = :schoolId")
    suspend fun getAllPaymentsDirect(schoolId: Int): List<Payment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment)
    
    @Query("SELECT SUM(amount) FROM payments WHERE schoolId = :schoolId")
    fun getTotalCollected(schoolId: Int): Flow<Long?>

    @Query("DELETE FROM payments WHERE id = :paymentId")
    suspend fun deletePaymentById(paymentId: Int)

    @Query("SELECT * FROM expenses WHERE schoolId = :schoolId ORDER BY date DESC")
    fun getAllExpenses(schoolId: Int): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE schoolId = :schoolId")
    suspend fun getAllExpensesDirect(schoolId: Int): List<Expense>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)
    
    @Query("SELECT SUM(amount) FROM expenses WHERE schoolId = :schoolId")
    fun getTotalExpenses(schoolId: Int): Flow<Long?>

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpenseById(expenseId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchoolAccount(account: com.example.data.models.SchoolAccount)

    @Query("SELECT * FROM school_accounts WHERE schoolName = :name LIMIT 1")
    suspend fun getSchoolAccountByName(name: String): com.example.data.models.SchoolAccount?
    
    @Query("SELECT COUNT(*) FROM school_accounts")
    suspend fun getAccountCount(): Int
    
    @Query("UPDATE school_accounts SET hasActiveSubscription = 1, isPendingValidation = 0 WHERE id = :schoolId")
    suspend fun activateSubscription(schoolId: Int)

    @Query("SELECT hasActiveSubscription FROM school_accounts WHERE id = :schoolId")
    fun getSubscriptionStatus(schoolId: Int): Flow<Boolean>

    @Query("UPDATE school_accounts SET isPendingValidation = 1, paymentPhoneNumber = :phoneNumber, transactionId = :transactionId WHERE id = :schoolId")
    suspend fun submitSubscriptionRequest(schoolId: Int, phoneNumber: String, transactionId: String)
    
    @Query("SELECT isPendingValidation FROM school_accounts WHERE id = :schoolId")
    fun getPendingValidationStatus(schoolId: Int): Flow<Boolean>

    @Query("SELECT * FROM students WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getStudentByRemoteId(remoteId: String): Student?

    @Query("SELECT * FROM payments WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getPaymentByRemoteId(remoteId: String): Payment?

    @Query("SELECT * FROM expenses WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getExpenseByRemoteId(remoteId: String): Expense?

    @Query("SELECT id FROM students WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getStudentIdByRemoteId(remoteId: String): Int?

    @Query("DELETE FROM students WHERE id = :studentId")
    suspend fun deleteStudentById(studentId: Int)

    @Query("DELETE FROM students WHERE remoteId = :remoteId")
    suspend fun deleteStudentByRemoteId(remoteId: String)

    @Query("DELETE FROM payments WHERE remoteId = :remoteId")
    suspend fun deletePaymentByRemoteId(remoteId: String)

    @Query("DELETE FROM expenses WHERE remoteId = :remoteId")
    suspend fun deleteExpenseByRemoteId(remoteId: String)
}
