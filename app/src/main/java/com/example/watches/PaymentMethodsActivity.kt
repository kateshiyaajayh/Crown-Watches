package com.example.watches

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PaymentMethodsActivity : AppCompatActivity() {

    private lateinit var rvPaymentMethods: RecyclerView
    private lateinit var adapter: PaymentMethodAdapter
    private val paymentList = mutableListOf<PaymentMethod>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var emptyState: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_methods)

        initViews()
        setupRecyclerView()
        loadPaymentMethods()

        findViewById<ImageView>(R.id.backBtn).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.btnAddCard).setOnClickListener {
            startActivity(Intent(this, AddPaymentMethodActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadPaymentMethods()
    }

    private fun initViews() {
        rvPaymentMethods = findViewById(R.id.rvPaymentMethods)
        emptyState = findViewById(R.id.emptyState)
    }

    private fun setupRecyclerView() {
        adapter = PaymentMethodAdapter(paymentList) { card ->
            deletePaymentMethod(card)
        }
        rvPaymentMethods.layoutManager = LinearLayoutManager(this)
        rvPaymentMethods.adapter = adapter
    }

    private fun loadPaymentMethods() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("payments")
            .get()
            .addOnSuccessListener { documents ->
                paymentList.clear()
                for (document in documents) {
                    val payment = document.toObject(PaymentMethod::class.java).copy(id = document.id)
                    paymentList.add(payment)
                }
                
                if (paymentList.isEmpty()) {
                    emptyState.visibility = View.VISIBLE
                    rvPaymentMethods.visibility = View.GONE
                } else {
                    emptyState.visibility = View.GONE
                    rvPaymentMethods.visibility = View.VISIBLE
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun deletePaymentMethod(card: PaymentMethod) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("payments").document(card.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Card Removed", Toast.LENGTH_SHORT).show()
                loadPaymentMethods()
            }
    }
}
