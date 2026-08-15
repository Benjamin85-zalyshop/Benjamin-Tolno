import re

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r') as f:
    code = f.read()

new_item = """data class SchoolAdminItem(
    val email: String,
    val displayName: String = "",
    val schoolName: String = "",
    val founderPhone: String = "",
    val address: String = "",
    val isPendingValidation: Boolean = false,
    val hasActiveSubscription: Boolean = false,
    val subscriptionExpiryDate: Long? = null,
    val createdAt: Long = 0L,
    val paymentPhoneNumber: String? = null,
    val transactionId: String? = null,
    val rejectionReason: String? = null
)"""

code = re.sub(r'data class SchoolAdminItem\(.*?\)', new_item, code)

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w') as f:
    f.write(code)

