package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.JarvisPersonaMode
import com.example.model.JarvisState
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.GfLavender
import com.example.ui.theme.GfRose
import com.example.ui.theme.GfRoseGlow
import com.example.ui.theme.GfWarmPink
import com.example.ui.theme.LaserCrimson
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.StarkAmber
import com.example.ui.theme.TechEmerald
import com.example.ui.theme.TechEmeraldGlow
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactorView(
  state: JarvisState,
  modifier: Modifier = Modifier,
  personaMode: JarvisPersonaMode = JarvisPersonaMode.BOSS_MODE,
  sizeDp: Dp = 220.dp,
  onClick: () -> Unit = {}
) {
  val infiniteTransition = rememberInfiniteTransition(label = "arc_reactor_anim")

  // Clockwise outer ring rotation
  val outerRotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(
        durationMillis = when (state) {
          JarvisState.LISTENING -> 4000
          JarvisState.PROCESSING -> 2000
          JarvisState.SPEAKING -> 5000
          JarvisState.STANDBY -> 16000
          else -> 8000
        },
        easing = LinearEasing
      ),
      repeatMode = RepeatMode.Restart
    ),
    label = "outer_rot"
  )

  // Counter-clockwise inner ring rotation
  val innerRotation by infiniteTransition.animateFloat(
    initialValue = 360f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
      animation = tween(
        durationMillis = when (state) {
          JarvisState.LISTENING -> 3000
          JarvisState.PROCESSING -> 1500
          JarvisState.SPEAKING -> 4000
          JarvisState.STANDBY -> 12000
          else -> 6000
        },
        easing = LinearEasing
      ),
      repeatMode = RepeatMode.Restart
    ),
    label = "inner_rot"
  )

  // Core pulse scale
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.85f,
    targetValue = 1.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(
        durationMillis = when (state) {
          JarvisState.LISTENING -> 600
          JarvisState.PROCESSING -> 400
          JarvisState.SPEAKING -> 800
          JarvisState.STANDBY -> 2500
          else -> 1500
        },
        easing = FastOutSlowInEasing
      ),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_scale"
  )

  val modeBaseColor = when (personaMode) {
    JarvisPersonaMode.GF_MODE -> GfRose
    JarvisPersonaMode.BOSS_MODE -> ArcCyan
    JarvisPersonaMode.TECH_MODE -> TechEmerald
  }

  val primaryColor = when (state) {
    JarvisState.LISTENING -> modeBaseColor
    JarvisState.PROCESSING -> if (personaMode == JarvisPersonaMode.GF_MODE) GfWarmPink else StarkAmber
    JarvisState.SPEAKING -> modeBaseColor
    JarvisState.STANDBY -> Color(0xFF475569)
    JarvisState.IDLE -> modeBaseColor
  }

  val secondaryColor = when (state) {
    JarvisState.LISTENING -> Color.White
    JarvisState.PROCESSING -> if (personaMode == JarvisPersonaMode.GF_MODE) GfLavender else StarkAmber
    JarvisState.SPEAKING -> if (personaMode == JarvisPersonaMode.GF_MODE) GfWarmPink else MatrixGreen
    JarvisState.STANDBY -> Color(0xFF334155)
    JarvisState.IDLE -> when (personaMode) {
      JarvisPersonaMode.GF_MODE -> GfRoseGlow
      JarvisPersonaMode.BOSS_MODE -> ArcCyanGlow
      JarvisPersonaMode.TECH_MODE -> TechEmeraldGlow
    }
  }

  Box(
    modifier = modifier
      .size(sizeDp)
      .testTag("arc_reactor_core")
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = false, radius = sizeDp / 2),
        onClick = onClick
      ),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.size(sizeDp)) {
      val center = Offset(size.width / 2f, size.height / 2f)
      val radius = size.minDimension / 2f

      // 1. Outer Glow Aura
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(primaryColor.copy(alpha = 0.35f * pulseScale), Color.Transparent),
          center = center,
          radius = radius
        ),
        radius = radius,
        center = center
      )

      // 2. Outermost boundary thin ring
      drawCircle(
        color = primaryColor.copy(alpha = 0.4f),
        radius = radius * 0.95f,
        center = center,
        style = Stroke(width = 1.5.dp.toPx())
      )

      // 3. Rotating Segmented Outer Ring (360 degrees ticks)
      drawOuterSegmentedRing(
        center = center,
        radius = radius * 0.88f,
        rotationDeg = outerRotation,
        color = primaryColor
      )

      // 4. Middle Arc Brackets (3 main segments)
      drawMiddleBrackets(
        center = center,
        radius = radius * 0.72f,
        rotationDeg = innerRotation,
        color = secondaryColor
      )

      // 5. Triangular Energy Nodes / Tech markers
      drawEnergyNodes(
        center = center,
        radius = radius * 0.54f,
        rotationDeg = outerRotation * 0.5f,
        color = primaryColor
      )

      // 6. Inner Glowing Core with Pulse
      val coreRadius = radius * 0.36f * pulseScale
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            Color.White,
            primaryColor,
            primaryColor.copy(alpha = 0.2f),
            Color.Transparent
          ),
          center = center,
          radius = coreRadius
        ),
        radius = coreRadius,
        center = center
      )

      // 7. Core Central Ring
      drawCircle(
        color = Color.White.copy(alpha = 0.9f),
        radius = radius * 0.18f,
        center = center,
        style = Stroke(width = 2.5.dp.toPx())
      )
      drawCircle(
        color = primaryColor,
        radius = radius * 0.08f * pulseScale,
        center = center
      )
    }
  }
}

private fun DrawScope.drawOuterSegmentedRing(
  center: Offset,
  radius: Float,
  rotationDeg: Float,
  color: Color
) {
  val tickCount = 36
  val angleStep = (2 * PI / tickCount).toFloat()
  val rotRad = (rotationDeg * PI / 180f).toFloat()

  for (i in 0 until tickCount) {
    val angle = i * angleStep + rotRad
    val isMajor = i % 3 == 0
    val innerR = if (isMajor) radius * 0.92f else radius * 0.96f
    val outerR = radius

    val start = Offset(
      x = center.x + innerR * cos(angle),
      y = center.y + innerR * sin(angle)
    )
    val end = Offset(
      x = center.x + outerR * cos(angle),
      y = center.y + outerR * sin(angle)
    )

    drawLine(
      color = if (isMajor) color else color.copy(alpha = 0.35f),
      start = start,
      end = end,
      strokeWidth = if (isMajor) 2.5.dp.toPx() else 1.2.dp.toPx(),
      cap = StrokeCap.Round
    )
  }
}

private fun DrawScope.drawMiddleBrackets(
  center: Offset,
  radius: Float,
  rotationDeg: Float,
  color: Color
) {
  val segments = 3
  val sweepAngle = 75f
  val step = 360f / segments

  for (i in 0 until segments) {
    val startAngle = i * step + rotationDeg
    drawArc(
      color = color.copy(alpha = 0.85f),
      startAngle = startAngle,
      sweepAngle = sweepAngle,
      useCenter = false,
      topLeft = Offset(center.x - radius, center.y - radius),
      size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
      style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
    )
  }
}

private fun DrawScope.drawEnergyNodes(
  center: Offset,
  radius: Float,
  rotationDeg: Float,
  color: Color
) {
  val nodeCount = 6
  val angleStep = (2 * PI / nodeCount).toFloat()
  val rotRad = (rotationDeg * PI / 180f).toFloat()

  for (i in 0 until nodeCount) {
    val angle = i * angleStep + rotRad
    val nodePos = Offset(
      x = center.x + radius * cos(angle),
      y = center.y + radius * sin(angle)
    )
    drawCircle(
      color = color,
      radius = 3.dp.toPx(),
      center = nodePos
    )
  }
}
