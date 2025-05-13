package ba.sum.fpmoz.drustvenamreza

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    private lateinit var registerFullNameTxt: EditText
    private lateinit var registerEmailTxt: EditText
    private lateinit var registerPasswordTxt: EditText
    private lateinit var registerCnfPasswordTxt: EditText
    private lateinit var registerSubmitBtn: Button
    private lateinit var buttonLoginRedirect: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

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

            if (!isPasswordValid(password)) {
                Toast.makeText(this, "Lozinka mora imati najmanje 8 znakova, uključujući slova, brojeve i posebne znakove", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        Toast.makeText(this, "Registracija uspješna!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } else {
                        val errorMessage = task.exception?.message
                        Toast.makeText(this, "Registracija nije uspjela: $errorMessage", Toast.LENGTH_LONG).show()
                    }
                }
        }

        buttonLoginRedirect.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    // Funkcija za provjeru kompleksnosti lozinke
    private fun isPasswordValid(password: String): Boolean {
        val minLength = 8
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }

        return password.length >= minLength && hasLetter && hasDigit && hasSpecialChar
    }
}
