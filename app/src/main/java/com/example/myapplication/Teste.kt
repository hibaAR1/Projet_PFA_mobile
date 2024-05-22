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
import android.location.Location
import android.net.Uri
import android.util.Log
import android.widget.TextView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var currentLatLng: Pair<Double, Double>? = null
    private val STORAGE_PERMISSION_CODE = 101



    private lateinit var firestore: FirebaseFirestore
    private var imagePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reclamtion2)

        // Initialize Firebase Firestore
        firestore = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        autoCompleteTextView = findViewById(R.id.autoCompleteTextView)
        adapterItems = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, items)
        autoCompleteTextView.setAdapter(adapterItems)

        autoCompleteTextView7 = findViewById(R.id.autoCompleteTextView7)
        adapterRegion = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, itemregion)
        autoCompleteTextView7.setAdapter(adapterRegion)

        messageInputLayout = findViewById(R.id.messageInputLayout)
        editTextTextMultiLine = findViewById(R.id.editTextTextMultiLine)

        messageInputLayoutMess = findViewById(R.id.messageInputLayoutMess)
        editTextTextMultiLine2 = findViewById(R.id.editTextTextMultiLine2)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val addLocationButton: Button = findViewById(R.id.add_location_button) // Add this button to your XML layout
        val locationTextView: TextView = findViewById(R.id.location_text_view) // Add this TextView to your XML layout
        addLocationButton.setOnClickListener {
            getUserLocation(locationTextView)
        }

        autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedItem = adapterItems.getItem(position).toString()
            autoCompleteTextView.setText(selectedItem)
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val selectedRegion = autoCompleteTextView7.text.toString()
            val objet = editTextTextMultiLine.text.toString()
            val message = editTextTextMultiLine2.text.toString()
            saveToFirestore(selectedItem, selectedRegion, userId, objet, message, "", latitude = currentLatLng?.first ?: 0.0, longitude =currentLatLng?.first ?: 0.0)
        }

        autoCompleteTextView7.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            val selectedRegion = parent.getItemAtPosition(position).toString()
            autoCompleteTextView7.setText(selectedRegion)
            val selectedItem = autoCompleteTextView.text.toString()
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val objet = editTextTextMultiLine.text.toString()
            val message = editTextTextMultiLine2.text.toString()
            saveToFirestore(selectedItem, selectedRegion, userId, objet, message, "",latitude = currentLatLng?.first ?: 0.0, longitude =currentLatLng?.first ?: 0.0)
        }

        addLocationButton.setOnClickListener {
            getUserLocation(locationTextView)
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
            if (validateInputs()) {
                val objet = editTextTextMultiLine.text.toString()
                val message = editTextTextMultiLine2.text.toString()
                val random = Random()
                val randomNumber = String.format("%04d", random.nextInt(10000))
                val intent = Intent(this, NumeroActivity::class.java)
                intent.putExtra("objet", objet)
                intent.putExtra("message", message)
                intent.putExtra("uniqueCode", randomNumber)
                intent.putExtra("imagePath", imagePath)
                startActivity(intent)

                uploadImageToImgBB(imagePath)

                val selectedItem = autoCompleteTextView.text.toString()
                val selectedRegion = autoCompleteTextView7.text.toString()
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                saveToFirestore(selectedItem, selectedRegion, userId, objet, message, randomNumber, latitude = currentLatLng?.first ?: 0.0, longitude =currentLatLng?.first ?: 0.0 )
            } else {
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

    private fun getUserLocation(locationTextView: TextView) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val latitude = 37.4219983 // Your desired latitude
                val longitude = -122.084 // Your desired longitude
                currentLatLng = Pair(it.latitude, it.longitude) // Store the location in the variable
                locationTextView.text = "Location: ${currentLatLng?.first}, ${currentLatLng?.second}"
                Toast.makeText(this, "Location: ${currentLatLng?.first}, ${currentLatLng?.second}", Toast.LENGTH_LONG).show()
                saveToFirestore(
                    selectedItem = "", // Remplace par la valeur appropriée
                    selectedRegion = "", // Remplace par la valeur appropriée
                    userId = "", // Remplace par la valeur appropriée
                    objet = "", // Remplace par la valeur appropriée
                    message = "", // Remplace par la valeur appropriée
                    uniqueCode = "", // Remplace par la valeur appropriée
                    latitude = latitude,
                    longitude = longitude
                )
            } ?: run {
                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permissions granted, you can proceed with the action that requires permission
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkStoragePermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            STORAGE_PERMISSION_CODE
        )
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
        if (checkStoragePermissions()) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        } else {
            requestStoragePermissions()
        }
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
                    data?.data?.let { selectedImageUri ->
                        val selectedImagePath = getRealPathFromURI(selectedImageUri)
                        if (selectedImagePath != null) {
                            val file = File(selectedImagePath)
                            if (file.canRead()) {
                                imagePath = selectedImagePath
                                Log.d("ImagePath", "Image selected from gallery: $imagePath")
                            } else {
                                Toast.makeText(this, "Cannot access selected image", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } ?: run {
                        Toast.makeText(this, "Failed to get image from gallery", Toast.LENGTH_SHORT).show()
                    }
                }

                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as? Bitmap
                    imageBitmap?.let {
                        imagePath = saveImageLocally(it)
                        if (imagePath != null) {
                            Log.d("ImagePath", "Image captured and saved locally: $imagePath")
                        }
                    } ?: run {
                        Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
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
    override fun onDestroy() {
        super.onDestroy()
        // Perform cleanup if necessary
    }


    private fun saveToFirestore(
        selectedItem: String,
        selectedRegion: String,
        userId: String,
        objet: String,
        message: String,
        uniqueCode: String,
        latitude: Double,
        longitude: Double
    ) {
        // Get a reference to the Firestore database
        val db = FirebaseFirestore.getInstance()

        // Create a map to store user data
        val userData = hashMapOf(
            "userId" to userId,
            "selectedItem" to selectedItem,
            "selectedRegion" to selectedRegion,
            "objet" to objet,
            "message" to message,
            "latitude" to latitude, // Ajouter la latitude
            "longitude" to longitude // Ajouter la longitude
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