package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CommunicationLog
import com.example.model.SystemMode
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
fun CommunicationHubScreen(
  viewModel: JarvisViewModel,
  logs: List<CommunicationLog>,
  currentMode: SystemMode,
  modifier: Modifier = Modifier
) {
  val voiceSettings by viewModel.voiceSettings.collectAsState()
  var showSimulationDialog by remember { mutableStateOf(false) }
  var showNameEditDialog by remember { mutableStateOf(false) }

  val activeRuleText = when (currentMode) {
    SystemMode.SLEEPING -> "Hey, ${voiceSettings.ownerName} is currently resting right now. I'll make sure they see this as soon as they wake up."
    SystemMode.BUSY -> "Hey, ${voiceSettings.ownerName} is currently away/busy. They'll get back to you shortly."
    SystemMode.NOMINAL -> "Transmissions routed directly to ${voiceSettings.ownerName}'s heads-up display."
  }

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
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "COMMUNICATION TRIAGE HUB",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ArcCyan,
            letterSpacing = 1.2.sp
          )
          Text(
            text = "Owner Designation: ${voiceSettings.ownerName} • Tap edit to change",
            fontSize = 11.5.sp,
            color = TextSecondary
          )
        }

        Row {
          IconButton(
            onClick = { showNameEditDialog = true },
            modifier = Modifier.size(36.dp)
          ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Name", tint = ArcCyan, modifier = Modifier.size(16.dp))
          }
          IconButton(
            onClick = { viewModel.clearAllComms() },
            modifier = Modifier.size(36.dp)
          ) {
            Icon(Icons.Default.Delete, contentDescription = "Clear Logs", tint = TextMuted, modifier = Modifier.size(18.dp))
          }
        }
      }
    }

    // Active Screener Rule Banner & Briefing Button
    Card(
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = ObsidianCard),
      border = androidx.compose.foundation.BorderStroke(1.dp, ArcCyanGlow),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "AUTONOMOUS SCREENER: ${currentMode.displayName.uppercase()}",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = StarkAmber
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "ACTIVE AUTO-REPLY TEMPLATE:",
              fontSize = 9.5.sp,
              fontWeight = FontWeight.Bold,
              color = ArcCyan
            )
            Text(
              text = "\"$activeRuleText\"",
              fontSize = 11.5.sp,
              color = TextPrimary,
              lineHeight = 16.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = { viewModel.generateBriefing() },
            colors = ButtonDefaults.buttonColors(containerColor = ArcCyan),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f).testTag("generate_briefing_btn")
          ) {
            Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, tint = Color(0xFF030712), modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Request Briefing", color = Color(0xFF030712), fontWeight = FontWeight.Bold, fontSize = 11.sp)
          }

          OutlinedButton(
            onClick = { showSimulationDialog = true },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f).testTag("simulate_event_button")
          ) {
            Icon(Icons.Default.AddAlert, contentDescription = null, tint = ArcCyan, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Simulate Call/SMS", color = ArcCyan, fontSize = 11.sp)
          }
        }
      }
    }

    // Logs Feed
    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      if (logs.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 40.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(Icons.Default.Check, contentDescription = null, tint = MatrixGreen, modifier = Modifier.size(36.dp))
              Spacer(modifier = Modifier.height(8.dp))
              Text("No pending communications in queue.", fontSize = 13.sp, color = TextSecondary)
              Text("Jarvis has triaged all channels cleanly, Boss.", fontSize = 11.sp, color = TextMuted)
            }
          }
        }
      } else {
        items(logs, key = { it.id }) { log ->
          CommunicationLogCard(
            log = log,
            onMarkReviewed = { viewModel.markCommsReviewed(log.id) }
          )
        }
      }
    }
  }

  // Simulation Dialog
  if (showSimulationDialog) {
    SimulationDialog(
      onDismiss = { showSimulationDialog = false },
      onSubmit = { name, detail, type, message ->
        viewModel.simulateIncomingCommunication(name, detail, type, message)
        showSimulationDialog = false
      }
    )
  }

  // Owner Name Edit Dialog
  if (showNameEditDialog) {
    var tempName by remember { mutableStateOf(voiceSettings.ownerName) }
    AlertDialog(
      onDismissRequest = { showNameEditDialog = false },
      containerColor = ObsidianCard,
      title = {
        Text("Customize Owner Name", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("This name is used in auto-replies (e.g. 'Hey, [User] is resting...').", fontSize = 11.sp, color = TextSecondary)
          OutlinedTextField(
            value = tempName,
            onValueChange = { tempName = it },
            placeholder = { Text("e.g. Alex or Boss") },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              focusedBorderColor = ArcCyan,
              unfocusedBorderColor = ObsidianCardBorder
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("owner_name_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.setOwnerName(tempName)
            showNameEditDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = ArcCyan)
        ) {
          Text("Save", color = Color(0xFF030712), fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showNameEditDialog = false }) {
          Text("Cancel", color = TextSecondary)
        }
      }
    )
  }
}

@Composable
private fun CommunicationLogCard(
  log: CommunicationLog,
  onMarkReviewed: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = ObsidianCard),
    border = androidx.compose.foundation.BorderStroke(
      0.8.dp,
      if (log.isUrgent) LaserCrimson.copy(alpha = 0.6f) else ObsidianCardBorder
    ),
    modifier = Modifier.fillMaxWidth().testTag("log_card_${log.id}")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .background(
                color = if (log.isUrgent) LaserCrimson.copy(alpha = 0.2f) else ArcCyan.copy(alpha = 0.15f),
                shape = CircleShape
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = getCommsIcon(log.type),
              contentDescription = null,
              tint = if (log.isUrgent) LaserCrimson else ArcCyan,
              modifier = Modifier.size(16.dp)
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Text(log.contactName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("${log.type} • ${log.timeFormatted}", fontSize = 10.sp, color = TextSecondary)
          }
        }

        if (log.isUrgent) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = LaserCrimson.copy(alpha = 0.2f),
            border = androidx.compose.foundation.BorderStroke(0.6.dp, LaserCrimson)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.PriorityHigh, contentDescription = null, tint = LaserCrimson, modifier = Modifier.size(11.dp))
              Text("URGENT", color = LaserCrimson, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "TRANSMISSION: \"${log.originalMessage}\"",
        fontSize = 12.sp,
        color = TextPrimary
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Auto-Response Dispatched
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = ObsidianBase,
        border = androidx.compose.foundation.BorderStroke(0.6.dp, ArcCyanGlow),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(10.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = null, tint = ArcCyan, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("JARVIS AUTONOMOUS DISPATCH:", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = ArcCyan)
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = log.autoResponseSent,
            fontSize = 11.sp,
            color = TextSecondary,
            lineHeight = 15.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        if (!log.reviewed) {
          Button(
            onClick = onMarkReviewed,
            colors = ButtonDefaults.buttonColors(containerColor = ArcCyan.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(6.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = ArcCyan, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Mark Reviewed", color = ArcCyan, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
          }
        } else {
          Text("Reviewed by Boss", color = MatrixGreen, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        }
      }
    }
  }
}

@Composable
private fun SimulationDialog(
  onDismiss: () -> Unit,
  onSubmit: (String, String, String, String) -> Unit
) {
  var name by remember { mutableStateOf("Sarah Jenkins") }
  var type by remember { mutableStateOf("Incoming Call") }
  var message by remember { mutableStateOf("Hey, need urgent confirmation on cloud server deployment!") }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = ObsidianCard,
    title = {
      Text("Simulate Communication Event", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Test Jarvis's autonomous triage and auto-reply during rest or busy sessions.", fontSize = 11.sp, color = TextSecondary)

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Sender / Contact Name", fontSize = 11.sp) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedBorderColor = ArcCyan,
            unfocusedBorderColor = ObsidianCardBorder
          ),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = message,
          onValueChange = { message = it },
          label = { Text("Message Content / Voicemail", fontSize = 11.sp) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedBorderColor = ArcCyan,
            unfocusedBorderColor = ObsidianCardBorder
          ),
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onSubmit(name, "+1 555-0100", type, message) },
        colors = ButtonDefaults.buttonColors(containerColor = ArcCyan)
      ) {
        Text("Dispatch to Jarvis", color = Color(0xFF030712), fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Cancel", color = TextSecondary)
      }
    }
  )
}

private fun getCommsIcon(type: String): ImageVector {
  return when {
    type.contains("Call", ignoreCase = true) -> Icons.Default.Call
    type.contains("Message", ignoreCase = true) || type.contains("SMS", ignoreCase = true) -> Icons.AutoMirrored.Filled.Message
    else -> Icons.Default.Notifications
  }
}
