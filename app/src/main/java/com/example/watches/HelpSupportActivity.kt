package com.example.watches

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HelpSupportActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help_support)

        findViewById<ImageView>(R.id.backBtn).setOnClickListener {
            finish()
        }

        // Call Support
        findViewById<android.view.View>(R.id.btnCall).setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:1234567890")
            startActivity(intent)
        }

        // Email Support
        findViewById<android.view.View>(R.id.btnEmail).setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:support@crownwatches.com")
            intent.putExtra(Intent.EXTRA_SUBJECT, "Support Request")
            startActivity(intent)
        }

        // Submit Message to Firestore
        val etMessage = findViewById<EditText>(R.id.etMessage)
        findViewById<MaterialButton>(R.id.btnSendMessage).setOnClickListener {
            val message = etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                submitSupportTicket(message)
            } else {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun submitSupportTicket(message: String) {
        val userId = auth.currentUser?.uid ?: return
        val ticket = hashMapOf(
            "userId" to userId,
            "message" to message,
            "timestamp" to System.currentTimeMillis(),
            "status" to "Open"
        )

        db.collection("support_tickets").add(ticket)
            .addOnSuccessListener {
                Toast.makeText(this, "Message sent! We will contact you soon.", Toast.LENGTH_LONG).show()
                findViewById<EditText>(R.id.etMessage).text.clear()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
            }
    }
}
