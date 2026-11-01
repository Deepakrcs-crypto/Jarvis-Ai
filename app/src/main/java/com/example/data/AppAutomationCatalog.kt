package com.example.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import com.example.model.AppAutomation
import com.example.model.AppCategory

object AppAutomationCatalog {

  fun getAutomations(): List<AppAutomation> = listOf(
    AppAutomation(
      id = "camera",
      name = "Camera / Optical Sensors",
      packageName = "com.google.android.GoogleCamera",
      category = AppCategory.SYSTEM,
      description = "Launch camera for immediate optical feed, snapshot or video capture.",
      directActionKey = "ACTION_CAMERA",
      stepByStepGuide = listOf(
        "1. Initialize Camera hardware via MediaStore intent.",
        "2. Switch between Front (TrueDepth) and Rear wide/telephoto lenses.",
        "3. Engage high-speed burst capture or 4K/60fps video logging.",
        "4. Stream feed into vision processing queue."
      ),
      automationScript = "// Kotlin / Android Intent\nval cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)\ncontext.startActivity(cameraIntent)"
    ),
    AppAutomation(
      id = "dialer",
      name = "Communications / Phone",
      packageName = "com.google.android.dialer",
      category = AppCategory.COMMUNICATION,
      description = "Open phone interface, dial contacts or emergency frequencies.",
      directActionKey = "ACTION_DIAL",
      stepByStepGuide = listOf(
        "1. Open Telephony manager via DIAL action with recipient URI.",
        "2. Bypass keypad with voice-directed DTMF tones.",
        "3. Log outgoing timestamp and call duration in Jarvis HUD.",
        "4. Activate speakerphone when hands-free mode is engaged."
      ),
      automationScript = "// Hands-free dialer launch\nval dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse(\"tel:\"))\ncontext.startActivity(dialIntent)"
    ),
    AppAutomation(
      id = "messages",
      name = "Encrypted SMS / Messages",
      packageName = "com.google.android.apps.messaging",
      category = AppCategory.COMMUNICATION,
      description = "Send and receive text transmissions with auto-formatting.",
      directActionKey = "ACTION_SMS",
      stepByStepGuide = listOf(
        "1. Construct SMS intent with pre-populated message payload.",
        "2. Verify contact identity against Jarvis address book.",
        "3. Dispatch message through default cellular SMSC provider.",
        "4. Auto-log transmission in Communication triage history."
      ),
      automationScript = "// Instant SMS dispatch\nval smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse(\"smsto:\")).apply {\n    putExtra(\"sms_body\", \"Sir, dispatching Jarvis status update.\")\n}\ncontext.startActivity(smsIntent)"
    ),
    AppAutomation(
      id = "browser",
      name = "Tactical Web Uplink / Browser",
      packageName = "com.android.chrome",
      category = AppCategory.PRODUCTIVITY,
      description = "Launch browser to any target URL, search query or web terminal.",
      directActionKey = "ACTION_BROWSER",
      stepByStepGuide = listOf(
        "1. Open default browser view via ACTION_VIEW.",
        "2. Direct URL target with HTTPS encryption enforcement.",
        "3. Enable desktop user-agent or headless scraping proxy if commanded.",
        "4. Extract DOM elements and summarize key findings."
      ),
      automationScript = "val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(\"https://google.com\"))\ncontext.startActivity(webIntent)"
    ),
    AppAutomation(
      id = "settings",
      name = "Core Android System Settings",
      packageName = "com.android.settings",
      category = AppCategory.SYSTEM,
      description = "Direct access to device configurations, radios, and security permissions.",
      directActionKey = "ACTION_SETTINGS",
      stepByStepGuide = listOf(
        "1. Route directly to Android System Settings panel.",
        "2. Access Wireless & Networks, Developer Options, or Battery Saver.",
        "3. Adjust Do Not Disturb permissions or Background App restrictions.",
        "4. Toggle Developer ADB debugging or USB tethering."
      ),
      automationScript = "val settingsIntent = Intent(Settings.ACTION_SETTINGS)\ncontext.startActivity(settingsIntent)"
    ),
    AppAutomation(
      id = "clock",
      name = "Chronometer / Alarms & Timers",
      packageName = "com.google.android.deskclock",
      category = AppCategory.SYSTEM,
      description = "Set tactical timers, morning wake protocols, and mission alarms.",
      directActionKey = "ACTION_ALARM",
      stepByStepGuide = listOf(
        "1. Trigger AlarmClock.ACTION_SET_TIMER or ACTION_SET_ALARM.",
        "2. Inject desired duration in seconds or time in 24h format.",
        "3. Set Jarvis wake message as the alarm label.",
        "4. Skip standard confirmation modal for instant hands-free arming."
      ),
      automationScript = "val timerIntent = Intent(AlarmClock.ACTION_SET_TIMER).apply {\n    putExtra(AlarmClock.EXTRA_LENGTH, 300)\n    putExtra(AlarmClock.EXTRA_MESSAGE, \"Jarvis Protocol Alpha\")\n    putExtra(AlarmClock.EXTRA_SKIP_UI, false)\n}\ncontext.startActivity(timerIntent)"
    ),
    AppAutomation(
      id = "whatsapp",
      name = "WhatsApp Automation Guide",
      packageName = "com.whatsapp",
      category = AppCategory.COMMUNICATION,
      description = "Hands-free messaging, contact triage, and auto-dispatch routines.",
      directActionKey = "GUIDE_WHATSAPP",
      stepByStepGuide = listOf(
        "Step 1: To send a message without saving the number, use the URI `https://api.whatsapp.com/send?phone=NUMBER&text=TEXT`.",
        "Step 2: For automated hands-free sending, use Android Accessibility Service or Tasker with AutoInput.",
        "Step 3: To broadcast status updates during Sleeping mode, configure WhatsApp Business Auto-Responder webhook.",
        "Step 4: Say: 'Jarvis, send WhatsApp message to [Contact]' to generate the exact intent."
      ),
      automationScript = "val waIntent = Intent(Intent.ACTION_VIEW).apply {\n    data = Uri.parse(\"https://api.whatsapp.com/send?text=At%20your%20service\")\n    `package` = \"com.whatsapp\"\n}\ncontext.startActivity(waIntent)"
    ),
    AppAutomation(
      id = "termux",
      name = "Termux / Linux Terminal",
      packageName = "com.termux",
      category = AppCategory.DEVELOPMENT,
      description = "Execute command-line shells, Python scripts, and penetration tests.",
      directActionKey = "GUIDE_TERMUX",
      stepByStepGuide = listOf(
        "Step 1: Install Termux & Termux:Tasker plugin from F-Droid.",
        "Step 2: Grant RUN_COMMAND permission: `pm grant com.termux com.termux.permission.RUN_COMMAND`.",
        "Step 3: Execute background bash scripts directly via Intent `com.termux.RUN_COMMAND`.",
        "Step 4: Example commands: `nmap -sV target.local`, `python3 server.py`, `curl ifconfig.me`."
      ),
      automationScript = "# Termux background execution intent\nam startservice --user 0 -n com.termux/.app.RunCommandService \\\n  -a com.termux.RUN_COMMAND \\\n  --es com.termux.RUN_COMMAND_PATH \"/data/data/com.termux/files/usr/bin/bash\" \\\n  --esa com.termux.RUN_COMMAND_ARGUMENTS \"-c,python3 ~/jarvis_agent.py\""
    ),
    AppAutomation(
      id = "spotify",
      name = "Audio Environment / Media",
      packageName = "com.spotify.music",
      category = AppCategory.MEDIA,
      description = "Control acoustic atmospheres, focus playlists, and tactical audio.",
      directActionKey = "GUIDE_MEDIA",
      stepByStepGuide = listOf(
        "Step 1: Dispatch standard MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH.",
        "Step 2: Query key: 'Focus Instrumental', 'Deep Work', or 'AC/DC - Back in Black'.",
        "Step 3: Use MediaControllerCompat to intercept playback state, volume, and track info.",
        "Step 4: Lower media volume dynamically when Jarvis voice synthesis activates."
      ),
      automationScript = "val mediaIntent = Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH).apply {\n    putExtra(MediaStore.EXTRA_MEDIA_FOCUS, \"vnd.android.cursor.item/*\")\n    putExtra(SearchManager.QUERY, \"Stark Industries Lab Soundtrack\")\n}\ncontext.startActivity(mediaIntent)"
    )
  )

  fun executeDirectAction(context: Context, key: String): String {
    return try {
      when (key) {
        "ACTION_CAMERA" -> {
          val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
          context.startActivity(intent)
          "Camera optical sensors engaged, sir."
        }
        "ACTION_DIAL" -> {
          val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:"))
          context.startActivity(intent)
          "Opening communications console dialer."
        }
        "ACTION_SMS" -> {
          val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"))
          context.startActivity(intent)
          "SMS messaging console initialized."
        }
        "ACTION_BROWSER" -> {
          val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com"))
          context.startActivity(intent)
          "Launching tactical web uplink."
        }
        "ACTION_SETTINGS" -> {
          val intent = Intent(Settings.ACTION_SETTINGS)
          context.startActivity(intent)
          "Accessing Android core system settings."
        }
        "ACTION_ALARM" -> {
          val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
          context.startActivity(intent)
          "Displaying chronometer and alarm protocols."
        }
        "GUIDE_WHATSAPP" -> {
          val intent = context.packageManager.getLaunchIntentForPackage("com.whatsapp")
          if (intent != null) {
            context.startActivity(intent)
            "Opening WhatsApp application."
          } else {
            "WhatsApp is not directly installed. Displaying automation script and API guidelines below."
          }
        }
        "GUIDE_TERMUX" -> {
          val intent = context.packageManager.getLaunchIntentForPackage("com.termux")
          if (intent != null) {
            context.startActivity(intent)
            "Engaging Termux Linux environment."
          } else {
            "Termux terminal environment script guidelines displayed below."
          }
        }
        else -> "Action executed, Boss."
      }
    } catch (e: Exception) {
      "Unable to launch app directly: ${e.localizedMessage}. Guiding instructions loaded."
    }
  }
}
