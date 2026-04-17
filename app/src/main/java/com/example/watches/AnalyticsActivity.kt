package com.example.watches

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AnalyticsActivity : AppCompatActivity() {

    private lateinit var totalOrdersValue: TextView
    private lateinit var totalRevenueValue: TextView
    private lateinit var todayOrdersValue: TextView
    private lateinit var revenueChart: LineChart
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        // Initialize UI elements
        totalOrdersValue = findViewById(R.id.total_orders_value)
        totalRevenueValue = findViewById(R.id.total_revenue_value)
        todayOrdersValue = findViewById(R.id.today_orders_value)
        revenueChart = findViewById(R.id.revenue_chart)

        // Back button
        findViewById<ImageView>(R.id.back_button).setOnClickListener {
            finish()
        }

        // Load analytics data
        loadAnalyticsData()
    }

    private fun loadAnalyticsData() {
        db.collection("orders")
            .get()
            .addOnSuccessListener { documents ->
                var totalOrders = 0
                var totalRevenue = 0.0
                var todayOrders = 0
                val monthlyRevenue = mutableMapOf<Int, Double>()
                
                val todayDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

                // Initialize monthly revenue map (1..12 months)
                for (i in 1..12) {
                    monthlyRevenue[i] = 0.0
                }

                for (document in documents) {
                    totalOrders++

                    // Match with CheckoutActivity fields: totalAmount and timestamp
                    val priceStr = document.get("totalAmount")?.toString() ?: "0"
                    val price = priceStr.toDoubleOrNull() ?: 0.0
                    totalRevenue += price

                    val orderTimestamp = document.getLong("timestamp") ?: 0
                    val date = Date(orderTimestamp)
                    val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)

                    // Check if order is from today
                    if (formattedDate == todayDate) {
                        todayOrders++
                    }

                    // Add to monthly revenue
                    val cal = Calendar.getInstance()
                    cal.time = date
                    val month = cal.get(Calendar.MONTH) + 1
                    monthlyRevenue[month] = (monthlyRevenue[month] ?: 0.0) + price
                }

                // Update UI
                totalOrdersValue.text = totalOrders.toString()
                totalRevenueValue.text = "₹${String.format("%.0f", totalRevenue)}"
                todayOrdersValue.text = todayOrders.toString()

                // Setup chart
                setupRevenueChart(monthlyRevenue)

                if (documents.isEmpty()) {
                    Toast.makeText(this, "No orders found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Error loading analytics: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun setupRevenueChart(monthlyRevenue: Map<Int, Double>) {
        val entries = mutableListOf<Entry>()
        val monthLabels = arrayOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

        for (i in 1..12) {
            val revenue = monthlyRevenue[i] ?: 0.0
            entries.add(Entry(i.toFloat(), revenue.toFloat()))
        }

        val dataSet = LineDataSet(entries, "Revenue (₹)").apply {
            color = android.graphics.Color.parseColor("#6C63FF")
            valueTextColor = android.graphics.Color.BLACK
            lineWidth = 3f
            circleRadius = 5f
            setCircleColor(android.graphics.Color.parseColor("#6C63FF"))
            setDrawValues(true)
            valueTextSize = 8f
            setDrawFilled(true)
            fillAlpha = 50
            fillColor = android.graphics.Color.parseColor("#6C63FF")
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        val lineData = LineData(dataSet)
        revenueChart.data = lineData

        // Configure chart appearance
        revenueChart.apply {
            description.isEnabled = false
            xAxis.apply {
                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        return if (index in 1..12) monthLabels[index] else ""
                    }
                }
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
            }
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = android.graphics.Color.parseColor("#E0E0E0")
            }
            axisRight.isEnabled = false
            legend.isEnabled = true
            animateXY(1000, 1000)
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
        }
    }
}
