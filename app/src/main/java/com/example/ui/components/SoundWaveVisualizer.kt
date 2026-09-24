package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.SurfaceCard

@Composable
fun SoundWaveVisualizer(
    currentDb: Float,
    ambientFloorDb: Float,
    triggerThresholdDb: Float,
    waveformPoints: List<Float>,
    isVoiceActive: Boolean,
    modifier: Modifier = Modifier
) {
    val accentColor by animateColorAsState(
        targetValue = when {
            isVoiceActive -> AlertAmber
            currentDb > triggerThresholdDb * 0.85f -> MintEmerald
            else -> ElectricCyan
        },
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "accentColorAnim"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(20.dp))
            .padding(16.dp)
            .testTag("sound_wave_visualizer")
    ) {
        Column {
            // Header with dB values
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isVoiceActive) Icons.Default.Mic else Icons.Default.GraphicEq,
                        contentDescription = "Acoustic level",
                        tint = accentColor,
                        modifier = Modifier.width(20.dp).height(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isVoiceActive) "VOICE CALL-OUT DETECTED" else "AMBIENT SOUND MONITOR",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = accentColor
                    )
                }

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${currentDb.toInt()}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 26.sp
                        ),
                        color = accentColor
                    )
                    Text(
                        text = " dB",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 24-band Equalizer Waveform Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
            ) {
                val totalBars = waveformPoints.size.coerceAtLeast(1)
                val barSpacing = 4.dp.toPx()
                val totalSpacing = barSpacing * (totalBars - 1)
                val barWidth = ((size.width - totalSpacing) / totalBars).coerceAtLeast(2.dp.toPx())

                val canvasHeight = size.height
                val midY = canvasHeight / 2f

                for (i in 0 until totalBars) {
                    val rawAmp = waveformPoints.getOrElse(i) { 0.05f }
                    val barHeight = (rawAmp * canvasHeight).coerceIn(4.dp.toPx(), canvasHeight)

                    val startX = i * (barWidth + barSpacing)
                    val topY = midY - (barHeight / 2f)

                    val barBrush = Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.95f),
                            accentColor.copy(alpha = 0.45f)
                        ),
                        startY = topY,
                        endY = topY + barHeight
                    )

                    drawRoundRect(
                        brush = barBrush,
                        topLeft = Offset(startX, topY),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                    )
                }

                // Threshold marker line
                val thresholdFraction = (triggerThresholdDb / 100f).coerceIn(0.1f, 0.95f)
                val thresholdY = canvasHeight * (1f - thresholdFraction)

                drawLine(
                    color = Color.White.copy(alpha = 0.35f),
                    start = Offset(0f, thresholdY),
                    end = Offset(size.width, thresholdY),
                    strokeWidth = 1.5.dp.toPx()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer info showing room floor & trigger limit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Room Baseline: ${ambientFloorDb.toInt()} dB",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Voice Trigger: ${triggerThresholdDb.toInt()} dB",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}
