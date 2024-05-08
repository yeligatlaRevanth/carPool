package com.example.sem8_project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.animation.AlphaAnimation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.TextView

class A1_SplashScreen : AppCompatActivity() {
    private lateinit var p1ImageView: ImageView
    private lateinit var p2ImageView: ImageView
    private lateinit var p3ImageView: ImageView
    private lateinit var p4ImageView: ImageView
    private lateinit var p5ImageView: ImageView
    private lateinit var p6ImageView: ImageView

    private lateinit var txtV: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_a1_splash_screen)

        p1ImageView = findViewById(R.id.p1)
        p2ImageView = findViewById(R.id.p2)
        p3ImageView = findViewById(R.id.p3)
        p4ImageView = findViewById(R.id.p4)
        p5ImageView = findViewById(R.id.p5)
        p6ImageView = findViewById(R.id.p6)

        txtV = findViewById(R.id.txtV)
        animateTextView(txtV)
        moveToCenterAndGoToNextActivity()

    }

    private fun moveToCenterAndGoToNextActivity() {
        animateImageView(p1ImageView, -80f, 0f)
        animateImageView(p2ImageView, 80f, 0f)
        animateImageView(p3ImageView, -120.0f, 240.0f)
        animateImageView(p4ImageView, 80.0f, -240.0f)
        animateImageView(p5ImageView, -80.0f, -240.0f)
        animateImageView(p6ImageView, 80.0f, 350.0f)

        val delayMillis = 2000L
        Handler().postDelayed({
            val intent = Intent(this@A1_SplashScreen, A2_SignUp::class.java)
            startActivity(intent)
            finish()
        }, delayMillis)
    }

    private fun animateImageView(imageView: ImageView, toX: Float,  toY: Float) {
        val translateAnimation = TranslateAnimation(0f, toX, 0f, toY)
        translateAnimation.duration = 2000
        translateAnimation.fillAfter = true

        imageView.startAnimation(translateAnimation)
    }
    private fun animateTextView(textView: TextView) {
        val alphaAnimation = AlphaAnimation(0f, 1f)
        alphaAnimation.duration = 1000
        alphaAnimation.fillAfter = true

        textView.startAnimation(alphaAnimation)
    }
}