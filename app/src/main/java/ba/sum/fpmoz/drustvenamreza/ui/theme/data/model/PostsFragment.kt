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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class PostsFragment : Fragment() {

    private lateinit var recyclerPosts: RecyclerView
    private lateinit var adapter: PostsAdapter
    private val postsList = mutableListOf<Post>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_posts, container, false)

        recyclerPosts = view.findViewById(R.id.recyclerPosts)
        recyclerPosts.layoutManager = LinearLayoutManager(requireContext())

        adapter = PostsAdapter(postsList) { post ->
            followUser(post)
        }
        recyclerPosts.adapter = adapter

        val addPostButton = view.findViewById<Button>(R.id.addPostButton)
        addPostButton.setOnClickListener {
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
                    doc.toObject(Post::class.java)?.let { post ->
                        postsList.add(post)
                    }
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun followUser(post: Post) {
        // TODO: implement follow logic
    }
}
