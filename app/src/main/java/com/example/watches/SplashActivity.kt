package com.example.watches

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                // If user is already logged in, go to MainActivity
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // If not logged in, go to WelcomeActivity
                startActivity(Intent(this, WelcomeActivity::class.java))
            }
            finish()
        }, 3000)
    }
}