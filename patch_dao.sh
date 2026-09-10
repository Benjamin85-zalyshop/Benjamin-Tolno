sed -i 's/@Insert(onConflict = OnConflictStrategy.REPLACE)//g' app/src/main/java/com/example/data/local/SchoolDao.kt
sed -i '/suspend fun updateSubject(subject: Subject)/i \    @Insert(onConflict = OnConflictStrategy.REPLACE)' app/src/main/java/com/example/data/local/SchoolDao.kt
