package com.example.watches

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CartActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CartAdapter
    private val cartItems = mutableListOf<Product>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var tvItemsCount: TextView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvDiscount: TextView
    private lateinit var tvDelivery: TextView
    private lateinit var tvTotal: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cart)

        // Handle Window Insets
        val mainView = findViewById<View>(R.id.headerLayout).parent as View
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupRecyclerView()
        loadCartFromFirestore()

        findViewById<ImageView>(R.id.backBtn).setOnClickListener {
            finish()
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCheckOut).setOnClickListener {
            if (cartItems.isNotEmpty()) {
                val intent = Intent(this, CheckoutActivity::class.java)
                intent.putExtra("total_amount", tvTotal.text.toString().replace("₹", "").replace("$", ""))
                startActivity(intent)
            } else {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.cartRecyclerView)
        tvItemsCount = findViewById(R.id.tvItemsCount)
        tvSubtotal = findViewById(R.id.tvSubtotal)
        tvDiscount = findViewById(R.id.tvDiscount)
        tvDelivery = findViewById(R.id.tvDelivery)
        tvTotal = findViewById(R.id.tvTotal)
    }

    private fun setupRecyclerView() {
        adapter = CartAdapter(cartItems) {
            updateSummary()
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadCartFromFirestore() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users").document(userId).collection("cart")
            .get()
            .addOnSuccessListener { documents ->
                cartItems.clear()
                for (document in documents) {
                    val product = Product(
                        id = document.id,
                        name = document.getString("name") ?: "",
                        price = document.getString("price") ?: "0",
                        category = document.getString("category") ?: "",
                        imageUrl = document.getString("imageUrl") ?: "",
                        description = "",
                        quantity = document.getLong("quantity")?.toInt() ?: 1
                    )
                    cartItems.add(product)
                }
                adapter.notifyDataSetChanged()
                updateSummary()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateSummary() {
        var subtotal = 0.0
        var count = 0
        for (product in cartItems) {
            val price = product.price.toDoubleOrNull() ?: 0.0
            subtotal += price * product.quantity
            count += product.quantity
        }

        val discount = 4.0
        val delivery = 2.0
        val total = if (subtotal > 0) subtotal - discount + delivery else 0.0

        tvItemsCount.text = count.toString()
        tvSubtotal.text = "₹${subtotal.toInt()}"
        tvDiscount.text = "₹${discount.toInt()}"
        tvDelivery.text = "₹${delivery.toInt()}"
        tvTotal.text = "₹${total.toInt()}"
    }
}
