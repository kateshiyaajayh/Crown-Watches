package com.example.watches

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class HomeProductAdapter(
    private val productList: List<Product>,
    private val context: Context
) : RecyclerView.Adapter<HomeProductAdapter.HomeProductViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    class HomeProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.product_image)
        val productRating: TextView = itemView.findViewById(R.id.product_rating)
        val productName: TextView = itemView.findViewById(R.id.product_name)
        val productPrice: TextView = itemView.findViewById(R.id.product_price)
        val productDesc: TextView = itemView.findViewById(R.id.product_desc_short)
        val addToCartBtn: MaterialButton = itemView.findViewById(R.id.add_to_cart_btn)
        val wishlistBtn: ImageView = itemView.findViewById(R.id.wishlist_btn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.home_product_item, parent, false)
        return HomeProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeProductViewHolder, position: Int) {
        val product = productList[position]

        holder.productName.text = product.name
        holder.productPrice.text = "₹${product.price}"
        holder.productDesc.text = product.description.ifEmpty { "Premium Quality Watch" }
        holder.productRating.text = " 3.8"

        if (product.imageUrl.isNotEmpty()) {
            Picasso.get()
                .load(product.imageUrl)
                .placeholder(android.R.color.transparent)
                .error(R.drawable.watch)
                .into(holder.productImage)
        } else {
            holder.productImage.setImageResource(R.drawable.watch)
        }

        // Initialize Wishlist State
        checkIfWishlisted(product, holder.wishlistBtn)

        holder.wishlistBtn.setOnClickListener {
            toggleWishlist(product, holder.wishlistBtn)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ProductDetailActivity::class.java)
            intent.putExtra("product_id", product.id)
            intent.putExtra("product_name", product.name)
            intent.putExtra("product_price", product.price)
            intent.putExtra("product_category", product.category)
            intent.putExtra("product_image_url", product.imageUrl)
            intent.putExtra("product_description", product.description)
            context.startActivity(intent)
        }

        holder.addToCartBtn.setOnClickListener {
            addToCart(product)
        }
    }

    private fun checkIfWishlisted(product: Product, wishlistBtn: ImageView) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("wishlist").document(product.id)
            .get()
            .addOnSuccessListener { doc ->
                product.isWishlisted = doc.exists()
                updateWishlistIcon(wishlistBtn, product.isWishlisted)
            }
    }

    private fun toggleWishlist(product: Product, wishlistBtn: ImageView) {
        val userId = auth.currentUser?.uid ?: return
        val wishlistRef = db.collection("users").document(userId).collection("wishlist").document(product.id)

        if (product.isWishlisted) {
            wishlistRef.delete().addOnSuccessListener {
                product.isWishlisted = false
                updateWishlistIcon(wishlistBtn, false)
                Toast.makeText(context, "Removed from Wishlist", Toast.LENGTH_SHORT).show()
            }
        } else {
            wishlistRef.set(product).addOnSuccessListener {
                product.isWishlisted = true
                updateWishlistIcon(wishlistBtn, true)
                Toast.makeText(context, "Added to Wishlist", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addToCart(product: Product) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("cart").document(product.id)
            .set(product)
            .addOnSuccessListener {
                Toast.makeText(context, "${product.name} added to cart", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateWishlistIcon(imageView: ImageView, isWishlisted: Boolean) {
        if (isWishlisted) {
            imageView.setImageResource(R.drawable.ic_wishlist_filled)
        } else {
            imageView.setImageResource(R.drawable.ic_wishlist_border)
        }
    }

    override fun getItemCount(): Int = productList.size
}
