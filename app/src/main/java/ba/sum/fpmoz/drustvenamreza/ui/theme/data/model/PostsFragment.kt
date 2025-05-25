package ba.sum.fpmoz.drustvenamreza.ui.theme.data.model

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import ba.sum.fpmoz.drustvenamreza.R
import ba.sum.fpmoz.drustvenamreza.model.Post
import ba.sum.fpmoz.drustvenamreza.adapter.PostsAdapter
import ba.sum.fpmoz.drustvenamreza.ui.theme.data.AddPostActivity
import android.content.Intent



class PostsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostsAdapter
    private val postsList = mutableListOf<Post>()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid ?: ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_posts, container, false)
        recyclerView = view.findViewById(R.id.recyclerPosts)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = PostsAdapter(
            postsList,
            onLikeClicked = { post -> toggleLike(post) },
            onCommentClicked = { post -> openComments(post) }
        )
        recyclerView.adapter = adapter

        val addPostButton = view.findViewById<View>(R.id.addPostButton)
        addPostButton.setOnClickListener {
            // Ovdje pokreni AddPost aktivnost ili navigaciju prema fragmentu
            // Ako imaš AddPostActivity:
            val intent = Intent(requireContext(), AddPostActivity::class.java)
            startActivity(intent)
        }


        loadPosts()

        return view
    }

    private fun loadPosts() {
        firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                if (snapshot != null) {
                    postsList.clear()
                    for (doc in snapshot.documents) {
                        val likesField = doc.get("likes")

                        // Provjeri da je 'likes' polje Map (ili null)
                        if (likesField == null || likesField is Map<*, *>) {
                            try {
                                val post = doc.toObject(Post::class.java)
                                if (post != null) {
                                    postsList.add(post.copy(id = doc.id))
                                }
                            } catch (e: Exception) {
                                // Preskoči ako dođe do greške u deserializaciji
                                e.printStackTrace()
                            }
                        } else {
                            // Preskoči dokument s pogrešnim tipom likes
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }

    private fun toggleLike(post: Post) {
        val docRef = firestore.collection("posts").document(post.id ?: return)
        val likes = post.likes?.toMutableMap() ?: mutableMapOf()

        if (likes.containsKey(currentUserId)) {
            likes.remove(currentUserId)
        } else {
            likes[currentUserId] = true
        }

        docRef.update("likes", likes)
    }

    private fun openComments(post: Post) {
        // Ovdje dodaj navigaciju prema fragmentu/aktivnosti za komentare
    }
}
