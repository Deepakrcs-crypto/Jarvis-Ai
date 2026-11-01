package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.StarkAmber

@Composable
fun SoundWaveVisualizer(
  isActive: Boolean,
  modifier: Modifier = Modifier,
  barCount: Int = 18,
  maxHeight: Dp = 38.dp,
  primaryColor: Color = ArcCyan
) {
  val infiniteTransition = rememberInfiniteTransition(label = "soundwave_transition")

  val animValues = List(barCount) { index ->
    val duration = 400 + (index % 5) * 120
    val delay = (index * 60) % 300
    infiniteTransition.animateFloat(
      initialValue = 0.15f,
      targetValue = if (isActive) 0.95f else 0.15f,
      animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = duration, delayMillis = delay, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse
      ),
      label = "bar_$index"
    )
  }

  Row(
    modifier = modifier.height(maxHeight),
    horizontalArrangement = Arrangement.spacedBy(3.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    animValues.forEachIndexed { index, anim ->
      val heightFraction by anim
      val barBrush = Brush.verticalGradient(
        colors = listOf(
          primaryColor,
          if (index % 3 == 0) StarkAmber else primaryColor.copy(alpha = 0.5f)
        )
      )

      Box(
        modifier = Modifier
          .width(3.5.dp)
          .fillMaxHeight(fraction = if (isActive) heightFraction.coerceIn(0.15f, 1f) else 0.15f)
          .background(brush = barBrush, shape = RoundedCornerShape(2.dp))
      )
    }
  }
}
