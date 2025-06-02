package ba.sum.fpmoz.drustvenamreza.model
import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties


data class Post(
    val id: String? = null,
    val userId: String? = null,
    val userName: String? = null,
    val content: String? = null,
    val imageUrl: String? = null,
    val timestamp: Timestamp? = null, // Promijenjeno iz Long u Timestamp
    val likes: Map<String, Boolean>? = null,
    val commentCount: Int? = 0
)