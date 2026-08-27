import re

with open('app/src/main/java/com/example/utils/ChapChapPayApi.kt', 'r') as f:
    content = f.read()

old_urls = """            // Utilisation de l'URL Firebase Hosting pour le deep linking
            jsonParam.put("success_url", "https://scolapay-b6289.web.app/paiement/return")
            jsonParam.put("return_url", "https://scolapay-b6289.web.app/paiement/return")
            jsonParam.put("cancel_url", "https://scolapay-b6289.web.app/paiement/return")
            
            val options = JSONObject()
            options.put("return_url", "https://scolapay-b6289.web.app/paiement/return")
            jsonParam.put("options", options)"""

new_urls = """            // Utilisation de l'URL Custom Scheme pour garantir le retour dans l'appli ScolaPay
            val returnUrl = "scolapay://paiement/return"
            jsonParam.put("success_url", returnUrl)
            jsonParam.put("return_url", returnUrl)
            jsonParam.put("cancel_url", returnUrl)
            
            val options = JSONObject()
            options.put("return_url", returnUrl)
            jsonParam.put("options", options)"""

content = content.replace(old_urls, new_urls)

with open('app/src/main/java/com/example/utils/ChapChapPayApi.kt', 'w') as f:
    f.write(content)

print("ChapChap URL patched")
