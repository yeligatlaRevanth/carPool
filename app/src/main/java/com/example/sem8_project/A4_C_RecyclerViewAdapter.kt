package com.example.sem8_project

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class A4_C_RecyclerViewAdapter(private val trips: List<A4_C_Trip_Details>) : RecyclerView.Adapter<A4_C_RecyclerViewAdapter.TripViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.a4_item_trip, parent, false)
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
        private val nSeats: TextView = itemView.findViewById(R.id.nSeats)
        private val tripCreatedBy: TextView = itemView.findViewById(R.id.tripUsername)
        private val tripPricePerSeat: TextView = itemView.findViewById(R.id.tripPrice)
        private val joinButton: Button = itemView.findViewById(R.id.joinButton)

        fun bind(trip: A4_C_Trip_Details) {
            tripFromLocation.text = trip.fromLocation
            tripToLocation.text = trip.toLocation
            tripDate.text = trip.date
            tripTime.text = trip.time
            tripCreatedBy.text = trip.createdBy
            tripPricePerSeat.text ="Price: ${trip.pricePerSeat.toString()}"
            nSeats.text = "Seats: ${trip.seats.toString()}"

            joinButton.setOnClickListener {
                val intent = Intent(itemView.context, A7_TripPage::class.java)
                Log.d("tripId", trip.uniqueId)
                intent.putExtra("tripId", trip.uniqueId) // Replace "uniqueId" with the actual field in A4_C_Trip_Details
                itemView.context.startActivity(intent)
            }
        }
    }
}
