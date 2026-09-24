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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TriggerKeyword
import com.example.ui.HearClearViewModel
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated

@Composable
fun KeywordsScreen(
    viewModel: HearClearViewModel,
    modifier: Modifier = Modifier
) {
    val keywords by viewModel.triggerKeywords.collectAsStateWithLifecycle()
    val lastSpeech by viewModel.lastRecognizedText.collectAsStateWithLifecycle()
    val matchedWord by viewModel.matchedTriggerWord.collectAsStateWithLifecycle()
    val settings by viewModel.userSettings.collectAsStateWithLifecycle()

    val isHindi = settings.appLanguage == "hi"
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().testTag("keywords_screen")) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))

                // Intro explanation
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(ElectricCyan.copy(alpha = 0.15f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RecordVoiceOver,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isHindi) "नाम व पुकारने के शब्द / Trigger Words" else "Voice & Name Triggers",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isHindi) "जब कोई आपका नाम लेगा या इनमें से कोई शब्द बोलेगा, तो तुरंत आपके ईयरफोन में म्यूजिक धीमा होकर उनकी आवाज साफ़ सुनाई देने लगेगी।"
                            else "Add your name, nicknames, or common call-outs. When someone says these near you, HearClear instantly ducks media and alerts you.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Live Speech Recognition Status Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCardElevated),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("live_speech_card")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (lastSpeech.isNotBlank()) AlertAmber.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Mic",
                                tint = if (lastSpeech.isNotBlank()) AlertAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isHindi) "माइक्रोफोन क्या सुन रहा है:" else "Live Speech Heard:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (lastSpeech.isNotBlank()) "\"$lastSpeech\"" else if (isHindi) "आसपास कोई बोलेगा तो यहाँ दिखेगा..." else "Waiting for ambient speech...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (lastSpeech.isNotBlank()) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (lastSpeech.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 2
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = if (isHindi) "सक्रिय नाम व कीवर्ड सूची (${keywords.size})" else "Active Trigger Keywords (${keywords.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Keyword List
            items(keywords, key = { it.id }) { kw ->
                KeywordItemCard(
                    keyword = kw,
                    isMatched = matchedWord == kw.keyword,
                    onToggle = { viewModel.toggleKeywordEnabled(kw) },
                    onDelete = { viewModel.deleteKeyword(kw) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Floating Action Button to Add Name / Keyword
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = ElectricCyan,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_keyword_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Name or Trigger Word")
        }
    }

    if (showAddDialog) {
        AddKeywordDialog(
            isHindi = isHindi,
            onDismiss = { showAddDialog = false },
            onAdd = { newWord ->
                viewModel.addTriggerKeyword(newWord)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun KeywordItemCard(
    keyword: TriggerKeyword,
    isMatched: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isMatched) AlertAmber.copy(alpha = 0.15f) else SurfaceCard
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().testTag("keyword_item_${keyword.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (keyword.isEnabled) ElectricCyan.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.Hearing,
                    contentDescription = null,
                    tint = if (keyword.isEnabled) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = keyword.keyword,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (keyword.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (keyword.isPreset) "Preset phrase" else "Custom call-out",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            Switch(
                checked = keyword.isEnabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ElectricCyan,
                    checkedTrackColor = ElectricCyan.copy(alpha = 0.35f)
                ),
                modifier = Modifier.testTag("keyword_switch_${keyword.id}")
            )

            if (!keyword.isPreset) {
                IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_keyword_${keyword.id}")) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun AddKeywordDialog(
    isHindi: Boolean,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isHindi) "नया नाम या शब्द जोड़ें" else "Add Name / Trigger Word",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column {
                Text(
                    text = if (isHindi) "उदा: आपका नाम (जैसे 'राजेश', 'राहुल'), या घर पर बुलाने का शब्द।"
                    else "E.g. Your name (like 'Rajesh', 'Rahul'), nickname, or calling phrase.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(if (isHindi) "नाम / शब्द दर्ज करें" else "Enter Name / Word") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        focusedLabelColor = ElectricCyan
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_keyword_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (text.isNotBlank()) onAdd(text) },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                modifier = Modifier.testTag("confirm_add_keyword_btn")
            ) {
                Text(if (isHindi) "जोड़ें" else "Add Word", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isHindi) "रद्द करें" else "Cancel")
            }
        }
    )
}
