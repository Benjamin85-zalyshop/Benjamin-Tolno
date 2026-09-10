sed -i 's/suspend fun insertSubject/    @Insert(onConflict = OnConflictStrategy.REPLACE)\n    suspend fun insertSubject/g' app/src/main/java/com/example/data/local/SchoolDao.kt
sed -i 's/suspend fun insertGrade/    @Insert(onConflict = OnConflictStrategy.REPLACE)\n    suspend fun insertGrade/g' app/src/main/java/com/example/data/local/SchoolDao.kt
sed -i 's/suspend fun insertStudent/    @Insert(onConflict = OnConflictStrategy.REPLACE)\n    suspend fun insertStudent/g' app/src/main/java/com/example/data/local/SchoolDao.kt
sed -i 's/suspend fun insertPayment/    @Insert(onConflict = OnConflictStrategy.REPLACE)\n    suspend fun insertPayment/g' app/src/main/java/com/example/data/local/SchoolDao.kt
sed -i 's/suspend fun insertExpense/    @Insert(onConflict = OnConflictStrategy.REPLACE)\n    suspend fun insertExpense/g' app/src/main/java/com/example/data/local/SchoolDao.kt
sed -i 's/suspend fun insertSchoolAccount/    @Insert(onConflict = OnConflictStrategy.REPLACE)\n    suspend fun insertSchoolAccount/g' app/src/main/java/com/example/data/local/SchoolDao.kt
