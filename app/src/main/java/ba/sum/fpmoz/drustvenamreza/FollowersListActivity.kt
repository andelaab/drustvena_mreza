package ba.sum.fpmoz.drustvenamreza

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ba.sum.fpmoz.drustvenamreza.adapter.FollowersAdapter
import com.google.firebase.firestore.FirebaseFirestore

class FollowersListActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FollowersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_followers_list)

        val userId = intent.getStringExtra("userId") ?: return
        recyclerView = findViewById(R.id.followersRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Set empty adapter initially
        adapter = FollowersAdapter(emptyList())
        recyclerView.adapter = adapter

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val followersMap = document.get("followers") as? Map<*, *>
                val followersList = followersMap?.keys?.map { it.toString() } ?: emptyList()
                adapter = FollowersAdapter(followersList)
                recyclerView.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Log.e("FollowersListActivity", "Greška pri dohvaćanju pratitelja: ${exception.message}")
            }
    }
}