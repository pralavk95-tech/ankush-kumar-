package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ChatMessage
import com.example.data.ChatPersona
import com.example.data.PersonaProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val activePersona by viewModel.selectedPersona.collectAsState()
    val messages by viewModel.chatMessages.collectAsState()
    val isAILoading by viewModel.isAILoading.collectAsState()
    val currentInput by viewModel.messageInput.collectAsState()

    var showClearDialog by remember { mutableStateOf(false) }

    // Resolve specific persona color safely
    val activeAccentColor = remember(activePersona) {
        try {
            Color(android.graphics.Color.parseColor(activePersona.accentColorHex))
        } catch (e: Exception) {
            Color(0xFF6200EE)
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(text = "Clear Chat History?") },
            text = { Text(text = "This will erase all conversation logs with ${activePersona.name} and restore their initial greeting.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearConversation()
                        showClearDialog = false
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > 600.dp

        if (isWideScreen) {
            // Adaptive Two-Pane Split Layout
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Panel: Persona List and Details (1/3 of space)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    tonalElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "AI Personas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Choose your AI conversation partner. Each assistant possesses a distinct personality and expertise.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        PersonaListPane(
                            personas = PersonaProvider.personas,
                            selectedPersona = activePersona,
                            onPersonaSelected = { viewModel.selectPersona(it) },
                            activeAccentColor = activeAccentColor
                        )
                    }
                }

                // Right Panel: Chat Window (2/3 of space)
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight()
                ) {
                    ChatWindowContent(
                        activePersona = activePersona,
                        messages = messages,
                        isAILoading = isAILoading,
                        currentInput = currentInput,
                        activeAccentColor = activeAccentColor,
                        onInputChanged = { viewModel.updateInput(it) },
                        onSendClicked = { viewModel.sendMessage() },
                        onClearClicked = { showClearDialog = true },
                        onPersonaSelected = { viewModel.selectPersona(it) },
                        isWideScreen = true
                    )
                }
            }
        } else {
            // Compact Mobile Layout
            ChatWindowContent(
                activePersona = activePersona,
                messages = messages,
                isAILoading = isAILoading,
                currentInput = currentInput,
                activeAccentColor = activeAccentColor,
                onInputChanged = { viewModel.updateInput(it) },
                onSendClicked = { viewModel.sendMessage() },
                onClearClicked = { showClearDialog = true },
                onPersonaSelected = { viewModel.selectPersona(it) },
                isWideScreen = false
            )
        }
    }
}

@Composable
fun PersonaListPane(
    personas: List<ChatPersona>,
    selectedPersona: ChatPersona,
    onPersonaSelected: (ChatPersona) -> Unit,
    activeAccentColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        personas.forEach { persona ->
            val isSelected = persona.id == selectedPersona.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPersonaSelected(persona) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        activeAccentColor.copy(alpha = 0.15f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                shape = RoundedCornerShape(16.dp),
                border = if (isSelected) {
                    androidx.compose.foundation.BorderStroke(2.dp, activeAccentColor)
                } else null,
                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = persona.avatarResId),
                        contentDescription = persona.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = persona.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) activeAccentColor else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = persona.role,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatWindowContent(
    activePersona: ChatPersona,
    messages: List<ChatMessage>,
    isAILoading: Boolean,
    currentInput: String,
    activeAccentColor: Color,
    onInputChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    onClearClicked: () -> Unit,
    onPersonaSelected: (ChatPersona) -> Unit,
    isWideScreen: Boolean
) {
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    var showMenu by remember { mutableStateOf(false) }

    // Scroll to bottom when a new message arrives
    LaunchedEffect(messages.size, isAILoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.statusBarsPadding()) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = activePersona.avatarResId),
                                contentDescription = activePersona.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = activePersona.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = activePersona.role,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = onClearClicked,
                            modifier = Modifier.testTag("clear_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteSweep,
                                contentDescription = "Clear History",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = currentInput,
                        onValueChange = onInputChanged,
                        placeholder = { Text("Chat with ${activePersona.name}...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input"),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (currentInput.isNotBlank()) {
                                    onSendClicked()
                                    keyboardController?.hide()
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent,
                            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            cursorColor = activeAccentColor
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = {
                            if (currentInput.isNotBlank()) {
                                onSendClicked()
                                keyboardController?.hide()
                            }
                        },
                        enabled = currentInput.isNotBlank(),
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (currentInput.isNotBlank()) activeAccentColor else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .testTag("send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (currentInput.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Mobile-only top persona selector
            if (!isWideScreen) {
                HorizontalPersonaSelector(
                    personas = PersonaProvider.personas,
                    selectedPersona = activePersona,
                    onPersonaSelected = onPersonaSelected,
                    activeAccentColor = activeAccentColor
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (messages.isEmpty() && !isAILoading) {
                    // Empty State
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = null,
                            tint = activeAccentColor.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No messages yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Type something below to start chatting with ${activePersona.name}!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(messages) { message ->
                            MessageBubble(
                                message = message,
                                activeAccentColor = activeAccentColor
                            )
                        }

                        if (isAILoading) {
                            item {
                                TypingIndicator(activePersona = activePersona, activeAccentColor = activeAccentColor)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HorizontalPersonaSelector(
    personas: List<ChatPersona>,
    selectedPersona: ChatPersona,
    onPersonaSelected: (ChatPersona) -> Unit,
    activeAccentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        personas.forEach { persona ->
            val isSelected = persona.id == selectedPersona.id
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onPersonaSelected(persona) }
                    .padding(8.dp)
            ) {
                Image(
                    painter = painterResource(id = persona.avatarResId),
                    contentDescription = persona.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .then(
                            if (isSelected) Modifier.background(activeAccentColor.copy(alpha = 0.2f)) else Modifier
                        )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = persona.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) activeAccentColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    activeAccentColor: Color
) {
    val isUser = message.isUser
    val timeString = remember(message.timestamp) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    val bubbleShape = if (isUser) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    val bubbleColor = if (isUser) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isUser) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val alignment = if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(bubbleColor, bubbleShape)
                .padding(12.dp)
        ) {
            Column {
                // If it contains triple backticks, parse out as potential code block
                if (!isUser && message.content.contains("```")) {
                    MarkdownContent(content = message.content, textColor = textColor)
                } else {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = textColor.copy(alpha = 0.75f)
                    )
                    if (isUser) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Read",
                            tint = textColor.copy(alpha = 0.8f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MarkdownContent(
    content: String,
    textColor: Color
) {
    val clipboardManager = LocalClipboardManager.current
    val parts = content.split("```")

    Column {
        parts.forEachIndexed { index, part ->
            if (index % 2 == 1) {
                // Code block content
                val lines = part.trim().split("\n")
                val language = lines.firstOrNull() ?: ""
                val code = if (lines.size > 1) lines.drop(1).joinToString("\n") else ""

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = language.uppercase(Locale.ROOT).ifEmpty { "CODE" },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                fontFamily = FontFamily.Monospace
                            )
                            IconButton(
                                onClick = { clipboardManager.setText(AnnotatedString(code)) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy code",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = code,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE0E0E0),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            } else {
                // Standard text part
                if (part.isNotBlank()) {
                    Text(
                        text = part,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun TypingIndicator(
    activePersona: ChatPersona,
    activeAccentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
            modifier = Modifier.padding(end = 12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = activeAccentColor
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "${activePersona.name} is thinking...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
