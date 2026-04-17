package com.example.watches

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

class CheckoutActivity : AppCompatActivity(), PaymentResultListener {

    private lateinit var layoutAddress: LinearLayout
    private lateinit var layoutPayment: LinearLayout
    private lateinit var layoutSuccess: LinearLayout
    private lateinit var headerLayout: View
    private lateinit var stepContainer: View

    private lateinit var stepAddress: View
    private lateinit var stepPayment: View
    private lateinit var stepConfirm: View

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var totalAmount: String = "0"
    
    // Address Fields
    private lateinit var etAddress: EditText
    private lateinit var etLocality: EditText
    private lateinit var etState: EditText
    private lateinit var etCity: EditText
    private lateinit var etPincode: EditText

    // Payment Selection
    private lateinit var btnSelectCreditCard: MaterialButton
    private lateinit var btnSelectCOD: MaterialButton
    private lateinit var layoutCreditCardFields: LinearLayout
    private lateinit var layoutCODFields: LinearLayout
    private var selectedPaymentMethod: String = "Online Payment"

    // Razorpay Credentials
    private val RAZORPAY_KEY_ID = "rzp_test_SU88ccKrS8e85b"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        // Preload Razorpay
        Checkout.preload(applicationContext)

        totalAmount = intent.getStringExtra("total_amount") ?: "0"

        initViews()
        showAddressStep()

        findViewById<ImageView>(R.id.backBtn).setOnClickListener {
            onBackPressed()
        }

        findViewById<Button>(R.id.btnSubmitAddress).setOnClickListener {
            if (validateAddress()) {
                showPaymentStep()
            }
        }

        findViewById<Button>(R.id.btnPay).setOnClickListener {
            if (selectedPaymentMethod == "Online Payment") {
                startPayment()
            } else {
                placeOrder("COD_SUCCESS_ID")
            }
        }

        findViewById<Button>(R.id.btnContinue).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        btnSelectCreditCard.setOnClickListener {
            selectPaymentMethod("Online Payment")
        }

        btnSelectCOD.setOnClickListener {
            selectPaymentMethod("COD")
        }
    }

    private fun initViews() {
        layoutAddress = findViewById(R.id.layoutAddress)
        layoutPayment = findViewById(R.id.layoutPayment)
        layoutSuccess = findViewById(R.id.layoutSuccess)
        headerLayout = findViewById(R.id.headerLayout)
        stepContainer = findViewById(R.id.stepContainer)

        stepAddress = findViewById(R.id.stepAddress)
        stepPayment = findViewById(R.id.stepPayment)
        stepConfirm = findViewById(R.id.stepConfirm)

        etAddress = findViewById(R.id.etAddress)
        etLocality = findViewById(R.id.etLocality)
        etState = findViewById(R.id.etState)
        etCity = findViewById(R.id.etCity)
        etPincode = findViewById(R.id.etPincode)

        btnSelectCreditCard = findViewById(R.id.btnSelectCreditCard)
        btnSelectCreditCard.text = "Razorpay"
        btnSelectCOD = findViewById(R.id.btnSelectCOD)
        layoutCreditCardFields = findViewById(R.id.layoutCreditCardFields)
        layoutCODFields = findViewById(R.id.layoutCODFields)
        
        findViewById<TextView>(R.id.tvPaymentAmount).text = "₹$totalAmount"
    }

    private fun selectPaymentMethod(method: String) {
        selectedPaymentMethod = method
        val purpleColor = ContextCompat.getColor(this, R.color.purple_primary)
        
        if (method == "Online Payment") {
            btnSelectCreditCard.backgroundTintList = ColorStateList.valueOf(purpleColor)
            btnSelectCreditCard.setTextColor(Color.WHITE)

            btnSelectCOD.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
            btnSelectCOD.setTextColor(Color.BLACK)

            layoutCreditCardFields.visibility = View.VISIBLE
            layoutCODFields.visibility = View.GONE
        } else {
            btnSelectCOD.backgroundTintList = ColorStateList.valueOf(purpleColor)
            btnSelectCOD.setTextColor(Color.WHITE)

            btnSelectCreditCard.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
            btnSelectCreditCard.setTextColor(Color.BLACK)

            layoutCreditCardFields.visibility = View.GONE
            layoutCODFields.visibility = View.VISIBLE
        }
    }

    private fun showAddressStep() {
        layoutAddress.visibility = View.VISIBLE
        layoutPayment.visibility = View.GONE
        layoutSuccess.visibility = View.GONE
        headerLayout.visibility = View.VISIBLE
        stepContainer.visibility = View.VISIBLE
        
        stepAddress.setBackgroundResource(R.drawable.circle_step_active)
        stepPayment.setBackgroundResource(R.drawable.circle_step_inactive)
        stepConfirm.setBackgroundResource(R.drawable.circle_step_inactive)
    }

    private fun showPaymentStep() {
        layoutAddress.visibility = View.GONE
        layoutPayment.visibility = View.VISIBLE
        layoutSuccess.visibility = View.GONE
        headerLayout.visibility = View.VISIBLE
        stepContainer.visibility = View.VISIBLE
        
        stepPayment.setBackgroundResource(R.drawable.circle_step_active)
    }

    private fun showSuccessStep() {
        layoutAddress.visibility = View.GONE
        layoutPayment.visibility = View.GONE
        layoutSuccess.visibility = View.VISIBLE
        
        headerLayout.visibility = View.GONE
        stepContainer.visibility = View.GONE
    }

    private fun validateAddress(): Boolean {
        if (etAddress.text.toString().isEmpty()) {
            Toast.makeText(this, "Please enter address", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun startPayment() {
        val checkout = Checkout()
        checkout.setKeyID(RAZORPAY_KEY_ID)

        try {
            val options = JSONObject()
            options.put("name", "Watches App")
            options.put("description", "Purchase Payment")
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png")
            options.put("theme.color", "#6200EE")
            options.put("currency", "INR")
            
            // Amount is in paisa (100 paisa = 1 INR)
            val amountInPaisa = (totalAmount.toDouble() * 100).toInt()
            options.put("amount", amountInPaisa.toString())

            val retryObj = JSONObject()
            retryObj.put("enabled", true)
            retryObj.put("max_count", 4)
            options.put("retry", retryObj)

            val prefill = JSONObject()
            prefill.put("email", auth.currentUser?.email ?: "customer@example.com")
            prefill.put("contact", "9999999999")
            options.put("prefill", prefill)

            checkout.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(this, "Error in payment: " + e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onPaymentSuccess(razorpayPaymentID: String?) {
        Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show()
        placeOrder(razorpayPaymentID ?: "SUCCESS")
    }

    override fun onPaymentError(code: Int, response: String?) {
        Toast.makeText(this, "Payment Failed: $response", Toast.LENGTH_LONG).show()
    }

    private fun placeOrder(paymentId: String) {
        val userId = auth.currentUser?.uid ?: return
        
        db.collection("users").document(userId).get().addOnSuccessListener { userDoc ->
            val userName = userDoc.getString("name") ?: "Customer"
            
            db.collection("users").document(userId).collection("cart").get().addOnSuccessListener { cartDocs ->
                val firstProduct = cartDocs.documents.firstOrNull()
                val productName = firstProduct?.getString("name") ?: "Watch Order"
                val productImage = firstProduct?.getString("imageUrl") ?: ""
                val totalItems = cartDocs.size()

                val orderData = hashMapOf(
                    "userId" to userId,
                    "userName" to userName,
                    "productNames" to productName,
                    "productImage" to productImage,
                    "totalItems" to totalItems,
                    "address" to etAddress.text.toString(),
                    "totalAmount" to totalAmount,
                    "paymentMethod" to selectedPaymentMethod,
                    "paymentId" to paymentId,
                    "status" to "Confirmed",
                    "timestamp" to System.currentTimeMillis()
                )

                db.collection("orders").add(orderData)
                    .addOnSuccessListener {
                        clearCart(userId)
                        showSuccessStep()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to place order", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun clearCart(userId: String) {
        db.collection("users").document(userId).collection("cart")
            .get()
            .addOnSuccessListener { documents ->
                val batch = db.batch()
                for (doc in documents) {
                    batch.delete(doc.reference)
                }
                batch.commit()
            }
    }
}
