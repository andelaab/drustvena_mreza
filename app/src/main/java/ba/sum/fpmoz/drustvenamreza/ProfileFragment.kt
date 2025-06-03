package ba.sum.fpmoz.drustvenamreza

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ba.sum.fpmoz.drustvenamreza.adapter.UserPostsAdapter
import ba.sum.fpmoz.drustvenamreza.model.Post
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import ba.sum.fpmoz.drustvenamreza.ui.theme.data.model.CommentsActivity

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var profileImageView: ImageView
    private lateinit var emailText: TextView
    private lateinit var fullNameText: TextView
    private lateinit var bioText: TextView
    private lateinit var interestsText: TextView
    private lateinit var followersCountText: TextView
    private lateinit var followingCountText: TextView

    private lateinit var editProfileBtn: Button
    private lateinit var logoutBtn: Button

    private lateinit var recyclerView: RecyclerView
    private lateinit var postsAdapter: UserPostsAdapter
    private val postsList = mutableListOf<Post>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        profileImageView = view.findViewById(R.id.profileImageView)
        emailText = view.findViewById(R.id.profileEmail)
        fullNameText = view.findViewById(R.id.profileFullName)
        bioText = view.findViewById(R.id.profileBio)
        interestsText = view.findViewById(R.id.profileInterests)
        followersCountText = view.findViewById(R.id.followersCountText)
        followingCountText = view.findViewById(R.id.followingCountText)
        editProfileBtn = view.findViewById(R.id.btnEditProfile)
        logoutBtn = view.findViewById(R.id.btnLogout)

        recyclerView = view.findViewById(R.id.recyclerUserPosts)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        postsAdapter = UserPostsAdapter(
            postsList,
            onLikeClicked = { post -> toggleLike(post) },
            onCommentClicked = { post -> openComments(post) }
        )
        recyclerView.adapter = postsAdapter

        editProfileBtn.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        logoutBtn.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        followersCountText.setOnClickListener {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                val intent = Intent(requireContext(), FollowersListActivity::class.java)
                intent.putExtra("userId", userId)
                startActivity(intent)
            }
        }

        followingCountText.setOnClickListener {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                val intent = Intent(requireContext(), FollowingListActivity::class.java)
                intent.putExtra("userId", userId)
                startActivity(intent)
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }

    private fun loadUserData() {
        val user = auth.currentUser ?: return
        emailText.text = user.email ?: "Nepoznat email"

        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { doc ->
                fullNameText.text = doc.getString("fullName") ?: "Nepoznato ime"
                bioText.text = doc.getString("bio") ?: "Biografija nije dostupna"
                interestsText.text = doc.getString("interests") ?: "Nema interesa"

                val imageUrl = doc.getString("profileImageUrl")
                if (!imageUrl.isNullOrBlank()) {
                    Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_person)
                        .into(profileImageView)
                } else {
                    profileImageView.setImageResource(R.drawable.ic_person)
                }

                val followers = doc.get("followers") as? Map<*, *>
                val following = doc.get("following") as? Map<*, *>
                followersCountText.text = "Pratitelji: ${followers?.size ?: 0}"
                followingCountText.text = "Prati: ${following?.size ?: 0}"

                loadUserPosts(user.uid)
            }
            .addOnFailureListener {
                fullNameText.text = "Nepoznato ime"
                bioText.text = "Biografija nije dostupna"
                interestsText.text = "Nema interesa"
                followersCountText.text = "Pratitelji: 0"
                followingCountText.text = "Prati: 0"
            }
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
        val currentUserId = auth.currentUser?.uid ?: return
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
        val intent = Intent(requireContext(), CommentsActivity::class.java)
        intent.putExtra("postId", post.id)
        startActivity(intent)
    }
}