package com.example.watches

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.squareup.picasso.Picasso

class EditProfileActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var imageUri: Uri? = null
    private lateinit var profileImageView: ImageView
    private var isUploading = false

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            profileImageView.setImageURI(imageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)

        val mainView = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        profileImageView = findViewById(R.id.editProfileImage)
        
        initCloudinary()
        loadUserData()

        findViewById<View>(R.id.editProfileCard).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnChangePassword).apply {
            text = "Save Profile"
            setOnClickListener {
                if (isUploading) {
                    Toast.makeText(this@EditProfileActivity, "Please wait for image upload...", Toast.LENGTH_SHORT).show()
                } else {
                    saveProfileChanges()
                }
            }
        }
    }

    private fun initCloudinary() {
        try {
            MediaManager.get()
        } catch (e: Exception) {
            val config = mapOf(
                "cloud_name" to "dmootnykz",
                "api_key" to "722683188962756",
                "api_secret" to "mvSTeLLMXqFkyUZpeQRauAPM3IU"
            )
            MediaManager.init(this, config)
        }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    findViewById<EditText>(R.id.etFirstName).setText(document.getString("firstName"))
                    findViewById<EditText>(R.id.etLastName).setText(document.getString("lastName"))
                    findViewById<EditText>(R.id.etUsername).setText(document.getString("username"))
                    findViewById<EditText>(R.id.etEmail).setText(document.getString("email"))
                    
                    val imageUrl = document.getString("profileImage")
                    if (!imageUrl.isNullOrEmpty()) {
                        Picasso.get().load(imageUrl).placeholder(R.drawable.profile).into(profileImageView)
                    }
                } else {
                    // If document doesn't exist, pre-fill email from Auth
                    findViewById<EditText>(R.id.etEmail).setText(auth.currentUser?.email)
                }
            }
    }

    private fun saveProfileChanges() {
        val firstName = findViewById<EditText>(R.id.etFirstName).text.toString().trim()
        if (firstName.isEmpty()) {
            Toast.makeText(this, "Please enter your first name", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri != null) {
            uploadImageToCloudinary()
        } else {
            updateFirestore(null)
        }
    }

    private fun uploadImageToCloudinary() {
        isUploading = true
        Toast.makeText(this, "Uploading Image...", Toast.LENGTH_SHORT).show()

        MediaManager.get().upload(imageUri)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    isUploading = false
                    val imageUrl = resultData["secure_url"].toString()
                    updateFirestore(imageUrl)
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    isUploading = false
                    Toast.makeText(this@EditProfileActivity, "Upload Failed: ${error.description}", Toast.LENGTH_SHORT).show()
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            }).dispatch()
    }

    private fun updateFirestore(imageUrl: String?) {
        val userId = auth.currentUser?.uid ?: return
        val firstName = findViewById<EditText>(R.id.etFirstName).text.toString().trim()
        val lastName = findViewById<EditText>(R.id.etLastName).text.toString().trim()
        val username = findViewById<EditText>(R.id.etUsername).text.toString().trim()
        val email = findViewById<EditText>(R.id.etEmail).text.toString().trim()
        
        val updates = mutableMapOf<String, Any>(
            "name" to "$firstName $lastName",
            "firstName" to firstName,
            "lastName" to lastName,
            "username" to username,
            "email" to email,
            "uid" to userId
        )
        
        if (imageUrl != null) {
            updates["profileImage"] = imageUrl
        }

        // Use set with SetOptions.merge() to handle cases where document doesn't exist
        db.collection("users").document(userId)
            .set(updates, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Update Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
