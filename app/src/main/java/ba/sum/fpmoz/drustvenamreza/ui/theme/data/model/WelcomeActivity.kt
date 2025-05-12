package ba.sum.fpmoz.drustvenamreza

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class WelcomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        auth = FirebaseAuth.getInstance()

        val email = intent.getStringExtra("email")
        val welcomeText = findViewById<TextView>(R.id.welcomeText)
        val logoutBtn = findViewById<Button>(R.id.logoutBtn)

        welcomeText.text = "Dobrodošli!"

        logoutBtn.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Odjavljeni ste!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}