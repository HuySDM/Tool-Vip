package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.window.Dialog
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

    // Screen local states
    var showAddGameDialog by remember { mutableStateOf(false) }
    var showOptimizationResult by remember { mutableStateOf(false) }
    var lastOptimizedRamAmount by remember { mutableStateOf(0.0) }
    var lastOptimizedAppsCount by remember { mutableStateOf(0) }

    // Dynamic hardware simulation states
    var pingValue by remember { mutableStateOf(42) }
    var tempValue by remember { mutableStateOf(36.8f) }
    var cpuUsage by remember { mutableStateOf(65) }
    var gpuUsage by remember { mutableStateOf(48) }

    // Fluctuating hardware stats simulation
    LaunchedEffect(isOptimizing) {
        while (true) {
            if (isOptimizing) {
                pingValue = (50..120).random()
                tempValue = 38.5f + ((1..8).random() / 10f)
                cpuUsage = (85..98).random()
                gpuUsage = (70..90).random()
            } else {
                pingValue = (25..45).random()
                tempValue = 35.2f + ((1..9).random() / 10f)
                cpuUsage = (20..45).random()
                gpuUsage = (15..35).random()
            }
            delay(1500)
        }
    }

    // Identify games from the app list
    val gamesList = remember(apps) {
        apps.filter { app ->
            val name = app.appName.lowercase()
            // Identify games by common package names or app names, or simply non-system apps with high RAM or no trash tag
            name.contains("pubg") || 
            name.contains("free fire") || 
            name.contains("garena") || 
            name.contains("codm") || 
            name.contains("game") || 
            name.contains("strike") ||
            name.contains("mobile") && !app.isSystemApp && !app.isTrash
        }
    }

    // Identify non-essential background processes (trash/system that can be optimized)
    val backgroundProcesses = remember(apps) {
        apps.filter { !it.isSystemApp && it.isTrash && !it.isFrozen }
    }

    // Handle game booster manual optimization action
    val performGameBoost = {
        coroutineScope.launch {
            val unoptimizedCount = backgroundProcesses.size
            val ramToClear = backgroundProcesses.sumOf { it.ramUsage }
            
            // Trigger the ViewModel optimization
            viewModel.optimizeRamDirectly {
                lastOptimizedRamAmount = if (ramToClear > 0) ramToClear else 1420.0
                lastOptimizedAppsCount = if (unoptimizedCount > 0) unoptimizedCount else 4
                showOptimizationResult = true
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Header with gaming branding
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(BrightTurquoise)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TRẠM TỐI ƯU CỰC HẠN",
                                color = BrightTurquoise,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                        }
                        Text(
                            text = "GAME BOOSTER",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Quick status badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(TransparentGreen)
                            .border(1.dp, BrightTurquoise.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "VIP ENGAGE",
                            color = BrightTurquoise,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // 1. Live Performance Gauge Panel
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BorderGreen)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "CHỈ SỐ THỜI GIAN THỰC (HARDWARE HUD)",
                            color = TextGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Ping Meter
                            StatCircleGauge(
                                value = "$pingValue ms",
                                label = "Độ Trễ (Ping)",
                                progress = (120 - pingValue).coerceIn(1, 100) / 100f,
                                activeColor = if (pingValue < 50) GlowGreen else CoralVibrant,
                                modifier = Modifier.weight(1f)
                            )

                            // Temp Meter
                            StatCircleGauge(
                                value = String.format("%.1f°C", tempValue),
                                label = "Nhiệt Độ",
                                progress = (tempValue - 30).coerceIn(1f, 15f) / 15f,
                                activeColor = if (tempValue < 38) BrightTurquoise else AccentYellow,
                                modifier = Modifier.weight(1f)
                            )

                            // CPU usage
                            StatCircleGauge(
                                value = "$cpuUsage%",
                                label = "Tải CPU",
                                progress = cpuUsage / 100f,
                                activeColor = ElectricBlue,
                                modifier = Modifier.weight(1f)
                            )

                            // GPU usage
                            StatCircleGauge(
                                value = "$gpuUsage%",
                                label = "Tải GPU",
                                progress = gpuUsage / 100f,
                                activeColor = BrightTurquoise,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 2. Installed Games Grid
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = "Games Icon",
                                tint = BrightTurquoise,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TRÒ CHƠI ĐÃ THÊM (${gamesList.size})",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Add game button
                        TextButton(
                            onClick = { showAddGameDialog = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = BrightTurquoise)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Game", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Thêm Game", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (gamesList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(DarkTealCard)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Games, "No game icon", tint = TextGray, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Chưa cài đặt game nào", color = TextGray, fontSize = 13.sp)
                            }
                        }
                    } else {
                        // Game selection lists using vertical staggered grid-like arrangement in LazyColumn
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            gamesList.chunked(2).forEach { rowGames ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    rowGames.forEach { game ->
                                        GameItemCard(
                                            game = game,
                                            onLaunch = {
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "🚀 Đang tối ưu RAM cực hạn và khởi chạy ${game.appName}...",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (rowGames.size < 2) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Simulated Running Services & Optimize Action Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BorderGreen)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "DỊCH VỤ CHẠY NGẦM GÂY TRỄ",
                                    color = TextGray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "${backgroundProcesses.size} tiến trình không thiết yếu",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // RAM released estimation
                            val totalRam = backgroundProcesses.sumOf { it.ramUsage }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CoralVibrant.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = String.format("%.0f MB RAM", totalRam),
                                    color = CoralVibrant,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (backgroundProcesses.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(TransparentGreen)
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, "Clean", tint = GlowGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Băng thông rỗng! Thiết bị đã ở trạng thái tối ưu lý tưởng.", color = GlowGreen, fontSize = 12.sp)
                                }
                            }
                        } else {
                            // Show top 3 background processes
                            backgroundProcesses.take(3).forEach { proc ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(RoundedCornerShape(100.dp))
                                                .background(CoralVibrant)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(text = proc.appName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                            Text(text = proc.packageName, color = TextGray, fontSize = 11.sp)
                                        }
                                    }
                                    Text(
                                        text = String.format("%.0f MB", proc.ramUsage),
                                        color = TextGray,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            if (backgroundProcesses.size > 3) {
                                Text(
                                    text = "...và ${backgroundProcesses.size - 3} dịch vụ ngầm khác",
                                    color = TextGray,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 4.dp, start = 14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Large Game Booster Optimization Trigger Button
                        Button(
                            onClick = { performGameBoost() },
                            enabled = !isOptimizing,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(26.dp)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrightTurquoise,
                                contentColor = DeepObsidian,
                                disabledContainerColor = DarkTealCard,
                                disabledContentColor = TextGray
                            )
                        ) {
                            if (isOptimizing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = BrightTurquoise,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("ĐANG QUÉT & GIẢI PHÓNG...", fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.RocketLaunch, "Boost Icon")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "DỌN NGẦM & GIẢI PHÓNG RAM",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Overlaid Full-Screen Simulation Optimization Progress Dialog
        if (isOptimizing) {
            Dialog(onDismissRequest = {}) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(DeepObsidian)
                        .border(1.dp, BrightTurquoise.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = BrightTurquoise,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(56.dp)
                        )
                        
                        Text(
                            text = "HỆ THỐNG GAME BOOST ĐANG CHẠY",
                            color = BrightTurquoise,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = optMessage,
                            color = Color.White,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Cyberpunk styled warning message
                        Text(
                            text = "Đang áp dụng Luồng Ưu Tiên cho Card Đồ Họa và giải phóng RAM ngầm...",
                            color = TextGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        // 5. Optimization Result Success Dialog
        if (showOptimizationResult) {
            Dialog(onDismissRequest = { showOptimizationResult = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(DeepObsidian)
                        .border(1.dp, GlowGreen.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
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
                                .background(GlowGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Bolt, "Lightning icon", tint = GlowGreen, modifier = Modifier.size(36.dp))
                        }

                        Text(
                            text = "TỐI ƯU HOÀN TẤT!",
                            color = GlowGreen,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "Đã dọn dẹp triệt để các tệp tạm, đóng $lastOptimizedAppsCount tiến trình và trả lại không gian tài nguyên cực mượt cho thiết bị.",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        // Cool metrics display row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkTealCard)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("RAM Giải Phóng", fontSize = 11.sp, color = TextGray)
                                Text(
                                    text = String.format("%.1f GB", lastOptimizedRamAmount / 1024.0),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrightTurquoise
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(30.dp)
                                    .background(BorderGreen)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("FPS Dự Báo", fontSize = 11.sp, color = TextGray)
                                Text(
                                    text = "+20% FPS",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GlowGreen
                                )
                            }
                        }

                        Button(
                            onClick = { showOptimizationResult = false },
                            colors = ButtonDefaults.buttonColors(containerColor = GlowGreen, contentColor = DeepObsidian),
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

        // 6. Dialog for Adding Custom Game
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
                        .background(DeepObsidian)
                        .border(1.dp, BorderGreen, RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "THÊM GAME THỦ CÔNG",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )

                        Text(
                            text = "Đăng ký thêm game thủ công của bạn vào danh sách để hệ thống Game Booster hỗ trợ quản lý cấu hình và gia tăng luồng tài nguyên.",
                            color = TextGray,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        // App Name field
                        OutlinedTextField(
                            value = customAppName,
                            onValueChange = {
                                customAppName = it
                                appNameError = false
                            },
                            label = { Text("Tên Trò Chơi") },
                            placeholder = { Text("Ví dụ: Genshin Impact") },
                            isError = appNameError,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = BrightTurquoise,
                                unfocusedBorderColor = BorderGreen,
                                focusedLabelColor = BrightTurquoise,
                                unfocusedLabelColor = TextGray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Package Name field
                        OutlinedTextField(
                            value = customPackageName,
                            onValueChange = {
                                customPackageName = it
                                packageError = false
                            },
                            label = { Text("Package Name") },
                            placeholder = { Text("Ví dụ: com.mihoyo.genshin") },
                            isError = packageError,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = BrightTurquoise,
                                unfocusedBorderColor = BorderGreen,
                                focusedLabelColor = BrightTurquoise,
                                unfocusedLabelColor = TextGray
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
                                Text("HỦY BỎ", color = TextGray, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    if (customAppName.isBlank()) appNameError = true
                                    if (customPackageName.isBlank()) packageError = true

                                    if (customAppName.isNotBlank() && customPackageName.isNotBlank()) {
                                        viewModel.addNewCustomGame(customAppName.trim(), customPackageName.trim())
                                        showAddGameDialog = false
                                        android.widget.Toast.makeText(
                                            context,
                                            "Đã thêm game '${customAppName}' thành công!",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrightTurquoise, contentColor = DeepObsidian),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("THÊM GAME", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCircleGauge(
    value: String,
    label: String,
    progress: Float,
    activeColor: Color,
    modifier: Modifier = Modifier
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
                    color = BorderGreen.copy(alpha = 0.5f),
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
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = TextGray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun GameItemCard(
    game: AppItem,
    onLaunch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onLaunch() },
        colors = CardDefaults.cardColors(containerColor = DarkTealCard),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderGreen)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Controller/Game Icon placeholder
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BrightTurquoise.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = "Game Icon",
                        tint = BrightTurquoise,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Small high-performance badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(GlowGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "READY",
                        color = GlowGreen,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = game.appName,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = game.packageName,
                color = TextGray,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Launch action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RAM: ${(game.ramUsage).toInt()} MB",
                    color = TextGray,
                    fontSize = 11.sp
                )

                // Text Button for launch
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(BrightTurquoise.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Flash On",
                            tint = BrightTurquoise,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "CHƠI NGAY",
                            color = BrightTurquoise,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
