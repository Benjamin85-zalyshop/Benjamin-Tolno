import java.net.URL
import java.net.HttpURLConnection
fun main() {
    val url = URL("https://chapchappay.com/api/ecommerce/order/SUB_123456789")
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "GET"
    // connection.setRequestProperty("CCP-Api-Key", "dummy")
    try {
        println(connection.responseCode)
        println(connection.inputStream.bufferedReader().use { it.readText() })
    } catch(e: Exception) {
        println(connection.errorStream?.bufferedReader()?.use { it.readText() })
    }
}
