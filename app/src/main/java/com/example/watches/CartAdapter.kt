package com.example.watches

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class CartAdapter(
    private val cartItems: MutableList<Product>,
    private val onUpdate: () -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productImage: ImageView = view.findViewById(R.id.productImage)
        val productName: TextView = view.findViewById(R.id.productName)
        val productBrand: TextView = view.findViewById(R.id.productBrand)
        val productPrice: TextView = view.findViewById(R.id.productPrice)
        val tvQuantity: TextView = view.findViewById(R.id.tvQuantity)
        val btnPlus: ImageView = view.findViewById(R.id.btnPlus)
        val btnMinus: ImageView = view.findViewById(R.id.btnMinus)
        val btnDelete: ImageView = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val product = cartItems[position]
        holder.productName.text = product.name
        holder.productBrand.text = product.category
        holder.productPrice.text = "₹${product.price}"
        holder.tvQuantity.text = String.format("%02d", product.quantity)

        if (product.imageUrl.isNotEmpty()) {
            Picasso.get().load(product.imageUrl).placeholder(R.drawable.watch).into(holder.productImage)
        }

        holder.btnPlus.setOnClickListener {
            product.quantity++
            updateFirestoreQuantity(product)
            notifyItemChanged(position)
            onUpdate()
        }

        holder.btnMinus.setOnClickListener {
            if (product.quantity > 1) {
                product.quantity--
                updateFirestoreQuantity(product)
                notifyItemChanged(position)
                onUpdate()
            }
        }

        holder.btnDelete.setOnClickListener {
            removeFromFirestore(product, position)
        }
    }

    private fun updateFirestoreQuantity(product: Product) {
        if (userId == null) return
        db.collection("users").document(userId).collection("cart").document(product.id)
            .update("quantity", product.quantity)
    }

    private fun removeFromFirestore(product: Product, position: Int) {
        if (userId == null) return
        db.collection("users").document(userId).collection("cart").document(product.id)
            .delete()
            .addOnSuccessListener {
                if (position < cartItems.size) {
                    cartItems.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, cartItems.size)
                    onUpdate()
                }
            }
    }

    override fun getItemCount() = cartItems.size
}
