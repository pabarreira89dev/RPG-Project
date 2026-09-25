package pab.rpg.android.data

import java.time.Instant
import kotlinx.coroutines.flow.Flow
import pab.rpg.android.data.local.AppDatabase
import pab.rpg.android.data.local.ChatMessageDao
import pab.rpg.android.data.local.ChatMessageEntity

class ChatHistoryRepository(private val dao: ChatMessageDao = AppDatabase.instance.chatMessageDao()) {

    fun observeMessages(sessionId: String): Flow<List<ChatMessageEntity>> = dao.observeMessages(sessionId)

    suspend fun addMessage(sessionId: String, role: String, text: String) {
        dao.insert(ChatMessageEntity(sessionId = sessionId, role = role, text = text, createdAt = Instant.now().toString()))
    }
}
