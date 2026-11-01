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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppAutomationCatalog
import com.example.model.AppAutomation
import com.example.model.DeviceTelemetry
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
fun DeviceControlScreen(
  viewModel: JarvisViewModel,
  telemetry: DeviceTelemetry,
  systemMode: SystemMode,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val automations = remember { AppAutomationCatalog.getAutomations() }
  var selectedAutomation by remember { mutableStateOf<AppAutomation?>(null) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(ObsidianBase)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header
    item {
      Column {
        Text(
          text = "DEVICE CONTROL & AUTOMATION HUB",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = ArcCyan,
          letterSpacing = 1.2.sp
        )
        Text(
          text = "Hands-free background supervisor, hardware telemetry & app execution",
          fontSize = 12.sp,
          color = TextSecondary
        )
      }
    }

    // Live Telemetry Cards Grid
    item {
      TelemetryGrid(telemetry)
    }

    // Mode Protocol Control
    item {
      ModeStatusBanner(
        currentMode = systemMode,
        onModeSelect = { viewModel.setSystemMode(it) }
      )
    }

    // App Control Direct Launchers
    item {
      Text(
        text = "HANDS-FREE APP CONTROLLER",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = StarkAmber,
        letterSpacing = 1.sp
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Tap to launch or inspect exact automation logic and scripts",
        fontSize = 11.sp,
        color = TextMuted
      )
    }

    items(automations, key = { it.id }) { item ->
      AppAutomationCard(
        automation = item,
        onDirectLaunch = {
          viewModel.executeAppDirectAction(item.directActionKey)
        },
        onViewSteps = {
          selectedAutomation = item
        }
      )
    }
  }

  // Dialog showing step-by-step logic & automation script
  selectedAutomation?.let { auto ->
    AlertDialog(
      onDismissRequest = { selectedAutomation = null },
      containerColor = ObsidianCard,
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = getIconForApp(auto.id),
            contentDescription = null,
            tint = ArcCyan,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = auto.name,
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
          )
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = auto.description,
            fontSize = 12.5.sp,
            color = TextSecondary
          )

          Text(
            text = "STEP-BY-STEP AUTOMATION LOGIC:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = StarkAmber
          )

          auto.stepByStepGuide.forEach { step ->
            Text(
              text = step,
              fontSize = 12.sp,
              color = TextPrimary,
              lineHeight = 17.sp
            )
          }

          if (!auto.automationScript.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "TACTICAL EXECUTION SCRIPT:",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = ArcCyan
            )
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color(0xFF030712),
              border = androidx.compose.foundation.BorderStroke(0.8.dp, ArcCyanGlow),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = auto.automationScript,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = Color(0xFFCBD5E1),
                modifier = Modifier.padding(8.dp)
              )
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.executeAppDirectAction(auto.directActionKey)
            selectedAutomation = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = ArcCyan)
        ) {
          Text("Execute Action", color = Color(0xFF030712), fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = {
            auto.automationScript?.let { script ->
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              clipboard.setPrimaryClip(ClipData.newPlainText("Jarvis Script", script))
              viewModel.showNotice("Automation script copied.")
            }
            selectedAutomation = null
          }
        ) {
          Text("Copy Script", color = ArcCyan)
        }
      }
    )
  }
}

@Composable
private fun TelemetryGrid(telemetry: DeviceTelemetry) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = ObsidianCard),
    border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianCardBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "LIVE SYSTEM TELEMETRY",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = ArcCyan,
          letterSpacing = 1.sp
        )
        Text(
          text = "Uptime: ${telemetry.uptimeText}",
          fontSize = 10.sp,
          color = TextMuted
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Battery
        TelemetryItem(
          icon = Icons.Default.BatteryChargingFull,
          label = "Battery",
          value = "${telemetry.batteryLevel}%",
          subValue = if (telemetry.isCharging) "Charging" else "Discharging",
          progress = telemetry.batteryLevel / 100f,
          accentColor = if (telemetry.batteryLevel < 20) LaserCrimson else MatrixGreen,
          modifier = Modifier.weight(1f)
        )

        // RAM
        val ramFraction = if (telemetry.ramTotalMb > 0) telemetry.ramUsedMb.toFloat() / telemetry.ramTotalMb else 0.4f
        TelemetryItem(
          icon = Icons.Default.Memory,
          label = "RAM Usage",
          value = "${telemetry.ramUsedMb} MB",
          subValue = "of ${telemetry.ramTotalMb} MB",
          progress = ramFraction,
          accentColor = ArcCyan,
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Storage
        val storageFraction = if (telemetry.storageTotalGb > 0) (telemetry.storageTotalGb - telemetry.storageFreeGb).toFloat() / telemetry.storageTotalGb.toFloat() else 0.5f
        TelemetryItem(
          icon = Icons.Default.SdStorage,
          label = "Storage Free",
          value = "${telemetry.storageFreeGb} GB",
          subValue = "Total: ${telemetry.storageTotalGb} GB",
          progress = storageFraction,
          accentColor = StarkAmber,
          modifier = Modifier.weight(1f)
        )

        // Network
        TelemetryItem(
          icon = Icons.Default.Wifi,
          label = "Network Link",
          value = telemetry.networkType.take(12),
          subValue = "Encrypted TLS",
          progress = 1.0f,
          accentColor = MatrixGreen,
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

@Composable
private fun TelemetryItem(
  icon: ImageVector,
  label: String,
  value: String,
  subValue: String,
  progress: Float,
  accentColor: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = ObsidianBase,
    border = androidx.compose.foundation.BorderStroke(0.6.dp, ObsidianCardBorder),
    modifier = modifier
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = accentColor,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = label,
          fontSize = 10.sp,
          color = TextSecondary,
          fontWeight = FontWeight.Medium
        )
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = value,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )
      Text(
        text = subValue,
        fontSize = 9.sp,
        color = TextMuted
      )
      Spacer(modifier = Modifier.height(6.dp))
      LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        color = accentColor,
        trackColor = ObsidianCardBorder,
        modifier = Modifier.fillMaxWidth().height(3.dp)
      )
    }
  }
}

@Composable
private fun ModeStatusBanner(
  currentMode: SystemMode,
  onModeSelect: (SystemMode) -> Unit
) {
  Card(
    shape = RoundedCornerShape(14.dp),
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
        Column {
          Text(
            text = "OPERATIONAL STATUS: ${currentMode.displayName.uppercase()}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = when (currentMode) {
              SystemMode.NOMINAL -> ArcCyan
              SystemMode.BUSY -> StarkAmber
              SystemMode.SLEEPING -> MatrixGreen
            }
          )
          Text(
            text = currentMode.subtitle,
            fontSize = 11.sp,
            color = TextSecondary
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        SystemMode.values().forEach { mode ->
          val isSelected = mode == currentMode
          Button(
            onClick = { onModeSelect(mode) },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isSelected) ArcCyan else ObsidianBase
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f)
          ) {
            Text(
              text = mode.name,
              color = if (isSelected) Color(0xFF030712) else TextSecondary,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}

@Composable
private fun AppAutomationCard(
  automation: AppAutomation,
  onDirectLaunch: () -> Unit,
  onViewSteps: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = ObsidianCard),
    border = androidx.compose.foundation.BorderStroke(0.8.dp, ObsidianCardBorder),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("app_card_${automation.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .background(ArcCyan.copy(alpha = 0.15f), shape = RoundedCornerShape(10.dp))
            .border(0.8.dp, ArcCyanGlow, RoundedCornerShape(10.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = getIconForApp(automation.id),
            contentDescription = null,
            tint = ArcCyan,
            modifier = Modifier.size(22.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Text(
            text = automation.name,
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = automation.description,
            color = TextSecondary,
            fontSize = 11.sp,
            maxLines = 1
          )
        }
      }

      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedButton(
          onClick = onViewSteps,
          shape = RoundedCornerShape(8.dp),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Text("Logic", fontSize = 10.5.sp, color = ArcCyan)
        }

        Button(
          onClick = onDirectLaunch,
          colors = ButtonDefaults.buttonColors(containerColor = ArcCyan),
          shape = RoundedCornerShape(8.dp),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Execute",
            tint = Color(0xFF030712),
            modifier = Modifier.size(14.dp)
          )
        }
      }
    }
  }
}

private fun getIconForApp(id: String): ImageVector {
  return when (id) {
    "camera" -> Icons.Default.CameraAlt
    "dialer" -> Icons.Default.Phone
    "messages" -> Icons.AutoMirrored.Filled.Chat
    "browser" -> Icons.Default.Language
    "settings" -> Icons.Default.Settings
    "clock" -> Icons.Default.Alarm
    "whatsapp" -> Icons.AutoMirrored.Filled.Chat
    "termux" -> Icons.Default.Terminal
    "spotify" -> Icons.Default.MusicNote
    else -> Icons.Default.PlayArrow
  }
}
