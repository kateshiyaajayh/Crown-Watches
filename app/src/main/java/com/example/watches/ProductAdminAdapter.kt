package com.example.watches

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class ProductAdminAdapter(
    private val productList: MutableList<Product>,
    private val context: Context
) : RecyclerView.Adapter<ProductAdminAdapter.ProductViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    inner class ProductViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.product_image)
        val productName: TextView = itemView.findViewById(R.id.product_name)
        val productPrice: TextView = itemView.findViewById(R.id.product_price)
        val productCategory: TextView = itemView.findViewById(R.id.product_category)
        val editButton: Button = itemView.findViewById(R.id.edit_button)
        val deleteButton: Button = itemView.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.product_item_admin, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        holder.productName.text = product.name
        holder.productPrice.text = "₹${product.price}"
        holder.productCategory.text = product.category

        // Load image using Picasso
        if (product.imageUrl.isNotEmpty()) {
            Picasso.get().load(product.imageUrl).into(holder.productImage)
        }

        // Edit button click listener
        holder.editButton.setOnClickListener {
            val intent = Intent(context, EditProductActivity::class.java)
            intent.putExtra("product_id", product.id)
            intent.putExtra("product_name", product.name)
            intent.putExtra("product_price", product.price)
            intent.putExtra("product_category", product.category)
            intent.putExtra("product_image_url", product.imageUrl)
            intent.putExtra("product_description", product.description)
            context.startActivity(intent)
        }

        // Delete button click listener
        holder.deleteButton.setOnClickListener {
            deleteProduct(product.id, position)
        }
    }

    override fun getItemCount(): Int = productList.size

    private fun deleteProduct(productId: String, position: Int) {
        db.collection("products").document(productId)
            .delete()
            .addOnSuccessListener {
                productList.removeAt(position)
                notifyItemRemoved(position)
                Toast.makeText(context, "Product Deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }
}
