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

@Database(entities = [Student::class, Payment::class, Expense::class, SchoolAccount::class, Subject::class, StudentGrade::class], version = 24, exportSchema = false)
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

        
        val MIGRATION_22_23 = object : Migration(22, 23) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE expenses ADD COLUMN schoolYear TEXT NOT NULL DEFAULT '2025-2026'")
            }
        }

        val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE school_accounts ADD COLUMN logoBase64 TEXT")
            }
        }

        val MIGRATION_23_24 = object : Migration(23, 24) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE payments ADD COLUMN isCancelled INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE payments ADD COLUMN cancellationReason TEXT")
                database.execSQL("ALTER TABLE payments ADD COLUMN cancelledBy TEXT")
                database.execSQL("ALTER TABLE payments ADD COLUMN cancelledAt INTEGER")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "scolapay_database"
                )
                .addMigrations(MIGRATION_20_21, MIGRATION_21_22, MIGRATION_22_23, MIGRATION_23_24)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
