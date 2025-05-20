package ba.sum.fpmoz.drustvenamreza.ui.theme.data

import android.app.Activity
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
import java.util.*

class AddPostActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var contentEditText: EditText
    private lateinit var addPostBtn: Button
    private var imageUri: Uri? = null

    private val storageRef = FirebaseStorage.getInstance().reference
    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        imageView = findViewById(R.id.imageView)
        contentEditText = findViewById(R.id.descriptionEditText) // ili zamijeni ID u layoutu
        addPostBtn = findViewById(R.id.addPostBtn)

        imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        addPostBtn.setOnClickListener {
            if (imageUri != null) {
                uploadImageAndSavePost()
            } else {
                savePost(null)
            }
        }
    }

    private fun uploadImageAndSavePost() {
        val fileName = UUID.randomUUID().toString()
        val imageRef = storageRef.child("post_images/$fileName.jpg")

        imageUri?.let { uri ->
            imageRef.putFile(uri).addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    savePost(downloadUrl.toString())
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Neuspješno slanje slike", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun savePost(imageUrl: String?) {
        val postMap = hashMapOf(
            "content" to contentEditText.text.toString(),
            "timestamp" to Timestamp.now(),
            "imageUrl" to imageUrl
        )

        firestore.collection("posts")
            .add(postMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Objava dodana!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Greška: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            imageView.setImageURI(imageUri)
        }
    }
}
