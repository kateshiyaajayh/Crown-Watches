package com.example.watches

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NotificationsActivity : AppCompatActivity() {

    private lateinit var rvNotifications: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private val notificationList = mutableListOf<Notification>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var emptyState: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        initViews()
        setupRecyclerView()
        loadNotifications()

        findViewById<ImageView>(R.id.backBtn).setOnClickListener {
            finish()
        }
    }

    private fun initViews() {
        rvNotifications = findViewById(R.id.rvNotifications)
        emptyState = findViewById(R.id.emptyState)
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter(notificationList)
        rvNotifications.layoutManager = LinearLayoutManager(this)
        rvNotifications.adapter = adapter
    }

    private fun loadNotifications() {
        val userId = auth.currentUser?.uid ?: return
        
        db.collection("users").document(userId).collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                notificationList.clear()
                for (document in documents) {
                    val notification = Notification(
                        id = document.id,
                        title = document.getString("title") ?: "",
                        message = document.getString("message") ?: "",
                        timestamp = document.getLong("timestamp") ?: 0,
                        type = document.getString("type") ?: "Order"
                    )
                    notificationList.add(notification)
                }
                
                if (notificationList.isEmpty()) {
                    emptyState.visibility = View.VISIBLE
                    rvNotifications.visibility = View.GONE
                } else {
                    emptyState.visibility = View.GONE
                    rvNotifications.visibility = View.VISIBLE
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
