package com.example.watches

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AddressAdapter(
    private val addressList: List<Address>,
    private val onDeleteClick: (Address) -> Unit
) : RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {

    inner class AddressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAddressType: TextView = itemView.findViewById(R.id.tvAddressType)
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvFullAddress: TextView = itemView.findViewById(R.id.tvFullAddress)
        val tvMobile: TextView = itemView.findViewById(R.id.tvMobile)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDeleteAddress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_address, parent, false)
        return AddressViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val address = addressList[position]
        holder.tvAddressType.text = address.addressType
        holder.tvUserName.text = address.fullName
        holder.tvFullAddress.text = "${address.addressLine}, ${address.city}, ${address.state} - ${address.pincode}"
        holder.tvMobile.text = "Phone: ${address.mobileNumber}"
        
        holder.btnDelete.setOnClickListener {
            onDeleteClick(address)
        }
    }

    override fun getItemCount(): Int = addressList.size
}
