package com.example.data

import com.example.model.CommunicationBriefing
import com.example.model.CommunicationLog
import com.example.model.SystemMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommunicationRepository {

  private val _logs = MutableStateFlow<List<CommunicationLog>>(createInitialLogs("Boss"))
  val logs: StateFlow<List<CommunicationLog>> = _logs.asStateFlow()

  fun addSimulatedEvent(
    name: String,
    detail: String,
    type: String,
    message: String,
    currentMode: SystemMode,
    ownerName: String = "Boss"
  ): CommunicationLog {
    val autoReply = when (currentMode) {
      SystemMode.SLEEPING ->
        "Hey, $ownerName is currently resting right now. I'll make sure they see this as soon as they wake up."
      SystemMode.BUSY ->
        "Hey, $ownerName is currently away/busy. They'll get back to you shortly."
      SystemMode.NOMINAL ->
        "Jarvis OS acknowledged your alert. Dispatched directly to $ownerName's heads-up display."
    }

    val isUrgent = message.contains("urgent", ignoreCase = true) ||
                   message.contains("emergency", ignoreCase = true) ||
                   message.contains("asap", ignoreCase = true)

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val newLog = CommunicationLog(
      id = System.currentTimeMillis().toString(),
      contactName = name,
      contactDetail = detail,
      type = type,
      timeFormatted = timeFormat.format(Date()),
      originalMessage = message,
      autoResponseSent = autoReply,
      isUrgent = isUrgent,
      reviewed = false
    )

    _logs.value = listOf(newLog) + _logs.value
    return newLog
  }

  fun markReviewed(id: String) {
    _logs.value = _logs.value.map {
      if (it.id == id) it.copy(reviewed = true) else it
    }
  }

  fun clearAll() {
    _logs.value = emptyList()
  }

  fun generateBriefing(ownerName: String): CommunicationBriefing {
    val currentLogs = _logs.value
    val missedCalls = currentLogs.filter { it.type.contains("Call", ignoreCase = true) }
    val missedMessages = currentLogs.filter { !it.type.contains("Call", ignoreCase = true) }
    val urgentItems = currentLogs.filter { it.isUrgent }

    val callsSummary = missedCalls.map { "${it.contactName} at ${it.timeFormatted}: ${it.originalMessage}" }
    val messagesSummary = missedMessages.map { "${it.contactName} (${it.type}) at ${it.timeFormatted}: ${it.originalMessage}" }

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timestamp = timeFormat.format(Date())

    val speechBrief = buildString {
      append("Good day, $ownerName. Here is your consolidated communication briefing. ")
      if (currentLogs.isEmpty()) {
        append("You have zero missed communications. All channels are completely clear and nominal.")
      } else {
        append("You have ${currentLogs.size} total intercepted communications, including ${missedCalls.size} missed calls and ${missedMessages.size} incoming messages. ")
        if (urgentItems.isNotEmpty()) {
          append("There are ${urgentItems.size} high priority matters requiring your attention, notably from ${urgentItems.first().contactName}. ")
        } else {
          append("All items were screened with your custom auto-replies, and none were flagged as critical. ")
        }
        append("All records are preserved in your Triage Hub.")
      }
    }

    return CommunicationBriefing(
      totalMissed = currentLogs.size,
      urgentCount = urgentItems.size,
      callsSummary = callsSummary,
      messagesSummary = messagesSummary,
      timestamp = timestamp,
      formattedSpeechBrief = speechBrief
    )
  }

  private fun createInitialLogs(ownerName: String): List<CommunicationLog> = listOf(
    CommunicationLog(
      id = "101",
      contactName = "Sarah Jenkins (Project Lead)",
      contactDetail = "+1 (555) 019-2834",
      type = "Incoming Call",
      timeFormatted = "06:45",
      originalMessage = "Missed call (Duration: 3 rings). Followed up: 'Need approval on the Q4 deployment architecture'.",
      autoResponseSent = "Hey, $ownerName is currently resting right now. I'll make sure they see this as soon as they wake up.",
      isUrgent = true,
      reviewed = false
    ),
    CommunicationLog(
      id = "102",
      contactName = "Cloud Infrastructure Alert",
      contactDetail = "alerts@system-ops.io",
      type = "System Notification",
      timeFormatted = "05:12",
      originalMessage = "K8s cluster pod autoscaling event: CPU reached 78% nominal threshold on production node 3.",
      autoResponseSent = "Hey, $ownerName is currently away/busy. They'll get back to you shortly.",
      isUrgent = false,
      reviewed = true
    ),
    CommunicationLog(
      id = "103",
      contactName = "David Chen (Security Team)",
      contactDetail = "+1 (555) 782-9912",
      type = "SMS Message",
      timeFormatted = "03:30",
      originalMessage = "Morning boss, can we schedule 15 mins today to review the penetration test report?",
      autoResponseSent = "Hey, $ownerName is currently resting right now. I'll make sure they see this as soon as they wake up.",
      isUrgent = false,
      reviewed = false
    )
  )
}
