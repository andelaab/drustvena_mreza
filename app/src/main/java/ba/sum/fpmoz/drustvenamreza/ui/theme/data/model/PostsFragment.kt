package ba.sum.fpmoz.drustvenamreza.ui.theme.data.model

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ba.sum.fpmoz.drustvenamreza.R
import ba.sum.fpmoz.drustvenamreza.adapter.PostsAdapter
import ba.sum.fpmoz.drustvenamreza.model.Post
import ba.sum.fpmoz.drustvenamreza.ui.theme.data.AddPostActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class PostsFragment : Fragment() {

    private lateinit var recyclerPosts: RecyclerView
    private lateinit var adapter: PostsAdapter
    private val postsList = mutableListOf<Post>()
    private val db = FirebaseFirestore.getInstance()
    private val currentUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_posts, container, false)

        recyclerPosts = view.findViewById(R.id.recyclerPosts)
        recyclerPosts.layoutManager = LinearLayoutManager(requireContext())

        adapter = PostsAdapter(
            postsList,
            onFollowClicked = { post -> followUser(post) },
            onLikeClicked = { post, position -> likePost(post, position) }
        )
        recyclerPosts.adapter = adapter

        view.findViewById<Button>(R.id.addPostButton).setOnClickListener {
            startActivity(Intent(requireContext(), AddPostActivity::class.java))
        }

        loadPosts()
        return view
    }

    private fun loadPosts() {
        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                postsList.clear()
                snapshot.documents.forEach { doc ->
                    val post = doc.toObject(Post::class.java)
                    post?.id = doc.id
                    post?.let { postsList.add(it) }
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun likePost(post: Post, position: Int) {
        val userId = currentUserId ?: return
        val postId = post.id ?: return
        val postRef = db.collection("posts").document(postId)

        val isLiked = post.likes?.contains(userId) == true

        if (isLiked) {
            postRef.update("likes", FieldValue.arrayRemove(userId))
        } else {
            postRef.update("likes", FieldValue.arrayUnion(userId))
        }
    }

    private fun followUser(post: Post) {
        // Logika za follow ako zatreba u budućnosti
    }
}
