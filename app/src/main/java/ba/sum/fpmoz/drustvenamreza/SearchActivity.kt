package ba.sum.fpmoz.drustvenamreza

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
    private lateinit var adapter: UserAdapter
    private var users: List<User> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        searchInput = findViewById(R.id.searchInput)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Set empty adapter initially
        adapter = UserAdapter(users) { user ->
            openUserProfile(user)
        }
        recyclerView.adapter = adapter

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
                    updateResults(emptyList())
                } else {
                    val filteredUsers = documents.mapNotNull { document ->
                        val fullNameRaw = document.getString("fullName") ?: return@mapNotNull null
                        val email = document.getString("email") ?: ""
                        val uid = document.getString("uid") ?: document.id
                        if (fullNameRaw.lowercase().contains(queryLower)) {
                            User(uid, fullNameRaw, email)
                        } else null
                    }
                    if (filteredUsers.isEmpty()) {
                        Toast.makeText(this, "Nema rezultata za pretragu", Toast.LENGTH_SHORT).show()
                    }
                    updateResults(filteredUsers)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Greška: ${e.message}", Toast.LENGTH_SHORT).show()
                updateResults(emptyList())
            }
    }

    private fun updateResults(newUsers: List<User>) {
        users = newUsers
        adapter = UserAdapter(users) { user ->
            openUserProfile(user)
        }
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
    }

    private fun openUserProfile(user: User) {
        Log.d("SearchActivity", "Otvaram UserProfileActivity za ${user.uid}")
        val intent = Intent(this, UserProfileActivity::class.java)
        intent.putExtra("userId", user.uid)
        intent.putExtra("fullName", user.fullName)
        intent.putExtra("email", user.email)
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}