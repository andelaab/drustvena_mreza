package ba.sum.fpmoz.drustvenamreza

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var loginEmailTxt: EditText
    private lateinit var loginPasswordTxt: EditText
    private lateinit var loginSubmitBtn: Button
    private lateinit var registerBtn: Button
    private lateinit var forgotPasswordText: TextView  // Dodano za zaboravljenu lozinku

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // Provjerite da datoteka postoji i da nema grešaka

        auth = FirebaseAuth.getInstance()

        // Inicijalizacija UI elemenata
        loginEmailTxt = findViewById(R.id.loginEmailTxt)
        loginPasswordTxt = findViewById(R.id.loginPasswordTxt)
        loginSubmitBtn = findViewById(R.id.loginSubmitBtn)
        registerBtn = findViewById(R.id.registerBtn)
        forgotPasswordText = findViewById(R.id.textForgotPassword)  // Dodano za zaboravljenu lozinku

        // Postavljanje OnClickListener za gumb za prijavu
        loginSubmitBtn.setOnClickListener {
            val email = loginEmailTxt.text.toString()
            val password = loginPasswordTxt.text.toString()

            // Provjera ispravnosti unesenog emaila
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Neispravan email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Provjera jačine lozinke
            if (password.length < 6) {
                Toast.makeText(this, "Lozinka mora imati barem 6 znakova", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Prijava korisnika putem Firebase-a
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Uspješna prijava", Toast.LENGTH_SHORT).show()
                    // Pokretanje nove aktivnosti nakon uspješne prijave
                    startActivity(Intent(this, WelcomeActivity::class.java))
                    finish()
                } else {
                    // U slučaju neuspjeha, prikazivanje greške
                    Toast.makeText(this, "Prijava nije uspjela: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Postavljanje OnClickListener za gumb za registraciju
        registerBtn.setOnClickListener {
            // Pokretanje aktivnosti za registraciju
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Postavljanje OnClickListener za "Zaboravili ste lozinku?" tekst
        forgotPasswordText.setOnClickListener {
            // Pokretanje aktivnosti za resetiranje lozinke
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }
}
