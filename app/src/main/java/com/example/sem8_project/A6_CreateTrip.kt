package com.example.sem8_project

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import java.util.Calendar

class A6_CreateTrip : AppCompatActivity() {
    lateinit var edtFromLocation: EditText
    lateinit var edtToLocation: EditText
    lateinit var edtDate: EditText
    lateinit var edtTime: EditText
    lateinit var edtSeats: EditText
    lateinit var edtPricePerSeat: EditText
    lateinit var edtCarFacilities: EditText
    lateinit var btnCreateRide: Button
    lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_a6_create_trip)

        // Initialize views
        edtFromLocation = findViewById(R.id.edt_from_location)
        edtToLocation = findViewById(R.id.edt_to_location)
        edtDate = findViewById(R.id.edt_date)
        edtTime = findViewById(R.id.edt_time)
        edtSeats = findViewById(R.id.edt_seats)
        edtPricePerSeat = findViewById(R.id.edt_price_per_seat)
        edtCarFacilities = findViewById(R.id.edt_car_facilities)
        btnCreateRide = findViewById(R.id.btn_create_ride)

        // Initialize Firebase database reference
        val currentUser = FirebaseAuth.getInstance().currentUser
        val currentUserId = currentUser?.uid ?: return
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId).child("MyRides")

        // Set click listeners
        edtDate.setOnClickListener { showDatePicker() }
        edtTime.setOnClickListener { showTimePicker() }
        btnCreateRide.setOnClickListener { createRide() }
    }

    private fun showTimePicker() {
        // Get current time
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        // Create a time picker dialog
        val timePickerDialog = TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { view, selectedHour, selectedMinute ->
                // Handle time selection
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                // Assuming edtTime is your EditText for displaying time
                edtTime.setText(formattedTime)
            },
            hour,
            minute,
            false
        )

        // Show the time picker dialog
        timePickerDialog.show()
    }



    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                val formattedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
                edtDate.setText(formattedDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }
    private fun loadTripList() {

        val i = Intent(this, A4_UserDashBoard::class.java)
        startActivity(i)
        finish()
    }

    private fun createRide() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val currentUserId = currentUser?.uid ?: return
        val currentUserEmail = currentUser?.email ?: return

        val fromLocation = edtFromLocation.text.toString()
        val toLocation = edtToLocation.text.toString()
        val date = edtDate.text.toString()
        val time = edtTime.text.toString()
        val seats = edtSeats.text.toString()
        val pricePerSeat = edtPricePerSeat.text.toString()
        val carFacilities = edtCarFacilities.text.toString()


        // Validation
        if (fromLocation.isEmpty() || toLocation.isEmpty() || date.isEmpty() || time.isEmpty() || seats.isEmpty() || pricePerSeat.isEmpty() || carFacilities.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the current count of trips and increment it for the new trip
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tripCount = snapshot.childrenCount.toInt() + 1
                val rideData = hashMapOf(
                    "from_location" to fromLocation,
                    "to_location" to toLocation,
                    "date" to date,
                    "time" to time,
                    "seats" to seats.toInt(),
                    "price_per_seat" to pricePerSeat.toString(),
                    "car_facilities" to carFacilities,
                    "created_by_email" to currentUserEmail,
                    "trip_id" to "$currentUserId%_%Trip${tripCount}"
                )

                // Save ride data in Firebase under the next child
                databaseReference.child("Trip$tripCount").setValue(rideData)
                    .addOnSuccessListener {
                        Toast.makeText(this@A6_CreateTrip, "Ride created successfully", Toast.LENGTH_SHORT).show()
                        loadTripList()
                        finish() // Close the activity
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this@A6_CreateTrip, "Failed to create ride: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@A6_CreateTrip, "Failed to get trip count: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
