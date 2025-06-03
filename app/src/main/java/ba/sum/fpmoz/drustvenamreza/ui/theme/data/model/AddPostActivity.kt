package ba.sum.fpmoz.drustvenamreza.ui.theme.data

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ba.sum.fpmoz.drustvenamreza.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class AddPostActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var descriptionEditText: EditText
    private lateinit var addPostBtn: Button

    private val db = FirebaseFirestore.getInstance()
    private var selectedImageUri: Uri? = null

    private val PICK_IMAGE_REQUEST = 1001

    // OVDJE STAVI SVOJ ImageKit private API key
    private val imageKitPrivateKey = "private_iXm5gCe9WCLcnqWkMMIyRbo4usw="

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Nova objava"

        imageView = findViewById(R.id.imageView)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        addPostBtn = findViewById(R.id.addPostBtn)

        imageView.setOnClickListener {
            openGallery()
        }

        addPostBtn.setOnClickListener {
            addPost()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                selectedImageUri = uri
                imageView.setImageURI(uri)
            }
        }
    }

    private fun addPost() {
        val description = descriptionEditText.text.toString().trim()
        if (description.isEmpty()) {
            Toast.makeText(this, "Unesite opis objave", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri != null) {
            uploadImageToImageKit(selectedImageUri!!) { imageUrl ->
                savePostToFirestore(description, imageUrl)
            }
        } else {
            savePostToFirestore(description, null)
        }
    }

    private fun uploadImageToImageKit(uri: Uri, onSuccess: (String) -> Unit) {
        val uploadUrl = "https://upload.imagekit.io/api/v1/files/upload"

        val inputStream = contentResolver.openInputStream(uri)
        val imageBytes = inputStream?.readBytes()
        inputStream?.close()

        if (imageBytes == null) {
            runOnUiThread {
                Toast.makeText(this, "Greška kod čitanja slike", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "image.jpg", imageBytes.toRequestBody("image/*".toMediaTypeOrNull()))
            .addFormDataPart("fileName", "post_${System.currentTimeMillis()}.jpg")
            .build()

        val credential = Credentials.basic(imageKitPrivateKey, "")

        val request = Request.Builder()
            .url(uploadUrl)
            .addHeader("Authorization", credential)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@AddPostActivity, "Greška upload slike: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                response.body?.close()

                if (!response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@AddPostActivity, "ImageKit greška: $responseBody", Toast.LENGTH_LONG).show()
                    }
                    return
                }

                try {
                    val json = JSONObject(responseBody ?: "")
                    val imageUrl = json.getString("url")
                    runOnUiThread {
                        onSuccess(imageUrl)
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@AddPostActivity, "Greška u odgovoru servera: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun savePostToFirestore(description: String, imageUrl: String?) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Morate biti prijavljeni za objavu.", Toast.LENGTH_SHORT).show()
            return
        }

        val post = hashMapOf(
            "content" to description,
            "timestamp" to Timestamp.now(),
            "imageUrl" to imageUrl,
            "likes" to hashMapOf<String, Boolean>(),
            "userId" to currentUser.uid.trim(),
            "userName" to currentUser.displayName
        )

        db.collection("posts")
            .add(post)
            .addOnSuccessListener {
                Toast.makeText(this, "Objava uspješno dodana", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Greška pri dodavanju objave: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}