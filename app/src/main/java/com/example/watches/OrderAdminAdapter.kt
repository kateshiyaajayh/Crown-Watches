package com.example.watches

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Order(
    val id: String = "",
    val productName: String = "",
    val quantity: Int = 0,
    val totalPrice: String = "",
    var status: String = "Pending",
    val orderDate: Long = 0,
    val productImage: String = ""
)

class OrderAdminAdapter(
    private val orderList: MutableList<Order>,
    private val context: Context
) : RecyclerView.Adapter<OrderAdminAdapter.OrderViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val statusOptions = arrayOf("Confirmed", "Processing", "Shipped", "Delivered")

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderId: TextView = itemView.findViewById(R.id.order_id)
        val orderDate: TextView = itemView.findViewById(R.id.order_date)
        val productName: TextView = itemView.findViewById(R.id.product_name)
        val quantity: TextView = itemView.findViewById(R.id.quantity)
        val totalPrice: TextView = itemView.findViewById(R.id.total_price)
        val orderStatus: TextView = itemView.findViewById(R.id.order_status)
        val statusSpinner: Spinner = itemView.findViewById(R.id.status_spinner)
        val btnUpdateStatus: Button = itemView.findViewById(R.id.btn_update_status)
        val downloadLabelButton: Button = itemView.findViewById(R.id.download_label_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.order_item_admin, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orderList[position]

        holder.orderId.text = "Order ID: #${order.id.takeLast(6).uppercase()}"
        holder.productName.text = order.productName
        holder.quantity.text = order.quantity.toString()
        holder.totalPrice.text = "₹${order.totalPrice}"
        holder.orderStatus.text = order.status

        // Status Badge Style
        when (order.status) {
            "Confirmed", "Pending" -> holder.orderStatus.setBackgroundResource(R.drawable.status_badge_pending)
            "Processing", "Shipped" -> holder.orderStatus.setBackgroundResource(R.drawable.status_badge_processing)
            "Delivered" -> holder.orderStatus.setBackgroundResource(R.drawable.status_badge_delivered)
        }

        // Setup Spinner
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, statusOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.statusSpinner.adapter = adapter
        
        // Set current status in spinner
        val currentPos = statusOptions.indexOf(order.status)
        if (currentPos >= 0) holder.statusSpinner.setSelection(currentPos)

        // Date format
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        holder.orderDate.text = dateFormat.format(Date(order.orderDate))

        // Update Button Click
        holder.btnUpdateStatus.setOnClickListener {
            val selectedStatus = holder.statusSpinner.selectedItem.toString()
            updateOrderStatus(order.id, selectedStatus, position)
        }

        holder.downloadLabelButton.setOnClickListener {
            val intent = Intent(context, ShippingLabelActivity::class.java)
            intent.putExtra("order_id", order.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = orderList.size

    private fun updateOrderStatus(orderId: String, newStatus: String, position: Int) {
        db.collection("orders").document(orderId)
            .update("status", newStatus)
            .addOnSuccessListener {
                orderList[position].status = newStatus
                notifyItemChanged(position)
                Toast.makeText(context, "Order updated to $newStatus", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }
}
