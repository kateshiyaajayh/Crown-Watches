package com.example.watches

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso

class OrderTrackingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_tracking)

        val productName = intent.getStringExtra("productName")
        val orderId = intent.getStringExtra("orderId")
        val price = intent.getStringExtra("price")
        val status = intent.getStringExtra("status")
        val image = intent.getStringExtra("image")

        findViewById<TextView>(R.id.tvProductName).text = productName
        findViewById<TextView>(R.id.tvOrderId).text = "Order ID: #$orderId"
        findViewById<TextView>(R.id.tvPrice).text = "₹$price"
        
        val productIV = findViewById<ImageView>(R.id.ivProductImage)
        if (!image.isNullOrEmpty()) {
            Picasso.get().load(image).placeholder(R.drawable.watch).into(productIV)
        }

        updateTrackingUI(status ?: "Confirmed")

        findViewById<ImageView>(R.id.backBtn).setOnClickListener {
            finish()
        }
    }

    private fun updateTrackingUI(status: String) {
        val purple = Color.parseColor("#7C3AED")
        val gray = Color.parseColor("#DDDDDD")
        val black = Color.BLACK
        val lightGray = Color.parseColor("#808080")

        // Default Reset
        resetStep(findViewById(R.id.dot1), findViewById(R.id.status1), findViewById(R.id.desc1), gray)
        resetStep(findViewById(R.id.dot2), findViewById(R.id.status2), findViewById(R.id.desc2), gray)
        resetStep(findViewById(R.id.dot3), findViewById(R.id.status3), findViewById(R.id.desc3), gray)
        resetStep(findViewById(R.id.dot4), findViewById(R.id.status4), findViewById(R.id.desc4), gray)
        
        findViewById<View>(R.id.line1).setBackgroundColor(gray)
        findViewById<View>(R.id.line2).setBackgroundColor(gray)
        findViewById<View>(R.id.line3).setBackgroundColor(gray)

        // Step 1 is always active since order is placed
        activateStep(findViewById(R.id.dot1), findViewById(R.id.status1), findViewById(R.id.desc1), purple)

        when (status) {
            "Confirmed", "Pending" -> {
                // Only step 1
            }
            "Processing" -> {
                findViewById<View>(R.id.line1).setBackgroundColor(purple)
                activateStep(findViewById(R.id.dot2), findViewById(R.id.status2), findViewById(R.id.desc2), purple)
            }
            "Shipped" -> {
                findViewById<View>(R.id.line1).setBackgroundColor(purple)
                findViewById<View>(R.id.line2).setBackgroundColor(purple)
                activateStep(findViewById(R.id.dot2), findViewById(R.id.status2), findViewById(R.id.desc2), purple)
                activateStep(findViewById(R.id.dot3), findViewById(R.id.status3), findViewById(R.id.desc3), purple)
            }
            "Delivered" -> {
                findViewById<View>(R.id.line1).setBackgroundColor(purple)
                findViewById<View>(R.id.line2).setBackgroundColor(purple)
                findViewById<View>(R.id.line3).setBackgroundColor(purple)
                activateStep(findViewById(R.id.dot2), findViewById(R.id.status2), findViewById(R.id.desc2), purple)
                activateStep(findViewById(R.id.dot3), findViewById(R.id.status3), findViewById(R.id.desc3), purple)
                activateStep(findViewById(R.id.dot4), findViewById(R.id.status4), findViewById(R.id.desc4), purple)
            }
        }
    }

    private fun activateStep(dot: View, title: TextView, desc: TextView, color: Int) {
        dot.backgroundTintList = android.content.res.ColorStateList.valueOf(color)
        title.setTextColor(Color.BLACK)
        desc.setTextColor(Color.parseColor("#666666"))
    }

    private fun resetStep(dot: View, title: TextView, desc: TextView, color: Int) {
        dot.backgroundTintList = android.content.res.ColorStateList.valueOf(color)
        title.setTextColor(Color.parseColor("#808080"))
        desc.setTextColor(Color.parseColor("#AAAAAA"))
    }
}
