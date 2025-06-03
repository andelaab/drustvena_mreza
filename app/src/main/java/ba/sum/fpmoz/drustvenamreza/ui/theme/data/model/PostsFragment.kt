package ba.sum.fpmoz.drustvenamreza.ui.theme.data.model

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.google.firebase.firestore.SetOptions
import android.widget.Toast

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
            onCommentClicked = { post -> openComments(post) },
            onDeleteClicked = { post -> deletePost(post) } // NOVO
        )
        recyclerView.adapter = adapter

        val addPostButton = view.findViewById<View>(R.id.addPostButton)
        addPostButton.setOnClickListener {
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

                        if (likesField == null || likesField is Map<*, *>) {
                            try {
                                val post = doc.toObject(Post::class.java)
                                if (post != null) {
                                    postsList.add(post.copy(id = doc.id))
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
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

        if (likes.isEmpty()) {
            docRef.update("likes", FieldValue.delete())
                .addOnSuccessListener {
                    updateLocalPostLikes(post, null)
                }
        } else {
            val filteredLikes = likes.filterKeys { it.isNotBlank() && it != "null" }

            if (filteredLikes.isNotEmpty()) {
                docRef.set(mapOf("likes" to filteredLikes), SetOptions.merge())
                    .addOnSuccessListener {
                        updateLocalPostLikes(post, filteredLikes)
                    }
            } else {
                docRef.update("likes", FieldValue.delete())
                    .addOnSuccessListener {
                        updateLocalPostLikes(post, null)
                    }
            }
        }
    }

    private fun updateLocalPostLikes(post: Post, updatedLikes: Map<String, Boolean>?) {
        val index = postsList.indexOfFirst { it.id == post.id }
        if (index != -1) {
            val updatedPost = post.copy(likes = updatedLikes)
            postsList[index] = updatedPost
            adapter.notifyItemChanged(index)
        }
    }

    private fun openComments(post: Post) {
        val intent = Intent(requireContext(), CommentsActivity::class.java)
        intent.putExtra("postId", post.id)
        startActivity(intent)
    }

    // NOVO: Brisanje objave i komentara
    private fun deletePost(post: Post) {
        val postId = post.id ?: return
        val postRef = firestore.collection("posts").document(postId)
        // Prvo obriši sve komentare
        firestore.collection("posts").document(postId).collection("comments")
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()
                for (doc in snapshot.documents) {
                    batch.delete(doc.reference)
                }
                // Obriši i samu objavu
                batch.delete(postRef)
                batch.commit().addOnSuccessListener {
                    postsList.removeAll { it.id == postId }
                    adapter.notifyDataSetChanged()
                    Toast.makeText(requireContext(), "Objava obrisana", Toast.LENGTH_SHORT).show()
                }
            }
    }
}