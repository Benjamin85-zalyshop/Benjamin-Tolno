package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.models.Expense
import com.example.data.models.Payment
import com.example.data.models.SchoolAccount
import com.example.data.models.Student
import com.example.data.models.Subject
import com.example.data.models.StudentGrade

@Database(entities = [Student::class, Payment::class, Expense::class, SchoolAccount::class, Subject::class, StudentGrade::class], version = 20, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun schoolDao(): SchoolDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "scolapay_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
