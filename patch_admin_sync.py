with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r') as f:
    content = f.read()

force_sync = """    fun forceSyncSchools() {
        firestore.collection("schools").get().addOnSuccessListener { snapshot ->
            viewModelScope.launch {
                for (doc in snapshot.documents) {
                    val email = doc.id
                    val displayName = doc.getString("displayName") ?: email
                    val hasActiveSubscription = doc.getBoolean("hasActiveSubscription") ?: false
                    val isPendingValidation = doc.getBoolean("isPendingValidation") ?: false
                    val founderPhone = doc.getString("founderPhone") ?: ""
                    val financierPasswordHash = doc.getString("financierPasswordHash") ?: ""
                    val passwordHash = doc.getString("passwordHash") ?: ""
                    val logoBase64 = doc.getString("logoBase64")
                    val address = doc.getString("address") ?: ""
                    val paymentPhoneNumber = doc.getString("paymentPhoneNumber")
                    val transactionId = doc.getString("transactionId")
                    val rejectionReason = doc.getString("rejectionReason")
                    val subscriptionExpiryDate = doc.getLong("subscriptionExpiryDate") ?: 0L
                    val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    
                    val existing = repository.getSchoolAccountByName(email)
                    if (existing != null) {
                        repository.updateSchoolAccount(
                            existing.copy(
                                displayName = displayName,
                                hasActiveSubscription = hasActiveSubscription,
                                isPendingValidation = isPendingValidation,
                                founderPhone = founderPhone,
                                financierPasswordHash = financierPasswordHash,
                                passwordHash = passwordHash,
                                logoBase64 = logoBase64 ?: existing.logoBase64,
                                address = address,
                                paymentPhoneNumber = paymentPhoneNumber ?: existing.paymentPhoneNumber,
                                transactionId = transactionId ?: existing.transactionId,
                                rejectionReason = rejectionReason,
                                subscriptionExpiryDate = subscriptionExpiryDate,
                                createdAt = createdAt
                            )
                        )
                    } else {
                        repository.insertSchoolAccountDirect(
                            SchoolAccount(
                                schoolName = email,
                                displayName = displayName,
                                hasActiveSubscription = hasActiveSubscription,
                                isPendingValidation = isPendingValidation,
                                founderPhone = founderPhone,
                                financierPasswordHash = financierPasswordHash,
                                passwordHash = passwordHash,
                                logoBase64 = logoBase64,
                                address = address,
                                paymentPhoneNumber = paymentPhoneNumber,
                                transactionId = transactionId,
                                rejectionReason = rejectionReason,
                                subscriptionExpiryDate = subscriptionExpiryDate,
                                createdAt = createdAt
                            )
                        )
                    }
                }
                loadAdminSchools()
            }
        }.addOnFailureListener { e ->
            _adminError.value = "Erreur de synchronisation: ${e.message}"
        }
    }

    fun loadAdminSchools() {"""

content = content.replace("    fun loadAdminSchools() {", force_sync)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w') as f:
    f.write(content)
