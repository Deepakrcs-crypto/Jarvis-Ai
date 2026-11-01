package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppAutomationCatalog
import com.example.data.CommunicationRepository
import com.example.model.JarvisPersonaMode
import com.example.model.JarvisState
import com.example.model.SystemMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Jarvis OS", appName)
  }

  @Test
  fun `verify automation catalog loads standard apps`() {
    val automations = AppAutomationCatalog.getAutomations()
    assertTrue(automations.isNotEmpty())
    val camera = automations.find { it.id == "camera" }
    assertNotNull(camera)
  }

  @Test
  fun `verify system modes and personas exist`() {
    assertEquals(5, JarvisState.values().size)
    assertEquals(3, SystemMode.values().size)
    assertEquals(3, JarvisPersonaMode.values().size)
    assertNotNull(JarvisPersonaMode.GF_MODE)
    assertNotNull(JarvisPersonaMode.BOSS_MODE)
    assertNotNull(JarvisPersonaMode.TECH_MODE)
  }

  @Test
  fun `verify autonomous auto-responder exact string templates`() {
    val repo = CommunicationRepository()
    val restingLog = repo.addSimulatedEvent(
      name = "Sarah",
      detail = "+1555",
      type = "Incoming Call",
      message = "Are you awake?",
      currentMode = SystemMode.SLEEPING,
      ownerName = "Alex"
    )
    assertEquals(
      "Hey, Alex is currently resting right now. I'll make sure they see this as soon as they wake up.",
      restingLog.autoResponseSent
    )

    val busyLog = repo.addSimulatedEvent(
      name = "David",
      detail = "+1555",
      type = "SMS Message",
      message = "Quick question",
      currentMode = SystemMode.BUSY,
      ownerName = "Alex"
    )
    assertEquals(
      "Hey, Alex is currently away/busy. They'll get back to you shortly.",
      busyLog.autoResponseSent
    )

    val briefing = repo.generateBriefing("Alex")
    assertTrue(briefing.totalMissed >= 2)
    assertTrue(briefing.formattedSpeechBrief.contains("Alex"))
  }
}
