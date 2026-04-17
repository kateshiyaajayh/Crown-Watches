package com.example.watches

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class AdminDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        // Get references to all cards
        val addProductCard: CardView = findViewById(R.id.add_product_card)
        val manageProductsCard: CardView = findViewById(R.id.manage_products_card)
        val ordersCard: CardView = findViewById(R.id.orders_card)
        val analyticsCard: CardView = findViewById(R.id.analytics_card)
        val barcodeScannerCard: CardView = findViewById(R.id.barcode_scanner_card)
        val logoutCard: CardView = findViewById(R.id.logout_card)

        // Add Product Card Click Listener
        addProductCard.setOnClickListener {
            val intent = Intent(this, AddProductActivity::class.java)
            startActivity(intent)
        }

        // Manage Products Card Click Listener
        manageProductsCard.setOnClickListener {
            val intent = Intent(this, ManageProductsActivity::class.java)
            startActivity(intent)
        }

        // Orders Card Click Listener -> Pass isAdmin = true
        ordersCard.setOnClickListener {
            val intent = Intent(this, OrdersActivity::class.java)
            intent.putExtra("isAdmin", true)
            startActivity(intent)
        }

        // Analytics Card Click Listener
        analyticsCard.setOnClickListener {
            val intent = Intent(this, AnalyticsActivity::class.java)
            startActivity(intent)
        }

        // Barcode Scanner Card Click Listener
        barcodeScannerCard.setOnClickListener {
            val intent = Intent(this, BarcodeScannerActivity::class.java)
            startActivity(intent)
        }

        // Logout Card Click Listener
        logoutCard.setOnClickListener {
            val intent = Intent(this, AdminLoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
