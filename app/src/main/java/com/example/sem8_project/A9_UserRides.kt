package com.example.sem8_project

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class A9_UserRides : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var mDrawer: DrawerLayout? = null
    private var toolbar: Toolbar? = null
    private lateinit var navMenuImageProfile: ImageView
    private lateinit var navMenuNameText: TextView
    private lateinit var rv_requests: RecyclerView
    private lateinit var rv_myRides: RecyclerView
    private lateinit var requestAdapter: A9_RequestsAdapter
    private lateinit var ridesAdapter: A9_MyRides_Adapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_a9_user_rides)

        requestAdapter = A9_RequestsAdapter(mutableListOf())
        ridesAdapter = A9_MyRides_Adapter(mutableListOf())

        rv_requests = findViewById(R.id.rv_requests)
        rv_requests.layoutManager = LinearLayoutManager(this)

        rv_myRides = findViewById(R.id.rv_myrides)
        rv_myRides.layoutManager = LinearLayoutManager(this)
        fetchRideRequests()
        getMyOldRides()


        setHamburgerIcon()


        val nvDrawer: NavigationView = findViewById(R.id.nvView)
        setupDrawerContent(nvDrawer)
        val headerView = nvDrawer.getHeaderView(0)
        navMenuImageProfile = headerView.findViewById(R.id.navmenu_ImageProfile)
        navMenuNameText = headerView.findViewById(R.id.navmenu_nameview)

        loadUserProfileImage()
        loadUserName()


    }

    private fun getMyOldRides() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val userId = user.uid
            val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("OtherRides")

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val rideRequests = mutableListOf<A9_MyRides_Model>()
                    for (rideSnapshot in dataSnapshot.children) {
                        val tripId = rideSnapshot.getValue(String::class.java)
                        if (!tripId.isNullOrBlank()) {
                            // Split the tripId to get user id and trip number
                            val (otherUserId, tripNumber) = tripId.split("%_%")

                            val rideRequest = A9_MyRides_Model(otherUserId, tripNumber)
                            rideRequests.add(rideRequest)
                        }
                    }
                    setupRecyclerViewAgain(rideRequests)
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                    Log.e("Firebase", "onCancelled", databaseError.toException())
                }
            })
        }
    }

    private fun loadUserName() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val userId = user.uid
            val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val userName = dataSnapshot.child("userName").getValue(String::class.java)
                    // Set the user name to the TextView
                    navMenuNameText.text = userName
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle errors
                    Log.e(ContentValues.TAG, "Failed to load user name: ${databaseError.message}")
                }
            })
        }
    }
    private fun loadUserProfileImage() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val userId = user.uid
            val userImageRef = FirebaseStorage.getInstance().getReference("UserImages")

            userImageRef.listAll().addOnSuccessListener { result ->
                result.items.firstOrNull { imageRef ->
                    val parts = imageRef.name.split("%_%")
                    parts.size == 2 && parts[0] == userId
                }?.downloadUrl?.addOnSuccessListener { uri ->
                    // Load the image into ImageView
                    Glide.with(this).load(uri).into(navMenuImageProfile)
                } ?: run {
                    // Handle no matching image found
                    Log.e(ContentValues.TAG, "No profile image found for user: $userId")
                    navMenuImageProfile.setImageURI(Uri.parse("R.id.imageProfile"))
                }
            }.addOnFailureListener { exception ->
                // Handle errors
                Log.e(ContentValues.TAG, "Failed to fetch user profile image: ${exception.message}")
            }
        }
    }
    private fun fetchRideRequests() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val userId = user.uid
            val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("MyRides")

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val rideRequests = mutableListOf<A9_Ride_Request>()

                    for (tripSnapshot in dataSnapshot.children) {
                        for (passengerSnapshot in tripSnapshot.child("Passengers").children) {
                            val status = passengerSnapshot.child("status").getValue(String::class.java)
                            if (status == "pending") { // Only add items with "pending" status
                                val tripId = tripSnapshot.key // This is how you get the tripId
                                val fromLocation = tripSnapshot.child("from_location").getValue(String::class.java)
                                val toLocation = tripSnapshot.child("to_location").getValue(String::class.java)
                                val date = tripSnapshot.child("date").getValue(String::class.java)
                                val time = tripSnapshot.child("time").getValue(String::class.java)
                                val requestUser = passengerSnapshot.child("userId").getValue(String::class.java)

                                val rideRequest = A9_Ride_Request(fromLocation!!, toLocation ?: "", date ?: "", time ?: "", requestUser ?: "")
                                rideRequests.add(rideRequest)
                            }
                        }
                    }

                    // Set up RecyclerView with the fetched ride requests
                    setupRecyclerView(rideRequests)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle errors
                    Log.e(ContentValues.TAG, "Failed to fetch ride requests: ${databaseError.message}")
                }
            })
        }
    }

    private fun setupRecyclerViewAgain(tripsList: List<A9_MyRides_Model>) {
        // Create an instance of the adapter with the list of ride requests
        ridesAdapter = A9_MyRides_Adapter(tripsList.toMutableList())

        // Set the adapter for the RecyclerView

        rv_myRides.adapter = ridesAdapter
    }


    private fun setupRecyclerView(tripsList: List<A9_Ride_Request>) {
        // Create an instance of the adapter with the list of ride requests
        requestAdapter = A9_RequestsAdapter(tripsList.toMutableList())

        // Set the adapter for the RecyclerView

        rv_requests.adapter = requestAdapter
    }


    fun setHamburgerIcon()
    {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
        mDrawer = findViewById(R.id.drawer_layout)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home ->{
                mDrawer?.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        selectDrawerItem(item)
        return true
    }
    fun setupDrawerContent(navigationView: NavigationView) {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            selectDrawerItem(menuItem)
            true
        }
    }

    fun selectDrawerItem(menuItem: MenuItem)
    {
        when(menuItem.itemId){
            R.id.nav_createTrip -> {
                val i = Intent(this, A6_CreateTrip::class.java)
                startActivity(i)
            }
            R.id.nav_userProf ->{
                val i = Intent(this, A5_UserProfile::class.java)
                startActivity(i)

            }
            R.id.nav_logout->{
                showLogoutConfirmationDialog()
            }
            R.id.nav_steps->{
                val i = Intent(this, A9_UserRides::class.java)
                startActivity(i)
            }
        }
        mDrawer?.closeDrawers()
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { dialog, which ->
                // Logout user
                FirebaseAuth.getInstance().signOut()
                // Redirect to login or splash screen
                // For example:
                val intent = Intent(this, A3_LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
