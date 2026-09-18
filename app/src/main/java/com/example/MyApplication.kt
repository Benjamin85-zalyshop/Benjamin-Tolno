package com.example

import android.app.Application
import com.google.firebase.FirebaseApp
import android.util.Log

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                Log.d("FirebaseInit", "Firebase initialized successfully in Application class")
            }
        } catch (e: Exception) {
            Log.e("FirebaseInit", "Error initializing Firebase in Application class", e)
        }
    }
}
