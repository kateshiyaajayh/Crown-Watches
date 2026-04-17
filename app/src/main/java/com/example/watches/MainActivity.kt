package com.example.watches

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: HomeProductAdapter
    private val allProductsList = mutableListOf<Product>()
    private val displayedProductList = mutableListOf<Product>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var selectedCategory = "All"
    private var searchQuery = ""

    private lateinit var btnAll: MaterialButton
    private lateinit var btnMen: MaterialButton
    private lateinit var btnWomen: MaterialButton
    private lateinit var btnKids: MaterialButton
    private lateinit var searchET: EditText

    // Banner Slider variables
    private lateinit var bannerViewPager: ViewPager2
    private lateinit var bannerAdapter: BannerAdapter
    private val sliderHandler = Handler(Looper.getMainLooper())
    private val sliderRunnable = Runnable {
        val nextItem = (bannerViewPager.currentItem + 1) % bannerAdapter.itemCount
        bannerViewPager.currentItem = nextItem
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_main)

            // UI Components setup
            btnAll = findViewById(R.id.btnAll)
            btnMen = findViewById(R.id.btnMen)
            btnWomen = findViewById(R.id.btnWomen)
            btnKids = findViewById(R.id.btnKids)
            searchET = findViewById(R.id.searchET)

            // RecyclerView setup
            recyclerView = findViewById(R.id.home_products_recycler_view)
            recyclerView.layoutManager = GridLayoutManager(this, 2)
            productAdapter = HomeProductAdapter(displayedProductList, this)
            recyclerView.adapter = productAdapter

            // Setup Banner Slider
            setupBannerSlider()

            // Category Click Listeners
            btnAll.setOnClickListener { selectCategory("All") }
            btnMen.setOnClickListener { selectCategory("Men") }
            btnWomen.setOnClickListener { selectCategory("Women") }
            btnKids.setOnClickListener { selectCategory("Kids") }

            // Search Filter
            searchET.addTextChangedListener { text ->
                searchQuery = text.toString().lowercase(Locale.getDefault())
                applyFilters()
            }

            // Bottom Nav
            findViewById<ImageView>(R.id.nav_shop)?.setOnClickListener {
                startActivity(Intent(this, ShopActivity::class.java))
            }
            findViewById<ImageView>(R.id.nav_cart)?.setOnClickListener {
                startActivity(Intent(this, CartActivity::class.java))
            }
            findViewById<ImageView>(R.id.nav_profile)?.setOnClickListener {
                startActivity(Intent(this, ProfileActivity::class.java))
            }

            // Profile Image click in Header
            findViewById<ImageView>(R.id.profileImg)?.setOnClickListener {
                startActivity(Intent(this, ProfileActivity::class.java))
            }

            loadUserData()
            loadProducts()

        } catch (e: Exception) {
            Log.e("MainActivity", "Error: ${e.message}")
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupBannerSlider() {
        bannerViewPager = findViewById(R.id.bannerViewPager)

        val bannerItems = listOf(
            BannerItem(
                1,
                BannerType.VIDEO,
                "android.resource://${packageName}/${R.raw.crown_watches_video}",
                "Luxury Collection",
                "Experience Elegance with Crown Watches"
            ),
            BannerItem(
                2,
                BannerType.IMAGE,
                "watch", 
                "Special Offer",
                "Get 30% Off on New Arrivals"
            ),
            BannerItem(
                3,
                BannerType.IMAGE,
                "watch", 
                "Flash Sale",
                "Grab Your Favorite Watches Now"
            )
        )

        // Pass both lambdas to fix the compilation error
        bannerAdapter = BannerAdapter(
            context = this,
            bannerItems = bannerItems,
            onVideoFinished = {
                // Move to next slide when video ends
                val nextItem = (bannerViewPager.currentItem + 1) % bannerItems.size
                bannerViewPager.currentItem = nextItem
            },
            onGrabNowClick = { item ->
                // Working Grab Now Button - Navigate to ShopActivity
                startActivity(Intent(this, ShopActivity::class.java))
            }
        )
        bannerViewPager.adapter = bannerAdapter

        // Dots setup
        setupDots(bannerItems.size)

        // ViewPager Configuration
        bannerViewPager.clipToPadding = false
        bannerViewPager.clipChildren = false
        bannerViewPager.offscreenPageLimit = 3
        bannerViewPager.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(MarginPageTransformer(40))
        compositePageTransformer.addTransformer { page, position ->
            val r = 1 - Math.abs(position)
            page.scaleY = 0.85f + r * 0.15f
        }
        bannerViewPager.setPageTransformer(compositePageTransformer)

        bannerViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                
                updateDots(position)

                // Video Control
                for (i in 0 until bannerAdapter.itemCount) {
                    if (i == position) {
                        bannerAdapter.playVideo(i)
                    } else {
                        bannerAdapter.pauseVideo(i)
                    }
                }

                sliderHandler.removeCallbacks(sliderRunnable)
                sliderHandler.postDelayed(sliderRunnable, 9000) 
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    sliderHandler.removeCallbacks(sliderRunnable)
                } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    sliderHandler.removeCallbacks(sliderRunnable)
                    sliderHandler.postDelayed(sliderRunnable, 9000) 
                }
            }
        })
    }

    private fun setupDots(size: Int) {
        val dotsLayout = findViewById<LinearLayout>(R.id.dotsLayout)
        dotsLayout.removeAllViews()
        val dots = arrayOfNulls<ImageView>(size)

        for (i in 0 until size) {
            dots[i] = ImageView(this)
            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(8, 0, 8, 0)
            dots[i]?.layoutParams = params
            dots[i]?.setImageResource(android.R.drawable.presence_invisible) 
            dots[i]?.setBackgroundResource(R.drawable.dot_selector) 
            dotsLayout.addView(dots[i])
        }
    }
    
    private fun updateDots(position: Int) {
        val dotsLayout = findViewById<LinearLayout>(R.id.dotsLayout)
        if (dotsLayout == null) return
        for (i in 0 until dotsLayout.childCount) {
            val dot = dotsLayout.getChildAt(i) as? ImageView ?: continue
            if (i == position) {
                dot.alpha = 1.0f
                dot.scaleX = 1.2f
                dot.scaleY = 1.2f
                dot.setColorFilter(ContextCompat.getColor(this, R.color.purple_primary))
            } else {
                dot.alpha = 0.4f
                dot.scaleX = 1.0f
                dot.scaleY = 1.0f
                dot.setColorFilter(ContextCompat.getColor(this, android.R.color.darker_gray))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
        sliderHandler.postDelayed(sliderRunnable, 9000) 
    }

    override fun onPause() {
        super.onPause()
        sliderHandler.removeCallbacks(sliderRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        bannerAdapter.releasePlayers()
    }

    private fun selectCategory(category: String) {
        selectedCategory = category
        updateCategoryButtonStyles()
        applyFilters()
    }

    private fun updateCategoryButtonStyles() {
        val buttons = listOf(btnAll, btnMen, btnWomen, btnKids)
        val categories = listOf("All", "Men", "Women", "Kids")

        for (i in buttons.indices) {
            if (categories[i] == selectedCategory) {
                buttons[i].backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.purple_primary))
                buttons[i].setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                buttons[i].backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
                buttons[i].setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }
    }

    private fun applyFilters() {
        displayedProductList.clear()
        val filtered = allProductsList.filter { product ->
            val matchesCategory = selectedCategory == "All" || product.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isEmpty() || product.name.lowercase().contains(searchQuery)
            matchesCategory && matchesSearch
        }
        displayedProductList.addAll(filtered)
        productAdapter.notifyDataSetChanged()
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                findViewById<TextView>(R.id.userNameTV)?.text = "Hello, ${doc.getString("name") ?: "User"}"

                val imageUrl = doc.getString("profileImage")
                if (!imageUrl.isNullOrEmpty()) {
                    val profileImg = findViewById<ImageView>(R.id.profileImg)
                    Picasso.get().load(imageUrl).placeholder(R.drawable.profile).into(profileImg)
                }
            }
        }
    }

    private fun loadProducts() {
        db.collection("products").get().addOnSuccessListener { docs ->
            allProductsList.clear()
            for (doc in docs) {
                val product = Product(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    price = doc.get("price")?.toString() ?: "0",
                    category = doc.getString("category") ?: "",
                    imageUrl = doc.getString("imageUrl") ?: "",
                    description = doc.getString("description") ?: "",
                    quantity = 1,
                    isWishlisted = false
                )
                allProductsList.add(product)
            }
            applyFilters()
        }
    }
}
