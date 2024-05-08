package com.example.sem8_project

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Email
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import java.util.regex.Pattern

class A2_SignUp : AppCompatActivity() {
    lateinit var btn_signup: Button
    lateinit var edt_uemail: EditText
    lateinit var edt_upwd: EditText
    lateinit var edt_upwd1: EditText
    private lateinit var auth: FirebaseAuth

    //Database Variables
    lateinit var firebaseDatabase : FirebaseDatabase
    lateinit var databaseReference: DatabaseReference

    lateinit var userInfo: A2_UserProfile
    lateinit var btn_toLogin: Button
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_a2_sign_up)

        btn_signup = findViewById(R.id.btn_signup)
        edt_uemail = findViewById(R.id.edt_uemail)
        edt_upwd = findViewById(R.id.edt_upwd)
        edt_upwd1 = findViewById(R.id.edt_upwd1)

        auth = Firebase.auth
        firebaseDatabase= com.google.firebase.ktx.Firebase.database
        databaseReference = firebaseDatabase.getReference("Users")


        btn_toLogin = findViewById(R.id.btn_toLogin)
        btn_signup.setOnClickListener {
            val email = edt_uemail.text.toString()
            val pwd = edt_upwd.text.toString()
            val pwd1 = edt_upwd1.text.toString()

            if(performChecks(email, pwd, pwd1))
            {
                createUser(email,pwd)
            }
        }

        btn_toLogin.setOnClickListener {
            val i = Intent(this@A2_SignUp, A3_LoginActivity::class.java)
            startActivity(i)
        }
    }

    fun storeUserToDB(email: String)
    {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val myUser = A2_UserProfile(userId, null, email, null,null,null,0)

        databaseReference.child(userId!!).setValue(myUser)
            .addOnCompleteListener {
                edt_uemail.text.clear()
                edt_upwd.text.clear()
                edt_upwd1.text.clear()


            }.addOnFailureListener { err ->
                Toast.makeText(this, "Error ${err.message}", Toast.LENGTH_LONG).show()
            }

    }

    fun performChecks(email: String, pwd : String, pwd1 : String) : Boolean
    {
        val pattern : Pattern = Patterns.EMAIL_ADDRESS
        if(!pattern.matcher(email).matches())
        {
            Toast.makeText(this, "Incorrect Email Format",Toast.LENGTH_SHORT).show()
            return false
        }
        if(pwd != pwd1)
        {
            Toast.makeText(this, "Password Mismatch", Toast.LENGTH_SHORT).show()
            return false
        }
        if(pwd.length < 7) {
            Toast.makeText(this, "Password length should be atleast 7", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    fun createUser(email: String, pwd: String)
    {
        auth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener {
            if(it.isSuccessful)
            {
                Toast.makeText(this, "Successfully Signed Up", Toast.LENGTH_SHORT).show()
                storeUserToDB(email)
                login(email,pwd)
            }
            else{
                Toast.makeText(this, "Singed Up Failed!"+it.exception, Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun login(email:String, pass:String)
    {
        auth.signInWithEmailAndPassword(email, pass).
        addOnCompleteListener(this) {
            if (it.isSuccessful) {
                val intent = Intent(this, A5_UserProfile::class.java)
                startActivity(intent)
            } else
                Toast.makeText(this, "Log In failed ", Toast.LENGTH_SHORT).show()
        }
    }

}