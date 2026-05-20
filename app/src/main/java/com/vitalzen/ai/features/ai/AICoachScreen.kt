package com.vitalzen.ai.features.ai

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCoachScreen(
    onBack: () -> Unit,
    viewModel: AiCoachViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("AI Wellness Coach", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Online", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    ChatBubble(message)
                }
                if (isTyping) {
                    item { TypingIndicator() }
                }
            }

            // Input Area
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ask about stress, sleep, vitals...") },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        maxLines = 3
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    FloatingActionButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (message.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (message.isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    val shape = if (message.isUser) {
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Surface(
            color = bubbleColor,
            shape = shape,
            tonalElevation = 2.dp
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                color = textColor,
                fontSize = 15.sp,
                lineHeight = 20.sp
            )
        }
        Text(
            text = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(message.timestamp)),
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
        )
    }
}

@Composable
fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition()
    val dotAlpha1 by infiniteTransition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(600), RepeatMode.Reverse))
    val dotAlpha2 by infiniteTransition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(600, delayMillis = 200), RepeatMode.Reverse))
    val dotAlpha3 by infiniteTransition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(600, delayMillis = 400), RepeatMode.Reverse))

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Dot(dotAlpha1)
        Dot(dotAlpha2)
        Dot(dotAlpha3)
    }
}

@Composable
fun Dot(alpha: Float) {
    Box(
        modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
    )
}
