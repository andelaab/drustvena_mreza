package ba.sum.fpmoz.drustvenamreza

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class UserProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        // Enable back arrow
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val fullName = intent.getStringExtra("fullName")
        val email = intent.getStringExtra("email")

        Log.d("UserProfileActivity", "Primljeni podaci: fullName=$fullName, email=$email")

        val fullNameTextView: TextView = findViewById(R.id.profileFullName)
        val emailTextView: TextView = findViewById(R.id.profileEmail)

        fullNameTextView.text = fullName ?: "Nepoznato ime"
        emailTextView.text = email ?: "Nepoznat email"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish() // Close the activity and go back
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}