package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE personaId = :personaId ORDER BY timestamp ASC")
    fun getMessagesForPersona(personaId: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessage): Long

    @Update
    suspend fun update(message: ChatMessage)

    @Delete
    suspend fun delete(message: ChatMessage)

    @Query("DELETE FROM chat_messages WHERE personaId = :personaId")
    suspend fun clearConversation(personaId: String)
}
