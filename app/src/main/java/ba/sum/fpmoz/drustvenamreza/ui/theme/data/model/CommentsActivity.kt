package ba.sum.fpmoz.drustvenamreza.ui.theme.data.model

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ba.sum.fpmoz.drustvenamreza.R
import ba.sum.fpmoz.drustvenamreza.adapter.CommentsAdapter
import ba.sum.fpmoz.drustvenamreza.model.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class CommentsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CommentsAdapter
    private val commentsList = mutableListOf<Comment>()

    private lateinit var editTextComment: EditText
    private lateinit var buttonSend: ImageButton

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid ?: ""

    private var postId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_comments)

        supportActionBar?.hide()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerComments)
        editTextComment = findViewById(R.id.editTextComment)
        buttonSend = findViewById(R.id.buttonSendComment)

        adapter = CommentsAdapter(commentsList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        postId = intent.getStringExtra("postId")

        val buttonBack = findViewById<ImageButton>(R.id.buttonBack)
        buttonBack.setOnClickListener {
            finish()
        }

        loadComments()

        buttonSend.setOnClickListener {
            val commentText = editTextComment.text.toString().trim()
            if (commentText.isNotEmpty() && postId != null) {
                sendComment(postId!!, commentText)
            }
        }
    }

    private fun loadComments() {
        if (postId == null) return

        firestore.collection("posts")
            .document(postId!!)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                if (snapshot != null) {
                    commentsList.clear()
                    val docs = snapshot.documents
                    if (docs.isEmpty()) {
                        adapter.notifyDataSetChanged()
                        return@addSnapshotListener
                    }
                    val userIds = docs.mapNotNull { it.getString("userId") }.toSet()
                    firestore.collection("users")
                        .whereIn("uid", userIds.toList())
                        .get()
                        .addOnSuccessListener { usersSnapshot ->
                            val userMap = usersSnapshot.documents.associateBy(
                                { it.getString("uid") ?: "" },
                                { it.getString("fullName") ?: "Nepoznato ime" }
                            )
                            for (doc in docs) {
                                val comment = doc.toObject(Comment::class.java)
                                if (comment != null) {
                                    val fullName = userMap[comment.userId] ?: "Nepoznato ime"
                                    commentsList.add(
                                        comment.copy(
                                            commentId = doc.id,
                                            userFullName = fullName
                                        )
                                    )
                                }
                            }
                            adapter.notifyDataSetChanged()
                            recyclerView.scrollToPosition(commentsList.size - 1)
                        }
                }
            }
    }

    private fun sendComment(postId: String, content: String) {
        if (currentUserId.isEmpty()) return // sigurnosna provjera

        // prvo dohvat fullName trenutnog korisnika
        firestore.collection("users").document(currentUserId).get()
            .addOnSuccessListener { document ->
                val fullName = document.getString("fullName") ?: "Nepoznato ime"

                val newComment = Comment(
                    commentId = "",           // može ostati prazno, Firestore će dati ID
                    userId = currentUserId,
                    userFullName = fullName,  // sad postavljaš pravo ime
                    content = content,
                    timestamp = System.currentTimeMillis()
                )

                firestore.collection("posts")
                    .document(postId)
                    .collection("comments")
                    .add(newComment)
                    .addOnSuccessListener {
                        editTextComment.text.clear()
                        incrementCommentCount(postId)
                    }
                    .addOnFailureListener {
                        // opcionalno prikazati grešku
                    }
            }
            .addOnFailureListener {
                // opcionalno prikazati grešku kod dohvata korisnika
            }
    }

    private fun incrementCommentCount(postId: String) {
        val postRef = firestore.collection("posts").document(postId)
        postRef.update("commentCount", FieldValue.increment(1))
    }
}
