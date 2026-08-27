fun main() {
    val expiredDate = System.currentTimeMillis() - 100L * 24 * 60 * 60 * 1000 // 100 days ago
    val elapsed = System.currentTimeMillis() - expiredDate
    val trialDuration = 90L * 24L * 60L * 60L * 1000L
    println("expiredDate: $expiredDate")
    println("elapsed: $elapsed")
    println("trialDuration: $trialDuration")
    println("trialActive: ${elapsed < trialDuration}")
}
