package ba.sum.fpmoz.drustvenamreza.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ba.sum.fpmoz.drustvenamreza.R

class FollowersAdapter(private val followers: List<String>) : RecyclerView.Adapter<FollowersAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val followerName: TextView = view.findViewById(R.id.followerName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_follower, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.followerName.text = followers[position] // trenutno prikazuje samo UID
    }

    override fun getItemCount(): Int = followers.size
}
