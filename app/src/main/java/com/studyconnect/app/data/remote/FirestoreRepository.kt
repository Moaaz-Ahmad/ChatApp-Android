package com.studyconnect.app.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.studyconnect.app.data.model.ChatRoom
import com.studyconnect.app.data.model.Message
import com.studyconnect.app.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreRepository( 
    private val auth: FirebaseAuth = Firebase.auth,
    private val firestore: FirebaseFirestore = Firebase.firestore
) {

    private val usersCollection = firestore.collection("users")
    private val chatRoomsCollection = firestore.collection("chatRooms")

    suspend fun createChatRoom(title: String, memberEmails: List<String>): String {
        val currentUser = auth.currentUser ?: error("User must be authenticated")
        val normalizedTitle = title.ifBlank { "Untitled Room" }
        val memberIds = mutableSetOf(currentUser.uid)
        if (memberEmails.isNotEmpty()) {
            val missingEmails = mutableListOf<String>()
            memberEmails.forEach { email ->
                if (email == currentUser.email?.lowercase()) return@forEach
                val matchingUser = usersCollection
                    .whereEqualTo("email", email)
                    .limit(1)
                    .get()
                    .await()
                    .documents
                    .firstOrNull()
                if (matchingUser != null) {
                    memberIds.add(matchingUser.id)
                } else {
                    missingEmails.add(email)
                }
            }
            if (missingEmails.isNotEmpty()) {
                error("No StudyConnect account found for: ${missingEmails.joinToString()}")
            }
        }
        val payload = mapOf(
            "title" to normalizedTitle,
            "memberIds" to memberIds.toList(),
            "lastMessage" to "",
            "updatedAt" to FieldValue.serverTimestamp()
        )
        val roomRef = chatRoomsCollection.add(payload).await()
        return roomRef.id
    }

    fun chatRoomsFlow(userId: String): Flow<List<ChatRoom>> = callbackFlow {
        val listener = chatRoomsCollection
            .whereArrayContains("memberIds", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val rooms = snapshot?.documents
                    ?.map { it.toChatRoom() }
                    ?.sortedByDescending { it.updatedAt }
                    ?: emptyList()
                trySend(rooms)
            }
        awaitClose { listener.remove() }
    }

    fun chatRoomFlow(roomId: String): Flow<ChatRoom?> = callbackFlow {
        val listener = chatRoomsCollection.document(roomId)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.toChatRoom(roomId))
            }
        awaitClose { listener.remove() }
    }

    fun messagesFlow(roomId: String): Flow<List<Message>> = callbackFlow {
        val listener = chatRoomsCollection.document(roomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.map { it.toMessage(roomId) } ?: emptyList()
                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(roomId: String, content: String) {
        val user = auth.currentUser ?: error("User must be authenticated")
        if (content.isBlank()) return
        val roomSnapshot = chatRoomsCollection.document(roomId).get().await()
        if (!roomSnapshot.exists()) {
            error("Chat room not found")
        }
        val memberIds = (roomSnapshot.get("memberIds") as? List<*>)
            ?.filterIsInstance<String>()
            ?: emptyList()
        if (memberIds.isNotEmpty() && user.uid !in memberIds) {
            error("You are not a member of this room")
        }
        val displayName = fetchDisplayName(user.uid)
        val messagePayload = mapOf(
            "senderId" to user.uid,
            "senderName" to displayName,
            "content" to content.trim(),
            "timestamp" to FieldValue.serverTimestamp()
        )
        chatRoomsCollection.document(roomId)
            .collection("messages")
            .add(messagePayload)
            .await()
        chatRoomsCollection.document(roomId)
            .set(
                mapOf(
                    "memberIds" to memberIds,
                    "lastMessage" to content.trim(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            .await()
    }

    suspend fun ensureUserDocument(displayName: String) {
        val user = auth.currentUser ?: return
        val sanitizedName = displayName.ifBlank { user.email.orEmpty() }
        val profileUpdate = userProfileChangeRequest {
            this.displayName = sanitizedName
        }
        user.updateProfile(profileUpdate).await()
        val payload = User(
            uid = user.uid,
            email = user.email.orEmpty(),
            displayName = sanitizedName,
            photoUrl = user.photoUrl?.toString()
        )
        usersCollection.document(user.uid).set(payload, SetOptions.merge()).await()
    }

    suspend fun updateDisplayName(displayName: String) {
        val user = auth.currentUser ?: error("User must be authenticated")
        val profileRequest = userProfileChangeRequest {
            this.displayName = displayName
        }
        user.updateProfile(profileRequest).await()
        usersCollection.document(user.uid)
            .set(
                mapOf("displayName" to displayName),
                SetOptions.merge()
            )
            .await()
    }

    fun currentUserFlow(): Flow<User?> = callbackFlow {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            trySend(null)
            close()
        } else {
            val listener = usersCollection.document(firebaseUser.uid)
                .addSnapshotListener { snapshot, _ ->
                    val userModel = snapshot?.toUser(firebaseUser.email.orEmpty(), firebaseUser.uid)
                    trySend(userModel)
                }
            awaitClose { listener.remove() }
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun currentUserId(): String? = auth.currentUser?.uid

    private suspend fun fetchDisplayName(uid: String): String {
        val snapshot = usersCollection.document(uid).get().await()
        return snapshot.getString("displayName")
            ?: auth.currentUser?.displayName
            ?: auth.currentUser?.email
            ?: "Anonymous"
    }

    private fun DocumentSnapshot.toChatRoom(forcedId: String = id): ChatRoom {
        val memberIds = get("memberIds") as? List<*> ?: emptyList<Any>()
        val timestamp = getTimestamp("updatedAt")?.toDate()?.time ?: 0L
        return ChatRoom(
            id = forcedId,
            title = getString("title") ?: "Untitled Room",
            memberIds = memberIds.filterIsInstance<String>(),
            lastMessage = getString("lastMessage") ?: "",
            updatedAt = timestamp
        )
    }

    private fun DocumentSnapshot.toMessage(roomId: String): Message {
        val timestamp = getTimestamp("timestamp")?.toDate()?.time ?: 0L
        return Message(
            id = id,
            roomId = roomId,
            senderId = getString("senderId") ?: "",
            senderName = getString("senderName") ?: "",
            content = getString("content") ?: "",
            timestamp = timestamp
        )
    }

    private fun DocumentSnapshot.toUser(fallbackEmail: String, forcedId: String): User {
        return User(
            uid = forcedId,
            email = getString("email") ?: fallbackEmail,
            displayName = getString("displayName") ?: "",
            photoUrl = getString("photoUrl")
        )
    }
}
