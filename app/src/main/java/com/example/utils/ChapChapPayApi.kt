package com.example.utils

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
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
        // Direct boolean flags
        if (jsonObject.optBoolean("paid", false) || jsonObject.optBoolean("is_paid", false) || jsonObject.optBoolean("success", false)) {
            return "SUCCESS"
        }

        val successCodes = setOf(
            "completed", "successful", "success", "approved", "paid",
            "done", "valid", "valide", "validé", "validee",
            "effectue", "effectué", "effectuée", "settled", "accepted",
            "confirmed", "1", "ok", "true", "paye", "payé", "payee", "payée"
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

        var code = ""
        var statusMethod: String? = null
        if (jsonObject.has("status")) {
            val status = jsonObject.get("status")
            if (status is JSONObject) {
                code = status.optString("code", "").lowercase()
                statusMethod = status.optString("payment_method", "").takeIf { it.isNotBlank() && it != "null" }
            } else {
                code = status.toString().lowercase()
            }
        } else if (jsonObject.has("code")) {
            code = jsonObject.optString("code", "").lowercase()
        }

        val paymentStatus = jsonObject.optString("payment_status", "").lowercase()
        val state = jsonObject.optString("state", "").lowercase()
        val result = jsonObject.optString("result", "").lowercase()

        for (candidate in listOf(code, paymentStatus, state, result)) {
            if (candidate in successCodes) return "SUCCESS"
            if (candidate in failedCodes) return "FAILED"
        }

        // Si payment_method est renseigné et que ce n'est pas failed
        if (statusMethod != null && code !in failedCodes && code != "new") {
            return "SUCCESS"
        }

        // Vérification de la section transaction
        if (jsonObject.has("transaction") && !jsonObject.isNull("transaction")) {
            val txObj = jsonObject.opt("transaction")
            if (txObj is JSONObject) {
                val txStatus = txObj.optString("status", txObj.optString("state", "")).lowercase()
                if (txStatus in successCodes) {
                    return "SUCCESS"
                }
                val txId = txObj.optString("transaction_id", txObj.optString("id", ""))
                if (txId.isNotBlank() && code !in failedCodes && txStatus !in failedCodes) {
                    return "SUCCESS"
                }
            } else if (txObj is JSONArray && txObj.length() > 0) {
                return "SUCCESS"
            } else if (txObj is String && txObj.isNotBlank() && code !in failedCodes) {
                return "SUCCESS"
            }
        }

        if (jsonObject.has("transactions") && !jsonObject.isNull("transactions")) {
            val txArr = jsonObject.optJSONArray("transactions")
            if (txArr != null && txArr.length() > 0) {
                return "SUCCESS"
            }
        }

        return when {
            code in successCodes -> "SUCCESS"
            code in failedCodes -> "FAILED"
            code in pendingCodes -> "PENDING"
            else -> "PENDING"
        }
    }

    suspend fun checkOrderStatus(identifier: String): String = withContext(Dispatchers.IO) {
        val cleanId = identifier.trim()
        if (cleanId.isBlank()) return@withContext "FAILED"

        val endpoints = mutableListOf<String>()
        if (cleanId.startsWith("COMM_") || cleanId.startsWith("SUB_") || cleanId.startsWith("PAY_")) {
            endpoints.add("https://chapchappay.com/api/ecommerce/order/$cleanId")
            endpoints.add("https://chapchappay.com/api/ecommerce/$cleanId")
            val cleanNumeric = cleanId.substringAfter("_")
            if (cleanNumeric.isNotBlank() && cleanNumeric != cleanId) {
                endpoints.add("https://chapchappay.com/api/ecommerce/order/$cleanNumeric")
            }
        } else {
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
