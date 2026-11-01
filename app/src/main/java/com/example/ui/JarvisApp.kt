package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ControlCamera
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.JarvisPersonaMode
import com.example.ui.screens.CommunicationHubScreen
import com.example.ui.screens.DeviceControlScreen
import com.example.ui.screens.HudVoiceScreen
import com.example.ui.screens.PersonaSettingsScreen
import com.example.ui.screens.TechConsoleScreen
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.GfRose
import com.example.ui.theme.ObsidianBase
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.StarkAmber
import com.example.ui.theme.TechEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

sealed class NavItem(val title: String, val icon: ImageVector, val tag: String) {
  object Hud : NavItem("HUD Voice", Icons.Default.GraphicEq, "nav_hud")
  object Control : NavItem("Device", Icons.Default.ControlCamera, "nav_control")
  object Tech : NavItem("Console", Icons.Default.Code, "nav_tech")
  object Triage : NavItem("Triage", Icons.Default.NotificationsActive, "nav_triage")
  object Persona : NavItem("Multi-Mode", Icons.Default.SettingsSuggest, "nav_persona")
}

@Composable
fun JarvisApp(viewModel: JarvisViewModel) {
  val jarvisState by viewModel.jarvisState.collectAsState()
  val personaMode by viewModel.personaMode.collectAsState()
  val systemMode by viewModel.systemMode.collectAsState()
  val telemetry by viewModel.telemetry.collectAsState()
  val messages by viewModel.messages.collectAsState()
  val voiceSettings by viewModel.voiceSettings.collectAsState()
  val commsLogs by viewModel.commsLogs.collectAsState()
  val activeBriefing by viewModel.activeBriefing.collectAsState()
  val diagnosticReport by viewModel.diagnosticReport.collectAsState()
  val isDiagnosing by viewModel.isDiagnosing.collectAsState()
  val selectedTab by viewModel.selectedTab.collectAsState()
  val statusNotice by viewModel.statusNotice.collectAsState()

  val activeAccent = when (personaMode) {
    JarvisPersonaMode.GF_MODE -> GfRose
    JarvisPersonaMode.BOSS_MODE -> ArcCyan
    JarvisPersonaMode.TECH_MODE -> TechEmerald
  }

  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(statusNotice) {
    statusNotice?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.dismissNotice()
    }
  }

  val navItems = listOf(
    NavItem.Hud,
    NavItem.Control,
    NavItem.Tech,
    NavItem.Triage,
    NavItem.Persona
  )

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianBase),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    bottomBar = {
      NavigationBar(
        modifier = Modifier
          .border(0.5.dp, ObsidianCardBorder)
          .navigationBarsPadding(),
        containerColor = ObsidianCard,
        contentColor = activeAccent
      ) {
        navItems.forEachIndexed { index, item ->
          val isSelected = selectedTab == index
          NavigationBarItem(
            selected = isSelected,
            onClick = { viewModel.setTab(index) },
            icon = {
              Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = if (isSelected) activeAccent else TextMuted,
                modifier = Modifier.size(20.dp)
              )
            },
            label = {
              Text(
                text = item.title,
                fontSize = 9.5.sp,
                color = if (isSelected) activeAccent else TextMuted
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = activeAccent,
              selectedTextColor = activeAccent,
              indicatorColor = activeAccent.copy(alpha = 0.15f),
              unselectedIconColor = TextMuted,
              unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag(item.tag)
          )
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (selectedTab) {
        0 -> HudVoiceScreen(
          viewModel = viewModel,
          state = jarvisState,
          personaMode = personaMode,
          systemMode = systemMode,
          messages = messages,
          activeBriefing = activeBriefing
        )
        1 -> DeviceControlScreen(
          viewModel = viewModel,
          telemetry = telemetry,
          systemMode = systemMode
        )
        2 -> TechConsoleScreen(
          viewModel = viewModel,
          diagnosticReport = diagnosticReport,
          isDiagnosing = isDiagnosing
        )
        3 -> CommunicationHubScreen(
          viewModel = viewModel,
          logs = commsLogs,
          currentMode = systemMode
        )
        4 -> PersonaSettingsScreen(
          viewModel = viewModel,
          voiceSettings = voiceSettings
        )
      }
    }
  }
}
