package com.example.sem8_project

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import android.telephony.SmsManager

// Inside your TripViewHolder class

class A9_RequestsAdapter(private val trips: MutableList<A9_Ride_Request>) : RecyclerView.Adapter<A9_RequestsAdapter.TripViewHolder>() {

    lateinit var otherUserId: String
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.a9_request_item, parent, false)
        return TripViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val trip = trips[position]
        holder.bind(trip)
    }

    override fun getItemCount(): Int {
        return trips.size
    }


    inner class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tripFromLocation: TextView = itemView.findViewById(R.id.tripFromLocation)
        private val tripToLocation: TextView = itemView.findViewById(R.id.tripToLocation)
        private val tripDate: TextView = itemView.findViewById(R.id.tripDate)
        private val tripTime: TextView = itemView.findViewById(R.id.tripTime)
        private val requestUser:TextView = itemView.findViewById(R.id.tripUsername)
        private val btnAccept: Button = itemView.findViewById(R.id.btn_accept)
        private val btnReject: Button = itemView.findViewById(R.id.btn_reject)
        private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        fun bind(trip: A9_Ride_Request) {
            tripFromLocation.text = trip.fromLocation
            tripToLocation.text = trip.toLocation
            tripDate.text = trip.date
            tripTime.text = trip.time
            otherUserId = trip.requestUser
            loadUserName(otherUserId)



            // Set click listener for accept button
            btnAccept.setOnClickListener {
                showConfirmationDialog()
            }

            // Set click listener for reject button
            btnReject.setOnClickListener {
                showRejectConfirmationDialog()
            }
        }
        private fun showConfirmationDialog() {
            val alertDialogBuilder = AlertDialog.Builder(itemView.context)
            alertDialogBuilder.setTitle("Accept Confirmation")
            alertDialogBuilder.setMessage("Are you sure you want to accept this ride request?")
            alertDialogBuilder.setPositiveButton("Yes") { dialog, which ->
                updateStatus("accepted")
            }
            alertDialogBuilder.setNegativeButton("No", null)
            alertDialogBuilder.show()
        }

        private fun showRejectConfirmationDialog() {
            val alertDialogBuilder = AlertDialog.Builder(itemView.context)
            alertDialogBuilder.setTitle("Reject Confirmation")
            alertDialogBuilder.setMessage("Are you sure you want to reject this ride request?")
            alertDialogBuilder.setPositiveButton("Yes") { dialog, which ->
                updateStatus("rejected")
            }
            alertDialogBuilder.setNegativeButton("No", null)
            alertDialogBuilder.show()
        }

        private fun sendSMS(userNumber: String, message: String) {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(userNumber, null, message, null, null)
        }

        private fun loadUserNumber(userId: String, status: String) {
            val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val userNumber = dataSnapshot.child("userNumber").getValue(String::class.java)
                    if (userNumber != null) {
                        val message = if (status == "accepted") {
                            "Your ride request has been accepted."
                        } else {
                            "Your ride request has been rejected."
                        }
                        // Send SMS
                        sendSMS(userNumber, message)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle errors
                    Log.e("A9_RequestsAdapter", "Failed to load user number: ${databaseError.message}")
                }
            })
        }


        private fun updateStatus(status: String) {
            val currentUserId = currentUserId ?: return
            val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId).child("MyRides")

            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (tripSnapshot in dataSnapshot.children) {
                        for (passengerSnapshot in tripSnapshot.child("Passengers").children) {
                            val passengerStatus = passengerSnapshot.child("status").getValue(String::class.java)
                            if (passengerStatus == "pending") {
                                passengerSnapshot.child("status").ref.setValue(status)
                                    .addOnSuccessListener {
                                        // Status updated successfully
                                        Log.d("A9_RequestsAdapter", "Trip status changed to $status")
                                        // Remove the item from the list and notify the adapter
                                        val position = adapterPosition
                                        if (position != RecyclerView.NO_POSITION) {
                                            trips.removeAt(position)
                                            notifyItemRemoved(position)
                                        }
                                        loadUserNumber(otherUserId, status)
                                        val seatsNum = tripSnapshot.child("seats").value
                                        if (status == "accepted") {
                                            // Decrease the available seats count by 1
                                            val updatedSeats = (seatsNum as? Long ?: 0) - 1
                                            tripSnapshot.child("seats").ref.setValue(updatedSeats)
                                                .addOnSuccessListener {
                                                    // Seats updated successfully
                                                    Log.d("A9_RequestsAdapter", "Available seats decremented to $updatedSeats")
                                                }
                                                .addOnFailureListener { e ->
                                                    // Failed to update seats
                                                    Log.e("A9_RequestsAdapter", "Failed to update seats for trip: ${e.message}", e)
                                                }
                                        }

                                    }
                                    .addOnFailureListener { e ->
                                        // Failed to update status
                                        Log.e("A9_RequestsAdapter", "Failed to update status for trip: ${e.message}", e)
                                    }
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                    Log.e("A9_RequestsAdapter", "onCancelled", databaseError.toException())
                }
            })
        }

        private fun loadUserName(userId: String) {
            val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val userName = dataSnapshot.child("userName").getValue(String::class.java)
                    // Set the user name to the TextView
                    requestUser.text = userName
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle errors
                    Log.e("A9_RequestsAdapter", "Failed to load user name: ${databaseError.message}")
                }
            })
        }
    }
}
