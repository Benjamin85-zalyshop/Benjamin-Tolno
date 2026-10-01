with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r') as f:
    content = f.read()

# 1. Fix syncSchoolsFromFirestore
old_sync_fs = '''                    val isLocallySettled = isCommissionSettled(email) || isCommissionSettled(displayName) || (existing != null && (isCommissionSettled(existing.schoolName) || isCommissionSettled(existing.displayName)))
                    val effectiveIsLocked = if (isLocallySettled) false else isAppLocked
                    val effectiveComm = if (isLocallySettled) 0L else unpaidCommission
                    val effectiveLock = if (isLocallySettled) "" else (lockReason ?: "")'''

new_sync_fs = '''                    val isLocallySettled = isCommissionSettled(email) || isCommissionSettled(displayName) || (existing != null && (isCommissionSettled(existing.schoolName) || isCommissionSettled(existing.displayName)))
                    val settledCount = maxOf(getCommissionSettledCount(email), getCommissionSettledCount(displayName), existing?.let { getCommissionSettledCount(it.schoolName) } ?: 0)
                    val effectiveIsLocked = if (isLocallySettled && unpaidCommission <= 0L) false else isAppLocked
                    val calcComm = if (isLocallySettled && settledCount > 0) maxOf(0L, (onlinePaymentsCount - settledCount) * 3000L) else (onlinePaymentsCount * 3000L)
                    val effectiveComm = if (isLocallySettled && calcComm == 0L && unpaidCommission == 0L) 0L else maxOf(unpaidCommission, calcComm)
                    val effectiveLock = if (isLocallySettled && unpaidCommission <= 0L) "" else (lockReason ?: "")'''

assert old_sync_fs in content, 'old_sync_fs not found!'
content = content.replace(old_sync_fs, new_sync_fs, 1)

# 2. Fix forceSyncSchools
old_force_fs = '''                        val isLocallySettled = isCommissionSettled(email) || isCommissionSettled(displayName) || (existing != null && (isCommissionSettled(existing.schoolName) || isCommissionSettled(existing.displayName)))
                        val effectiveIsLocked = if (isLocallySettled) false else isAppLocked
                        val effectiveComm = if (isLocallySettled) 0L else unpaidCommission
                        val effectiveLock = if (isLocallySettled) "" else (lockReason ?: "")'''

new_force_fs = '''                        val isLocallySettled = isCommissionSettled(email) || isCommissionSettled(displayName) || (existing != null && (isCommissionSettled(existing.schoolName) || isCommissionSettled(existing.displayName)))
                        val settledCount = maxOf(getCommissionSettledCount(email), getCommissionSettledCount(displayName), existing?.let { getCommissionSettledCount(it.schoolName) } ?: 0)
                        val effectiveIsLocked = if (isLocallySettled && unpaidCommission <= 0L) false else isAppLocked
                        val calcComm = if (isLocallySettled && settledCount > 0) maxOf(0L, (onlinePaymentsCount - settledCount) * 3000L) else (onlinePaymentsCount * 3000L)
                        val effectiveComm = if (isLocallySettled && calcComm == 0L && unpaidCommission == 0L) 0L else maxOf(unpaidCommission, calcComm)
                        val effectiveLock = if (isLocallySettled && unpaidCommission <= 0L) "" else (lockReason ?: "")'''

assert old_force_fs in content, 'old_force_fs not found!'
content = content.replace(old_force_fs, new_force_fs, 1)

# 3. Fix syncSchoolsFromRTDB
old_rtdb_sync = '''                    val isLocallyUnlocked = isCommissionSettled(acc.schoolName) || isCommissionSettled(acc.displayName) || (!acc.isAppLocked && acc.unpaidCommission == 0L) || sharedPrefs.getBoolean("school_unlocked_global", false)
                    for (cKey in candidateKeys) {
                        if (cKey.isBlank()) continue
                        try {
                            val schoolChildSnap = rtdb.getReference("schools").child(cKey).get().await()
                            if (schoolChildSnap.exists()) {
                                val rtdbComm = schoolChildSnap.child("unpaidCommission").getValue(Long::class.java)
                                    ?: schoolChildSnap.child("unpaidCommission").getValue(Double::class.java)?.toLong() ?: 0L
                                val rtdbCount = schoolChildSnap.child("onlinePaymentsCount").getValue(Long::class.java)?.toInt()
                                    ?: schoolChildSnap.child("onlinePaymentsCount").getValue(Double::class.java)?.toInt() ?: 0
                                val rtdbTotal = schoolChildSnap.child("onlinePaymentsTotal").getValue(Long::class.java)
                                    ?: schoolChildSnap.child("onlinePaymentsTotal").getValue(Double::class.java)?.toLong() ?: 0L
                                val rtdbOnlineEnabled = schoolChildSnap.child("onlinePaymentEnabled").getValue(Boolean::class.java)
                                val rtdbIsLocked = schoolChildSnap.child("isAppLocked").getValue(Boolean::class.java)
                                val rtdbLock = schoolChildSnap.child("lockReason").getValue(String::class.java)

                                if (!isLocallyUnlocked && rtdbComm > bestComm) bestComm = rtdbComm
                                if (rtdbCount > bestCount) bestCount = rtdbCount
                                if (rtdbTotal > bestTotal) bestTotal = rtdbTotal
                                if (rtdbOnlineEnabled != null) bestOnlineEnabled = rtdbOnlineEnabled
                                if (!isLocallyUnlocked && rtdbIsLocked != null) bestIsLocked = rtdbIsLocked
                                if (!isLocallyUnlocked && rtdbLock != null) bestLockReason = rtdbLock
                            }
                        } catch (childErr: Exception) {
                            android.util.Log.d("ScolaPay", "Child read notice for $cKey: ${childErr.message}")
                        }
                    }

                    if (isLocallyUnlocked) {
                        bestComm = 0L
                        bestIsLocked = false
                        bestLockReason = ""
                    }'''

new_rtdb_sync = '''                    val isLocallyUnlocked = (isCommissionSettled(acc.schoolName) || isCommissionSettled(acc.displayName)) && acc.unpaidCommission == 0L
                    for (cKey in candidateKeys) {
                        if (cKey.isBlank()) continue
                        try {
                            val schoolChildSnap = rtdb.getReference("schools").child(cKey).get().await()
                            if (schoolChildSnap.exists()) {
                                val rtdbComm = schoolChildSnap.child("unpaidCommission").getValue(Long::class.java)
                                    ?: schoolChildSnap.child("unpaidCommission").getValue(Double::class.java)?.toLong() ?: 0L
                                val rtdbCount = schoolChildSnap.child("onlinePaymentsCount").getValue(Long::class.java)?.toInt()
                                    ?: schoolChildSnap.child("onlinePaymentsCount").getValue(Double::class.java)?.toInt() ?: 0
                                val rtdbTotal = schoolChildSnap.child("onlinePaymentsTotal").getValue(Long::class.java)
                                    ?: schoolChildSnap.child("onlinePaymentsTotal").getValue(Double::class.java)?.toLong() ?: 0L
                                val rtdbOnlineEnabled = schoolChildSnap.child("onlinePaymentEnabled").getValue(Boolean::class.java)
                                val rtdbIsLocked = schoolChildSnap.child("isAppLocked").getValue(Boolean::class.java)
                                val rtdbLock = schoolChildSnap.child("lockReason").getValue(String::class.java)

                                if (rtdbComm > bestComm) bestComm = rtdbComm
                                if (rtdbCount > bestCount) bestCount = rtdbCount
                                if (rtdbTotal > bestTotal) bestTotal = rtdbTotal
                                if (rtdbOnlineEnabled != null) bestOnlineEnabled = rtdbOnlineEnabled
                                if (!isLocallyUnlocked && rtdbIsLocked != null) bestIsLocked = rtdbIsLocked
                                if (!isLocallyUnlocked && rtdbLock != null) bestLockReason = rtdbLock
                            }
                        } catch (childErr: Exception) {
                            android.util.Log.d("ScolaPay", "Child read notice for $cKey: ${childErr.message}")
                        }
                    }

                    if (isLocallyUnlocked) {
                        bestIsLocked = false
                        bestLockReason = ""
                    }'''

assert old_rtdb_sync in content, 'old_rtdb_sync not found!'
content = content.replace(old_rtdb_sync, new_rtdb_sync, 1)

# 4. Fix processRtdbSchoolsSnapshot
old_proc_rtdb = '''                val isLocallyUnlocked = isCommissionSettled(matchedAccount.schoolName) || isCommissionSettled(matchedAccount.displayName) || (!matchedAccount.isAppLocked && matchedAccount.unpaidCommission == 0L) || sharedPrefs.getBoolean("school_unlocked_global", false)
                val newCommission = if (isLocallyUnlocked) 0L else maxOf(matchedAccount.unpaidCommission, rtdbCommission)
                val newCount = maxOf(matchedAccount.onlinePaymentsCount, rtdbCount)
                val newTotal = maxOf(matchedAccount.onlinePaymentsTotal, rtdbTotal)
                val newOnlineEnabled = rtdbOnlinePaymentEnabled ?: matchedAccount.onlinePaymentEnabled
                val newIsLocked = if (isLocallyUnlocked) false else (rtdbIsAppLocked ?: matchedAccount.isAppLocked)
                val newLockReason = if (isLocallyUnlocked) "" else (rtdbLockReason ?: matchedAccount.lockReason)'''

new_proc_rtdb = '''                val isLocallyUnlocked = (isCommissionSettled(matchedAccount.schoolName) || isCommissionSettled(matchedAccount.displayName)) && matchedAccount.unpaidCommission == 0L
                val newCommission = maxOf(matchedAccount.unpaidCommission, rtdbCommission)
                val newCount = maxOf(matchedAccount.onlinePaymentsCount, rtdbCount)
                val newTotal = maxOf(matchedAccount.onlinePaymentsTotal, rtdbTotal)
                val newOnlineEnabled = rtdbOnlinePaymentEnabled ?: matchedAccount.onlinePaymentEnabled
                val newIsLocked = if (isLocallyUnlocked) false else (rtdbIsAppLocked ?: matchedAccount.isAppLocked)
                val newLockReason = if (isLocallyUnlocked) "" else (rtdbLockReason ?: matchedAccount.lockReason)'''

assert old_proc_rtdb in content, 'old_proc_rtdb not found!'
content = content.replace(old_proc_rtdb, new_proc_rtdb, 1)

# 5. Fix cleanupDuplicateSchoolAccounts
old_cleanup = '''            // Purge dummy accounts without '@' in email, and heal commission-locked accounts
            val current = repository.getAllSchoolAccounts()
            for (acc in current) {
                if (!acc.schoolName.contains("@")) {
                    repository.deleteSchoolAccountByName(acc.schoolName)
                } else if (acc.isAppLocked && (acc.lockReason?.contains("commission", ignoreCase = true) == true || isCommissionSettled(acc.schoolName))) {
                    val unlocked = acc.copy(isAppLocked = false, lockReason = "", unpaidCommission = 0L)
                    repository.updateSchoolAccount(unlocked)
                    setCommissionSettled(acc.schoolName, true, acc.onlinePaymentsCount)
                    if (acc.displayName.isNotBlank()) setCommissionSettled(acc.displayName, true, acc.onlinePaymentsCount)
                }
            }'''

new_cleanup = '''            // Purge dummy accounts without '@' in email
            val current = repository.getAllSchoolAccounts()
            for (acc in current) {
                if (!acc.schoolName.contains("@")) {
                    repository.deleteSchoolAccountByName(acc.schoolName)
                }
            }'''

assert old_cleanup in content, 'old_cleanup not found!'
content = content.replace(old_cleanup, new_cleanup, 1)

# 6. Fix init block
old_init = '''    init {
        sharedPrefs.edit().remove("school_unlocked_global").apply()
        viewModelScope.launch {
            cleanupDuplicateSchoolAccounts()
            loadAdminSchools()
        }
        syncSchoolsFromFirestore()
        viewModelScope.launch {
            val loggedInEmail = sharedPrefs.getString("logged_in_email", null)
            val loggedInRole = sharedPrefs.getString("logged_in_role", null)
            
            if (loggedInEmail != null && loggedInRole != null) {
                if (loggedInRole == "ADMIN" && loggedInEmail.equals("benjamintolno7@gmail.com", ignoreCase = true)) {
                    _userRole.value = "ADMIN"
                    _schoolName.value = "ScolaPay Admin"
                    _currentSchoolId.value = -1
                    ensureAdminFirebaseAuth()
                    cleanupDuplicateSchoolAccounts()
                    loadAdminSchools()
                    listenToSchoolsFromRTDB()
                    forceSyncSchools()
                } else {
                    var account = repository.getSchoolAccountByName(loggedInEmail)
                    if (account != null) {
                        if (account.isAppLocked && (account.lockReason?.contains("commission", ignoreCase = true) == true || account.unpaidCommission > 0L || isCommissionSettled(account.schoolName))) {
                            val unlocked = account.copy(isAppLocked = false, lockReason = "", unpaidCommission = 0L)
                            repository.updateSchoolAccount(unlocked)
                            account = unlocked
                            setCommissionSettled(account.schoolName, true, account.onlinePaymentsCount)
                            if (account.displayName.isNotBlank()) setCommissionSettled(account.displayName, true, account.onlinePaymentsCount)
                        }'''

new_init = '''    init {
        val oldSettledKeys = sharedPrefs.all.keys.filter { it.startsWith("comm_settled_") }
        val ed = sharedPrefs.edit()
        for (k in oldSettledKeys) {
            ed.remove(k)
        }
        ed.remove("school_unlocked_global").apply()
        viewModelScope.launch {
            cleanupDuplicateSchoolAccounts()
            loadAdminSchools()
        }
        syncSchoolsFromFirestore()
        viewModelScope.launch {
            val loggedInEmail = sharedPrefs.getString("logged_in_email", null)
            val loggedInRole = sharedPrefs.getString("logged_in_role", null)
            
            if (loggedInEmail != null && loggedInRole != null) {
                if (loggedInRole == "ADMIN" && loggedInEmail.equals("benjamintolno7@gmail.com", ignoreCase = true)) {
                    _userRole.value = "ADMIN"
                    _schoolName.value = "ScolaPay Admin"
                    _currentSchoolId.value = -1
                    ensureAdminFirebaseAuth()
                    cleanupDuplicateSchoolAccounts()
                    loadAdminSchools()
                    listenToSchoolsFromRTDB()
                    forceSyncSchools()
                } else {
                    var account = repository.getSchoolAccountByName(loggedInEmail)
                    if (account != null) {'''

assert old_init in content, 'old_init not found!'
content = content.replace(old_init, new_init, 1)

# 7. Fix registerSchool block
old_reg = '''        if (account != null) {
            val isAccountSettled = isCommissionSettled(account.schoolName) || (account.displayName.isNotBlank() && isCommissionSettled(account.displayName))
            if (account.isAppLocked && (isAccountSettled || account.unpaidCommission <= 0L || (account.lockReason?.contains("commission", ignoreCase = true) == true))) {
                val unlocked = account.copy(isAppLocked = false, lockReason = "", unpaidCommission = 0L)
                repository.updateSchoolAccount(unlocked)
                account = unlocked
                setCommissionSettled(account.schoolName, true, account.onlinePaymentsCount)
                if (account.displayName.isNotBlank()) setCommissionSettled(account.displayName, true, account.onlinePaymentsCount)
            }
            if (account.createdAt <= 0L) {'''

new_reg = '''        if (account != null) {
            if (account.createdAt <= 0L) {'''

assert old_reg in content, 'old_reg not found!'
content = content.replace(old_reg, new_reg, 1)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w') as f:
    f.write(content)

print('SchoolViewModel updated successfully!')
