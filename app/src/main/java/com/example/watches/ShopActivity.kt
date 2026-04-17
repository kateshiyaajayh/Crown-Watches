package com.example.watches

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class ShopActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private val fullProductList = mutableListOf<Product>()
    private val filteredProductList = mutableListOf<Product>()
    private val db = FirebaseFirestore.getInstance()

    private var currentCategory: String = "All"
    private var currentSearchQuery: String = ""
    private var currentPriceRange: String = "All"
    private var currentSort: String = "Default"

    private lateinit var tvFilter: TextView
    private lateinit var tvPriceRange: TextView
    private lateinit var tvAllItems: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_shop)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        tvFilter = findViewById(R.id.tvFilter)
        tvPriceRange = findViewById(R.id.tvPriceRange)
        tvAllItems = findViewById(R.id.tvAllItems)

        recyclerView = findViewById(R.id.products_recycler_view)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        productAdapter = ProductAdapter(filteredProductList, this)
        recyclerView.adapter = productAdapter

        val searchEditText = findViewById<EditText>(R.id.searchEditText)
        searchEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s.toString().lowercase(Locale.getDefault())
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Top Cart Icon Click
        findViewById<ImageView>(R.id.cartIcon)?.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        // Filter Dropdown (Categories)
        findViewById<LinearLayout>(R.id.btnFilter)?.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menu.add("All")
            popup.menu.add("Men")
            popup.menu.add("Women")
            popup.menu.add("Kids")
            popup.setOnMenuItemClickListener { item ->
                currentCategory = item.title.toString()
                tvFilter.text = currentCategory
                applyFilters()
                true
            }
            popup.show()
        }

        // Price Range Dropdown
        findViewById<LinearLayout>(R.id.btnPriceRange)?.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menu.add("All")
            popup.menu.add("Under ₹500")
            popup.menu.add("₹500 - ₹1000")
            popup.menu.add("Above ₹1000")
            popup.setOnMenuItemClickListener { item ->
                currentPriceRange = item.title.toString()
                tvPriceRange.text = currentPriceRange
                applyFilters()
                true
            }
            popup.show()
        }

        // Sorting Dropdown (All items)
        findViewById<LinearLayout>(R.id.btnAllItems)?.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menu.add("Default")
            popup.menu.add("Price: Low to High")
            popup.menu.add("Price: High to Low")
            popup.setOnMenuItemClickListener { item ->
                currentSort = item.title.toString()
                tvAllItems.text = currentSort
                applyFilters()
                true
            }
            popup.show()
        }

        // Bottom Navigation
        findViewById<ImageView>(R.id.nav_home)?.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        findViewById<ImageView>(R.id.nav_cart)?.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
        findViewById<ImageView>(R.id.nav_profile)?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        loadProducts()
    }

    private fun loadProducts() {
        db.collection("products").get()
            .addOnSuccessListener { documents ->
                fullProductList.clear()
                for (document in documents) {
                    val product = Product(
                        id = document.id,
                        name = document.getString("name") ?: "",
                        price = document.get("price")?.toString() ?: "0",
                        category = document.getString("category") ?: "",
                        imageUrl = document.getString("imageUrl") ?: "",
                        description = document.getString("description") ?: "",
                        quantity = 1,
                        isWishlisted = false
                    )
                    fullProductList.add(product)
                }
                applyFilters()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun applyFilters() {
        var resultList = fullProductList.filter { product ->
            val matchesCategory = currentCategory == "All" || product.category.equals(currentCategory, ignoreCase = true)
            val matchesSearch = product.name.lowercase(Locale.getDefault()).contains(currentSearchQuery)
            
            val price = product.price.toDoubleOrNull() ?: 0.0
            val matchesPrice = when (currentPriceRange) {
                "Under ₹500" -> price < 500
                "₹500 - ₹1000" -> price in 500.0..1000.0
                "Above ₹1000" -> price > 1000
                else -> true
            }
            
            matchesCategory && matchesSearch && matchesPrice
        }

        // Apply Sorting
        resultList = when (currentSort) {
            "Price: Low to High" -> resultList.sortedBy { it.price.toDoubleOrNull() ?: 0.0 }
            "Price: High to Low" -> resultList.sortedByDescending { it.price.toDoubleOrNull() ?: 0.0 }
            else -> resultList
        }

        filteredProductList.clear()
        filteredProductList.addAll(resultList)
        productAdapter.notifyDataSetChanged()
    }
}
