package com.example.watches

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ShippingLabelActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var labelContainer: LinearLayout
    private var isGeneratingPdf = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_shipping_label)

            val orderId = intent.getStringExtra("order_id") ?: ""
            if (orderId.isEmpty()) {
                Toast.makeText(this, "Order ID missing", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            labelContainer = findViewById(R.id.labelContainer)
            findViewById<ImageView>(R.id.back_button)?.setOnClickListener { finish() }

            loadOrderDetails(orderId)

            findViewById<MaterialButton>(R.id.btnPrintLabel)?.setOnClickListener {
                if (!isGeneratingPdf) {
                    generatePdfFromView(labelContainer)
                } else {
                    Toast.makeText(this, "Please wait, generating PDF...", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("ShippingLabel", "Error in onCreate: ${e.message}")
            Toast.makeText(this, "Failed to initialize: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadOrderDetails(orderId: String) {
        db.collection("orders").document(orderId).get()
            .addOnSuccessListener { doc ->
                try {
                    if (doc.exists() && !isFinishing) {
                        findViewById<TextView>(R.id.tvOrderId)?.text = "Order ID: \n$orderId"
                        
                        val timestamp = doc.getLong("timestamp") ?: 0
                        val date = Date(timestamp)
                        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                        findViewById<TextView>(R.id.tvOrderDate)?.text = "Order Date: ${sdf.format(date)}"
                        
                        val amount = doc.get("totalAmount")?.toString() ?: "0"
                        findViewById<TextView>(R.id.tvCodAmount)?.text = "₹$amount.00"
                        
                        val userName = doc.getString("userName") ?: doc.getString("name") ?: "Customer"
                        val address = doc.getString("address") ?: "Address not provided"
                        
                        findViewById<TextView>(R.id.tvAddress)?.text = "$userName\n$address"
                    } else if (!doc.exists()) {
                        Toast.makeText(this, "Order not found in database", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("ShippingLabel", "Error parsing order details: ${e.message}")
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Firestore Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun generatePdfFromView(view: View) {
        if (view.width <= 0 || view.height <= 0) {
            Toast.makeText(this, "Layout not ready, please wait", Toast.LENGTH_SHORT).show()
            return
        }

        isGeneratingPdf = true
        Toast.makeText(this, "Generating PDF...", Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed({
            var pdfDocument: PdfDocument? = null
            try {
                val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                view.draw(canvas)

                pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(view.width, view.height, 1).create()
                val page = pdfDocument.startPage(pageInfo)
                
                page.canvas.drawBitmap(bitmap, 0f, 0f, null)
                pdfDocument.finishPage(page)

                val fileName = "ShippingLabel_${System.currentTimeMillis()}.pdf"
                val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

                val outputStream = FileOutputStream(file)
                pdfDocument.writeTo(outputStream)
                outputStream.close()
                
                Toast.makeText(this, "Saved: ${file.name}", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e("ShippingLabel", "Error generating PDF: ${e.message}")
                Toast.makeText(this, "PDF Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                pdfDocument?.close()
                isGeneratingPdf = false
            }
        }, 500)
    }
}