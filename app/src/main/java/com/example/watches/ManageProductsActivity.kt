package com.example.watches

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class ManageProductsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdminAdapter
    private val fullProductList = mutableListOf<Product>()
    private val filteredProductList = mutableListOf<Product>()
    private val db = FirebaseFirestore.getInstance()
    
    private lateinit var tvTotalProducts: TextView
    private lateinit var searchProductET: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_products)

        initViews()
        setupRecyclerView()
        setupSearch()

        // Back button
        findViewById<ImageView>(R.id.back_button).setOnClickListener {
            finish()
        }

        // Load products from Firestore
        loadProducts()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.products_recycler_view)
        tvTotalProducts = findViewById(R.id.tvTotalProducts)
        searchProductET = findViewById(R.id.searchProductET)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        productAdapter = ProductAdminAdapter(filteredProductList, this)
        recyclerView.adapter = productAdapter
    }

    private fun setupSearch() {
        searchProductET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterProducts(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterProducts(query: String) {
        filteredProductList.clear()
        if (query.isEmpty()) {
            filteredProductList.addAll(fullProductList)
        } else {
            val lowerCaseQuery = query.lowercase(Locale.ROOT)
            for (product in fullProductList) {
                if (product.name.lowercase(Locale.ROOT).contains(lowerCaseQuery) ||
                    product.category.lowercase(Locale.ROOT).contains(lowerCaseQuery)) {
                    filteredProductList.add(product)
                }
            }
        }
        productAdapter.notifyDataSetChanged()
    }

    private fun loadProducts() {
        db.collection("products")
            .get()
            .addOnSuccessListener { documents ->
                fullProductList.clear()
                
                for (document in documents) {
                    val product = Product(
                        id = document.id,
                        name = document.getString("name") ?: "",
                        price = document.get("price")?.toString() ?: "0",
                        category = document.getString("category") ?: "",
                        imageUrl = document.getString("imageUrl") ?: "",
                        description = document.getString("description") ?: ""
                    )
                    fullProductList.add(product)
                }

                tvTotalProducts.text = "Total: ${fullProductList.size}"
                
                // Initially show all products
                filterProducts(searchProductET.text.toString())

                if (fullProductList.isEmpty()) {
                    Toast.makeText(this, "No products found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Error loading products: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }
}
