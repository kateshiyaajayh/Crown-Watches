package com.example.watches

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddAddressActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_address)

        findViewById<ImageView>(R.id.backBtn).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.btnSaveAddress).setOnClickListener {
            saveAddress()
        }
    }

    private fun saveAddress() {
        val userId = auth.currentUser?.uid ?: return
        val fullName = findViewById<EditText>(R.id.etFullName).text.toString().trim()
        val mobile = findViewById<EditText>(R.id.etMobile).text.toString().trim()
        val addressLine = findViewById<EditText>(R.id.etAddressLine).text.toString().trim()
        val city = findViewById<EditText>(R.id.etCity).text.toString().trim()
        val state = findViewById<EditText>(R.id.etState).text.toString().trim()
        val pincode = findViewById<EditText>(R.id.etPincode).text.toString().trim()
        
        val rgType = findViewById<RadioGroup>(R.id.rgAddressType)
        val addressType = if (rgType.checkedRadioButtonId == R.id.rbHome) "Home" else "Office"
        val isDefault = findViewById<CheckBox>(R.id.cbDefault).isChecked

        if (fullName.isEmpty() || mobile.isEmpty() || addressLine.isEmpty() || city.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val address = Address(
            fullName = fullName,
            mobileNumber = mobile,
            addressLine = addressLine,
            city = city,
            state = state,
            pincode = pincode,
            addressType = addressType,
            isDefault = isDefault
        )

        db.collection("users").document(userId).collection("addresses").add(address)
            .addOnSuccessListener {
                Toast.makeText(this, "Address Saved Successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save address", Toast.LENGTH_SHORT).show()
            }
    }
}
