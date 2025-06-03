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

class UserPostsAdapter(private val posts: List<Post>) : RecyclerView.Adapter<UserPostsAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.postImage)
        val postDescription: TextView = itemView.findViewById(R.id.postDescription)
        val postTimestamp: TextView = itemView.findViewById(R.id.postTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.postDescription.text = post.content ?: ""
        holder.postTimestamp.text = post.timestamp?.toDate()?.toString() ?: ""

        if (!post.imageUrl.isNullOrBlank()) {
            holder.imageView.visibility = View.VISIBLE
            Glide.with(holder.imageView.context)
                .load(post.imageUrl)
                .placeholder(R.drawable.placeholder)
                .into(holder.imageView)
        } else {
            holder.imageView.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = posts.size
}