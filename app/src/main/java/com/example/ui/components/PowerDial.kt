package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted

@Composable
fun PowerDial(
    isListening: Boolean,
    isVoiceDetected: Boolean,
    isPassthroughActive: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val infiniteTransition = rememberInfiniteTransition(label = "halo_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.14f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = if (isListening) 0.35f else 0.08f,
        targetValue = if (isListening) 0.05f else 0.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_alpha"
    )

    val activeGlowColor by animateColorAsState(
        targetValue = when {
            isVoiceDetected -> AlertAmber
            isPassthroughActive -> MintEmerald
            isListening -> ElectricCyan
            else -> TextMuted
        },
        animationSpec = tween(durationMillis = 240),
        label = "dial_glow_color"
    )

    Column(
        modifier = modifier.testTag("power_dial_container"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(170.dp)
        ) {
            // Outer Pulsing Ambient Halo
            Box(
                modifier = Modifier
                    .size(165.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(activeGlowColor.copy(alpha = haloAlpha))
            )

            // Outer Border Ring
            Box(
                modifier = Modifier
                    .size(136.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.5.dp,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                activeGlowColor.copy(alpha = 0.8f),
                                activeGlowColor.copy(alpha = 0.15f)
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // Main Core Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(118.dp)
                    .shadow(elevation = if (isListening) 16.dp else 4.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (isListening) {
                                listOf(
                                    SurfaceCard,
                                    DeepNavy
                                )
                            } else {
                                listOf(
                                    SurfaceCard.copy(alpha = 0.6f),
                                    DeepNavy.copy(alpha = 0.9f)
                                )
                            }
                        )
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onToggle(!isListening) }
                    )
                    .testTag("main_power_button")
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                    contentDescription = if (isListening) "Stop Listening" else "Start Listening",
                    tint = activeGlowColor,
                    modifier = Modifier.size(46.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // State Text & Subtitle
        Text(
            text = when {
                isVoiceDetected -> "CALL-OUT HEARD! / आवाज़ सुनी गई"
                isPassthroughActive -> "PASSTHROUGH ACTIVE / आवाज़ चालू"
                isListening -> "MONITORING ACTIVE / निगरानी चालू है"
                else -> "TAP TO START / चालू करने के लिए छुएं"
            },
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                fontSize = 15.sp
            ),
            color = activeGlowColor
        )

        Text(
            text = if (isListening) {
                "Listening for your name & ambient calls"
            } else {
                "Turn on to hear voices while listening to music"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
