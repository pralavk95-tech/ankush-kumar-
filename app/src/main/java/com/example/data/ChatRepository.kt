package com.example.data

import com.example.BuildConfig
import com.example.api.Content
import com.example.api.GenerateContentRequest
import com.example.api.Part
import com.example.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ChatRepository(private val chatDao: ChatDao) {

    fun getMessagesForPersona(personaId: String): Flow<List<ChatMessage>> {
        return chatDao.getMessagesForPersona(personaId)
    }

    suspend fun insertMessage(message: ChatMessage): Long = withContext(Dispatchers.IO) {
        chatDao.insert(message)
    }

    suspend fun updateMessage(message: ChatMessage) = withContext(Dispatchers.IO) {
        chatDao.update(message)
    }

    suspend fun clearConversation(personaId: String) = withContext(Dispatchers.IO) {
        chatDao.clearConversation(personaId)
    }

    suspend fun getGeminiResponse(
        personaId: String,
        systemInstruction: String,
        history: List<ChatMessage>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API Key not found or invalid! Please configure your GEMINI_API_KEY in the Secrets panel in AI Studio."
        }

        // Map ChatMessage history to Gemini Content structure
        // Keep the latest ~15 messages to respect token limits and speed up responses
        val maxHistorySize = 15
        val recentHistory = if (history.size > maxHistorySize) {
            history.takeLast(maxHistorySize)
        } else {
            history
        }

        val contentsList = recentHistory.map { msg ->
            Content(
                role = if (msg.isUser) "user" else "model",
                parts = listOf(Part(text = msg.content))
            )
        }

        val request = GenerateContentRequest(
            contents = contentsList,
            systemInstruction = Content(
                parts = listOf(Part(text = systemInstruction))
            )
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            responseText ?: "No response from chat assistant."
        } catch (e: Exception) {
            "Connection failed: ${e.message}"
        }
    }
}
