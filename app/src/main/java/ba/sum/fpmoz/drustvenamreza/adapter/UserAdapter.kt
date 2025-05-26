package ba.sum.fpmoz.drustvenamreza.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ba.sum.fpmoz.drustvenamreza.R
import ba.sum.fpmoz.drustvenamreza.UserProfileActivity
import ba.sum.fpmoz.drustvenamreza.model.User

class UserAdapter(private val users: List<User>) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fullNameTextView: TextView = itemView.findViewById(R.id.fullNameTextView)
        val emailTextView: TextView = itemView.findViewById(R.id.emailTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.fullNameTextView.text = user.fullName
        holder.emailTextView.text = user.email

        // Log poruka za prikaz podataka korisnika
        Log.d("UserAdapter", "Prikaz korisnika: fullName=${user.fullName}, email=${user.email}")

        // Klik vodi na UserProfileActivity
        holder.itemView.setOnClickListener {
            Log.d("UserAdapter", "Kliknuto na korisnika: fullName=${user.fullName}, email=${user.email}")
            val context = holder.itemView.context
            val intent = Intent(context, UserProfileActivity::class.java)
            intent.putExtra("fullName", user.fullName)
            intent.putExtra("email", user.email)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = users.size
}