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
    private lateinit var likesCountText: TextView
    private lateinit var commentsCountText: TextView

    private lateinit var editProfileBtn: Button
    private lateinit var logoutBtn: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Inicijalizacija Firebase-a
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Povezivanje UI elemenata
        profileImageView = view.findViewById(R.id.profileImageView)
        emailText = view.findViewById(R.id.profileEmail)
        fullNameText = view.findViewById(R.id.profileFullName)
        bioText = view.findViewById(R.id.profileBio)
        interestsText = view.findViewById(R.id.profileInterests)
        likesCountText = view.findViewById(R.id.likesCountText)
        commentsCountText = view.findViewById(R.id.commentsCountText)
        editProfileBtn = view.findViewById(R.id.btnEditProfile)
        logoutBtn = view.findViewById(R.id.btnLogout)

        // Učitavanje podataka korisnika
        loadUserData()

        // Listener za uređivanje profila
        editProfileBtn.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        // Listener za odjavu
        logoutBtn.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        // Osvježavanje podataka korisnika nakon povratka
        loadUserData()
    }

    private fun loadUserData() {
        val user = auth.currentUser
        if (user != null) {
            emailText.text = user.email

            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        fullNameText.text = document.getString("fullName") ?: "Nema imena"
                        bioText.text = document.getString("bio") ?: "Biografija nije dostupna"
                        interestsText.text = document.getString("interests") ?: "Nema interesa"
                        likesCountText.text = "Lajkova: ${document.getLong("likesCount") ?: 0}"
                        commentsCountText.text = "Komentara: ${document.getLong("commentsCount") ?: 0}"
                    } else {
                        Toast.makeText(requireContext(), "Korisnički podaci nisu pronađeni.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Greška: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}