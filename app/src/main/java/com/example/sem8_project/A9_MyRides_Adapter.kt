package com.example.sem8_project

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class A9_MyRides_Adapter(private val myRides: List<A9_MyRides_Model>) : RecyclerView.Adapter<A9_MyRides_Adapter.MyRideViewHolder>() {

    inner class MyRideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fromLocationTextView: TextView = itemView.findViewById(R.id.tripFromLocation)
        val toLocationTextView: TextView = itemView.findViewById(R.id.tripToLocation)
        val dateTextView: TextView = itemView.findViewById(R.id.tripDate)
        val timeTextView: TextView = itemView.findViewById(R.id.tripTime)
        val seatsTextView: TextView = itemView.findViewById(R.id.nSeats)
        val pricePerSeatTextView: TextView = itemView.findViewById(R.id.tripPrice)
        val createdByEmailTextView: TextView = itemView.findViewById(R.id.tripUsername)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyRideViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.a9_myrides_item, parent, false)
        return MyRideViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyRideViewHolder, position: Int) {
        val currentRide = myRides[position]
        val userId = currentRide.userId
        val tripId = currentRide.tripId

        Log.d("mycheck", "$userId \n $tripId")
        // Fetch additional details from the database based on userId and tripId
        val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("MyRides").child(tripId)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val fromLocation = dataSnapshot.child("from_location").value as? String ?: ""
                val toLocation = dataSnapshot.child("to_location").value as? String ?: ""
                val date = dataSnapshot.child("date").value as? String ?: ""
                val time = dataSnapshot.child("time").value as? String ?: ""
                val seats = dataSnapshot.child("seats").value as? String ?: ""
                val pricePerSeat = dataSnapshot.child("price_per_seat").value as? String ?: ""
                val carFacilities = dataSnapshot.child("car_facilities").value as? String ?: ""
                val createdByEmail = dataSnapshot.child("created_by_email").value as? String ?: ""

                // Bind fetched data to the views
                holder.fromLocationTextView.text = "From Location: $fromLocation"
                holder.toLocationTextView.text = "To Location: $toLocation"
                holder.dateTextView.text = "Date: $date"
                holder.timeTextView.text = "Time: $time"
                holder.seatsTextView.text = "Seats: $seats"
                holder.pricePerSeatTextView.text = "Price Per Seat: $pricePerSeat"
                holder.createdByEmailTextView.text = "Created By: $createdByEmail"
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }

    override fun getItemCount() = myRides.size
}
