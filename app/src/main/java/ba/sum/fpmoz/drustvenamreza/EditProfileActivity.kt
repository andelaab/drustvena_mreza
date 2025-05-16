package ba.sum.fpmoz.drustvenamreza

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var interestsEditText: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        firstNameEditText = findViewById(R.id.editTextFirstName)
        lastNameEditText = findViewById(R.id.editTextLastName)
        interestsEditText = findViewById(R.id.editTextInterests)
        saveButton = findViewById(R.id.btnSaveProfile)

        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val fullName = document.getString("fullName") ?: ""
                        val interests = document.getString("interests") ?: ""

                        val parts = fullName.split(" ", limit = 2)
                        if (parts.size == 2) {
                            firstNameEditText.setText(parts[0])
                            lastNameEditText.setText(parts[1])
                        } else {
                            firstNameEditText.setText(fullName)
                        }

                        interestsEditText.setText(interests)
                    }
                }
        }

        saveButton.setOnClickListener {
            val firstName = firstNameEditText.text.toString().trim()
            val lastName = lastNameEditText.text.toString().trim()
            val interests = interestsEditText.text.toString().trim()

            if (firstName.isEmpty() || lastName.isEmpty()) {
                Toast.makeText(this, "Unesite ime i prezime", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fullName = "$firstName $lastName"

            val updates = mapOf(
                "fullName" to fullName,
                "interests" to interests
            )

            if (user != null) {
                db.collection("users").document(user.uid).update(updates)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Profil ažuriran", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Greška pri ažuriranju", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
