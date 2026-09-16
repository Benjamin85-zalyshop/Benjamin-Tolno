import re

filepath = 'app/src/main/java/com/example/MainActivity.kt'
with open(filepath, 'r') as f:
    content = f.read()

find_text = '''        super.onCreate(savedInstanceState)
        
        // Add security flag to prevent screen capture
        window.setFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE, android.view.WindowManager.LayoutParams.FLAG_SECURE)'''

replace_text = '''        super.onCreate(savedInstanceState)
        
        // Add security flag to prevent screen capture
        window.setFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE, android.view.WindowManager.LayoutParams.FLAG_SECURE)
        
        try {
            if (com.google.firebase.FirebaseApp.getApps(this).isEmpty()) {
                com.google.firebase.FirebaseApp.initializeApp(this)
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseInit", "Error initializing Firebase", e)
        }'''

content = content.replace(find_text, replace_text)

with open(filepath, 'w') as f:
    f.write(content)

