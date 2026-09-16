import re

filepath = 'app/src/main/java/com/example/MainActivity.kt'
with open(filepath, 'r') as f:
    content = f.read()

find_text = '''        try {
            com.google.firebase.FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            android.util.Log.e("FirebaseInit", "Error initializing Firebase", e)
        }'''

if find_text in content:
    print("Firebase init logic is present.")
else:
    print("Firebase init logic is MISSING.")

