package com.example.watches

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class AdminLoginActivity : AppCompatActivity() {
    
    // Admin credentials
    private val ADMIN_EMAIL = "akateshiya799@rku.ac.in"
    private val ADMIN_PASSWORD = "24251234"
    private lateinit var auth: FirebaseAuth
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_login)

        auth = FirebaseAuth.getInstance()

        val emailInput: EditText = findViewById(R.id.admin_email_input)
        val passwordInput: EditText = findViewById(R.id.admin_password_input)
        val loginButton: Button = findViewById(R.id.admin_login_button)

        // Login button click listener
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            // Validate fields
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else if (email == ADMIN_EMAIL && password == ADMIN_PASSWORD) {
                // To avoid PERMISSION_DENIED, we should sign in with Firebase
                // If this admin user is already in Firebase Auth, we sign in.
                // If not, for testing, we can use signInWithEmailAndPassword.
                // But a better way to fix PERMISSION_DENIED is to make sure the app is "authenticated"
                
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Admin Login Successful!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, AdminDashboardActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // If user doesn't exist in Firebase Auth, but matches hardcoded credentials,
                            // we might need to create it or just show error.
                            // For now, let's assume the admin should be in Firebase.
                            Toast.makeText(this, "Firebase Auth failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Invalid Admin Login", Toast.LENGTH_SHORT).show()
            }
        }
    }
}