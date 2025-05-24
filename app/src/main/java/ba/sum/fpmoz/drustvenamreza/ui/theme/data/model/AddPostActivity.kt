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

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Nova objava"

        imageView = findViewById(R.id.imageView)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        addPostBtn = findViewById(R.id.addPostBtn)

        addPostBtn.setOnClickListener {
            addPost()
        }

        // POZOVI OVDJE AKO ŽELIŠ IZVRŠITI KONVERZIJU (SAMO JEDNOM!)
        // convertLikesToList()
    }

    private fun addPost() {
        val description = descriptionEditText.text.toString().trim()
        if (description.isEmpty()) {
            Toast.makeText(this, "Unesite opis objave", Toast.LENGTH_SHORT).show()
            return
        }

        val post = hashMapOf(
            "content" to description,
            "timestamp" to Timestamp.now(),
            "imageUrl" to null,
            "likes" to arrayListOf<String>() // ✅ prazni niz lajkova
        )

        db.collection("posts")
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

    // ✅ FUNKCIJA ZA KONVERZIJU - unutar klase, ali izvan metoda
    private fun convertLikesToList() {
        db.collection("posts")
            .get()
            .addOnSuccessListener { snapshot ->
                for (document in snapshot.documents) {
                    val likesMap = document.get("likes") as? Map<String, Boolean>
                    if (likesMap != null) {
                        val likesList = likesMap.filterValues { it }.keys.toList()

                        db.collection("posts")
                            .document(document.id)
                            .update("likes", likesList)
                            .addOnSuccessListener {
                                println("Likes za post ${document.id} uspješno konvertirani.")
                            }
                            .addOnFailureListener { e ->
                                println("Greška pri konverziji posta ${document.id}: ${e.message}")
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                println("Greška pri dohvaćanju postova: ${e.message}")
            }
    }
}
