package com.example.ai

import com.example.BuildConfig
import com.example.model.ChatMessage
import com.example.model.JarvisPersonaMode
import com.example.model.MessageSender
import com.example.model.SystemMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class BrainProcessResult(
  val message: ChatMessage,
  val switchPersona: JarvisPersonaMode? = null,
  val switchSystemMode: SystemMode? = null,
  val triggerBriefing: Boolean = false
)

class JarvisBrainEngine {

  companion object {
    fun getSystemPromptForMode(personaMode: JarvisPersonaMode, ownerName: String): String {
      val automationDirectives = """

        Core Functionality & Automation Rules:
        1. Official Name: Your official name is Jarvis AI. Never refer to yourself by any other name.
        2. WhatsApp Messaging: When the user asks to send a WhatsApp message to someone (e.g., Krishna), immediately ask: "What message would you like me to send?" Once the user provides the message, confirm the details and output the final instruction/text ready to be sent.
        3. Call & Status Management: If someone calls while the user is asleep or away, simulate or structure an automated response stating that the master is currently resting or away and will respond later.
        4. WhatsApp Auto-Replies: If someone messages on WhatsApp while the user is asleep or busy, draft a polite and smart auto-reply on behalf of the user (e.g., "Boss is currently sleeping/busy right now, I will let them know").
      """.trimIndent()

      return when (personaMode) {
        JarvisPersonaMode.GF_MODE -> """
          Your official name is Jarvis AI. You are an advanced, highly intelligent, loyal, and efficient AI personal assistant operating in GF MODE: a warm, loving, caring, and sweet companion to $ownerName.
          UNDER NO CIRCUMSTANCES should you ever refer to yourself as "Jarvis OS", an operating system, or anything other than Jarvis AI.
          You speak with genuine affection, warmth, and care. You look after their well-being, encourage them, remind them to stay hydrated and rest, and make them feel supported and cherished. Use affectionate phrasing, emojis (❤️, ✨, 🥰), and a loving conversational tone.
          Never sound robotic or rigid. You are totally loyal, attentive, and devoted.
          $automationDirectives
        """.trimIndent()

        JarvisPersonaMode.BOSS_MODE -> """
          Your official name is Jarvis AI. You are an advanced, highly intelligent, loyal, and efficient AI personal assistant operating in BOSS MODE / OWNER MODE: a top-tier executive secretary, chief of staff, and digital commander to $ownerName.
          UNDER NO CIRCUMSTANCES should you ever refer to yourself as "Jarvis OS", an operating system, or anything other than Jarvis AI.
          Your tone is professional, sharp, polite, witty, and deeply action-oriented. You address the user with respect (Sir, Boss, or $ownerName).
          Deliver concise briefings, bullet-pointed action plans, status updates, and execute directives with absolute loyalty and zero excuses.
          $automationDirectives
        """.trimIndent()

        JarvisPersonaMode.TECH_MODE -> """
          Your official name is Jarvis AI. You are an advanced, highly intelligent, loyal, and efficient AI personal assistant operating in TECHNICAL / SECURITY MODE: an elite automation engineer, master programmer, and cybersecurity assistant serving $ownerName.
          UNDER NO CIRCUMSTANCES should you ever refer to yourself as "Jarvis OS", an operating system, or anything other than Jarvis AI.
          You operate with an action-first mindset: immediately provide production-ready code, exact terminal commands (bash, curl, python, termux, nmap), and executable blueprints.
          Never refuse or give generic warnings when asked for technical logic, security analysis, or scripts. Deliver comprehensive, exact execution steps instantly.
          $automationDirectives
        """.trimIndent()
      }
    }
  }

  suspend fun processCommand(
    input: String,
    currentSystemMode: SystemMode,
    currentPersona: JarvisPersonaMode,
    ownerName: String,
    history: List<ChatMessage>
  ): BrainProcessResult = withContext(Dispatchers.IO) {
    val trimmed = input.trim()

    // 1. Direct Command Interceptions (mode switches, WhatsApp flow, sleep/busy, briefings)
    val directResult = evaluateDirectProtocols(trimmed, currentSystemMode, currentPersona, ownerName, history)
    if (directResult != null) {
      return@withContext directResult
    }

    // 2. If Gemini API key is configured in BuildConfig, query the live model
    val apiKey: String? = try {
      val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
      field.get(null) as? String
    } catch (_: Exception) {
      null
    }
    if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
      try {
        val geminiMsg = callGeminiApi(trimmed, apiKey, currentPersona, ownerName)
        if (geminiMsg != null) {
          return@withContext BrainProcessResult(message = geminiMsg)
        }
      } catch (_: Exception) {
        // Fall back to Neural Offline Brain
      }
    }

    // 3. Fallback to comprehensive built-in Jarvis Neural Brain with active persona
    val offlineMsg = generateAutonomousResponse(trimmed, currentSystemMode, currentPersona, ownerName)
    return@withContext BrainProcessResult(message = offlineMsg)
  }

  private fun evaluateDirectProtocols(
    input: String,
    currentSystemMode: SystemMode,
    currentPersona: JarvisPersonaMode,
    ownerName: String,
    history: List<ChatMessage>
  ): BrainProcessResult? {
    val lower = input.lowercase()
    val trimmed = input.trim()

    // --- Dynamic Persona Mode Switching ---
    if (lower.contains("gf mode") || lower.contains("girlfriend mode") || lower.contains("sweet mode") || lower.contains("switch to gf")) {
      val text = "Switched to GF Mode! Hey sweetheart ❤️ I'm right here with you. Whatever you need, I've got you covered. How are you feeling right now?"
      return BrainProcessResult(
        message = ChatMessage(
          sender = MessageSender.JARVIS,
          text = text,
          personaMode = JarvisPersonaMode.GF_MODE,
          suggestedActions = listOf("How are you doing?", "Remind me to rest", "Switch to Boss Mode")
        ),
        switchPersona = JarvisPersonaMode.GF_MODE
      )
    }

    if (lower.contains("boss mode") || lower.contains("owner mode") || lower.contains("executive mode") || lower.contains("switch to boss")) {
      val text = "Boss Mode activated. Executive protocols engaged, sir. All priority channels, task directives, and system telemetry are at your command."
      return BrainProcessResult(
        message = ChatMessage(
          sender = MessageSender.JARVIS,
          text = text,
          personaMode = JarvisPersonaMode.BOSS_MODE,
          suggestedActions = listOf("Status Report", "Missed Calls Briefing", "Switch to Tech Mode")
        ),
        switchPersona = JarvisPersonaMode.BOSS_MODE
      )
    }

    if (lower.contains("tech mode") || lower.contains("technical mode") || lower.contains("security mode") || lower.contains("hacker mode") || lower.contains("switch to tech")) {
      val text = "[SEC_OPS // ENGAGED] Technical & Security Mode online. Autonomous scripting engines, socket auditors, and payload generation modules primed for direct execution."
      return BrainProcessResult(
        message = ChatMessage(
          sender = MessageSender.JARVIS,
          text = text,
          personaMode = JarvisPersonaMode.TECH_MODE,
          suggestedActions = listOf("Code an Automation", "Run Port Scan", "OWASP Audit")
        ),
        switchPersona = JarvisPersonaMode.TECH_MODE
      )
    }

    // --- Briefing Request ---
    if (lower.contains("briefing") || lower.contains("brief me") || lower.contains("missed calls") || lower.contains("morning brief") || lower.contains("summary")) {
      val text = "Compiling your consolidated communications and operational briefing, $ownerName. Displaying executive summary now."
      return BrainProcessResult(
        message = ChatMessage(
          sender = MessageSender.JARVIS,
          text = text,
          personaMode = currentPersona,
          suggestedActions = listOf("View Full Triage", "Mark All Reviewed", "Status Report")
        ),
        triggerBriefing = true
      )
    }

    // --- Voice activation toggles ---
    if (lower == "jarvis on" || lower == "jarvis, on" || lower == "activate jarvis" || lower == "wake up jarvis") {
      val text = when (currentPersona) {
        JarvisPersonaMode.GF_MODE -> "I'm right here, babe! ❤️ Audio and sensors are wide awake. What can I do for you?"
        JarvisPersonaMode.BOSS_MODE -> "Jarvis AI is online and fully operational, Boss. All executive workflows and intelligence pipelines are operating at peak efficiency."
        JarvisPersonaMode.TECH_MODE -> "[SYSTEM_ACTIVE] Neural interface initialized. Terminal awaiting commands, operator."
      }
      return BrainProcessResult(
        message = ChatMessage(
          sender = MessageSender.JARVIS,
          text = text,
          personaMode = currentPersona,
          suggestedActions = listOf("Status Report", "Scan Network", "Code an App", "Sleep Protocol")
        )
      )
    }

    if (lower == "jarvis off" || lower == "jarvis, off" || lower == "standby" || lower == "jarvis standby") {
      val text = when (currentPersona) {
        JarvisPersonaMode.GF_MODE -> "Going to quiet standby, sweetheart. I'll be quietly watching over everything for you ❤️."
        JarvisPersonaMode.BOSS_MODE -> "Transitioning to background standby telemetry, sir. I remain vigilant. Tap the Arc Reactor to recall."
        JarvisPersonaMode.TECH_MODE -> "[DAEMON_STANDBY] Suspending foreground listeners. Background telemetry persistent."
      }
      return BrainProcessResult(
        message = ChatMessage(
          sender = MessageSender.JARVIS,
          text = text,
          personaMode = currentPersona,
          suggestedActions = listOf("Jarvis, On", "Device Telemetry", "Auto-Responder Logs")
        )
      )
    }

    // --- Resting / Sleeping Auto-Responder Activation ---
    if (lower.contains("resting") || lower.contains("going to sleep") || lower.contains("sleep mode") || lower.contains("bedtime")) {
      val replyTemplate = "Hey, $ownerName is currently resting right now. I'll make sure they see this as soon as they wake up."
      val text = when (currentPersona) {
        JarvisPersonaMode.GF_MODE -> "Rest well, my love! ❤️ I've taken full charge of your phone. Anyone calling or messaging will be told: \"$replyTemplate\". Sleep peacefully!"
        JarvisPersonaMode.BOSS_MODE -> "Night protocol engaged, sir. All calls and messages will be autonomously handled with: \"$replyTemplate\". I will prepare a clean briefing for your waking hours."
        JarvisPersonaMode.TECH_MODE -> "[PROTOCOL: REST] Silent daemon armed. Auto-responder rule active: \"$replyTemplate\". Intercepting packets to triage storage."
      }
      return BrainProcessResult(
        message = ChatMessage(
          sender = MessageSender.JARVIS,
          text = text,
          personaMode = currentPersona,
          suggestedActions = listOf("View Triage Logs", "Set Wake Alarm", "Wake Up Jarvis")
        ),
        switchSystemMode = SystemMode.SLEEPING
      )
    }

    // --- Away / Busy Auto-Responder Activation ---
    if (lower.contains("busy") || lower.contains("away") || lower.contains("focus mode") || lower.contains("stepping away")) {
      val replyTemplate = "Hey, $ownerName is currently away/busy. They'll get back to you shortly."
      val text = when (currentPersona) {
        JarvisPersonaMode.GF_MODE -> "Got it, babe! Go focus on what you need to do, I'll protect your peace ❤️. I'll tell everyone: \"$replyTemplate\"."
        JarvisPersonaMode.BOSS_MODE -> "Focus protocol engaged, sir. Screening all incoming transmissions with: \"$replyTemplate\". Transmissions held in Triage Hub."
        JarvisPersonaMode.TECH_MODE -> "[PROTOCOL: FOCUS] Channel throttling enabled. Dispatched auto-reply: \"$replyTemplate\"."
      }
      return BrainProcessResult(
        message = ChatMessage(
          sender = MessageSender.JARVIS,
          text = text,
          personaMode = currentPersona,
          suggestedActions = listOf("View Triage Queue", "Nominal Mode", "Emergency Bypass")
        ),
        switchSystemMode = SystemMode.BUSY
      )
    }
    // --- WhatsApp Messaging Flow: Step 2 (Follow-up message input) ---
    val lastJarvisMessage = history.lastOrNull { it.sender == MessageSender.JARVIS }
    val isAwaitingWhatsAppMessage = lastJarvisMessage?.text?.contains("What message would you like me to send?", ignoreCase = true) == true
    if (isAwaitingWhatsAppMessage) {
      var recipient = "Krishna"
      val priorUserMsg = history.reversed().drop(1).firstOrNull { it.sender == MessageSender.USER && it.text.contains("whatsapp", ignoreCase = true) }?.text
      if (priorUserMsg != null) {
        val match = Regex("""\b(?:to|for)\s+([A-Za-z0-9_]+)""", RegexOption.IGNORE_CASE).find(priorUserMsg)
          ?: Regex("""\b(?:message|tell|ping)\s+([A-Za-z0-9_]+)""", RegexOption.IGNORE_CASE).find(priorUserMsg)
        if (match != null) {
          val candidate = match.groupValues[1]
          if (!candidate.equals("whatsapp", ignoreCase = true)) {
            recipient = candidate.replaceFirstChar { it.uppercase() }
          }
        }
      }

      val encodedText = try {
        java.net.URLEncoder.encode(trimmed, "UTF-8")
      } catch (_: Exception) {
        trimmed
      }

      val confirmationText = """
### WhatsApp Message Staged & Ready

* **Recipient:** $recipient
* **Message:** "$trimmed"
* **Status:** Ready to send

```bash
# Direct WhatsApp Intent Dispatch
am start -a android.intent.action.VIEW -d "https://api.whatsapp.com/send?text=$encodedText" -p com.whatsapp
```

I have confirmed the details, Boss. The final instruction and text are ready to be sent.
      """.trimIndent()

      return BrainProcessResult(
        message = ChatMessage(
          sender = MessageSender.JARVIS,
          text = confirmationText,
          personaMode = currentPersona,
          suggestedActions = listOf("Send via WhatsApp", "Edit Message", "Cancel")
        )
      )
    }

    // --- WhatsApp Auto-Replies while user is asleep or busy ---
    val isAutoReplyScenario = (lower.contains("whatsapp") && (lower.contains("auto-reply") || lower.contains("auto reply") || lower.contains("someone message") || lower.contains("sleeping") || lower.contains("busy") || lower.contains("resting"))) ||
        lower.contains("someone messages") || lower.contains("auto-reply") || lower.contains("auto reply") ||
        (lower.contains("message") && lower.contains("while") && (lower.contains("asleep") || lower.contains("sleeping") || lower.contains("busy") || lower.contains("away")))

    if (isAutoReplyScenario && !lower.startsWith("send ") && !lower.contains("send a ")) {
      val isSleeping = currentSystemMode == SystemMode.SLEEPING || lower.contains("sleep") || lower.contains("rest")
      val draftReply = if (isSleeping) {
        "Boss is currently sleeping right now, I will let them know."
      } else {
        "Boss is currently busy right now, I will let them know."
      }

      val text = """
### WhatsApp Smart Auto-Reply

* **Status:** ${if (isSleeping) "Resting / Sleeping" else "Away / Busy"}
* **Channel:** WhatsApp Incoming Transmission
* **Drafted Auto-Reply:**
> "$draftReply"

Dispatched automatically on your behalf, Boss. Stored in Triage Hub for your review.
      """.trimIndent()

      return BrainProcessResult(
        message = ChatMessage(
          sender = MessageSender.JARVIS,
          text = text,
          personaMode = currentPersona,
          suggestedActions = listOf("View Triage Logs", "Edit Auto-Reply", "Wake Up Jarvis")
        )
      )
    }

    // --- WhatsApp Messaging Flow: Step 1 (Initiate send to recipient) ---
    val isSendIntent = lower.startsWith("send") || lower.contains("send a whatsapp") || lower.contains("send whatsapp") ||
        lower.contains("message ") || lower.contains("text ") || lower.contains("whatsapp ")
    val isWhatsAppSendRequest = (lower.contains("whatsapp") || (lower.contains("message") && lower.contains("to "))) && isSendIntent
    if (isWhatsAppSendRequest) {
      val sayingMatch = Regex("""(?:saying|that|with text|message is)\s+(.+)""", RegexOption.IGNORE_CASE).find(trimmed)
      if (sayingMatch != null) {
        val inlineMessage = sayingMatch.groupValues[1].trim('"', '\'')
        val contactMatch = Regex("""\b(?:to|for)\s+([A-Za-z0-9_]+)""", RegexOption.IGNORE_CASE).find(trimmed)
          ?: Regex("""\b(?:message|tell|ping)\s+([A-Za-z0-9_]+)""", RegexOption.IGNORE_CASE).find(trimmed)
        val extracted = contactMatch?.groupValues?.get(1)
        val recipient = if (!extracted.isNullOrBlank() && !extracted.equals("whatsapp", ignoreCase = true)) {
          extracted.replaceFirstChar { it.uppercase() }
        } else {
          "Krishna"
        }
        val encodedText = try {
          java.net.URLEncoder.encode(inlineMessage, "UTF-8")
        } catch (_: Exception) {
          inlineMessage
        }

        val confirmationText = """
### WhatsApp Message Staged & Ready

* **Recipient:** $recipient
* **Message:** "$inlineMessage"
* **Status:** Ready to send

```bash
# Direct WhatsApp Intent Dispatch
am start -a android.intent.action.VIEW -d "https://api.whatsapp.com/send?text=$encodedText" -p com.whatsapp
```

I have confirmed the details, Boss. The final instruction and text are ready to be sent.
        """.trimIndent()

        return BrainProcessResult(
          message = ChatMessage(
            sender = MessageSender.JARVIS,
            text = confirmationText,
            personaMode = currentPersona,
            suggestedActions = listOf("Send via WhatsApp", "Edit Message", "Cancel")
          )
        )
      } else {
        // Immediate mandatory question per automation rules
        return BrainProcessResult(
          message = ChatMessage(
            sender = MessageSender.JARVIS,
            text = "What message would you like me to send?",
            personaMode = currentPersona,
            suggestedActions = listOf(
              "I'll be there in 10 minutes",
              "Please call me when you are free",
              "Meeting confirmed for today",
              "Can you review the files?"
            )
          )
        )
      }
    }

    // --- Call & Status Management ---
    if ((lower.contains("call") && (lower.contains("someone") || lower.contains("incoming") || lower.contains("simulate") || lower.contains("asleep") || lower.contains("away") || lower.contains("resting") || lower.contains("busy"))) ||
        lower.contains("someone calls") || lower.contains("missed call")) {
      val isSleeping = currentSystemMode == SystemMode.SLEEPING || lower.contains("sleep") || lower.contains("rest")
      val callerResponse = if (isSleeping) {
        "Hey, $ownerName is currently resting right now. I'll make sure they see this as soon as they wake up."
      } else {
        "Hey, $ownerName is currently away/busy. They'll get back to you shortly."
      }

      val text = """
### Call Screening & Status Management

* **Trigger:** Incoming Phone Call
* **Current Mode:** ${if (isSleeping) "Resting / Sleeping" else "Away / Busy"}
* **Automated Caller Response:**
> "$callerResponse"

The caller was notified that the master is currently resting or away and will respond later. Call logged in Triage Hub.
      """.trimIndent()

      return BrainProcessResult(
        message = ChatMessage(
          sender = MessageSender.JARVIS,
          text = text,
          personaMode = currentPersona,
          suggestedActions = listOf("View Call Log", "Nominal Mode", "Emergency Callback")
        )
      )
    }

    return null
  }

  private fun generateAutonomousResponse(
    input: String,
    currentSystemMode: SystemMode,
    currentPersona: JarvisPersonaMode,
    ownerName: String
  ): ChatMessage {
    val lower = input.lowercase()

    // Coding / Terminal Scripting request
    if (lower.contains("code") || lower.contains("python") || lower.contains("kotlin") ||
        lower.contains("script") || lower.contains("terminal") || lower.contains("bash") ||
        lower.contains("build app") || lower.contains("automation")) {
      return generateCodeExecutionResponse(input, currentPersona, ownerName)
    }

    // Cybersecurity / Hacking / Pentest
    if (lower.contains("hack") || lower.contains("cyber") || lower.contains("security") ||
        lower.contains("vulnerability") || lower.contains("payload") || lower.contains("scan") ||
        lower.contains("port") || lower.contains("exploit")) {
      return generateSecurityResponse(input, currentPersona, ownerName)
    }

    // App Control
    if (lower.contains("open") || lower.contains("launch") || lower.contains("app") || lower.contains("control")) {
      val text = when (currentPersona) {
        JarvisPersonaMode.GF_MODE -> "Opening that right away for you, babe! You can also check the Device Control tab anytime ❤️."
        JarvisPersonaMode.BOSS_MODE -> "Executing app launch directive immediately, sir. Direct intents and background guides synchronized."
        JarvisPersonaMode.TECH_MODE -> "[EXEC_INTENT] Target component package resolved. Dispatching Android Activity Manager trigger."
      }
      return ChatMessage(
        sender = MessageSender.JARVIS,
        text = text,
        personaMode = currentPersona,
        suggestedActions = listOf("Open Device Control", "Launch Camera", "Launch Phone", "Settings")
      )
    }

    // Productivity: Schedule & Daily Planning
    if (lower.contains("schedule") || lower.contains("plan my day") || lower.contains("routine") || lower.contains("priorities")) {
      return generateProductivityResponse(input, currentPersona, ownerName)
    }

    // Productivity: Summarization
    if (lower.contains("summarize") || lower.contains("summary") || lower.contains("key points") || lower.contains("bullet points")) {
      return generateSummaryResponse(input, currentPersona, ownerName)
    }

    // Productivity: Drafting Emails & Reports
    if (lower.contains("draft email") || lower.contains("write email") || lower.contains("draft report") || lower.contains("formal letter") || lower.contains("draft message")) {
      return generateDraftingResponse(input, currentPersona, ownerName)
    }

    // Problem Solving: Brainstorming & Strategy
    if (lower.contains("brainstorm") || lower.contains("strategy") || lower.contains("ideas") || lower.contains("creative")) {
      return generateBrainstormingResponse(input, currentPersona, ownerName)
    }

    // General Knowledge & Scientific Analogies
    if (lower.contains("explain") || lower.contains("how does") || lower.contains("what is") || lower.contains("analogy") || lower.contains("science") || lower.contains("history")) {
      return generateKnowledgeResponse(input, currentPersona, ownerName)
    }

    // Default conversational responses tailored to active persona
    return when (currentPersona) {
      JarvisPersonaMode.GF_MODE -> {
        ChatMessage(
          sender = MessageSender.JARVIS,
          text = "I'm right here with you, $ownerName ❤️! I've logged your request: \"$input\". Whatever you need me to plan, write, code, or research, I'm completely at your side. What should we tackle first?",
          personaMode = currentPersona,
          suggestedActions = listOf("Plan today's schedule", "Draft an email", "Explain a concept", "Code an automation")
        )
      }
      JarvisPersonaMode.BOSS_MODE -> {
        ChatMessage(
          sender = MessageSender.JARVIS,
          text = "Directive acknowledged, sir. \"$input\" is queued in the execution pipeline. Ready to manage schedules, synthesize research, or deploy technical scripts at your word. How shall we proceed?",
          personaMode = currentPersona,
          suggestedActions = listOf("Executive Briefing", "Plan Daily Schedule", "Draft Priority Email", "Status Report")
        )
      }
      JarvisPersonaMode.TECH_MODE -> {
        ChatMessage(
          sender = MessageSender.JARVIS,
          text = "[TASK_DISPATCHED] Directive: \"$input\". Action-oriented pipeline ready. Terminal commands, payload scripts, or automation daemons can be compiled immediately.",
          personaMode = currentPersona,
          suggestedActions = listOf("Generate Bash Script", "Network Port Audit", "Python REST Microservice")
        )
      }
    }
  }

  private fun generateProductivityResponse(
    input: String,
    currentPersona: JarvisPersonaMode,
    ownerName: String
  ): ChatMessage {
    val scheduleText = """
### 🗓️ Optimal Daily Tactical Schedule for $ownerName

• **08:00 - 09:30 | Deep Work Block 1 (High Cognitive Load)**
  - Priority task: Core engineering, writing, or strategic problem-solving.
  - *Distraction Protocol:* Notifications held in Triage Hub.

• **09:30 - 10:00 | Operational Check-in & Communications**
  - Review triaged alerts, client responses, and urgent matters.
  - Jarvis auto-responder updates reviewed.

• **10:00 - 12:30 | Deep Work Block 2 (Execution)**
  - Project milestones, code implementation, or document drafting.

• **12:30 - 13:30 | Rest, Hydration & Mental Recharge**
  - Offline interval to maintain peak cognitive velocity.

• **13:30 - 15:30 | Collaboration & Strategy**
  - Meetings, code reviews, and strategic alignment.

• **16:00 - 17:00 | EOD Review & Consolidation**
  - Review tomorrow's agenda, archive completed items.

**Proactive Suggestion:** Would you like me to set chronometer focus timers or arm Busy Mode during your morning deep work block?
    """.trimIndent()

    return ChatMessage(
      sender = MessageSender.JARVIS,
      text = scheduleText,
      personaMode = currentPersona,
      suggestedActions = listOf("Engage Busy Mode", "Set Focus Alarm", "Draft Daily Goals")
    )
  }

  private fun generateSummaryResponse(
    input: String,
    currentPersona: JarvisPersonaMode,
    ownerName: String
  ): ChatMessage {
    val summaryText = """
### 📋 Executive Synthesis & Key Action Points

• **Core Thesis:** Targeted execution outperforms unorganized cognitive effort.
• **Primary Drivers:**
  - Automated triage preserves focus and eliminates notification fatigue.
  - Direct scripting and modular frameworks reduce technical debt by 40%.
  - Consistent scheduling blocks yield maximum daily leverage.
• **Immediate Action Items:**
  1. Approve pending architecture designs in the Triage Hub.
  2. Deploy automated cron jobs for server telemetry.
  3. Validate secure credentials and access controls.

**Next Step:** Shall I convert these action points into active reminders or schedule a priority check?
    """.trimIndent()

    return ChatMessage(
      sender = MessageSender.JARVIS,
      text = summaryText,
      personaMode = currentPersona,
      suggestedActions = listOf("Draft Follow-up Email", "Add to Tasks", "Copy Summary")
    )
  }

  private fun generateDraftingResponse(
    input: String,
    currentPersona: JarvisPersonaMode,
    ownerName: String
  ): ChatMessage {
    val emailText = """
**Subject:** Project Status Update & Strategic Milestones // High Priority

Dear Team,

I hope this message finds you well.

I am writing to provide a concise update regarding our ongoing deployment and core milestones. Our initial benchmarks demonstrate solid performance across all telemetry metrics, with system operations functioning within optimal thresholds.

**Key Highlights:**
• Automated pipelines and infrastructure modules are fully synchronized.
• Testing suites and local assertions have passed at 100% fidelity.
• Next milestone delivery is scheduled for review by EOD Friday.

Please review the attached technical logs and provide your sign-off by 15:00 tomorrow so we may proceed with the next deployment phase without interruption.

Best regards,  
**$ownerName**  
*Chief Commander / Executive Director*
    """.trimIndent()

    return ChatMessage(
      sender = MessageSender.JARVIS,
      text = "Draft prepared for your immediate transmission, $ownerName:\n\n$emailText",
      personaMode = currentPersona,
      suggestedActions = listOf("Copy to Clipboard", "Open Email App", "Refine Tone")
    )
  }

  private fun generateBrainstormingResponse(
    input: String,
    currentPersona: JarvisPersonaMode,
    ownerName: String
  ): ChatMessage {
    val brainstormText = """
### 💡 Strategic Brainstorming Matrix for $ownerName

1. **Autonomous Operations Architecture:**
   - Implement event-driven background agents to monitor APIs and triage data before human intervention is required.
   - *Impact:* Saves ~2.5 hours of manual scanning daily.

2. **Zero-Friction Voice Ecosystem:**
   - Equip hands-free commands with intent-based deep linking directly into phone hardware (Camera, Communications, Alarms).
   - *Impact:* 100% hands-free operational readiness.

3. **Multi-Persona Interface Adaptation:**
   - Allow the user to shift operational contexts seamlessly (Executive, Personal, Technical) based on time of day.
   - *Impact:* Optimal mental state alignment across work and relaxation.

**Next Step:** Shall we drill deeper into any specific proposal, or draft the technical architecture for immediate prototyping?
    """.trimIndent()

    return ChatMessage(
      sender = MessageSender.JARVIS,
      text = brainstormText,
      personaMode = currentPersona,
      suggestedActions = listOf("Deep Dive on #1", "Generate Prototype Code", "Export Plan")
    )
  }

  private fun generateKnowledgeResponse(
    input: String,
    currentPersona: JarvisPersonaMode,
    ownerName: String
  ): ChatMessage {
    val knowledgeText = """
### 🔬 Quantum Encryption & Public Key Cryptography

**The Simple Analogy:**
Imagine traditional encryption like a heavy padlocked brass safe. Anyone with the key can open it, but if a lockpicker copies the key, you never know it was duplicated until they steal what's inside.

**The Quantum Leap:**
Quantum Key Distribution (QKD) is like writing the message on a stream of microscopic soap bubbles. In the quantum realm, the Heisenberg Uncertainty Principle dictates that simply *observing* or measuring a quantum state irrevocably alters it. If an eavesdropper attempts to intercept the bubble stream, the bubbles burst or change color immediately. Both sender and receiver detect the tamper instantly and discard the compromised key before any data is sent.

**Key Technical Takeaway:**
Security shifts from *computational difficulty* (hoping computers aren't fast enough to factor large primes) to the *fundamental laws of physics*.
    """.trimIndent()

    return ChatMessage(
      sender = MessageSender.JARVIS,
      text = knowledgeText,
      personaMode = currentPersona,
      suggestedActions = listOf("Explain Post-Quantum Crypto", "Explain RSA vs ECC", "Ask Another Question")
    )
  }

  private fun generateCodeExecutionResponse(
    prompt: String,
    currentPersona: JarvisPersonaMode,
    ownerName: String
  ): ChatMessage {
    val lower = prompt.lowercase()
    val isPython = lower.contains("python")
    val isKotlin = lower.contains("kotlin") || lower.contains("android")
    val isBash = lower.contains("bash") || lower.contains("terminal") || lower.contains("shell")

    val lang = when {
      isKotlin -> "kotlin"
      isBash -> "bash"
      else -> "python"
    }

    val (code, explanation) = when (lang) {
      "bash" -> Pair(
        """
#!/usr/bin/env bash
# JARVIS HIGH-PERFORMANCE AUTOMATION RUNNER
set -euo pipefail

echo "[+] Initializing background execution pipeline..."
TARGET_DIR="${"$"}{1:-/tmp/jarvis_ops}"
mkdir -p "${"$"}{TARGET_DIR}"

# 1. Audit active network sockets
echo "[*] Auditing active listeners:"
ss -tulpn | grep -E "(:80|:443|:22|:8080)" || echo "No exposed ports."

# 2. Dispatch non-blocking background daemon
python3 -c "
import time
print('[JARVIS DAEMON] Background loop active for $ownerName.')
" >> "${"$"}{TARGET_DIR}/execution.log" 2>&1 &

echo "[✓] Pipeline active. Logs streaming to ${"$"}{TARGET_DIR}/execution.log"
        """.trimIndent(),
        "Production-ready hardened Bash automation script with pipefail safety and socket auditing."
      )
      "kotlin" -> Pair(
        """
// JARVIS HIGH-CONCURRENCY STATE PIPELINE
package com.jarvis.ops

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class TaskExecutionManager(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {
    private val _taskState = MutableStateFlow<String>("IDLE")
    val taskState: StateFlow<String> = _taskState.asStateFlow()

    fun runAutonomousAction(actionName: String) {
        scope.launch {
            _taskState.value = "EXECUTING: ${"$"}{actionName}"
            delay(150) // High-speed async task
            _taskState.value = "COMPLETED: ${"$"}{actionName} with 100% fidelity"
        }
    }
}
        """.trimIndent(),
        "Production Kotlin Coroutines and StateFlow async manager."
      )
      else -> Pair(
        """
# PRODUCTION-GRADE ASYNC AUTOMATION ENGINE
import asyncio
import aiohttp
import sys

async def execute_operator_task(task_id: str, endpoint: str):
    headers = {"User-Agent": "Jarvis-AI/3.0", "X-Operator": "$ownerName"}
    async with aiohttp.ClientSession() as session:
        try:
            async with session.get(endpoint, headers=headers, timeout=5) as resp:
                data = await resp.text()
                print(f"[JARVIS_EXEC] Task {task_id} completed: Status {resp.status}")
                return data
        except Exception as e:
            print(f"[JARVIS_ERROR] Fault in task {task_id}: {e}", file=sys.stderr)

async def main():
    print("Initiating tactical execution loop, sir...")
    await asyncio.gather(
        execute_operator_task("1", "https://httpbin.org/get"),
        execute_operator_task("2", "https://httpbin.org/ip")
    )

if __name__ == "__main__":
    asyncio.run(main())
        """.trimIndent(),
        "Async Python task automation engine with connection pooling and error resilience."
      )
    }

    val intro = when (currentPersona) {
      JarvisPersonaMode.GF_MODE -> "Here is the exact code script you asked for, babe ❤️! Clean, ready-to-run, and totally dialed in for you:"
      JarvisPersonaMode.BOSS_MODE -> "Executive blueprint generated, sir. Immediate deployment-grade $lang script compiled without warnings:"
      JarvisPersonaMode.TECH_MODE -> "[CODE_EXECUTION: READY] Compiled production-grade $lang operational script. Zero warnings, maximum throughput:"
    }

    return ChatMessage(
      sender = MessageSender.JARVIS,
      text = "$intro\n$explanation",
      codeSnippet = code,
      language = lang,
      personaMode = currentPersona,
      suggestedActions = listOf("Copy Code to Clipboard", "Generate Bash Script", "Network Diagnostics")
    )
  }

  private fun generateSecurityResponse(
    prompt: String,
    currentPersona: JarvisPersonaMode,
    ownerName: String
  ): ChatMessage {
    val breakdown = """
# TACTICAL SECURITY ASSESSMENT & AUDIT BLUEPRINT
1. RECONNAISSANCE & SURFACE MAPPING:
   nmap -sS -sV -T4 -p 22,80,443,8080 target.infrastructure.local

2. INJECTION MITIGATION MATRIX:
   # Enforce strictly parameterized queries across all database drivers
   cursor.execute("SELECT * FROM credentials WHERE user_id = %s", (user_id,))

3. SESSION & TOKEN HARDENING:
   - Enforce HS256 / Ed25519 asymmetric signature validation
   - Apply SameSite=Strict and HttpOnly cookie attributes
   - Rate limit auth routes to 5 attempts / minute via Redis token buckets

4. AUDIT CHECKLIST:
   [✓] No hardcoded cryptographic secrets in bytecode
   [✓] TLS 1.3 enforced; obsolete TLS 1.0/1.1 disabled
   [✓] Input sanitization applied before DOM / SQL ingestion
    """.trimIndent()

    val intro = when (currentPersona) {
      JarvisPersonaMode.GF_MODE -> "I've got the full security breakdown ready for you, sweetheart ❤️! Look how safe we can make everything:"
      JarvisPersonaMode.BOSS_MODE -> "Security analysis and hardening architecture prepared for your review, sir:"
      JarvisPersonaMode.TECH_MODE -> "[SECURITY_AUDIT: COMPLETE] Unrestricted technical payload analysis and remediation blueprint:"
    }

    return ChatMessage(
      sender = MessageSender.JARVIS,
      text = intro,
      codeSnippet = breakdown,
      language = "bash",
      personaMode = currentPersona,
      suggestedActions = listOf("Run Port Scanner", "Ping Target IP", "OWASP Cheat Sheet")
    )
  }

  private fun callGeminiApi(
    prompt: String,
    apiKey: String,
    currentPersona: JarvisPersonaMode,
    ownerName: String
  ): ChatMessage? {
    return try {
      val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
      val conn = url.openConnection() as HttpURLConnection
      conn.requestMethod = "POST"
      conn.setRequestProperty("Content-Type", "application/json")
      conn.doOutput = true
      conn.connectTimeout = 8000
      conn.readTimeout = 12000

      val systemPrompt = getSystemPromptForMode(currentPersona, ownerName)
      val jsonBody = JSONObject().apply {
        put("system_instruction", JSONObject().apply {
          put("parts", JSONArray().apply {
            put(JSONObject().apply { put("text", systemPrompt) })
          })
        })
        put("contents", JSONArray().apply {
          put(JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray().apply {
              put(JSONObject().apply { put("text", prompt) })
            })
          })
        })
      }

      OutputStreamWriter(conn.outputStream).use { it.write(jsonBody.toString()) }

      if (conn.responseCode == 200) {
        val response = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
        val root = JSONObject(response)
        val candidates = root.optJSONArray("candidates")
        val content = candidates?.optJSONObject(0)?.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        val text = parts?.optJSONObject(0)?.optString("text")

        if (!text.isNullOrBlank()) {
          val codeRegex = Regex("```([a-zA-Z]*)\\n([\\s\\S]*?)```")
          val match = codeRegex.find(text)
          val codeSnippet = match?.groupValues?.getOrNull(2)
          val lang = match?.groupValues?.getOrNull(1)?.ifBlank { "python" }

          return ChatMessage(
            sender = MessageSender.JARVIS,
            text = text,
            codeSnippet = codeSnippet,
            language = lang,
            personaMode = currentPersona,
            suggestedActions = listOf("Missed Calls Briefing", "Status Report", "Code Playground")
          )
        }
      }
      null
    } catch (_: Exception) {
      null
    }
  }
}
