package ba.sum.fpmoz.drustvenamreza

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ba.sum.fpmoz.drustvenamreza.adapter.UserPostsAdapter
import ba.sum.fpmoz.drustvenamreza.manager.FollowManager
import ba.sum.fpmoz.drustvenamreza.model.Post
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import ba.sum.fpmoz.drustvenamreza.ui.theme.data.model.CommentsActivity

class UserProfileActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val followManager = FollowManager(db)
    private val currentUserId = auth.currentUser?.uid ?: ""

    private lateinit var followToggleButton: Button
    private lateinit var followersCountTextView: TextView
    private lateinit var bioTextView: TextView
    private lateinit var interestsTextView: TextView
    private lateinit var fullNameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var profileImageView: ImageView
    private var isFollowing = false
    private lateinit var targetUserId: String

    private lateinit var recyclerView: RecyclerView
    private lateinit var postsAdapter: UserPostsAdapter
    private val postsList = mutableListOf<Post>()

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

        fullNameTextView = findViewById(R.id.profileFullName)
        emailTextView = findViewById(R.id.profileEmail)
        followersCountTextView = findViewById(R.id.followersCountText)
        followToggleButton = findViewById(R.id.followToggleButton)
        bioTextView = findViewById(R.id.profileBio)
        interestsTextView = findViewById(R.id.profileInterests)
        profileImageView = findViewById(R.id.profileImageView)

        fullNameTextView.text = fullName ?: "Nepoznato ime"
        emailTextView.text = email ?: "Nepoznat email"

        recyclerView = findViewById(R.id.recyclerUserPosts)
        recyclerView.layoutManager = LinearLayoutManager(this)
        postsAdapter = UserPostsAdapter(
            postsList,
            onLikeClicked = { post -> toggleLike(post) },
            onCommentClicked = { post -> openComments(post) }
        )
        recyclerView.adapter = postsAdapter

        loadUserProfileData()
        loadFollowersCount()
        updateFollowButtonState()
        loadUserPosts(targetUserId)

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
                val fullName = doc.getString("fullName") ?: "Nepoznato ime"
                val email = doc.getString("email") ?: "Nepoznat email"
                val imageUrl = doc.getString("profileImageUrl")

                bioTextView.text = bio
                interestsTextView.text = interests
                fullNameTextView.text = fullName
                emailTextView.text = email

                if (!imageUrl.isNullOrBlank()) {
                    Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_person)
                        .into(profileImageView)
                } else {
                    profileImageView.setImageResource(R.drawable.ic_person)
                }
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

    private fun loadUserPosts(userId: String) {
        db.collection("posts")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                postsList.clear()
                for (doc in snapshot.documents) {
                    val post = doc.toObject(Post::class.java)
                    if (post != null) {
                        postsList.add(post.copy(id = doc.id))
                    }
                }
                postsAdapter.notifyDataSetChanged()
            }
    }

    private fun toggleLike(post: Post) {
        val docRef = db.collection("posts").document(post.id ?: return)
        val likes = post.likes?.toMutableMap() ?: mutableMapOf()

        if (likes.containsKey(currentUserId)) {
            likes.remove(currentUserId)
        } else {
            likes[currentUserId] = true
        }

        if (likes.isEmpty()) {
            docRef.update("likes", FieldValue.delete())
        } else {
            docRef.set(mapOf("likes" to likes), SetOptions.merge())
        }
    }

    private fun openComments(post: Post) {
        val intent = Intent(this, CommentsActivity::class.java)
        intent.putExtra("postId", post.id)
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