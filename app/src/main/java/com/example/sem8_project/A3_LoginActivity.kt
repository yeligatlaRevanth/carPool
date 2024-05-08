package com.example.sem8_project

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import java.util.regex.Pattern

class A3_LoginActivity : AppCompatActivity() {
    lateinit var edt_login_email: EditText
    lateinit var edt_login_pwd: EditText
    lateinit var btn_login: Button
    lateinit var auth: FirebaseAuth
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_a3_login)

        edt_login_email = findViewById(R.id.edt_login_uemail)
        edt_login_pwd = findViewById(R.id.edt_login_upwd)
        btn_login = findViewById(R.id.btn_login)


        auth = FirebaseAuth.getInstance()

        btn_login.setOnClickListener {
            val email = edt_login_email.text.toString()
            val pwd = edt_login_pwd.text.toString()
            if(performChecks(email,pwd))
            {
                login(email,pwd)
            }

        }
    }

    fun performChecks(email: String, pwd : String) : Boolean
    {
        val pattern : Pattern = Patterns.EMAIL_ADDRESS
        if(!pattern.matcher(email).matches())
        {
            Toast.makeText(this, "Incorrect Email Address", Toast.LENGTH_SHORT).show()
            return false
        }

        if(pwd.length < 7) {
            Toast.makeText(this, "Password length should be atleast 7", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    fun login(email:String, pass:String)
    {
        auth.signInWithEmailAndPassword(email, pass).
        addOnCompleteListener(this) {
            if (it.isSuccessful) {
                Toast.makeText(this, "Successfully LoggedIn", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, A4_HomePage::class.java)

                startActivity(intent)
            } else
                Toast.makeText(this, "Log In failed ", Toast.LENGTH_SHORT).show()
        }
    }
}