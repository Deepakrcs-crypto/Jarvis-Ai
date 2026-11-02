package com.example

import com.example.data.AppAutomationCatalog
import com.example.data.CommunicationRepository
import com.example.model.ChatMessage
import com.example.model.JarvisPersonaMode
import com.example.model.JarvisState
import com.example.model.MessageSender
import com.example.model.SystemMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

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

  @Test
  fun `verify whatsapp messaging and call handling rules`() = kotlinx.coroutines.runBlocking {
    val engine = com.example.ai.JarvisBrainEngine()

    // 1. Initial WhatsApp request asking for message
    val step1 = engine.processCommand(
      input = "Send a WhatsApp message to Krishna",
      currentSystemMode = SystemMode.NOMINAL,
      currentPersona = JarvisPersonaMode.BOSS_MODE,
      ownerName = "Alex",
      history = emptyList()
    )
    assertEquals("What message would you like me to send?", step1.message.text)

    // 2. Follow-up response providing message text
    val step2History = listOf(
      ChatMessage(sender = MessageSender.USER, text = "Send a WhatsApp message to Krishna", personaMode = JarvisPersonaMode.BOSS_MODE),
      ChatMessage(sender = MessageSender.JARVIS, text = "What message would you like me to send?", personaMode = JarvisPersonaMode.BOSS_MODE)
    )
    val step2 = engine.processCommand(
      input = "I am on my way to the meeting",
      currentSystemMode = SystemMode.NOMINAL,
      currentPersona = JarvisPersonaMode.BOSS_MODE,
      ownerName = "Alex",
      history = step2History
    )
    assertTrue(step2.message.text.contains("Krishna"))
    assertTrue(step2.message.text.contains("I am on my way to the meeting"))
    assertTrue(step2.message.text.contains("Ready to send"))

    // 3. WhatsApp Auto-Replies while user is sleeping
    val waAutoReply = engine.processCommand(
      input = "What if someone messages on WhatsApp while I'm sleeping?",
      currentSystemMode = SystemMode.SLEEPING,
      currentPersona = JarvisPersonaMode.BOSS_MODE,
      ownerName = "Alex",
      history = emptyList()
    )
    assertTrue(waAutoReply.message.text.contains("Boss is currently sleeping right now, I will let them know."))

    // 4. Call Management while user is sleeping
    val callResult = engine.processCommand(
      input = "Someone calls while I am asleep",
      currentSystemMode = SystemMode.SLEEPING,
      currentPersona = JarvisPersonaMode.BOSS_MODE,
      ownerName = "Alex",
      history = emptyList()
    )
    assertTrue(callResult.message.text.contains("Alex is currently resting right now. I'll make sure they see this as soon as they wake up."))
  }
}
