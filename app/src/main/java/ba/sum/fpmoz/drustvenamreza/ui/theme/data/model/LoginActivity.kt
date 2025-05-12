package ba.sum.fpmoz.drustvenamreza

import android.os.Bundle
import android.content.Intent
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var loginEmailTxt: EditText
    private lateinit var loginPasswordTxt: EditText
    private lateinit var loginSubmitBtn: Button
    private lateinit var registerBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // Provjerite da datoteka postoji i da nema grešaka

        auth = FirebaseAuth.getInstance()

        loginEmailTxt = findViewById(R.id.loginEmailTxt)
        loginPasswordTxt = findViewById(R.id.loginPasswordTxt)
        loginSubmitBtn = findViewById(R.id.loginSubmitBtn)
        registerBtn = findViewById(R.id.registerBtn)

        loginSubmitBtn.setOnClickListener {
            val email = loginEmailTxt.text.toString()
            val password = loginPasswordTxt.text.toString()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Neispravan email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Lozinka mora imati barem 6 znakova", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Uspješna prijava", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, WelcomeActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Prijava nije uspjela: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        registerBtn.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}