import re

# 1. Update app/build.gradle.kts
gradle_path = 'app/build.gradle.kts'
with open(gradle_path, 'r') as f:
    content = f.read()

content = content.replace('isMinifyEnabled = true', 'isMinifyEnabled = false')
content = content.replace('isShrinkResources = true', 'isShrinkResources = false')

# bump version
content = re.sub(r'versionCode = \d+', 'versionCode = 19', content)
content = re.sub(r'versionName = ".*?"', 'versionName = "1.0.18"', content)

with open(gradle_path, 'w') as f:
    f.write(content)
print("Updated build.gradle.kts")

# 2. Update MyApplication.kt with explicit FirebaseOptions fallback
app_path = 'app/src/main/java/com/example/MyApplication.kt'
app_content = """package com.example

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import android.util.Log

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initFirebase()
    }

    private fun initFirebase() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                try {
                    FirebaseApp.initializeApp(this)
                } catch (e: Exception) {
                    Log.w("FirebaseInit", "Standard init failed, trying explicit options", e)
                }
            }

            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:906222981497:android:a45eed22d2b75d5ff0dfa8")
                    .setApiKey("AIzaSyDcVAJyU74j6wx_M4sPTUaUTrpNgijj9X0")
                    .setProjectId("scolapay-b6289")
                    .setStorageBucket("scolapay-b6289.firebasestorage.app")
                    .build()
                FirebaseApp.initializeApp(this, options)
                Log.d("FirebaseInit", "Firebase initialized with explicit options")
            }
        } catch (e: Exception) {
            Log.e("FirebaseInit", "Fatal: Failed to initialize Firebase", e)
        }
    }
}
"""

with open(app_path, 'w') as f:
    f.write(app_content)
print("Updated MyApplication.kt")

