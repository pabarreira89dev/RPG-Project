package pab.rpg.android.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_message")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val role: String, // "PLAYER" | "NARRATOR" | "SYSTEM"
    val text: String,
    val createdAt: String
)
