package ba.sum.fpmoz.drustvenamreza

import android.os.Bundle
import android.view.MenuItem
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
    private lateinit var bioEditText: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Omogućavanje strelice za povratak
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Uredi Profil"

        // Inicijalizacija Firebase instanci
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Povezivanje UI elemenata
        firstNameEditText = findViewById(R.id.editTextFirstName)
        lastNameEditText = findViewById(R.id.editTextLastName)
        interestsEditText = findViewById(R.id.editTextInterests)
        bioEditText = findViewById(R.id.editTextBio)
        saveButton = findViewById(R.id.btnSaveProfile)

        // Dohvaćanje trenutnog korisnika
        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val fullName = document.getString("fullName") ?: ""
                        val interests = document.getString("interests") ?: ""
                        val bio = document.getString("bio") ?: ""

                        val parts = fullName.split(" ", limit = 2)
                        if (parts.size == 2) {
                            firstNameEditText.setText(parts[0])
                            lastNameEditText.setText(parts[1])
                        } else {
                            firstNameEditText.setText(fullName)
                        }

                        interestsEditText.setText(interests)
                        bioEditText.setText(bio)
                    } else {
                        firstNameEditText.setText("")
                        lastNameEditText.setText("")
                        interestsEditText.setText("")
                        bioEditText.setText("")
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Greška pri dohvaćanju podataka", Toast.LENGTH_SHORT).show()
                }
        }

        // Listener za spremanje podataka
        saveButton.setOnClickListener {
            val firstName = firstNameEditText.text.toString().trim()
            val lastName = lastNameEditText.text.toString().trim()
            val interests = interestsEditText.text.toString().trim()
            val bio = bioEditText.text.toString().trim()

            if (firstName.isEmpty() || lastName.isEmpty()) {
                Toast.makeText(this, "Unesite ime i prezime", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fullName = "$firstName $lastName"
            val updates = mapOf(
                "fullName" to fullName,
                "interests" to interests,
                "bio" to bio
            )

            if (user != null) {
                db.collection("users").document(user.uid).set(updates)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Profil uspješno ažuriran", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Greška pri ažuriranju profila", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    // Povratak na prethodni ekran
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}