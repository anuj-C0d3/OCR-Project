package com.example.ocr

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ocr.databinding.ActivityMainBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.database.FirebaseDatabase
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class MainActivity : AppCompatActivity() {
    val CAMERA_REQUEST_CODE = 100;
    var extractedText :String =""
    private lateinit var bitmap: Bitmap
    private  lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val ref = FirebaseDatabase.getInstance().reference
        if (ContextCompat.checkSelfPermission(this, Manifest
                .permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        }
        binding.SavedDetails.setOnClickListener {
            startActivity(Intent(this,SavedDetailsLIst::class.java))
        }
        // Inside onCreate or onViewCreated
        val name = intent.getStringExtra("name")
        val email = intent.getStringExtra("email")
        val phone = intent.getStringExtra("phone")
        val web = intent.getStringExtra("web")
        val company = intent.getStringExtra("company")
        var notkey = intent.getStringExtra("notkey")

        if (!name.isNullOrEmpty() && !email.isNullOrEmpty() && !phone.isNullOrEmpty()
            && !web.isNullOrEmpty() && !company.isNullOrEmpty() && !notkey.isNullOrEmpty()) {

            binding.name.setText(name)
            binding.email.setText(email)
            binding.phone.setText(phone)
            binding.website.setText(web)
            binding.companyName.setText(company)
        }

        binding.capturebut.setOnClickListener {
            ImagePicker.with(this)
                .crop()	    			//Crop image(Optional), Check Customization for more option
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }
        binding.save.setOnClickListener {
            val email = binding.email.text.toString()
            val name = binding.name.text.toString()
            val companyname = binding.companyName.text.toString()
            val web = binding.website.text.toString()
            val phone = binding.phone.text.toString()

            if (email.isNotEmpty() && name.isNotEmpty() && phone.isNotEmpty()) {
                if (notkey.isNullOrEmpty()) {
                    notkey = ref.child("user").push().key
                }

                notkey?.let { key ->
                    val data = SaveDetailInfo(name, email, companyname, phone, web, key)
                    ref.child("user").child(key).setValue(data)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { error ->
                            Toast.makeText(this, "Failed due to ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                } ?: run {
                    Toast.makeText(this, "Error creating key", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Email, phone and name are required fields.", Toast.LENGTH_SHORT).show()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            val uri: Uri = data?.data!!

            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,uri)
            getTextFromImg(bitmap)
            // Use Uri object instead of File to avoid storage permissions

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }
    @SuppressLint("SuspiciousIndentation")
    fun getTextFromImg(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap,0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image)
            .addOnSuccessListener {
                extext->
                extractedText = extext.text
                val lines = extractedText.lines().map { it.trim() }

                val emailRegex = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
                val phoneRegex = Regex("\\+?\\d{10,15}")
                val websiteRegex = Regex("(www\\.|https?://)[a-zA-Z0-9./]+")

                val possibleNameLines = lines.filter { line ->
                    !emailRegex.containsMatchIn(line) &&
                            !phoneRegex.containsMatchIn(line) &&
                            !websiteRegex.containsMatchIn(line) &&
                            !line.contains("Phone", ignoreCase = true) &&
                            !line.contains("Email", ignoreCase = true) &&
                            !line.contains("www", ignoreCase = true)
                }
                val companyKeywords = listOf("pvt", "ltd", "inc", "llc", "technologies", "solutions", "corporation", "systems")

                var companyLine = lines.find { line ->
                    companyKeywords.any { keyword -> line.lowercase().contains(keyword) } &&
                            !line.contains("@") && !line.matches(Regex("\\+?\\d{10,15}")) // not email or phone
                }

                val name = possibleNameLines[0]

                val email  = emailRegex.find(extractedText)?.value
                val phone = phoneRegex.find(extractedText)?.value
                val website = websiteRegex.find(extractedText)?.value
                 var company  = companyLine
                    binding.name.setText(name)
                    binding.email.setText(email)
                    binding.phone.setText(phone)
                    binding.website.setText(website)
                    binding.companyName.setText(company)
                

            }
            .addOnFailureListener{ error->
                Toast.makeText(this, "Text not extracted due to ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }
}