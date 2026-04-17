package com.example.watches

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class EditProductActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var productId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_product)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Get references to input fields
        val productNameInput: EditText = findViewById(R.id.edit_product_name_input)
        val productPriceInput: EditText = findViewById(R.id.edit_product_price_input)
        val productCategoryInput: EditText = findViewById(R.id.edit_product_category_input)
        val productImageUrlInput: EditText = findViewById(R.id.edit_product_image_url_input)
        val productDescriptionInput: EditText = findViewById(R.id.edit_product_description_input)
        val updateProductButton: Button = findViewById(R.id.update_product_button)

        // Get product data from Intent extras
        productId = intent.getStringExtra("product_id") ?: ""
        val productName = intent.getStringExtra("product_name") ?: ""
        val productPrice = intent.getStringExtra("product_price") ?: ""
        val productCategory = intent.getStringExtra("product_category") ?: ""
        val productImageUrl = intent.getStringExtra("product_image_url") ?: ""
        val productDescription = intent.getStringExtra("product_description") ?: ""

        // Pre-fill the fields with existing data
        productNameInput.setText(productName)
        productPriceInput.setText(productPrice)
        productCategoryInput.setText(productCategory)
        productImageUrlInput.setText(productImageUrl)
        productDescriptionInput.setText(productDescription)

        // Update Product Button Click Listener
        updateProductButton.setOnClickListener {
            val updatedName = productNameInput.text.toString().trim()
            val updatedPrice = productPriceInput.text.toString().trim()
            val updatedCategory = productCategoryInput.text.toString().trim()
            val updatedImageUrl = productImageUrlInput.text.toString().trim()
            val updatedDescription = productDescriptionInput.text.toString().trim()

            // Validate all fields
            if (updatedName.isEmpty() || updatedPrice.isEmpty() || 
                updatedCategory.isEmpty() || updatedImageUrl.isEmpty() || 
                updatedDescription.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                // Update product in Firebase Firestore
                updateProductInFirestore(updatedName, updatedPrice, updatedCategory, 
                    updatedImageUrl, updatedDescription)
            }
        }
    }

    private fun updateProductInFirestore(
        name: String,
        price: String,
        category: String,
        imageUrl: String,
        description: String
    ) {
        // Create an updated product map
        val updatedProduct = hashMapOf(
            "name" to name,
            "price" to price,
            "category" to category,
            "imageUrl" to imageUrl,
            "description" to description
        )

        // Update product in Firestore
        db.collection("products").document(productId)
            .update(updatedProduct as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(this, "Product Updated Successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }
}
