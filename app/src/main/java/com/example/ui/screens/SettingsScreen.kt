package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.HearClearViewModel
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.SurfaceCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: HearClearViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.userSettings.collectAsStateWithLifecycle()
    val isHindi = settings.appLanguage == "hi"

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .testTag("settings_screen")
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (isHindi) "सेटिंग्स / App Settings" else "Settings & Preferences",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = if (isHindi) "ब्लूटूथ ऑडियो और हियरिंग अनुभव अनुकूलित करें" else "Customize Bluetooth audio ducking and passthrough",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Language Selector
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth().testTag("language_card")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) "ऐप भाषा / Language" else "App Language",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = isHindi,
                        onClick = { viewModel.updateLanguage("hi") },
                        label = { Text("हिंदी (Hindi)", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricCyan.copy(alpha = 0.2f),
                            selectedLabelColor = ElectricCyan
                        ),
                        modifier = Modifier.weight(1f).testTag("lang_hi_chip")
                    )

                    FilterChip(
                        selected = !isHindi,
                        onClick = { viewModel.updateLanguage("en") },
                        label = { Text("English", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricCyan.copy(alpha = 0.2f),
                            selectedLabelColor = ElectricCyan
                        ),
                        modifier = Modifier.weight(1f).testTag("lang_en_chip")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Audio & Call-Out Behaviors
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth().testTag("audio_behavior_card")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (isHindi) "ऑडियो नियंत्रण / Audio Controls" else "Audio & Ducking Controls",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Auto Ducking
                SettingSwitchRow(
                    icon = Icons.Default.VolumeDown,
                    title = if (isHindi) "म्यूजिक/वीडियो धीमा करें (Auto-Duck)" else "Auto-Duck Media Volume",
                    subtitle = if (isHindi) "कोई बोले तो बैकग्राउंड म्यूजिक या वीडियो का वॉल्यूम धीमा कर दें" else "Automatically lowers background media when speech is heard",
                    checked = settings.autoDuckMedia,
                    onCheckedChange = { viewModel.updateAutoDuckMedia(it) },
                    testTag = "auto_duck_switch"
                )

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 10.dp))

                // Alert Chime
                SettingSwitchRow(
                    icon = Icons.Default.Notifications,
                    title = if (isHindi) "अलर्ट चाइम टोन बजाएं" else "Play Gentle In-Ear Chime",
                    subtitle = if (isHindi) "कॉल-आउट डिटेक्ट होने पर हेडफोन में मीठी चाइम बजेगी" else "Plays a soft dual-tone chime in earbuds so you notice the call",
                    checked = settings.playAlertChime,
                    onCheckedChange = { viewModel.updatePlayAlertChime(it) },
                    testTag = "alert_chime_switch"
                )

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 10.dp))

                // Vibration
                SettingSwitchRow(
                    icon = Icons.Default.Vibration,
                    title = if (isHindi) "वाइब्रेशन अलर्ट" else "Haptic Vibration Alert",
                    subtitle = if (isHindi) "फोन में डबल वाइब्रेशन से आपको तुरंत सूचित करेगा" else "Dual pulse vibration on device when called",
                    checked = settings.vibrateOnAlert,
                    onCheckedChange = { viewModel.updateVibrateOnAlert(it) },
                    testTag = "vibrate_switch"
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Silence Hold Time
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth().testTag("hold_time_card")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = MintEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) "बातचीत होल्ड अवधि (Resume Delay)" else "Silence Hold Duration",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isHindi) "बोलना बंद होने के कितने सेकंड बाद म्यूजिक वापस सामान्य हो:"
                    else "How many seconds of silence before media volume returns to normal:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(2, 3, 4, 5).forEach { seconds ->
                        FilterChip(
                            selected = settings.silenceHoldDurationSec == seconds,
                            onClick = { viewModel.updateSilenceHoldDuration(seconds) },
                            label = { Text("${seconds}s", fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MintEmerald.copy(alpha = 0.2f),
                                selectedLabelColor = MintEmerald
                            ),
                            modifier = Modifier.weight(1f).testTag("hold_time_${seconds}s_chip")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sensitivity Presets Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth().testTag("sensitivity_presets_card")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) "माहौल के अनुसार प्रीसेट (Presets)" else "Environment Presets",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = settings.sensitivityDb <= 52f,
                        onClick = { viewModel.updateSensitivity(50f) },
                        label = { Text(if (isHindi) "शांत कमरा" else "Quiet", fontSize = 11.5.sp) },
                        modifier = Modifier.weight(1f).testTag("preset_quiet")
                    )

                    FilterChip(
                        selected = settings.sensitivityDb in 53f..64f,
                        onClick = { viewModel.updateSensitivity(58f) },
                        label = { Text(if (isHindi) "सामान्य घर" else "Office", fontSize = 11.5.sp) },
                        modifier = Modifier.weight(1f).testTag("preset_office")
                    )

                    FilterChip(
                        selected = settings.sensitivityDb >= 65f,
                        onClick = { viewModel.updateSensitivity(70f) },
                        label = { Text(if (isHindi) "शोरगुल / जिम" else "Gym/Street", fontSize = 11.5.sp) },
                        modifier = Modifier.weight(1f).testTag("preset_gym")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Privacy & 100% On-Device Guarantee
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MintEmerald.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MintEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (isHindi) "100% ऑन-डिवाइस सुरक्षित प्रोसेसिंग" else "100% On-Device & Private",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isHindi) "आपकी आवाज़ या कोई भी बातचीत सर्वर पर नहीं भेजी जाती। सारा विश्लेषण फोन के अंदर ही होता है।"
                        else "No audio recordings or personal data are ever uploaded. All processing happens entirely on your phone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun SettingSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ElectricCyan,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ElectricCyan,
                checkedTrackColor = ElectricCyan.copy(alpha = 0.35f)
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
