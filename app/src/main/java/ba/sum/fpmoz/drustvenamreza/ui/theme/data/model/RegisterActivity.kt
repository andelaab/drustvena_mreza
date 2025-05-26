package ba.sum.fpmoz.drustvenamreza

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var registerFullNameTxt: EditText
    private lateinit var registerEmailTxt: EditText
    private lateinit var registerPasswordTxt: EditText
    private lateinit var registerCnfPasswordTxt: EditText
    private lateinit var registerSubmitBtn: Button
    private lateinit var buttonLoginRedirect: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        registerFullNameTxt = findViewById(R.id.registerFullNameTxt)
        registerEmailTxt = findViewById(R.id.registerEmailTxt)
        registerPasswordTxt = findViewById(R.id.registerPasswordTxt)
        registerCnfPasswordTxt = findViewById(R.id.registerCnfPasswordTxt)
        registerSubmitBtn = findViewById(R.id.registerSubmitBtn)
        buttonLoginRedirect = findViewById(R.id.buttonLoginRedirect)

        registerSubmitBtn.setOnClickListener {
            val fullName = registerFullNameTxt.text.toString().trim()
            val email = registerEmailTxt.text.toString().trim()
            val password = registerPasswordTxt.text.toString()
            val cnfPassword = registerCnfPasswordTxt.text.toString()

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || cnfPassword.isEmpty()) {
                Toast.makeText(this, "Molimo popunite sva polja", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != cnfPassword) {
                Toast.makeText(this, "Lozinke se ne podudaraju", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(fullName)
                            .build()

                        user?.updateProfile(profileUpdates)
                            ?.addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    val userId = user.uid
                                    val userData = hashMapOf(
                                        "fullName" to fullName,
                                        "email" to email
                                    )

                                    db.collection("users").document(userId)
                                        .set(userData)
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "Registracija uspješna!", Toast.LENGTH_SHORT).show()
                                            startActivity(Intent(this, LoginActivity::class.java))
                                            finish()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(this, "Greška pri spremanju podataka: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                } else {
                                    Toast.makeText(this, "Greška pri ažuriranju profila: ${updateTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, "Registracija nije uspjela: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        buttonLoginRedirect.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}