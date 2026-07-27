package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel
import com.example.ui.TelexConverter
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainHubScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val cpuCores by viewModel.cpuCores.collectAsState()
    val gpuFreq by viewModel.gpuFreq.collectAsState()
    val gpuHistory by viewModel.gpuHistory.collectAsState()
    val cpuHistory by viewModel.cpuHistory.collectAsState()

    var camUngSieuNhay by remember { mutableStateOf(true) }
    var tangTocPhanHoi by remember { mutableStateOf(true) }
    var openFpsFixLag by remember { mutableStateOf(true) }
    
    // Game FPS settings state
    val gameFpsMap = remember {
        mutableStateMapOf(
            "FreeFire (Root)" to false,
            "FreeFire (No Root)" to true,
            "PUBG Mobile (Root)" to false,
            "PUBG Mobile (No Root)" to false,
            "Liên Quân Mobile (Root)" to false,
            "Liên Quân Mobile (No Root)" to true,
            "Call of Duty (Root)" to false,
            "Call of Duty (No Root)" to false,
            "Blood Strike 90FPS" to false,
            "Blood Strike 120FPS" to false,
            "FC Mobile (Root)" to false,
            "Genshin Impact Vulkan" to false,
            "Wuthering Waves (No Root)" to false
        )
    }

    var toiUu4G5G by remember { mutableStateOf(false) }
    var dnsGamingEnabled by remember { mutableStateOf(true) }
    var selectedDns by remember { mutableStateOf("NextDNS (Gaming)") }
    var showDnsDropdown by remember { mutableStateOf(false) }
    val dnsList = listOf("NextDNS (Gaming)", "Cloudflare (1.1.1.1)", "Google (8.8.8.8)", "AdGuard (Gaming)", "Quad9 Private")

    var sucKhoePinEnabled by remember { mutableStateOf(true) }
    var doPhanGiaiEnabled by remember { mutableStateOf(false) }
    var showResolutionDropdown by remember { mutableStateOf(false) }
    var selectedResolution by remember { mutableStateOf("FHD+ (1080p)") }
    val resolutionList = listOf("QHD+ (1440p)", "FHD+ (1080p)", "HD+ (720p)")

    var toiUuGpuEnabled by remember { mutableStateOf(true) }
    var toiUuCpuEnabled by remember { mutableStateOf(true) }
    var hoTroNgamGyro by remember { mutableStateOf(false) }
    var tatGioiHanNhiet by remember { mutableStateOf(false) }
    var mauSacRucRo by remember { mutableStateOf(true) }

    // Priority selector game list search
    var searchGameText by remember { mutableStateOf("") }
    val priorityGames = remember {
        mutableStateMapOf(
            "Liên Quân Mobile [Garena]" to true,
            "Block Blast!" to false,
            "Blood Strike: Vây Hãm" to false,
            "Free Fire MAX [MAX]" to false,
            "PUBG Mobile" to false
        )
    }

    val isOptimizing by viewModel.isOptimizing.collectAsState()
    val optMessage by viewModel.optimizationMessage.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. RAM & System Pulse Widget Card
        item {
            val userAccount by viewModel.userAccount.collectAsState()
            var currentRamUsage by remember { mutableStateOf(75) }
            
            // Fluctuating RAM usage simulation during idle/optimization
            LaunchedEffect(isOptimizing) {
                if (isOptimizing) {
                    currentRamUsage = 95
                    kotlinx.coroutines.delay(600)
                    currentRamUsage = 55
                    kotlinx.coroutines.delay(600)
                    currentRamUsage = 40
                } else {
                    currentRamUsage = 75
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BrightTurquoise.copy(alpha = 0.15f))
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Badge upper right corner
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(14.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(BrightTurquoise.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        val currentRoleLabel = if (!userAccount?.customRole.isNullOrBlank()) {
                            userAccount?.customRole!!.uppercase()
                        } else {
                            "${userAccount?.tier ?: "FREE"} STATUS"
                        }
                        Text(
                            text = currentRoleLabel,
                            color = BrightTurquoise,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Circular Progress Indicator using Canvas
                        Box(
                            modifier = Modifier.size(84.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val activeColor = BrightTurquoise
                            val inactiveColor = Color.White.copy(alpha = 0.05f)
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(
                                    color = inactiveColor,
                                    radius = size.minDimension / 2 - 4.dp.toPx(),
                                    style = Stroke(width = 7.dp.toPx())
                                )
                                drawArc(
                                    color = activeColor,
                                    startAngle = -90f,
                                    sweepAngle = (currentRamUsage / 100f) * 360f,
                                    useCenter = false,
                                    style = Stroke(width = 7.dp.toPx())
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$currentRamUsage%",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = if (isOptimizing) "CLEANING" else "OPTIMIZED",
                                    color = TextGray,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        // Text details and Quick Action button
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "System Pulse",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isOptimizing) "Đang đóng băng tác vụ ngầm..." else "32 Apps Frozen • 2.4GB Free",
                                color = TextGray,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { viewModel.optimizeRamDirectly() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrightTurquoise,
                                    contentColor = DeepObsidian
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = if (isOptimizing) "OPTIMIZING..." else "BOOST NOW",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. AI Recommendation Banner Card
        item {
            var showRecommendation by remember { mutableStateOf(true) }
            AnimatedVisibility(
                visible = showRecommendation,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BrightTurquoise.copy(alpha = 0.1f))
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF0D2525), Color(0xFF0A1A1A))
                                )
                            )
                            .drawBehind {
                                // Left accent border indicator
                                drawRect(
                                    color = BrightTurquoise,
                                    size = size.copy(width = 4.dp.toPx())
                                )
                            }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(22.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = "AI Recommendation",
                                    tint = BrightTurquoise,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "GỢI Ý TỪ TRỢ LÝ AI",
                                    color = BrightTurquoise,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "\"Chào sếp! Có 4 ứng dụng ngầm đang làm hao tổn pin và dung lượng RAM. Sếp có muốn em đóng băng ngay không?\"",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = { showRecommendation = false },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.White.copy(alpha = 0.05f),
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("Để sau", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.optimizeRamDirectly {
                                                showRecommendation = false
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = BrightTurquoise,
                                            contentColor = DeepObsidian
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("Đóng băng ngay", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // CPU Monitoring Module with fluctuating core frequency grid and line charts
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderGreen)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CPU Monitoring",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CoralVibrant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "LIVE",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 8 Core layout grid
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        maxItemsInEachRow = 4
                    ) {
                        cpuCores.forEachIndexed { index, clock ->
                            Card(
                                modifier = Modifier
                                    .padding(vertical = 4.dp, horizontal = 2.dp)
                                    .weight(1f)
                                    .minimumInteractiveComponentSize(),
                                colors = CardDefaults.cardColors(containerColor = DeepObsidian),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, BorderGreen)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "CORE $index",
                                        color = TextGray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$clock",
                                        color = BrightTurquoise,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live CPU/RAM Workload Graph
                    Text(text = "Biểu đồ hiệu suất CPU (%)", color = TextGray, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .background(DeepObsidian, RoundedCornerShape(8.dp))
                            .padding(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val path = Path()
                            val widthInterval = size.width / (cpuHistory.size - 1)
                            cpuHistory.forEachIndexed { index, value ->
                                val x = index * widthInterval
                                val y = size.height - (value / 100f * size.height)
                                if (index == 0) {
                                    path.moveTo(x, y)
                                } else {
                                    path.lineTo(x, y)
                                }
                            }
                            drawPath(
                                path = path,
                                color = GlowGreen,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    }
                }
            }
        }

        // Toggles: Cảm ứng siêu nhạy
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderGreen)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Cảm ứng siêu nhạy", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.Info, "Touch sensitive info", tint = TextGray, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(BorderGreen)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("NO ROOT", color = GlowGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Switch(
                        checked = camUngSieuNhay,
                        onCheckedChange = { camUngSieuNhay = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DeepObsidian,
                            checkedTrackColor = BrightTurquoise,
                            uncheckedThumbColor = TextGray,
                            uncheckedTrackColor = DeepObsidian
                        )
                    )
                }
            }
        }

        // Toggles: Tăng tốc phản hồi cảm ứng
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderGreen)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Tăng tốc phản hồi cảm ứng", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.Info, "Touch response info", tint = TextGray, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(BorderGreen)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("NO ROOT", color = GlowGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Switch(
                        checked = tangTocPhanHoi,
                        onCheckedChange = { tangTocPhanHoi = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DeepObsidian,
                            checkedTrackColor = BrightTurquoise,
                            uncheckedThumbColor = TextGray,
                            uncheckedTrackColor = DeepObsidian
                        )
                    )
                }
            }
        }

        // Expandable list: Mở khóa 120FPS & Fix Lag
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderGreen)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { openFpsFixLag = !openFpsFixLag },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Mở khóa 120FPS & Fix Lag",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CoralVibrant.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("NO ROOT/ROOT", color = CoralVibrant, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(text = "Cấu hình FPS cao cho từng game", color = TextGray, fontSize = 11.sp)
                        }
                        Icon(
                            imageVector = if (openFpsFixLag) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand",
                            tint = TextGray
                        )
                    }

                    AnimatedVisibility(visible = openFpsFixLag) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Divider(color = BorderGreen)
                            gameFpsMap.keys.forEach { gameKey ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val isRootGame = gameKey.contains("Root") && !gameKey.contains("No Root")
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (isRootGame) CoralVibrant.copy(alpha = 0.15f) else BorderGreen)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (isRootGame) "ROOT" else "NO ROOT",
                                                color = if (isRootGame) CoralVibrant else GlowGreen,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = gameKey, color = Color.White, fontSize = 13.sp)
                                    }
                                    Switch(
                                        checked = gameFpsMap[gameKey] ?: false,
                                        onCheckedChange = { gameFpsMap[gameKey] = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = DeepObsidian,
                                            checkedTrackColor = BrightTurquoise
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Tối ưu 4G/5G Network Boost
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderGreen)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Tối ưu 4G/5G", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.Info, "4G/5G optimization", tint = TextGray, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(BorderGreen)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("NO ROOT", color = GlowGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Switch(
                        checked = toiUu4G5G,
                        onCheckedChange = { toiUu4G5G = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DeepObsidian,
                            checkedTrackColor = BrightTurquoise
                        )
                    )
                }
            }
        }

        // DNS Gaming Dropdown selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderGreen)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "DNS Gaming", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.Info, "DNS description", tint = TextGray, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(BorderGreen)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                "NO ROOT".let { tag ->
                                    Text(tag, color = GlowGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Switch(
                            checked = dnsGamingEnabled,
                            onCheckedChange = { dnsGamingEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DeepObsidian,
                                checkedTrackColor = BrightTurquoise
                            )
                        )
                    }

                    if (dnsGamingEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DeepObsidian)
                                .border(1.dp, BorderGreen, RoundedCornerShape(8.dp))
                                .clickable { showDnsDropdown = !showDnsDropdown }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ElectricBolt, "DNS Icon", tint = AccentYellow, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = selectedDns, color = Color.White, fontSize = 13.sp)
                                }
                                Icon(Icons.Default.ArrowDropDown, "Dropdown", tint = TextGray)
                            }

                            DropdownMenu(
                                expanded = showDnsDropdown,
                                onDismissRequest = { showDnsDropdown = false },
                                modifier = Modifier.background(DarkTealCard)
                            ) {
                                dnsList.forEach { dns ->
                                    DropdownMenuItem(
                                        text = { Text(dns, color = Color.White) },
                                        onClick = {
                                            selectedDns = dns
                                            showDnsDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Battery Health ROOT monitor
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderGreen)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "Sức khỏe Pin", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.Info, "Battery Health Info", tint = TextGray, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CoralVibrant.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("ROOT", color = CoralVibrant, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Switch(
                            checked = sucKhoePinEnabled,
                            onCheckedChange = { sucKhoePinEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DeepObsidian,
                                checkedTrackColor = BrightTurquoise
                            )
                        )
                    }

                    if (sucKhoePinEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Sức khỏe Pin: 92% (3680 / 4000 mAh)",
                            color = GlowGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Screen Resolution Tweak with Dropdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderGreen)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "Độ phân giải MH", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.Info, "Resolution", tint = TextGray, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(BorderGreen)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("NO ROOT/ROOT", color = GlowGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Switch(
                            checked = doPhanGiaiEnabled,
                            onCheckedChange = { doPhanGiaiEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DeepObsidian,
                                checkedTrackColor = BrightTurquoise
                            )
                        )
                    }

                    if (doPhanGiaiEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DeepObsidian)
                                .border(1.dp, BorderGreen, RoundedCornerShape(8.dp))
                                .clickable { showResolutionDropdown = !showResolutionDropdown }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = selectedResolution, color = Color.White, fontSize = 13.sp)
                                Icon(Icons.Default.ArrowDropDown, "Dropdown", tint = TextGray)
                            }

                            DropdownMenu(
                                expanded = showResolutionDropdown,
                                onDismissRequest = { showResolutionDropdown = false },
                                modifier = Modifier.background(DarkTealCard)
                            ) {
                                resolutionList.forEach { res ->
                                    DropdownMenuItem(
                                        text = { Text(res, color = Color.White) },
                                        onClick = {
                                            selectedResolution = res
                                            showResolutionDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // GPU Booster + Live Frequency indicator graph
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderGreen)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "Tối ưu GPU (Max GPU)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.Info, "GPU Booster", tint = TextGray, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(BorderGreen)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("NO ROOT/ROOT", color = GlowGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Switch(
                            checked = toiUuGpuEnabled,
                            onCheckedChange = { toiUuGpuEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DeepObsidian,
                                checkedTrackColor = BrightTurquoise
                            )
                        )
                    }

                    if (toiUuGpuEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Tần số GPU hiện tại:", color = TextGray, fontSize = 12.sp)
                            Text(
                                text = "$gpuFreq MHz",
                                color = BrightTurquoise,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Custom GPU frequency wave drawing canvas
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .background(DeepObsidian, RoundedCornerShape(8.dp))
                                .padding(horizontal = 4.dp, vertical = 8.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val path = Path()
                                val maxVal = 650f
                                val minVal = 400f
                                val widthInterval = size.width / (gpuHistory.size - 1)
                                gpuHistory.forEachIndexed { index, value ->
                                    val x = index * widthInterval
                                    val normalized = (value - minVal) / (maxVal - minVal)
                                    val y = size.height - (normalized * size.height)
                                    if (index == 0) {
                                        path.moveTo(x, y)
                                    } else {
                                        path.lineTo(x, y)
                                    }
                                }
                                drawPath(
                                    path = path,
                                    color = BrightTurquoise,
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }
                        }
                    }
                }
            }
        }

        // Tối ưu CPU (Max CPU)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderGreen)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Tối ưu CPU (Max CPU)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.Info, "CPU booster", tint = TextGray, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(BorderGreen)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("NO ROOT/ROOT", color = GlowGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Switch(
                        checked = toiUuCpuEnabled,
                        onCheckedChange = { toiUuCpuEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DeepObsidian,
                            checkedTrackColor = BrightTurquoise
                        )
                    )
                }
            }
        }

        // Network prioritization checklist & Search block
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderGreen)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Ưu tiên mạng, Ping thấp",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.ArrowDropDown, "Dropdown icon", tint = TextGray, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(BorderGreen)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("NO ROOT/ROOT", color = GlowGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchGameText,
                        onValueChange = { searchGameText = TelexConverter.convert(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Tìm kiếm app/game...", fontSize = 12.sp, color = TextGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrightTurquoise,
                            unfocusedBorderColor = BorderGreen,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Games Checkbox List
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        priorityGames.keys
                            .filter { it.lowercase().contains(searchGameText.lowercase()) }
                            .forEach { gameTitle ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DeepObsidian)
                                        .clickable { priorityGames[gameTitle] = !(priorityGames[gameTitle] ?: false) }
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = priorityGames[gameTitle] ?: false,
                                            onCheckedChange = { priorityGames[gameTitle] = it },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = BrightTurquoise,
                                                checkmarkColor = DeepObsidian
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = gameTitle, color = Color.White, fontSize = 13.sp)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(CoralVibrant)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("GAME", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                    }
                }
            }
        }

        // Toggles: Hỗ trợ ngắm & Gyro (ROOT)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderGreen)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Hỗ trợ ngắm & Gyro", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.Info, "Aim helper", tint = TextGray, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CoralVibrant.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("ROOT", color = CoralVibrant, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Switch(
                        checked = hoTroNgamGyro,
                        onCheckedChange = { hoTroNgamGyro = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DeepObsidian,
                            checkedTrackColor = BrightTurquoise
                        )
                    )
                }
            }
        }

        // Tắt giới hạn nhiệt
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderGreen)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Tắt giới hạn nhiệt", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.Info, "Thermal throttling removal", tint = TextGray, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(BorderGreen)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("NO ROOT/ROOT", color = GlowGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Switch(
                        checked = tatGioiHanNhiet,
                        onCheckedChange = { tatGioiHanNhiet = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DeepObsidian,
                            checkedTrackColor = BrightTurquoise
                        )
                    )
                }
            }
        }

        // Màu sắc rực rỡ (ROOT)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderGreen)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Màu sắc rực rỡ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.Info, "Vivid display", tint = TextGray, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CoralVibrant.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("ROOT", color = CoralVibrant, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Switch(
                        checked = mauSacRucRo,
                        onCheckedChange = { mauSacRucRo = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DeepObsidian,
                            checkedTrackColor = BrightTurquoise
                        )
                    )
                }
            }
        }

        // Security & Fair-play warning about Florentino automatic flower picking removal
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeepObsidian),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CoralVibrant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning icon",
                        tint = CoralVibrant,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "LƯU Ý AN TOÀN TÀI KHOẢN",
                            color = CoralVibrant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Chế độ tự động nhặt hoa Florentino (Auto Flo) đã bị loại bỏ và gỡ hẳn khỏi hệ thống nhằm phòng tránh nguy cơ bị khóa tài khoản Garena vĩnh viễn.",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Bottom text notice
        item {
            Text(
                text = "Đã bật: Độ nhạy, Tắt giới hạn nhiệt, Phản hồi chạm, Màu sắc rực rỡ, Ưu tiên mạng, Ping thấp, Game Booster vĩnh viễn.",
                color = TextGray,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )
        }

        // Big floating "GAME BOOSTER" Trigger button
        item {
            Button(
                onClick = { viewModel.optimizeRamDirectly() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(27.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CoralVibrant,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.RocketLaunch, "Rocket Icon")
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isOptimizing) "ĐANG TỐI ƯU CỰC HẠN..." else "KÍCH HOẠT GAME BOOSTER",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }
        }
    }
}
