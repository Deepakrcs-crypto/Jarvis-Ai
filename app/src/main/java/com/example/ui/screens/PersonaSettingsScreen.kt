package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.JarvisPersonaMode
import com.example.model.VoiceSettings
import com.example.ui.JarvisViewModel
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.GfRose
import com.example.ui.theme.LaserCrimson
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.ObsidianBase
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.StarkAmber
import com.example.ui.theme.TechEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PersonaSettingsScreen(
  viewModel: JarvisViewModel,
  voiceSettings: VoiceSettings,
  modifier: Modifier = Modifier
) {
  val activePersona by viewModel.personaMode.collectAsState()
  var pitch by remember(voiceSettings.pitch) { mutableFloatStateOf(voiceSettings.pitch) }
  var rate by remember(voiceSettings.speechRate) { mutableFloatStateOf(voiceSettings.speechRate) }
  var autoSpeak by remember(voiceSettings.autoSpeakResponse) { mutableStateOf(voiceSettings.autoSpeakResponse) }
  var ownerNameInput by remember(voiceSettings.ownerName) { mutableStateOf(voiceSettings.ownerName) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(ObsidianBase)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Column {
        Text(
          text = "DYNAMIC MULTI-MODE & DIRECTIVES",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = ArcCyan,
          letterSpacing = 1.2.sp
        )
        Text(
          text = "Dynamic personalities: GF Mode, Boss Mode, and Technical/Security Mode",
          fontSize = 11.5.sp,
          color = TextSecondary
        )
      }
    }

    // Dynamic Multi-Mode Selection Card
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, ArcCyanGlow),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "ACTIVE PERSONALITY MODE",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = StarkAmber
          )
          Spacer(modifier = Modifier.height(8.dp))

          JarvisPersonaMode.values().forEach { mode ->
            val isSelected = mode == activePersona
            val modeColor = when (mode) {
              JarvisPersonaMode.GF_MODE -> GfRose
              JarvisPersonaMode.BOSS_MODE -> ArcCyan
              JarvisPersonaMode.TECH_MODE -> TechEmerald
            }

            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isSelected) modeColor.copy(alpha = 0.2f) else ObsidianBase,
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isSelected) modeColor else ObsidianCardBorder
              ),
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { viewModel.setPersonaMode(mode) }
                .testTag("settings_persona_${mode.name.lowercase()}")
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(mode.iconEmoji, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = mode.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) modeColor else TextPrimary
                  )
                  Text(
                    text = mode.subtitle,
                    fontSize = 10.5.sp,
                    color = TextSecondary
                  )
                }
                if (isSelected) {
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = modeColor
                  ) {
                    Text(
                      text = "ACTIVE",
                      color = Color(0xFF030712),
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // Owner Identity Card
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Person, contentDescription = null, tint = ArcCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("OWNER IDENTITY / DESIGNATION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
          }

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = ownerNameInput,
            onValueChange = {
              ownerNameInput = it
              viewModel.setOwnerName(it)
            },
            placeholder = { Text("Enter your name (e.g. Alex or Boss)") },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              focusedBorderColor = ArcCyan,
              unfocusedBorderColor = ObsidianCardBorder
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("owner_name_field")
          )

          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Used in autonomous screening: \"Hey, $ownerNameInput is currently resting right now...\"",
            fontSize = 10.sp,
            color = TextMuted
          )
        }
      }
    }

    // Voice Persona Tuner
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = ArcCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("ACOUSTIC VOICE TUNER", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Pitch Slider
          Text("Vocal Warmth / Pitch: ${String.format("%.2f", pitch)}x", fontSize = 11.sp, color = TextSecondary)
          Slider(
            value = pitch,
            onValueChange = {
              pitch = it
              viewModel.updateVoiceSettings(pitch, rate, autoSpeak)
            },
            valueRange = 0.75f..1.35f,
            colors = SliderDefaults.colors(thumbColor = ArcCyan, activeTrackColor = ArcCyan),
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Rate Slider
          Text("Cadence / Speech Rate: ${String.format("%.2f", rate)}x", fontSize = 11.sp, color = TextSecondary)
          Slider(
            value = rate,
            onValueChange = {
              rate = it
              viewModel.updateVoiceSettings(pitch, rate, autoSpeak)
            },
            valueRange = 0.75f..1.4f,
            colors = SliderDefaults.colors(thumbColor = ArcCyan, activeTrackColor = ArcCyan),
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Auto-speak toggle
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("Vocal Synthesis Playback", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
              Text("Speak conversational replies aloud automatically", fontSize = 10.sp, color = TextMuted)
            }
            Switch(
              checked = autoSpeak,
              onCheckedChange = {
                autoSpeak = it
                viewModel.updateVoiceSettings(pitch, rate, it)
              },
              colors = SwitchDefaults.colors(
                checkedThumbColor = ArcCyan,
                checkedTrackColor = ArcCyan.copy(alpha = 0.3f)
              )
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Test Voice Button
          Button(
            onClick = {
              val testPhrase = when (activePersona) {
                JarvisPersonaMode.GF_MODE -> "Hey sweetheart ❤️ I'm here and listening! Everything is working so smoothly for you."
                JarvisPersonaMode.BOSS_MODE -> "At your command, ${voiceSettings.ownerName}. Audio transducers, neural gates, and vocal intonation are calibrated."
                JarvisPersonaMode.TECH_MODE -> "[SYSTEM_CALIBRATED] Voice telemetry nominal. Script execution engines standing by."
              }
              viewModel.speakPhrase(testPhrase)
            },
            colors = ButtonDefaults.buttonColors(containerColor = ArcCyan),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().testTag("calibrate_voice_btn")
          ) {
            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Color(0xFF030712), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Calibrate Vocal Engine (${activePersona.title})", color = Color(0xFF030712), fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }
      }
    }

    // Reset Chat & Diagnostics
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text("MAINTENANCE PROTOCOLS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
          Spacer(modifier = Modifier.height(10.dp))

          OutlinedButton(
            onClick = {
              viewModel.clearChat()
              viewModel.showNotice("Command buffer cleared.")
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().testTag("clear_chat_button")
          ) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = LaserCrimson, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Clear Session Transcripts", color = LaserCrimson, fontSize = 12.sp)
          }
        }
      }
    }
  }
}
