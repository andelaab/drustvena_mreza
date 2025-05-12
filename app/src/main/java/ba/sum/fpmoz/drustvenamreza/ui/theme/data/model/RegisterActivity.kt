package ba.sum.fpmoz.drustvenamreza

import android.content.Intent  // Dodaj import za Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    // Instanca za korištenje firebase sustava
    private lateinit var auth: FirebaseAuth
    // Polja za unos podataka
    private lateinit var registerEmailTxt: EditText
    private lateinit var registerPasswordTxt: EditText
    private lateinit var registerCnfPasswordTxt: EditText
    // Button za potvrdu registracije
    private lateinit var registerSubmitBtn: Button
    // Button za preusmjeravanje na login ekran
    private lateinit var buttonLoginRedirect: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        // Inicijaliziraj polja i gumbe
        registerEmailTxt = findViewById(R.id.registerEmailTxt)
        registerPasswordTxt = findViewById(R.id.registerPasswordTxt)
        registerCnfPasswordTxt = findViewById(R.id.registerCnfPasswordTxt)
        registerSubmitBtn = findViewById(R.id.registerSubmitBtn)
        buttonLoginRedirect = findViewById(R.id.buttonLoginRedirect)

        // Gumb za registraciju
        registerSubmitBtn.setOnClickListener {
            val email = registerEmailTxt.text.toString()
            val password = registerPasswordTxt.text.toString()
            val cnfPassword = registerCnfPasswordTxt.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && password == cnfPassword) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Uspješna registracija
                            val user = auth.currentUser
                            println("Registracija uspješna: ${user?.email}")
                        } else {
                            // Neuspješna registracija
                            val errorMessage = task.exception?.message
                            println("Registracija nije uspjela: $errorMessage")
                        }
                    }
            }
        }

        // Gumb za preusmjeravanje na login ekran
        buttonLoginRedirect.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()  // Opcionalno, završava trenutnu aktivnost
        }
    }
}
