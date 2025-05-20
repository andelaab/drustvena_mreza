package ba.sum.fpmoz.drustvenamreza.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Post(
    val content: String? = null,
    val timestamp: Any? = null,    // može biti Long ili Firebase Timestamp
    val imageUrl: String? = null
)
