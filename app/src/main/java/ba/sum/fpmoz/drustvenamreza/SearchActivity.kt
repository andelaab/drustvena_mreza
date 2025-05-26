package ba.sum.fpmoz.drustvenamreza

import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ba.sum.fpmoz.drustvenamreza.adapter.UserAdapter
import ba.sum.fpmoz.drustvenamreza.model.User
import com.google.firebase.firestore.FirebaseFirestore

class SearchActivity : AppCompatActivity() {
    private lateinit var searchInput: EditText
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Enable back arrow
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        searchInput = findViewById(R.id.searchInput)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<Button>(R.id.searchButton).setOnClickListener {
            val query = searchInput.text.toString()
            if (query.isNotEmpty()) {
                searchUsers(query)
            } else {
                Toast.makeText(this, "Unesite pojam za pretragu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchUsers(query: String) {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")
        val queryLower = query.lowercase()

        usersCollection
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "Nema podataka u bazi", Toast.LENGTH_SHORT).show()
                } else {
                    val users = documents.mapNotNull { document ->
                        val fullName = document.getString("fullName")?.lowercase() ?: ""
                        val email = document.getString("email") ?: ""
                        if (fullName.contains(queryLower)) {
                            User(fullName, email)
                        } else null
                    }
                    if (users.isEmpty()) {
                        Toast.makeText(this, "Nema rezultata za pretragu", Toast.LENGTH_SHORT).show()
                    } else {
                        showSearchResults(users)
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Greška: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showSearchResults(users: List<User>) {
        val adapter = UserAdapter(users)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
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