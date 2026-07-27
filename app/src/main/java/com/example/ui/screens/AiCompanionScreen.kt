package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.ui.TelexConverter
import com.example.ui.theme.*

@Composable
fun AiCompanionScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val chatHistory by viewModel.chatHistory.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Automatically scroll to bottom of chat when new message arrives
    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            lazyListState.animateScrollToItem(chatHistory.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
    ) {
        // Chat Header with status indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkTealCard)
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "Bot",
                    tint = BrightTurquoise,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Tool Vip AI Assistant",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = if (isAiLoading) "Đang suy nghĩ câu trả lời..." else "Hoạt động • Sẵn sàng tối ưu",
                        color = GlowGreen,
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(onClick = { viewModel.clearChat() }) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear Chat",
                    tint = TextGray
                )
            }
        }

        // Chat bubble lists
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (chatHistory.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Forum, "Empty chat", tint = TextGray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Bắt đầu cuộc trò chuyện với Tool Vip AI!\nBạn có thể hỏi bất kỳ câu hỏi nào về thiết bị của bạn,\ncác tính năng VIP, hoặc chỉ ra lệnh:\n'Tối ưu lại ram mượt máy'",
                                color = TextGray,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            } else {
                items(chatHistory, key = { it.id }) { message ->
                    val isUser = message.sender == "USER"
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUser) TransparentGreen else DarkTealCard
                            ),
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isUser) 16.dp else 0.dp,
                                bottomEnd = if (isUser) 0.dp else 16.dp
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isUser) BrightTurquoise.copy(alpha = 0.5f) else BorderGreen
                            ),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = message.message,
                                    color = if (isUser) BrightTurquoise else Color.White,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isUser) "Bạn" else "AI Assistant",
                                    color = TextGray,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            if (isAiLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, BorderGreen)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = BrightTurquoise,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = "AI đang phản hồi...", color = TextGray, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Telex typing helper state
        var isAutoTelexEnabled by remember { mutableStateOf(true) }
        var showTelexHelp by remember { mutableStateOf(true) } // Show by default so they see it!
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkTealCard.copy(alpha = 0.95f))
                .border(BorderStroke(0.5.dp, BorderGreen))
                .padding(vertical = 6.dp, horizontal = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showTelexHelp = !showTelexHelp }
                ) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = "Telex Helper",
                        tint = BrightTurquoise,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Bộ gõ Telex thông minh",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (showTelexHelp) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle Telex Help",
                        tint = TextGray,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Interactive Auto-convert switch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isAutoTelexEnabled) BrightTurquoise.copy(alpha = 0.15f) else Color.Transparent)
                        .border(0.5.dp, if (isAutoTelexEnabled) BrightTurquoise else Color.Gray, RoundedCornerShape(6.dp))
                        .clickable { isAutoTelexEnabled = !isAutoTelexEnabled }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isAutoTelexEnabled) GlowGreen else Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isAutoTelexEnabled) "Tự động: BẬT" else "Tự động: TẮT",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            AnimatedVisibility(visible = showTelexHelp) {
                Column {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "👉 Nhấp vào phím bên dưới để thêm dấu nhanh vào từ cuối cùng:",
                        color = TextGray,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val items = listOf(
                            "s" to "Dấu sắc (á)",
                            "f" to "Dấu huyền (à)",
                            "r" to "Dấu hỏi (ả)",
                            "x" to "Dấu ngã (ã)",
                            "j" to "Dấu nặng (ạ)",
                            "w" to "ă, ư, ow->ơ",
                            "aa" to "â",
                            "ee" to "ê",
                            "oo" to "ô",
                            "dd" to "đ"
                        )
                        items.forEach { (key, desc) ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DeepObsidian),
                                border = BorderStroke(0.5.dp, BorderGreen),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.clickable {
                                    val newText = textInput + key
                                    textInput = TelexConverter.convert(newText)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(BrightTurquoise)
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = key,
                                            color = DeepObsidian,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = desc,
                                        color = Color.White,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        // Message input bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkTealCard)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { 
                    textInput = if (isAutoTelexEnabled) TelexConverter.convert(it) else it
                },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Nhập tin nhắn (tự gõ Telex...)", fontSize = 12.sp, color = TextGray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrightTurquoise,
                    unfocusedBorderColor = BorderGreen,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (textInput.isNotBlank()) {
                        viewModel.sendChatMessage(textInput)
                        textInput = ""
                        keyboardController?.hide()
                    }
                })
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        viewModel.sendChatMessage(textInput)
                        textInput = ""
                        keyboardController?.hide()
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BrightTurquoise)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = DeepObsidian
                )
            }
        }
    }
}
