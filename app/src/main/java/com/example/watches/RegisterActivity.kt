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

class RegisterActivity : AppCompatActivity() {
    
    private lateinit var firebaseAuth: FirebaseAuth
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        val fullNameInput: EditText = findViewById(R.id.full_name_input)
        val emailInput: EditText = findViewById(R.id.email_input)
        val mobileInput: EditText = findViewById(R.id.mobile_input)
        val passwordInput: EditText = findViewById(R.id.password_input)
        val privacyCheckbox: CheckBox = findViewById(R.id.privacy_checkbox)
        val registerButton: Button = findViewById(R.id.register_button)
        val loginLink: TextView = findViewById(R.id.login_link)
        val googleIcon: ImageView = findViewById(R.id.google_icon)
        val facebookIcon: ImageView = findViewById(R.id.facebook_icon)
        val appleIcon: ImageView = findViewById(R.id.apple_icon)

        // Register button click listener
        registerButton.setOnClickListener {
            val fullName = fullNameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val mobile = mobileInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            // Validate all fields
            if (fullName.isEmpty() || email.isEmpty() || mobile.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else if (!privacyCheckbox.isChecked) {
                Toast.makeText(this, "Please agree to Privacy Policy", Toast.LENGTH_SHORT).show()
            } else if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            } else {
                // Create user with Firebase Authentication
                registerUserWithFirebase(email, password)
            }
        }

        // Login link click listener
        loginLink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Social login icon listeners (optional)
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

    // Function to register user with Firebase
    private fun registerUserWithFirebase(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registration successful
                    Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
                    
                    // Navigate to LoginActivity
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Registration failed
                    val errorMessage = task.exception?.message ?: "Registration failed"
                    Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }
    }
}
