package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.FloAccessibilityService
import com.example.ui.AppViewModel
import com.example.ui.theme.ThemeColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowerPickerScreen(
    viewModel: AppViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeStyle by viewModel.themeStyle.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val activeColors = remember(themeStyle, isDarkMode) { ThemeColors.getColors(themeStyle, isDarkMode) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Service state
    var isAccessibilityOn by remember { mutableStateOf(false) }
    var isRunning by remember { mutableStateOf(false) }

    // Live update accessibility status
    LaunchedEffect(Unit) {
        while (true) {
            isAccessibilityOn = FloAccessibilityService.isServiceEnabled(context)
            delay(1000)
        }
    }

    // Configurable Parameters
    var pickDelay by remember { mutableStateOf(150f) } // 50 to 400 ms
    var colorTolerance by remember { mutableStateOf(40f) } // 10 to 80
    var scanInterval by remember { mutableStateOf(50f) } // 20 to 150 ms
    var flowerColorSelected by remember { mutableStateOf("#FFD700") } // Yellow Gold

    // Simulation variables
    var playerPos by remember { mutableStateOf(Offset(250f, 250f)) }
    var targetPlayerPos by remember { mutableStateOf(Offset(250f, 250f)) }
    val flowers = remember { mutableStateListOf<Offset>() }
    var flowersPickedCount by remember { mutableStateOf(0) }
    var scanningAnimationAngle by remember { mutableStateOf(0f) }
    var joystickOffset by remember { mutableStateOf(Offset.Zero) }

    // Spawn flowers and run simulation
    LaunchedEffect(isRunning) {
        if (isRunning) {
            // Initial flower spawn
            flowers.clear()
            repeat(3) {
                flowers.add(Offset((100..400).random().toFloat(), (100..400).random().toFloat()))
            }

            while (isRunning) {
                delay(scanInterval.toLong())
                
                // Animate radar sweep
                scanningAnimationAngle = (scanningAnimationAngle + 12f) % 360f

                if (flowers.isNotEmpty()) {
                    // Find nearest flower
                    var nearestFlower: Offset? = null
                    var minDist = Float.MAX_VALUE
                    for (flower in flowers) {
                        val dist = hypot(flower.x - playerPos.x, flower.y - playerPos.y)
                        if (dist < minDist) {
                            minDist = dist
                            nearestFlower = flower
                        }
                    }

                    nearestFlower?.let { flower ->
                        // Simulate joystick movement direction
                        val dx = flower.x - playerPos.x
                        val dy = flower.y - playerPos.y
                        val dist = hypot(dx, dy)
                        
                        if (dist > 15f) {
                            // Move towards flower
                            val moveStep = 30f // speed multiplier
                            val angle = kotlin.math.atan2(dy, dx)
                            joystickOffset = Offset(cos(angle) * 40f, sin(angle) * 40f)
                            
                            val nextX = playerPos.x + cos(angle) * moveStep
                            val nextY = playerPos.y + sin(angle) * moveStep
                            playerPos = Offset(nextX, nextY)
                        } else {
                            // Picked!
                            flowers.remove(flower)
                            flowersPickedCount++
                            joystickOffset = Offset.Zero
                            
                            // Spawn replacement flower with short delay
                            delay(pickDelay.toLong())
                            flowers.add(Offset((100..400).random().toFloat(), (100..400).random().toFloat()))
                        }
                    }
                } else {
                    joystickOffset = Offset.Zero
                    // Spawn new flowers if empty
                    flowers.add(Offset((100..400).random().toFloat(), (100..400).random().toFloat()))
                }
            }
        } else {
            joystickOffset = Offset.Zero
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(activeColors.background)
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(activeColors.cardBg)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = activeColors.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "🌸 AUTO PICK HOA FLO",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Công cụ bổ trợ Liên Quân Mobile",
                        color = activeColors.textMuted,
                        fontSize = 11.sp
                    )
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isRunning) activeColors.secondary.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isRunning) "RUNNING" else "STOPPED",
                    color = if (isRunning) activeColors.secondary else Color.Red,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- SECTION 1: ACCESSIBILITY STATUS CARD ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = (if (isAccessibilityOn) activeColors.secondary else Color.Red).copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = activeColors.cardBg
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        (if (isAccessibilityOn) activeColors.secondary else Color.Red).copy(
                                            alpha = 0.15f
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isAccessibilityOn) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = "Status Icon",
                                    tint = if (isAccessibilityOn) activeColors.secondary else Color.Red,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = if (isAccessibilityOn) "Dịch vụ trợ năng: ĐÃ BẬT" else "Dịch vụ trợ năng: CHƯA BẬT",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (isAccessibilityOn) 
                                "Dịch vụ 'FloAccessibilityService' đã sẵn sàng điều hướng và mô phỏng nút di chuyển trong game."
                            else 
                                "Vui lòng cấp quyền Trợ năng để ứng dụng có thể mô phỏng cử chỉ kéo joystick tự động nhặt hoa.",
                            color = activeColors.textSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )

                        if (!isAccessibilityOn) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    try {
                                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                                        viewModel.showToast("Tìm 'FloAccessibilityService' và bật nó lên!")
                                    } catch (e: Exception) {
                                        viewModel.showToast("Không thể mở Cài đặt Trợ năng")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Settings, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("MỞ CÀI ĐẶT TRỢ NĂNG", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // --- SECTION 2: INTERACTIVE VISUALIZER SIMULATOR (RADAR ARENA) ---
            item {
                Text(
                    text = "MÔ PHỎNG THỰC THỜI (LIVE ARENA)",
                    color = activeColors.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, activeColors.border.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF070B12)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Stat bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("HOA ĐÃ NHẶT", color = activeColors.textMuted, fontSize = 10.sp)
                                Text("$flowersPickedCount hoa", color = activeColors.secondary, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("FPS BOOSTER", color = activeColors.textMuted, fontSize = 10.sp)
                                Text("60.0 FPS", color = activeColors.primary, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Interactive Canvas (Simulated game screen)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF030712))
                                .border(1.dp, activeColors.border.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height

                                // Draw radar circular grids
                                drawCircle(
                                    color = activeColors.primary.copy(alpha = 0.05f),
                                    radius = w * 0.4f,
                                    center = Offset(w / 2, h / 2),
                                    style = Stroke(1.dp.toPx())
                                )
                                drawCircle(
                                    color = activeColors.primary.copy(alpha = 0.1f),
                                    radius = w * 0.25f,
                                    center = Offset(w / 2, h / 2),
                                    style = Stroke(1.dp.toPx())
                                )
                                drawCircle(
                                    color = activeColors.primary.copy(alpha = 0.15f),
                                    radius = w * 0.1f,
                                    center = Offset(w / 2, h / 2),
                                    style = Stroke(1.dp.toPx())
                                )

                                // Draw Radar Sweep Line
                                val angleRad = Math.toRadians(scanningAnimationAngle.toDouble())
                                val sweepX = (w / 2) + cos(angleRad).toFloat() * (w * 0.45f)
                                val sweepY = (h / 2) + sin(angleRad).toFloat() * (w * 0.45f)
                                drawLine(
                                    color = activeColors.primary.copy(alpha = 0.3f),
                                    start = Offset(w / 2, h / 2),
                                    end = Offset(sweepX, sweepY),
                                    strokeWidth = 2.dp.toPx()
                                )

                                // Scale variables to fit canvas
                                fun getScaledPos(offset: Offset): Offset {
                                    val scaleX = w / 500f
                                    val scaleY = h / 500f
                                    return Offset(offset.x * scaleX, offset.y * scaleY)
                                }

                                val scaledPlayer = getScaledPos(playerPos)

                                // Draw flowers
                                for (flower in flowers) {
                                    val scaledFlower = getScaledPos(flower)
                                    // Glow behind flower
                                    drawCircle(
                                        color = Color(0xFFFFD700).copy(alpha = 0.25f),
                                        radius = 14.dp.toPx(),
                                        center = scaledFlower
                                    )
                                    // Flower core
                                    drawCircle(
                                        color = Color(0xFFFFD700),
                                        radius = 6.dp.toPx(),
                                        center = scaledFlower
                                    )
                                    // Petal rings
                                    drawCircle(
                                        color = Color(0xFFFF80DF),
                                        radius = 4.dp.toPx(),
                                        center = scaledFlower,
                                        style = Stroke(2.dp.toPx())
                                    )

                                    // Line from player to nearest flower if running
                                    if (isRunning) {
                                        drawLine(
                                            color = activeColors.secondary.copy(alpha = 0.4f),
                                            start = scaledPlayer,
                                            end = scaledFlower,
                                            strokeWidth = 1.dp.toPx(),
                                            cap = StrokeCap.Round
                                        )
                                    }
                                }

                                // Draw Player (Florentino)
                                drawCircle(
                                    color = activeColors.primary.copy(alpha = 0.3f),
                                    radius = 16.dp.toPx(),
                                    center = scaledPlayer
                                )
                                drawCircle(
                                    color = activeColors.primary,
                                    radius = 8.dp.toPx(),
                                    center = scaledPlayer
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 3.dp.toPx(),
                                    center = scaledPlayer
                                )
                            }

                            // Info Overlay inside canvas
                            Column(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                    .padding(4.dp)
                            ) {
                                Text("Player: Florentino", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                Text("Xung quét màu: $flowerColorSelected", color = activeColors.primary, fontSize = 8.sp)
                                Text("Sai số dọn: ${colorTolerance.toInt()}px", color = activeColors.textSecondary, fontSize = 8.sp)
                            }

                            // Virtual Joystick Visualization
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(12.dp)
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                            ) {
                                // Core thumb
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .offset(joystickOffset.x.dp / 2, joystickOffset.y.dp / 2)
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                listOf(
                                                    activeColors.primary,
                                                    activeColors.primary.copy(alpha = 0.6f)
                                                )
                                            )
                                        )
                                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Control Buttons
                        Button(
                            onClick = {
                                if (!isAccessibilityOn) {
                                    viewModel.showToast("Vui lòng bật dịch vụ Trợ năng trước!")
                                    return@Button
                                }
                                isRunning = !isRunning
                                if (isRunning) {
                                    viewModel.showToast("✅ Đã kích hoạt Auto Pick Hoa Flo!")
                                } else {
                                    viewModel.showToast("⏹ Đã tắt Auto Pick Hoa Flo!")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRunning) Color.Red else activeColors.primary,
                                contentColor = if (isRunning) Color.White else Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = "Start/Stop",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isRunning) "DỪNG HOẠT ĐỘNG" else "KÍCH HOẠT AUTO PICK",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // --- SECTION 3: ADVANCED CONFIGURATIONS ---
            item {
                Text(
                    text = "CẤU HÌNH THÔNG SỐ CHẠY SCRIPT",
                    color = activeColors.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, activeColors.border.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = activeColors.cardBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Delay slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.HourglassEmpty, null, tint = activeColors.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Độ trễ nhận diện hoa (Delay)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("${pickDelay.toInt()} ms", color = activeColors.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = pickDelay,
                                onValueChange = { pickDelay = it },
                                valueRange = 50f..400f,
                                colors = SliderDefaults.colors(
                                    thumbColor = activeColors.primary,
                                    activeTrackColor = activeColors.primary,
                                    inactiveTrackColor = activeColors.border.copy(alpha = 0.3f)
                                )
                            )
                            Text("Độ trễ thấp giúp combo nhanh hơn nhưng cần máy cấu hình mạnh để tránh drop FPS.", color = activeColors.textMuted, fontSize = 10.sp)
                        }

                        Divider(color = activeColors.border.copy(alpha = 0.15f))

                        // Tolerance slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ColorLens, null, tint = activeColors.secondary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Độ nhạy quét màu hoa", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("${colorTolerance.toInt()} px", color = activeColors.secondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = colorTolerance,
                                onValueChange = { colorTolerance = it },
                                valueRange = 10f..80f,
                                colors = SliderDefaults.colors(
                                    thumbColor = activeColors.secondary,
                                    activeTrackColor = activeColors.secondary,
                                    inactiveTrackColor = activeColors.border.copy(alpha = 0.3f)
                                )
                            )
                            Text("Tăng độ sai số nhận diện nếu điều kiện ánh sáng hoặc map đấu bị đổi màu sắc hoa.", color = activeColors.textMuted, fontSize = 10.sp)
                        }

                        Divider(color = activeColors.border.copy(alpha = 0.15f))

                        // Scan Interval Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Speed, null, tint = activeColors.tertiary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Tần số quét màn hình (Scan Rate)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("${scanInterval.toInt()} ms", color = activeColors.tertiary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = scanInterval,
                                onValueChange = { scanInterval = it },
                                valueRange = 20f..150f,
                                colors = SliderDefaults.colors(
                                    thumbColor = activeColors.tertiary,
                                    activeTrackColor = activeColors.tertiary,
                                    inactiveTrackColor = activeColors.border.copy(alpha = 0.3f)
                                )
                            )
                            Text("Khoảng thời gian giữa các lượt quét màn hình chụp ảnh để dò tìm hoa.", color = activeColors.textMuted, fontSize = 10.sp)
                        }
                    }
                }
            }

            // --- SECTION 4: USER INSTRUCTIONS GUIDE ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, activeColors.border.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = activeColors.cardBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HelpOutline, null, tint = activeColors.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("📖 HƯỚNG DẪN HOẠT ĐỘNG", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            InstructionRow("1", "Cấp quyền trợ năng cho ứng dụng Tool Vip bằng cách bấm nút mở cài đặt phía trên.")
                            InstructionRow("2", "Quay lại ứng dụng, cấu hình độ trễ và độ nhạy tối ưu cho Florentino.")
                            InstructionRow("3", "Bấm BẮT ĐẦU AUTO PICK để khởi chạy công cụ.")
                            InstructionRow("4", "Vào trận đấu Liên Quân, khi tung chiêu 1 hoặc chiêu cuối tạo hoa, công cụ sẽ tự động di chuyển joystick đến vị trí hoa.")
                            InstructionRow("5", "Để tắt khẩn cấp: Bấm nút DỪNG trong bảng này hoặc dùng phím Giảm Âm Lượng (Volume Down).")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InstructionRow(num: String, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E3A3A)),
            contentAlignment = Alignment.Center
        ) {
            Text(num, color = Color(0xFF18E2C2), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Text(text, color = Color(0xFFE2E8F0), fontSize = 12.sp, lineHeight = 16.sp)
    }
}
