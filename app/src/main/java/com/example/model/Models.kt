package com.example.model

enum class JarvisState {
  STANDBY,
  LISTENING,
  PROCESSING,
  SPEAKING,
  IDLE
}

enum class JarvisPersonaMode(
  val title: String,
  val subtitle: String,
  val iconEmoji: String,
  val defaultPitch: Float,
  val defaultRate: Float
) {
  GF_MODE("GF Mode", "Warm, loving, caring & sweet companion", "❤️", 1.05f, 0.98f),
  BOSS_MODE("Boss Mode", "Executive secretary & chief commander", "💼", 0.95f, 1.02f),
  TECH_MODE("Technical / Security", "Elite automation, scripting & cyber expert", "⚡", 0.90f, 1.05f)
}

enum class SystemMode(val displayName: String, val subtitle: String) {
  NOMINAL("Nominal (Active)", "Full assistance, voice activation ready"),
  BUSY("Busy (Away / Focus)", "Screening callers, logging notifications"),
  SLEEPING("Sleeping (Night Rest)", "Silent background monitoring, auto-replying")
}

enum class MessageSender {
  USER,
  JARVIS
}

data class ChatMessage(
  val id: String = System.currentTimeMillis().toString(),
  val sender: MessageSender,
  val text: String,
  val timestamp: Long = System.currentTimeMillis(),
  val codeSnippet: String? = null,
  val language: String? = null,
  val suggestedActions: List<String> = emptyList(),
  val personaMode: JarvisPersonaMode = JarvisPersonaMode.BOSS_MODE
)

data class DeviceTelemetry(
  val batteryLevel: Int = 85,
  val isCharging: Boolean = false,
  val ramUsedMb: Long = 3200,
  val ramTotalMb: Long = 8192,
  val storageFreeGb: Double = 64.5,
  val storageTotalGb: Double = 128.0,
  val networkType: String = "WiFi (Nominal)",
  val uptimeText: String = "14h 22m"
)

enum class AppCategory {
  SYSTEM,
  COMMUNICATION,
  DEVELOPMENT,
  NAVIGATION,
  MEDIA,
  PRODUCTIVITY
}

data class AppAutomation(
  val id: String,
  val name: String,
  val packageName: String,
  val category: AppCategory,
  val description: String,
  val directActionKey: String,
  val stepByStepGuide: List<String>,
  val automationScript: String? = null
)

data class CommunicationLog(
  val id: String,
  val contactName: String,
  val contactDetail: String,
  val type: String, // "Incoming Call", "SMS Message", "WhatsApp Notice", "Email"
  val timeFormatted: String,
  val originalMessage: String,
  val autoResponseSent: String,
  val isUrgent: Boolean = false,
  val reviewed: Boolean = false
)

data class CommunicationBriefing(
  val totalMissed: Int,
  val urgentCount: Int,
  val callsSummary: List<String>,
  val messagesSummary: List<String>,
  val timestamp: String,
  val formattedSpeechBrief: String
)

data class DiagnosticReport(
  val targetHost: String,
  val pingLatencyMs: Long?,
  val status: String,
  val openPorts: List<Int>,
  val vulnerabilityNotes: List<String>,
  val protocolSummary: String,
  val timestamp: Long = System.currentTimeMillis()
)

data class VoiceSettings(
  val pitch: Float = 0.95f,
  val speechRate: Float = 1.0f,
  val autoSpeakResponse: Boolean = true,
  val voiceName: String = "Warm Chief of Staff (British/Natural)",
  val wakeWordEnabled: Boolean = true,
  val ownerName: String = "Boss"
)
