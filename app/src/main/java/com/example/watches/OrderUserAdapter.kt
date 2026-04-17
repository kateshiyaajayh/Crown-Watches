package com.example.watches

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderUserAdapter(
    private val orderList: List<Order>,
    private val context: Context
) : RecyclerView.Adapter<OrderUserAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOrderId: TextView = itemView.findViewById(R.id.tvOrderId)
        val tvOrderDate: TextView = itemView.findViewById(R.id.tvOrderDate)
        val tvOrderPrice: TextView = itemView.findViewById(R.id.tvOrderPrice)
        val tvOrderStatus: TextView = itemView.findViewById(R.id.tvOrderStatus)
        val tvProductInfo: TextView = itemView.findViewById(R.id.tvProductInfo)
        val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        val tvOrderTrack: TextView = itemView.findViewById(R.id.tvOrderTrack)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_user, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orderList[position]

        holder.tvOrderId.text = "Order ID: #${order.id.takeLast(6).uppercase()}"
        holder.tvOrderPrice.text = "₹${order.totalPrice}"
        holder.tvOrderStatus.text = order.status
        holder.tvProductInfo.text = order.productName

        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        holder.tvOrderDate.text = "Placed on: ${dateFormat.format(Date(order.orderDate))}"

        when (order.status) {
            "Pending", "Confirmed" -> {
                holder.tvOrderStatus.setBackgroundResource(R.drawable.status_badge_pending)
                holder.tvOrderTrack.text = "Your order has been confirmed."
            }
            "Processing" -> {
                holder.tvOrderStatus.setBackgroundResource(R.drawable.status_badge_processing)
                holder.tvOrderTrack.text = "Your order is being prepared and packed."
            }
            "Shipped" -> {
                holder.tvOrderStatus.setBackgroundResource(R.drawable.status_badge_processing)
                holder.tvOrderTrack.text = "Your order is out for delivery."
            }
            "Delivered" -> {
                holder.tvOrderStatus.setBackgroundResource(R.drawable.status_badge_delivered)
                holder.tvOrderTrack.text = "Order delivered successfully!"
            }
            else -> {
                holder.tvOrderStatus.setBackgroundResource(R.drawable.status_badge_pending)
                holder.tvOrderTrack.text = "Status: ${order.status}"
            }
        }
        
        Picasso.get().cancelRequest(holder.ivProductImage)
        val placeholder = ColorDrawable(Color.parseColor("#F5F5F5"))
        holder.ivProductImage.setImageDrawable(placeholder)
        
        val imageUrl = order.productImage
        if (!imageUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(imageUrl)
                .placeholder(placeholder)
                .error(R.drawable.watch)
                .fit()
                .centerCrop()
                .into(holder.ivProductImage)
        } else {
            holder.ivProductImage.setImageResource(R.drawable.watch)
        }

        // On Click -> Go to OrderTrackingActivity
        holder.itemView.setOnClickListener {
            val intent = Intent(context, OrderTrackingActivity::class.java).apply {
                putExtra("productName", order.productName)
                putExtra("orderId", order.id)
                putExtra("price", order.totalPrice)
                putExtra("status", order.status)
                putExtra("image", order.productImage)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = orderList.size
}
