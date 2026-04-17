package com.example.watches

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class AddProductActivity : AppCompatActivity() {
    
    private lateinit var db: FirebaseFirestore
    private lateinit var productImagePreview: ImageView
    private lateinit var categorySpinner: Spinner
    private var selectedImageUri: Uri? = null
    private var uploadedImageUrl: String = ""
    private var isUploading = false

    private val categories = arrayOf("Men", "Women", "Kids")

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
        private const val PERMISSION_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Initialize Cloudinary
        initializeCloudinary()

        // Get references
        val productNameInput: EditText = findViewById(R.id.product_name_input)
        val productPriceInput: EditText = findViewById(R.id.product_price_input)
        categorySpinner = findViewById(R.id.product_category_spinner)
        val productDescriptionInput: EditText = findViewById(R.id.product_description_input)
        val selectImageButton: Button = findViewById(R.id.select_image_button)
        val addProductButton: Button = findViewById(R.id.add_product_button)
        productImagePreview = findViewById(R.id.product_image_preview)

        // Setup Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        // Select Image Button
        selectImageButton.setOnClickListener {
            if (hasPermissions()) {
                openImageGallery()
            } else {
                requestPermissions()
            }
        }

        // Add Product Button
        addProductButton.setOnClickListener {
            if (isUploading) {
                Toast.makeText(this, "Please wait for image upload...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val productName = productNameInput.text.toString().trim()
            val productPrice = productPriceInput.text.toString().trim()
            val productCategory = categorySpinner.selectedItem.toString()
            val productDescription = productDescriptionInput.text.toString().trim()

            if (productName.isEmpty() || productPrice.isEmpty() || productDescription.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (uploadedImageUrl.isEmpty()) {
                Toast.makeText(this, "Please select and upload an image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            saveProductToFirestore(productName, productPrice, productCategory, uploadedImageUrl, productDescription)
        }
    }

    private fun hasPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), PERMISSION_REQUEST_CODE)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openImageGallery()
        }
    }

    private fun initializeCloudinary() {
        try {
            MediaManager.get()
        } catch (e: Exception) {
            val config = HashMap<String, String>()
            config["cloud_name"] = "dmootnykz"
            config["api_key"] = "722683188962756"
            config["api_secret"] = "mvSTeLLMXqFkyUZpeQRauAPM3IU"
            MediaManager.init(this, config)
        }
    }

    private fun openImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data
            selectedImageUri?.let {
                Picasso.get().load(it).into(productImagePreview)
                uploadImageToCloudinary(it)
            }
        }
    }

    private fun uploadImageToCloudinary(imageUri: Uri) {
        isUploading = true
        MediaManager.get().upload(imageUri)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {
                    Toast.makeText(this@AddProductActivity, "Uploading...", Toast.LENGTH_SHORT).show()
                }
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    isUploading = false
                    uploadedImageUrl = resultData?.get("secure_url").toString()
                    Toast.makeText(this@AddProductActivity, "Upload success", Toast.LENGTH_SHORT).show()
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    isUploading = false
                    Toast.makeText(this@AddProductActivity, "Upload failed", Toast.LENGTH_SHORT).show()
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }

    private fun saveProductToFirestore(name: String, price: String, category: String, imageUrl: String, description: String) {
        val product = hashMapOf(
            "name" to name,
            "price" to price,
            "category" to category,
            "description" to description,
            "imageUrl" to imageUrl
        )

        db.collection("products").add(product)
            .addOnSuccessListener {
                Toast.makeText(this, "Product Added!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
