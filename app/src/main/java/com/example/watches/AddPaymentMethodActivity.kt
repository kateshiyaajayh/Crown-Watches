package com.example.watches

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddPaymentMethodActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var etCardHolder: EditText
    private lateinit var etCardNumber: EditText
    private lateinit var etExpiry: EditText
    private lateinit var previewNumber: TextView
    private lateinit var previewHolder: TextView
    private lateinit var previewExpiry: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_payment_method)

        initViews()

        findViewById<ImageView>(R.id.backBtn).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.btnSaveCard).setOnClickListener {
            saveCard()
        }

        setupPreview()
    }

    private fun initViews() {
        etCardHolder = findViewById(R.id.etCardHolder)
        etCardNumber = findViewById(R.id.etCardNumber)
        etExpiry = findViewById(R.id.etExpiry)
        previewNumber = findViewById(R.id.previewCardNumber)
        previewHolder = findViewById(R.id.previewHolderName)
        previewExpiry = findViewById(R.id.previewExpiry)
    }

    private fun setupPreview() {
        etCardNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val num = s.toString()
                if (num.isEmpty()) {
                    previewNumber.text = "**** **** **** ****"
                } else {
                    val formatted = num.chunked(4).joinToString(" ")
                    previewNumber.text = formatted
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        etCardHolder.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                previewHolder.text = if (s.isNullOrEmpty()) "CARD HOLDER" else s.toString().uppercase()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        etExpiry.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                previewExpiry.text = if (s.isNullOrEmpty()) "MM/YY" else s.toString()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun saveCard() {
        val userId = auth.currentUser?.uid ?: return
        val holderName = etCardHolder.text.toString().trim()
        val cardNumber = etCardNumber.text.toString().trim()
        val expiry = etExpiry.text.toString().trim()

        if (holderName.isEmpty() || cardNumber.length < 16 || expiry.isEmpty()) {
            Toast.makeText(this, "Please enter valid card details", Toast.LENGTH_SHORT).show()
            return
        }

        val card = PaymentMethod(
            cardHolderName = holderName,
            cardNumber = "**** **** **** " + cardNumber.takeLast(4),
            expiryDate = expiry,
            cardType = "Visa"
        )

        db.collection("users").document(userId).collection("payments").add(card)
            .addOnSuccessListener {
                Toast.makeText(this, "Card Saved Successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
    }
}
