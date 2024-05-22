package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityContactBinding
import com.google.firebase.firestore.FirebaseFirestore

class ContactActivity : AppCompatActivity() {
    private lateinit var binding: ActivityContactBinding

    private val items = arrayOf(
        "Citoyen",
        "Entreprise",
        "Association",
        "Professions libérales",
        "Autres",
        "Marocain résidant à l'étranger (MRE)"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the AutoCompleteTextView and ArrayAdapter
        val autoCompleteTextView: AutoCompleteTextView = findViewById(R.id.autoCompleteTextView)
        val adapterItems = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            items
        )
        autoCompleteTextView.setAdapter(adapterItems)

        // Get references to the EditText and Button
        val editTextEmail: EditText = findViewById(R.id.editTextemail)
        val editTextNom: EditText = findViewById(R.id.editTextnom)
        val editTextPrenom: EditText = findViewById(R.id.editTextprenom)
        val editTextTelf: EditText = findViewById(R.id.editTexttelf)
        val editTextAdr1: EditText = findViewById(R.id.editTextadr1)
        val editTextAdr2: EditText = findViewById(R.id.editTextadr2)
        val editTextVille: EditText = findViewById(R.id.editTextville)
        val editTextCodePostal: EditText = findViewById(R.id.edittextcodepostal)
        val editTextPays: EditText = findViewById(R.id.edittextpays)
        val buttonSuivant: Button = findViewById(R.id.button_s)

        // Initialize Firestore
        val db = FirebaseFirestore.getInstance()

        // Set click listener for the button
        buttonSuivant.setOnClickListener {
            val selectedItem = autoCompleteTextView.text.toString()
            val email = editTextEmail.text.toString()
            val nom = editTextNom.text.toString()
            val prenom = editTextPrenom.text.toString()
            val telf = editTextTelf.text.toString()
            val adr1 = editTextAdr1.text.toString()
            val adr2 = editTextAdr2.text.toString()
            val ville = editTextVille.text.toString()
            val codePostal = editTextCodePostal.text.toString()
            val pays = editTextPays.text.toString()

            // Validate inputs
            if (selectedItem.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Veuillez entrer un email valide", Toast.LENGTH_SHORT).show()
            } else if (!telf.matches(Regex("^\\+[0-9]{10}\$"))) {
            Toast.makeText(
                this,
                "Veuillez entrer un numéro de téléphone valide",
                Toast.LENGTH_SHORT
            ).show()
        } else {
                // Create a new contact with a map
                val contact = hashMapOf(
                    "selectedItem" to selectedItem,
                    "email" to email,
                    "nom" to nom,
                    "prenom" to prenom,
                    "telf" to telf,
                    "adr1" to adr1,
                    "adr2" to adr2,
                    "ville" to ville,
                    "codePostal" to codePostal,
                    "pays" to pays
                )

                // Add a new document with a generated ID
                db.collection("contacts")
                    .add(contact)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Contact ajouté avec succès", Toast.LENGTH_SHORT).show()
                        // Create an intent to start the next activity
                        val intent = Intent(this, Teste::class.java)
                        startActivity(intent)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Erreur lors de l'ajout du contact: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
