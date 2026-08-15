import re

with open('app/src/main/java/com/example/data/local/AppDatabase.kt', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace("version = 20", "version = 21")
content = content.replace("import androidx.room.RoomDatabase", "import androidx.room.RoomDatabase\nimport androidx.room.migration.Migration\nimport androidx.sqlite.db.SupportSQLiteDatabase")

migration = """
        val MIGRATION_20_21 = object : Migration(20, 21) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE school_accounts ADD COLUMN currency TEXT NOT NULL DEFAULT 'GNF'")
            }
        }

        fun getDatabase(context: Context): AppDatabase {"""

content = content.replace("fun getDatabase(context: Context): AppDatabase {", migration)
content = content.replace(".fallbackToDestructiveMigration()", ".addMigrations(MIGRATION_20_21)\n                .fallbackToDestructiveMigration()")

with open('app/src/main/java/com/example/data/local/AppDatabase.kt', 'w', encoding='utf-8') as f:
    f.write(content)
