package com.example.sem8_project
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.Locale

class A5_UserProfile : AppCompatActivity() {
    private lateinit var edt_uname: EditText
    private lateinit var edt_age: EditText
    private lateinit var edt_phoneNum: EditText
    private lateinit var edt_loc: EditText
    private lateinit var edt_email: EditText
    private lateinit var edt_trips: EditText
    private lateinit var btn_updateUser: Button
    private lateinit var imgProfile: ImageView
    private val PICK_IMAGE = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var imgVTxt: TextView
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    private lateinit var uID: String
    private var selectedImageUri: Uri? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_a5_user_profile)

        edt_uname = findViewById(R.id.edt_c_uname)
        edt_age = findViewById(R.id.edt_c_age)
        edt_phoneNum = findViewById(R.id.edt_c_phoneNum)
        edt_loc = findViewById(R.id.edt_c_loc)
        btn_updateUser = findViewById(R.id.btn_updateUser)
        edt_email = findViewById(R.id.edt_c_email)
        edt_trips = findViewById(R.id.edt_c_trips)
        imgVTxt = findViewById(R.id.myImageViewText)
        imgProfile = findViewById(R.id.imageProfile)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Request location permissions
        requestLocationPermissions()

        // Get the user's current location
        getUserLocation()
        getCurrentUser()
        showUserData()
        getUserLocation()

        btn_updateUser.setOnClickListener {
            val uName = edt_uname.text.toString()
            val uAge = edt_age.text.toString()
            val uNum = edt_phoneNum.text.toString()
            val uLoc = edt_loc.text.toString()

            if(uNum.length != 10)
            {
                Toast.makeText(this,"Incorrect Mobile Number", Toast.LENGTH_SHORT).show()
            }
            else
            {
                updateUserData(
                    newUsername = uName,
                    newUserNumber = uNum,
                    newUserLoc = uLoc,
                    newUserAge = uAge
                )

            }

        }

        imgProfile.setOnClickListener {
            setUserImage()
        }
    }
    private fun requestLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }
    @SuppressLint("MissingPermission")
    private fun getUserLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    // Now you have the latitude and longitude
                    Log.d(TAG, "Latitude: $latitude, Longitude: $longitude")
                    // You can use these values to get the city using Geocoder
                    val geocoder = Geocoder(this@A5_UserProfile, Locale.getDefault())
                    try {
                        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                        if (addresses!!.isNotEmpty()) {
                            val cityName = addresses[0].locality
                            // Set the city name to the location EditText
                            edt_loc.setText(cityName)
                        } else {
                            Log.e(TAG, "No address found for the location")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error getting location address: ${e.message}")
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting location: ${e.message}")
            }
    }

    private fun setUserImage() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            selectedImageUri?.let {
                imgProfile.setImageURI(selectedImageUri)
                imgProfile.alpha = 1f
                imgVTxt.visibility = View.INVISIBLE
            }
        }
    }

    private fun showUserData() {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(uID)
        databaseReference.get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val userData = dataSnapshot.value as? Map<String, Any>
                userData?.let { data ->
                    val username = data["userName"] as? String
                    val age = data["userAge"] as? String
                    val phoneNumber = data["userNumber"] as? String
                    val location = data["userLoc"] as? String
                    val email = data["userEmail"] as? String
                    val trips = data["numTrips"] as? String

                    edt_uname.setText(username)
                    edt_age.setText(age)
                    edt_email.setText(email)
                    edt_trips.setText(trips)
                    edt_phoneNum.setText(phoneNumber)
                    edt_loc.setText(location)

                    // Load user image using Glide if it exists in Firebase Storage
                    val imageFileName = "$uID%_%"
                    val storageRef = FirebaseStorage.getInstance().reference.child("UserImages")
                    storageRef.listAll().addOnSuccessListener { listResult ->
                        listResult.items.forEach { item ->
                            if (item.name.startsWith(imageFileName)) {
                                item.downloadUrl.addOnSuccessListener { imageUrl ->
                                    Glide.with(this@A5_UserProfile)
                                        .load(imageUrl)
                                        .placeholder(R.drawable.userimage_placeholder) // Placeholder image while loading
                                        .into(imgProfile)
                                }
                            }
                        }
                    }.addOnFailureListener { e ->
                        Log.e(TAG, "Error loading user image", e)
                    }
                }
            } else {
                Log.d(TAG, "No data found for user: $uID")
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error fetching user data", e)
        }
    }

    private fun getCurrentUser() {
        val mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser

        currentUser?.let { user ->
            uID = user.uid
            Log.d(TAG, "User ID: $uID")
        } ?: run {
            Log.d(TAG, "No user is currently signed in")
        }
    }

    private fun updateUserData(
        newUsername: String,
        newUserNumber: String,
        newUserLoc: String,
        newUserAge: String
    ) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(uID)

        val updates = HashMap<String, Any>()
        updates["userName"] = newUsername
        updates["userNumber"] = newUserNumber
        updates["userLoc"] = newUserLoc
        updates["userAge"] = newUserAge

        selectedImageUri?.let { uri ->
            uploadImage(uri) { imageUrl ->
                imageUrl?.let {
                    updates["imageUrl"] = it
                    updateUserDataInDatabase(databaseReference, updates)
                }
            }
        } ?: run {
            updateUserDataInDatabase(databaseReference, updates)
        }
    }

    private fun uploadImage(imageUri: Uri, onComplete: (String?) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("UserImages").child("$uID%_%${System.currentTimeMillis()}")

        try {
            // Decode bitmap with BitmapFactory.Options to compress it
            val options = BitmapFactory.Options().apply {
                // Set inJustDecodeBounds to true to get the image dimensions without loading it into memory
                inJustDecodeBounds = true
                BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri), null, this)

                // Calculate inSampleSize to reduce the image size while maintaining the aspect ratio
                inSampleSize = calculateInSampleSize(this, 1000, 1000) // Adjust the target width and height as needed
                // Decode bitmap with the calculated inSampleSize
                inJustDecodeBounds = false
            }

            // Compress the image and upload it to Firebase Storage
            contentResolver.openInputStream(imageUri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)?.let { bitmap ->
                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream) // Adjust the compression quality as needed
                    val compressedData = outputStream.toByteArray()

                    imageRef.putBytes(compressedData)
                        .addOnSuccessListener { taskSnapshot ->
                            imageRef.downloadUrl.addOnSuccessListener { uri ->
                                onComplete(uri.toString())
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error uploading compressed image", e)
                            onComplete(null)
                        }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing image", e)
            onComplete(null)
        }
    }

    // Calculate the inSampleSize to reduce the image size while maintaining the aspect ratio
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1
        val height = options.outHeight
        val width = options.outWidth

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }


    private fun updateUserDataInDatabase(databaseReference: DatabaseReference, updates: Map<String, Any>) {
        databaseReference.updateChildren(updates)
            .addOnSuccessListener {
                Log.d(TAG, "Data updated successfully")
                val i = Intent(this, A4_UserDashBoard::class.java)
                startActivity(i)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error updating data", e)
            }
    }
}
