package com.example.watches

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {
    
    private lateinit var firebaseAuth: FirebaseAuth
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        val mobileInput: EditText = findViewById(R.id.mobile_input)
        val otpInput: EditText = findViewById(R.id.otp_input)
        val rememberCheckbox: CheckBox = findViewById(R.id.remember_checkbox)
        val sendButton: Button = findViewById(R.id.send_button)
        val resetButton: Button = findViewById(R.id.reset_button)
        val registerLink: TextView = findViewById(R.id.register_link)
        val googleIcon: ImageView = findViewById(R.id.google_icon)
        val facebookIcon: ImageView = findViewById(R.id.facebook_icon)
        val appleIcon: ImageView = findViewById(R.id.apple_icon)

        // Send OTP button click listener
        sendButton.setOnClickListener {
            val mobile = mobileInput.text.toString().trim()
            if (mobile.isEmpty()) {
                Toast.makeText(this, "Please enter mobile number", Toast.LENGTH_SHORT).show()
            } else if (mobile.length < 10) {
                Toast.makeText(this, "Please enter valid mobile number", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "OTP sent to $mobile", Toast.LENGTH_SHORT).show()
            }
        }

        // Reset button click listener
        resetButton.setOnClickListener {
            val mobile = mobileInput.text.toString().trim()
            val otp = otpInput.text.toString().trim()

            // Validate fields
            if (mobile.isEmpty() || otp.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else if (otp.length < 4) {
                Toast.makeText(this, "Please enter valid OTP", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Password reset successful!", Toast.LENGTH_SHORT).show()
                // Navigate to LoginActivity
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        // Register link click listener
        registerLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Social login icon listeners
        googleIcon.setOnClickListener {
            Toast.makeText(this, "Google login clicked", Toast.LENGTH_SHORT).show()
        }

        facebookIcon.setOnClickListener {
            Toast.makeText(this, "Facebook login clicked", Toast.LENGTH_SHORT).show()
        }

        appleIcon.setOnClickListener {
            Toast.makeText(this, "Apple login clicked", Toast.LENGTH_SHORT).show()
        }
    }
}
