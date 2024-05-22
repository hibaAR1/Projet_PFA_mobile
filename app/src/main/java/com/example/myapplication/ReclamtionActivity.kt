package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityReclamationBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class ReclamtionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReclamationBinding
    private lateinit var logout: FloatingActionButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReclamationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set OnClickListener for the button
        binding.button3.setOnClickListener {
            val intent = Intent(this,ContactActivity::class.java)
            startActivity(intent)
        }

        logout=findViewById(R.id.logout)
        logout.setOnClickListener{
            Firebase.auth.signOut()

            val intent = Intent(this,SignInActivity::class.java)
            startActivity(intent)

            Toast.makeText(this, "Logout successful", Toast.LENGTH_SHORT).show()
        }



    }

}
