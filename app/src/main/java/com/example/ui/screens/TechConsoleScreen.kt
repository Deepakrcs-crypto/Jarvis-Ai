package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DiagnosticReport
import com.example.tech.CodeTemplate
import com.example.tech.SecurityTopic
import com.example.ui.JarvisViewModel
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.LaserCrimson
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.ObsidianBase
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.StarkAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TechConsoleScreen(
  viewModel: JarvisViewModel,
  diagnosticReport: DiagnosticReport?,
  isDiagnosing: Boolean,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var subTabIndex by remember { mutableStateOf(0) }
  val tabs = listOf("Code Engine", "Network Lab", "Cybersecurity Vault")

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(ObsidianBase)
  ) {
    // Top Bar
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(ObsidianCard)
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      Text(
        text = "TACTICAL TECH CONSOLE",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = ArcCyan,
        letterSpacing = 1.2.sp
      )
      Text(
        text = "Master programmer runtime, port diagnostics & penetration models",
        fontSize = 11.5.sp,
        color = TextSecondary
      )
    }

    // Sub Navigation Tabs
    ScrollableTabRow(
      selectedTabIndex = subTabIndex,
      containerColor = ObsidianCard,
      contentColor = ArcCyan,
      edgePadding = 16.dp,
      indicator = { tabPositions ->
        Box(
          Modifier
            .fillMaxWidth()
            .height(2.5.dp)
            .background(ArcCyan)
        )
      }
    ) {
      tabs.forEachIndexed { index, title ->
        Tab(
          selected = subTabIndex == index,
          onClick = { subTabIndex = index },
          text = {
            Text(
              text = title,
              color = if (subTabIndex == index) ArcCyan else TextSecondary,
              fontSize = 12.sp,
              fontWeight = if (subTabIndex == index) FontWeight.Bold else FontWeight.Normal
            )
          }
        )
      }
    }

    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
      when (subTabIndex) {
        0 -> CodeGeneratorTab(viewModel = viewModel, context = context)
        1 -> NetworkLabTab(
          viewModel = viewModel,
          report = diagnosticReport,
          isDiagnosing = isDiagnosing
        )
        2 -> CybersecurityVaultTab(
          topics = viewModel.getSecurityTopics(),
          context = context
        )
      }
    }
  }
}

@Composable
private fun CodeGeneratorTab(viewModel: JarvisViewModel, context: Context) {
  val templates = remember { viewModel.getCodeTemplates() }
  var customPrompt by remember { mutableStateOf("") }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, ArcCyanGlow),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "INSTANT PRODUCTION CODE GENERATOR",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = StarkAmber
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Generate complete, production-ready code with architecture & tests",
            fontSize = 11.sp,
            color = TextSecondary
          )
          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = customPrompt,
            onValueChange = { customPrompt = it },
            placeholder = { Text("e.g. Build an encrypted SQLite vault in Python", fontSize = 11.sp, color = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              focusedBorderColor = ArcCyan,
              unfocusedBorderColor = ObsidianCardBorder
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().testTag("custom_code_prompt_field")
          )

          Spacer(modifier = Modifier.height(10.dp))

          Button(
            onClick = {
              if (customPrompt.isNotBlank()) {
                viewModel.sendCommand("Generate complete production-ready code: $customPrompt")
                customPrompt = ""
                viewModel.setTab(0) // Return to HUD to view generation
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = ArcCyan),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().testTag("generate_code_btn")
          ) {
            Icon(Icons.Default.Code, contentDescription = null, tint = Color(0xFF030712), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Generate with Jarvis", color = Color(0xFF030712), fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }
      }
    }

    item {
      Text(
        text = "PRODUCTION ARCHITECTURE BLUEPRINTS",
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = ArcCyan,
        letterSpacing = 1.sp
      )
    }

    items(templates) { template ->
      CodeTemplateCard(template = template, context = context)
    }
  }
}

@Composable
private fun CodeTemplateCard(template: CodeTemplate, context: Context) {
  var isExpanded by remember { mutableStateOf(false) }

  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = ObsidianCard),
    border = androidx.compose.foundation.BorderStroke(0.8.dp, ObsidianCardBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = ArcCyan.copy(alpha = 0.2f),
              border = androidx.compose.foundation.BorderStroke(0.5.dp, ArcCyan)
            ) {
              Text(
                text = template.language.uppercase(),
                color = ArcCyan,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = template.title,
              color = TextPrimary,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
          }
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = template.description,
            color = TextSecondary,
            fontSize = 11.sp
          )
        }

        IconButton(
          onClick = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Jarvis Code", template.code))
          },
          modifier = Modifier.size(32.dp)
        ) {
          Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextSecondary, modifier = Modifier.size(16.dp))
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF030712),
        border = androidx.compose.foundation.BorderStroke(0.6.dp, ObsidianCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(10.dp)) {
          Text(
            text = if (isExpanded) template.code else template.code.lines().take(6).joinToString("\n") + "\n...",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.5.sp,
            color = Color(0xFFE2E8F0),
            lineHeight = 15.sp
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = if (isExpanded) "Show Less" else "Expand Full Source",
            color = ArcCyan,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { isExpanded = !isExpanded }
          )
        }
      }
    }
  }
}

@Composable
private fun NetworkLabTab(
  viewModel: JarvisViewModel,
  report: DiagnosticReport?,
  isDiagnosing: Boolean
) {
  var targetInput by remember { mutableStateOf("1.1.1.1") }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "NETWORK PROTOCOL & PORT DIAGNOSTICS",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = ArcCyan,
            letterSpacing = 1.sp
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Probe active ICMP latency and scan socket endpoints (80, 443, 22, 8080, 53)",
            fontSize = 11.sp,
            color = TextSecondary
          )
          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = targetInput,
            onValueChange = { targetInput = it },
            placeholder = { Text("IP or Hostname (e.g. 8.8.8.8 or google.com)", fontSize = 11.sp, color = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              focusedBorderColor = ArcCyan,
              unfocusedBorderColor = ObsidianCardBorder
            ),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("network_host_input")
          )

          Spacer(modifier = Modifier.height(10.dp))

          Button(
            onClick = { viewModel.runNetworkDiagnostic(targetInput) },
            enabled = !isDiagnosing && targetInput.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = ArcCyan),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().testTag("run_diagnostics_btn")
          ) {
            if (isDiagnosing) {
              CircularProgressIndicator(color = Color(0xFF030712), modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Scanning Socket Matrix...", color = Color(0xFF030712), fontSize = 12.sp)
            } else {
              Icon(Icons.Default.Lan, contentDescription = null, tint = Color(0xFF030712), modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Execute Ping & Port Audit", color = Color(0xFF030712), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }
      }
    }

    if (report != null) {
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = ObsidianCard),
          border = androidx.compose.foundation.BorderStroke(1.dp, ArcCyanGlow),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "DIAGNOSTIC REPORT // ${report.targetHost}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ArcCyan
              )
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (report.pingLatencyMs != null) MatrixGreen.copy(alpha = 0.2f) else LaserCrimson.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(
                  0.6.dp,
                  if (report.pingLatencyMs != null) MatrixGreen else LaserCrimson
                )
              ) {
                Text(
                  text = report.status,
                  color = if (report.pingLatencyMs != null) MatrixGreen else LaserCrimson,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
              Column {
                Text("RTT Latency", fontSize = 10.sp, color = TextMuted)
                Text(
                  text = if (report.pingLatencyMs != null) "${report.pingLatencyMs} ms" else "Timed Out",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary
                )
              }
              Column {
                Text("Open Ports", fontSize = 10.sp, color = TextMuted)
                Text(
                  text = if (report.openPorts.isNotEmpty()) report.openPorts.joinToString(", ") else "None / Filtered",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (report.openPorts.isNotEmpty()) StarkAmber else MatrixGreen
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text("Vulnerability Assessment:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            report.vulnerabilityNotes.forEach { note ->
              Text("• $note", fontSize = 11.sp, color = TextSecondary)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CybersecurityVaultTab(topics: List<SecurityTopic>, context: Context) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Text(
        text = "ETHICAL HACKING & PENETRATION REPOSITORY",
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = StarkAmber,
        letterSpacing = 1.sp
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "In-depth technical analysis, payload concepts, and hardened remediations",
        fontSize = 11.sp,
        color = TextMuted
      )
    }

    items(topics) { topic ->
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
        border = androidx.compose.foundation.BorderStroke(0.8.dp, ObsidianCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(topic.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
              Text(topic.category, fontSize = 10.sp, color = ArcCyan)
            }
            Icon(Icons.Default.Shield, contentDescription = null, tint = ArcCyan, modifier = Modifier.size(18.dp))
          }

          Spacer(modifier = Modifier.height(6.dp))
          Text(topic.description, fontSize = 11.5.sp, color = TextSecondary)

          Spacer(modifier = Modifier.height(8.dp))
          Text("PAYLOAD CONCEPT:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LaserCrimson)
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFF030712),
            border = androidx.compose.foundation.BorderStroke(0.6.dp, LaserCrimson.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = topic.payloadExample,
              fontFamily = FontFamily.Monospace,
              fontSize = 10.sp,
              color = Color(0xFFFCA5A5),
              modifier = Modifier.padding(8.dp)
            )
          }

          Spacer(modifier = Modifier.height(8.dp))
          Text("HARDENED MITIGATION:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MatrixGreen)
          Text(topic.remediation, fontSize = 11.sp, color = Color(0xFF86EFAC))
        }
      }
    }
  }
}
