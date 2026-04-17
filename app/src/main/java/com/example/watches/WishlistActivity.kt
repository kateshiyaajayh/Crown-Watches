package com.example.watches

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WishlistActivity : AppCompatActivity() {

    private lateinit var rvWishlist: RecyclerView
    private lateinit var adapter: WishlistAdapter
    private val wishlistList = mutableListOf<Product>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var emptyState: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wishlist)

        initViews()
        setupRecyclerView()
        loadWishlist()

        findViewById<ImageView>(R.id.backBtn).setOnClickListener {
            finish()
        }
    }

    private fun initViews() {
        rvWishlist = findViewById(R.id.rvWishlist)
        emptyState = findViewById(R.id.emptyState)
    }

    private fun setupRecyclerView() {
        adapter = WishlistAdapter(wishlistList, this) {
            emptyState.visibility = View.VISIBLE
            rvWishlist.visibility = View.GONE
        }
        rvWishlist.layoutManager = GridLayoutManager(this, 2)
        rvWishlist.adapter = adapter
    }

    private fun loadWishlist() {
        val userId = auth.currentUser?.uid ?: return
        
        db.collection("users").document(userId).collection("wishlist")
            .get()
            .addOnSuccessListener { documents ->
                wishlistList.clear()
                for (document in documents) {
                    val product = Product(
                        id = document.id,
                        name = document.getString("name") ?: "",
                        price = document.getString("price") ?: "0",
                        category = document.getString("category") ?: "",
                        imageUrl = document.getString("imageUrl") ?: "",
                        description = document.getString("description") ?: "",
                        isWishlisted = true
                    )
                    wishlistList.add(product)
                }
                
                if (wishlistList.isEmpty()) {
                    emptyState.visibility = View.VISIBLE
                    rvWishlist.visibility = View.GONE
                } else {
                    emptyState.visibility = View.GONE
                    rvWishlist.visibility = View.VISIBLE
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
