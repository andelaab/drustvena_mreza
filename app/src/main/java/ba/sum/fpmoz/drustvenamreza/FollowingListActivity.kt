package ba.sum.fpmoz.drustvenamreza

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ba.sum.fpmoz.drustvenamreza.adapter.FollowersAdapter
import com.google.firebase.firestore.FirebaseFirestore

class FollowingListActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_followers_list)

        val userId = intent.getStringExtra("userId") ?: return
        val recyclerView: RecyclerView = findViewById(R.id.followersRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val followingMap = document.get("following") as? Map<*, *>
                val ids = followingMap?.keys?.map { it.toString() } ?: emptyList()

                if (ids.isEmpty()) {
                    recyclerView.adapter = FollowersAdapter(emptyList())
                    return@addOnSuccessListener
                }

                db.collection("users").whereIn("uid", ids.take(10)).get() // Firestore limit je 10!
                    .addOnSuccessListener { result ->
                        val names = result.map { it.getString("fullName") ?: it.id }
                        recyclerView.adapter = FollowersAdapter(names)
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("FollowingListActivity", "Greška: ${exception.message}")
            }
    }
}
