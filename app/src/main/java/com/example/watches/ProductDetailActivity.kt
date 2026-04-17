package com.example.watches

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class ProductDetailActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var isWishlisted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        // Get product data from intent
        val productId = intent.getStringExtra("product_id") ?: ""
        val productName = intent.getStringExtra("product_name") ?: ""
        val productPrice = intent.getStringExtra("product_price") ?: ""
        val productImageUrl = intent.getStringExtra("product_image_url") ?: ""
        val productCategory = intent.getStringExtra("product_category") ?: ""
        val productDescription = intent.getStringExtra("product_description") ?: ""

        // Initialize UI elements
        val productImage: ImageView = findViewById(R.id.product_image)
        val nameTextView: TextView = findViewById(R.id.product_name)
        val priceTextView: TextView = findViewById(R.id.product_price)
        val descriptionTextView: TextView = findViewById(R.id.product_description)
        val addToCartButton: MaterialButton = findViewById(R.id.add_to_cart_button)
        val backButton: ImageView = findViewById(R.id.back_button)
        val wishlistBtn: ImageView = findViewById(R.id.btnFavorite)

        // Clear image initially
        productImage.setImageDrawable(null)

        // Set product data
        nameTextView.text = productName
        priceTextView.text = "₹$productPrice"
        descriptionTextView.text = if (productDescription.isEmpty()) {
            "Premium quality watch with modern design and durable straps. Perfect for any occasion."
        } else {
            productDescription
        }

        // Load product image
        if (productImageUrl.isNotEmpty()) {
            Picasso.get()
                .load(productImageUrl)
                .placeholder(android.R.color.transparent)
                .error(R.drawable.watch)
                .noFade()
                .into(productImage)
        } else {
            productImage.setImageResource(R.drawable.watch)
        }

        // Check wishlist state
        checkIfWishlisted(productId, wishlistBtn)

        // Back button click listener
        backButton.setOnClickListener {
            finish()
        }

        // Wishlist button click listener
        wishlistBtn.setOnClickListener {
            val product = Product(
                id = productId,
                name = productName,
                price = productPrice,
                imageUrl = productImageUrl,
                category = productCategory,
                description = productDescription
            )
            toggleWishlist(product, wishlistBtn)
        }

        // Add to cart button click listener
        addToCartButton.setOnClickListener {
            addToCart(productId, productName, productPrice, productImageUrl, productCategory)
        }
    }

    private fun checkIfWishlisted(productId: String, wishlistBtn: ImageView) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("wishlist").document(productId)
            .get()
            .addOnSuccessListener { doc ->
                isWishlisted = doc.exists()
                updateWishlistIcon(wishlistBtn, isWishlisted)
            }
    }

    private fun toggleWishlist(product: Product, wishlistBtn: ImageView) {
        val userId = auth.currentUser?.uid ?: return
        val wishlistRef = db.collection("users").document(userId).collection("wishlist").document(product.id)

        if (isWishlisted) {
            wishlistRef.delete().addOnSuccessListener {
                isWishlisted = false
                updateWishlistIcon(wishlistBtn, false)
                Toast.makeText(this, "Removed from Wishlist", Toast.LENGTH_SHORT).show()
            }
        } else {
            wishlistRef.set(product).addOnSuccessListener {
                isWishlisted = true
                updateWishlistIcon(wishlistBtn, true)
                Toast.makeText(this, "Added to Wishlist", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateWishlistIcon(imageView: ImageView, isWishlisted: Boolean) {
        if (isWishlisted) {
            imageView.setImageResource(R.drawable.ic_wishlist_filled)
        } else {
            imageView.setImageResource(R.drawable.ic_wishlist_border)
        }
    }

    private fun addToCart(id: String, name: String, price: String, imageUrl: String, category: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        val cartItem = hashMapOf(
            "name" to name,
            "price" to price,
            "imageUrl" to imageUrl,
            "category" to category,
            "quantity" to 1
        )

        db.collection("users").document(userId).collection("cart").document(id)
            .set(cartItem)
            .addOnSuccessListener {
                Toast.makeText(this, "$name added to cart", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
