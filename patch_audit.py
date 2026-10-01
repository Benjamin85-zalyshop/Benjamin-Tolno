with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r') as f:
    content = f.read()

target = '''    suspend fun auditOnlinePaymentsFromRTDB() {
        try {
            val rtdb = com.google.firebase.database.FirebaseDatabase.getInstance("https://scolapay-b6289-default-rtdb.europe-west1.firebasedatabase.app")
            val studentsSnap = rtdb.getReference("students").get().await()
            if (studentsSnap.exists()) {
                val allAccounts = repository.getAllSchoolAccounts()
                if (allAccounts.isEmpty()) return

                val paymentCounts = mutableMapOf<String, Int>()
                val paymentTotals = mutableMapOf<String, Long>()

                for (stChild in studentsSnap.children) {
                    val paymentsNode = stChild.child("payments")
                    if (paymentsNode.exists()) {
                        for (pChild in paymentsNode.children) {
                            val sName = pChild.child("schoolName").getValue(String::class.java)
                                ?: stChild.child("schoolName").getValue(String::class.java) ?: ""
                            val sEmail = pChild.child("schoolEmail").getValue(String::class.java)
                                ?: stChild.child("schoolEmail").getValue(String::class.java) ?: ""
                            val amt = pChild.child("amount").getValue(Long::class.java)
                                ?: pChild.child("amount").getValue(Double::class.java)?.toLong() ?: 0L
                            if (amt > 0L) {
                                if (sName.isNotBlank()) {
                                    val normN = normalizeSyncKey(sName)
                                    paymentCounts[normN] = (paymentCounts[normN] ?: 0) + 1
                                    paymentTotals[normN] = (paymentTotals[normN] ?: 0L) + amt
                                }
                                if (sEmail.isNotBlank()) {
                                    val normE = normalizeSyncKey(sEmail)
                                    paymentCounts[normE] = (paymentCounts[normE] ?: 0) + 1
                                    paymentTotals[normE] = (paymentTotals[normE] ?: 0L) + amt
                                }
                            }
                        }
                    }
                }

                var hasChanges = false
                for (acc in allAccounts) {
                    val normDisplay = normalizeSyncKey(acc.displayName)
                    val normEmail = normalizeSyncKey(acc.schoolName)

                    var count = 0
                    var total = 0L
                    val countedKeys = mutableSetOf<String>()
                    for ((normKey, c) in paymentCounts) {
                        if (normKey.isNotBlank() && (normKey == normDisplay || normKey == normEmail)) {
                            countedKeys.add(normKey)
                        }
                    }
                    for (k in countedKeys) {
                        count = maxOf(count, paymentCounts[k] ?: 0)
                        total = maxOf(total, paymentTotals[k] ?: 0L)
                    }

                    val isSettled = isCommissionSettled(acc.schoolName) || isCommissionSettled(acc.displayName) || (!acc.isAppLocked && acc.unpaidCommission == 0L) || sharedPrefs.getBoolean("school_unlocked_global", false)
                    val settledCount = maxOf(getCommissionSettledCount(acc.schoolName), getCommissionSettledCount(acc.displayName))
                    val newCount = maxOf(acc.onlinePaymentsCount, count)
                    val newTotal = maxOf(acc.onlinePaymentsTotal, total)

                    val commission = if (isSettled) {
                        val delta = (newCount - settledCount).coerceAtLeast(0)
                        delta * 3000L
                    } else {
                        newCount * 3000L
                    }
                    val newCommission = if (isSettled && commission == 0L) 0L else commission

                    if (newCount != acc.onlinePaymentsCount || newTotal != acc.onlinePaymentsTotal || newCommission != acc.unpaidCommission) {
                        val updated = acc.copy(
                            onlinePaymentsCount = newCount,
                            onlinePaymentsTotal = newTotal,
                            unpaidCommission = newCommission
                        )
                        repository.updateSchoolAccount(updated)
                        hasChanges = true
                        try {
                            firestore.collection("schools").document(acc.schoolName).set(
                                mapOf(
                                    "unpaidCommission" to newCommission,
                                    "onlinePaymentsCount" to newCount,
                                    "onlinePaymentsTotal" to newTotal
                                ),
                                com.google.firebase.firestore.SetOptions.merge()
                            )
                        } catch (eFs: Exception) {
                            android.util.Log.w("ScolaPay", "Firestore update warning in audit: ${eFs.message}")
                        }
                    }
                }
                if (hasChanges) {
                    loadAdminSchools()
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("ScolaPay", "Audit payments error: ${e.message}")
        }
    }'''

replacement = '''    suspend fun auditOnlinePaymentsFromRTDB() {
        auditOnlinePayments()
    }

    suspend fun auditOnlinePayments() {
        try {
            val allAccounts = repository.getAllSchoolAccounts()
            if (allAccounts.isEmpty()) return

            var hasChanges = false
            val rtdb = com.google.firebase.database.FirebaseDatabase.getInstance("https://scolapay-b6289-default-rtdb.europe-west1.firebasedatabase.app")
            var rtdbStudentsSnap: com.google.firebase.database.DataSnapshot? = null
            try {
                rtdbStudentsSnap = rtdb.getReference("students").get().await()
            } catch (eRtdb: Exception) {
                android.util.Log.d("ScolaPay", "RTDB students fetch notice: ${eRtdb.message}")
            }

            for (acc in allAccounts) {
                if (!acc.schoolName.contains("@")) continue

                var onlineCount = 0
                var onlineTotal = 0L
                val processedPayments = mutableSetOf<String>()

                // 1. Audit from Firestore schools/{email}/payments subcollection
                try {
                    val pSnap = firestore.collection("schools").document(acc.schoolName).collection("payments").get().await()
                    for (doc in pSnap.documents) {
                        val pid = doc.id
                        val pMethod = doc.getString("paymentMethod") ?: ""
                        val isCanc = doc.getBoolean("isCancelled") ?: false
                        val amt = doc.getLong("amount") ?: 0L
                        val isOnline = !isCanc && amt > 0L && (
                            pid.startsWith("PAY_") ||
                            pMethod.contains("ligne", ignoreCase = true) ||
                            pMethod.contains("ChapChap", ignoreCase = true) ||
                            pMethod.contains("Orange", ignoreCase = true) ||
                            pMethod.contains("MoMo", ignoreCase = true)
                        )
                        if (isOnline && processedPayments.add(pid)) {
                            onlineCount++
                            onlineTotal += amt
                        }
                    }
                } catch (eFs: Exception) {
                    android.util.Log.d("ScolaPay", "Firestore payments audit notice for ${acc.schoolName}: ${eFs.message}")
                }

                // 2. Audit from RTDB students/{id}/payments
                if (rtdbStudentsSnap != null && rtdbStudentsSnap.exists()) {
                    val normAccName = normalizeSyncKey(acc.displayName)
                    val normAccEmail = normalizeSyncKey(acc.schoolName)
                    for (stChild in rtdbStudentsSnap.children) {
                        val paymentsNode = stChild.child("payments")
                        if (paymentsNode.exists()) {
                            for (pChild in paymentsNode.children) {
                                val pid = pChild.key ?: continue
                                val sName = pChild.child("schoolName").getValue(String::class.java)
                                    ?: stChild.child("schoolName").getValue(String::class.java) ?: ""
                                val sEmail = pChild.child("schoolEmail").getValue(String::class.java)
                                    ?: stChild.child("schoolEmail").getValue(String::class.java) ?: ""
                                val isMatch = (sEmail.isNotBlank() && (sEmail.equals(acc.schoolName, ignoreCase = true) || normalizeSyncKey(sEmail) == normAccEmail)) ||
                                              (sName.isNotBlank() && (sName.equals(acc.displayName, ignoreCase = true) || normalizeSyncKey(sName) == normAccName))
                                if (isMatch) {
                                    val amt = pChild.child("amount").getValue(Long::class.java)
                                        ?: pChild.child("amount").getValue(Double::class.java)?.toLong() ?: 0L
                                    if (amt > 0L && processedPayments.add(pid)) {
                                        onlineCount++
                                        onlineTotal += amt
                                    }
                                }
                            }
                        }
                    }
                }

                val finalCount = maxOf(acc.onlinePaymentsCount, onlineCount)
                val finalTotal = maxOf(acc.onlinePaymentsTotal, onlineTotal)

                val settledCount = maxOf(getCommissionSettledCount(acc.schoolName), getCommissionSettledCount(acc.displayName))
                val isSettled = isCommissionSettled(acc.schoolName) || isCommissionSettled(acc.displayName)

                val calculatedComm = if (isSettled && settledCount > 0) {
                    maxOf(0L, (finalCount - settledCount) * 3000L)
                } else if (isSettled && finalCount == 0) {
                    0L
                } else {
                    finalCount * 3000L
                }
                val finalCommission = if (isSettled && calculatedComm == 0L && acc.unpaidCommission == 0L) 0L else maxOf(calculatedComm, acc.unpaidCommission)

                if (finalCount != acc.onlinePaymentsCount || finalTotal != acc.onlinePaymentsTotal || finalCommission != acc.unpaidCommission) {
                    val updated = acc.copy(
                        onlinePaymentsCount = finalCount,
                        onlinePaymentsTotal = finalTotal,
                        unpaidCommission = finalCommission
                    )
                    repository.updateSchoolAccount(updated)
                    hasChanges = true

                    try {
                        firestore.collection("schools").document(acc.schoolName).set(
                            mapOf(
                                "unpaidCommission" to finalCommission,
                                "onlinePaymentsCount" to finalCount,
                                "onlinePaymentsTotal" to finalTotal
                            ),
                            com.google.firebase.firestore.SetOptions.merge()
                        )
                    } catch (eFs: Exception) {
                        android.util.Log.d("ScolaPay", "Firestore commission sync notice: ${eFs.message}")
                    }

                    val sName = if (acc.displayName.isNotBlank()) acc.displayName else acc.schoolName
                    val schoolKey = sName.replace(Regex("[.#$\\\\[\\\\]/]"), "_").trim()
                    try {
                        rtdb.getReference("schools").child(schoolKey).updateChildren(
                            mapOf(
                                "unpaidCommission" to finalCommission,
                                "onlinePaymentsCount" to finalCount,
                                "onlinePaymentsTotal" to finalTotal
                            )
                        )
                    } catch (eR: Exception) {}
                }
            }
            if (hasChanges) {
                loadAdminSchools()
            }
        } catch (e: Exception) {
            android.util.Log.w("ScolaPay", "Audit payments error: ${e.message}")
        }
    }'''

assert target in content, 'Target for auditOnlinePayments not found!'
content = content.replace(target, replacement, 1)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w') as f:
    f.write(content)

print('Audit online payments updated successfully!')
