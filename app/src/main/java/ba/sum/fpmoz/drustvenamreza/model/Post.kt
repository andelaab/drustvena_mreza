package ba.sum.fpmoz.drustvenamreza.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Post(
    val content: String? = null,
    val timestamp: Any? = null,
    val imageUrl: String? = null,
    val likes: List<String>? = listOf(),
    var id: String? = null
)
