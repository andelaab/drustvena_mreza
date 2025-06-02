package ba.sum.fpmoz.drustvenamreza.manager

import com.google.firebase.firestore.FirebaseFirestore

class FollowManager(private val db: FirebaseFirestore) {

    fun followUser(
        currentUserId: String,
        targetUserId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val currentUserRef = db.collection("users").document(currentUserId)
        val targetUserRef = db.collection("users").document(targetUserId)

        db.runBatch { batch ->
            batch.update(currentUserRef, "following.$targetUserId", true)
            batch.update(targetUserRef, "followers.$currentUserId", true)
        }.addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun unfollowUser(
        currentUserId: String,
        targetUserId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val currentUserRef = db.collection("users").document(currentUserId)
        val targetUserRef = db.collection("users").document(targetUserId)

        db.runBatch { batch ->
            batch.update(currentUserRef, mapOf("following.$targetUserId" to com.google.firebase.firestore.FieldValue.delete()))
            batch.update(targetUserRef, mapOf("followers.$currentUserId" to com.google.firebase.firestore.FieldValue.delete()))
        }.addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}
