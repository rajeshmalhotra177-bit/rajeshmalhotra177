package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.HearClearViewModel
import com.example.ui.components.BluetoothStatusCard
import com.example.ui.components.PowerDial
import com.example.ui.components.SoundWaveVisualizer
import com.example.ui.components.VoiceAlertBanner
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.SurfaceCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HearClearViewModel,
    modifier: Modifier = Modifier
) {
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val currentDb by viewModel.currentDb.collectAsStateWithLifecycle()
    val ambientFloorDb by viewModel.ambientFloorDb.collectAsStateWithLifecycle()
    val waveformPoints by viewModel.waveformPoints.collectAsStateWithLifecycle()
    val isVoiceActive by viewModel.isVoiceActive.collectAsStateWithLifecycle()
    val isPassthroughActive by viewModel.isPassthroughActive.collectAsStateWithLifecycle()
    val bluetoothStatus by viewModel.bluetoothStatus.collectAsStateWithLifecycle()
    val matchedKeyword by viewModel.matchedTriggerWord.collectAsStateWithLifecycle()
    val settings by viewModel.userSettings.collectAsStateWithLifecycle()

    val isHindi = settings.appLanguage == "hi"

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .testTag("home_screen_content")
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Heads-up voice alert banner when active
        VoiceAlertBanner(
            isVoiceActive = isVoiceActive,
            currentDb = currentDb,
            matchedKeyword = matchedKeyword
        )

        if (isVoiceActive) {
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Bluetooth connection indicator card
        BluetoothStatusCard(
            status = bluetoothStatus,
            autoDuckEnabled = settings.autoDuckMedia
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Master Central Power Dial
        PowerDial(
            isListening = isListening,
            isVoiceDetected = isVoiceActive,
            isPassthroughActive = isPassthroughActive,
            onToggle = { enable -> viewModel.toggleListening(enable) }
        )

        Spacer(modifier = Modifier.height(22.dp))

        // Real-time Sound Wave Equalizer
        SoundWaveVisualizer(
            currentDb = currentDb,
            ambientFloorDb = ambientFloorDb,
            triggerThresholdDb = settings.sensitivityDb,
            waveformPoints = waveformPoints,
            isVoiceActive = isVoiceActive
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Listening Modes Switcher (Smart on-demand vs Continuous Transparency)
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth().testTag("mode_selector_card")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (isHindi) "मोड चुनें / Listening Mode" else "Listening Mode",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !settings.continuousPassthrough,
                        onClick = { viewModel.updateContinuousPassthrough(false) },
                        label = {
                            Text(
                                text = if (isHindi) "स्मार्ट कॉल-आउट (Auto)" else "Smart Alert (Auto)",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricCyan.copy(alpha = 0.2f),
                            selectedLabelColor = ElectricCyan
                        ),
                        modifier = Modifier.weight(1f).testTag("mode_smart_chip")
                    )

                    FilterChip(
                        selected = settings.continuousPassthrough,
                        onClick = { viewModel.updateContinuousPassthrough(true) },
                        label = {
                            Text(
                                text = if (isHindi) "लगातार ट्रांसपेरेंसी" else "Continuous Ambient",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MintEmerald.copy(alpha = 0.2f),
                            selectedLabelColor = MintEmerald
                        ),
                        modifier = Modifier.weight(1f).testTag("mode_continuous_chip")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (!settings.continuousPassthrough) {
                        if (isHindi) "म्यूजिक/वीडियो सामान्य रूप से बजेगा, जब कोई बोलेगा तो अपने आप धीमा होकर उनकी आवाज साफ़ सुनाई देगी।"
                        else "Media plays normally; when someone speaks or calls, media volume ducks and voice passes through."
                    } else {
                        if (isHindi) "सुपर-हियरिंग मोड: ईयरफोन पहने हुए भी आसपास की बातचीत लगातार साफ़ सुनाई देती रहेगी।"
                        else "Transparency mode: Amplifies outside voices continuously so you can talk with earphones on."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Controls: Sensitivity & Mic Boost Gain
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth().testTag("quick_controls_card")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isHindi) "ट्रिगर सेंसिटिविटी (Sensitivity)" else "Voice Sensitivity Threshold",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "${settings.sensitivityDb.toInt()} dB",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = ElectricCyan
                    )
                }

                Slider(
                    value = settings.sensitivityDb,
                    onValueChange = { viewModel.updateSensitivity(it) },
                    valueRange = 45f..80f,
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricCyan,
                        activeTrackColor = ElectricCyan
                    ),
                    modifier = Modifier.testTag("sensitivity_slider")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Hearing,
                            contentDescription = null,
                            tint = MintEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isHindi) "वॉइस बूस्ट / एम्प्लिफिकेशन" else "Voice Boost & Clarity",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "${String.format("%.1f", settings.micGainMultiplier)}x",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MintEmerald
                    )
                }

                Slider(
                    value = settings.micGainMultiplier,
                    onValueChange = { viewModel.updateMicGain(it) },
                    valueRange = 1.0f..4.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = MintEmerald,
                        activeTrackColor = MintEmerald
                    ),
                    modifier = Modifier.testTag("mic_gain_slider")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Simulation / Instant Test Button
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth().testTag("simulation_card")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (isHindi) "तुरंत टेस्ट करें / Test In Your Earphones" else "Test Call-Out Feature",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isHindi) "अपने ईयरफोन में म्यूजिक या वीडियो चलाएं और नीचे दिए बटन को दबाकर चेक करें कि आवाज कैसे साफ़ सुनाई देती है।"
                    else "Play any music/video in background and tap below to experience real media ducking and alert chime.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.simulateVoiceCall(70f, "Suno!") },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertAmber),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("simulate_call_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isHindi) "कॉल-आउट सिमुलेट करें (Simulate Call-Out)" else "Simulate Call-Out (Ducks Media)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
    }
}
