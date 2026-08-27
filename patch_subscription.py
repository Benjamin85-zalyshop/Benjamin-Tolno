import re

with open('app/src/main/java/com/example/ui/screens/SubscriptionScreen.kt', 'r') as f:
    content = f.read()

# 1. Remove selectedPaymentMethod var and replace with constant
content = re.sub(r'var selectedPaymentMethod by remember { mutableStateOf\("CHAP_CHAP"\) }', 'val selectedPaymentMethod = "MOBILE_MONEY"', content)

# 2. Remove TabRow block
tab_row_pattern = re.compile(r'\s*TabRow\(\s*selectedTabIndex = if \(selectedPaymentMethod == "CHAP_CHAP"\) 0 else 1.*?\s*\}\s*Spacer\(modifier = Modifier.height\(20\.dp\)\)', re.DOTALL)
content = tab_row_pattern.sub('\n                Spacer(modifier = Modifier.height(20.dp))', content)

# 3. Remove "if (selectedPaymentMethod == "MOBILE_MONEY") {" and the else block
# Since this is a bit complex with nested braces, let's just make the CHAP_CHAP button disappear.
# Actually, it's easier to just strip the ChapChap blocks.

with open('app/src/main/java/com/example/ui/screens/SubscriptionScreen.kt', 'w') as f:
    f.write(content)
