package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PowerSettingsNew
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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.model.ChatMessage
import com.example.model.CommunicationBriefing
import com.example.model.JarvisPersonaMode
import com.example.model.JarvisState
import com.example.model.MessageSender
import com.example.model.SystemMode
import com.example.ui.JarvisViewModel
import com.example.ui.components.ArcReactorView
import com.example.ui.components.SoundWaveVisualizer
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.GfRose
import com.example.ui.theme.GfRoseGlow
import com.example.ui.theme.GfWarmPink
import com.example.ui.theme.LaserCrimson
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.ObsidianBase
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.StarkAmber
import com.example.ui.theme.TechEmerald
import com.example.ui.theme.TechEmeraldGlow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HudVoiceScreen(
  viewModel: JarvisViewModel,
  state: JarvisState,
  personaMode: JarvisPersonaMode,
  systemMode: SystemMode,
  messages: List<ChatMessage>,
  activeBriefing: CommunicationBriefing?,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var textInput by remember { mutableStateOf("") }
  val listState = rememberLazyListState()

  val activeAccentColor = when (personaMode) {
    JarvisPersonaMode.GF_MODE -> GfRose
    JarvisPersonaMode.BOSS_MODE -> ArcCyan
    JarvisPersonaMode.TECH_MODE -> TechEmerald
  }

  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(ObsidianBase)
  ) {
    // Top HUD Mode Status Bar & Multi-Persona Switcher
    HudTopBar(
      systemMode = systemMode,
      personaMode = personaMode,
      state = state,
      onToggleStandby = { viewModel.toggleStandby() },
      onSelectMode = { viewModel.setSystemMode(it) },
      onSelectPersona = { viewModel.setPersonaMode(it) },
      onTriggerBriefing = { viewModel.generateBriefing() }
    )

    // Center Arc Reactor Core with Holographic Aura
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        ArcReactorView(
          state = state,
          personaMode = personaMode,
          sizeDp = 160.dp,
          onClick = { viewModel.toggleVoiceListening() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // State indicator & Voice Waveform
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier.padding(horizontal = 16.dp)
        ) {
          SoundWaveVisualizer(
            isActive = state == JarvisState.SPEAKING || state == JarvisState.LISTENING,
            maxHeight = 20.dp,
            barCount = 12,
            primaryColor = activeAccentColor
          )

          Spacer(modifier = Modifier.width(8.dp))

          Text(
            text = when (state) {
              JarvisState.LISTENING -> "JARVIS IS LISTENING..."
              JarvisState.PROCESSING -> "NEURAL PROCESSING..."
              JarvisState.SPEAKING -> "TRANSMITTING VOCAL FEEDBACK..."
              JarvisState.STANDBY -> "STANDBY // TAP TO ACTIVATE"
              JarvisState.IDLE -> "${personaMode.iconEmoji} ${personaMode.title.uppercase()} ONLINE"
            },
            color = when (state) {
              JarvisState.LISTENING -> activeAccentColor
              JarvisState.PROCESSING -> StarkAmber
              JarvisState.SPEAKING -> MatrixGreen
              JarvisState.STANDBY -> TextMuted
              JarvisState.IDLE -> activeAccentColor
            },
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.1.sp
          )

          Spacer(modifier = Modifier.width(8.dp))

          SoundWaveVisualizer(
            isActive = state == JarvisState.SPEAKING || state == JarvisState.LISTENING,
            maxHeight = 20.dp,
            barCount = 12,
            primaryColor = activeAccentColor
          )
        }
      }
    }

    // Scrollable Terminal / Chat Feed
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      contentPadding = PaddingValues(bottom = 12.dp)
    ) {
      items(messages, key = { it.id }) { msg ->
        MessageBubble(
          message = msg,
          onActionClick = { action -> viewModel.sendCommand(action) },
          onCopyCode = { code ->
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Jarvis Code", code))
            viewModel.showNotice("Code snippet copied to clipboard.")
          }
        )
      }
    }

    // Command Input & Mic Bar
    CommandInputBar(
      textInput = textInput,
      onTextChange = { textInput = it },
      isListening = state == JarvisState.LISTENING,
      accentColor = activeAccentColor,
      onSend = {
        if (textInput.isNotBlank()) {
          viewModel.sendCommand(textInput)
          textInput = ""
        }
      },
      onToggleMic = { viewModel.toggleVoiceListening() }
    )
  }

  // Active Briefing Dialog
  activeBriefing?.let { brief ->
    BriefingDialog(
      briefing = brief,
      onDismiss = { viewModel.dismissBriefing() }
    )
  }
}

@Composable
private fun HudTopBar(
  systemMode: SystemMode,
  personaMode: JarvisPersonaMode,
  state: JarvisState,
  onToggleStandby: () -> Unit,
  onSelectMode: (SystemMode) -> Unit,
  onSelectPersona: (JarvisPersonaMode) -> Unit,
  onTriggerBriefing: () -> Unit
) {
  val accentColor = when (personaMode) {
    JarvisPersonaMode.GF_MODE -> GfRose
    JarvisPersonaMode.BOSS_MODE -> ArcCyan
    JarvisPersonaMode.TECH_MODE -> TechEmerald
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(ObsidianCard)
      .border(0.5.dp, ObsidianCardBorder)
      .padding(horizontal = 14.dp, vertical = 8.dp)
  ) {
    // Title row with Status & Standby
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .background(
              color = if (state == JarvisState.STANDBY) LaserCrimson else accentColor,
              shape = CircleShape
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "JARVIS OS // ${personaMode.title.uppercase()}",
          color = TextPrimary,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        // Briefing shortcut button
        IconButton(
          onClick = onTriggerBriefing,
          modifier = Modifier.size(32.dp).testTag("briefing_btn")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Assignment,
            contentDescription = "Executive Briefing",
            tint = accentColor,
            modifier = Modifier.size(18.dp)
          )
        }

        // Standby Power Toggle
        IconButton(
          onClick = onToggleStandby,
          modifier = Modifier.size(32.dp).testTag("standby_toggle_btn")
        ) {
          Icon(
            imageVector = Icons.Default.PowerSettingsNew,
            contentDescription = "Toggle Standby",
            tint = if (state == JarvisState.STANDBY) LaserCrimson else accentColor,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Multi-Mode Persona Switcher Row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      JarvisPersonaMode.values().forEach { mode ->
        val isSelected = mode == personaMode
        val modeColor = when (mode) {
          JarvisPersonaMode.GF_MODE -> GfRose
          JarvisPersonaMode.BOSS_MODE -> ArcCyan
          JarvisPersonaMode.TECH_MODE -> TechEmerald
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = if (isSelected) modeColor.copy(alpha = 0.2f) else ObsidianBase,
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) modeColor else ObsidianCardBorder
          ),
          modifier = Modifier
            .clickable { onSelectPersona(mode) }
            .testTag("persona_${mode.name.lowercase()}")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(mode.iconEmoji, fontSize = 11.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = mode.title,
              color = if (isSelected) modeColor else TextSecondary,
              fontSize = 11.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Operational Status Mode Pills (Nominal / Busy / Sleeping)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      SystemMode.values().forEach { mode ->
        val isSelected = mode == systemMode
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = if (isSelected) accentColor.copy(alpha = 0.15f) else ObsidianBase,
          border = androidx.compose.foundation.BorderStroke(
            0.8.dp,
            if (isSelected) accentColor else ObsidianCardBorder
          ),
          modifier = Modifier
            .clickable { onSelectMode(mode) }
            .testTag("mode_${mode.name.lowercase()}")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            if (mode == SystemMode.SLEEPING) {
              Icon(
                imageVector = Icons.Default.Bedtime,
                contentDescription = null,
                tint = if (isSelected) accentColor else TextMuted,
                modifier = Modifier.size(11.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
              text = mode.displayName,
              color = if (isSelected) accentColor else TextMuted,
              fontSize = 10.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
          }
        }
      }
    }
  }
}

@Composable
private fun MessageBubble(
  message: ChatMessage,
  onActionClick: (String) -> Unit,
  onCopyCode: (String) -> Unit
) {
  val isUser = message.sender == MessageSender.USER
  val modeColor = when (message.personaMode) {
    JarvisPersonaMode.GF_MODE -> GfRose
    JarvisPersonaMode.BOSS_MODE -> ArcCyan
    JarvisPersonaMode.TECH_MODE -> TechEmerald
  }

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
  ) {
    Surface(
      shape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isUser) 16.dp else 4.dp,
        bottomEnd = if (isUser) 4.dp else 16.dp
      ),
      color = if (isUser) modeColor.copy(alpha = 0.15f) else ObsidianCard,
      border = androidx.compose.foundation.BorderStroke(
        1.dp,
        if (isUser) modeColor.copy(alpha = 0.4f) else ObsidianCardBorder
      ),
      modifier = Modifier
        .fillMaxWidth(if (isUser) 0.85f else 0.95f)
        .testTag("chat_bubble_${message.id}")
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (isUser) "OWNER / YOU" else "JARVIS // ${message.personaMode.title.uppercase()} ${message.personaMode.iconEmoji}",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isUser) modeColor else modeColor,
            letterSpacing = 1.sp
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = message.text,
          fontSize = 13.5.sp,
          color = TextPrimary,
          lineHeight = 20.sp
        )

        // Code snippet block if provided
        if (!message.codeSnippet.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(10.dp))
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF030712),
            border = androidx.compose.foundation.BorderStroke(1.dp, modeColor.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = (message.language ?: "CODE").uppercase(),
                  fontSize = 10.sp,
                  color = modeColor,
                  fontWeight = FontWeight.Bold
                )
                IconButton(
                  onClick = { onCopyCode(message.codeSnippet) },
                  modifier = Modifier.size(24.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy Code",
                    tint = TextSecondary,
                    modifier = Modifier.size(14.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(4.dp))

              Text(
                text = message.codeSnippet,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = Color(0xFFE2E8F0),
                lineHeight = 16.sp
              )
            }
          }
        }
      }
    }

    // Suggested Follow-up Actions
    if (!isUser && message.suggestedActions.isNotEmpty()) {
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        message.suggestedActions.forEach { action ->
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = ObsidianCard,
            border = androidx.compose.foundation.BorderStroke(0.8.dp, modeColor.copy(alpha = 0.4f)),
            modifier = Modifier.clickable { onActionClick(action) }
          ) {
            Text(
              text = action,
              color = modeColor,
              fontSize = 11.sp,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun CommandInputBar(
  textInput: String,
  onTextChange: (String) -> Unit,
  isListening: Boolean,
  accentColor: Color,
  onSend: () -> Unit,
  onToggleMic: () -> Unit
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .background(ObsidianCard)
      .border(0.5.dp, ObsidianCardBorder),
    color = ObsidianCard
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Voice Mic Button
      IconButton(
        onClick = onToggleMic,
        modifier = Modifier
          .size(44.dp)
          .background(
            color = if (isListening) LaserCrimson else accentColor.copy(alpha = 0.2f),
            shape = CircleShape
          )
          .testTag("mic_toggle_button")
      ) {
        Icon(
          imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
          contentDescription = "Voice Input",
          tint = if (isListening) Color.White else accentColor,
          modifier = Modifier.size(22.dp)
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      OutlinedTextField(
        value = textInput,
        onValueChange = onTextChange,
        placeholder = {
          Text(
            text = "Command Jarvis (e.g. 'GF Mode', 'Status', 'Code a script')",
            fontSize = 12.sp,
            color = TextMuted
          )
        },
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary,
          focusedBorderColor = accentColor,
          unfocusedBorderColor = ObsidianCardBorder,
          cursorColor = accentColor
        ),
        shape = RoundedCornerShape(24.dp),
        singleLine = true,
        modifier = Modifier
          .weight(1f)
          .heightIn(min = 44.dp)
          .testTag("command_input_field")
      )

      Spacer(modifier = Modifier.width(8.dp))

      IconButton(
        onClick = onSend,
        enabled = textInput.isNotBlank(),
        modifier = Modifier
          .size(44.dp)
          .background(
            color = if (textInput.isNotBlank()) accentColor else Color.Transparent,
            shape = CircleShape
          )
          .testTag("send_command_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.Send,
          contentDescription = "Send Command",
          tint = if (textInput.isNotBlank()) Color(0xFF030712) else TextMuted,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

@Composable
private fun BriefingDialog(
  briefing: CommunicationBriefing,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = ObsidianCard,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, tint = ArcCyan, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Executive Briefing // ${briefing.timestamp}",
          color = TextPrimary,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = ObsidianBase,
            border = androidx.compose.foundation.BorderStroke(0.6.dp, ObsidianCardBorder),
            modifier = Modifier.weight(1f)
          ) {
            Column(modifier = Modifier.padding(8.dp)) {
              Text("Total Screened", fontSize = 9.sp, color = TextMuted)
              Text("${briefing.totalMissed}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = ObsidianBase,
            border = androidx.compose.foundation.BorderStroke(0.6.dp, ObsidianCardBorder),
            modifier = Modifier.weight(1f)
          ) {
            Column(modifier = Modifier.padding(8.dp)) {
              Text("Urgent Flags", fontSize = 9.sp, color = TextMuted)
              Text("${briefing.urgentCount}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (briefing.urgentCount > 0) LaserCrimson else MatrixGreen)
            }
          }
        }

        if (briefing.callsSummary.isNotEmpty()) {
          Text("MISSED CALLS:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ArcCyan)
          briefing.callsSummary.forEach { call ->
            Text("• $call", fontSize = 11.5.sp, color = TextPrimary)
          }
        }

        if (briefing.messagesSummary.isNotEmpty()) {
          Text("LOGGED TRANSMISSIONS:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StarkAmber)
          briefing.messagesSummary.forEach { msg ->
            Text("• $msg", fontSize = 11.5.sp, color = TextSecondary)
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = ArcCyan)
      ) {
        Text("Understood", color = Color(0xFF030712), fontWeight = FontWeight.Bold)
      }
    }
  )
}
