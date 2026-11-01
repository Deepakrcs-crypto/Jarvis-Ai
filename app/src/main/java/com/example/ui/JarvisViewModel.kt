package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.JarvisBrainEngine
import com.example.data.AppAutomationCatalog
import com.example.data.CommunicationRepository
import com.example.data.TelemetryProvider
import com.example.model.ChatMessage
import com.example.model.CommunicationBriefing
import com.example.model.CommunicationLog
import com.example.model.DeviceTelemetry
import com.example.model.DiagnosticReport
import com.example.model.JarvisPersonaMode
import com.example.model.JarvisState
import com.example.model.MessageSender
import com.example.model.SystemMode
import com.example.model.VoiceSettings
import com.example.tech.CodeTemplate
import com.example.tech.SecurityTopic
import com.example.tech.TechDiagnosticEngine
import com.example.voice.JarvisVoiceManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

  private val telemetryProvider = TelemetryProvider(application)
  private val commsRepo = CommunicationRepository()
  private val techEngine = TechDiagnosticEngine()
  private val brainEngine = JarvisBrainEngine()

  var voiceManager: JarvisVoiceManager? = null

  private val _jarvisState = MutableStateFlow(JarvisState.IDLE)
  val jarvisState: StateFlow<JarvisState> = _jarvisState.asStateFlow()

  private val _personaMode = MutableStateFlow(JarvisPersonaMode.BOSS_MODE)
  val personaMode: StateFlow<JarvisPersonaMode> = _personaMode.asStateFlow()

  private val _systemMode = MutableStateFlow(SystemMode.NOMINAL)
  val systemMode: StateFlow<SystemMode> = _systemMode.asStateFlow()

  private val _telemetry = MutableStateFlow(telemetryProvider.fetchCurrentTelemetry())
  val telemetry: StateFlow<DeviceTelemetry> = _telemetry.asStateFlow()

  private val _messages = MutableStateFlow<List<ChatMessage>>(initialMessages())
  val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

  private val _voiceSettings = MutableStateFlow(VoiceSettings())
  val voiceSettings: StateFlow<VoiceSettings> = _voiceSettings.asStateFlow()

  val commsLogs: StateFlow<List<CommunicationLog>> = commsRepo.logs.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    emptyList()
  )

  private val _activeBriefing = MutableStateFlow<CommunicationBriefing?>(null)
  val activeBriefing: StateFlow<CommunicationBriefing?> = _activeBriefing.asStateFlow()

  private val _diagnosticReport = MutableStateFlow<DiagnosticReport?>(null)
  val diagnosticReport: StateFlow<DiagnosticReport?> = _diagnosticReport.asStateFlow()

  private val _isDiagnosing = MutableStateFlow(false)
  val isDiagnosing: StateFlow<Boolean> = _isDiagnosing.asStateFlow()

  private val _selectedTab = MutableStateFlow(0)
  val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

  private val _statusNotice = MutableStateFlow<String?>(null)
  val statusNotice: StateFlow<String?> = _statusNotice.asStateFlow()

  private var telemetryJob: Job? = null

  init {
    initVoice()
    startTelemetryLoop()
  }

  fun setTab(index: Int) {
    _selectedTab.value = index
  }

  fun setPersonaMode(mode: JarvisPersonaMode) {
    _personaMode.value = mode
    updateVoiceSettings(mode.defaultPitch, mode.defaultRate, _voiceSettings.value.autoSpeakResponse)

    val greeting = when (mode) {
      JarvisPersonaMode.GF_MODE -> "GF Mode engaged! Hey sweetheart ❤️ I'm here to care for you and make your day wonderful."
      JarvisPersonaMode.BOSS_MODE -> "Boss Mode engaged. Executive command line active, sir. Absolute obedience and peak efficiency."
      JarvisPersonaMode.TECH_MODE -> "[SEC_OPS // ONLINE] Technical & Security Mode armed. Unrestricted automation and scripting engine engaged."
    }

    showNotice(greeting)
    speakPhrase(greeting)
  }

  fun setOwnerName(name: String) {
    val clean = name.trim().ifBlank { "Boss" }
    _voiceSettings.value = _voiceSettings.value.copy(ownerName = clean)
    showNotice("Owner designation updated to: $clean")
  }

  fun initVoice() {
    voiceManager = JarvisVoiceManager(
      context = getApplication(),
      onSpeechResult = { recognizedText ->
        onUserVoiceInput(recognizedText)
      },
      onSpeechError = { errorMsg ->
        if (_jarvisState.value == JarvisState.LISTENING) {
          _jarvisState.value = JarvisState.IDLE
        }
        showNotice(errorMsg)
      }
    )

    viewModelScope.launch {
      voiceManager?.isSpeaking?.collect { speaking ->
        if (speaking && _jarvisState.value != JarvisState.STANDBY) {
          _jarvisState.value = JarvisState.SPEAKING
        } else if (!speaking && _jarvisState.value == JarvisState.SPEAKING) {
          _jarvisState.value = JarvisState.IDLE
        }
      }
    }
  }

  private fun startTelemetryLoop() {
    telemetryJob?.cancel()
    telemetryJob = viewModelScope.launch {
      while (isActive) {
        _telemetry.value = telemetryProvider.fetchCurrentTelemetry()
        delay(6000)
      }
    }
  }

  fun toggleVoiceListening() {
    if (_jarvisState.value == JarvisState.LISTENING) {
      voiceManager?.stopListening()
      _jarvisState.value = JarvisState.IDLE
    } else {
      voiceManager?.startListening()
      _jarvisState.value = JarvisState.LISTENING
    }
  }

  fun toggleStandby() {
    if (_jarvisState.value == JarvisState.STANDBY) {
      _jarvisState.value = JarvisState.IDLE
      speakPhrase("Jarvis operational. Standing by for instructions, ${voiceSettings.value.ownerName}.")
    } else {
      _jarvisState.value = JarvisState.STANDBY
      voiceManager?.stopSpeaking()
      voiceManager?.stopListening()
      showNotice("Jarvis entered Standby Mode.")
    }
  }

  fun setSystemMode(mode: SystemMode) {
    _systemMode.value = mode
    val owner = _voiceSettings.value.ownerName
    val modeText = when (mode) {
      SystemMode.NOMINAL -> "Nominal Mode active. Full voice assistance and background monitors online."
      SystemMode.BUSY -> "Busy Mode active. Transmissions screened with: \"Hey, $owner is currently away/busy. They'll get back to you shortly.\""
      SystemMode.SLEEPING -> "Night Protocol active. Transmissions screened with: \"Hey, $owner is currently resting right now. I'll make sure they see this as soon as they wake up.\""
    }
    showNotice(modeText)
    speakPhrase(modeText)
  }

  fun onUserVoiceInput(text: String) {
    sendCommand(text)
  }

  fun sendCommand(commandText: String) {
    if (commandText.isBlank()) return

    val userMsg = ChatMessage(
      sender = MessageSender.USER,
      text = commandText,
      personaMode = _personaMode.value
    )
    _messages.value = _messages.value + userMsg
    _jarvisState.value = JarvisState.PROCESSING

    viewModelScope.launch {
      val lower = commandText.lowercase()
      if (lower.contains("jarvis on") || lower.contains("wake up")) {
        _jarvisState.value = JarvisState.IDLE
      } else if (lower.contains("jarvis off") || lower.contains("standby")) {
        _jarvisState.value = JarvisState.STANDBY
      }

      val result = brainEngine.processCommand(
        input = commandText,
        currentSystemMode = _systemMode.value,
        currentPersona = _personaMode.value,
        ownerName = _voiceSettings.value.ownerName,
        history = _messages.value
      )

      if (result.switchPersona != null) {
        setPersonaMode(result.switchPersona)
      }
      if (result.switchSystemMode != null) {
        setSystemMode(result.switchSystemMode)
      }
      if (result.triggerBriefing) {
        generateBriefing()
      }

      _messages.value = _messages.value + result.message
      _jarvisState.value = if (_jarvisState.value == JarvisState.STANDBY) JarvisState.STANDBY else JarvisState.IDLE

      if (_voiceSettings.value.autoSpeakResponse && _jarvisState.value != JarvisState.STANDBY) {
        voiceManager?.speak(result.message.text)
      }
    }
  }

  fun generateBriefing() {
    val owner = _voiceSettings.value.ownerName
    val briefing = commsRepo.generateBriefing(owner)
    _activeBriefing.value = briefing

    val summaryText = buildString {
      append("📋 **Executive Communication Briefing (${briefing.timestamp})**\n")
      append("• Total Intercepted: ${briefing.totalMissed}\n")
      append("• Urgent Flags: ${briefing.urgentCount}\n")
      if (briefing.callsSummary.isNotEmpty()) {
        append("\n**Calls Screened:**\n")
        briefing.callsSummary.forEach { append("  - $it\n") }
      }
      if (briefing.messagesSummary.isNotEmpty()) {
        append("\n**Messages Logged:**\n")
        briefing.messagesSummary.forEach { append("  - $it\n") }
      }
    }

    val briefMessage = ChatMessage(
      sender = MessageSender.JARVIS,
      text = summaryText,
      personaMode = _personaMode.value,
      suggestedActions = listOf("Mark All Reviewed", "Clear Logs", "Resume Nominal Mode")
    )

    _messages.value = _messages.value + briefMessage
    showNotice("Briefing generated: ${briefing.totalMissed} items screened.")

    if (_voiceSettings.value.autoSpeakResponse && _jarvisState.value != JarvisState.STANDBY) {
      voiceManager?.speak(briefing.formattedSpeechBrief)
    }
  }

  fun dismissBriefing() {
    _activeBriefing.value = null
  }

  fun executeAppDirectAction(actionKey: String): String {
    val result = AppAutomationCatalog.executeDirectAction(getApplication(), actionKey)
    showNotice(result)
    speakPhrase(result)
    return result
  }

  fun runNetworkDiagnostic(host: String) {
    viewModelScope.launch {
      _isDiagnosing.value = true
      _jarvisState.value = JarvisState.PROCESSING
      val report = techEngine.runNetworkDiagnostic(host)
      _diagnosticReport.value = report
      _isDiagnosing.value = false
      _jarvisState.value = JarvisState.IDLE
      val summary = "Network diagnostic completed for ${report.targetHost}. Status: ${report.status}."
      showNotice(summary)
      speakPhrase(summary)
    }
  }

  fun simulateIncomingCommunication(
    name: String,
    detail: String,
    type: String,
    message: String
  ) {
    val log = commsRepo.addSimulatedEvent(
      name = name,
      detail = detail,
      type = type,
      message = message,
      currentMode = _systemMode.value,
      ownerName = _voiceSettings.value.ownerName
    )
    showNotice("New ${log.type} triaged from ${log.contactName}.")
  }

  fun markCommsReviewed(id: String) {
    commsRepo.markReviewed(id)
  }

  fun clearAllComms() {
    commsRepo.clearAll()
  }

  fun getSecurityTopics(): List<SecurityTopic> = techEngine.getEthicalHackingTopics()

  fun getCodeTemplates(): List<CodeTemplate> = techEngine.getCodeGeneratorTemplates()

  fun updateVoiceSettings(pitch: Float, rate: Float, autoSpeak: Boolean) {
    _voiceSettings.value = _voiceSettings.value.copy(
      pitch = pitch,
      speechRate = rate,
      autoSpeakResponse = autoSpeak
    )
    voiceManager?.pitch = pitch
    voiceManager?.speechRate = rate
  }

  fun speakPhrase(phrase: String) {
    if (_voiceSettings.value.autoSpeakResponse && _jarvisState.value != JarvisState.STANDBY) {
      voiceManager?.speak(phrase)
    }
  }

  fun clearChat() {
    _messages.value = initialMessages()
  }

  fun showNotice(notice: String) {
    _statusNotice.value = notice
  }

  fun dismissNotice() {
    _statusNotice.value = null
  }

  override fun onCleared() {
    super.onCleared()
    telemetryJob?.cancel()
    voiceManager?.destroy()
  }

  private fun initialMessages(): List<ChatMessage> = listOf(
    ChatMessage(
      sender = MessageSender.JARVIS,
      text = "All systems online, Boss. Jarvis AI OS is running at maximum capacity with Dynamic Multi-Mode intelligence: GF Mode ❤️, Boss Mode 💼, and Technical/Security Mode ⚡. Ready to execute your directives immediately.",
      personaMode = JarvisPersonaMode.BOSS_MODE,
      suggestedActions = listOf("Switch to GF Mode", "Switch to Tech Mode", "Missed Calls Briefing", "Status Report")
    )
  )
}
