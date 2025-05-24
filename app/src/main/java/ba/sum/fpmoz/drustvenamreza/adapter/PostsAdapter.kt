package ba.sum.fpmoz.drustvenamreza.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ba.sum.fpmoz.drustvenamreza.R
import ba.sum.fpmoz.drustvenamreza.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PostsAdapter(
    private val posts: List<Post>,
    private val onLikeClicked: (Post) -> Unit,
    private val onCommentClicked: (Post) -> Unit
) : RecyclerView.Adapter<PostsAdapter.PostViewHolder>() {

    private val auth = FirebaseAuth.getInstance()

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.postImage)
        val usernameTextView: TextView = itemView.findViewById(R.id.username)
        val likeIcon: ImageView = itemView.findViewById(R.id.likeIcon)
        val likeCountText: TextView = itemView.findViewById(R.id.likeCount)
        val commentIcon: ImageView = itemView.findViewById(R.id.commentIcon)
        val commentCountText: TextView = itemView.findViewById(R.id.commentCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        holder.usernameTextView.text = post.userName
        holder.likeCountText.text = "Lajkova: ${post.likes?.size ?: 0}"
        holder.commentCountText.text = "Komentara: ${post.commentCount ?: 0}"

        Glide.with(holder.imageView.context)
            .load(post.imageUrl ?: R.drawable.placeholder)
            .into(holder.imageView)

        val currentUser = auth.currentUser?.uid
        val isLiked = post.likes?.containsKey(currentUser) == true

        holder.likeIcon.setImageResource(if (isLiked) R.drawable.ic_like else R.drawable.ic_like_outline)

        holder.likeIcon.setOnClickListener { onLikeClicked(post) }
        holder.commentIcon.setOnClickListener { onCommentClicked(post) }
    }

    override fun getItemCount(): Int = posts.size
}