import re
with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r') as f:
    content = f.read()

replacement = """        val schoolDocListener = firestore.collection("schools").document(email).addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) return@addSnapshotListener
            
            // Sync class fees
            val classFeesStr = snapshot.getString("classFeesStr")
            if (classFeesStr != null) {
                try {
                    val fees = kotlinx.serialization.json.Json.decodeFromString<List<ClassFee>>(classFeesStr)
                    _classFees.value = fees
                    sharedPrefs.edit().putString("class_fees_$schoolId", classFeesStr).apply()
                } catch (e: Exception) {
                    android.util.Log.e("ScolaPay-Firebase", "Error syncing class fees", e)
                }
            }
            
            // Sync school account details (Logo, displayName, subscriptions)
            viewModelScope.launch {
                val currentAccount = repository.getSchoolAccountByName(email)
                if (currentAccount != null) {
                    val remoteLogo = snapshot.getString("logoBase64")
                    val remoteDisplayName = snapshot.getString("displayName")
                    val remoteAddress = snapshot.getString("address")
                    val remotePhone = snapshot.getString("founderPhone")
                    val hasSub = snapshot.getBoolean("hasActiveSubscription") ?: currentAccount.hasActiveSubscription
                    val pendingVal = snapshot.getBoolean("isPendingValidation") ?: currentAccount.isPendingValidation
                    val subExpiry = snapshot.getLong("subscriptionExpiryDate") ?: currentAccount.subscriptionExpiryDate
                    val remoteFinancierPwd = snapshot.getString("financierPasswordHash")
                    val remotePwd = snapshot.getString("passwordHash")

                    var isUpdated = false
                    var updatedAccount = currentAccount
                    
                    if (remoteLogo != null && remoteLogo != updatedAccount.logoBase64) {
                        updatedAccount = updatedAccount.copy(logoBase64 = remoteLogo)
                        _schoolLogoBase64.value = remoteLogo
                        isUpdated = true
                    }
                    if (remoteDisplayName != null && remoteDisplayName != updatedAccount.displayName) {
                        updatedAccount = updatedAccount.copy(displayName = remoteDisplayName)
                        _schoolName.value = remoteDisplayName
                        isUpdated = true
                    }
                    if (remoteAddress != null && remoteAddress != updatedAccount.address) {
                        updatedAccount = updatedAccount.copy(address = remoteAddress)
                        isUpdated = true
                    }
                    if (remotePhone != null && remotePhone != updatedAccount.founderPhone) {
                        updatedAccount = updatedAccount.copy(founderPhone = remotePhone)
                        isUpdated = true
                    }
                    if (hasSub != updatedAccount.hasActiveSubscription || pendingVal != updatedAccount.isPendingValidation || subExpiry != updatedAccount.subscriptionExpiryDate) {
                        updatedAccount = updatedAccount.copy(
                            hasActiveSubscription = hasSub,
                            isPendingValidation = pendingVal,
                            subscriptionExpiryDate = subExpiry
                        )
                        isUpdated = true
                    }
                    if (remoteFinancierPwd != null && remoteFinancierPwd != updatedAccount.financierPasswordHash) {
                        updatedAccount = updatedAccount.copy(financierPasswordHash = remoteFinancierPwd)
                        isUpdated = true
                    }
                    if (remotePwd != null && remotePwd != updatedAccount.passwordHash) {
                        updatedAccount = updatedAccount.copy(passwordHash = remotePwd)
                        isUpdated = true
                    }
                    
                    if (isUpdated) {
                        repository.updateSchoolAccount(updatedAccount)
                        _schoolAccount.value = updatedAccount
                    }
                }
            }
        }"""

content = re.sub(
    r'        val schoolDocListener = firestore\.collection\("schools"\)\.document\(email\)\.addSnapshotListener \{ snapshot, e ->\n.*?\}\n        \}\n',
    replacement + '\n',
    content,
    flags=re.DOTALL
)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w') as f:
    f.write(content)
