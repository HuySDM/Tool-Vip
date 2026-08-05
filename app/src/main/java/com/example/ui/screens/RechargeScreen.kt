package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.AppViewModel
import com.example.ui.TelexConverter
import com.example.ui.theme.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RechargeScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val userAccount by viewModel.userAccount.collectAsState()
    val allUserAccounts by viewModel.allUserAccounts.collectAsState()
    var selectedUserForAdminActions by remember { mutableStateOf("") }
    var adminActiveTab by remember { mutableStateOf(0) } // 0: Users, 1: Revenue & Prices, 2: Giftcodes
    val df = DecimalFormat("#,###")

    var customAmountText by remember { mutableStateOf("") }
    var selectedAmountByQuick by remember { mutableStateOf(50000.0) }
    var promoInputText by remember { mutableStateOf("") }
    
    // Bank transfer simulation display state
    var showBankTransferPortal by remember { mutableStateOf(false) }
    var lastRechargedAmount by remember { mutableStateOf(0.0) }
    
    var adminUsername by remember { mutableStateOf("") }
    var adminPassword by remember { mutableStateOf("") }
    
    var notificationMessage by remember { mutableStateOf<String?>(null) }
    var notificationError by remember { mutableStateOf<String?>(null) }

    var aiChatInput by remember { mutableStateOf("") }
    val chatHistory by viewModel.chatHistory.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    val isGeneratingPromoCode by viewModel.isGeneratingPromoCode.collectAsState()
    val aiPromoCodeMessage by viewModel.aiPromoCodeMessage.collectAsState()

    // AI Bot logs & Transactions
    val aiAdminLogs by viewModel.aiAdminLogs.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val userTransactions by viewModel.userTransactions.collectAsState()

    // Auto-dismiss notifications
    LaunchedEffect(notificationMessage) {
        if (notificationMessage != null) {
            kotlinx.coroutines.delay(4000)
            notificationMessage = null
        }
    }
    LaunchedEffect(notificationError) {
        if (notificationError != null) {
            kotlinx.coroutines.delay(4000)
            notificationError = null
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Premium Logo Header & App Identity Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BrightTurquoise.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            // Subtle radial top teal glow
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(BrightTurquoise.copy(alpha = 0.08f), Color.Transparent),
                                    center = Offset(size.width / 2, 0f),
                                    radius = size.width / 1.5f
                                ),
                                radius = size.width / 1.5f,
                                center = Offset(size.width / 2, 0f)
                            )
                        }
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Dynamic Logo Loading from drawable/img_tool_vip_logo.jpg
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(
                                    BorderStroke(2.dp, BrightTurquoise),
                                    RoundedCornerShape(16.dp)
                                )
                                .background(DeepObsidian),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_tool_vip_logo_v2_1785668711300),
                                contentDescription = "Tool Vip Premium Logo",
                                modifier = Modifier.fillMaxSize().padding(2.dp),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "TOOL VIP v9.0",
                            color = BrightTurquoise,
                            fontWeight = FontWeight.Black,
                            fontSize = 26.sp,
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "HỆ THỐNG TỐI ƯU HÓA GAME & ĐÓNG BĂNG MÁY CHUYÊN SÂU",
                            color = TextGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Login Card Section or Admin Control Panel Section
        item {
            if (userAccount?.tier == "ADMIN") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(2.dp, CoralVibrant)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "BẢNG ĐIỀU KHIỂN ADMIN TỐI CAO",
                                    color = CoralVibrant,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Hệ thống quản trị cấp bậc & chi phối dòng tiền",
                                    color = TextGray,
                                    fontSize = 11.sp
                                )
                            }
                            Icon(Icons.Default.Security, "Admin Shield", tint = CoralVibrant, modifier = Modifier.size(30.dp))
                        }
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = Color.White.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(14.dp))

                        // Collect duration configurations
                        val vip1DurationValue by viewModel.vip1DurationValue.collectAsState()
                        val vip1DurationUnit by viewModel.vip1DurationUnit.collectAsState()
                        val vip2DurationValue by viewModel.vip2DurationValue.collectAsState()
                        val vip2DurationUnit by viewModel.vip2DurationUnit.collectAsState()

                        // 3 Beautifully Designed Tab Selectors
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val tabsList = listOf(
                                "Thành Viên" to Icons.Default.People,
                                "Doanh Thu & Giá" to Icons.Default.AttachMoney,
                                "Mã Giftcode" to Icons.Default.CardGiftcard
                            )
                            tabsList.forEachIndexed { idx, (title, icon) ->
                                val isSelected = adminActiveTab == idx
                                Button(
                                    onClick = { adminActiveTab = idx },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) CoralVibrant else CoralVibrant.copy(alpha = 0.08f),
                                        contentColor = if (isSelected) Color.White else TextGray
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(vertical = 8.dp),
                                    border = if (isSelected) BorderStroke(1.dp, CoralVibrant) else BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(icon, null, modifier = Modifier.size(16.dp), tint = if (isSelected) Color.White else TextGray)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(title, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        if (adminActiveTab == 0) {
                            // ==================== TAB 0: QUẢN LÝ THÀNH VIÊN ====================
                            Text(
                                text = "CHỌN TÀI KHOẢN THÀNH VIÊN ĐỂ ĐIỀU KHIỂN",
                                color = BrightTurquoise,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            var userSearchText by remember { mutableStateOf("") }
                            OutlinedTextField(
                                value = userSearchText,
                                onValueChange = { userSearchText = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Tìm kiếm tên tài khoản...", color = TextGray, fontSize = 11.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrightTurquoise,
                                    unfocusedBorderColor = BorderGreen.copy(alpha = 0.5f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val filteredUsers = allUserAccounts.filter {
                                it.username.contains(userSearchText, ignoreCase = true)
                            }
                            
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                items(filteredUsers) { account ->
                                    val isSelected = selectedUserForAdminActions == account.username
                                    val borderGlow = if (isSelected) BrightTurquoise else BorderGreen.copy(alpha = 0.3f)
                                    val bgGlow = if (isSelected) BrightTurquoise.copy(alpha = 0.15f) else Color.Transparent
                                    
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(bgGlow)
                                            .border(BorderStroke(1.dp, borderGlow), RoundedCornerShape(8.dp))
                                            .clickable {
                                                selectedUserForAdminActions = account.username
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = account.username,
                                                color = if (isSelected) BrightTurquoise else Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                            Text(
                                                text = "${df.format(account.balance)}đ",
                                                color = GlowGreen,
                                                fontSize = 9.sp
                                            )
                                            Text(
                                                text = account.tier,
                                                color = CoralVibrant,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                            
                            val effectiveTargetUser = if (selectedUserForAdminActions.isNotEmpty()) selectedUserForAdminActions else (userAccount?.username ?: "")
                            val targetAccount = allUserAccounts.find { it.username == effectiveTargetUser }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BrightTurquoise.copy(alpha = 0.1f))
                                    .border(1.dp, BrightTurquoise.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "🎯 ĐANG ĐIỀU HÀNH TÀI KHOẢN: $effectiveTargetUser",
                                        color = BrightTurquoise,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    if (targetAccount != null) {
                                        Text(
                                            text = "• Số dư hiện tại: ${df.format(targetAccount.balance)}đ  |  • Cấp độ: ${targetAccount.tier} (${targetAccount.customRole})",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            Divider(color = Color.White.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            // PART A: QUẢN LÝ CẤP BẬC / PHÂN QUYỀN
                            Text(
                                text = "1. PHÂN QUYỀN CẤP BẬC THÀNH VIÊN",
                                color = BrightTurquoise,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val tiersList = listOf(
                                    "UNPAID" to "HỘI VIÊN THƯỜNG",
                                    "VIP1" to "NÂNG VIP 1",
                                    "VIP2" to "NÂNG VIP 2"
                                )
                                tiersList.forEach { (tKey, label) ->
                                    Button(
                                        onClick = { 
                                            viewModel.adjustUserTierDirect(effectiveTargetUser, tKey)
                                            notificationMessage = "Đã thay đổi cấp bậc cho [$effectiveTargetUser] thành công: $label!"
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (tKey == "UNPAID") Color(0xFF2D151B) else BorderGreen,
                                            contentColor = if (tKey == "UNPAID") CoralVibrant else GlowGreen
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                    ) {
                                        Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val staffList = listOf(
                                    "STAFF" to "BỔ NHIỆM NV",
                                    "MANAGER" to "BỔ NHIỆM QL",
                                    "PARTNER" to "BỔ NHIỆM CTV"
                                )
                                staffList.forEach { (roleKey, label) ->
                                    Button(
                                        onClick = {
                                            viewModel.adjustUserTierDirect(effectiveTargetUser, roleKey)
                                            notificationMessage = "Đã bổ nhiệm chức vụ cho [$effectiveTargetUser] sang $label!"
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CoralVibrant.copy(alpha = 0.15f),
                                            contentColor = CoralVibrant
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(0.5.dp, CoralVibrant),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                    ) {
                                        Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // PART B: CHI PHỐI DÒNG TIỀN
                            Text(
                                text = "2. CHI PHỐI DÒNG TIỀN ẢO HỆ THỐNG",
                                color = BrightTurquoise,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val cashModifiers = listOf(
                                    50000.0 to "+50K",
                                    100000.0 to "+100K",
                                    200000.0 to "+200K",
                                    -100000.0 to "-100K"
                                )
                                cashModifiers.forEach { (amount, label) ->
                                    Button(
                                        onClick = { 
                                            viewModel.adjustUserBalanceDirect(effectiveTargetUser, amount, "Admin chi phối dòng tiền")
                                            notificationMessage = "Đã thay đổi số dư tài khoản [$effectiveTargetUser]: ${if (amount >= 0) "+" else ""}${df.format(amount)}đ"
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (amount < 0) Color(0xFF2D151B) else DeepObsidian,
                                            contentColor = if (amount < 0) CoralVibrant else GlowGreen
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                    ) {
                                        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            var customBalanceText by remember { mutableStateOf("") }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = customBalanceText,
                                    onValueChange = { input ->
                                        if (input.all { it.isDigit() }) {
                                            customBalanceText = input
                                        }
                                    },
                                    modifier = Modifier.weight(1.5f),
                                    placeholder = { Text("Nhập số tiền...", color = TextGray, fontSize = 11.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CoralVibrant,
                                        unfocusedBorderColor = BorderGreen,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                
                                Button(
                                    onClick = {
                                        val balanceValue = customBalanceText.toDoubleOrNull()
                                        if (balanceValue != null && balanceValue >= 0) {
                                            viewModel.setUserBalanceDirect(effectiveTargetUser, balanceValue)
                                            notificationMessage = "Đã đặt số dư cho [$effectiveTargetUser] thành ${df.format(balanceValue)}đ"
                                            customBalanceText = ""
                                        } else {
                                            notificationError = "Vui lòng nhập số dư hợp lệ!"
                                        }
                                    },
                                    modifier = Modifier.weight(1.5f),
                                    colors = ButtonDefaults.buttonColors(containerColor = CoralVibrant, contentColor = Color.White),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(vertical = 12.dp)
                                ) {
                                    Text("ĐẶT SỐ DƯ THỦ CÔNG", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = Color.White.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(12.dp))

                            // PART D: PHÊ DUYỆT THÀNH VIÊN BAN QUẢN TRỊ
                            Text(
                                text = "3. DUYỆT THÀNH VIÊN BAN QUẢN TRỊ",
                                color = BrightTurquoise,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val pendingRequests by viewModel.pendingRoleRequests.collectAsState()
                            LaunchedEffect(Unit) {
                                viewModel.loadPendingRoleRequests()
                            }

                            if (pendingRequests.isEmpty()) {
                                Text(
                                    text = "Không có yêu cầu ứng tuyển ban quản lý nào đang chờ duyệt.",
                                    color = TextGray,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    pendingRequests.forEach { req ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(DeepObsidian, RoundedCornerShape(8.dp))
                                                .border(BorderStroke(0.5.dp, BorderGreen.copy(alpha = 0.5f)), RoundedCornerShape(8.dp))
                                                .padding(8.dp)
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Tài khoản: ${req.username}",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                                Text(
                                                    text = "Email: ${req.email}",
                                                    color = TextGray,
                                                    fontSize = 10.sp
                                                )
                                                Text(
                                                    text = "Ứng tuyển vai trò: ${req.requestedRoleName}",
                                                    color = GlowGreen,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 11.sp
                                                )
                                                
                                                Spacer(modifier = Modifier.height(6.dp))
                                                
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Button(
                                                        onClick = { viewModel.approveRoleRequest(req.username) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = GlowGreen, contentColor = DeepObsidian),
                                                        shape = RoundedCornerShape(6.dp),
                                                        modifier = Modifier.weight(1f),
                                                        contentPadding = PaddingValues(vertical = 4.dp)
                                                    ) {
                                                        Text("DUYỆT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    Button(
                                                        onClick = { viewModel.rejectRoleRequest(req.username) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = CoralVibrant, contentColor = Color.White),
                                                        shape = RoundedCornerShape(6.dp),
                                                        modifier = Modifier.weight(1f),
                                                        contentPadding = PaddingValues(vertical = 4.dp)
                                                    ) {
                                                        Text("TỪ CHỐI", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = Color.White.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(12.dp))

                            // PART E: CẬP NHẬT TÀI KHOẢN & MẬT KHẨU ADMIN TỐI CAO
                            Text(
                                text = "4. CẤU HÌNH TÀI KHOẢN & MẬT KHẨU ADMIN",
                                color = BrightTurquoise,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            var newAdminUser by remember { mutableStateOf("") }
                            var newAdminPass by remember { mutableStateOf("") }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = newAdminUser,
                                    onValueChange = { newAdminUser = it },
                                    label = { Text("Tên tài khoản Admin mới", color = TextGray, fontSize = 10.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CoralVibrant,
                                        unfocusedBorderColor = BorderGreen,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = newAdminPass,
                                    onValueChange = { newAdminPass = it },
                                    label = { Text("Mật khẩu Admin mới", color = TextGray, fontSize = 10.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CoralVibrant,
                                        unfocusedBorderColor = BorderGreen,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        viewModel.updateAdminCredentials(newAdminUser, newAdminPass) { success, message ->
                                            if (success) {
                                                notificationMessage = message
                                                newAdminUser = ""
                                                newAdminPass = ""
                                            } else {
                                                notificationError = message
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CoralVibrant, contentColor = Color.White),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("CẬP NHẬT TÀI KHOẢN ADMIN", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else if (adminActiveTab == 1) {
                            // ==================== TAB 1: DOANH THU & GIÁ VIP ====================
                            val succTx = allTransactions.filter { it.status == "SUCCESS" }
                            val totalDeposited = succTx.filter { it.type == "DEPOSIT" }.sumOf { it.amount }
                            val totalVip1Purchased = succTx.filter { it.type == "UPGRADE_VIP1" }.sumOf { kotlin.math.abs(it.amount) }
                            val totalVip2Purchased = succTx.filter { it.type == "UPGRADE_VIP2" }.sumOf { kotlin.math.abs(it.amount) }
                            val allAccountsBalances = allUserAccounts.sumOf { it.balance }
                            val calculatedTotalRevenue = totalDeposited // Since total money entered in the system is deposits!
                            
                            Text(
                                text = "BÁO CÁO DOANH THU HỆ THỐNG",
                                color = BrightTurquoise,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            // Grid reporting
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Row 1: Calculated Pure Revenue
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(GlowGreen.copy(alpha = 0.1f))
                                        .border(1.dp, GlowGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("TỔNG DOANH THU THỰC TẾ (NẠP TIỀN)", color = TextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            Text("${df.format(calculatedTotalRevenue)}đ", color = GlowGreen, fontSize = 20.sp, fontWeight = FontWeight.Black)
                                        }
                                        Icon(Icons.Default.TrendingUp, null, tint = GlowGreen, modifier = Modifier.size(36.dp))
                                    }
                                }
                                
                                // Row 2: Grid of specific sales
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(BrightTurquoise.copy(alpha = 0.05f))
                                            .border(0.5.dp, BrightTurquoise.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Text("DOANH SỐ VIP 1", color = TextGray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            Text("${df.format(totalVip1Purchased)}đ", color = BrightTurquoise, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                            Text("Lượt mua: ${succTx.filter { it.type == "UPGRADE_VIP1" }.size}", color = TextGray, fontSize = 8.sp)
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(CoralVibrant.copy(alpha = 0.05f))
                                            .border(0.5.dp, CoralVibrant.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Text("DOANH SỐ VIP 2 PRO", color = TextGray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            Text("${df.format(totalVip2Purchased)}đ", color = CoralVibrant, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                            Text("Lượt mua: ${succTx.filter { it.type == "UPGRADE_VIP2" }.size}", color = TextGray, fontSize = 8.sp)
                                        }
                                    }
                                }
                                
                                // Row 3: Grid of liability & counts
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color.White.copy(alpha = 0.03f))
                                            .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Text("SỐ DƯ THÀNH VIÊN", color = TextGray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            Text("${df.format(allAccountsBalances)}đ", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("Tổng quỹ ví", color = TextGray, fontSize = 8.sp)
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color.White.copy(alpha = 0.03f))
                                            .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Text("SỐ THÀNH VIÊN", color = TextGray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            Text("${allUserAccounts.size} người dùng", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("VIP1: ${allUserAccounts.filter { it.tier == "VIP1" }.size} | VIP2: ${allUserAccounts.filter { it.tier == "VIP2" }.size}", color = TextGray, fontSize = 8.sp)
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            Divider(color = Color.White.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // GIAO DIỆN CẤU HÌNH VIP GIÁ & THỜI HẠN
                            Text(
                                text = "THIẾT LẬP GIÁ TIỀN & THỜI HẠN VIP TOÀN CẦU",
                                color = BrightTurquoise,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Thiết lập giá trị nạp kích hoạt VIP 1 & VIP 2, cùng thời hạn trải nghiệm khi người dùng nhấn nút mua VIP.",
                                color = TextGray,
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // VIP 1 config inputs
                            var editVip1Price by remember { mutableStateOf("") }
                            var editVip1DurVal by remember { mutableStateOf("") }
                            var editVip1DurUnit by remember { mutableStateOf("ngày") } // "giờ", "ngày", "tuần", "tháng", "vĩnh viễn"
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BrightTurquoise.copy(alpha = 0.04f))
                                    .border(0.5.dp, BrightTurquoise.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("CẤU HÌNH GÓI VIP 1 - HIỆN TẠI: ${df.format(userAccount?.vip1Price ?: 50000.0)}đ / $vip1DurationValue $vip1DurationUnit", color = BrightTurquoise, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = editVip1Price,
                                            onValueChange = { if (it.all { c -> c.isDigit() }) editVip1Price = it },
                                            modifier = Modifier.weight(1.5f),
                                            placeholder = { Text("Giá mới (VND)", color = TextGray, fontSize = 10.sp) },
                                            label = { Text("Giá tiền (đ)", color = TextGray, fontSize = 8.sp) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = BrightTurquoise,
                                                unfocusedBorderColor = BorderGreen.copy(alpha = 0.4f),
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                        )
                                        OutlinedTextField(
                                            value = editVip1DurVal,
                                            onValueChange = { if (it.all { c -> c.isDigit() }) editVip1DurVal = it },
                                            modifier = Modifier.weight(1f),
                                            placeholder = { Text("VD: 30", color = TextGray, fontSize = 10.sp) },
                                            label = { Text("Thời lượng", color = TextGray, fontSize = 8.sp) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = BrightTurquoise,
                                                unfocusedBorderColor = BorderGreen.copy(alpha = 0.4f),
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            enabled = editVip1DurUnit != "vĩnh viễn"
                                        )
                                    }
                                    
                                    // Unit selectors
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        val units = listOf("giờ", "ngày", "tuần", "tháng", "vĩnh viễn")
                                        units.forEach { u ->
                                            val isSelected = editVip1DurUnit == u
                                            Button(
                                                onClick = { editVip1DurUnit = u },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isSelected) BrightTurquoise else DeepObsidian,
                                                    contentColor = if (isSelected) DeepObsidian else TextGray
                                                ),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp)
                                            ) {
                                                Text(u, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    
                                    Button(
                                        onClick = {
                                            val priceVal = editVip1Price.toDoubleOrNull() ?: (userAccount?.vip1Price ?: 50000.0)
                                            val durValue = editVip1DurVal.toIntOrNull() ?: 30
                                            // Save prices
                                            viewModel.updateVipPricesGlobally(priceVal, userAccount?.vip2Price ?: 120000.0)
                                            // Save duration
                                            viewModel.updateVipConfig("VIP1", durValue, editVip1DurUnit)
                                            notificationMessage = "Cập nhật cấu hình VIP 1 thành công!"
                                            editVip1Price = ""
                                            editVip1DurVal = ""
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = BrightTurquoise, contentColor = DeepObsidian),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("LƯU CẤU HÌNH VIP 1", fontSize = 10.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // VIP 2 config inputs
                            var editVip2Price by remember { mutableStateOf("") }
                            var editVip2DurVal by remember { mutableStateOf("") }
                            var editVip2DurUnit by remember { mutableStateOf("ngày") } // "giờ", "ngày", "tuần", "tháng", "vĩnh viễn"
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CoralVibrant.copy(alpha = 0.04f))
                                    .border(0.5.dp, CoralVibrant.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("CẤU HÌNH GÓI VIP 2 PRO - HIỆN TẠI: ${df.format(userAccount?.vip2Price ?: 120000.0)}đ / $vip2DurationValue $vip2DurationUnit", color = CoralVibrant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = editVip2Price,
                                            onValueChange = { if (it.all { c -> c.isDigit() }) editVip2Price = it },
                                            modifier = Modifier.weight(1.5f),
                                            placeholder = { Text("Giá mới (VND)", color = TextGray, fontSize = 10.sp) },
                                            label = { Text("Giá tiền (đ)", color = TextGray, fontSize = 8.sp) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = CoralVibrant,
                                                unfocusedBorderColor = BorderGreen.copy(alpha = 0.4f),
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                        )
                                        OutlinedTextField(
                                            value = editVip2DurVal,
                                            onValueChange = { if (it.all { c -> c.isDigit() }) editVip2DurVal = it },
                                            modifier = Modifier.weight(1f),
                                            placeholder = { Text("VD: 30", color = TextGray, fontSize = 10.sp) },
                                            label = { Text("Thời lượng", color = TextGray, fontSize = 8.sp) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = CoralVibrant,
                                                unfocusedBorderColor = BorderGreen.copy(alpha = 0.4f),
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            enabled = editVip2DurUnit != "vĩnh viễn"
                                        )
                                    }
                                    
                                    // Unit selectors
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        val units = listOf("giờ", "ngày", "tuần", "tháng", "vĩnh viễn")
                                        units.forEach { u ->
                                            val isSelected = editVip2DurUnit == u
                                            Button(
                                                onClick = { editVip2DurUnit = u },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isSelected) CoralVibrant else DeepObsidian,
                                                    contentColor = if (isSelected) Color.White else TextGray
                                                ),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp)
                                            ) {
                                                Text(u, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    
                                    Button(
                                        onClick = {
                                            val priceVal = editVip2Price.toDoubleOrNull() ?: (userAccount?.vip2Price ?: 120000.0)
                                            val durValue = editVip2DurVal.toIntOrNull() ?: 30
                                            // Save prices
                                            viewModel.updateVipPricesGlobally(userAccount?.vip1Price ?: 50000.0, priceVal)
                                            // Save duration
                                            viewModel.updateVipConfig("VIP2", durValue, editVip2DurUnit)
                                            notificationMessage = "Cập nhật cấu hình VIP 2 PRO thành công!"
                                            editVip2Price = ""
                                            editVip2DurVal = ""
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = CoralVibrant, contentColor = Color.White),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("LƯU CẤU HÌNH VIP 2 PRO", fontSize = 10.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        } else {
                            // ==================== TAB 2: QUẢN LÝ GIFTCODES VIP ====================
                            Text(
                                text = "TẠO & QUẢN LÝ MÃ GIFTCODE VIP",
                                color = BrightTurquoise,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Khởi tạo mã Giftcode với giá trị bậc VIP và khoảng thời lượng sử dụng chính xác tùy chọn của bạn.",
                                color = TextGray,
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            var newCodeText by remember { mutableStateOf("") }
                            var selectedCodeTier by remember { mutableStateOf("VIP1") }
                            var selectedCodeDurVal by remember { mutableStateOf("30") }
                            var selectedCodeDurUnit by remember { mutableStateOf("ngày") } // "giờ", "ngày", "tuần", "tháng", "vĩnh viễn"

                            // Expiration settings for the gift code itself
                            var expiryPreset by remember { mutableStateOf("unlimited") } // "unlimited", "10m", "1h", "1d", "7d", "30d", "custom"
                            var expiryCustomValue by remember { mutableStateOf("24") }
                            var expiryCustomUnit by remember { mutableStateOf("giờ") } // "giờ", "ngày"
                            var randomCodeLength by remember { mutableStateOf(8) } // 6, 8, 12

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = newCodeText,
                                    onValueChange = { newCodeText = it.uppercase() },
                                    label = { Text("Nhập mã Giftcode tự chọn (VD: PRO99)", color = TextGray, fontSize = 10.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CoralVibrant,
                                        unfocusedBorderColor = BorderGreen,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Generator helper row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Length selector
                                    Row(
                                        modifier = Modifier
                                            .weight(1.2f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(DeepObsidian)
                                            .border(0.5.dp, BorderGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                            .padding(2.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        listOf(6, 8, 12).forEach { len ->
                                            val isSelected = randomCodeLength == len
                                            Button(
                                                onClick = { randomCodeLength = len },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isSelected) CoralVibrant else Color.Transparent,
                                                    contentColor = if (isSelected) Color.White else TextGray
                                                 ),
                                                 shape = RoundedCornerShape(6.dp),
                                                 modifier = Modifier.weight(1f),
                                                 contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp)
                                             ) {
                                                 Text("${len} ký tự", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                             }
                                         }
                                     }

                                     // Trigger generator
                                     Button(
                                         onClick = {
                                             val allowedChars = ('A'..'Z') + ('0'..'9')
                                             newCodeText = (1..randomCodeLength).map { allowedChars.random() }.joinToString("")
                                         },
                                         colors = ButtonDefaults.buttonColors(
                                             containerColor = ElectricBlue,
                                             contentColor = Color.White
                                         ),
                                         shape = RoundedCornerShape(8.dp),
                                         modifier = Modifier.weight(1f),
                                         contentPadding = PaddingValues(vertical = 8.dp)
                                     ) {
                                         Icon(
                                             imageVector = Icons.Default.Refresh,
                                             contentDescription = "Tạo ngẫu nhiên",
                                             modifier = Modifier.size(12.dp)
                                         )
                                         Spacer(modifier = Modifier.width(4.dp))
                                         Text("NGẪU NHIÊN", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                     }
                                 }

                                 Row(
                                     modifier = Modifier.fillMaxWidth(),
                                     horizontalArrangement = Arrangement.spacedBy(8.dp),
                                     verticalAlignment = Alignment.CenterVertically
                                 ) {
                                     // Tier selector
                                     Box(
                                         modifier = Modifier.weight(1.2f).clip(RoundedCornerShape(8.dp)).background(DeepObsidian).border(0.5.dp, BorderGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(2.dp)
                                     ) {
                                         Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                             val tiers = listOf("VIP1" to "VIP 1", "VIP2" to "VIP 2")
                                             tiers.forEach { (t, label) ->
                                                 val isSel = selectedCodeTier == t
                                                 Button(
                                                     onClick = { selectedCodeTier = t },
                                                     colors = ButtonDefaults.buttonColors(
                                                         containerColor = if (isSel) CoralVibrant else Color.Transparent,
                                                         contentColor = if (isSel) Color.White else TextGray
                                                     ),
                                                     shape = RoundedCornerShape(6.dp),
                                                     modifier = Modifier.weight(1f),
                                                     contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp)
                                                 ) {
                                                     Text(label, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                 }
                                             }
                                         }
                                     }

                                     // Duration val text field
                                     OutlinedTextField(
                                         value = selectedCodeDurVal,
                                         onValueChange = { if (it.all { c -> c.isDigit() }) selectedCodeDurVal = it },
                                         modifier = Modifier.weight(0.8f),
                                         placeholder = { Text("Thời lượng", color = TextGray, fontSize = 10.sp) },
                                         label = { Text("Số lượng", color = TextGray, fontSize = 8.sp) },
                                         colors = OutlinedTextFieldDefaults.colors(
                                             focusedBorderColor = BrightTurquoise,
                                             unfocusedBorderColor = BorderGreen,
                                             focusedTextColor = Color.White,
                                             unfocusedTextColor = Color.White
                                         ),
                                         shape = RoundedCornerShape(8.dp),
                                         singleLine = true,
                                         keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                         enabled = selectedCodeDurUnit != "vĩnh viễn"
                                     )
                                 }
                                 
                                 // Unit selector buttons row
                                 Row(
                                     modifier = Modifier.fillMaxWidth(),
                                     horizontalArrangement = Arrangement.spacedBy(4.dp)
                                 ) {
                                     val units = listOf("giờ", "ngày", "tuần", "tháng", "vĩnh viễn")
                                     units.forEach { u ->
                                         val isSel = selectedCodeDurUnit == u
                                         Button(
                                             onClick = { selectedCodeDurUnit = u },
                                             modifier = Modifier.weight(1f),
                                             colors = ButtonDefaults.buttonColors(
                                                 containerColor = if (isSel) BrightTurquoise else DeepObsidian,
                                                 contentColor = if (isSel) DeepObsidian else TextGray
                                             ),
                                             shape = RoundedCornerShape(6.dp),
                                             contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp)
                                         ) {
                                             Text(u, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                         }
                                     }
                                 }

                                 Spacer(modifier = Modifier.height(2.dp))
                                 Text(
                                     text = "HẠN TỰ ĐỘNG HỦY CỦA MÃ GIFTCODE:",
                                     color = TextGray,
                                     fontSize = 9.sp,
                                     fontWeight = FontWeight.Bold
                                 )

                                 // Preset grid row 1
                                 Row(
                                     modifier = Modifier.fillMaxWidth(),
                                     horizontalArrangement = Arrangement.spacedBy(4.dp)
                                 ) {
                                     val presets1 = listOf(
                                         "unlimited" to "Không hạn",
                                         "10m" to "10 phút",
                                         "1h" to "1 giờ",
                                         "1d" to "1 ngày"
                                     )
                                     presets1.forEach { (p, label) ->
                                         val isSel = expiryPreset == p
                                         Button(
                                             onClick = { expiryPreset = p },
                                             modifier = Modifier.weight(1f),
                                             colors = ButtonDefaults.buttonColors(
                                                 containerColor = if (isSel) BrightTurquoise else DeepObsidian,
                                                 contentColor = if (isSel) DeepObsidian else TextGray
                                             ),
                                             shape = RoundedCornerShape(6.dp),
                                             contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp)
                                         ) {
                                             Text(label, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                         }
                                     }
                                 }

                                 // Preset grid row 2
                                 Row(
                                     modifier = Modifier.fillMaxWidth(),
                                     horizontalArrangement = Arrangement.spacedBy(4.dp)
                                 ) {
                                     val presets2 = listOf(
                                         "7d" to "7 ngày",
                                         "30d" to "30 ngày",
                                         "custom" to "Tùy chỉnh"
                                     )
                                     presets2.forEach { (p, label) ->
                                         val isSel = expiryPreset == p
                                         Button(
                                             onClick = { expiryPreset = p },
                                             modifier = Modifier.weight(1f),
                                             colors = ButtonDefaults.buttonColors(
                                                 containerColor = if (isSel) BrightTurquoise else DeepObsidian,
                                                 contentColor = if (isSel) DeepObsidian else TextGray
                                             ),
                                             shape = RoundedCornerShape(6.dp),
                                             contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp)
                                         ) {
                                             Text(label, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                         }
                                     }
                                 }

                                 // Show custom expiration input if selected
                                 if (expiryPreset == "custom") {
                                     Row(
                                         modifier = Modifier.fillMaxWidth(),
                                         horizontalArrangement = Arrangement.spacedBy(8.dp),
                                         verticalAlignment = Alignment.CenterVertically
                                     ) {
                                         OutlinedTextField(
                                             value = expiryCustomValue,
                                             onValueChange = { if (it.all { c -> c.isDigit() }) expiryCustomValue = it },
                                             modifier = Modifier.weight(1.2f),
                                             placeholder = { Text("Số lượng", color = TextGray, fontSize = 10.sp) },
                                             label = { Text("Hạn sử dụng", color = TextGray, fontSize = 8.sp) },
                                             colors = OutlinedTextFieldDefaults.colors(
                                                 focusedBorderColor = BrightTurquoise,
                                                 unfocusedBorderColor = BorderGreen,
                                                 focusedTextColor = Color.White,
                                                 unfocusedTextColor = Color.White
                                             ),
                                             shape = RoundedCornerShape(8.dp),
                                             singleLine = true,
                                             keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                         )

                                         Row(
                                             modifier = Modifier
                                                 .weight(1f)
                                                 .clip(RoundedCornerShape(8.dp))
                                                 .background(DeepObsidian)
                                                 .border(0.5.dp, BorderGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                                 .padding(2.dp),
                                             horizontalArrangement = Arrangement.spacedBy(4.dp)
                                         ) {
                                             listOf("giờ", "ngày").forEach { unit ->
                                                 val isSel = expiryCustomUnit == unit
                                                 Button(
                                                     onClick = { expiryCustomUnit = unit },
                                                     colors = ButtonDefaults.buttonColors(
                                                         containerColor = if (isSel) BrightTurquoise else Color.Transparent,
                                                         contentColor = if (isSel) DeepObsidian else TextGray
                                                     ),
                                                     shape = RoundedCornerShape(6.dp),
                                                     modifier = Modifier.weight(1f),
                                                     contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp)
                                                 ) {
                                                     Text(unit, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                 }
                                             }
                                         }
                                     }
                                 }

                                 Button(
                                     onClick = {
                                         if (newCodeText.trim().isNotBlank()) {
                                             val durVal = selectedCodeDurVal.toIntOrNull() ?: 30
                                             
                                             // Calculate expiry timestamp
                                             val now = System.currentTimeMillis()
                                             val expiryTimestamp = when (expiryPreset) {
                                                 "10m" -> now + 10 * 60 * 1000L
                                                 "1h" -> now + 60 * 60 * 1000L
                                                 "1d" -> now + 24 * 60 * 60 * 1000L
                                                 "7d" -> now + 7 * 24 * 60 * 60 * 1000L
                                                 "30d" -> now + 30L * 24 * 60 * 60 * 1000L
                                                 "custom" -> {
                                                     val customVal = expiryCustomValue.toLongOrNull() ?: 24L
                                                     if (expiryCustomUnit == "ngày") {
                                                         now + customVal * 24 * 60 * 60 * 1000L
                                                     } else {
                                                         now + customVal * 60 * 60 * 1000L
                                                     }
                                                 }
                                                 else -> 0L // unlimited
                                             }

                                             viewModel.addCustomGiftCode(
                                                 code = newCodeText.trim(),
                                                 tier = selectedCodeTier,
                                                 durationValue = durVal,
                                                 durationUnit = selectedCodeDurUnit,
                                                 expiryTimestamp = expiryTimestamp
                                             )
                                             notificationMessage = "Đã tạo mã quà tặng thành công!"
                                             newCodeText = ""
                                         } else {
                                             notificationError = "Vui lòng nhập hoặc chọn tạo mã ngẫu nhiên!"
                                         }
                                     },
                                     colors = ButtonDefaults.buttonColors(containerColor = GlowGreen, contentColor = DeepObsidian),
                                     shape = RoundedCornerShape(8.dp),
                                     modifier = Modifier.fillMaxWidth()
                                 ) {
                                     Text("TẠO MÃ QUÀ TẶNG NGAY", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                 }
                             }

                            // Display active custom gift codes
                            val customGiftCodes by viewModel.customGiftCodes.collectAsState()
                            if (customGiftCodes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "DANH SÁCH MÃ QUÀ TẶNG ĐÃ TẠO:",
                                    color = TextGray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    customGiftCodes.forEach { gc ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(DeepObsidian, RoundedCornerShape(6.dp))
                                                .border(0.5.dp, BorderGreen.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = gc.code,
                                                    color = BrightTurquoise,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                                val durDesc = if (gc.durationUnit == "vĩnh viễn") "Vĩnh viễn" else "${gc.durationValue} ${gc.durationUnit}"
                                                val isExpired = gc.expiryTimestamp > 0L && System.currentTimeMillis() > gc.expiryTimestamp
                                                val expiryText = if (gc.expiryTimestamp > 0L) {
                                                    val sdfStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(gc.expiryTimestamp))
                                                    if (isExpired) "Hết hạn: $sdfStr (QUÁ HẠN)" else "Hết hạn: $sdfStr"
                                                } else {
                                                    "Hạn sử dụng: Không giới hạn"
                                                }
                                                Text(
                                                    text = "Loại: ${gc.tier} • Trị giá: $durDesc",
                                                    color = TextGray,
                                                    fontSize = 9.sp
                                                )
                                                Text(
                                                    text = expiryText,
                                                    color = if (isExpired) Color.Red else if (gc.expiryTimestamp > 0L) AccentYellow else GlowGreen,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                val isExpired = gc.expiryTimestamp > 0L && System.currentTimeMillis() > gc.expiryTimestamp
                                                val statusLabel = if (gc.isUsed) "ĐÃ DÙNG" else if (isExpired) "HẾT HẠN" else "SẴN SÀNG"
                                                val statusColor = if (gc.isUsed) Color.Red else if (isExpired) Color.Gray else GlowGreen
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(statusColor.copy(alpha = 0.15f))
                                                        .border(0.5.dp, statusColor, RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = statusLabel,
                                                        color = statusColor,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = CoralVibrant,
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                        .clickable {
                                                            viewModel.deleteCustomGiftCode(gc.code)
                                                        }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color.White.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Exit/Logout Admin button
                        Button(
                            onClick = { 
                                viewModel.logoutOrReset() 
                                notificationMessage = "Đã thoát chế độ Admin thành công!"
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ExitToApp, "Exit")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("THOÁT QUYỀN ADMIN", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, CoralVibrant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "ĐĂNG NHẬP QUẢN TRỊ VIÊN (ADMIN)",
                                color = CoralVibrant,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Icon(Icons.Default.Security, "Admin Login Icon", tint = CoralVibrant, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = adminUsername,
                            onValueChange = { adminUsername = TelexConverter.convert(it) },
                            label = { Text("Tên tài khoản (TK)", color = TextGray) },
                            placeholder = { Text("Nhập tài khoản admin", color = TextGray.copy(alpha = 0.5f)) },
                            leadingIcon = { Icon(Icons.Default.Person, "Username", tint = TextGray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CoralVibrant,
                                unfocusedBorderColor = BorderGreen,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        OutlinedTextField(
                            value = adminPassword,
                            onValueChange = { adminPassword = TelexConverter.convert(it) },
                            label = { Text("Mật khẩu (MK)", color = TextGray) },
                            placeholder = { Text("Nhập mật khẩu admin", color = TextGray.copy(alpha = 0.5f)) },
                            leadingIcon = { Icon(Icons.Default.Lock, "Password", tint = TextGray) },
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CoralVibrant,
                                unfocusedBorderColor = BorderGreen,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        Button(
                            onClick = {
                                if (adminUsername.isBlank() || adminPassword.isBlank()) {
                                    notificationError = "Vui lòng nhập đầy đủ TK và MK Admin!"
                                } else {
                                    viewModel.verifyAndEnableAdminMode(adminUsername, adminPassword) { success, message ->
                                        if (success) {
                                            notificationMessage = message
                                            adminUsername = ""
                                            adminPassword = ""
                                        } else {
                                            notificationError = message
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = CoralVibrant, contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Login, "Login")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "ĐĂNG NHẬP ADMIN", fontWeight = FontWeight.Black, fontSize = 13.sp, letterSpacing = 0.5.sp)
                        }
                    }
                }
            }
        }

        // 2. Notifications overlay (Success and Failure alerts)
        item {
            AnimatedVisibility(
                visible = notificationMessage != null || notificationError != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (notificationMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(TransparentGreen)
                            .border(1.dp, GlowGreen, RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, "Success", tint = GlowGreen, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = notificationMessage ?: "",
                                color = GlowGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else if (notificationError != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF2D151B))
                            .border(1.dp, CoralVibrant, RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, "Error", tint = CoralVibrant, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = notificationError ?: "",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // ỨNG TUYỂN BAN QUẢN TRỊ / CTV CARD FOR NON-ADMIN USERS
        if (userAccount?.tier != "ADMIN") {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, GlowGreen.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, "Apply", tint = GlowGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ỨNG TUYỂN BAN QUẢN TRỊ / CTV",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Bạn muốn đồng hành phát triển cùng hệ thống? Gửi yêu cầu ứng tuyển bổ nhiệm vai trò đặc quyền tại đây. Yêu cầu của bạn sẽ được chuyển đến Admin tối cao xét duyệt trực tiếp.",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (userAccount?.hasPendingRequest == true) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF2D2715), RoundedCornerShape(10.dp))
                                    .border(BorderStroke(1.dp, Color(0xFFFBBF24)), RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.HourglassEmpty, "Pending", tint = Color(0xFFFBBF24))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "ĐANG CHỜ PHÊ DUYỆT",
                                            color = Color(0xFFFBBF24),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "Vị trí ứng tuyển: ${userAccount?.requestedRoleName}",
                                            color = Color.White,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        } else {
                            var selectedOptionRole by remember { mutableStateOf("STAFF") }
                            val roleOptions = listOf(
                                Triple("STAFF", "STAFF (Nhân viên quản lý)", "Quyền quản lý danh sách rác, được cấp 5,000,000 Coin dùng thử."),
                                Triple("CTV", "CTV (Cộng tác viên)", "Đặc quyền đóng băng sâu cực hạn, dùng thử full VIP 1."),
                                Triple("MANAGER", "MANAGER (Tổng quản trị)", "Đặc quyền toàn diện, được phép phê duyệt tài khoản và xem console.")
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                roleOptions.forEach { (tierName, label, desc) ->
                                    val isSelected = selectedOptionRole == tierName
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) DeepObsidian else Color.White.copy(alpha = 0.02f))
                                            .border(
                                                BorderStroke(
                                                    if (isSelected) 1.5.dp else 0.5.dp,
                                                    if (isSelected) GlowGreen else Color.Gray.copy(alpha = 0.3f)
                                                ),
                                                RoundedCornerShape(10.dp)
                                            )
                                            .clickable { selectedOptionRole = tierName }
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                RadioButton(
                                                    selected = isSelected,
                                                    onClick = { selectedOptionRole = tierName },
                                                    colors = RadioButtonDefaults.colors(selectedColor = GlowGreen, unselectedColor = Color.Gray)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = label,
                                                    color = if (isSelected) GlowGreen else Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                            Text(
                                                text = desc,
                                                color = TextGray,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(start = 36.dp, top = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Button(
                                    onClick = {
                                        val selectedName = when (selectedOptionRole) {
                                            "STAFF" -> "Nhân viên quản lý"
                                            "CTV" -> "Cộng tác viên"
                                            else -> "Tổng quản trị viên"
                                        }
                                        viewModel.submitRoleRequest(selectedOptionRole, selectedName)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GlowGreen, contentColor = DeepObsidian),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("GỬI YÊU CẦU ỨNG TUYỂN NGAY", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Current Membership Status & Balance Panel
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "TRẠNG THÁI THÀNH VIÊN",
                        color = BrightTurquoise,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when (userAccount?.tier) {
                                        "VIP1" -> Icons.Default.OfflineBolt
                                        "VIP2" -> Icons.Default.Stars
                                        "ADMIN" -> Icons.Default.Security
                                        else -> Icons.Default.Lock
                                    },
                                    contentDescription = "Tier Icon",
                                    tint = when (userAccount?.tier) {
                                        "VIP1" -> BrightTurquoise
                                        "VIP2" -> GlowGreen
                                        "ADMIN" -> CoralVibrant
                                        else -> TextGray
                                    },
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when (userAccount?.tier) {
                                        "VIP1" -> "TÀI KHOẢN VIP 1"
                                        "VIP2" -> "SIÊU VIP 2 PRO"
                                        "ADMIN" -> "QUẢN TRỊ VIÊN (ADMIN)"
                                        else -> "MEMBER THƯỜNG (FREE)"
                                    },
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            // Expiry date countdown calculation
                            val expiry = userAccount?.expiryTimestamp ?: 0L
                            val statusText = if (userAccount?.tier == "ADMIN") {
                                "Thời hạn: Vô hạn (Mở khóa vĩnh viễn)"
                            } else if (expiry > System.currentTimeMillis()) {
                                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                "Hết hạn vào: ${sdf.format(Date(expiry))}"
                            } else {
                                "Thời hạn: Chưa gia hạn VIP"
                            }
                            Text(
                                text = statusText,
                                color = TextGray,
                                fontSize = 12.sp
                            )
                        }

                        // Wallet Balance View
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Số dư tài khoản", color = TextGray, fontSize = 11.sp)
                            Text(
                                text = "${df.format(userAccount?.balance ?: 0.0)}đ",
                                color = GlowGreen,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = Color.White.copy(alpha = 0.05f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Active Privileges Bullet List
                    Text(text = "Quyền lợi đang hoạt động:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    val privileges = when (userAccount?.tier) {
                        "VIP1" -> listOf(
                            "Tối ưu độ nhạy màn hình cảm ứng siêu cấp",
                            "Tùy biến DNS Game Server chuyên dụng giảm giật lag",
                            "Dọn dẹp RAM nhanh không cần khởi động lại máy",
                            "Đóng băng tối đa 5 ứng dụng ngầm tự động"
                        )
                        "VIP2" -> listOf(
                            "Mở khóa TOÀN BỘ tính năng VIP 1",
                            "Quyền ĐÓNG BĂNG ỨNG DỤNG HỆ THỐNG & FILE NGUỒN (Frozen)",
                            "AI tự động phân tích và diệt tiến trình rác ngầm tiêu hao RAM",
                            "Tự động giải phóng dung lượng bộ nhớ tạm nâng cao",
                            "Dành riêng cho máy cấu hình yếu chơi game nặng mượt mà"
                        )
                        "ADMIN" -> listOf(
                            "Toàn quyền kiểm soát hệ thống Tool Vip",
                            "Sử dụng miễn phí vĩnh viễn tất cả dịch vụ VIP 1 & VIP 2",
                            "Không giới hạn băng thông tối ưu mạng",
                            "Hỗ trợ đặc quyền 24/7 từ nhà phát triển"
                        )
                        else -> listOf(
                            "Chỉ xem các chỉ số phần cứng GPU/CPU cơ bản",
                            "Chưa thể đóng băng ứng dụng hệ thống rác",
                            "Bị giới hạn tốc độ đường truyền khi chơi game"
                        )
                    }

                    privileges.forEach { priv ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = if (userAccount?.tier != "UNPAID") Icons.Default.Check else Icons.Default.Close,
                                contentDescription = "bullet",
                                tint = if (userAccount?.tier != "UNPAID") GlowGreen else CoralVibrant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = priv, color = TextGray, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // 3.5. ADMIN AI BOT MONITOR CONSOLE (THẢ BOT AI QUÉT GIAO DỊCH)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeepObsidian),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.5.dp, BrightTurquoise)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SmartToy, "AI Admin Bot", tint = GlowGreen, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BOT AI QUẢN TRỊ VIÊN (ĐANG HOẠT ĐỘNG)",
                                color = GlowGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                        
                        // Pulsing green dot
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(GlowGreen)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Bot AI đang 'thả ra' chạy ngầm đối chiếu biến động số dư VietQR hệ thống. Tự động cộng tiền & nâng VIP khi phát hiện nội dung hợp lệ.",
                        color = TextGray,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Simulated Terminal Console Display
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF040D12))
                            .border(1.dp, BorderGreen, RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(aiAdminLogs) { log ->
                                Text(
                                    text = log,
                                    color = if (log.contains("❌") || log.contains("⚠️")) CoralVibrant else if (log.contains("✅") || log.contains("🚀")) GlowGreen else Color.White,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Buttons to control / simulate deposit transactions and show AI processing
                    Text(
                        text = "MÔ PHỎNG KIỂM THỬ HỆ THỐNG AI:",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.simulateBankDeposit(100000.0, "tv ${userAccount?.username ?: "lopte"}", "NGUYEN VAN CUONG")
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B2B28), contentColor = GlowGreen),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text("Duyệt Đuôi tv [${userAccount?.username ?: "lopte"}]", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.simulateBankDeposit(50000.0, "tv lopte_noapp", "TRAN HUY KHANG")
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B1017), contentColor = CoralVibrant),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text("Quét Sai Tên (Hủy)", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3.6. TRANSACTION HISTORY UI (GIAO DIỆN LỊCH SỬ GIAO DỊCH)
        item {
            val isAdmin = userAccount?.tier == "ADMIN"
            val txList = if (isAdmin) allTransactions else userTransactions
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isAdmin) "LỊCH SỬ GIAO DỊCH TOÀN CẦU (ADMIN)" else "LỊCH SỬ GIAO DỊCH CỦA BẠN",
                                color = BrightTurquoise,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = if (isAdmin) "Hiển thị tất cả dòng tiền hệ thống" else "Lịch sử nạp tiền & đăng ký VIP",
                                color = TextGray,
                                fontSize = 10.sp
                            )
                        }
                        
                        if (txList.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.clearAllTransactions() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.DeleteSweep, "Clear History", tint = TextGray, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (txList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.ReceiptLong, "No transaction", tint = TextGray.copy(alpha = 0.5f), modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Chưa có giao dịch phát sinh nào.", color = TextGray, fontSize = 11.sp)
                            }
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            txList.take(15).forEach { tx ->
                                val isPositive = tx.amount >= 0
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(DeepObsidian)
                                        .border(0.5.dp, if (tx.status == "SUCCESS") BorderGreen.copy(alpha = 0.5f) else if (tx.status == "PENDING_AI") Color.Yellow.copy(alpha = 0.5f) else CoralVibrant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(
                                                            when (tx.status) {
                                                                "SUCCESS" -> GlowGreen
                                                                "PENDING_AI" -> Color.Yellow
                                                                else -> CoralVibrant
                                                            }
                                                        )
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = when (tx.type) {
                                                        "DEPOSIT" -> "NẠP TIỀN"
                                                        "UPGRADE_VIP1" -> "NÂNG VIP 1"
                                                        "UPGRADE_VIP2" -> "NÂNG VIP 2"
                                                        else -> "ĐIỀU CHỈNH ADM"
                                                    },
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            
                                            Text(
                                                text = if (isPositive) "+${df.format(tx.amount)}đ" else "${df.format(tx.amount)}đ",
                                                color = if (isPositive) GlowGreen else CoralVibrant,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 12.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = tx.referenceNote,
                                            color = TextGray,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                        
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "User: ${tx.username}",
                                                color = BrightTurquoise,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            
                                            val dateStr = java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault()).format(java.util.Date(tx.timestamp))
                                            Text(
                                                text = dateStr,
                                                color = TextGray,
                                                fontSize = 9.sp
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

        // Promo Code / Giftcode Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderGreen.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MÃ QUÀ TẶNG / GIFTCODE VIP",
                        color = BrightTurquoise,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    if (userAccount?.tier == "VIP1" && (userAccount?.customRole?.contains("Mã") == true)) {
                        Text(
                            text = "Bạn đang kích hoạt đặc quyền VIP 1 bằng Mã Quà Tặng.",
                            color = GlowGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.revokePromoCode() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = CoralVibrant),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Revoke Code")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("HUỶ SỬ DỤNG MÃ (BỎ MÃ)", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text(
                            text = "Nhập Giftcode hoặc mã khuyến mãi của bạn để nhận đặc quyền VIP 1 miễn phí ngay lập tức.",
                            color = TextGray,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = promoInputText,
                                onValueChange = { promoInputText = it.trim().uppercase() },
                                placeholder = { Text("Nhập Giftcode...", color = TextGray, fontSize = 13.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrightTurquoise,
                                    unfocusedBorderColor = BorderGreen.copy(alpha = 0.5f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Button(
                                onClick = {
                                    if (promoInputText.isNotBlank()) {
                                        val success = viewModel.submitPromoCode(promoInputText)
                                        if (success) {
                                            promoInputText = ""
                                        }
                                    } else {
                                        viewModel.showToast("Vui lòng nhập mã quà tặng!")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrightTurquoise, contentColor = DeepObsidian),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .height(50.dp)
                                    .minimumInteractiveComponentSize()
                            ) {
                                Text("ÁP DỤNG", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        
                        if (userAccount?.tier == "VIP2" || userAccount?.tier == "ADMIN") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Lưu ý: Bạn đang có cấp bậc cao hơn VIP 1. Sử dụng mã quà tặng sẽ hạ cấp vai trò của bạn xuống VIP 1.",
                                color = CoralVibrant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // 4. PRICE PLANS SECTION (BẢNG GIÁ KÍCH HOẠT VIP)
        item {
            Text(
                text = "BẢNG GIÁ KÍCH HOẠT VIP HỆ THỐNG",
                color = BrightTurquoise,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
            )
        }

        // VIP 1 CARD
        item {
            val isCurrent = userAccount?.tier == "VIP1"
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(
                            width = if (isCurrent) 2.dp else 1.dp,
                            color = if (isCurrent) BrightTurquoise else BrightTurquoise.copy(alpha = 0.2f)
                        ),
                        RoundedCornerShape(18.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.OfflineBolt, "VIP 1 Icon", tint = BrightTurquoise, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GÓI VIP 1 - CƠ BẢN",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp
                            )
                        }
                        val price1 = userAccount?.vip1Price ?: 50000.0
                        Text(
                            text = "${df.format(price1)}đ / Tháng",
                            color = BrightTurquoise,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Phù hợp để tối ưu chạm, tinh chỉnh kết nối mạng cơ bản và đo đạc phần cứng thời gian thực mượt mà.",
                        color = TextGray,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val balance = userAccount?.balance ?: 0.0
                            val cost = userAccount?.vip1Price ?: 50000.0
                            if (balance >= cost) {
                                viewModel.buyVipTier("VIP1", cost)
                                notificationMessage = "Kích hoạt thành công gói VIP 1! Thiết bị đã được mở khóa đặc quyền VIP1."
                            } else {
                                notificationError = "Số dư không đủ! Cần thêm ${df.format(cost - balance)}đ để kích hoạt."
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCurrent) TransparentGreen else BrightTurquoise,
                            contentColor = if (isCurrent) GlowGreen else DeepObsidian
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = if (isCurrent) Icons.Default.CheckCircle else Icons.Default.OfflineBolt,
                            contentDescription = "Buy"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isCurrent) "ĐANG SỬ DỤNG GÓI VIP 1" else "ĐĂNG KÝ GÓI VIP 1",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // VIP 2 PRO CARD (RECOMMENDED/HOT)
        item {
            val isCurrent = userAccount?.tier == "VIP2"
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(
                            width = 2.dp,
                            color = if (isCurrent) GlowGreen else CoralVibrant
                        ),
                        RoundedCornerShape(20.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Pulsing / glowing ribbon in top right corner
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clip(RoundedCornerShape(bottomStart = 12.dp, topEnd = 18.dp))
                            .background(CoralVibrant)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "KHUYÊN DÙNG",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 9.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(end = 90.dp) // Leave space for tag
                            ) {
                                Icon(Icons.Default.Stars, "VIP 2 Icon", tint = GlowGreen, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "VIP 2 PRO - AI",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp
                                )
                            }
                        }
                        
                        val price2 = userAccount?.vip2Price ?: 120000.0
                        Text(
                            text = "${df.format(price2)}đ / Tháng",
                            color = GlowGreen,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Đầy đủ đặc quyền VIP 1 + ĐÓNG BĂNG ỨNG DỤNG HỆ THỐNG VÀ TIẾN TRÌNH CHẠY NGẦM rác hỗ trợ bởi trí tuệ nhân tạo AI siêu thông minh.",
                            color = TextGray,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val balance = userAccount?.balance ?: 0.0
                                val cost = userAccount?.vip2Price ?: 120000.0
                                if (balance >= cost) {
                                    viewModel.buyVipTier("VIP2", cost)
                                    notificationMessage = "CHÚC MỪNG! Bạn đã nâng cấp thành công lên VIP 2 PRO. Toàn bộ tính năng đã được mở khóa!"
                                } else {
                                    notificationError = "Số dư không đủ! Cần nạp thêm ${df.format(cost - balance)}đ để nâng cấp lên VIP 2 PRO."
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCurrent) TransparentGreen else GlowGreen,
                                contentColor = if (isCurrent) GlowGreen else DeepObsidian
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (isCurrent) Icons.Default.CheckCircle else Icons.Default.FlashOn,
                                contentDescription = "Upgrade"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isCurrent) "ĐANG SỬ DỤNG GÓI VIP 2 PRO" else "ĐĂNG KÝ GÓI VIP 2 PRO",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }

        // 5. WALLET TOP-UP SECTION (NẠP TIỀN VÀO VÍ HỆ THỐNG)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, BrightTurquoise.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "NẠP TIỀN VÀO TÀI KHOẢN",
                        color = BrightTurquoise,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Chọn hoặc nhập số tiền nạp để mô phỏng thanh toán quét mã QR cực tiện lợi:",
                        color = TextGray,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick-choose amounts row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(20000.0, 50000.0, 100000.0, 200000.0, 500000.0).forEach { amount ->
                            val isSelected = selectedAmountByQuick == amount && customAmountText.isEmpty()
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) TransparentGreen else DeepObsidian)
                                    .border(
                                        1.dp,
                                        if (isSelected) BrightTurquoise else Color.White.copy(alpha = 0.05f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        selectedAmountByQuick = amount
                                        customAmountText = ""
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${df.format(amount / 1000)}K",
                                    color = if (isSelected) BrightTurquoise else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Custom amount input field
                    OutlinedTextField(
                        value = customAmountText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                customAmountText = input
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Hoặc nhập số tiền khác...", color = TextGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrightTurquoise,
                            unfocusedBorderColor = BorderGreen,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        leadingIcon = {
                            Icon(Icons.Default.AccountBalanceWallet, "Wallet Icon", tint = BrightTurquoise)
                        },
                        suffix = {
                            Text("VND", color = BrightTurquoise, fontWeight = FontWeight.Bold)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    val activeRechargeAmount = if (customAmountText.isNotEmpty()) {
                        customAmountText.toDoubleOrNull() ?: 0.0
                    } else {
                        selectedAmountByQuick
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (activeRechargeAmount > 0) {
                                lastRechargedAmount = activeRechargeAmount
                                showBankTransferPortal = true
                                notificationMessage = "Đã tạo mã QR nạp tiền thành công! Vui lòng quét mã bên dưới."
                            } else {
                                notificationError = "Vui lòng chọn hoặc nhập số tiền nạp hợp lệ!"
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrightTurquoise,
                            contentColor = DeepObsidian
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, "QR Code Scan")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TẠO QR NẠP ${df.format(activeRechargeAmount)}đ",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        // 6. BANK TRANSFER PORTAL (SIMULATED HIGH-TECH QR CARD)
        item {
            AnimatedVisibility(
                visible = showBankTransferPortal,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(2.dp, BrightTurquoise)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CỔNG GIAO DỊCH CHUYỂN KHOẢN",
                            color = BrightTurquoise,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Vui lòng quét mã VietQR hoặc chuyển khoản đúng nội dung",
                            color = TextGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // High tech Simulated QR Code Canvas!
                        Box(
                            modifier = Modifier
                                .size(210.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .border(4.dp, BrightTurquoise, RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height

                                // Draw QR corner focus boxes
                                val boxSize = 36f
                                val strokeWidth = 8f

                                // Top-left
                                drawRect(Color(0xFF0D2525), Offset(0f, 0f), Size(boxSize, boxSize))
                                drawRect(Color.White, Offset(strokeWidth, strokeWidth), Size(boxSize - strokeWidth * 2, boxSize - strokeWidth * 2))
                                drawRect(Color(0xFF0D2525), Offset(strokeWidth * 1.5f, strokeWidth * 1.5f), Size(boxSize - strokeWidth * 3, boxSize - strokeWidth * 3))

                                // Top-right
                                drawRect(Color(0xFF0D2525), Offset(w - boxSize, 0f), Size(boxSize, boxSize))
                                drawRect(Color.White, Offset(w - boxSize + strokeWidth, strokeWidth), Size(boxSize - strokeWidth * 2, boxSize - strokeWidth * 2))
                                drawRect(Color(0xFF0D2525), Offset(w - boxSize + strokeWidth * 1.5f, strokeWidth * 1.5f), Size(boxSize - strokeWidth * 3, boxSize - strokeWidth * 3))

                                // Bottom-left
                                drawRect(Color(0xFF0D2525), Offset(0f, h - boxSize), Size(boxSize, boxSize))
                                drawRect(Color.White, Offset(strokeWidth, h - boxSize + strokeWidth), Size(boxSize - strokeWidth * 2, boxSize - strokeWidth * 2))
                                drawRect(Color(0xFF0D2525), Offset(strokeWidth * 1.5f, h - boxSize + strokeWidth * 1.5f), Size(boxSize - strokeWidth * 3, boxSize - strokeWidth * 3))

                                // Draw simulated barcode dots/lines
                                for (i in 0..10) {
                                    for (j in 0..10) {
                                        // Skip corners
                                        if (i < 3 && j < 3) continue
                                        if (i > 7 && j < 3) continue
                                        if (i < 3 && j > 7) continue

                                        // Pseudorandom dots
                                        val seed = (i * 31 + j * 17) % 3
                                        if (seed == 0) {
                                            drawRect(
                                                color = Color(0xFF040D12),
                                                topLeft = Offset(i * (w / 10f) + 4f, j * (h / 10f) + 4f),
                                                size = Size((w / 12f), (h / 12f))
                                            )
                                        }
                                    }
                                }

                                // Center TV logo box
                                drawRect(
                                    color = BrightTurquoise,
                                    topLeft = Offset(w / 2 - 20f, h / 2 - 20f),
                                    size = Size(40f, 40f)
                                )
                            }
                            
                            // Center overlay badge
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DeepObsidian),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("TV", color = BrightTurquoise, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Transfer Details Card
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DeepObsidian)
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Ngân hàng:", color = TextGray, fontSize = 12.sp)
                                Text("VietinBank (ICB)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Số tài khoản:", color = TextGray, fontSize = 12.sp)
                                Text("1028 9999 8888", color = BrightTurquoise, fontWeight = FontWeight.Black, fontSize = 13.sp)
                            }
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Chủ tài khoản:", color = TextGray, fontSize = 12.sp)
                                Text("PHAM QUANG HUY", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Số tiền nạp:", color = TextGray, fontSize = 12.sp)
                                Text("${df.format(lastRechargedAmount)} VND", color = GlowGreen, fontWeight = FontWeight.Black, fontSize = 13.sp)
                            }
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Nội dung CK:", color = TextGray, fontSize = 12.sp)
                                Text("TV ${userAccount?.username ?: "quanghuypham"}", color = CoralVibrant, fontWeight = FontWeight.Black, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Primary simulation button: Correct content (auto-approved by AI Bot)
                        Button(
                            onClick = {
                                viewModel.simulateBankDeposit(lastRechargedAmount, "TV ${userAccount?.username ?: "quanghuypham"}", "MÔ PHỎNG SẾP")
                                showBankTransferPortal = false
                                customAmountText = ""
                                notificationMessage = "Đang gửi tín hiệu giao dịch đến AI Admin Bot để phân tích!"
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GlowGreen,
                                contentColor = DeepObsidian
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.SmartToy, "AI Bot Scan")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "ĐÃ CHUYỂN KHOẢN (AI TỰ ĐỘNG PHÂN TÍCH)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Secondary simulation button: Incorrect/Foreign content (AI flags and ignores auto-credit)
                        Button(
                            onClick = {
                                viewModel.simulateBankDeposit(lastRechargedAmount, "Nạp tiền game booster", "KHÁCH VÃNG LAI")
                                showBankTransferPortal = false
                                customAmountText = ""
                                notificationMessage = "Đang gửi giao dịch lỗi cho AI Admin Bot thẩm định!"
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2D151B),
                                contentColor = CoralVibrant
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CoralVibrant)
                        ) {
                            Icon(Icons.Default.Warning, "AI Flag Error")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "MÔ PHỎNG GIAO DỊCH SAI NỘI DUNG (AI SẼ BỎ QUA)", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // 6.5. AI-DRIVEN DAILY FREE VIP 1 PROMO SYSTEM WITH 2 LINK BYPASSES
        item {
            Text(
                text = "MÃ VIP 1 MIỄN PHÍ HÀNG NGÀY (AI HÓA)",
                color = BrightTurquoise,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
            )
        }

        item {
            val link1 = userAccount?.link1Passed ?: false
            val link2 = userAccount?.link2Passed ?: false
            var showLink1Dialog by remember { mutableStateOf(false) }
            var showLink2Dialog by remember { mutableStateOf(false) }
            var codeInputText by remember { mutableStateOf("") }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, BrightTurquoise.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "NHẬN VIP 1 MIỄN PHÍ QUA 2 BƯỚC LINK RÚT GỌN",
                        color = BrightTurquoise,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Vượt qua 2 liên kết tài trợ của nhà phát triển để kích hoạt mô-đun AI tạo Giftcode VIP 1 miễn phí sử dụng trong 24 giờ cực ngon!",
                        color = TextGray,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // STEP 1: Pass 2 Links
                    Text(
                        text = "Bước 1: Vượt qua 2 liên kết tài tài trợ",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Link 1 Button
                        Button(
                            onClick = {
                                if (!link1) {
                                    showLink1Dialog = true
                                } else {
                                    viewModel.showToast("Bạn đã vượt qua Liên kết 1!")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (link1) TransparentGreen else Color(0xFF1E3232),
                                contentColor = if (link1) GlowGreen else BrightTurquoise
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.5.dp, if (link1) GlowGreen else BrightTurquoise.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = if (link1) Icons.Default.CheckCircle else Icons.Default.Link,
                                contentDescription = "Link 1",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Liên kết 1", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        // Link 2 Button
                        Button(
                            onClick = {
                                if (!link2) {
                                    showLink2Dialog = true
                                } else {
                                    viewModel.showToast("Bạn đã vượt qua Liên kết 2!")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (link2) TransparentGreen else Color(0xFF1E3232),
                                contentColor = if (link2) GlowGreen else BrightTurquoise
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.5.dp, if (link2) GlowGreen else BrightTurquoise.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = if (link2) Icons.Default.CheckCircle else Icons.Default.Link,
                                contentDescription = "Link 2",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Liên kết 2", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // STEP 2: Generate code by AI
                    Text(
                        text = "Bước 2: Tạo mã Giftcode bằng Trí tuệ Nhân tạo",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = { viewModel.generateAiPromoCode() },
                        enabled = link1 && link2,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (link1 && link2) BrightTurquoise else Color.DarkGray,
                            contentColor = if (link1 && link2) DeepObsidian else TextGray
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Psychology, "AI psychology")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🤖 AI: TẠO MÃ VIP 1 MIỄN PHÍ", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    if (isGeneratingPromoCode) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BrightTurquoise, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI đang liên lạc với máy chủ để cấp mã...", color = BrightTurquoise, fontSize = 11.sp)
                        }
                    }

                    if (aiPromoCodeMessage.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DeepObsidian, RoundedCornerShape(12.dp))
                                .border(0.5.dp, BorderGreen, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("PHẢN HỒI TỪ AI SYSTEM:", color = GlowGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = aiPromoCodeMessage,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    val codeRegex = "GIFTCODE:\\s*(\\S+)".toRegex()
                                    val match = codeRegex.find(aiPromoCodeMessage)
                                    val extracted = match?.groupValues?.get(1)?.trim() ?: ""
                                    if (extracted.isNotBlank()) {
                                        Button(
                                            onClick = {
                                                codeInputText = extracted
                                                viewModel.showToast("Đã tự động điền mã: $extracted!")
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = GlowGreen, contentColor = DeepObsidian),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                        ) {
                                            Text("SỬ DỤNG MÃ NÀY", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // STEP 3: Submit Promo Code
                    Text(
                        text = "Bước 3: Nhập mã xác nhận nhận quyền VIP 1",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Mẹo: Bạn có thể nhập mã dùng thử VIP 1: VIP1_FREE, VIP1_GIFT, hoặc VIP1 để kích hoạt dùng ngay 30 ngày!",
                        color = GlowGreen,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = codeInputText,
                            onValueChange = { codeInputText = TelexConverter.convert(it) },
                            modifier = Modifier.weight(1.5f),
                            placeholder = { Text("Dán mã quà tặng...", color = TextGray, fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrightTurquoise,
                                unfocusedBorderColor = BorderGreen,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                if (codeInputText.isNotBlank()) {
                                    val ok = viewModel.submitPromoCode(codeInputText)
                                    if (ok) {
                                        codeInputText = ""
                                    }
                                } else {
                                    viewModel.showToast("Vui lòng dán mã trước!")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = BrightTurquoise, contentColor = DeepObsidian),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Text("KÍCH HOẠT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // SIMULATED DIALOGS FOR BYPASS LINKS
            if (showLink1Dialog) {
                var progress by remember { mutableStateOf(0f) }
                var textState by remember { mutableStateOf("Đang chuyển hướng qua link rút gọn 1...") }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(1000)
                    progress = 0.4f
                    textState = "Đang kiểm chứng mã độc & chống virus (Link 1)..."
                    kotlinx.coroutines.delay(1200)
                    progress = 0.8f
                    textState = "Vượt liên kết thành công! Đang kết chuyển mã xác minh AI..."
                    kotlinx.coroutines.delay(1000)
                    progress = 1.0f
                    viewModel.setLinkPassed(1, true)
                    showLink1Dialog = false
                }
                AlertDialog(
                    onDismissRequest = { },
                    containerColor = DeepObsidian,
                    title = { Text("VƯỢT LIÊN KẾT 1 (SIMULATION)", color = BrightTurquoise, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(textState, color = Color.White, fontSize = 11.sp, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth(), color = BrightTurquoise, trackColor = DarkTealCard)
                        }
                    },
                    confirmButton = {}
                )
            }

            if (showLink2Dialog) {
                var progress by remember { mutableStateOf(0f) }
                var textState by remember { mutableStateOf("Đang chuyển hướng qua link rút gọn 2...") }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(1000)
                    progress = 0.3f
                    textState = "Vui lòng đợi 3 giây để xác minh không phải robot..."
                    kotlinx.coroutines.delay(1200)
                    progress = 0.7f
                    textState = "Đang đối sánh chuỗi mã hóa bảo mật MD5..."
                    kotlinx.coroutines.delay(1000)
                    progress = 1.0f
                    viewModel.setLinkPassed(2, true)
                    showLink2Dialog = false
                }
                AlertDialog(
                    onDismissRequest = { },
                    containerColor = DeepObsidian,
                    title = { Text("VƯỢT LIÊN KẾT 2 (SIMULATION)", color = BrightTurquoise, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(textState, color = Color.White, fontSize = 11.sp, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth(), color = BrightTurquoise, trackColor = DarkTealCard)
                        }
                    },
                    confirmButton = {}
                )
            }
        }

        // 7. ADMIN BYPASS PANEL (ONLY FOR TESTING)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CoralVibrant.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "CHẾ ĐỘ QUẢN TRỊ VIÊN (ADMIN BYPASS)",
                            color = CoralVibrant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Icon(Icons.Default.Security, "Security", tint = CoralVibrant, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Nhấn nút dưới để chuyển đổi thiết bị của bạn sang chế độ ADMIN hoàn toàn miễn phí, mở khóa vĩnh viễn không giới hạn tính năng.",
                        color = TextGray,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            viewModel.enableAdminMode()
                            notificationMessage = "Đặc quyền ADMIN kích hoạt! Toàn bộ tính năng cao cấp đã mở khóa miễn phí vĩnh viễn!"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralVibrant, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "KÍCH HOẠT CHẾ ĐỘ ADMIN (FREE)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // 8. AI ASSISTANT CHAT TERMINAL (TRỢ LÝ BOT AI HỖ TRỢ ĐĂNG KÝ)
        item {
            Text(
                text = "TRỢ LÝ BOT AI HỖ TRỢ KÍCH HOẠT",
                color = BrightTurquoise,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Hỏi AI về cách nạp tiền, thông tin các gói cước và tư vấn tối ưu hiệu năng thiết bị của bạn:",
                        color = TextGray,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Chat conversation container
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp)
                            .background(DeepObsidian, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val messages = chatHistory.takeLast(4)
                        if (messages.isEmpty()) {
                            Text(
                                text = "Bot AI: Xin chào sếp! Em là AI Trợ Lý của Tool Vip. Sếp cần tư vấn nạp tiền, nâng cấp gói VIP hay tối ưu RAM đóng băng máy thế nào ạ?",
                                color = TextGray,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        } else {
                            messages.forEach { msg ->
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = if (msg.sender == "USER") Alignment.End else Alignment.Start
                                ) {
                                    Text(
                                        text = if (msg.sender == "USER") "Bạn" else "Bot AI",
                                        color = if (msg.sender == "USER") BrightTurquoise else GlowGreen,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (msg.sender == "USER") Color(0xFF0F3232) else Color(0xFF08251B))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = msg.message,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }

                        if (isAiLoading) {
                            Text(text = "AI đang suy nghĩ...", color = CoralVibrant, fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Input Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = aiChatInput,
                            onValueChange = { aiChatInput = TelexConverter.convert(it) },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Nhập câu hỏi để hỏi Bot AI...", fontSize = 11.sp, color = TextGray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrightTurquoise,
                                unfocusedBorderColor = BorderGreen,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (aiChatInput.isNotBlank()) {
                                    viewModel.sendChatMessage(aiChatInput)
                                    aiChatInput = ""
                                }
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(BrightTurquoise)
                        ) {
                            Icon(Icons.Default.Send, "Send Message", tint = DeepObsidian)
                        }
                    }
                }
            }
        }
    }
}
