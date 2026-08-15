package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.models.Expense
import com.example.data.models.Payment
import com.example.data.models.SchoolAccount
import com.example.data.models.Student
import com.example.data.models.Subject
import com.example.data.models.StudentGrade

@Database(entities = [Student::class, Payment::class, Expense::class, SchoolAccount::class, Subject::class, StudentGrade::class], version = 22, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun schoolDao(): SchoolDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        val MIGRATION_20_21 = object : Migration(20, 21) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE school_accounts ADD COLUMN currency TEXT NOT NULL DEFAULT 'GNF'")
            }
        }

        val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE school_accounts ADD COLUMN logoBase64 TEXT")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "scolapay_database"
                )
                .addMigrations(MIGRATION_20_21, MIGRATION_21_22)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
