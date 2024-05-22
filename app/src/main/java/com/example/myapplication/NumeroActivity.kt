package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityNumeroBinding
import com.google.firebase.auth.FirebaseAuth

class NumeroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNumeroBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityNumeroBinding.inflate(layoutInflater)
        setContentView(binding.root)
// Retrieve the unique code from the intent extras
        val uniqueCode = intent.getStringExtra("uniqueCode")

        // Set the unique code to the TextView
        val textViewUniqueCode: TextView = findViewById(R.id.textViewUniqueCode)
        textViewUniqueCode.text = uniqueCode
// Récupérer l'utilisateur actuellement connecté à Firebase Auth
        val user = FirebaseAuth.getInstance().currentUser

        // Récupérer l'e-mail de l'utilisateur
        val email = user?.email

        // Trouver votre TextView
        val textViewEmail: TextView = findViewById(R.id.textView2)

        // Définir l'e-mail de l'utilisateur comme texte de votre TextView
        textViewEmail.text = email
        val retourAccueilButton: Button = findViewById(R.id.button4)

        // Ajoutez un écouteur de clic au bouton
        retourAccueilButton.setOnClickListener {
            // Créez une intention pour démarrer l'activité de l'accueil
            val intent = Intent(this, ReclamtionActivity::class.java) // Remplacez HomeActivity par le nom de votre activité d'accueil
            startActivity(intent) // Démarrer l'activité de l'accueil
        }
    }
}
