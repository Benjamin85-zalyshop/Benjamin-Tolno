package com.example.utils

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object ChapChapPayApi {
    suspend fun createPaymentOperation(amount: Double, description: String = "Abonnement ScolaPay", orderId: String? = null): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://chapchappay.com/api/ecommerce/create")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("CCP-Api-Key", BuildConfig.CHAP_CHAP_TEST_API_KEY)
            connection.doOutput = true

            val jsonParam = JSONObject()
            jsonParam.put("amount", amount)
            jsonParam.put("description", description)
            if (orderId != null) {
                jsonParam.put("order_id", orderId)
            }
            
            // Utilisation de l'URL Firebase Hosting pour le deep linking
            jsonParam.put("success_url", "https://scolapay-b6289.web.app/paiement/return")
            jsonParam.put("return_url", "https://scolapay-b6289.web.app/paiement/return")
            jsonParam.put("cancel_url", "https://scolapay-b6289.web.app/paiement/return")
            
            val options = JSONObject()
            options.put("return_url", "https://scolapay-b6289.web.app/paiement/return")
            jsonParam.put("options", options)

            val outputStreamWriter = OutputStreamWriter(connection.outputStream)
            outputStreamWriter.write(jsonParam.toString())
            outputStreamWriter.flush()

            if (connection.responseCode in 200..299) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(response)
                if (jsonObject.has("payment_url")) {
                    return@withContext jsonObject.getString("payment_url")
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

    suspend fun checkOrderStatus(orderId: String): String = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://chapchappay.com/api/ecommerce/order/$orderId")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("CCP-Api-Key", BuildConfig.CHAP_CHAP_TEST_API_KEY)

            if (connection.responseCode in 200..299) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(response)
                if (jsonObject.has("status")) {
                    val statusObj = jsonObject.getJSONObject("status")
                    val code = statusObj.optString("code")
                    if (code == "completed" || code == "successful" || code == "success" || code == "approved" || code == "paid") {
                        return@withContext "SUCCESS"
                    } else if (code == "failed" || code == "cancelled" || code == "canceled" || code == "expired" || code == "rejected") {
                        return@withContext "FAILED"
                    } else {
                        return@withContext "PENDING"
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext "PENDING"
    }
}
