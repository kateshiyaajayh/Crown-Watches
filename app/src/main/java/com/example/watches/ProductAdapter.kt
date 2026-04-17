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
import com.squareup.picasso.Picasso

class ProductAdapter(
    private val productList: MutableList<Product>,
    private val context: Context
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.product_image)
        val productName: TextView = itemView.findViewById(R.id.product_name)
        val productCategory: TextView = itemView.findViewById(R.id.product_category)
        val productPrice: TextView = itemView.findViewById(R.id.product_price)
        val productRating: TextView = itemView.findViewById(R.id.product_rating)
        val wishlistBtn: ImageView = itemView.findViewById(R.id.wishlist_btn)
        val addToCartBtn: MaterialButton = itemView.findViewById(R.id.add_to_cart_btn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.product_item, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        holder.productName.text = product.name
        holder.productCategory.text = product.category
        holder.productPrice.text = "₹${product.price}"
        
        holder.productRating.text = " 3.8"

        // Clear existing image immediately to prevent showing old recycled image
        holder.productImage.setImageDrawable(null)

        if (product.imageUrl.isNotEmpty()) {
            Picasso.get()
                .load(product.imageUrl)
                .placeholder(android.R.color.transparent)
                .error(R.drawable.watch)
                .noFade() // Added noFade to see if it helps with the flash
                .into(holder.productImage)
        } else {
            holder.productImage.setImageResource(R.drawable.watch)
        }

        updateWishlistIcon(holder.wishlistBtn, product.isWishlisted)

        holder.wishlistBtn.setOnClickListener {
            product.isWishlisted = !product.isWishlisted
            updateWishlistIcon(holder.wishlistBtn, product.isWishlisted)
            val msg = if (product.isWishlisted) "Added to Wishlist" else "Removed from Wishlist"
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }

        holder.addToCartBtn.setOnClickListener {
            Toast.makeText(context, "${product.name} added to cart", Toast.LENGTH_SHORT).show()
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
