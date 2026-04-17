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

class ShippingAddressActivity : AppCompatActivity() {

    private lateinit var rvAddresses: RecyclerView
    private lateinit var adapter: AddressAdapter
    private val addressList = mutableListOf<Address>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var emptyState: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shipping_address)

        initViews()
        setupRecyclerView()
        loadAddresses()

        findViewById<ImageView>(R.id.backBtn).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.btnAddAddress).setOnClickListener {
            startActivity(Intent(this, AddAddressActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadAddresses()
    }

    private fun initViews() {
        rvAddresses = findViewById(R.id.rvAddresses)
        emptyState = findViewById(R.id.emptyState)
    }

    private fun setupRecyclerView() {
        adapter = AddressAdapter(addressList) { address ->
            deleteAddress(address)
        }
        rvAddresses.layoutManager = LinearLayoutManager(this)
        rvAddresses.adapter = adapter
    }

    private fun loadAddresses() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("addresses")
            .get()
            .addOnSuccessListener { documents ->
                addressList.clear()
                for (document in documents) {
                    val address = document.toObject(Address::class.java).copy(id = document.id)
                    addressList.add(address)
                }
                
                if (addressList.isEmpty()) {
                    emptyState.visibility = View.VISIBLE
                    rvAddresses.visibility = View.GONE
                } else {
                    emptyState.visibility = View.GONE
                    rvAddresses.visibility = View.VISIBLE
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun deleteAddress(address: Address) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("addresses").document(address.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Address Deleted", Toast.LENGTH_SHORT).show()
                loadAddresses()
            }
    }
}
