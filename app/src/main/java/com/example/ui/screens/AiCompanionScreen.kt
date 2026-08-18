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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel
import com.example.ui.TelexConverter
import com.example.ui.theme.*
import com.example.data.*

@Composable
fun AiCompanionScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val chatHistory by viewModel.chatHistory.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    
    val allImportedFeatures by viewModel.allImportedFeatures.collectAsState()
    val isImportingCode by viewModel.isImportingCode.collectAsState()
    val importResultMsg by viewModel.importResultMsg.collectAsState()

    var activeScreenTab by remember { mutableStateOf(0) } // 0: Chatbot Trò Chuyện, 1: AI Ghép Code & Kịch Bản

    var textInput by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Fields for importing custom code
    var importFileName by remember { mutableStateOf("customer_care_ai.py") }
    var importRawCode by remember { mutableStateOf("") }

    // Scroll to bottom when new chat message comes
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
        // Upper Tab Bar for switching workspaces
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkTealCard)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { activeScreenTab = 0 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeScreenTab == 0) BrightTurquoise else Color.Black.copy(alpha = 0.4f)
                ),
                border = BorderStroke(1.dp, if (activeScreenTab == 0) BrightTurquoise else BorderGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Chat",
                        tint = if (activeScreenTab == 0) DeepObsidian else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CHAT TRỢ LÝ",
                        color = if (activeScreenTab == 0) DeepObsidian else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Button(
                onClick = { activeScreenTab = 1 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeScreenTab == 1) CoralVibrant else Color.Black.copy(alpha = 0.4f)
                ),
                border = BorderStroke(1.dp, if (activeScreenTab == 1) CoralVibrant else BorderGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1.1f),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "Code",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GHÉP CODE & BOT",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        if (activeScreenTab == 0) {
            // WORKSPACE: ACTIVE CHATBOT CONVERSATION
            val activeBot = allImportedFeatures.firstOrNull { it.isActive }
            
            // Bot Info Panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .border(BorderStroke(0.5.dp, BorderGreen.copy(alpha = 0.3f)))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (activeBot != null) CoralVibrant.copy(alpha = 0.2f) else BrightTurquoise.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (activeBot != null) Icons.Default.Bolt else Icons.Default.SmartToy,
                            contentDescription = "Bot Status",
                            tint = if (activeBot != null) CoralVibrant else BrightTurquoise,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = activeBot?.detectedName ?: "Trợ lý AI Tool Vip (Mặc định)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = activeBot?.detectedDescription ?: "Hỗ trợ chơi game đỉnh cao, kéo tâm mượt mà, giảm giật lag.",
                            color = TextGray,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }
                
                IconButton(onClick = { viewModel.clearChat() }) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Xóa hội thoại",
                        tint = TextGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Chat bubble list area
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
                                Icon(Icons.Default.Forum, "No chats yet", tint = TextGray, modifier = Modifier.size(44.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (activeBot != null) {
                                        "Kịch bản '${activeBot.detectedName}' đã được kích hoạt!\nHãy hỏi bot bất cứ điều gì để thử nghiệm kịch bản\nhoặc cấu hình bạn vừa ghép từ GitHub."
                                    } else {
                                        "Chào sếp! Em là AI Trợ lý mặc định.\nHãy hỏi em bất kỳ câu hỏi nào về game Liên Quân,\nđộ nhạy bắn Free Fire, kéo tâm cực mượt,\nhoặc cách tăng FPS & hạ ping mạng nhé!"
                                    },
                                    color = TextGray,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 17.sp
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
                                    0.5.dp,
                                    if (isUser) BrightTurquoise.copy(alpha = 0.5f) else BorderGreen
                                ),
                                modifier = Modifier.widthIn(max = 290.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = message.message,
                                        color = if (isUser) BrightTurquoise else Color.White,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isUser) "Sếp" else (activeBot?.detectedName ?: "AI Assistant"),
                                        color = TextGray,
                                        fontSize = 8.sp,
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
                                        modifier = Modifier.size(12.dp),
                                        color = BrightTurquoise,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(text = "AI đang xử lý kịch bản phản hồi...", color = TextGray, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Telex panel & input
            var isAutoTelexEnabled by remember { mutableStateOf(true) }
            var showTelexHelp by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkTealCard.copy(alpha = 0.95f))
                    .border(BorderStroke(0.5.dp, BorderGreen))
                    .padding(vertical = 4.dp, horizontal = 12.dp)
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
                            contentDescription = "Telex",
                            tint = BrightTurquoise,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Hỗ trợ gõ dấu Telex Việt Nam",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (showTelexHelp) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle help",
                            tint = TextGray,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { isAutoTelexEnabled = !isAutoTelexEnabled }
                            .padding(4.dp)
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
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                AnimatedVisibility(visible = showTelexHelp) {
                    Column {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val helperKeys = listOf(
                                "s" to "Sắc (á)", "f" to "Huyền (à)", "r" to "Hỏi (ả)",
                                "x" to "Ngã (ã)", "j" to "Nặng (ạ)", "w" to "ă/ư",
                                "aa" to "â", "ee" to "ê", "oo" to "ô", "dd" to "đ"
                            )
                            helperKeys.forEach { (key, label) ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.Black.copy(alpha = 0.4f))
                                        .border(0.5.dp, BorderGreen, RoundedCornerShape(4.dp))
                                        .clickable {
                                            val converted = TelexConverter.convert(textInput + key)
                                            textInput = converted
                                        }
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(text = "$key : $label", color = Color.White, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Msg sending input field
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
                    placeholder = { Text("Trò chuyện cùng AI...", fontSize = 12.sp, color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrightTurquoise,
                        unfocusedBorderColor = BorderGreen,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
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
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BrightTurquoise)
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = DeepObsidian)
                }
            }

        } else {
            // WORKSPACE: AI GHÉP CODE & KỊCH BẢN CHATBOT (IMPORT RAW FILES / GITHUB REPO SCRIPTS)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Interactive Dashboard Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CoralVibrant.copy(alpha = 0.12f))
                        .border(BorderStroke(1.dp, CoralVibrant.copy(alpha = 0.4f)), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudDownload, "GitHub Importer", tint = CoralVibrant, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BỘ CHUYỂN ĐỔI CHATBOT AI & MÃ NGUỒN CUSTOM",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            text = "Sếp dán bất kỳ file code nào tìm được trên GitHub (Python, JS, C++, Kotlin, JSON, Script), AI sẽ tự động phân tích và chuyển đổi thành kịch bản chatbot thông minh chạy ngay tức khắc!",
                            color = TextGray,
                            fontSize = 10.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                // --- PREMIUM DROP ZONE & FILE DROPPING MOCK AREA ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, CoralVibrant.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DriveFolderUpload,
                            contentDescription = "Drop zone",
                            tint = CoralVibrant,
                            modifier = Modifier.size(44.dp)
                        )
                        
                        Text(
                            text = "Kéo & Thả Tệp Tin GitHub Hoặc Dán Code Tại Đây",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        
                        Text(
                            text = "Sếp có thể nhấn dán trực tiếp từ bộ nhớ tạm, hoặc nhấp chọn nhanh một trong các mẫu kịch bản đắt giá bên dưới để AI tự dọn dẹp và ghép mã:",
                            color = TextGray,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 15.sp
                        )

                        // QUICK TEMPLATE SELECTOR PILLS
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val presetTemplates = listOf(
                                Triple(
                                    "cskh_auto.json", 
                                    "Mẫu: CSKH Tự Động (JSON)",
                                    "{\n  \"bot_profile\": \"AI Chăm Sóc Khách Hàng chuyên nghiệp\",\n  \"instructions\": \"Tự động hướng dẫn nâng cấp VIP1/VIP2, nạp tiền nhanh và xử lý khiếu nại game 24/7. Luôn xưng hô thân mật.\"\n}"
                                ),
                                Triple(
                                    "freefire_booster.py", 
                                    "Mẫu: Ghìm Tâm Free Fire (Python)",
                                    "import fff_optimizer\ndef auto_drag_headshot(dpi=600, speed=1.5):\n    # Hướng dẫn game thủ vuốt tâm chữ J, tinh chỉnh độ nhạy cho mọi điện thoại Samsung, iPhone, Xiaomi cực mượt để ghim đầu đối thủ\n    return fff_optimizer.apply_aim(dpi, speed)"
                                ),
                                Triple(
                                    "boost_fps.sh", 
                                    "Mẫu: Fix Lag & Optimize (Shell)",
                                    "#!/bin/bash\necho 'Đang dọn dẹp bộ nhớ đệm RAM...'\nsudo sysctl -w vm.drop_caches=3\necho 'Kích hoạt DNS Google 8.8.8.8 để hạ ping giật lag cực mượt'"
                                )
                            )

                            presetTemplates.forEach { (fname, btnLabel, content) ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(CoralVibrant.copy(alpha = 0.2f))
                                        .border(BorderStroke(0.5.dp, CoralVibrant), RoundedCornerShape(16.dp))
                                        .clickable {
                                            importFileName = fname
                                            importRawCode = content
                                            viewModel.showToast("Đã nạp thành công mã nguồn mẫu: $fname")
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = btnLabel,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Instant clipboard trigger
                        Button(
                            onClick = {
                                importFileName = "clipboard_github_source.txt"
                                importRawCode = "BOT_NAME: AI Chăm Sóc Độc Quyền\nBOT_DESC: Bot chăm sóc khách hàng tự động cực đỉnh\nBOT_SYSTEM_PROMPT: Bạn là chuyên gia chăm sóc khách hàng VIP của Tool Vip. Luôn xưng hô sếp, chào đón nhiệt thành và hướng dẫn nạp tiền cực nhanh!"
                                viewModel.showToast("Đã dán dữ liệu thành công từ Khay nhớ tạm!")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.5f)),
                            border = BorderStroke(0.5.dp, BorderGreen),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.ContentPaste, null, tint = BrightTurquoise, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("DÁN NHANH TỪ KHAY NHỚ TẠM", color = BrightTurquoise, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // File Upload Inputs
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                    border = BorderStroke(0.5.dp, BorderGreen)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Thông tin mã nguồn đã dán:",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )

                        OutlinedTextField(
                            value = importFileName,
                            onValueChange = { importFileName = it },
                            label = { Text("Tên tệp tin (VD: customer_support.py)", fontSize = 10.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrightTurquoise,
                                unfocusedBorderColor = BorderGreen,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = BrightTurquoise,
                                unfocusedLabelColor = TextGray
                            ),
                            shape = RoundedCornerShape(6.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = importRawCode,
                            onValueChange = { importRawCode = it },
                            label = { Text("Nội dung kịch bản hoặc mã nguồn dán tại vùng thả", fontSize = 10.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrightTurquoise,
                                unfocusedBorderColor = BorderGreen,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = BrightTurquoise,
                                unfocusedLabelColor = TextGray
                            ),
                            shape = RoundedCornerShape(6.dp),
                            minLines = 4,
                            maxLines = 8,
                            modifier = Modifier.fillMaxWidth()
                        )

                        importResultMsg?.let { msg ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black.copy(alpha = 0.4f))
                                    .border(0.5.dp, BorderGreen.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = msg,
                                    color = if (msg.contains("thành công")) GlowGreen else Color.White,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.importCustomCodeAndCompile(importFileName, importRawCode)
                                importRawCode = ""
                            },
                            enabled = !isImportingCode,
                            colors = ButtonDefaults.buttonColors(containerColor = CoralVibrant),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            if (isImportingCode) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ĐANG BIÊN DỊCH & GHÉP BOT AI...", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.Bolt, null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("BIÊN DỊCH & KÍCH HOẠT CHATBOT AI", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // List of Imported Custom bots
                Text(
                    text = "Danh sách Chatbot AI sẵn có (${allImportedFeatures.size}):",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )

                allImportedFeatures.forEach { feature ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                        border = BorderStroke(1.dp, if (feature.isActive) CoralVibrant else BorderGreen)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = feature.detectedName,
                                        color = if (feature.isActive) CoralVibrant else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Tệp: ${feature.fileName}",
                                        color = TextGray,
                                        fontSize = 9.sp
                                    )
                                }

                                // Active Status Badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (feature.isActive) CoralVibrant.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.4f))
                                        .border(0.5.dp, if (feature.isActive) CoralVibrant else Color.Gray, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (feature.isActive) "ĐANG HOẠT ĐỘNG" else "ĐANG TẮT",
                                        color = if (feature.isActive) CoralVibrant else TextGray,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = feature.detectedDescription,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )

                            Divider(color = BorderGreen.copy(alpha = 0.3f), thickness = 0.5.dp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Deletable
                                IconButton(
                                    onClick = { viewModel.deleteImportedFeature(feature.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, "Delete", tint = Color.Red.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Toggle activation
                                Button(
                                    onClick = {
                                        viewModel.setImportedFeatureActiveInVm(feature.id, !feature.isActive)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (feature.isActive) Color.Black.copy(alpha = 0.6f) else CoralVibrant
                                    ),
                                    border = BorderStroke(1.dp, if (feature.isActive) Color.Gray else CoralVibrant),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(
                                        text = if (feature.isActive) "TẮT KỊCH BẢN" else "BẬT KỊCH BẢN AI",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
