package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val JarvisColorScheme =
  darkColorScheme(
    primary = JarvisDarkPrimary,
    onPrimary = JarvisDarkOnPrimary,
    primaryContainer = JarvisDarkPrimaryContainer,
    onPrimaryContainer = JarvisDarkOnPrimaryContainer,
    secondary = JarvisDarkSecondary,
    onSecondary = JarvisDarkOnSecondary,
    secondaryContainer = JarvisDarkSecondaryContainer,
    onSecondaryContainer = JarvisDarkOnSecondaryContainer,
    tertiary = JarvisDarkTertiary,
    onTertiary = JarvisDarkOnTertiary,
    tertiaryContainer = JarvisDarkTertiaryContainer,
    onTertiaryContainer = JarvisDarkOnTertiaryContainer,
    background = JarvisDarkBackground,
    onBackground = JarvisDarkOnBackground,
    surface = JarvisDarkSurface,
    onSurface = JarvisDarkOnSurface,
    surfaceVariant = JarvisDarkSurfaceVariant,
    onSurfaceVariant = JarvisDarkOnSurfaceVariant,
    outline = JarvisDarkOutline,
    error = JarvisDarkError,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false, // Keep high-tech Stark cyan/obsidian identity
  content: @Composable () -> Unit,
) {
  val colorScheme =
    if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val context = LocalContext.current
      dynamicDarkColorScheme(context)
    } else {
      JarvisColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
