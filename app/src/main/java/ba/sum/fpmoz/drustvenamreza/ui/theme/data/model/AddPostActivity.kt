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
import com.google.firebase.storage.FirebaseStorage

class AddPostActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var descriptionEditText: EditText
    private lateinit var addPostBtn: Button

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    private var selectedImageUri: Uri? = null

    private val PICK_IMAGE_REQUEST = 1001

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
            uploadImageToFirebase(selectedImageUri!!) { imageUrl ->
                savePostToFirestore(description, imageUrl)
            }
        } else {
            // Ako nema slike, spremi post bez slike
            savePostToFirestore(description, null)
        }
    }

    private fun uploadImageToFirebase(uri: Uri, onSuccess: (String) -> Unit) {
        val storageRef = storage.reference
        val imageRef = storageRef.child("post_images/${System.currentTimeMillis()}.jpg")

        imageRef.putFile(uri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    onSuccess(downloadUri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Greška pri uploadu slike", Toast.LENGTH_SHORT).show()
            }
    }

    private fun savePostToFirestore(description: String, imageUrl: String?) {
        val post = hashMapOf(
            "content" to description,
            "timestamp" to Timestamp.now(),
            "imageUrl" to imageUrl,
            "likes" to hashMapOf<String, Boolean>()
        )

        db.collection("pogit addsts")
            .add(post)
            .addOnSuccessListener {
                Toast.makeText(this, "Objava uspješno dodana", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Greška pri dodavanju objave", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
