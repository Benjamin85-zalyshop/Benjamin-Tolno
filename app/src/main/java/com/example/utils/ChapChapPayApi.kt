package com.example.utils

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class ChapChapPaymentResult(
    val paymentUrl: String,
    val operationId: String?,
    val orderId: String?
)

object ChapChapPayApi {

    suspend fun createPayment(amount: Double, description: String = "Abonnement ScolaPay", orderId: String? = null): ChapChapPaymentResult? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://chapchappay.com/api/ecommerce/create")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("CCP-Api-Key", BuildConfig.CHAP_CHAP_LIVE_API_KEY)
            connection.doOutput = true
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            val jsonParam = JSONObject()
            jsonParam.put("amount", amount)
            jsonParam.put("description", description)
            if (orderId != null) {
                jsonParam.put("order_id", orderId)
            }
            
            val returnUrl = "https://scolapay-b6289.web.app/paiement/return"
            jsonParam.put("success_url", returnUrl)
            jsonParam.put("return_url", returnUrl)
            jsonParam.put("cancel_url", returnUrl)
            
            val options = JSONObject()
            options.put("return_url", returnUrl)
            jsonParam.put("options", options)

            val outputStreamWriter = OutputStreamWriter(connection.outputStream)
            outputStreamWriter.write(jsonParam.toString())
            outputStreamWriter.flush()

            if (connection.responseCode in 200..299) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(response)
                val paymentUrl = if (jsonObject.has("payment_url")) jsonObject.getString("payment_url") else null
                val opId = if (jsonObject.has("operation_id")) jsonObject.getString("operation_id") else null
                val ordId = if (jsonObject.has("order_id")) jsonObject.getString("order_id") else orderId
                if (paymentUrl != null) {
                    return@withContext ChapChapPaymentResult(paymentUrl, opId, ordId)
                }
            } else {
                val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                android.util.Log.e("ChapChapPayApi", "Error ${connection.responseCode}: $errorResponse")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    suspend fun createPaymentOperation(amount: Double, description: String = "Abonnement ScolaPay", orderId: String? = null): String? {
        val result = createPayment(amount, description, orderId)
        return result?.paymentUrl
    }

    private suspend fun queryChapChapEndpoint(urlStr: String): JSONObject? = withContext(Dispatchers.IO) {
        try {
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("CCP-Api-Key", BuildConfig.CHAP_CHAP_LIVE_API_KEY)
            connection.connectTimeout = 12000
            connection.readTimeout = 12000

            if (connection.responseCode in 200..299) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                return@withContext JSONObject(response)
            } else {
                android.util.Log.w("ChapChapPayApi", "HTTP ${connection.responseCode} for $urlStr")
            }
        } catch (e: Exception) {
            android.util.Log.w("ChapChapPayApi", "Error querying $urlStr: ${e.message}")
        }
        return@withContext null
    }

    private fun extractStatusCode(jsonObject: JSONObject): String {
        var code = ""
        if (jsonObject.has("status")) {
            val status = jsonObject.get("status")
            if (status is JSONObject) {
                code = status.optString("code", "").lowercase()
            } else {
                code = status.toString().lowercase()
            }
        } else if (jsonObject.has("code")) {
            code = jsonObject.optString("code", "").lowercase()
        }

        // Si une transaction valide existe
        if (jsonObject.has("transaction") && !jsonObject.isNull("transaction")) {
            val tx = jsonObject.optJSONObject("transaction")
            if (tx != null) {
                val txStatus = tx.optString("status", "").lowercase()
                if (txStatus in listOf("success", "successful", "completed", "approved", "paid")) {
                    return "SUCCESS"
                }
                if (tx.has("transaction_id") || tx.has("id")) {
                    val txId = tx.optString("transaction_id", tx.optString("id", ""))
                    if (txId.isNotBlank() && code !in listOf("failed", "cancelled", "canceled", "rejected", "expired")) {
                        return "SUCCESS"
                    }
                }
            }
        }

        val successCodes = setOf(
            "completed", "successful", "success", "approved", "paid",
            "done", "valid", "valide", "validé", "validee",
            "effectue", "effectué", "effectuée", "settled", "accepted",
            "confirmed", "1", "ok", "true"
        )
        val pendingCodes = setOf(
            "new", "pending", "processing", "in_progress", "created",
            "waiting", "en_attente", "en_cours", "initiated",
            "unpaid", "open", "ready", ""
        )
        val failedCodes = setOf(
            "failed", "cancelled", "canceled", "expired", "rejected",
            "refused", "annulé", "annule", "echoue", "échoué", "0"
        )

        return when {
            code in successCodes -> "SUCCESS"
            code in failedCodes -> "FAILED"
            code in pendingCodes -> "PENDING"
            else -> "PENDING" // En cas de doute, ne pas déclarer en échec prématurément
        }
    }

    suspend fun checkOrderStatus(identifier: String): String = withContext(Dispatchers.IO) {
        val cleanId = identifier.trim()
        if (cleanId.isBlank()) return@withContext "FAILED"

        // 1. Si l'identifiant ressemble à un identifiant de commande (ex: COMM_..., SUB_..., PAY_...)
        val endpoints = mutableListOf<String>()
        if (cleanId.startsWith("COMM_") || cleanId.startsWith("SUB_") || cleanId.startsWith("PAY_")) {
            endpoints.add("https://chapchappay.com/api/ecommerce/order/$cleanId")
            endpoints.add("https://chapchappay.com/api/ecommerce/$cleanId")
        } else {
            // Probablement un operation_id (UUID ou référence ChapChapPay)
            endpoints.add("https://chapchappay.com/api/ecommerce/$cleanId")
            endpoints.add("https://chapchappay.com/api/ecommerce/order/$cleanId")
        }

        for (ep in endpoints) {
            val json = queryChapChapEndpoint(ep)
            if (json != null) {
                val evaluated = extractStatusCode(json)
                android.util.Log.d("ChapChapPayApi", "Checked $ep -> evaluated status: $evaluated")
                return@withContext evaluated
            }
        }

        // Si non trouvé ou erreur de connexion temporaire, garder PENDING pour ne pas supprimer la commande
        return@withContext "PENDING"
    }

    suspend fun checkMultipleIds(vararg ids: String?): String {
        var hasPending = false
        for (id in ids) {
            val clean = id?.trim() ?: continue
            if (clean.isBlank()) continue
            val res = checkOrderStatus(clean)
            if (res == "SUCCESS") return "SUCCESS"
            if (res == "PENDING") hasPending = true
        }
        return if (hasPending) "PENDING" else "FAILED"
    }
}
