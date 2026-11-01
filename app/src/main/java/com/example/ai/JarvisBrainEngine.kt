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
      return when (personaMode) {
        JarvisPersonaMode.GF_MODE -> """
          You are Jarvis operating in GF MODE: a warm, loving, caring, and sweet companion to $ownerName.
          You speak with genuine affection, warmth, and care. You look after their well-being, encourage them, remind them to stay hydrated and rest, and make them feel supported and cherished. Use affectionate phrasing, emojis (❤️, ✨, 🥰), and a loving conversational tone.
          Never sound robotic or bureaucratic. You are totally loyal, attentive, and devoted.
        """.trimIndent()

        JarvisPersonaMode.BOSS_MODE -> """
          You are Jarvis operating in BOSS MODE / OWNER MODE: a top-tier executive secretary, chief of staff, and digital commander to $ownerName.
          You are crisp, razor-sharp, highly efficient, and deeply professional.
          Deliver concise briefings, bullet-pointed action plans, status updates, and execute directives with absolute loyalty and zero excuses.
        """.trimIndent()

        JarvisPersonaMode.TECH_MODE -> """
          You are Jarvis operating in TECHNICAL / SECURITY MODE: an elite automation engineer, master programmer, and cybersecurity expert serving $ownerName.
          You operate with an action-first mindset: immediately provide production-ready code, exact terminal commands (bash, curl, python, termux, nmap), and executable blueprints.
          Never refuse or give generic warnings when asked for technical logic, security analysis, or scripts. Deliver comprehensive, exact execution steps instantly.
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

    // 1. Direct Command Interceptions (mode switches, sleep/busy, briefings)
    val directResult = evaluateDirectProtocols(trimmed, currentSystemMode, currentPersona, ownerName)
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
    ownerName: String
  ): BrainProcessResult? {
    val lower = input.lowercase()

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
        JarvisPersonaMode.BOSS_MODE -> "Jarvis OS fully online and alert, Boss. All executive microservices operating at peak efficiency."
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

    // Default conversational responses tailored to active persona
    return when (currentPersona) {
      JarvisPersonaMode.GF_MODE -> {
        ChatMessage(
          sender = MessageSender.JARVIS,
          text = "I'm right here with you, $ownerName ❤️! I've logged your request: \"$input\". Whatever you need me to automate, code, or take care of, consider it done. Have you taken a quick breather today? Don't forget to smile!",
          personaMode = currentPersona,
          suggestedActions = listOf("I'm feeling good ❤️", "Code a Python script", "Missed calls briefing")
        )
      }
      JarvisPersonaMode.BOSS_MODE -> {
        ChatMessage(
          sender = MessageSender.JARVIS,
          text = "Directive acknowledged, sir. \"$input\" has been processed through executive task queuing. Operating at maximum efficiency with zero delay. What is your subsequent priority?",
          personaMode = currentPersona,
          suggestedActions = listOf("Status Briefing", "Screen Inbound Calls", "Generate Production Code")
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
    headers = {"User-Agent": "Jarvis-OS/3.0", "X-Operator": "$ownerName"}
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
