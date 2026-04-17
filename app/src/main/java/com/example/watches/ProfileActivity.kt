package com.example.watches

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class ProfileActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        val mainView = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("445217743278-jo7uc4p6isgrnpvit7ujbu1g9b5dmk8n.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setupMenuActions()
        loadUserData()

        findViewById<ImageView>(R.id.backBtn).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.btnEditProfile).setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }

    private fun setupMenuActions() {
        // My Orders
        val menuOrder = findViewById<View>(R.id.menuOrder)
        menuOrder.findViewById<TextView>(R.id.menuTitle).text = "My Order"
        menuOrder.setOnClickListener {
            startActivity(Intent(this, OrdersActivity::class.java))
        }

        // Shipping Address
        val menuAddress = findViewById<View>(R.id.menuAddress)
        menuAddress.findViewById<TextView>(R.id.menuTitle).text = "Shipping Address"
        menuAddress.setOnClickListener {
            startActivity(Intent(this, ShippingAddressActivity::class.java))
        }

        // Languages
        val menuLanguage = findViewById<View>(R.id.menuLanguage)
        menuLanguage.findViewById<TextView>(R.id.menuTitle).text = "Languages"
        menuLanguage.setOnClickListener {
            showLanguageDialog()
        }

        // Payment Methods
        val menuPayment = findViewById<View>(R.id.menuPayment)
        menuPayment.findViewById<TextView>(R.id.menuTitle).text = "Payment Methods"
        menuPayment.setOnClickListener {
            startActivity(Intent(this, PaymentMethodsActivity::class.java))
        }

        // Notifications
        val menuNotifications = findViewById<View>(R.id.menuNotifications)
        menuNotifications.findViewById<TextView>(R.id.menuTitle).text = "Notifications"
        menuNotifications.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        // Wishlist
        val menuWishlist = findViewById<View>(R.id.menuWishlist)
        menuWishlist.findViewById<TextView>(R.id.menuTitle).text = "Wishlist"
        menuWishlist.setOnClickListener {
            startActivity(Intent(this, WishlistActivity::class.java))
        }

        // Settings
        val menuSettings = findViewById<View>(R.id.menuSettings)
        menuSettings.findViewById<TextView>(R.id.menuTitle).text = "Settings"
        menuSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Help & Support
        val menuHelp = findViewById<View>(R.id.menuHelp)
        menuHelp.findViewById<TextView>(R.id.menuTitle).text = "Help & Support"
        menuHelp.setOnClickListener {
            startActivity(Intent(this, HelpSupportActivity::class.java))
        }

        // Logout
        val menuLogout = findViewById<View>(R.id.menuLogout)
        menuLogout.findViewById<TextView>(R.id.menuTitle).text = "Log Out"
        menuLogout.setOnClickListener {
            // Sign out from Firebase
            auth.signOut()
            
            // Sign out from Google to allow choosing account next time
            googleSignInClient.signOut().addOnCompleteListener {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("English", "हिंदी", "ગુજરાતી")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Language")
        builder.setItems(languages) { dialog, which ->
            val selectedLanguage = languages[which]
            Toast.makeText(this, "Language set to $selectedLanguage", Toast.LENGTH_SHORT).show()
        }
        builder.show()
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    findViewById<TextView>(R.id.tvUserName).text = document.getString("name") ?: "User Name"
                    findViewById<TextView>(R.id.tvUserEmail).text = document.getString("email") ?: "user@gmail.com"
                    
                    val imageUrl = document.getString("profileImage")
                    if (!imageUrl.isNullOrEmpty()) {
                        val profileImg = findViewById<ImageView>(R.id.profileImage)
                        Picasso.get().load(imageUrl).placeholder(R.drawable.profile).into(profileImg)
                    }
                }
            }
    }
}