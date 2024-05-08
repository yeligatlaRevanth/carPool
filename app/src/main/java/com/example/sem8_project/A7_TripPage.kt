package com.example.sem8_project

import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class A7_TripPage : AppCompatActivity(){
    lateinit var tripId: String
    private lateinit var fromLocationTextView: TextView
    private lateinit var toLocationTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var seatsTextView: TextView
    private lateinit var pricePerSeatTextView: TextView
    private lateinit var carFacilitiesTextView: TextView
    private lateinit var createdByEmailTextView: TextView
    private lateinit var btnViewRoute: Button
    private lateinit var btnJoinTrip: Button

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap


    private val currUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_a7_trip_page)

        backBtn()
        fromLocationTextView = findViewById(R.id.fromLocationTextView)
        toLocationTextView = findViewById(R.id.toLocationTextView)
        dateTextView = findViewById(R.id.dateTextView)
        timeTextView = findViewById(R.id.timeTextView)
        seatsTextView = findViewById(R.id.seatsTextView)
        pricePerSeatTextView = findViewById(R.id.pricePerSeatTextView)
        carFacilitiesTextView = findViewById(R.id.carFacilitiesTextView)
        createdByEmailTextView = findViewById(R.id.createdByEmailTextView)
        btnViewRoute = findViewById(R.id.btnViewRoute)
        btnJoinTrip = findViewById(R.id.btnJoinTrip)
        tripId = mytripId()
        loadTripDetails()

        btnViewRoute.setOnClickListener {
            openGoogleMapsRoute()
        }
        btnJoinTrip.setOnClickListener {
            sendJoinRequest()
            addToDB()

        }
    }

    private fun addToDB() {
        val currentUserId = currUserId ?: return

        val tripRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId).child("OtherRides")

        // Get the number of existing rides to determine the new ride number
        tripRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val rideCount = dataSnapshot.childrenCount.toInt() + 1

                // Create a new ride node
                val rideRef = tripRef.child("Ride$rideCount")
                rideRef.setValue(tripId)

                Toast.makeText(applicationContext, "Added to your rides", Toast.LENGTH_SHORT).show()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase", "onCancelled", databaseError.toException())
            }
        })
    }
    private fun sendJoinRequest() {
        // Get the current user's ID
        val currentUserId = currUserId ?: return

        // Reference to the trip in the database
        val userId = tripId.split("%_%")[0]
        val tripNumber = tripId.split("%_%")[1]
        val tripRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("MyRides").child(tripNumber)

        // Check if there's still available seats
        tripRef.child("seats").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val availableSeats = dataSnapshot.getValue(Long::class.java) ?: 0
                if (availableSeats > 0) {
                    // Decrease the available seats count

                    // Get the count of passengers
                    tripRef.child("Passengers").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(passengerDataSnapshot: DataSnapshot) {
                            val passengerCount = passengerDataSnapshot.childrenCount.toInt() + 1

                            // Update the Passenger node with the user's details
                            val passengerRef = tripRef.child("Passengers").child("Child$passengerCount")
                            passengerRef.child("userId").setValue(currentUserId)
                            passengerRef.child("status").setValue("pending")

                            Toast.makeText(applicationContext, "Join request sent", Toast.LENGTH_SHORT).show()
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.e("Firebase", "onCancelled", databaseError.toException())
                        }
                    })
                } else {
                    Toast.makeText(applicationContext, "No available seats", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase", "onCancelled", databaseError.toException())
            }
        })
    }


    private fun openGoogleMapsRoute() {
        // Extract from and to locations from TextViews
        val fromLocation = fromLocationTextView.text.toString()
        val toLocation = toLocationTextView.text.toString()

        // Construct the URI with the query parameters for the route
        val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=$fromLocation&destination=$toLocation")

        // Create an intent to view the map
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        mapIntent.setPackage("com.google.android.apps.maps")

        // Check if the Google Maps app is installed
        if (mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        } else {
            // If Google Maps app is not installed, display a message or fallback to a web browser
            Toast.makeText(applicationContext, "Google Maps app not found", Toast.LENGTH_SHORT).show()
        }
    }


    private fun mytripId(): String {
        val tId = intent.getStringExtra("tripId")
        Toast.makeText(this, tId, Toast.LENGTH_SHORT).show()
        return tId!!
    }

    private fun backBtn() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun loadTripDetails() {
        val userId = tripId.split("%_%")[0]
        val tripNumber = tripId.split("%_%")[1]
        val tripRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("MyRides").child(tripNumber)
        tripRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val fromLocation = dataSnapshot.child("from_location").value as? String ?: ""
                val toLocation = dataSnapshot.child("to_location").value as? String ?: ""
                val date = dataSnapshot.child("date").value as? String ?: ""
                val time = dataSnapshot.child("time").value as? String ?: ""
                val seats = dataSnapshot.child("seats").value as? Long ?: 0
                val pricePerSeat = dataSnapshot.child("price_per_seat").value as? String ?: ""
                val carFacilities = dataSnapshot.child("car_facilities").value as? String ?: ""
                val createdByEmail = dataSnapshot.child("created_by_email").value as? String ?: ""

                // Set trip details to TextViews
                fromLocationTextView.text = "From Location: $fromLocation"
                toLocationTextView.text = "To Location: $toLocation"
                dateTextView.text = "Date: $date"
                timeTextView.text = "Time: $time"
                seatsTextView.text = "Seats: $seats"
                pricePerSeatTextView.text = "Price Per Seat: $pricePerSeat"
                carFacilitiesTextView.text = "Car Facilities: $carFacilities"
                createdByEmailTextView.text = "Created By: $createdByEmail"

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
                Log.e("Firebase", "onCancelled", databaseError.toException())
            }
        })
    }
}
