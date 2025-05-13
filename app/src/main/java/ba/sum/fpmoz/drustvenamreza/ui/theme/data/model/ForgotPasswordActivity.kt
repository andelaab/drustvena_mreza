package ba.sum.fpmoz.drustvenamreza

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<EditText>(R.id.etEmail)
        val resetButton = findViewById<Button>(R.id.btnResetPassword)

        resetButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            // Provjera da li je email prazan
            if (email.isEmpty()) {
                Toast.makeText(this, "Unesite email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Provjera valjanosti emaila
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Unesite ispravan email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Slanje zahtjeva za resetiranje lozinke putem Firebase Auth
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Email za reset lozinke je poslan", Toast.LENGTH_LONG).show()
                        finish() // Zatvara ovu aktivnost nakon uspješnog slanja e-maila
                    } else {
                        // Ispisivanje greške ako slanje nije uspjelo
                        Toast.makeText(this, "Greška: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}
