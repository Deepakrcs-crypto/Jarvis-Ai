package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import com.example.ui.JarvisApp
import com.example.ui.JarvisViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private val viewModel: JarvisViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val permissionLauncher = rememberLauncherForActivityResult(
          ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
          val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
          if (audioGranted) {
            viewModel.showNotice("Voice recognition audio sensors authorized, Boss.")
          }
        }

        LaunchedEffect(Unit) {
          val permissionsToRequest = mutableListOf<String>()
          if (ContextCompat.checkSelfPermission(
              this@MainActivity,
              Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
          ) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
          }
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
              this@MainActivity,
              Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
          ) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
          }

          if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
          }
        }

        JarvisApp(viewModel = viewModel)
      }
    }
  }
}
