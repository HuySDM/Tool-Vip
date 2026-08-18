package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.example.R
import com.example.data.AppItem
import com.example.ui.AppViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GameBoosterDashboard(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val apps by viewModel.allApps.collectAsState()
    val isOptimizing by viewModel.isOptimizing.collectAsState()
    val optMessage by viewModel.optimizationMessage.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Interface Customization states (dynamic from AppViewModel)
    val themeStyle by viewModel.themeStyle.collectAsState()
    val cardTransparency by viewModel.cardTransparency.collectAsState()
    val cardCornerRadius by viewModel.cardCornerRadius.collectAsState()
    val glowIntensity by viewModel.glowIntensity.collectAsState()
    val dynamicPulseEnabled by viewModel.dynamicPulseEnabled.collectAsState()

    // AI Status Board states (dynamic from AppViewModel)
    val aiStatusScore by viewModel.aiStatusScore.collectAsState()
    val aiScanLogs by viewModel.aiScanLogs.collectAsState()
    val aiRecommendation by viewModel.aiRecommendation.collectAsState()
    val isAiScanning by viewModel.isAiStatusBoardScanning.collectAsState()

    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val activeColors = remember(themeStyle, isDarkMode) { ThemeColors.getColors(themeStyle, isDarkMode) }
    val corners = cardCornerRadius.dp

    // Dynamic Pulsating Animation for card borders (Interface Customization feature)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseGlowAlpha by if (dynamicPulseEnabled) {
        infiniteTransition.animateFloat(
            initialValue = 0.15f,
            targetValue = 0.55f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_alpha"
        )
    } else {
        remember { mutableStateOf(0.3f) }
    }

    // Tab control inside the optimization center
    var activeTab by remember { mutableStateOf(0) } // 0: Tối ưu App, 1: Thử nghiệm băng thông, 2: Trạng thái AI, 3: Biểu đồ thời gian thực, 4: Giao diện

    // Screen local states
    var showAddGameDialog by remember { mutableStateOf(false) }
    var showOptimizationResult by remember { mutableStateOf(false) }
    var lastOptimizedRamAmount by remember { mutableStateOf(0.0) }
    var lastOptimizedAppsCount by remember { mutableStateOf(0) }

    // Dynamic hardware simulation states
    val networkPingBoosted by viewModel.networkPingBoosted.collectAsState()
    var pingValue by remember { mutableStateOf(18) }
    var tempValue by remember { mutableStateOf(36.8f) }
    var cpuUsage by remember { mutableStateOf(65) }
    var gpuUsage by remember { mutableStateOf(48) }

    // Real-time scrolling chart histories
    val cpuHistory = remember { mutableStateListOf<Float>() }
    val ramHistory = remember { mutableStateListOf<Float>() }
    var isAutoAiOptimizingEnabled by remember { mutableStateOf(true) }
    var isExtremeGamerModeEnabled by remember { mutableStateOf(false) }
    var lastRamValue by remember { mutableStateOf(58f) }

    // Fluctuating hardware stats simulation including history, Automated AI, and Gamer Mode
    LaunchedEffect(isOptimizing, networkPingBoosted, isExtremeGamerModeEnabled) {
        // Pre-populate initial histories so charts look beautifully active immediately
        if (cpuHistory.isEmpty()) {
            repeat(15) {
                cpuHistory.add((15..35).random().toFloat())
                ramHistory.add((45..60).random().toFloat())
            }
        }
        while (true) {
            if (isExtremeGamerModeEnabled) {
                pingValue = (11..15).random()
                tempValue = 38.9f + ((1..6).random() / 10f)
                cpuUsage = (91..97).random()
                gpuUsage = (85..95).random()
                lastRamValue = (72..81).random().toFloat()
            } else if (isOptimizing) {
                pingValue = if (networkPingBoosted) (25..45).random() else (50..120).random()
                tempValue = 38.5f + ((1..8).random() / 10f)
                cpuUsage = (85..98).random()
                gpuUsage = (70..90).random()
                lastRamValue = (60..74).random().toFloat()
            } else {
                pingValue = if (networkPingBoosted) (12..21).random() else (25..45).random()
                tempValue = 34.1f + ((1..9).random() / 10f)
                cpuUsage = (15..35).random()
                gpuUsage = (12..28).random()
                lastRamValue = (42..54).random().toFloat()
            }

            // Auto AI Optimization trigger
            if (isAutoAiOptimizingEnabled) {
                if (cpuUsage > 85 || lastRamValue > 80) {
                    viewModel.optimizeRamDirectly {}
                    // Drop values back safely to simulate cleanup
                    cpuUsage = (cpuUsage - (15..25).random()).coerceIn(20, 100)
                    lastRamValue = (lastRamValue - (12..18).random()).coerceIn(35f, 100f)
                }
            }

            if (cpuHistory.size >= 20) cpuHistory.removeAt(0)
            cpuHistory.add(cpuUsage.toFloat())

            if (ramHistory.size >= 20) ramHistory.removeAt(0)
            ramHistory.add(lastRamValue)

            delay(1500)
        }
    }

    // Search and filter states for the list of apps to optimize
    var appSearchText by remember { mutableStateOf("") }
    var selectedAppTypeFilter by remember { mutableStateOf("ALL") } // ALL, USER, SYSTEM, GAMES

    val filteredAppsToOptimize = remember(apps, appSearchText, selectedAppTypeFilter) {
        apps.filter { app ->
            val matchesSearch = app.appName.lowercase().contains(appSearchText.lowercase()) ||
                    app.packageName.lowercase().contains(appSearchText.lowercase())
            
            val isGame = app.appName.lowercase().contains("pubg") ||
                    app.appName.lowercase().contains("free fire") ||
                    app.appName.lowercase().contains("garena") ||
                    app.appName.lowercase().contains("codm") ||
                    app.appName.lowercase().contains("game") ||
                    app.appName.lowercase().contains("strike") ||
                    app.appName.lowercase().contains("mobile")

            val matchesFilter = when (selectedAppTypeFilter) {
                "USER" -> !app.isSystemApp && !isGame
                "SYSTEM" -> app.isSystemApp
                "GAMES" -> isGame
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    // Perform massive game and app optimization action
    val performMassBoost = {
        coroutineScope.launch {
            val unoptimizedCount = filteredAppsToOptimize.count { !it.isFrozen }
            val ramToClear = filteredAppsToOptimize.filter { !it.isFrozen }.sumOf { it.ramUsage }
            
            viewModel.optimizeRamDirectly {
                lastOptimizedRamAmount = if (ramToClear > 0) ramToClear else 1340.0
                lastOptimizedAppsCount = if (unoptimizedCount > 0) unoptimizedCount else 4
                showOptimizationResult = true
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(activeColors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            
            // Modern HUD Header area
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = activeColors.cardBg.copy(alpha = cardTransparency)),
                shape = RoundedCornerShape(corners),
                border = BorderStroke(
                    width = if (glowIntensity == "Strong") 1.5.dp else 1.dp,
                    color = activeColors.border.copy(alpha = pulseGlowAlpha)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(100.dp))
                                        .background(activeColors.primary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "HỆ THỐNG GIÁM SÁT THỜI GIAN THỰC",
                                    color = activeColors.primary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = "TRẠM TỐI ƯU TOÀN DIỆN",
                                color = activeColors.textPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // Logo and Theme Switcher / Mode toggler
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Quick Dark Mode Switcher
                            IconButton(
                                onClick = { viewModel.toggleDarkMode() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(activeColors.border.copy(alpha = 0.15f))
                            ) {
                                Icon(
                                    imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = "Chuyển chế độ Sáng/Tối",
                                    tint = activeColors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Quick Theme Palette Switcher
                            var showThemeMenu by remember { mutableStateOf(false) }
                            Box {
                                IconButton(
                                    onClick = { showThemeMenu = true },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(activeColors.border.copy(alpha = 0.15f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = "Chuyển đổi chủ đề",
                                        tint = activeColors.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = showThemeMenu,
                                    onDismissRequest = { showThemeMenu = false },
                                    modifier = Modifier.background(activeColors.cardBg)
                                ) {
                                    val themeStyles = listOf(
                                        "DeepObsidian" to "Slate Obsidian (Teal)",
                                        "CyberpunkAurora" to "Cyberpunk Aurora (Pink)",
                                        "NeonInferno" to "Neon Inferno (Red)",
                                        "AcidEmerald" to "Acid Emerald (Green)",
                                        "IceArctic" to "Ice Arctic (Blue)"
                                    )
                                    themeStyles.forEach { (styleKey, styleLabel) ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = styleLabel,
                                                    color = if (themeStyle == styleKey) activeColors.primary else activeColors.textPrimary,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (themeStyle == styleKey) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            onClick = {
                                                viewModel.setThemeStyle(styleKey)
                                                showThemeMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            Image(
                                painter = painterResource(id = R.drawable.img_tool_vip_logo_v2_1785668711300),
                                contentDescription = "Tool Vip Logo",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, activeColors.primary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Hardware HUD
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatCircleGauge(
                            value = "$pingValue ms",
                            label = "Độ Trễ",
                            progress = (120 - pingValue).coerceIn(1, 100) / 100f,
                            activeColor = if (pingValue < 50) activeColors.secondary else activeColors.tertiary,
                            borderColor = activeColors.border,
                            textColor = activeColors.textPrimary,
                            labelColor = activeColors.textSecondary,
                            modifier = Modifier.weight(1f)
                        )

                        StatCircleGauge(
                            value = String.format("%.1f°C", tempValue),
                            label = "Nhiệt Độ",
                            progress = (tempValue - 30).coerceIn(1f, 15f) / 15f,
                            activeColor = if (tempValue < 38) activeColors.primary else Color(0xFFF59E0B),
                            borderColor = activeColors.border,
                            textColor = activeColors.textPrimary,
                            labelColor = activeColors.textSecondary,
                            modifier = Modifier.weight(1f)
                        )

                        StatCircleGauge(
                            value = "$cpuUsage%",
                            label = "Tải CPU",
                            progress = cpuUsage / 100f,
                            activeColor = activeColors.secondary,
                            borderColor = activeColors.border,
                            textColor = activeColors.textPrimary,
                            labelColor = activeColors.textSecondary,
                            modifier = Modifier.weight(1f)
                        )

                        StatCircleGauge(
                            value = "$gpuUsage%",
                            label = "Tải GPU",
                            progress = gpuUsage / 100f,
                            activeColor = activeColors.primary,
                            borderColor = activeColors.border,
                            textColor = activeColors.textPrimary,
                            labelColor = activeColors.textSecondary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(activeColors.border.copy(alpha = 0.3f)))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "CHẾ ĐỘ TĂNG TỐC GAME (4 MỨC ĐỘ CHUYÊN SÂU)",
                        color = activeColors.primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val levels = listOf(
                        1 to "Mức 1\nTiết kiệm",
                        2 to "Mức 2\nCân bằng",
                        3 to "Mức 3\nGiới hạn CS",
                        4 to "Mức 4\nMở giới hạn"
                    )

                    val currentLevel by viewModel.boosterLevel.collectAsState()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        levels.forEach { (levelNum, label) ->
                            val isSelected = currentLevel == levelNum
                            val levelBg = if (isSelected) activeColors.primary else activeColors.border.copy(alpha = 0.15f)
                            val levelBorderColor = if (isSelected) activeColors.primary else activeColors.border.copy(alpha = 0.2f)
                            val levelTextColor = if (isSelected) activeColors.background else activeColors.textPrimary

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(levelBg)
                                    .border(1.dp, levelBorderColor, RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.setBoosterLevel(levelNum)
                                        viewModel.showToast("Đã kích hoạt Chế độ Tăng tốc: ${label.replace("\n", " ")}")
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = levelTextColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(activeColors.secondary.copy(alpha = 0.12f))
                            .border(0.5.dp, activeColors.secondary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Network Opt",
                                tint = activeColors.secondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Bảo vệ ngầm đang chạy: Đang tự dọn dẹp RAM & tối ưu băng thông mạng ngầm liên tục (Tối ưu PIN cực nhẹ máy)",
                                color = activeColors.textPrimary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Central Navigation TabRow inside the dashboard
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color.Transparent,
                contentColor = activeColors.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = activeColors.primary
                    )
                },
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("TỐI ƯU", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.RocketLaunch, null, modifier = Modifier.size(15.dp)) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("BĂNG THÔNG", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Speed, null, modifier = Modifier.size(15.dp)) }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("TRẠNG THÁI AI", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.SmartToy, null, modifier = Modifier.size(15.dp)) }
                )
                Tab(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    text = { Text("BIỂU ĐỒ", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.ShowChart, null, modifier = Modifier.size(15.dp)) }
                )
                Tab(
                    selected = activeTab == 4,
                    onClick = { activeTab = 4 },
                    text = { Text("GIAO DIỆN", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Palette, null, modifier = Modifier.size(15.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab Content Router
            Box(modifier = Modifier.fillMaxWidth()) {
                when (activeTab) {
                    0 -> {
                        // TAB 0: Central Apps List & Optimization Action (Converted to Column for unified scroll)
                        val isGameProfileAiEnabled by viewModel.isGameProfileAiEnabled.collectAsState()
                        val selectedGameProfilePackage by viewModel.selectedGameProfilePackage.collectAsState()
                        val selectedGraphicsProfile by viewModel.selectedGraphicsProfile.collectAsState()
                        val boosterLevel by viewModel.boosterLevel.collectAsState()

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 1. AI-Driven Game Profile Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isGameProfileAiEnabled) activeColors.primary.copy(alpha = 0.08f) else activeColors.cardBg.copy(alpha = if (isDarkMode) cardTransparency else 1.0f)
                                ),
                                shape = RoundedCornerShape(corners),
                                border = BorderStroke(
                                    width = 1.5.dp,
                                    color = if (isGameProfileAiEnabled) activeColors.primary else activeColors.border.copy(alpha = 0.3f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(activeColors.primary.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.SmartToy,
                                                    contentDescription = "AI Icon",
                                                    tint = activeColors.primary,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                            Column {
                                                Text(
                                                    text = "AI GAME PROFILE",
                                                    color = activeColors.primary,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Black,
                                                    letterSpacing = 1.sp
                                                )
                                                Text(
                                                    text = "Tự động cấu hình màn hình & dọn dẹp bộ nhớ đệm",
                                                    color = activeColors.textSecondary,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }

                                        Switch(
                                            checked = isGameProfileAiEnabled,
                                            onCheckedChange = { viewModel.setGameProfileAiEnabled(it) },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = activeColors.primary,
                                                checkedTrackColor = activeColors.primary.copy(alpha = 0.3f),
                                                uncheckedThumbColor = activeColors.textMuted,
                                                uncheckedTrackColor = activeColors.border.copy(alpha = 0.2f)
                                            ),
                                            modifier = Modifier.scale(0.85f)
                                        )
                                    }

                                    if (isGameProfileAiEnabled) {
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(1.dp)
                                                .background(activeColors.border.copy(alpha = 0.2f))
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))

                                        Text(
                                            text = "CHỌN GAME ĐỂ ÁP DỤNG CẤU HÌNH:",
                                            color = activeColors.textPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Simple horizontal game selectors
                                        val gameApps = remember(apps) {
                                            apps.filter { app ->
                                                app.appName.lowercase().contains("pubg") ||
                                                app.appName.lowercase().contains("free fire") ||
                                                app.appName.lowercase().contains("garena") ||
                                                app.appName.lowercase().contains("codm") ||
                                                app.appName.lowercase().contains("game") ||
                                                app.appName.lowercase().contains("strike") ||
                                                app.appName.lowercase().contains("mobile") ||
                                                !app.isSystemApp
                                            }
                                        }

                                        if (gameApps.isEmpty()) {
                                            Text(
                                                text = "Chưa có trò chơi nào được đăng ký. Hãy thêm game thủ công bên dưới.",
                                                color = activeColors.textMuted,
                                                fontSize = 12.sp
                                            )
                                        } else {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState())
                                                    .padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                gameApps.forEach { game ->
                                                    val isSelected = selectedGameProfilePackage == game.packageName
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(10.dp))
                                                            .background(
                                                                if (isSelected) activeColors.primary.copy(alpha = 0.2f)
                                                                else activeColors.border.copy(alpha = 0.1f)
                                                            )
                                                            .border(
                                                                width = 1.2.dp,
                                                                color = if (isSelected) activeColors.primary else activeColors.border.copy(alpha = 0.3f),
                                                                shape = RoundedCornerShape(10.dp)
                                                            )
                                                            .clickable {
                                                                viewModel.setSelectedGameProfilePackage(game.packageName)
                                                                viewModel.showToast("Đã thiết lập AI Game Profile cho ${game.appName}")
                                                            }
                                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.SportsEsports,
                                                                contentDescription = null,
                                                                tint = if (isSelected) activeColors.primary else activeColors.textSecondary,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                            Text(
                                                                text = game.appName,
                                                                color = if (isSelected) activeColors.primary else activeColors.textPrimary,
                                                                fontSize = 13.sp,
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Status indicator showing what the profile will do when launched
                                        val targetGameName = gameApps.find { it.packageName == selectedGameProfilePackage }?.appName ?: "Trò chơi"
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(activeColors.secondary.copy(alpha = 0.08f))
                                                .border(0.5.dp, activeColors.secondary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                .padding(12.dp)
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Icon(Icons.Default.AutoAwesome, null, tint = activeColors.secondary, modifier = Modifier.size(14.dp))
                                                    Text(
                                                        text = "CẤU HÌNH TỰ ĐỘNG CHO $targetGameName:",
                                                        color = activeColors.secondary,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Text(
                                                    text = "• Màn hình hiển thị: Mở khóa đồ họa Extreme 120 FPS Max\n" +
                                                            "• Luồng phần cứng: Kích hoạt Game Boost Mức 4 (Mở Giới Hạn)\n" +
                                                            "• Dọn dẹp cache: Đóng toàn bộ ứng dụng chạy ngầm rác",
                                                    color = activeColors.textPrimary,
                                                    fontSize = 12.sp,
                                                    lineHeight = 17.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Button to launch simulation
                                        Button(
                                            onClick = {
                                                viewModel.activateAiGameProfile(targetGameName)
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = activeColors.secondary,
                                                contentColor = activeColors.background
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "KHỞI CHẠY GAME VỚI AI PROFILE",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 13.sp
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Bật cấu hình AI Game Profile để tự động thiết lập đồ họa cao nhất và dọn dẹp RAM tối đa khi chơi game.",
                                            color = activeColors.textMuted,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }

                            // 2. Search and Quick Filter Box
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = activeColors.cardBg.copy(alpha = if (isDarkMode) cardTransparency else 1.0f)),
                                shape = RoundedCornerShape(corners),
                                border = BorderStroke(1.dp, activeColors.border.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    // Search Text Field
                                    OutlinedTextField(
                                        value = appSearchText,
                                        onValueChange = { appSearchText = it },
                                        placeholder = { Text("Tìm kiếm ứng dụng cần tối ưu...", color = activeColors.textSecondary.copy(alpha = 0.5f), fontSize = 13.sp) },
                                        leadingIcon = { Icon(Icons.Default.Search, "Search", tint = activeColors.primary) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = activeColors.textPrimary,
                                            unfocusedTextColor = activeColors.textPrimary,
                                            focusedBorderColor = activeColors.primary,
                                            unfocusedBorderColor = activeColors.border,
                                            focusedLabelColor = activeColors.primary,
                                            unfocusedLabelColor = activeColors.textSecondary
                                        ),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Category filter chips
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val filterOptions = listOf(
                                            "ALL" to "Tất cả",
                                            "USER" to "Ứng dụng",
                                            "SYSTEM" to "Hệ thống",
                                            "GAMES" to "Trò chơi"
                                        )
                                        filterOptions.forEach { (filterKey, filterName) ->
                                            val isSelected = selectedAppTypeFilter == filterKey
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSelected) activeColors.primary.copy(alpha = 0.15f) else activeColors.textPrimary.copy(alpha = 0.05f))
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (isSelected) activeColors.primary else activeColors.border.copy(alpha = 0.3f),
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable { selectedAppTypeFilter = filterKey }
                                                    .padding(vertical = 10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = filterName,
                                                    color = if (isSelected) activeColors.primary else activeColors.textSecondary,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 3. Trigger Action Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = activeColors.cardBg.copy(alpha = if (isDarkMode) cardTransparency else 1.0f)),
                                shape = RoundedCornerShape(corners),
                                border = BorderStroke(
                                    width = if (glowIntensity == "Strong") 1.5.dp else 1.dp,
                                    color = activeColors.border.copy(alpha = pulseGlowAlpha)
                                )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "TÀI NGUYÊN SẴN SÀNG GIẢI PHÓNG",
                                                color = activeColors.textSecondary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${filteredAppsToOptimize.size} ứng dụng/tiến trình chờ dọn dẹp",
                                                color = activeColors.textPrimary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        // Quick simulated stats
                                        val estimatedGbToClear = filteredAppsToOptimize.sumOf { it.ramUsage } / 1024.0
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(activeColors.tertiary.copy(alpha = 0.15f))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = String.format("%.2f GB RAM", estimatedGbToClear),
                                                color = activeColors.tertiary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = { performMassBoost() },
                                        enabled = !isOptimizing && filteredAppsToOptimize.isNotEmpty(),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .clip(RoundedCornerShape(corners)),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = activeColors.primary,
                                            contentColor = activeColors.background,
                                            disabledContainerColor = activeColors.border.copy(alpha = 0.3f),
                                            disabledContentColor = activeColors.textPrimary.copy(alpha = 0.4f)
                                        )
                                    ) {
                                        if (isOptimizing) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                color = activeColors.primary,
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("ĐANG QUÉT & TỐI ƯU HOÁ...", fontWeight = FontWeight.Bold)
                                        } else {
                                            Icon(Icons.Default.RocketLaunch, "Optimize Icon", modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("TỐI ƯU HÀNG LOẠT KHẨN CẤP", fontWeight = FontWeight.Black, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }

                            // 4. Application List section title
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "DANH SÁCH ỨNG DỤNG HỆ THỐNG (${filteredAppsToOptimize.size})",
                                    color = activeColors.textSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )

                                if (selectedAppTypeFilter == "GAMES") {
                                    TextButton(
                                        onClick = { showAddGameDialog = true },
                                        colors = ButtonDefaults.textButtonColors(contentColor = activeColors.primary)
                                    ) {
                                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Thêm Game", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            if (filteredAppsToOptimize.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .clip(RoundedCornerShape(corners))
                                        .background(activeColors.cardBg.copy(alpha = if (isDarkMode) cardTransparency else 1.0f))
                                        .border(1.dp, activeColors.border.copy(alpha = 0.2f), RoundedCornerShape(corners))
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.SearchOff, "No app found", tint = activeColors.textMuted, modifier = Modifier.size(36.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Không tìm thấy ứng dụng phù hợp", color = activeColors.textSecondary, fontSize = 13.sp)
                                    }
                                }
                            } else {
                                filteredAppsToOptimize.forEach { app ->
                                    var appIsOptimizedLocal by remember { mutableStateOf(false) }
                                    var isLocalScanningApp by remember { mutableStateOf(false) }

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = activeColors.cardBg.copy(alpha = if (isDarkMode) cardTransparency else 1.0f)),
                                        shape = RoundedCornerShape(corners),
                                        border = BorderStroke(1.dp, activeColors.border.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1.5f)
                                            ) {
                                                // Decorative Circular App Icon Badge
                                                Box(
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(
                                                            if (appIsOptimizedLocal) activeColors.secondary.copy(alpha = 0.15f)
                                                            else activeColors.primary.copy(alpha = 0.15f)
                                                        )
                                                        .border(
                                                            1.dp,
                                                            if (appIsOptimizedLocal) activeColors.secondary.copy(alpha = 0.3f)
                                                            else activeColors.primary.copy(alpha = 0.3f),
                                                            RoundedCornerShape(10.dp)
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = when {
                                                            selectedAppTypeFilter == "GAMES" || app.appName.lowercase().contains("game") -> Icons.Default.SportsEsports
                                                            app.isSystemApp -> Icons.Default.SettingsSystemDaydream
                                                            else -> Icons.Default.AppShortcut
                                                        },
                                                        contentDescription = "App Icon",
                                                        tint = if (appIsOptimizedLocal) activeColors.secondary else activeColors.primary,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(12.dp))

                                                Column {
                                                    Text(
                                                        text = app.appName,
                                                        color = activeColors.textPrimary,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = app.packageName,
                                                        color = activeColors.textSecondary,
                                                        fontSize = 11.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(top = 2.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(6.dp)
                                                                .clip(RoundedCornerShape(100.dp))
                                                                .background(if (appIsOptimizedLocal) activeColors.secondary else activeColors.tertiary)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = if (appIsOptimizedLocal) "Đã giải phóng" else "Đang ngốn ${(app.ramUsage).toInt()} MB RAM",
                                                            color = if (appIsOptimizedLocal) activeColors.secondary else activeColors.textSecondary,
                                                            fontSize = 11.sp
                                                        )
                                                    }
                                                }
                                            }

                                            // Individual optimize trigger button
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (appIsOptimizedLocal) activeColors.secondary.copy(alpha = 0.1f)
                                                        else activeColors.primary.copy(alpha = 0.1f)
                                                    )
                                                    .border(
                                                        1.dp,
                                                        if (appIsOptimizedLocal) activeColors.secondary.copy(alpha = 0.3f)
                                                        else activeColors.primary.copy(alpha = 0.3f),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable(enabled = !isLocalScanningApp && !appIsOptimizedLocal) {
                                                        coroutineScope.launch {
                                                            isLocalScanningApp = true
                                                            delay(1200)
                                                            appIsOptimizedLocal = true
                                                            isLocalScanningApp = false
                                                            viewModel.showToast("Đã tối ưu và giải phóng ${(app.ramUsage).toInt()} MB RAM của ${app.appName} thành công!")
                                                        }
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isLocalScanningApp) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(12.dp),
                                                        color = activeColors.primary,
                                                        strokeWidth = 1.5.dp
                                                    )
                                                } else {
                                                    Text(
                                                        text = if (appIsOptimizedLocal) "ĐÃ DỌN" else "TỐI ƯU",
                                                        color = if (appIsOptimizedLocal) activeColors.secondary else activeColors.primary,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // TAB 1: Đo Băng Thông & Tốc Độ Mạng (Bandwidth Speed Test)
                        val speedTestPhase by viewModel.speedTestPhase.collectAsState()
                        val speedTestProgress by viewModel.speedTestProgress.collectAsState()
                        val speedTestDownload by viewModel.speedTestDownload.collectAsState()
                        val speedTestUpload by viewModel.speedTestUpload.collectAsState()
                        val speedTestPing by viewModel.speedTestPing.collectAsState()
                        val speedTestJitter by viewModel.speedTestJitter.collectAsState()
                        val speedTestServer by viewModel.speedTestServer.collectAsState()

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = activeColors.cardBg.copy(alpha = cardTransparency)),
                                shape = RoundedCornerShape(corners),
                                border = BorderStroke(1.dp, activeColors.border.copy(alpha = 0.4f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "ĐO BĂNG THÔNG MẠNG CHUYÊN SÂU",
                                        color = activeColors.primary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = "Kiểm tra tốc độ download, upload & ping thời gian thực tối ưu kết nối game",
                                        color = activeColors.textSecondary,
                                        fontSize = 10.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )

                                    Spacer(modifier = Modifier.height(24.dp))

                                    // Central Speed Dial Gauge
                                    Box(
                                        modifier = Modifier.size(180.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val strokeWidth = 10.dp.toPx()
                                            // Base track
                                            drawArc(
                                                color = activeColors.border.copy(alpha = 0.2f),
                                                startAngle = 135f,
                                                sweepAngle = 270f,
                                                useCenter = false,
                                                style = Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                            )
                                            
                                            // Active fill based on current phase progress
                                            val sweepProgress = when (speedTestPhase) {
                                                "Ping" -> speedTestProgress * 0.2f
                                                "Download" -> 0.2f + (speedTestDownload / 400f).coerceIn(0f, 1f) * 0.4f
                                                "Upload" -> 0.6f + (speedTestUpload / 200f).coerceIn(0f, 1f) * 0.4f
                                                "Done" -> 1f
                                                else -> 0f
                                            }
                                            
                                            drawArc(
                                                brush = Brush.sweepGradient(
                                                    listOf(activeColors.primary, activeColors.secondary, activeColors.primary)
                                                ),
                                                startAngle = 135f,
                                                sweepAngle = 270f * sweepProgress,
                                                useCenter = false,
                                                style = Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                            )
                                        }

                                        // Central display values
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            val speedValue = if (speedTestPhase == "Upload") speedTestUpload else if (speedTestPhase == "Download" || speedTestPhase == "Done") speedTestDownload else 0f
                                            Text(
                                                text = String.format("%.1f", speedValue),
                                                color = activeColors.textPrimary,
                                                fontSize = 32.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                            Text(
                                                text = "Mbps",
                                                color = activeColors.secondary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(activeColors.primary.copy(alpha = 0.15f))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = when (speedTestPhase) {
                                                        "Ping" -> "ĐANG ĐO PING..."
                                                        "Download" -> "ĐO DOWNLOAD..."
                                                        "Upload" -> "ĐO UPLOAD..."
                                                        "Done" -> "HOÀN TẤT"
                                                        else -> "SẴN SÀNG"
                                                    },
                                                    color = activeColors.primary,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Stats Row (Ping, Jitter, Download, Upload)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Ping", color = activeColors.textSecondary, fontSize = 10.sp)
                                            Text(
                                                text = if (speedTestPing > 0) "$speedTestPing ms" else "--",
                                                color = if (speedTestPing < 30 && speedTestPing > 0) activeColors.secondary else activeColors.textPrimary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(activeColors.border.copy(alpha = 0.3f)))
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Jitter", color = activeColors.textSecondary, fontSize = 10.sp)
                                            Text(
                                                text = if (speedTestJitter > 0) "$speedTestJitter ms" else "--",
                                                color = activeColors.textPrimary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(activeColors.border.copy(alpha = 0.3f)))
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Tải Về", color = activeColors.textSecondary, fontSize = 10.sp)
                                            Text(
                                                text = if (speedTestDownload > 0f) String.format("%.1f Mbps", speedTestDownload) else "--",
                                                color = activeColors.primary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(activeColors.border.copy(alpha = 0.3f)))
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Tải Lên", color = activeColors.textSecondary, fontSize = 10.sp)
                                            Text(
                                                text = if (speedTestUpload > 0f) String.format("%.1f Mbps", speedTestUpload) else "--",
                                                color = activeColors.secondary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    if (speedTestPhase != "Idle" && speedTestPhase != "Done") {
                                        Spacer(modifier = Modifier.height(18.dp))
                                        LinearProgressIndicator(
                                            progress = { speedTestProgress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = activeColors.primary,
                                            trackColor = activeColors.border.copy(alpha = 0.2f),
                                        )
                                    }
                                }
                            }

                            // Servers Selector Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = activeColors.cardBg.copy(alpha = cardTransparency)),
                                shape = RoundedCornerShape(corners),
                                border = BorderStroke(1.dp, activeColors.border.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "CHỌN MÁY CHỦ THỬ NGHIỆM",
                                        color = activeColors.primary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    val servers = listOf(
                                        "Hà Nội - FPT Telecom",
                                        "Hồ Chí Minh - Viettel IDC",
                                        "Đà Nẵng - VNPT Network",
                                        "Singapore - AWS Cloud"
                                    )

                                    servers.forEach { srv ->
                                        val isSelected = speedTestServer == srv
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) activeColors.primary.copy(alpha = 0.1f) else Color.Transparent)
                                                .clickable(enabled = speedTestPhase == "Idle" || speedTestPhase == "Done") {
                                                    viewModel.setSpeedTestServer(srv)
                                                }
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Dns,
                                                    contentDescription = null,
                                                    tint = if (isSelected) activeColors.primary else TextGray,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = srv,
                                                    color = if (isSelected) activeColors.textPrimary else activeColors.textSecondary,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = activeColors.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = { viewModel.runSpeedTest() },
                                enabled = speedTestPhase == "Idle" || speedTestPhase == "Done",
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = activeColors.primary,
                                    disabledContainerColor = activeColors.primary.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = activeColors.background,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (speedTestPhase == "Idle" || speedTestPhase == "Done") "BẮT ĐẦU ĐO TỐC ĐỘ" else "ĐANG ĐO...",
                                        color = activeColors.background,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            // Network evaluation banner if done
                            if (speedTestPhase == "Done") {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = GlowGreen.copy(alpha = 0.12f)),
                                    shape = RoundedCornerShape(corners),
                                    border = BorderStroke(1.dp, GlowGreen.copy(alpha = 0.4f))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = GlowGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "MẠNG ĐẠT TIÊU CHUẨN VIP ESPORT",
                                                color = GlowGreen,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Hệ thống phân tích: Kết nối cực kỳ ổn định. Ping thấp (${speedTestPing}ms) thích hợp cho mọi tựa game MOBA, FPS đòi hỏi phản xạ cao mà không lo trễ nhịp.",
                                            color = activeColors.textPrimary,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // TAB 2: Bảng Trạng Thái AI (AI Status Board)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            
                            // Top circular score gauges
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = activeColors.cardBg.copy(alpha = cardTransparency)),
                                shape = RoundedCornerShape(corners),
                                border = BorderStroke(
                                    width = if (glowIntensity == "Strong") 1.5.dp else 1.dp,
                                    color = activeColors.border.copy(alpha = pulseGlowAlpha)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "CHỈ SỐ TỐI ƯU HOÁ THẦN KINH AI",
                                        color = activeColors.textSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Big pulsating AI score circle
                                    Box(
                                        modifier = Modifier.size(130.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Glowing outer boundary
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            drawCircle(
                                                color = activeColors.primary.copy(alpha = 0.05f),
                                                radius = size.minDimension / 2
                                            )
                                            drawArc(
                                                color = activeColors.primary.copy(alpha = 0.2f),
                                                startAngle = 0f,
                                                sweepAngle = 360f,
                                                useCenter = false,
                                                style = Stroke(width = 8.dp.toPx())
                                            )
                                            drawArc(
                                                color = activeColors.primary,
                                                startAngle = -90f,
                                                sweepAngle = 3.6f * aiStatusScore,
                                                useCenter = false,
                                                style = Stroke(width = 8.dp.toPx())
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$aiStatusScore%",
                                                color = activeColors.textPrimary,
                                                fontSize = 32.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                            Text(
                                                text = "CHỈ SỐ SẠCH",
                                                color = activeColors.primary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text(
                                        text = "Trạng thái tối ưu hóa AI đạt chất lượng cao. Khuyên dùng quét định kỳ.",
                                        color = activeColors.textSecondary,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            // AI Assistant Recommendations
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = activeColors.cardBg.copy(alpha = cardTransparency)),
                                shape = RoundedCornerShape(corners),
                                border = BorderStroke(1.dp, activeColors.border.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.SmartToy, null, tint = activeColors.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "AI KHUYẾN NGHỊ THỜI GIAN THỰC",
                                            color = activeColors.textPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = aiRecommendation,
                                        color = activeColors.textSecondary,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            // AI Scanning Terminal Logs
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.85f)),
                                shape = RoundedCornerShape(corners),
                                border = BorderStroke(1.dp, activeColors.border.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "AI NEURAL ENGINE LOGS",
                                            color = activeColors.primary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )

                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(RoundedCornerShape(100.dp))
                                                .background(if (isAiScanning) activeColors.tertiary else activeColors.secondary)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                            .verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        aiScanLogs.forEach { log ->
                                            Text(
                                                text = "> $log",
                                                color = if (log.contains("✅") || log.contains("đối tác")) activeColors.secondary else activeColors.textSecondary,
                                                fontSize = 11.sp,
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }

                            // Trigger scan button
                            Button(
                                onClick = { viewModel.runNeuralCoreScan() },
                                enabled = !isAiScanning,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(corners)),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = activeColors.primary,
                                    contentColor = Color(0xFF040D12)
                                )
                            ) {
                                if (isAiScanning) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color(0xFF040D12),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("ĐANG QUÉT MẠNG THẦN KINH...", fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.WifiTethering, null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("CHẠY PHÂN TÍCH LÕI AI", fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }

                    3 -> {
                        // TAB 3: Biểu Đồ Hệ Thống & Chế Độ Game Thủ
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Header Information
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = activeColors.cardBg.copy(alpha = cardTransparency)),
                                shape = RoundedCornerShape(corners),
                                border = BorderStroke(1.dp, activeColors.border.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(activeColors.primary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Timeline,
                                            contentDescription = null,
                                            tint = activeColors.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "BẢNG ĐIỀU KHIỂN CHIẾN THUẬT",
                                            color = activeColors.textPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = "Giám sát tài nguyên phần cứng và kích hoạt cấu hình Extreme",
                                            color = activeColors.textSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            // 1. Extreme Gamer Mode Switch Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isExtremeGamerModeEnabled) activeColors.primary.copy(alpha = 0.12f)
                                    else activeColors.cardBg.copy(alpha = cardTransparency)
                                ),
                                shape = RoundedCornerShape(corners),
                                border = BorderStroke(
                                    width = if (isExtremeGamerModeEnabled) 1.5.dp else 1.dp,
                                    color = if (isExtremeGamerModeEnabled) activeColors.primary else activeColors.border.copy(alpha = 0.3f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.SportsEsports,
                                                contentDescription = null,
                                                tint = if (isExtremeGamerModeEnabled) activeColors.primary else activeColors.textSecondary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = "CHẾ ĐỘ GAME THỦ CỰC HẠN",
                                                    color = if (isExtremeGamerModeEnabled) activeColors.primary else activeColors.textPrimary,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Black,
                                                    letterSpacing = 0.5.sp
                                                )
                                                Text(
                                                    text = "Tập trung ép xung hiệu năng & ổn định đường truyền",
                                                    color = activeColors.textSecondary,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }

                                        Switch(
                                            checked = isExtremeGamerModeEnabled,
                                            onCheckedChange = { isExtremeGamerModeEnabled = it },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = activeColors.primary,
                                                checkedTrackColor = activeColors.primary.copy(alpha = 0.3f),
                                                uncheckedThumbColor = activeColors.textMuted,
                                                uncheckedTrackColor = activeColors.border.copy(alpha = 0.2f)
                                            ),
                                            modifier = Modifier.scale(0.85f)
                                        )
                                    }
                                }
                            }

                            // 2. Automated AI Optimization Switch Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isAutoAiOptimizingEnabled) activeColors.secondary.copy(alpha = 0.08f)
                                    else activeColors.cardBg.copy(alpha = cardTransparency)
                                ),
                                shape = RoundedCornerShape(corners),
                                border = BorderStroke(
                                    width = if (isAutoAiOptimizingEnabled) 1.5.dp else 1.dp,
                                    color = if (isAutoAiOptimizingEnabled) activeColors.secondary else activeColors.border.copy(alpha = 0.3f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Memory,
                                                contentDescription = null,
                                                tint = if (isAutoAiOptimizingEnabled) activeColors.secondary else activeColors.textSecondary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = "TỰ ĐỘNG TỐI ƯU AI REAL-TIME",
                                                    color = if (isAutoAiOptimizingEnabled) activeColors.secondary else activeColors.textPrimary,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Black,
                                                    letterSpacing = 0.5.sp
                                                )
                                                Text(
                                                    text = "Tự dọn dẹp RAM ngầm khi CPU > 85% hoặc RAM > 80%",
                                                    color = activeColors.textSecondary,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }

                                        Switch(
                                            checked = isAutoAiOptimizingEnabled,
                                            onCheckedChange = { isAutoAiOptimizingEnabled = it },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = activeColors.secondary,
                                                checkedTrackColor = activeColors.secondary.copy(alpha = 0.3f),
                                                uncheckedThumbColor = activeColors.textMuted,
                                                uncheckedTrackColor = activeColors.border.copy(alpha = 0.2f)
                                            ),
                                            modifier = Modifier.scale(0.85f)
                                        )
                                    }
                                }
                            }

                            // 3. Real-time CPU Usage Line Graph Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = activeColors.cardBg.copy(alpha = cardTransparency)),
                                shape = RoundedCornerShape(corners),
                                border = BorderStroke(1.dp, activeColors.border.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    CanvasLineChart(
                                        dataPoints = cpuHistory,
                                        label = "MỨC SỬ DỤNG LÕI CPU REAL-TIME",
                                        value = "$cpuUsage%",
                                        lineColor = activeColors.primary,
                                        gridColor = activeColors.border,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            // 4. Real-time RAM Usage Line Graph Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = activeColors.cardBg.copy(alpha = cardTransparency)),
                                shape = RoundedCornerShape(corners),
                                border = BorderStroke(1.dp, activeColors.border.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    CanvasLineChart(
                                        dataPoints = ramHistory,
                                        label = "TỈ LỆ TIÊU THỤ RAM REAL-TIME",
                                        value = String.format("%.0f%%", lastRamValue),
                                        lineColor = activeColors.secondary,
                                        gridColor = activeColors.border,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    4 -> {
                        // TAB 4: Tùy Chỉnh Giao Diện (Theme Customization)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            
                            // Preset style selector cards
                            Column {
                                Text(
                                    text = "CHỌN GIAO DIỆN CHỦ ĐỀ CHUYÊN NGHIỆP",
                                    color = activeColors.textSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    modifier = Modifier.padding(bottom = 10.dp)
                                )

                                val themes = listOf(
                                    "DeepObsidian" to ("Slate Obsidian (Teal Neon)" to Color(0xFF18E2C2)),
                                    "CyberpunkAurora" to ("Cyberpunk Aurora (Magenta Glow)" to Color(0xFFFF007F)),
                                    "NeonInferno" to ("Neon Inferno (Crimson Heat)" to Color(0xFFFF3F1A)),
                                    "AcidEmerald" to ("Acid Emerald (Toxic Radioactive)" to Color(0xFF26FF4B)),
                                    "IceArctic" to ("Ice Arctic (Glacial Cool)" to Color(0xFF64E5FF))
                                )

                                themes.forEach { (themeKey, themeData) ->
                                    val (themeName, themeAccent) = themeData
                                    val isSelected = themeStyle == themeKey

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable { viewModel.setThemeStyle(themeKey) },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) activeColors.primary.copy(alpha = 0.12f)
                                            else activeColors.cardBg.copy(alpha = cardTransparency)
                                        ),
                                        shape = RoundedCornerShape(corners),
                                        border = BorderStroke(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) activeColors.primary else activeColors.border.copy(alpha = 0.3f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .clip(RoundedCornerShape(100.dp))
                                                        .background(themeAccent)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = themeName,
                                                    color = activeColors.textPrimary,
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                )
                                            }

                                            if (isSelected) {
                                                Icon(Icons.Default.CheckCircle, "Selected", tint = activeColors.primary)
                                            }
                                        }
                                    }
                                }
                            }

                            // Card Transparency setting
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = activeColors.cardBg.copy(alpha = cardTransparency)),
                                shape = RoundedCornerShape(corners),
                                border = BorderStroke(1.dp, activeColors.border.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = String.format("Độ mờ đục của Card (Acrylic Opacity): %.0f%%", cardTransparency * 100),
                                        color = activeColors.textPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Điều chỉnh độ trong suốt của hộp chứa để tạo hiệu ứng phủ kính (glassmorphism) đẳng cấp cao.",
                                        color = activeColors.textMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Slider(
                                        value = cardTransparency,
                                        onValueChange = { viewModel.setCardTransparency(it) },
                                        valueRange = 0.4f..1.0f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = activeColors.primary,
                                            activeTrackColor = activeColors.primary,
                                            inactiveTrackColor = activeColors.border
                                        )
                                    )
                                }
                            }

                            // Card Corner Roundness setting
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = activeColors.cardBg.copy(alpha = cardTransparency)),
                                shape = RoundedCornerShape(corners),
                                border = BorderStroke(1.dp, activeColors.border.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Bo tròn góc phần cứng: ${cardCornerRadius}dp",
                                        color = activeColors.textPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Tùy chỉnh góc bo của các khung card từ sắc cạnh tối giản đến bo mềm dễ thương.",
                                        color = activeColors.textMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Slider(
                                        value = cardCornerRadius.toFloat(),
                                        onValueChange = { viewModel.setCardCornerRadius(it.toInt()) },
                                        valueRange = 4f..24f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = activeColors.primary,
                                            activeTrackColor = activeColors.primary,
                                            inactiveTrackColor = activeColors.border
                                        )
                                    )
                                }
                            }

                            // Card border glow selection
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = activeColors.cardBg.copy(alpha = cardTransparency)),
                                shape = RoundedCornerShape(corners),
                                border = BorderStroke(1.dp, activeColors.border.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Cường độ phát quang viền (Neon Border Glow)",
                                        color = activeColors.textPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    val glowOptions = listOf("None" to "Tắt viền", "Normal" to "Vừa", "Strong" to "Cực mạnh (VIP)")
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        glowOptions.forEach { (glowKey, glowName) ->
                                            val isSelected = glowIntensity == glowKey
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSelected) activeColors.primary.copy(alpha = 0.15f) else activeColors.border.copy(alpha = 0.15f))
                                                    .border(
                                                        1.dp,
                                                        if (isSelected) activeColors.primary else activeColors.border.copy(alpha = 0.3f),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable { viewModel.setGlowIntensity(glowKey) }
                                                    .padding(vertical = 10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = glowName,
                                                    color = if (isSelected) activeColors.primary else activeColors.textSecondary,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Dynamic pulse animation switch
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = activeColors.cardBg.copy(alpha = cardTransparency)),
                                shape = RoundedCornerShape(corners),
                                border = BorderStroke(1.dp, activeColors.border.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Hiệu ứng viền xung điện động",
                                            color = activeColors.textPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "Kích hoạt hiệu ứng phát sáng viền nhấp nháy tuần hoàn cực đẹp mắt thời gian thực.",
                                            color = activeColors.textMuted,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }

                                    Switch(
                                        checked = dynamicPulseEnabled,
                                        onCheckedChange = { viewModel.setDynamicPulseEnabled(it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = activeColors.background,
                                            checkedTrackColor = activeColors.primary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Full-screen overlay optimization animation
        if (isOptimizing) {
            Dialog(onDismissRequest = {}) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(activeColors.background)
                        .border(
                            width = 1.5.dp,
                            color = activeColors.primary,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = activeColors.primary,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(56.dp)
                        )
                        
                        Text(
                            text = "LÕI GAME BOOST ĐANG KÍCH HOẠT",
                            color = activeColors.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = optMessage,
                            color = activeColors.textPrimary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "Đang tinh chỉnh luồng xử lý phần cứng cực hạn...",
                            color = activeColors.textSecondary,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        // Optimization Result Success Modal
        if (showOptimizationResult) {
            Dialog(onDismissRequest = { showOptimizationResult = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(activeColors.background)
                        .border(1.dp, activeColors.secondary.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(activeColors.secondary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Bolt, "Lightning icon", tint = activeColors.secondary, modifier = Modifier.size(36.dp))
                        }

                        Text(
                            text = "TỐI ƯU HOÀN TẤT!",
                            color = activeColors.secondary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "Đã dọn dẹp bộ nhớ đệm, thu hồi lượng RAM trống dồi dào, đóng $lastOptimizedAppsCount tiến trình không cần thiết và trả lại độ phản hồi mượt mà.",
                            color = activeColors.textSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(activeColors.cardBg.copy(alpha = 0.4f))
                                .padding(12.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("RAM Giải Phóng", fontSize = 11.sp, color = activeColors.textMuted)
                                Text(
                                    text = String.format("%.1f GB", lastOptimizedRamAmount / 1024.0),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = activeColors.primary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(30.dp)
                                    .background(activeColors.border.copy(alpha = 0.5f))
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("FPS Tăng Cường", fontSize = 11.sp, color = activeColors.textMuted)
                                Text(
                                    text = "+25% FPS",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = activeColors.secondary
                                )
                            }
                        }

                        Button(
                            onClick = { showOptimizationResult = false },
                            colors = ButtonDefaults.buttonColors(containerColor = activeColors.secondary, contentColor = activeColors.background),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                        ) {
                            Text("ĐỒNG Ý", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Dialog for Adding Custom Game manually
        if (showAddGameDialog) {
            var customAppName by remember { mutableStateOf("") }
            var customPackageName by remember { mutableStateOf("") }
            var appNameError by remember { mutableStateOf(false) }
            var packageError by remember { mutableStateOf(false) }

            Dialog(onDismissRequest = { showAddGameDialog = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(activeColors.background)
                        .border(1.dp, activeColors.border, RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "THÊM GAME THỦ CÔNG",
                            color = activeColors.textPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )

                        Text(
                            text = "Đăng ký thêm trò chơi của bạn để Game Booster tự động cấu hình và kích hoạt luồng ưu tiên độc quyền.",
                            color = activeColors.textSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        OutlinedTextField(
                            value = customAppName,
                            onValueChange = {
                                customAppName = it
                                appNameError = false
                            },
                            label = { Text("Tên Trò Chơi", color = activeColors.primary) },
                            placeholder = { Text("Ví dụ: Genshin Impact", color = activeColors.textMuted) },
                            isError = appNameError,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = activeColors.textPrimary,
                                unfocusedTextColor = activeColors.textPrimary,
                                focusedBorderColor = activeColors.primary,
                                unfocusedBorderColor = activeColors.border
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = customPackageName,
                            onValueChange = {
                                customPackageName = it
                                packageError = false
                            },
                            label = { Text("Package Name", color = activeColors.primary) },
                            placeholder = { Text("Ví dụ: com.mihoyo.genshin", color = activeColors.textMuted) },
                            isError = packageError,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = activeColors.textPrimary,
                                unfocusedTextColor = activeColors.textPrimary,
                                focusedBorderColor = activeColors.primary,
                                unfocusedBorderColor = activeColors.border
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TextButton(
                                onClick = { showAddGameDialog = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("HỦY BỎ", color = activeColors.textSecondary, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    if (customAppName.isBlank()) appNameError = true
                                    if (customPackageName.isBlank()) packageError = true

                                    if (customAppName.isNotBlank() && customPackageName.isNotBlank()) {
                                        viewModel.addNewCustomGame(customAppName.trim(), customPackageName.trim())
                                        showAddGameDialog = false
                                        viewModel.showToast("Đã thêm game '${customAppName}' thành công!")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = activeColors.primary, contentColor = activeColors.background),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("THÊM GAME", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button for Emergency Mass Optimization
        ExtendedFloatingActionButton(
            onClick = { performMassBoost() },
            containerColor = activeColors.primary,
            contentColor = activeColors.background,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .navigationBarsPadding(),
            icon = {
                Icon(
                    imageVector = Icons.Default.RocketLaunch,
                    contentDescription = "Optimize All Icon",
                    modifier = Modifier.size(20.dp)
                )
            },
            text = {
                Text(
                    text = "Tối ưu tất cả",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            }
        )
    }
}

@Composable
fun StatCircleGauge(
    value: String,
    label: String,
    progress: Float,
    activeColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    labelColor: Color = Color.White.copy(alpha = 0.6f)
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(64.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 5.dp.toPx()
                // Base background arc
                drawArc(
                    color = borderColor.copy(alpha = 0.3f),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )
                // Active arc representing value
                drawArc(
                    color = activeColor,
                    startAngle = 135f,
                    sweepAngle = 270f * progress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )
            }
            Text(
                text = value,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = labelColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CanvasLineChart(
    dataPoints: List<Float>,
    label: String,
    value: String,
    lineColor: Color,
    gridColor: Color,
    maxVal: Float = 100f,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = value,
                color = lineColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                .border(0.5.dp, gridColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // Draw background grid lines (horizontal)
                val gridLinesCount = 4
                for (i in 0..gridLinesCount) {
                    val y = height * (i / gridLinesCount.toFloat())
                    drawLine(
                        color = gridColor.copy(alpha = 0.08f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw vertical grid lines
                val vertLinesCount = 6
                for (i in 0..vertLinesCount) {
                    val x = width * (i / vertLinesCount.toFloat())
                    drawLine(
                        color = gridColor.copy(alpha = 0.08f),
                        start = Offset(x, 0f),
                        end = Offset(x, height),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                if (dataPoints.size > 1) {
                    val path = Path()
                    val stepX = width / (dataPoints.size - 1)

                    dataPoints.forEachIndexed { index, value ->
                        val x = index * stepX
                        // Invert Y because (0,0) is top-left in Canvas
                        val normalizedY = (value / maxVal).coerceIn(0f, 1f)
                        val y = height - (normalizedY * height)

                        if (index == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                    }

                    // Draw the line path with neon glow
                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )

                    // Draw semi-transparent gradient under the line
                    val fillPath = Path().apply {
                        addPath(path)
                        lineTo(width, height)
                        lineTo(0f, height)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                lineColor.copy(alpha = 0.22f),
                                lineColor.copy(alpha = 0.0f)
                            )
                        )
                    )

                    // Draw the latest point as a glowing circle
                    val lastIndex = dataPoints.size - 1
                    val lastX = lastIndex * stepX
                    val lastY = height - ((dataPoints.last() / maxVal).coerceIn(0f, 1f) * height)
                    
                    drawCircle(
                        color = lineColor.copy(alpha = 0.35f),
                        radius = 6.dp.toPx(),
                        center = Offset(lastX, lastY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.5.dp.toPx(),
                        center = Offset(lastX, lastY)
                    )
                }
            }
        }
    }
}
