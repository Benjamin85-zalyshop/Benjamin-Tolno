package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.models.Expense
import com.example.data.models.Payment
import com.example.data.models.Student
import com.example.data.models.Subject
import com.example.data.models.StudentGrade
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolDao {
    // Subjects
    @Query("SELECT * FROM subjects WHERE schoolId = :schoolId AND section = :section AND grade = :grade ORDER BY name ASC")
    fun getSubjectsForGrade(schoolId: Int, section: String, grade: String): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE schoolId = :schoolId ORDER BY section ASC, grade ASC, name ASC")
    fun getAllSubjects(schoolId: Int): Flow<List<Subject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject)

    @Query("DELETE FROM subjects WHERE id = :subjectId")
    suspend fun deleteSubjectById(subjectId: Int)

    @Query("DELETE FROM subjects WHERE remoteId = :remoteId")
    suspend fun deleteSubjectByRemoteId(remoteId: String)

    @Query("SELECT * FROM subjects WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getSubjectByRemoteId(remoteId: String): Subject?

    // Grades
    @Query("SELECT * FROM grades WHERE schoolId = :schoolId AND studentId = :studentId AND term = :term")
    fun getGradesForStudentAndTerm(schoolId: Int, studentId: Int, term: String): Flow<List<StudentGrade>>

    @Query("SELECT * FROM grades WHERE schoolId = :schoolId AND studentId = :studentId")
    fun getAllGradesForStudent(schoolId: Int, studentId: Int): Flow<List<StudentGrade>>

    @Query("SELECT * FROM grades WHERE schoolId = :schoolId AND term = :term")
    fun getAllGradesForTerm(schoolId: Int, term: String): Flow<List<StudentGrade>>

    @Query("SELECT * FROM grades WHERE schoolId = :schoolId")
    fun getAllGrades(schoolId: Int): Flow<List<StudentGrade>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrade(grade: StudentGrade)

    @Query("DELETE FROM grades WHERE id = :gradeId")
    suspend fun deleteGradeById(gradeId: Int)

    @Query("DELETE FROM grades WHERE remoteId = :remoteId")
    suspend fun deleteGradeByRemoteId(remoteId: String)

    @Query("SELECT * FROM grades WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getGradeByRemoteId(remoteId: String): StudentGrade?
    @Query("SELECT * FROM students WHERE schoolId = :schoolId ORDER BY lastName ASC, firstName ASC")
    fun getAllStudents(schoolId: Int): Flow<List<Student>>

    
    @Query("SELECT * FROM students WHERE schoolId = :schoolId")
    suspend fun getAllStudentsDirect(schoolId: Int): List<Student>

    @Query("SELECT * FROM subjects WHERE schoolId = :schoolId")
    suspend fun getAllSubjectsDirect(schoolId: Int): List<Subject>

    @Query("SELECT * FROM grades WHERE schoolId = :schoolId")
    suspend fun getAllGradesDirect(schoolId: Int): List<StudentGrade>


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
    
    @Query("DELETE FROM school_accounts WHERE schoolName = :name")
    suspend fun deleteSchoolAccountByName(name: String)
    
    @Query("DELETE FROM students WHERE schoolId = :schoolId")
    suspend fun deleteStudentsBySchoolId(schoolId: Int)
    
    @Query("DELETE FROM payments WHERE schoolId = :schoolId")
    suspend fun deletePaymentsBySchoolId(schoolId: Int)
    
    @Query("DELETE FROM expenses WHERE schoolId = :schoolId")
    suspend fun deleteExpensesBySchoolId(schoolId: Int)
    
    @Query("DELETE FROM grades WHERE schoolId = :schoolId")
    suspend fun deleteGradesBySchoolId(schoolId: Int)
    
    @Query("DELETE FROM subjects WHERE schoolId = :schoolId")
    suspend fun deleteSubjectsBySchoolId(schoolId: Int)
    
    @Query("SELECT COUNT(*) FROM school_accounts")
    suspend fun getAccountCount(): Int
    
    @Query("UPDATE school_accounts SET hasActiveSubscription = 1, isPendingValidation = 0, subscriptionExpiryDate = :expiryDate WHERE id = :schoolId")
    suspend fun activateSubscription(schoolId: Int, expiryDate: Long)

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
