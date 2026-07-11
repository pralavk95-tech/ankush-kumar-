package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ChatMessage
import com.example.data.ChatPersona
import com.example.data.ChatRepository
import com.example.data.PersonaProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = ChatRepository(db.chatDao())

    // Currently selected persona
    val selectedPersona = MutableStateFlow(PersonaProvider.personas.first())

    // Input message state
    val messageInput = MutableStateFlow("")

    // Whether we are currently waiting for an AI response
    val isAILoading = MutableStateFlow(false)

    // Reactive flow of messages that switches automatically when selectedPersona changes
    val chatMessages: StateFlow<List<ChatMessage>> = selectedPersona
        .flatMapLatest { persona ->
            repository.getMessagesForPersona(persona.id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Check database on startup and pre-populate greetings if empty
        viewModelScope.launch {
            for (persona in PersonaProvider.personas) {
                val initialMessages = repository.getMessagesForPersona(persona.id).first()
                if (initialMessages.isEmpty()) {
                    repository.insertMessage(
                        ChatMessage(
                            personaId = persona.id,
                            content = persona.greeting,
                            isUser = false,
                            status = "sent"
                        )
                    )
                }
            }
        }
    }

    fun selectPersona(persona: ChatPersona) {
        selectedPersona.value = persona
    }

    fun updateInput(input: String) {
        messageInput.value = input
    }

    fun sendMessage() {
        val content = messageInput.value.trim()
        if (content.isEmpty()) return

        val currentPersona = selectedPersona.value
        messageInput.value = ""

        viewModelScope.launch {
            // 1. Insert User Message
            val userMsg = ChatMessage(
                personaId = currentPersona.id,
                content = content,
                isUser = true,
                status = "sent"
            )
            repository.insertMessage(userMsg)

            // 2. Set Loading
            isAILoading.value = true

            // 3. Get snapshot of current history for API context
            val currentHistory = chatMessages.value

            // 4. Send API call and insert response
            val responseText = repository.getGeminiResponse(
                personaId = currentPersona.id,
                systemInstruction = currentPersona.systemInstruction,
                history = currentHistory + userMsg
            )

            val aiMsg = ChatMessage(
                personaId = currentPersona.id,
                content = responseText,
                isUser = false,
                status = "sent"
            )
            repository.insertMessage(aiMsg)

            // 5. Clear loading
            isAILoading.value = false
        }
    }

    fun clearConversation() {
        viewModelScope.launch {
            val currentPersona = selectedPersona.value
            repository.clearConversation(currentPersona.id)
            
            // Re-seed with the persona greeting
            repository.insertMessage(
                ChatMessage(
                    personaId = currentPersona.id,
                    content = currentPersona.greeting,
                    isUser = false,
                    status = "sent"
                )
            )
        }
    }
}
