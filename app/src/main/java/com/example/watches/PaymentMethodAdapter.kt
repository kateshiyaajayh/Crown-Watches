package com.example.watches

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PaymentMethodAdapter(
    private val paymentList: List<PaymentMethod>,
    private val onDeleteClick: (PaymentMethod) -> Unit
) : RecyclerView.Adapter<PaymentMethodAdapter.PaymentViewHolder>() {

    inner class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCardType: TextView = itemView.findViewById(R.id.tvCardType)
        val tvCardNumber: TextView = itemView.findViewById(R.id.tvCardNumber)
        val tvCardHolderName: TextView = itemView.findViewById(R.id.tvCardHolderName)
        val tvExpiryDate: TextView = itemView.findViewById(R.id.tvExpiryDate)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDeleteCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_payment_method, parent, false)
        return PaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val payment = paymentList[position]
        holder.tvCardType.text = payment.cardType.uppercase()
        holder.tvCardNumber.text = payment.cardNumber
        holder.tvCardHolderName.text = payment.cardHolderName.uppercase()
        holder.tvExpiryDate.text = payment.expiryDate
        
        holder.btnDelete.setOnClickListener {
            onDeleteClick(payment)
        }
    }

    override fun getItemCount(): Int = paymentList.size
}
