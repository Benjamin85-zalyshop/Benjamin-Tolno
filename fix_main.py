filepath = 'app/src/main/java/com/example/MainActivity.kt'
with open(filepath, 'r') as f:
    content = f.read()

old_block = '''        try {
            if (com.google.firebase.FirebaseApp.getApps(this).isEmpty()) {
                com.google.firebase.FirebaseApp.initializeApp(this)
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseInit", "Error initializing Firebase", e)
        }'''

new_block = '''        try {
            if (com.google.firebase.FirebaseApp.getApps(this).isEmpty()) {
                try {
                    com.google.firebase.FirebaseApp.initializeApp(this)
                } catch (_: Exception) {}
            }
            if (com.google.firebase.FirebaseApp.getApps(this).isEmpty()) {
                val options = com.google.firebase.FirebaseOptions.Builder()
                    .setApplicationId("1:906222981497:android:a45eed22d2b75d5ff0dfa8")
                    .setApiKey("AIzaSyDcVAJyU74j6wx_M4sPTUaUTrpNgijj9X0")
                    .setProjectId("scolapay-b6289")
                    .setStorageBucket("scolapay-b6289.firebasestorage.app")
                    .build()
                com.google.firebase.FirebaseApp.initializeApp(this, options)
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseInit", "Error initializing Firebase", e)
        }'''

if old_block in content:
    content = content.replace(old_block, new_block)
    with open(filepath, 'w') as f:
        f.write(content)
    print("MainActivity patched successfully")
else:
    print("Could not match old block")

