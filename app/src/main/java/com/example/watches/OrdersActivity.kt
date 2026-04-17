package com.example.watches

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class OrdersActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val orderList = mutableListOf<Order>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private lateinit var tvTotalOrders: TextView
    private var isAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)

        // Check if opened from Admin Dashboard
        isAdmin = intent.getBooleanExtra("isAdmin", false)

        val titleTV = findViewById<TextView>(R.id.orders_title)
        titleTV?.text = if (isAdmin) "Customer Orders" else "My Orders"

        recyclerView = findViewById(R.id.orders_recycler_view)
        tvTotalOrders = findViewById(R.id.tvTotalOrders)
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        // Use different adapter based on isAdmin
        if (isAdmin) {
            val adminAdapter = OrderAdminAdapter(orderList, this)
            recyclerView.adapter = adminAdapter
            loadAllOrdersForAdmin()
        } else {
            val userAdapter = OrderUserAdapter(orderList, this)
            recyclerView.adapter = userAdapter
            loadUserOrders()
        }

        findViewById<ImageView>(R.id.back_button).setOnClickListener {
            finish()
        }
    }

    private fun loadUserOrders() {
        val userId = auth.currentUser?.uid ?: return
        
        db.collection("orders")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                updateList(documents)
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadAllOrdersForAdmin() {
        db.collection("orders")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                updateList(documents)
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateList(documents: com.google.firebase.firestore.QuerySnapshot) {
        orderList.clear()
        for (document in documents) {
            val order = Order(
                id = document.id,
                productName = document.getString("productNames") ?: "Watch Order",
                quantity = document.getLong("totalItems")?.toInt() ?: 1,
                totalPrice = document.get("totalAmount")?.toString() ?: "0",
                status = document.getString("status") ?: "Confirmed",
                orderDate = document.getLong("timestamp") ?: 0,
                productImage = document.getString("productImage") ?: ""
            )
            orderList.add(order)
        }

        tvTotalOrders.text = if (isAdmin) "Total Orders: ${orderList.size}" else "My Total Orders: ${orderList.size}"

        if (orderList.isEmpty()) {
            Toast.makeText(this, "No orders found.", Toast.LENGTH_SHORT).show()
        }

        recyclerView.adapter?.notifyDataSetChanged()
    }
}
