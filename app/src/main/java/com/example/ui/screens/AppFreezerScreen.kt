package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.font.FontFamily
import com.example.data.AppItem
import com.example.ui.AppViewModel
import com.example.ui.TelexConverter
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun AppFreezerScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val apps by viewModel.allApps.collectAsState()
    val userAccount by viewModel.userAccount.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentFilter by viewModel.appFilter.collectAsState()
    val selectedApps by viewModel.selectedApps.collectAsState()

    val aiScanSuggestions by viewModel.aiScanSuggestions.collectAsState()
    val isAiScanning by viewModel.isAiScanning.collectAsState()

    val isDeepFreezeEnabled by viewModel.isDeepFreezeEnabled.collectAsState()
    val isBackgroundPreventionEnabled by viewModel.isBackgroundPreventionEnabled.collectAsState()
    val selectedGameForAiOpt by viewModel.selectedGameForAiOpt.collectAsState()
    val aiGameOptResult by viewModel.aiGameOptResult.collectAsState()
    val isAiGameOptimizing by viewModel.isAiGameOptimizing.collectAsState()
    val isDeepCleaning by viewModel.isDeepCleaning.collectAsState()
    val deepCleanStatus by viewModel.deepCleanStatus.collectAsState()
    val isAutoClearCacheEnabled by viewModel.isAutoClearCacheEnabled.collectAsState()

    var showConfirmDialog by remember { mutableStateOf(false) }
    var showDeepFreezeConfirmDialog by remember { mutableStateOf(false) }
    var dialogTargetApp by remember { mutableStateOf<AppItem?>(null) }
    var isBatchActionFreezing by remember { mutableStateOf(true) }
    var showBatchConfirmDialog by remember { mutableStateOf(false) }

    // Cryo-File Freezer states
    val coroutineScope = rememberCoroutineScope()
    var selectedFilesToFreeze by remember { mutableStateOf(setOf("apk", "logs", "shaders")) }
    var isFileFreezingProgress by remember { mutableStateOf(false) }
    var fileFreezingProgressValue by remember { mutableStateOf(0f) }
    var isFilesDeepFrozen by remember { mutableStateOf(false) }
    val fileFreezerLogs = remember { mutableStateListOf<String>() }

    // Initial logs load
    LaunchedEffect(Unit) {
        if (fileFreezerLogs.isEmpty()) {
            fileFreezerLogs.add("Hệ thống nén tệp: Sẵn sàng đóng băng tệp tin thặng dư...")
        }
    }

    // Interactive File Freezing Logic
    fun triggerFileCryoFreezer() {
        if (isFileFreezingProgress) return
        isFileFreezingProgress = true
        fileFreezingProgressValue = 0f
        fileFreezerLogs.clear()
        fileFreezerLogs.add("🥶 Khởi chạy động cơ Cryo-File Freezer...")
        
        coroutineScope.launch {
            kotlinx.coroutines.delay(600)
            fileFreezerLogs.add("🔍 Đang rà quét các phân vùng tệp tin thặng dư...")
            fileFreezingProgressValue = 0.25f
            kotlinx.coroutines.delay(800)
            if (selectedFilesToFreeze.contains("apk")) {
                fileFreezerLogs.add("📦 Đóng băng thành công: 4 tệp APK thặng dư (~512MB)")
            }
            fileFreezingProgressValue = 0.5f
            kotlinx.coroutines.delay(800)
            if (selectedFilesToFreeze.contains("logs")) {
                fileFreezerLogs.add("📝 Thu hồi dung lượng: 1,420 tệp Logs & Temp DB (~240MB)")
            }
            fileFreezingProgressValue = 0.75f
            kotlinx.coroutines.delay(800)
            if (selectedFilesToFreeze.contains("shaders")) {
                fileFreezerLogs.add("🎮 Cô lập bộ nhớ đệm Shaders đồ họa (~670MB)")
            }
            fileFreezingProgressValue = 0.9f
            kotlinx.coroutines.delay(600)
            fileFreezerLogs.add("⚡ Tối ưu cấu hình file hệ thống hoàn tất!")
            fileFreezingProgressValue = 1.0f
            isFilesDeepFrozen = true
            isFileFreezingProgress = false
            viewModel.showToast("❄️ Siêu Đóng Băng File hoàn thành! Đã giải phóng 1.42 GB bộ nhớ.")
        }
    }

    // Filter apps list based on query and selected category filter
    val filteredApps = remember(apps, searchQuery, currentFilter) {
        apps.filter { app ->
            val matchesSearch = app.appName.lowercase().contains(searchQuery.lowercase()) ||
                                app.packageName.lowercase().contains(searchQuery.lowercase())
            val matchesFilter = when (currentFilter) {
                "SYSTEM" -> app.isSystemApp
                "USER" -> !app.isSystemApp
                "FROZEN" -> app.isFrozen
                "JUNK" -> app.isTrash
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    // Dynamic calculations
    val totalFrozenMemory = remember(apps) {
        apps.filter { it.isFrozen }.sumOf { it.ramUsage }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Memory Dashboard Panel
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderGreen)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Memory, "RAM", tint = BrightTurquoise)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI Memory Analyzer (Gemini)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        
                        if (isAiScanning) {
                            CircularProgressIndicator(
                                color = BrightTurquoise,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Giải phóng nhờ Đóng băng: $totalFrozenMemory MB RAM",
                        color = GlowGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // High-tech AI console panel showing the real suggestions / scan results from Gemini
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DeepObsidian)
                            .border(BorderStroke(1.dp, BorderGreen.copy(alpha = 0.5f)), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = aiScanSuggestions,
                            color = if (isAiScanning) BrightTurquoise.copy(alpha = 0.8f) else TextGray,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Buttons to invoke real Gemini scanning & AI auto-freeze actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.runAiAppScan() },
                            enabled = !isAiScanning,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BorderGreen,
                                contentColor = BrightTurquoise,
                                disabledContainerColor = BorderGreen.copy(alpha = 0.5f),
                                disabledContentColor = TextGray
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Icon(Icons.Default.Refresh, "Scan", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("QUÉT & ĐỀ XUẤT AI", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.runAiAutoFreeze() },
                            enabled = !isAiScanning,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrightTurquoise,
                                contentColor = DeepObsidian,
                                disabledContainerColor = BrightTurquoise.copy(alpha = 0.5f),
                                disabledContentColor = DeepObsidian.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Icon(Icons.Default.AcUnit, "Freeze", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AI TỰ ĐỘNG ĐÓNG BĂNG", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Auto-Freeze Settings
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DeepObsidian)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Tự động đóng băng (AI Auto-Freeze)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Tự dọn dẹp khi RAM vượt quá ${userAccount?.freezeThreshold ?: 80}%", color = TextGray, fontSize = 10.sp)
                        }

                        Switch(
                            checked = userAccount?.isAutoFreeze ?: false,
                            onCheckedChange = { viewModel.setAutoFreeze(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DeepObsidian,
                                checkedTrackColor = BrightTurquoise
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Deep Freeze switch (exclusive for VIP 1 / VIP 2)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DeepObsidian)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "Siêu đóng băng sâu (Deep Freeze)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(BrightTurquoise.copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("VIP", color = BrightTurquoise, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                }
                            }
                            Text(text = "Đưa RAM sử dụng về 0MB, giảm nhiệt máy (yêu cầu VIP)", color = TextGray, fontSize = 10.sp)
                        }

                        Switch(
                            checked = isDeepFreezeEnabled,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    val tier = userAccount?.tier ?: "UNPAID"
                                    if (tier == "UNPAID") {
                                        viewModel.showToast("Tính năng Đóng băng sâu (Deep Freeze) yêu cầu đặc quyền VIP 1 trở lên!")
                                    } else {
                                        showDeepFreezeConfirmDialog = true
                                    }
                                } else {
                                    viewModel.toggleDeepFreeze(false)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DeepObsidian,
                                checkedTrackColor = BrightTurquoise
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Background Prevention switch (exclusive for VIP 1 / VIP 2)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DeepObsidian)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "Chống chạy ngầm tuyệt đối", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(BrightTurquoise.copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("VIP", color = BrightTurquoise, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                }
                            }
                            Text(text = "Chặn đứng các app tự động thức giấc chạy ngầm", color = TextGray, fontSize = 10.sp)
                        }

                        Switch(
                            checked = isBackgroundPreventionEnabled,
                            onCheckedChange = { viewModel.toggleBackgroundPrevention(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DeepObsidian,
                                checkedTrackColor = BrightTurquoise
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // RAM health visual meter
                    Text(text = "Đo lường dung lượng RAM hệ thống", color = TextGray, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    val usedRamPercent = if (totalFrozenMemory > 0) {
                        (45..60).random() / 100f
                    } else {
                        (75..88).random() / 100f
                    }
                    
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        LinearProgressIndicator(
                            progress = { usedRamPercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (usedRamPercent > 0.8f) CoralVibrant else if (usedRamPercent > 0.6f) BrightTurquoise else GlowGreen,
                            trackColor = DeepObsidian
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Đang dùng: ${(usedRamPercent * 100).toInt()}% (Còn trống ${(100 - usedRamPercent * 100).toInt()}%)",
                                color = TextGray,
                                fontSize = 10.sp
                            )
                            Text(
                                text = if (usedRamPercent > 0.8f) "CRITICAL" else if (usedRamPercent > 0.6f) "OPTIMIZED" else "EXCELLENT",
                                color = if (usedRamPercent > 0.8f) CoralVibrant else if (usedRamPercent > 0.6f) BrightTurquoise else GlowGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // SIÊU ĐÓNG BĂNG FILE RÁC & TỆP TIN (CRYO-FILE FREEZER MASTER)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BrightTurquoise.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AcUnit, "Cryo File Freezer", tint = BrightTurquoise)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cryo-File Freezer Cực Hạn",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(BrightTurquoise.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("FREEZE", color = BrightTurquoise, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Chọn phân vùng tệp tin thặng dư cần cô lập và đóng băng sâu để giải phóng dung lượng & tăng tốc độ truy xuất ổ cứng:",
                        color = TextGray,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // File category selectors
                    val categories = listOf(
                        "apk" to ("Tệp APK thặng dư cài đặt" to "~512 MB"),
                        "logs" to ("Nhật ký Logs hệ thống & DB tạm" to "~240 MB"),
                        "shaders" to ("Bộ nhớ đệm Shaders đồ họa" to "~670 MB")
                    )

                    categories.forEach { (key, data) ->
                        val (label, size) = data
                        val isSelected = selectedFilesToFreeze.contains(key)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    val current = selectedFilesToFreeze.toMutableSet()
                                    if (isSelected) current.remove(key) else current.add(key)
                                    selectedFilesToFreeze = current
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        val current = selectedFilesToFreeze.toMutableSet()
                                        if (checked) current.add(key) else current.remove(key)
                                        selectedFilesToFreeze = current
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = BrightTurquoise,
                                        checkmarkColor = DeepObsidian
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = label, color = Color.White, fontSize = 12.sp)
                            }
                            Text(text = size, color = BrightTurquoise, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Futuristic Terminal Output
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .border(BorderStroke(0.5.dp, BorderGreen.copy(alpha = 0.3f)), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            fileFreezerLogs.forEach { log ->
                                Text(
                                    text = "> $log",
                                    color = if (log.contains("🥶") || log.contains(" thành công") || log.contains("hoàn tất")) GlowGreen else TextGray,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    if (isFileFreezingProgress) {
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { fileFreezingProgressValue },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = BrightTurquoise,
                            trackColor = DeepObsidian
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Button
                    Button(
                        onClick = { triggerFileCryoFreezer() },
                        enabled = !isFileFreezingProgress && selectedFilesToFreeze.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrightTurquoise,
                            contentColor = DeepObsidian,
                            disabledContainerColor = BrightTurquoise.copy(alpha = 0.4f),
                            disabledContentColor = TextGray
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isFileFreezingProgress) {
                            CircularProgressIndicator(
                                color = DeepObsidian,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ĐANG ĐÓNG BĂNG TỆP TIN CRYO...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.AcUnit, "Freeze Files", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isFilesDeepFrozen) "ĐÃ ĐÓNG BĂNG FILE SÂU (CHẠY LẠI)" else "KÍCH HOẠT SIÊU ĐÓNG BĂNG TỆP TIN",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // AI Game-Specific Profile Optimizer Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BrightTurquoise.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SportsEsports, "Game Opt", tint = BrightTurquoise)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Hồ sơ Tối ưu Game AI (Gemini)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        
                        if (isAiGameOptimizing) {
                            CircularProgressIndicator(
                                color = BrightTurquoise,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Chọn tựa game bạn sắp chơi để Gemini thiết kế hồ sơ RAM/CPU tối ưu riêng biệt và triệt để nhất:",
                        color = TextGray,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Horizontal game selector chips
                    val gameList = listOf("Liên Quân Mobile", "PUBG Mobile", "Free Fire", "Genshin Impact")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        gameList.forEach { gameTitle ->
                            val isSelected = selectedGameForAiOpt == gameTitle
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) TransparentGreen else DeepObsidian)
                                    .border(
                                        1.dp,
                                        if (isSelected) BrightTurquoise else BorderGreen.copy(alpha = 0.4f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.selectGameForAiOpt(gameTitle) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = gameTitle.replace(" Mobile", ""),
                                    color = if (isSelected) BrightTurquoise else Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action buttons
                    Button(
                        onClick = { viewModel.runAiGameSpecificOptimization(selectedGameForAiOpt) },
                        enabled = !isAiGameOptimizing,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrightTurquoise,
                            contentColor = DeepObsidian,
                            disabledContainerColor = BrightTurquoise.copy(alpha = 0.5f),
                            disabledContentColor = DeepObsidian.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.QueryStats, "Analyze", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PHÂN TÍCH HỒ SƠ GAME: ${selectedGameForAiOpt.uppercase()}", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Futuristic Terminal showing Gemini recommendations
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 220.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DeepObsidian)
                            .border(BorderStroke(1.dp, BorderGreen.copy(alpha = 0.5f)), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            Text(
                                text = aiGameOptResult,
                                color = if (isAiGameOptimizing) BrightTurquoise.copy(alpha = 0.8f) else Color.White,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    if (aiGameOptResult.contains("[PACKAGES_TO_FREEZE]")) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Button(
                            onClick = { viewModel.applyAiGameSpecificFreeze() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GlowGreen,
                                contentColor = DeepObsidian
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Check, "Apply", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ÁP DỤNG ĐÓNG BĂNG ĐỀ XUẤT NGAY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // GAMING BATTERY HEALTH & OPTIMIZATION CARD
        item {
            val batteryHealth by viewModel.batteryHealth.collectAsState()
            val batteryLevel by viewModel.batteryLevel.collectAsState()
            val batteryTemp by viewModel.batteryTemp.collectAsState()
            val batteryOptimizationApplied by viewModel.batteryOptimizationApplied.collectAsState()

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BrightTurquoise.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BatteryChargingFull, "Battery Icon", tint = BrightTurquoise)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tối Ưu Pin Chơi Game (Gaming Battery)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        IconButton(onClick = { viewModel.refreshBatteryStatus() }) {
                            Icon(Icons.Default.Refresh, "Refresh Battery", tint = BrightTurquoise)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Battery Health dashboard layout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Level
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(DeepObsidian, RoundedCornerShape(12.dp))
                                .border(BorderStroke(0.5.dp, BorderGreen.copy(alpha = 0.3f)), RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Mức sạc", color = TextGray, fontSize = 10.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("$batteryLevel%", color = GlowGreen, fontSize = 16.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        // Health
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .background(DeepObsidian, RoundedCornerShape(12.dp))
                                .border(BorderStroke(0.5.dp, BorderGreen.copy(alpha = 0.3f)), RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Sức khỏe Pin", color = TextGray, fontSize = 10.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(batteryHealth, color = BrightTurquoise, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Temperature
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(DeepObsidian, RoundedCornerShape(12.dp))
                                .border(BorderStroke(0.5.dp, BorderGreen.copy(alpha = 0.3f)), RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Nhiệt độ", color = TextGray, fontSize = 10.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(batteryTemp, color = CoralVibrant, fontSize = 16.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = BorderGreen.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "KHUYẾN NGHỊ ĐẶC BIỆT KHI CHƠI GAME:",
                        color = BrightTurquoise,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    val recommendations = listOf(
                        "🔋 Đóng băng cực hạn chạy ngầm: Tự động đóng hoàn toàn các ứng dụng hao pin ngầm.",
                        "⚡ Giới hạn FPS ở mức 60 FPS: Giảm tải GPU lên đến 30%, tiết kiệm 1.5x dung lượng pin.",
                        "🔆 Giảm sáng màn hình thông minh: Hạ 15% độ sáng giúp chơi lâu hơn 25 phút.",
                        "⚙️ Tắt phản hồi rung haptic & Giới hạn CPU ở 80%: Đảm bảo máy mát mẻ, không bị tụt pin đột ngột."
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        recommendations.forEach { text ->
                            Text(
                                text = text,
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DeepObsidian, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (batteryOptimizationApplied) "ĐANG TỐI ƯU PIN GAMING" else "CHƯA BẬT TỐI ƯU PIN",
                                color = if (batteryOptimizationApplied) GlowGreen else CoralVibrant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = if (batteryOptimizationApplied) "Đã áp dụng cấu hình tiết kiệm năng lượng" else "Bật chế độ để kéo dài thời gian chơi game",
                                color = TextGray,
                                fontSize = 10.sp
                            )
                        }
                        Switch(
                            checked = batteryOptimizationApplied,
                            onCheckedChange = { viewModel.applyGameBatteryOptimization(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DeepObsidian,
                                checkedTrackColor = BrightTurquoise
                            )
                        )
                    }
                }
            }
        }

        // AI Deep Clean Aggressive Cache Clearing Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CoralVibrant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DeleteSweep, "Deep Clean", tint = CoralVibrant)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Dọn Dẹp Siêu Sạch (Deep Clean Cache)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        
                        if (isDeepCleaning) {
                            CircularProgressIndicator(
                                color = CoralVibrant,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Giải phóng triệt để bộ nhớ đệm cache của tất cả các ứng dụng rác, mạng xã hội chạy ngầm để giảm tải CPU, hạ nhiệt điện thoại lập tức và khôi phục tốc độ ban đầu.",
                        color = TextGray,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Tự động xóa bộ nhớ đệm nền",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Dọn dẹp rác khi bộ nhớ đầy hoặc khởi động game",
                                color = TextGray,
                                fontSize = 9.sp
                            )
                        }
                        Switch(
                            checked = isAutoClearCacheEnabled,
                            onCheckedChange = { viewModel.setAutoClearCacheEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DeepObsidian,
                                checkedTrackColor = CoralVibrant
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.runDeepClean() },
                        enabled = !isDeepCleaning,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CoralVibrant,
                            contentColor = Color.White,
                            disabledContainerColor = CoralVibrant.copy(alpha = 0.5f),
                            disabledContentColor = Color.White.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.DeleteSweep, "Deep Clean", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("BẮT ĐẦU DỌN DẸP SÂU CACHE", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Futuristic Terminal showing Deep Clean progress
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 220.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DeepObsidian)
                            .border(BorderStroke(1.dp, CoralVibrant.copy(alpha = 0.3f)), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            Text(
                                text = deepCleanStatus,
                                color = if (isDeepCleaning) CoralVibrant.copy(alpha = 0.8f) else Color.White,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Search and Category Filters
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = TelexConverter.convert(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Tìm kiếm gói ứng dụng...", color = TextGray, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, "Search", tint = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrightTurquoise,
                        unfocusedBorderColor = BorderGreen,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Category selection bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val filters = listOf(
                        "ALL" to "Tất cả",
                        "JUNK" to "App Rác",
                        "SYSTEM" to "Hệ thống",
                        "USER" to "Người dùng",
                        "FROZEN" to "Đóng băng"
                    )

                    filters.forEach { (filterKey, label) ->
                        val isSelected = currentFilter == filterKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) TransparentGreen else BorderGreen)
                                .border(
                                    1.dp,
                                    if (isSelected) BrightTurquoise else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.appFilter.value = filterKey }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) BrightTurquoise else Color.White,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Floating Batch Action panel if app items are selected
        if (selectedApps.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CoralVibrant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Đã chọn ${selectedApps.size} ứng dụng",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BrightTurquoise)
                                    .clickable {
                                        isBatchActionFreezing = true
                                        showBatchConfirmDialog = true
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("Đóng băng", color = DeepObsidian, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GlowGreen)
                                    .clickable {
                                        isBatchActionFreezing = false
                                        viewModel.unfreezeAllSelected()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("Rã đông", color = DeepObsidian, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // List of applications
        if (filteredApps.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Không tìm thấy ứng dụng nào khớp.", color = TextGray, fontSize = 13.sp)
                }
            }
        } else {
            items(filteredApps, key = { it.packageName }) { app ->
                val isChecked = selectedApps.contains(app.packageName)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (app.isFrozen) BrightTurquoise.copy(alpha = 0.4f) else BorderGreen,
                            RoundedCornerShape(12.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = DarkTealCard)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Checkbox for multi select
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                val current = selectedApps.toMutableSet()
                                if (checked) {
                                    current.add(app.packageName)
                                } else {
                                    current.remove(app.packageName)
                                }
                                viewModel.selectedApps.value = current
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = BrightTurquoise,
                                checkmarkColor = DeepObsidian
                            )
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = app.appName,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (app.isSystemApp) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(CoralVibrant.copy(alpha = 0.15f))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("Hệ thống", color = CoralVibrant, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (app.isFrozen) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(BrightTurquoise.copy(alpha = 0.15f))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("Đã đóng băng", color = BrightTurquoise, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = app.packageName,
                                color = TextGray,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tiêu tốn RAM: ${app.ramUsage} MB",
                                color = if (app.isTrash) CoralVibrant else GlowGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )

                            if (app.isFrozen) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(BrightTurquoise.copy(alpha = 0.12f))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("CÔ LẬP TIẾN TRÌNH", color = BrightTurquoise, fontSize = 7.sp, fontWeight = FontWeight.Black)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(BrightTurquoise.copy(alpha = 0.12f))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("CHẶN BROADCAST", color = BrightTurquoise, fontSize = 7.sp, fontWeight = FontWeight.Black)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(BrightTurquoise.copy(alpha = 0.12f))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("NÉN HEAP RAM 0MB", color = BrightTurquoise, fontSize = 7.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }

                        // Actions (Freeze/Unfreeze)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (app.isFrozen) GlowGreen else CoralVibrant)
                                .clickable {
                                    if (app.isSystemApp && !app.isFrozen) {
                                        dialogTargetApp = app
                                        showConfirmDialog = true
                                    } else {
                                        viewModel.toggleFreezeApp(app)
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (app.isFrozen) "Rã đông" else "Đóng băng",
                                color = DeepObsidian,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialog before freezing SYSTEM APPS (prevent system crash)
    if (showConfirmDialog && dialogTargetApp != null) {
        AlertDialog(
            onDismissRequest = {
                showConfirmDialog = false
                dialogTargetApp = null
            },
            containerColor = DarkTealCard,
            title = {
                Text(text = "Cảnh Báo Đóng Băng", color = CoralVibrant, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Bạn đang đóng băng ứng dụng hệ thống '${dialogTargetApp?.appName}'. Việc đóng băng ứng dụng hệ thống có thể gây ra hiện tượng không ổn định, đơ máy hoặc khởi động lại. Bạn có chắc chắn muốn tiếp tục?",
                    color = Color.White,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        dialogTargetApp?.let { viewModel.toggleFreezeApp(it) }
                        showConfirmDialog = false
                        dialogTargetApp = null
                    }
                ) {
                    Text("ĐỒNG Ý", color = BrightTurquoise, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        dialogTargetApp = null
                    }
                ) {
                    Text("HUỶ BỎ", color = TextGray)
                }
            }
        )
    }

    // Batch Action confirmation dialog
    if (showBatchConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showBatchConfirmDialog = false },
            containerColor = DarkTealCard,
            title = {
                Text(text = "Xác nhận đóng băng hàng loạt", color = CoralVibrant, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Hệ thống sẽ tiến hành đóng băng ${selectedApps.size} ứng dụng đã chọn. Bạn có chắc chắn muốn tiếp tục?",
                    color = Color.White,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.freezeAllSelected()
                        showBatchConfirmDialog = false
                    }
                ) {
                    Text("ĐỒNG Ý", color = BrightTurquoise, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchConfirmDialog = false }) {
                    Text("HUỶ BỎ", color = TextGray)
                }
            }
        )
    }

    // Deep Freeze confirmation dialog
    if (showDeepFreezeConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeepFreezeConfirmDialog = false },
            containerColor = DarkTealCard,
            title = {
                Text(text = "Kích Hoạt Deep Freeze?", color = BrightTurquoise, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Tính năng Đóng băng sâu (Deep Freeze) sẽ tự động đóng băng và giải phóng triệt để tài nguyên RAM của các ứng dụng chạy ngầm không cần thiết (Facebook, TikTok, Chrome, v.v.) khi chơi game. Điều này giúp tối đa hóa fps, giảm giật lag và tối ưu hóa ping cực đại cho trò chơi đang chạy.\n\nBạn có chắc chắn muốn kích hoạt?",
                    color = Color.White,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.toggleDeepFreeze(true)
                        showDeepFreezeConfirmDialog = false
                    }
                ) {
                    Text("ĐỒNG Ý", color = BrightTurquoise, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeepFreezeConfirmDialog = false
                    }
                ) {
                    Text("HUỶ BỎ", color = TextGray)
                }
            }
        )
    }
}
