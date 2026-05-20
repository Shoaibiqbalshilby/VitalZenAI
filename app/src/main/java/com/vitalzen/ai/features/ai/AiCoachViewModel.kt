package com.vitalzen.ai.features.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalzen.ai.domain.model.Vitals
import com.vitalzen.ai.domain.repository.VitalsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiCoachViewModel @Inject constructor(
    private val vitalsRepository: VitalsRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage("Hello! I'm your VitalZen AI coach. I've analyzed your recent vitals. How can I help you today?", false))
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        viewModelScope.launch {
            _messages.value += ChatMessage(text, true)
            generateAiResponse(text)
        }
    }

    private suspend fun generateAiResponse(userMessage: String) {
        _isTyping.value = true
        
        // Fetch context
        val history = vitalsRepository.getVitalsHistory().first()
        val latestVitals = history.firstOrNull()
        
        val prompt = buildPrompt(userMessage, latestVitals, history)
        
        // Simulate AI processing delay
        delay(1500)
        _isTyping.value = false

        // Start streaming response
        val responseText = getMockAiResponse(userMessage, latestVitals)
        streamResponse(responseText)
    }

    private fun buildPrompt(userMessage: String, latest: Vitals?, history: List<Vitals>): String {
        return """
            Context: The user is using VitalZen AI wellness app.
            Latest Vitals: HR: ${latest?.heartRate}, BR: ${latest?.breathRate}, HRV: ${latest?.stressLevel}, Mood: ${latest?.mood}.
            Historical Trend: Analyzed ${history.size} recent scans.
            User Query: $userMessage
            Instructions: Act as a wellness coach. Provide suggestions for stress, breathing, sleep, or hydration. 
            Avoid medical claims. Be supportive and encouraging.
        """.trimIndent()
    }

    private fun streamResponse(fullText: String) {
        viewModelScope.launch {
            val streamingMessage = ChatMessage("", false, isStreaming = true)
            _messages.value += streamingMessage
            
            val words = fullText.split(" ")
            var currentText = ""
            
            words.forEachIndexed { index, word ->
                delay(100)
                currentText += if (index == 0) word else " $word"
                _messages.value = _messages.value.dropLast(1) + streamingMessage.copy(text = currentText)
            }
            
            _messages.value = _messages.value.dropLast(1) + ChatMessage(currentText, false)
        }
    }

    private fun getMockAiResponse(query: String, vitals: Vitals?): String {
        return when {
            query.contains("stress", ignoreCase = true) -> 
                "I noticed your stress levels have been ${vitals?.stressLevel ?: "varying"}. Try a 4-7-8 breathing exercise: Inhale for 4s, hold for 7s, exhale for 8s. It helps calm the nervous system."
            query.contains("sleep", ignoreCase = true) -> 
                "Quality sleep is vital. Based on your trend, try avoiding screens 1 hour before bed and keeping your room at 18°C (65°F) for optimal rest."
            query.contains("water", ignoreCase = true) || query.contains("hydration", ignoreCase = true) ->
                "Staying hydrated improves focus. Aim for at least 2L today. I can remind you to take a sip every hour if you'd like!"
            else -> "That's a great question. Your current heart rate of ${vitals?.heartRate ?: "--"} bpm suggests you're in a good state for some light stretching or a short walk to boost your mood."
        }
    }
}
