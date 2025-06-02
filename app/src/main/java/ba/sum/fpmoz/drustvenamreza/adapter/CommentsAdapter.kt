package ba.sum.fpmoz.drustvenamreza.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ba.sum.fpmoz.drustvenamreza.R
import ba.sum.fpmoz.drustvenamreza.model.Comment

class CommentsAdapter(
    private val comments: List<Comment>
) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userIdTextView: TextView = itemView.findViewById(R.id.commentUserId)
        val contentTextView: TextView = itemView.findViewById(R.id.commentContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.userIdTextView.text = comment.userFullName // ili prikaži korisničko ime ako ga imaš
        holder.contentTextView.text = comment.content
    }

    override fun getItemCount(): Int = comments.size
}
