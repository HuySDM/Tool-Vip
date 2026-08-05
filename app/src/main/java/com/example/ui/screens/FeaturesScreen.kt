package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import com.example.ui.AppViewModel
import com.example.ui.OptimizationCycle
import com.example.ui.theme.ThemeColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun FeaturesScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val themeStyle by viewModel.themeStyle.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val activeColors = remember(themeStyle, isDarkMode) { ThemeColors.getColors(themeStyle, isDarkMode) }
    val optimizationCycles by viewModel.optimizationCycles.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Interactive Animation States
    var activeProgressName by remember { mutableStateOf("") }
    var activeProgressMsg by remember { mutableStateOf("") }
    var activeProgressVal by remember { mutableStateOf(0f) }
    var showProgressDialog by remember { mutableStateOf(false) }

    // Sub-feature state triggers
    val selectedGraphicsProfile by viewModel.selectedGraphicsProfile.collectAsState()
    var showGraphicsProfileDialog by remember { mutableStateOf(false) }
    var touchSensitivityMultiplier by remember { mutableStateOf(1.0f) }
    var showSensitivityDialog by remember { mutableStateOf(false) }
    var showNetworkMonitorDialog by remember { mutableStateOf(false) }
    var showFlowerPickerPanel by remember { mutableStateOf(false) }

    // Summary calculation
    val totalCleanedProcesses = remember(optimizationCycles) { optimizationCycles.sumOf { it.processesCleaned } }
    val totalReclaimedRamGb = remember(optimizationCycles) { optimizationCycles.sumOf { it.ramReclaimedMb } / 1024.0 }

    // Background scan status trigger simulator
    var lastBgRunTime by remember { mutableStateOf("05 phút trước") }

    // Function to run a custom optimization action with full visual feedback
    fun runOptimizationAction(
        title: String,
        messages: List<String>,
        onFinish: () -> Unit
    ) {
        coroutineScope.launch {
            activeProgressName = title
            activeProgressVal = 0f
            showProgressDialog = true
            for (i in 1..messages.size) {
                activeProgressMsg = messages[i - 1]
                val stepDuration = (400..700).random().toLong()
                var subStep = 0f
                while (subStep < 1f) {
                    delay(30)
                    subStep += 30f / stepDuration
                    activeProgressVal = ((i - 1) + subStep.coerceAtMost(1f)) / messages.size
                }
            }
            showProgressDialog = false
            onFinish()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(activeColors.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- SECTION 1: HEADER & STATISTICS SUMMARY ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = activeColors.primary.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = activeColors.cardBg
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(activeColors.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Analytics,
                                    contentDescription = "Analysis",
                                    tint = activeColors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "BÁO CÁO CHẠY NGẦM",
                                color = activeColors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(activeColors.secondary.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ĐANG HOẠT ĐỘNG",
                                color = activeColors.secondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Divider(color = activeColors.border.copy(alpha = 0.2f))

                    // Reclaimed info counters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Tiến trình đã dọn",
                                color = activeColors.textSecondary,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "$totalCleanedProcesses tiến trình",
                                color = activeColors.secondary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "RAM đã thu hồi",
                                color = activeColors.textSecondary,
                                fontSize = 11.sp
                            )
                            Text(
                                text = String.format("%.2f GB", totalReclaimedRamGb),
                                color = activeColors.primary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(activeColors.border.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Lần tối ưu ngầm gần nhất:",
                            color = activeColors.textSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = lastBgRunTime,
                            color = activeColors.textPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // --- SECTION 2: THAO TÁC NHANH (QUICK SHORTCUTS) ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "THAO TÁC NHANH VIP",
                    color = activeColors.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Quick Boost button
                    Button(
                        onClick = {
                            runOptimizationAction(
                                "TỐI ƯU SIÊU TỐC",
                                listOf(
                                    "Đang quét tệp rác hệ thống...",
                                    "Đang giải phóng luồng xử lý...",
                                    "Đang cân bằng bộ nhớ RAM..."
                                )
                            ) {
                                viewModel.addOptimizationCycle(8, 480.0, "Tối ưu siêu tốc")
                                lastBgRunTime = "Vừa xong"
                                viewModel.showToast("Đã kích hoạt tối ưu siêu tốc hoàn tất!")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = activeColors.primary),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FlashOn, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Dọn Rác Nhanh", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Quick Cooler button
                    Button(
                        onClick = {
                            runOptimizationAction(
                                "HẠ NHIỆT SIÊU CẤP",
                                listOf(
                                    "Đang dừng tiến trình rác...",
                                    "Cân bằng điện áp CPU...",
                                    "Ổn định nhiệt độ linh kiện..."
                                )
                            ) {
                                viewModel.addOptimizationCycle(5, 320.0, "Hạ nhiệt nhanh")
                                lastBgRunTime = "Vừa xong"
                                viewModel.showToast("Hạ nhiệt thành công! Nhiệt độ giảm 6°C")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = activeColors.secondary),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AcUnit, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hạ Nhiệt Nhanh", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }

        // --- SECTION 3: CORE OPTIMIZATION FEATURES LIST ---
        item {
            Text(
                text = "TÍNH NĂNG TỐI ƯU CHUYÊN SÂU",
                color = activeColors.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Feature 1: Tối Ưu Tăng Tốc CPU/GPU
        item {
            FeatureItemCard(
                title = "Tối Ưu Tăng Tốc CPU/GPU",
                description = "Ép xung nhân CPU/GPU ảo, dọn dẹp phân luồng xử lý và tối đa tài nguyên cho chip.",
                icon = Icons.Default.Memory,
                iconColor = activeColors.primary,
                buttonText = "TỐI ƯU NGAY",
                activeColors = activeColors
            ) {
                runOptimizationAction(
                    "TỐI ƯU CPU/GPU",
                    listOf(
                        "Cấu hình luồng tính toán CPU...",
                        "Điều chỉnh xung nhịp GPU...",
                        "Tăng tốc bộ nhớ đệm đồ họa...",
                        "Đồng bộ hóa khung hình mượt mà..."
                    )
                ) {
                    viewModel.addOptimizationCycle(12, 720.0, "Tối ưu CPU/GPU")
                    lastBgRunTime = "Vừa xong"
                    viewModel.showToast("Cấu hình tăng tốc CPU/GPU đã được áp dụng thành công!")
                }
            }
        }

        // Feature 2: Làm Mát Máy
        item {
            FeatureItemCard(
                title = "Hạ Nhiệt & Làm Mát Máy",
                description = "Phát hiện app ngầm gây nóng, giảm tải dòng điện và hạ nhiệt linh kiện cấp tốc.",
                icon = Icons.Default.Thermostat,
                iconColor = activeColors.secondary,
                buttonText = "LÀM MÁT",
                activeColors = activeColors
            ) {
                runOptimizationAction(
                    "LÀM MÁT ĐIỆN THOẠI",
                    listOf(
                        "Quét cảm biến nhiệt độ hệ thống...",
                        "Tắt ứng dụng chạy ngầm ngốn pin nóng máy...",
                        "Giảm xung nhịp tải dư thừa...",
                        "Cân bằng điện áp bo mạch chủ..."
                    )
                ) {
                    viewModel.addOptimizationCycle(9, 510.5, "Làm mát máy")
                    lastBgRunTime = "Vừa xong"
                    viewModel.showToast("Đã đóng ngầm các tiến trình gây nóng! Nhiệt độ giảm 8°C.")
                }
            }
        }

        // Feature 3: Chơi Game Nặng Với Đồ Họa Max Mượt Mà
        item {
            FeatureItemCard(
                title = "Đồ Họa Max Mượt Mà",
                description = "Cấu hình phân giải màn hình tối ưu, tối giản nén bộ nhớ và kích hoạt đồ họa HDR.",
                icon = Icons.Default.GridView,
                iconColor = activeColors.tertiary,
                buttonText = "CẤU HÌNH",
                activeColors = activeColors,
                statusBadgeText = selectedGraphicsProfile
            ) {
                showGraphicsProfileDialog = true
            }
        }

        // Feature 4: Tăng Độ Nhạy Cảm Ứng...
        item {
            FeatureItemCard(
                title = "Tăng Độ Nhạy Cảm Ứng",
                description = "Tăng tốc độ phản hồi của màn hình cảm ứng, hiệu chuẩn cảm biến chạm đa điểm.",
                icon = Icons.Default.AdsClick,
                iconColor = activeColors.primary,
                buttonText = "HIỆU CHUẨN",
                activeColors = activeColors,
                statusBadgeText = "${touchSensitivityMultiplier}x Nhạy"
            ) {
                showSensitivityDialog = true
            }
        }

        // Feature 5: Ổn Định Mạng/FPS
        item {
            FeatureItemCard(
                title = "Ổn Định Mạng & FPS",
                description = "Ưu tiên băng thông cho trò chơi đang mở, tinh chỉnh DNS tối ưu hóa Ping và chống giật lag.",
                icon = Icons.Default.Wifi,
                iconColor = activeColors.secondary,
                buttonText = "GIÁM SÁT MẠNG",
                activeColors = activeColors
            ) {
                showNetworkMonitorDialog = true
            }
        }

        // Feature 6: Auto Pick Hoa Flo (Liên Quân)
        item {
            FeatureItemCard(
                title = "Auto Pick Hoa Flo (Liên Quân)",
                description = "Kích hoạt trợ năng nhặt hoa tự động siêu tốc cho Florentino. Không lo khóa acc, tối ưu combo mượt mà.",
                icon = Icons.Default.Spa,
                iconColor = Color(0xFF00E5FF),
                buttonText = "MỞ PANEL TOOL",
                activeColors = activeColors
            ) {
                showFlowerPickerPanel = true
            }
        }

        // --- SECTION 4: HISTORICAL LOG REPORT LIST ---
        item {
            Text(
                text = "LỊCH SỬ CHU KỲ TỐI ƯU HOÁ NGẦM",
                color = activeColors.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        if (optimizationCycles.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chưa có chu kỳ tối ưu hóa nào được ghi nhận.",
                        color = activeColors.textMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(optimizationCycles, key = { it.id }) { cycle ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItemPlacement(),
                    colors = CardDefaults.cardColors(containerColor = activeColors.cardBg),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.8.dp, activeColors.border.copy(alpha = 0.15f))
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
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(activeColors.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when {
                                        cycle.type.contains("CPU") -> Icons.Default.Memory
                                        cycle.type.contains("nhiệt") -> Icons.Default.AcUnit
                                        cycle.type.contains("Mạng") -> Icons.Default.Wifi
                                        else -> Icons.Default.RocketLaunch
                                    },
                                    contentDescription = null,
                                    tint = activeColors.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = cycle.type,
                                    color = activeColors.textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Đã đóng: ${cycle.processesCleaned} tiến trình",
                                        color = activeColors.textSecondary,
                                        fontSize = 11.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(3.dp)
                                            .clip(CircleShape)
                                            .background(activeColors.textMuted)
                                    )
                                    Text(
                                        text = "Đã thu hồi: ${(cycle.ramReclaimedMb).toInt()} MB",
                                        color = activeColors.secondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Text(
                            text = cycle.timeString,
                            color = activeColors.textMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // --- MODAL 1: SCANNING PROGRESS DIALOG ---
    if (showProgressDialog) {
        Dialog(onDismissRequest = { /* Prevent cancel */ }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = activeColors.cardBg),
                border = BorderStroke(1.dp, activeColors.primary.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = activeProgressName,
                        color = activeColors.primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.sp
                    )

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = activeProgressVal,
                            color = activeColors.primary,
                            trackColor = activeColors.border.copy(alpha = 0.2f),
                            strokeWidth = 6.dp,
                            modifier = Modifier.size(100.dp)
                        )
                        Text(
                            text = "${(activeProgressVal * 100).toInt()}%",
                            color = activeColors.textPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Text(
                        text = activeProgressMsg,
                        color = activeColors.textSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    // --- MODAL 2: GRAPHICS PROFILE SELECTOR ---
    if (showGraphicsProfileDialog) {
        Dialog(onDismissRequest = { showGraphicsProfileDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = activeColors.cardBg),
                border = BorderStroke(1.2.dp, activeColors.primary)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "CHỌN CẤU HÌNH ĐỒ HỌA GAME",
                        color = activeColors.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    val profiles = listOf(
                        "Extreme 120 FPS Max" to "Tối đa hóa FPS hiển thị mượt mà nhất có thể.",
                        "Ultra Smooth Graphics" to "Nén họa tiết đồ họa tối giản để tránh sụt giảm FPS.",
                        "Zero-Lag Balanced" to "Cân bằng hoàn hảo hiệu năng xử lý hình ảnh và nhiệt độ máy."
                    )

                    profiles.forEach { (name, desc) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    runOptimizationAction(
                                        "THIẾT LẬP ĐỒ HỌA",
                                        listOf(
                                            "Phân tích GPU driver...",
                                            "Cấu hình nén hoạ tiết...",
                                            "Kích hoạt profile $name..."
                                        )
                                    ) {
                                        viewModel.setSelectedGraphicsProfile(name)
                                        viewModel.addOptimizationCycle(10, 600.0, "Cấu hình $name")
                                        lastBgRunTime = "Vừa xong"
                                        viewModel.showToast("Cấu hình $name thành công!")
                                    }
                                    showGraphicsProfileDialog = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedGraphicsProfile == name) activeColors.primary.copy(alpha = 0.12f) else activeColors.background
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (selectedGraphicsProfile == name) activeColors.primary else activeColors.border.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = name,
                                    color = if (selectedGraphicsProfile == name) activeColors.primary else activeColors.textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = desc,
                                    color = activeColors.textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { showGraphicsProfileDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = activeColors.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("ĐÓNG", color = activeColors.textPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // --- MODAL 3: TOUCH SENSITIVITY CALIBRATOR ---
    if (showSensitivityDialog) {
        Dialog(onDismissRequest = { showSensitivityDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = activeColors.cardBg),
                border = BorderStroke(1.2.dp, activeColors.primary)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "HIỆU CHUẨN CẢM ỨNG CHẠM",
                        color = activeColors.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = "Vẽ thử lên bảng dưới đây để đo lường phản hồi cảm ứng và tối ưu hóa độ nhạy của màn hình.",
                        color = activeColors.textSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp
                    )

                    var localMultiplier by remember { mutableStateOf(touchSensitivityMultiplier) }
                    val points = remember { mutableStateListOf<Offset>() }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = String.format("%.1fx Nhạy cảm ứng", localMultiplier),
                            color = activeColors.secondary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Slider(
                            value = localMultiplier,
                            onValueChange = { localMultiplier = it },
                            valueRange = 1.0f..3.0f,
                            steps = 3,
                            colors = SliderDefaults.colors(
                                thumbColor = activeColors.primary,
                                activeTrackColor = activeColors.primary,
                                inactiveTrackColor = activeColors.border.copy(alpha = 0.5f)
                            )
                        )
                    }

                    // Interactive Drawing Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.4f))
                            .border(BorderStroke(1.dp, activeColors.primary.copy(alpha = 0.3f)), RoundedCornerShape(10.dp))
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        points.add(offset)
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        points.add(change.position)
                                    }
                                )
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            if (points.size > 1) {
                                for (i in 0 until points.size - 1) {
                                    drawLine(
                                        color = activeColors.primary,
                                        start = points[i],
                                        end = points[i + 1],
                                        strokeWidth = (localMultiplier * 3f).dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                }
                            }
                        }

                        if (points.isEmpty()) {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Gesture, "Draw Here", tint = activeColors.primary.copy(alpha = 0.5f), modifier = Modifier.size(24.dp))
                                Text(
                                    text = "Hãy vẽ hoặc vuốt để thử cảm ứng",
                                    color = activeColors.textSecondary.copy(alpha = 0.6f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Live Diagnostics Information based on sensitivity and draw points
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("ĐỘ TRỄ", color = activeColors.textSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${String.format("%.1f", 12.0 / localMultiplier)} ms",
                                color = activeColors.secondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("TẦN SỐ", color = activeColors.textSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${(localMultiplier * 120).toInt()} Hz",
                                color = activeColors.secondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("ĐIỂM CHẠM", color = activeColors.textSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${points.size} pts",
                                color = activeColors.secondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("ĐỘ TRƠN", color = activeColors.textSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (localMultiplier < 1.6f) "Thường" else if (localMultiplier < 2.5f) "Mượt" else "Cực Nhạy",
                                color = activeColors.secondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Control Actions Row (Clear canvas & Apply calibrator)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { points.clear() },
                            border = BorderStroke(1.dp, activeColors.border),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = activeColors.textPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("XÓA NÉT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                runOptimizationAction(
                                    "HIỆU CHUẨN CẢM ỨNG",
                                    listOf(
                                        "Đang quét sơ đồ cảm ứng đa điểm...",
                                        "Tính toán gia tốc vuốt màn hình...",
                                        "Đã ghi nhận ${points.size} điểm chạm mẫu...",
                                        "Áp dụng độ nhạy phản xạ tối ưu..."
                                    )
                                ) {
                                    touchSensitivityMultiplier = (localMultiplier * 10).toInt() / 10f
                                    viewModel.addOptimizationCycle(4, 180.0, "Tăng độ nhạy chạm ${touchSensitivityMultiplier}x")
                                    lastBgRunTime = "Vừa xong"
                                    viewModel.showToast("Đã hiệu chuẩn cảm ứng ${touchSensitivityMultiplier}x thành công!")
                                }
                                showSensitivityDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = activeColors.primary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.5f)
                        ) {
                            Text("ÁP DỤNG", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // --- MODAL 4: ADAPTIVE NETWORK & DNS MONITOR ---
    if (showNetworkMonitorDialog) {
        val networkPingBoosted by viewModel.networkPingBoosted.collectAsState()
        // Dynamic simulated ping metrics
        var livePing by remember { mutableStateOf(16) }
        var liveJitter by remember { mutableStateOf(1.2f) }

        LaunchedEffect(key1 = showNetworkMonitorDialog, key2 = networkPingBoosted) {
            while (showNetworkMonitorDialog) {
                delay(1200)
                livePing = if (networkPingBoosted) {
                    12 + (0..6).random()
                } else {
                    24 + (0..15).random()
                }
                liveJitter = if (networkPingBoosted) {
                    0.2f + (1..5).random() * 0.1f
                } else {
                    0.8f + (1..10).random() * 0.15f
                }
            }
        }

        Dialog(onDismissRequest = { showNetworkMonitorDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = activeColors.cardBg),
                border = BorderStroke(1.2.dp, activeColors.secondary)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "GIÁM SÁT MẠNG & DNS THÍCH ỨNG",
                        color = activeColors.secondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )

                    // Network Health Gauge Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
                        border = BorderStroke(0.5.dp, activeColors.border.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("TRỄ MẠNG (PING)", color = activeColors.textSecondary, fontSize = 9.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("$livePing ms", color = if (livePing < 30) Color(0xFF00FFCC) else activeColors.primary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("JITTER (BIẾN ĐỘNG)", color = activeColors.textSecondary, fontSize = 9.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(String.format("%.2f ms", liveJitter), color = Color(0xFF00FFCC), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("MẤT GÓI (LOSS)", color = activeColors.textSecondary, fontSize = 9.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("0.0%", color = Color(0xFF00FFCC), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Adaptive DNS switcher panel
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Tự Động Thích Ứng DNS", color = activeColors.textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Tự động chuyển DNS nhanh nhất cho Game", color = activeColors.textSecondary, fontSize = 9.sp)
                        }
                        
                        Switch(
                            checked = true,
                            onCheckedChange = { },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = activeColors.secondary
                            )
                        )
                    }

                    // Active badge
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF00FFCC).copy(alpha = 0.1f))
                            .padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, "Active", tint = Color(0xFF00FFCC), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ĐÃ ĐỊNH TUYẾN TỐI ƯU: CLOUDFLARE DNS (12ms)",
                                color = Color(0xFF00FFCC),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Divider(color = activeColors.border.copy(alpha = 0.3f), thickness = 0.5.dp)

                    // List of DNS benchmarks
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("BẢNG TẦN SỐ ĐO TRỄ MÁY CHỦ DNS:", color = activeColors.textSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        
                        // Cloudflare Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF00FFCC)))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cloudflare DNS (1.1.1.1)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("12 ms (Tốt Nhất)", color = Color(0xFF00FFCC), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Google Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.02f))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.Gray))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Google DNS (8.8.8.8)", color = Color.White, fontSize = 11.sp)
                            }
                            Text("22 ms", color = activeColors.textSecondary, fontSize = 11.sp)
                        }

                        // AdGuard Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.02f))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.Gray))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AdGuard DNS", color = Color.White, fontSize = 11.sp)
                            }
                            Text("36 ms", color = activeColors.textSecondary, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showNetworkMonitorDialog = false },
                            border = BorderStroke(1.dp, activeColors.border),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = activeColors.textPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("ĐÓNG", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                runOptimizationAction(
                                    "ĐỊNH TUYẾN THÍCH ỨNG DNS",
                                    listOf(
                                        "Đang làm sạch bộ đệm DNS cache...",
                                        "Đang quét độ trễ Cloudflare, Google, AdGuard...",
                                        "Đang định tuyến đường truyền ưu tiên game...",
                                        "Kích hoạt tường lửa thích ứng chống nghẽn..."
                                    )
                                ) {
                                    viewModel.addOptimizationCycle(6, 280.0, "Thích ứng DNS & Tối ưu Mạng")
                                    lastBgRunTime = "Vừa xong"
                                    viewModel.showToast("Đã hoàn tất cấu hình DNS thích ứng bảo vệ mạng!")
                                }
                                showNetworkMonitorDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = activeColors.secondary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.5f)
                        ) {
                            Text("TỐI ƯU MẠNG", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showFlowerPickerPanel) {
        Dialog(
            onDismissRequest = { showFlowerPickerPanel = false },
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = activeColors.background
            ) {
                FlowerPickerScreen(
                    viewModel = viewModel,
                    onClose = { showFlowerPickerPanel = false }
                )
            }
        }
    }
}

@Composable
fun FeatureItemCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
    buttonText: String,
    activeColors: ThemeColors,
    statusBadgeText: String? = null,
    onButtonClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = activeColors.cardBg),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, activeColors.border.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(iconColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                    }
                    Text(
                        text = title,
                        color = activeColors.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (statusBadgeText != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(activeColors.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = statusBadgeText,
                            color = activeColors.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                color = activeColors.textSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onButtonClick,
                colors = ButtonDefaults.buttonColors(containerColor = activeColors.primary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(text = buttonText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
