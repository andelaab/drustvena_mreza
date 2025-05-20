package ba.sum.fpmoz.drustvenamreza.ui.theme.data

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

class AddPostActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var descriptionEditText: EditText
    private lateinit var addPostBtn: Button

    private val db = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        // Postavi toolbar s back buttonom
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Nova objava"

        imageView = findViewById(R.id.imageView)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        addPostBtn = findViewById(R.id.addPostBtn)

        // Klik na back button toolbar-a
        // Ovo vraća na prethodni ekran (finish activity)
        // Na uređaju postoji i hardverski back button
        // Ovo pokriva toolbar back strelicu
        // Override onOptionsItemSelected ispod

        addPostBtn.setOnClickListener {
            addPost()
        }
    }

    private fun addPost() {
        val description = descriptionEditText.text.toString().trim()
        if (description.isEmpty()) {
            Toast.makeText(this, "Unesite opis objave", Toast.LENGTH_SHORT).show()
            return
        }

        // Za jednostavnost sada objava bez slike
        val post = hashMapOf(
            "content" to description,
            "timestamp" to Timestamp.now(),
            "imageUrl" to null,
            "likes" to hashMapOf<String, Boolean>()
        )

        db.collection("posts")
            .add(post)
            .addOnSuccessListener {
                Toast.makeText(this, "Objava uspješno dodana", Toast.LENGTH_SHORT).show()
                finish() // Vrati se na prethodni ekran
            }
            .addOnFailureListener {
                Toast.makeText(this, "Greška pri dodavanju objave", Toast.LENGTH_SHORT).show()
            }
    }

    // Za toolbar back button:
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
