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

class UserPostsAdapter(
    private val posts: List<Post>,
    private val onLikeClicked: (Post) -> Unit,
    private val onCommentClicked: (Post) -> Unit
) : RecyclerView.Adapter<UserPostsAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.postImage)
        val postDescription: TextView = itemView.findViewById(R.id.postDescription)
        val postTimestamp: TextView = itemView.findViewById(R.id.postTimestamp)
        val likeIcon: ImageView = itemView.findViewById(R.id.likeIcon)
        val likeCount: TextView = itemView.findViewById(R.id.likeCount)
        val commentIcon: ImageView = itemView.findViewById(R.id.commentIcon)
        val commentCount: TextView = itemView.findViewById(R.id.commentCount)
        val username: TextView = itemView.findViewById(R.id.username)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.username.text = post.userName ?: "Korisnik"
        holder.postDescription.text = post.content ?: ""
        holder.postTimestamp.text = post.timestamp?.toDate()?.toString() ?: ""
        holder.likeCount.text = "${post.likes?.size ?: 0}"
        holder.commentCount.text = "${post.commentCount ?: 0}"

        if (!post.imageUrl.isNullOrBlank()) {
            holder.imageView.visibility = View.VISIBLE
            Glide.with(holder.imageView.context)
                .load(post.imageUrl)
                .placeholder(R.drawable.placeholder)
                .into(holder.imageView)
        } else {
            holder.imageView.visibility = View.GONE
        }

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val likedByUser = post.likes?.containsKey(currentUserId) == true
        holder.likeIcon.setImageResource(
            if (likedByUser) R.drawable.ic_like else R.drawable.ic_like_outline
        )

        holder.likeIcon.setOnClickListener { onLikeClicked(post) }
        holder.commentIcon.setOnClickListener { onCommentClicked(post) }
    }

    override fun getItemCount(): Int = posts.size
}