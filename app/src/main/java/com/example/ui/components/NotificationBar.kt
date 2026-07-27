package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel
import com.example.ui.theme.*

@Composable
fun SimulatedNotificationBar(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    var voiceMessage by remember { mutableStateOf<String?>(null) }
    var isListening by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current

    val isOptimizing by viewModel.isOptimizing.collectAsState()
    val optMessage by viewModel.optimizationMessage.collectAsState()
    
    val bgTaskMessage by viewModel.bgTaskMessage.collectAsState()
    val isBgTaskRunning by viewModel.isBgTaskRunning.collectAsState()

    // Voice Input Speech Simulation Effect
    LaunchedEffect(isListening) {
        if (isListening) {
            voiceMessage = "🎙️ Đang nghe giọng nói của bạn..."
            kotlinx.coroutines.delay(1800)
            
            val voicePhrases = listOf(
                "Làm mượt game và tăng tốc máy ngay",
                "Quét rác và dọn dẹp hệ thống chạy ngầm",
                "Bật cấu hình tiết kiệm pin game cực hạn",
                "Giải phóng RAM ngầm tối đa"
            )
            val randomPhrase = voicePhrases.random()
            voiceMessage = "🗣️ Nhận diện giọng nói: \"$randomPhrase\""
            kotlinx.coroutines.delay(1200)
            
            // Execute background voice command
            viewModel.runBackgroundVoiceOrTextCommand(randomPhrase)
            isListening = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            .background(DeepObsidian)
            .padding(12.dp)
    ) {
        // Notification Pill Header (Always Visible)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CircleNotifications,
                    contentDescription = "Notification",
                    tint = BrightTurquoise,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Bảng Điều Khiển Lệnh Ngầm AI",
                        color = BrightTurquoise,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (isBgTaskRunning) "🔄 AI đang chạy ngầm..." else "Hệ thống mượt mà • Vuốt xuống ra lệnh",
                        color = TextGray,
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(onClick = { isExpanded = !isExpanded }) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = TextGray
                )
            }
        }

        // Expanded Notification Center
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 4.dp)
            ) {
                Divider(color = BorderGreen, thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // Status row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trình tối ưu ngầm của AI (Anti-Lag)",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Ưu tiên luồng Game",
                        color = GlowGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick commands suggestions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Làm mượt máy", "Dọn dẹp rác", "Game Booster").forEach { label ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(BorderGreen)
                                .clickable {
                                    viewModel.runBackgroundVoiceOrTextCommand(label)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(text = label, color = BrightTurquoise, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action area: Text field & Voice Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        placeholder = { Text("Gõ lệnh: 'làm mượt máy', 'tiết kiệm pin'...", fontSize = 11.sp, color = TextGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrightTurquoise,
                            unfocusedBorderColor = BorderGreen,
                            focusedContainerColor = DarkTealCard,
                            unfocusedContainerColor = DarkTealCard,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (inputText.isNotBlank()) {
                                viewModel.runBackgroundVoiceOrTextCommand(inputText)
                                inputText = ""
                                keyboardController?.hide()
                            }
                        })
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Simulated Microphone button
                    IconButton(
                        onClick = {
                            if (!isListening) {
                                isListening = true
                            } else {
                                isListening = false
                                voiceMessage = null
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(if (isListening) CoralVibrant else BrightTurquoise)
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mic",
                            tint = DeepObsidian,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // AI Response / System Feedback Message
                val statusText = if (isOptimizing) optMessage else if (isListening) voiceMessage else bgTaskMessage
                if (statusText != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TransparentGreen)
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = "AI Feedback",
                                tint = GlowGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = statusText,
                                color = GlowGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
