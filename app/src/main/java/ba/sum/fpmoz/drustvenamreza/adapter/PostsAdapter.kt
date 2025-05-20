package ba.sum.fpmoz.drustvenamreza.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ba.sum.fpmoz.drustvenamreza.R
import ba.sum.fpmoz.drustvenamreza.model.Post
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class PostsAdapter(
    private val posts: List<Post>,
    private val onFollowClicked: (Post) -> Unit
) : RecyclerView.Adapter<PostsAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagePost: ImageView    = itemView.findViewById(R.id.imagePost)
        val textContent: TextView   = itemView.findViewById(R.id.textDescription)
        val btnFollow: TextView     = itemView.findViewById(R.id.btnFollow)
        val textTimestamp: TextView = itemView.findViewById(R.id.textTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        // Prikaz sadržaja objave
        holder.textContent.text = post.content

        // Obrada timestamp polja koje može biti Long ili Firebase Timestamp
        val rawTs = post.timestamp
        val date: Date? = when (rawTs) {
            is com.google.firebase.Timestamp -> rawTs.toDate()
            is Long                         -> Date(rawTs)
            else                            -> null
        }
        holder.textTimestamp.text = date?.let {
            val sdf = SimpleDateFormat("dd.MM.yyyy. HH:mm", Locale.getDefault())
            sdf.format(it)
        } ?: "Nema datuma"

        // Prikaz slike samo ako postoji URL, inače sakrij ImageView
        if (!post.imageUrl.isNullOrBlank()) {
            holder.imagePost.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(post.imageUrl)
                .into(holder.imagePost)
        } else {
            holder.imagePost.visibility = View.GONE
        }

        // Rukovanje klikom na Follow
        holder.btnFollow.setOnClickListener { onFollowClicked(post) }
    }

    override fun getItemCount(): Int = posts.size
}
