package ba.sum.fpmoz.drustvenamreza

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var profileImageView: ImageView
    private lateinit var emailText: TextView
    private lateinit var fullNameText: TextView
    private lateinit var bioText: TextView
    private lateinit var interestsText: TextView
    private lateinit var followersCountText: TextView
    private lateinit var followingCountText: TextView

    private lateinit var editProfileBtn: Button
    private lateinit var logoutBtn: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        profileImageView = view.findViewById(R.id.profileImageView)
        emailText = view.findViewById(R.id.profileEmail)
        fullNameText = view.findViewById(R.id.profileFullName)
        bioText = view.findViewById(R.id.profileBio)
        interestsText = view.findViewById(R.id.profileInterests)
        followersCountText = view.findViewById(R.id.followersCountText)
        followingCountText = view.findViewById(R.id.followingCountText)
        editProfileBtn = view.findViewById(R.id.btnEditProfile)
        logoutBtn = view.findViewById(R.id.btnLogout)

        editProfileBtn.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        logoutBtn.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        followersCountText.setOnClickListener {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                val intent = Intent(requireContext(), FollowersListActivity::class.java)
                intent.putExtra("userId", userId)
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Greška: Korisnik nije prijavljen.", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }

    private fun loadUserData() {
        val user = auth.currentUser ?: return
        emailText.text = user.email ?: "Nepoznat email"

        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    fullNameText.text = document.getString("fullName") ?: "Nepoznato ime"
                    bioText.text = document.getString("bio") ?: "Biografija nije dostupna"
                    interestsText.text = document.getString("interests") ?: "Nema interesa"

                    val followersMap = document.get("followers") as? Map<*, *>
                    val followingMap = document.get("following") as? Map<*, *>
                    followersCountText.text = "Pratitelji: ${followersMap?.size ?: 0}"
                    followingCountText.text = "Prati: ${followingMap?.size ?: 0}"
                } else {
                    Toast.makeText(requireContext(), "Korisnički dokument ne postoji.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Greška pri dohvaćanju korisničkih podataka.", Toast.LENGTH_SHORT).show()
            }
    }
}
