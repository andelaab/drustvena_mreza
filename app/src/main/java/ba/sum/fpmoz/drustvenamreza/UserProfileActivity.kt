package ba.sum.fpmoz.drustvenamreza

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ba.sum.fpmoz.drustvenamreza.manager.FollowManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val followManager = FollowManager(db)
    private val currentUserId = auth.currentUser?.uid ?: ""

    private lateinit var followToggleButton: Button
    private lateinit var followersCountTextView: TextView
    private lateinit var bioTextView: TextView
    private lateinit var interestsTextView: TextView
    private var isFollowing = false
    private lateinit var targetUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val fullName = intent.getStringExtra("fullName")
        val email = intent.getStringExtra("email")
        val userId = intent.getStringExtra("userId") ?: ""
        targetUserId = userId

        if (targetUserId.isEmpty()) {
            Toast.makeText(this, "Greška: korisnički ID nije proslijeđen.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d("UserProfileActivity", "Primljeno: $targetUserId, $fullName, $email")

        val fullNameTextView: TextView = findViewById(R.id.profileFullName)
        val emailTextView: TextView = findViewById(R.id.profileEmail)
        followersCountTextView = findViewById(R.id.followersCountText)
        followToggleButton = findViewById(R.id.followToggleButton)
        bioTextView = findViewById(R.id.profileBio)
        interestsTextView = findViewById(R.id.profileInterests)

        fullNameTextView.text = fullName ?: "Nepoznato ime"
        emailTextView.text = email ?: "Nepoznat email"

        loadUserProfileData()
        loadFollowersCount()
        updateFollowButtonState()

        followersCountTextView.setOnClickListener {
            val intent = Intent(this, FollowersListActivity::class.java)
            intent.putExtra("userId", targetUserId)
            startActivity(intent)
        }

        followToggleButton.setOnClickListener {
            if (isFollowing) {
                followManager.unfollowUser(currentUserId, targetUserId, {
                    Toast.makeText(this, "Otpratili ste korisnika", Toast.LENGTH_SHORT).show()
                    isFollowing = false
                    updateButtonUI()
                    loadFollowersCount()
                }, {
                    Toast.makeText(this, "Greška pri otpraćivanju", Toast.LENGTH_SHORT).show()
                })
            } else {
                followManager.followUser(currentUserId, targetUserId, {
                    Toast.makeText(this, "Zaprati korisnika", Toast.LENGTH_SHORT).show()
                    isFollowing = true
                    updateButtonUI()
                    loadFollowersCount()
                }, {
                    Toast.makeText(this, "Greška pri praćenju", Toast.LENGTH_SHORT).show()
                })
            }
        }
    }

    private fun loadUserProfileData() {
        db.collection("users").document(targetUserId).get()
            .addOnSuccessListener { doc ->
                val bio = doc.getString("bio") ?: "Biografija nije dostupna"
                val interests = doc.getString("interests") ?: "Nema interesa"
                bioTextView.text = bio
                interestsTextView.text = interests
            }
            .addOnFailureListener {
                bioTextView.text = "Biografija nije dostupna"
                interestsTextView.text = "Nema interesa"
            }
    }

    private fun loadFollowersCount() {
        db.collection("users").document(targetUserId).get()
            .addOnSuccessListener { doc ->
                val followers = doc.get("followers") as? Map<*, *>
                val count = followers?.size ?: 0
                followersCountTextView.text = "Pratitelji: $count"
            }
            .addOnFailureListener {
                followersCountTextView.text = "Pratitelji: 0"
            }
    }

    private fun updateFollowButtonState() {
        db.collection("users").document(currentUserId).get()
            .addOnSuccessListener { doc ->
                val following = doc.get("following") as? Map<*, *>
                isFollowing = following?.containsKey(targetUserId) == true
                updateButtonUI()
            }
    }

    private fun updateButtonUI() {
        followToggleButton.text = if (isFollowing) "Otprati" else "Prati"
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