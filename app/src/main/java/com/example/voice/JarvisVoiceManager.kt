package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class JarvisVoiceManager(
  private val context: Context,
  private val onSpeechResult: (String) -> Unit,
  private val onSpeechError: (String) -> Unit
) {

  private var tts: TextToSpeech? = null
  private var isTtsReady = false

  private var speechRecognizer: SpeechRecognizer? = null
  private val _isListening = MutableStateFlow(false)
  val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

  private val _isSpeaking = MutableStateFlow(false)
  val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

  private val _partialSpokenText = MutableStateFlow("")
  val partialSpokenText: StateFlow<String> = _partialSpokenText.asStateFlow()

  var pitch: Float = 0.92f
    set(value) {
      field = value
      tts?.setPitch(value)
    }

  var speechRate: Float = 1.0f
    set(value) {
      field = value
      tts?.setSpeechRate(value)
    }

  init {
    initTts()
    initSpeechRecognizer()
  }

  private fun initTts() {
    tts = TextToSpeech(context) { status ->
      if (status == TextToSpeech.SUCCESS) {
        val result = tts?.setLanguage(Locale.UK)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
          tts?.setLanguage(Locale.US)
        }
        tts?.setPitch(pitch)
        tts?.setSpeechRate(speechRate)
        isTtsReady = true

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
          override fun onStart(utteranceId: String?) {
            _isSpeaking.value = true
          }

          override fun onDone(utteranceId: String?) {
            _isSpeaking.value = false
          }

          @Deprecated("Deprecated in Java")
          override fun onError(utteranceId: String?) {
            _isSpeaking.value = false
          }
        })
      }
    }
  }

  private fun initSpeechRecognizer() {
    if (SpeechRecognizer.isRecognitionAvailable(context)) {
      speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
        setRecognitionListener(object : RecognitionListener {
          override fun onReadyForSpeech(params: Bundle?) {
            _isListening.value = true
            vibrate(30)
          }

          override fun onBeginningOfSpeech() {}
          override fun onRmsChanged(rmsdB: Float) {}
          override fun onBufferReceived(buffer: ByteArray?) {}

          override fun onEndOfSpeech() {
            _isListening.value = false
          }

          override fun onError(error: Int) {
            _isListening.value = false
            val message = when (error) {
              SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Say 'Jarvis' or tap the Arc Reactor."
              SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout."
              SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
              SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network audio link error."
              else -> "Voice input standby."
            }
            onSpeechError(message)
          }

          override fun onResults(results: Bundle?) {
            _isListening.value = false
            vibrate(50)
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val recognizedText = matches?.firstOrNull()?.trim()
            if (!recognizedText.isNullOrEmpty()) {
              _partialSpokenText.value = ""
              onSpeechResult(recognizedText)
            }
          }

          override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            matches?.firstOrNull()?.let {
              _partialSpokenText.value = it
            }
          }

          override fun onEvent(eventType: Int, params: Bundle?) {}
        })
      }
    }
  }

  fun startListening() {
    if (!SpeechRecognizer.isRecognitionAvailable(context)) {
      onSpeechError("Speech recognition is not supported on this device.")
      return
    }
    stopSpeaking()
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
      putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
      putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
      putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
      putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
      putExtra(RecognizerIntent.EXTRA_PROMPT, "Jarvis is listening, sir...")
    }
    try {
      speechRecognizer?.startListening(intent)
      _isListening.value = true
    } catch (e: Exception) {
      _isListening.value = false
      onSpeechError("Voice initialization failure: ${e.message}")
    }
  }

  fun stopListening() {
    try {
      speechRecognizer?.stopListening()
    } catch (_: Exception) {}
    _isListening.value = false
  }

  fun speak(text: String) {
    if (!isTtsReady || text.isBlank()) return
    // Clean markdown code blocks from speech so it sounds clean and conversational
    val speechCleaned = text
      .replace(Regex("```[a-zA-Z]*\\n[\\s\\S]*?```"), " I have provided the complete executable code script on your display, sir. ")
      .replace(Regex("[*#_`>]"), "")
      .take(450) // Keep spoken responses concise and punchy

    tts?.speak(speechCleaned, TextToSpeech.QUEUE_FLUSH, null, "JARVIS_VOICE_${System.currentTimeMillis()}")
  }

  fun stopSpeaking() {
    tts?.stop()
    _isSpeaking.value = false
  }

  fun vibrate(durationMs: Long) {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator?.vibrate(
          VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
        )
      } else {
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
          @Suppress("DEPRECATION")
          vibrator?.vibrate(durationMs)
        }
      }
    } catch (_: Exception) {}
  }

  fun destroy() {
    stopSpeaking()
    tts?.shutdown()
    tts = null
    speechRecognizer?.destroy()
    speechRecognizer = null
  }
}
