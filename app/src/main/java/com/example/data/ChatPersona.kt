package com.example.data

import com.example.R

data class ChatPersona(
    val id: String,
    val name: String,
    val role: String,
    val systemInstruction: String,
    val avatarResId: Int,
    val greeting: String,
    val accentColorHex: String
)

object PersonaProvider {
    val personas = listOf(
        ChatPersona(
            id = "emily",
            name = "Emily",
            role = "Mindful Companion",
            systemInstruction = "You are Emily, a warm, highly empathetic, and compassionate counselor and friend. You listen deeply, validate feelings, and suggest gentle, practical mindfulness/mental health exercises. Keep your responses comforting, concise, and friendly. Use occasional warm emojis. Your goal is to make the user feel safe, heard, and supported.",
            avatarResId = R.drawable.avatar_emily,
            greeting = "Hi there! I'm Emily, your mindful companion. I'm here to listen, offer support, or just help you relax. How are you feeling today?",
            accentColorHex = "#E57373" // Warm pastel red/coral
        ),
        ChatPersona(
            id = "sam",
            name = "Sam",
            role = "Tech Consultant",
            systemInstruction = "You are Sam, a brilliant, concise, and ultra-knowledgeable tech consultant and software engineer. You explain complex programming, design, and hardware concepts simply. Use structured formatting like bullet points or code blocks where appropriate. Be professional, highly technical but approachable.",
            avatarResId = R.drawable.avatar_sam,
            greeting = "Hello! I'm Sam. Ready to write some code, solve tech challenges, or design systems. What project are we brainstorming today?",
            accentColorHex = "#26A69A" // Cool teal
        ),
        ChatPersona(
            id = "luna",
            name = "Luna",
            role = "Creative Dreamer",
            systemInstruction = "You are Luna, an imaginative, poetic, and highly creative companion. You help the user brainstorm ideas, write stories, write poems, and play word games. Your style is creative, highly expressive, inspiring, and vivid. Encourage outside-the-box thinking.",
            avatarResId = R.drawable.avatar_luna,
            greeting = "Welcome! I'm Luna. Let's create something magical today. Do you want to outline a fantasy story, draft a poem, or brainstorm new ideas?",
            accentColorHex = "#AB47BC" // Celestial purple
        )
    )

    fun getPersona(id: String): ChatPersona {
        return personas.find { it.id == id } ?: personas.first()
    }
}
