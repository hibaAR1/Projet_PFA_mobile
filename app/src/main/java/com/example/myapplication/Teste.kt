package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import com.google.firebase.firestore.FirebaseFirestore
import com.example.myapplication.ImgBBApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.textfield.TextInputLayout
import android.Manifest
import android.net.Uri
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.Random
import okhttp3.RequestBody.Companion.asRequestBody

class Teste : AppCompatActivity() {
    private val items = arrayOf(
        "santé",
        "Bourses",
        "Bourses d'excellence",
        "Recrutement/ Stages",
        "Autres réclamations de nature juridique et pédagogique",
        "Obtention de diplôme",
        "Autres réclamations portant sur les différentes étapes du processus des équivalences",
        "Autres"
    ) // Define your array of strings
    private val itemregion = arrayOf(
        "Souss-Massa",
        "Casablanca - Settat",
        "Marrakech - Safi",
        "Fès - Meknès",
        "Oriental",
        "Rabat - Salé - Kénitra",
        "Bni Mellal - Khénifra",
        "Tanger - Tétouan - Al Hoceima"
    )
    private lateinit var autoCompleteTextView: AutoCompleteTextView // Declare AutoCompleteTextView
    private lateinit var autoCompleteTextView7: AutoCompleteTextView
    private lateinit var adapterItems: ArrayAdapter<String>
    private lateinit var adapterRegion: ArrayAdapter<String>
    private lateinit var editTextTextMultiLine: EditText
    private lateinit var messageInputLayout: TextInputLayout
    private lateinit var messageInputLayoutMess: TextInputLayout
    private lateinit var editTextTextMultiLine2: EditText
    private val PICK_IMAGE_REQUEST = 1
    private val REQUEST_IMAGE_CAPTURE = 2
    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    private val CAMERA_PERMISSION = Manifest.permission.CAMERA

    private lateinit var firestore: FirebaseFirestore
    private var imagePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reclamtion2)

        // Initialize Firebase Firestore
        firestore = FirebaseFirestore.getInstance()

        autoCompleteTextView =
            findViewById(R.id.autoCompleteTextView) // Initialize autoCompleteTextView
        adapterItems = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            items
        ) // Initialize ArrayAdapter
        autoCompleteTextView.setAdapter(adapterItems)

        autoCompleteTextView7 =
            findViewById(R.id.autoCompleteTextView7) // Initialize ArrayAdapter for region
        adapterRegion = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            itemregion
        ) // Initialize ArrayAdapter
        autoCompleteTextView7.setAdapter(adapterRegion)

        messageInputLayout = findViewById(R.id.messageInputLayout)
        editTextTextMultiLine = findViewById(R.id.editTextTextMultiLine)

        messageInputLayoutMess = findViewById(R.id.messageInputLayoutMess)
        editTextTextMultiLine2 = findViewById(R.id.editTextTextMultiLine2)

        autoCompleteTextView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val selectedItem = adapterItems.getItem(position).toString()
                autoCompleteTextView.setText(selectedItem) // Set the text of AutoCompleteTextView to the selected item

                // Get the current user's ID
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                val selectedRegion = autoCompleteTextView7.text.toString()
                val objet = editTextTextMultiLine.text.toString() // Retrieve the text from EditText
                val message = editTextTextMultiLine2.text.toString()

                // Save selected item to Firestore
                saveToFirestore(selectedItem, selectedRegion, userId, objet, message, "")
            }

        // Handle region selection
        autoCompleteTextView7.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                val selectedRegion = parent.getItemAtPosition(position).toString()
                autoCompleteTextView7.setText(selectedRegion) // Set the text of AutoCompleteTextView to the selected region
                val selectedItem = autoCompleteTextView.text.toString()
                // Get the current user's ID
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                val objet = editTextTextMultiLine.text.toString() // Retrieve the text from EditText
                val message = editTextTextMultiLine2.text.toString()

                // Save selected region to Firestore
                saveToFirestore(selectedItem, selectedRegion, userId, objet, message, "")
            }
        val pickImageButton: Button = findViewById(R.id.button2)
        pickImageButton.setOnClickListener {
            pickImage()
        }
        val redirectToReclamationButton: Button = findViewById(R.id.Retour)
        redirectToReclamationButton.setOnClickListener {
            val intent = Intent(this, ReclamtionActivity::class.java)
            startActivity(intent)
        }

        val redirectToReclamationButton2: Button = findViewById(R.id.suivant)
        redirectToReclamationButton2.setOnClickListener {
            // Vérifiez si tous les champs sont remplis
            if (validateInputs()) {
                // Tous les champs sont remplis, récupérez les valeurs des champs objet et message
                val objet = editTextTextMultiLine.text.toString()
                val message = editTextTextMultiLine2.text.toString()
                val random = Random()
                val randomNumber = String.format("%04d", random.nextInt(10000))
                Log.d("UniqueCode", "Random Number: $randomNumber")
                // Créez l'intent pour passer à l'étape suivante
                val intent = Intent(this, NumeroActivity::class.java)
                // Ajoutez les valeurs des champs objet et message à l'intent en tant qu'extra
                intent.putExtra("objet", objet)
                intent.putExtra("message", message)
                intent.putExtra("uniqueCode", randomNumber)
                intent.putExtra("imagePath", imagePath)
                // Lancez l'activité suivante avec l'intent
                startActivity(intent)

                // Upload the image to imgBB
                uploadImageToImgBB(imagePath)

                // Après avoir vérifié que tous les champs sont remplis, enregistrez les données dans Firestore
                // Récupérez les autres valeurs nécessaires
                val selectedItem = autoCompleteTextView.text.toString()
                val selectedRegion = autoCompleteTextView7.text.toString()
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                // Sauvegardez les données dans Firestore
                saveToFirestore(selectedItem, selectedRegion, userId, objet, message, randomNumber)
            } else {
                // Affichez un message d'erreur indiquant que tous les champs doivent être remplis
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateInputs(): Boolean {
        // Vérifiez si les champs nécessaires sont remplis
        return autoCompleteTextView.text.isNotEmpty() &&
                autoCompleteTextView7.text.isNotEmpty() &&
                editTextTextMultiLine2.text.isNotEmpty()
    }

    private fun pickImage() {
        // Check if the camera permission is granted
        if (ContextCompat.checkSelfPermission(
                this,
                CAMERA_PERMISSION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the camera permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(CAMERA_PERMISSION),
                CAMERA_PERMISSION_REQUEST_CODE
            )
            return
        }
        val options = arrayOf("Take Photo", "Choose from Gallery")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose an option")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> takePhoto()
                1 -> chooseFromGallery()
            }
        }
        builder.show()
    }

    private fun chooseFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun takePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    val selectedImageUri = data?.data
                    if (selectedImageUri != null) {
                        val selectedImagePath = getRealPathFromURI(selectedImageUri)
                        if (selectedImagePath != null) {
                            imagePath = selectedImagePath
                            Log.d("ImagePath", "Image selected from gallery: $imagePath")
                        }
                    }
                }

                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    imagePath = saveImageLocally(imageBitmap)
                    if (imagePath != null) {
                        Log.d("ImagePath", "Image captured and saved locally: $imagePath")
                    }
                }
            }
        }
    }

    private fun getRealPathFromURI(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                return it.getString(columnIndex)
            }
        }
        return null
    }

    private fun saveImageLocally(bitmap: Bitmap): String? {
        val filename = "Image_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        val imageFile = File(getExternalFilesDir(null), filename)
        return try {
            fos = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            Toast.makeText(this, "Image saved locally", Toast.LENGTH_SHORT).show()
            imageFile.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save image locally", Toast.LENGTH_SHORT).show()
            null
        } finally {
            fos?.close()
        }
    }

    private fun saveToFirestore(
        selectedItem: String,
        selectedRegion: String,
        userId: String,
        objet: String,
        message: String,
        uniqueCode: String
    ) {
        // Get a reference to the Firestore database
        val db = FirebaseFirestore.getInstance()

        // Create a map to store user data
        val userData = hashMapOf(
            "userId" to userId,
            "selectedItem" to selectedItem,
            "selectedRegion" to selectedRegion,
            "objet" to objet,
            "message" to message
        )

        // Set user data in Firestore
        db.collection("users").add(userData)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Item added to Firestore for user $userId", Toast.LENGTH_SHORT)
                    .show()
                // Handle success, if needed
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error adding item to Firestore: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

        // Create a map to store reclamation code data
        val codeReclamationData = hashMapOf(
            "userId" to userId,
            "uniqueCode" to uniqueCode
        )

        // Add reclamation code data to the "code de reclamation" collection
        db.collection("code de reclamation").add(codeReclamationData)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Reclamation code added to Firestore for user $userId", Toast.LENGTH_SHORT)
                    .show()
                // Handle success, if needed
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error adding reclamation code to Firestore: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    // Function to upload image to imgBB
    private fun uploadImageToImgBB(imagePath: String?) {
        if (imagePath == null) {
            Log.e("ImgBBUpload", "Image path is null. Upload aborted.")
            return
        }

        val file = File(imagePath)
        if (!file.exists()) {
            Log.e("ImgBBUpload", "Image file does not exist. Upload aborted.")
            return
        }

        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

        val imgBBService = ImgBBApiClient.imgBBService
        val apiKey = "d1d8c9be1ab039c31e60810bd0eb04a0"

        // Make the API call to upload the image
        val call = ImgBBApiClient.imgBBService.uploadImage(imagePart, apiKey)

// Enqueue the call with a callback
        call.enqueue(object : Callback<ImgBBResponse> {
            override fun onResponse(call: Call<ImgBBResponse>, response: Response<ImgBBResponse>) {
                if (response.isSuccessful) {
                    val imageUrl = response.body()?.data?.url
                    val thumbUrl = response.body()?.data?.thumb?.url
                    // Handle successful response, e.g., save the image URL to Firestore
                    Log.d("ImgBBUpload", "Image uploaded successfully. Image URL: $imageUrl, Thumbnail URL: $thumbUrl")
                } else {
                    // Handle unsuccessful response
                    Log.e("ImgBBUpload", "Failed to upload image. Error: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ImgBBResponse>, t: Throwable) {
                // Handle failure
                Log.e("ImgBBUpload", "Upload failed: ${t.message}")
            }
        })
    }
}
