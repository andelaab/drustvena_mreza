package ba.sum.fpmoz.drustvenamreza.ui.theme.data.model

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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
    private lateinit var buttonSend: Button

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid ?: ""

    // ID posta čije komentare prikazujemo, dobit će se iz intent-a
    private var postId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_comments)

        // Sakrij ActionBar (naslov aplikacije)
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

        // Dohvati postId iz intenta
        postId = intent.getStringExtra("postId")

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
                    for (doc in snapshot.documents) {
                        val comment = doc.toObject(Comment::class.java)
                        if (comment != null) {
                            commentsList.add(comment.copy(commentId = doc.id))
                        }
                    }
                    adapter.notifyDataSetChanged()
                    recyclerView.scrollToPosition(commentsList.size - 1)
                }
            }
    }

    private fun sendComment(postId: String, content: String) {
        val newComment = Comment(
            commentId = "",
            userId = currentUserId,
            content = content,
            timestamp = System.currentTimeMillis()
        )

        firestore.collection("posts")
            .document(postId)
            .collection("comments")
            .add(newComment)
            .addOnSuccessListener {
                // Očisti polje za unos nakon slanja
                editTextComment.text.clear()

                // Ažuriraj broj komentara na postu
                incrementCommentCount(postId)
            }
            .addOnFailureListener {
                // Opcionalno: prikazati poruku o grešci korisniku
            }
    }

    private fun incrementCommentCount(postId: String) {
        val postRef = firestore.collection("posts").document(postId)
        postRef.update("commentCount", FieldValue.increment(1))
    }
}
