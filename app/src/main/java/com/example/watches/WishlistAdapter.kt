package com.example.watches

import android.content.Context
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

class WishlistAdapter(
    private val wishlistList: MutableList<Product>,
    private val context: Context,
    private val onEmpty: () -> Unit
) : RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    inner class WishlistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.product_image)
        val productName: TextView = itemView.findViewById(R.id.product_name)
        val productPrice: TextView = itemView.findViewById(R.id.product_price)
        val btnRemove: ImageView = itemView.findViewById(R.id.wishlist_btn) // Using same icon ID from layout
        val btnAddToCart: MaterialButton = itemView.findViewById(R.id.add_to_cart_btn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishlistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.home_product_item, parent, false)
        return WishlistViewHolder(view)
    }

    override fun onBindViewHolder(holder: WishlistViewHolder, position: Int) {
        val product = wishlistList[position]

        holder.productName.text = product.name
        holder.productPrice.text = "₹${product.price}"
        
        if (product.imageUrl.isNotEmpty()) {
            Picasso.get().load(product.imageUrl).placeholder(R.drawable.watch).into(holder.productImage)
        }

        // Wishlist button acts as "Remove" here
        holder.btnRemove.setImageResource(R.drawable.ic_wishlist_filled)
        holder.btnRemove.setOnClickListener {
            removeFromWishlist(product, position)
        }

        holder.btnAddToCart.setOnClickListener {
            addToCart(product)
        }
    }

    private fun removeFromWishlist(product: Product, position: Int) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("wishlist").document(product.id)
            .delete()
            .addOnSuccessListener {
                wishlistList.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, wishlistList.size)
                if (wishlistList.isEmpty()) onEmpty()
                Toast.makeText(context, "Removed from wishlist", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addToCart(product: Product) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("cart").document(product.id)
            .set(product)
            .addOnSuccessListener {
                Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show()
            }
    }

    override fun getItemCount(): Int = wishlistList.size
}
