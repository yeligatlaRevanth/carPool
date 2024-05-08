package com.example.sem8_project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class A4_HomePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_a4_home_page)

        val btnFindRide = findViewById<ImageButton>(R.id.btn_find_ride)
        val btnCreateRideAlert = findViewById<ImageButton>(R.id.btn_create_ride_alert)

        // Set click listener for Find a Ride button
        btnFindRide.setOnClickListener {
            // Start A4_UserDashboard activity
            val intent = Intent(this, A4_UserDashBoard::class.java)
            startActivity(intent)
        }

        // Set click listener for Create Ride Alert button
        btnCreateRideAlert.setOnClickListener {
            // Start A6_CreateTrip activity
            val intent = Intent(this, A6_CreateTrip::class.java)
            startActivity(intent)
        }
    }
}
