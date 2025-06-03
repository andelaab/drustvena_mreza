package ba.sum.fpmoz.drustvenamreza.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ba.sum.fpmoz.drustvenamreza.R
import ba.sum.fpmoz.drustvenamreza.model.Post
import com.google.firebase.auth.FirebaseAuth

class PostsAdapter(
    private val posts: List<Post>,
    private val onLikeClicked: (Post) -> Unit,
    private val onCommentClicked: (Post) -> Unit,
    private val onDeleteClicked: (Post) -> Unit // NOVO
) : RecyclerView.Adapter<PostsAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.postImage)
        val usernameTextView: TextView = itemView.findViewById(R.id.username)
        val postDescription: TextView = itemView.findViewById(R.id.postDescription)
        val likeIcon: ImageView = itemView.findViewById(R.id.likeIcon)
        val likeCountText: TextView = itemView.findViewById(R.id.likeCount)
        val commentIcon: ImageView = itemView.findViewById(R.id.commentIcon)
        val commentCountText: TextView = itemView.findViewById(R.id.commentCount)
        val postOptions: ImageView = itemView.findViewById(R.id.postOptions)
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

        if (!post.content.isNullOrBlank()) {
            holder.postDescription.visibility = View.VISIBLE
            holder.postDescription.text = post.content
        } else {
            holder.postDescription.visibility = View.GONE
        }

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

        if (likedByUser) {
            holder.likeIcon.setImageResource(R.drawable.ic_like)
        } else {
            holder.likeIcon.setImageResource(R.drawable.ic_like_outline)
        }

        holder.likeIcon.setOnClickListener {
            onLikeClicked(post)
        }

        holder.commentIcon.setOnClickListener {
            onCommentClicked(post)
        }

        // Prikaz tri točkice samo za svoje objave
        if (post.userId == currentUserId) {
            holder.postOptions.visibility = View.VISIBLE
            holder.postOptions.setOnClickListener { view ->
                val popup = PopupMenu(view.context, view)
                popup.menuInflater.inflate(R.menu.menu_post_options, popup.menu)
                popup.setOnMenuItemClickListener { item ->
                    if (item.itemId == R.id.action_delete_post) {
                        onDeleteClicked(post)
                        true
                    } else false
                }
                popup.show()
            }
        } else {
            holder.postOptions.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = posts.size
}