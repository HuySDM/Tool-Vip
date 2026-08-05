package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserAccount
import com.example.ui.AppViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userAccount by viewModel.userAccount.collectAsState()
    val allUserAccounts by viewModel.allUserAccounts.collectAsState()
    val pendingRequests by viewModel.pendingRoleRequests.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Duyệt vai trò, 1: Cài đặt hệ thống, 2: Dữ liệu người dùng

    // System states
    var vip1PriceText by remember { mutableStateOf("") }
    var vip2PriceText by remember { mutableStateOf("") }
    var newAdminUser by remember { mutableStateOf("") }
    var newAdminPass by remember { mutableStateOf("") }

    // User data search & edit
    var userSearchQuery by remember { mutableStateOf("") }
    val filteredUsers = remember(allUserAccounts, userSearchQuery) {
        allUserAccounts.filter { 
            it.username.contains(userSearchQuery, ignoreCase = true) || 
            it.email.contains(userSearchQuery, ignoreCase = true) ||
            it.tier.contains(userSearchQuery, ignoreCase = true)
        }
    }

    // AI Password helper states
    var aiPrompt by remember { mutableStateOf("") }
    var aiResponseText by remember { mutableStateOf<String?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        viewModel.loadPendingRoleRequests()
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(DarkGradientStart, Color.Transparent)
                        )
                    )
                    .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(18.dp))
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Trở về", tint = BrightTurquoise)
                    }

                    Column {
                        Text(
                            text = "BẢNG QUẢN TRỊ TỐI CAO",
                            color = BrightTurquoise,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Quản lý hệ thống, vai trò và dữ liệu bảo mật",
                            color = TextGray,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        },
        containerColor = DeepObsidian
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Chips for sections
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    Triple(0, Icons.Default.HowToReg, "Duyệt vai trò"),
                    Triple(1, Icons.Default.Settings, "Hệ thống"),
                    Triple(2, Icons.Default.People, "Dữ liệu"),
                    Triple(3, Icons.Default.ReceiptLong, "Giao dịch"),
                    Triple(4, Icons.Default.Paid, "Cấp tiền")
                ).forEach { (index, icon, label) ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .width(105.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) BrightTurquoise else DarkTealCard)
                            .border(
                                BorderStroke(1.dp, if (isSelected) BrightTurquoise else BorderGreen),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) DeepObsidian else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = label,
                                color = if (isSelected) DeepObsidian else Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> {
                    // TAB: DUYỆT VAI TRÒ
                    Text(
                        text = "DANH SÁCH YÊU CẦU ỨNG TUYỂN BAN QUẢN TRỊ",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (pendingRequests.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(DarkTealCard, RoundedCornerShape(12.dp))
                                .border(BorderStroke(1.dp, BorderGreen), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CheckCircle, "No Requests", tint = GlowGreen, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Sạch sẽ! Không có yêu cầu đang chờ duyệt.",
                                    color = TextGray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            items(pendingRequests) { req ->
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
                                            Text(
                                                text = "@${req.username}",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .background(GlowGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = req.requestedRoleName,
                                                    color = GlowGreen,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Email liên kết: ${req.email.ifBlank { "Chưa liên kết" }}",
                                            color = TextGray,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = "Cấp bậc hiện tại: ${req.tier}",
                                            color = TextGray,
                                            fontSize = 11.sp
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Button(
                                                onClick = { viewModel.approveRoleRequest(req.username) },
                                                colors = ButtonDefaults.buttonColors(containerColor = GlowGreen, contentColor = DeepObsidian),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = "Duyệt", modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("PHÊ DUYỆT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = { viewModel.rejectRoleRequest(req.username) },
                                                colors = ButtonDefaults.buttonColors(containerColor = CoralVibrant, contentColor = Color.White),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "Từ chối", modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("TỪ CHỐI", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB: CÀI ĐẶT HỆ THỐNG
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                text = "BẢNG GIÁ ĐĂNG KÝ HỆ THỐNG",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, BorderGreen)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Thay đổi giá đăng ký dịch vụ của hệ thống (Đơn vị: VNĐ)",
                                        color = TextGray,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = vip1PriceText,
                                            onValueChange = { if (it.all { c -> c.isDigit() }) vip1PriceText = it },
                                            placeholder = { Text("Đơn vị: VNĐ", color = TextGray, fontSize = 10.sp) },
                                            label = { Text("Giá VIP 1 mới", color = TextGray, fontSize = 10.sp) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = BrightTurquoise,
                                                unfocusedBorderColor = BorderGreen,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                        )

                                        OutlinedTextField(
                                            value = vip2PriceText,
                                            onValueChange = { if (it.all { c -> c.isDigit() }) vip2PriceText = it },
                                            placeholder = { Text("Đơn vị: VNĐ", color = TextGray, fontSize = 10.sp) },
                                            label = { Text("Giá VIP 2 mới", color = TextGray, fontSize = 10.sp) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = BrightTurquoise,
                                                unfocusedBorderColor = BorderGreen,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            val p1 = vip1PriceText.toDoubleOrNull() ?: (userAccount?.vip1Price ?: 50000.0)
                                            val p2 = vip2PriceText.toDoubleOrNull() ?: (userAccount?.vip2Price ?: 120000.0)
                                            viewModel.updateVipPrices(p1, p2)
                                            vip1PriceText = ""
                                            vip2PriceText = ""
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrightTurquoise, contentColor = DeepObsidian),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Save, contentDescription = "Lưu", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("CẬP NHẬT GIÁ DỊCH VỤ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "TÀI KHOẢN ADMIN TỐI CAO",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, BorderGreen)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Cài đặt tài khoản & mật khẩu đăng nhập của Admin hệ thống",
                                        color = TextGray,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = newAdminUser,
                                        onValueChange = { newAdminUser = it },
                                        label = { Text("Tên tài khoản Admin mới", color = TextGray, fontSize = 10.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = BrightTurquoise,
                                            unfocusedBorderColor = BorderGreen,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = newAdminPass,
                                        onValueChange = { newAdminPass = it },
                                        label = { Text("Mật khẩu Admin mới", color = TextGray, fontSize = 10.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = BrightTurquoise,
                                            unfocusedBorderColor = BorderGreen,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            viewModel.updateAdminCredentials(newAdminUser, newAdminPass) { success, msg ->
                                                viewModel.showToast(msg)
                                                if (success) {
                                                    newAdminUser = ""
                                                    newAdminPass = ""
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CoralVibrant, contentColor = Color.White),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("LƯU TÀI KHOẢN ADMIN", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB: TRUY CẬP DỮ LIỆU NGƯỜI DÙNG
                    val isLoggedInAdmin = userAccount?.tier == "ADMIN"
                    val isAuthorizedManager = userAccount?.isUserDataAuthorized == true

                    if (!isLoggedInAdmin && !isAuthorizedManager) {
                        // Secure locked screen
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkTealCard, RoundedCornerShape(14.dp))
                                .border(BorderStroke(1.dp, CoralVibrant.copy(alpha = 0.5f)), RoundedCornerShape(14.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Lock, "Locked", tint = CoralVibrant, modifier = Modifier.size(56.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "TRUY CẬP BỊ TỪ CHỐI",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Bạn chưa được Admin phê duyệt quyền kiểm tra dữ liệu người dùng của hệ thống.\n\nVui lòng yêu cầu Admin tối cao phê duyệt tính năng này trong danh sách quản lý mới có thể truy cập kiểm tra.",
                                    color = TextGray,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    } else {
                        // AUTHORIZED VIEW (USER DATA + PASSWORD BOT)
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Part A: AI Password Assistant
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = DeepObsidian),
                                border = BorderStroke(1.dp, BorderGreen),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.SmartToy, "Bot AI", tint = BrightTurquoise)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "BOT AI TRỢ GIÚP MẬT KHẨU",
                                            color = BrightTurquoise,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Nhờ bot đổi mật khẩu, tìm mật khẩu hoặc cấp mk tạm thời.",
                                        color = TextGray,
                                        fontSize = 10.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = aiPrompt,
                                        onValueChange = { aiPrompt = it },
                                        placeholder = { Text("Ví dụ: 'Cấp mật khẩu mới là 123456 cho quanghuy'", color = TextGray, fontSize = 11.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = BrightTurquoise,
                                            unfocusedBorderColor = BorderGreen,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = {
                                            if (aiPrompt.isNotBlank()) {
                                                keyboardController?.hide()
                                                viewModel.askAiToManagePasswords(aiPrompt) { response ->
                                                    aiResponseText = response
                                                    aiPrompt = ""
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrightTurquoise, contentColor = DeepObsidian),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !isAiLoading
                                    ) {
                                        if (isAiLoading) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = DeepObsidian)
                                        } else {
                                            Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("AI XỬ LÝ LỆNH", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Display AI Response inside bubble if exists
                                    aiResponseText?.let { resp ->
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(DarkNavBackground, RoundedCornerShape(8.dp))
                                                .border(BorderStroke(0.5.dp, BorderGreen.copy(alpha = 0.5f)), RoundedCornerShape(8.dp))
                                                .padding(10.dp)
                                        ) {
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.SmartToy, null, tint = GlowGreen, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("AI Phản Hồi:", color = GlowGreen, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = resp,
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    lineHeight = 16.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Search user accounts
                            OutlinedTextField(
                                value = userSearchQuery,
                                onValueChange = { userSearchQuery = it },
                                placeholder = { Text("Tìm kiếm tài khoản, email...", color = TextGray, fontSize = 11.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrightTurquoise,
                                    unfocusedBorderColor = BorderGreen,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Search, null, tint = TextGray) }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // List of all user accounts
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredUsers) { user ->
                                    // Skip current user to avoid self modification
                                    UserAccountCard(
                                        user = user,
                                        isCurrentUserAdmin = isLoggedInAdmin,
                                        onToggleAuth = { authorized ->
                                            viewModel.setUserUserDataAuthorized(user.username, authorized)
                                        },
                                        onUpdatePassword = { targetPass ->
                                            viewModel.modifyUserPasswordDirect(user.username, targetPass)
                                        },
                                        onAdjustBalance = { amt, reason ->
                                            viewModel.adjustUserBalanceDirect(user.username, amt, reason)
                                        },
                                        onAdjustTier = { newTier ->
                                            viewModel.adjustUserTierDirect(user.username, newTier)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // TAB: QUẢN LÝ GIAO DỊCH & DUYỆT THANH TOÁN
                    val allTransactions by viewModel.allTransactions.collectAsState()
                    var transSearchQuery by remember { mutableStateOf("") }
                    val filteredTrans = remember(allTransactions, transSearchQuery) {
                        allTransactions.filter {
                            it.username.contains(transSearchQuery, ignoreCase = true) ||
                            it.type.contains(transSearchQuery, ignoreCase = true) ||
                            it.referenceNote.contains(transSearchQuery, ignoreCase = true)
                        }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "DANH SÁCH LỊCH SỬ GIAO DỊCH TOÀN HỆ THỐNG",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Giám sát nạp tiền ngân hàng VietQR, điều chỉnh số dư và thanh toán VIP của người dùng.",
                            color = TextGray,
                            fontSize = 10.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = transSearchQuery,
                            onValueChange = { transSearchQuery = it },
                            placeholder = { Text("Tìm tên tài khoản, nội dung, loại giao dịch...", color = TextGray, fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrightTurquoise,
                                unfocusedBorderColor = BorderGreen,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = TextGray) }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (filteredTrans.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .background(DarkTealCard, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Không tìm thấy giao dịch nào trùng khớp.", color = TextGray, fontSize = 12.sp)
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(filteredTrans.reversed()) { trans ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, if (trans.status == "PENDING_AI") CoralVibrant else BorderGreen.copy(alpha = 0.5f))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        text = "Giao dịch #${trans.id} - @${trans.username}",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp
                                                    )
                                                    Text(
                                                        text = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(trans.timestamp),
                                                        color = TextGray,
                                                        fontSize = 10.sp
                                                    )
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .background(
                                                            when (trans.status) {
                                                                "SUCCESS" -> GlowGreen.copy(alpha = 0.15f)
                                                                "PENDING_AI" -> CoralVibrant.copy(alpha = 0.15f)
                                                                else -> Color.White.copy(alpha = 0.1f)
                                                            },
                                                            RoundedCornerShape(4.dp)
                                                        )
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = when (trans.status) {
                                                            "SUCCESS" -> "THÀNH CÔNG"
                                                            "PENDING_AI" -> "CHỜ DUYỆT"
                                                            else -> "THẤT BẠI"
                                                        },
                                                        color = when (trans.status) {
                                                            "SUCCESS" -> GlowGreen
                                                            "PENDING_AI" -> CoralVibrant
                                                            else -> Color.White
                                                        },
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        val isAdd = trans.amount >= 0
                                                        Text(
                                                            text = if (isAdd) "CỘNG TIỀN: " else "TRỪ TIỀN: ",
                                                            color = TextGray,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Text(
                                                            text = "${if (isAdd) "+" else ""}${java.text.DecimalFormat("#,###").format(trans.amount)}đ",
                                                            color = if (isAdd) GlowGreen else CoralVibrant,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp
                                                        )
                                                    }
                                                    Text(
                                                        text = "Loại: ${trans.type}",
                                                        color = TextGray,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Text(
                                                text = "Chi tiết: ${trans.referenceNote}",
                                                color = Color.White.copy(alpha = 0.8f),
                                                fontSize = 11.sp,
                                                lineHeight = 16.sp
                                            )

                                            // Action Buttons for Verification if PENDING_AI
                                            if (trans.status == "PENDING_AI") {
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Button(
                                                        onClick = { viewModel.rejectTransactionManual(trans.id) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                                        shape = RoundedCornerShape(6.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text("TỪ CHỐI", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }

                                                    Button(
                                                        onClick = { viewModel.approveTransactionManual(trans.id) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = CoralVibrant),
                                                        shape = RoundedCornerShape(6.dp),
                                                        modifier = Modifier.weight(1.2f)
                                                    ) {
                                                        Text("PHÊ DUYỆT", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                4 -> {
                    // TAB: PHÂN QUYỀN TIỀN & ĐIỀU CHỈNH SỐ DƯ (RBAC)
                    val isLoggedInAdmin = userAccount?.tier == "ADMIN"
                    
                    var adminSelfBalanceText by remember { mutableStateOf("") }
                    
                    // Selected Staff State
                    var selectedStaffUsername by remember { mutableStateOf("") }
                    var staffAdjustmentAmountText by remember { mutableStateOf("") }
                    
                    // Selected User State (User search ID)
                    var userSearchIdQuery by remember { mutableStateOf("") }
                    var selectedUserUsername by remember { mutableStateOf("") }
                    var userAdjustmentAmountText by remember { mutableStateOf("") }

                    // Filter all accounts based on tiers
                    val staffAccounts = remember(allUserAccounts) {
                        allUserAccounts.filter { it.tier == "STAFF" }
                    }
                    val userAccounts = remember(allUserAccounts, userSearchIdQuery) {
                        allUserAccounts.filter { 
                            it.tier != "ADMIN" && it.tier != "STAFF" &&
                            (userSearchIdQuery.isEmpty() || it.username.contains(userSearchIdQuery, ignoreCase = true))
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "PHÂN QUYỀN CHI PHỐI TIỀN & ĐIỀU CHỈNH SỐ DƯ (RBAC)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Hệ thống kiểm soát tài chính tối mật phân định theo quyền hạn: Admin, Nhân viên và Người dùng.",
                            color = TextGray,
                            fontSize = 10.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // --- 1. PHẦN DÀNH CHO ADMIN TỰ CHỈNH TIỀN MÌNH ---
                        if (isLoggedInAdmin) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                                border = BorderStroke(1.2.dp, BrightTurquoise),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AdminPanelSettings, "Admin Self", tint = BrightTurquoise)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "ADMIN: ĐIỀU CHỈNH SỐ DƯ BẢN THÂN",
                                            color = BrightTurquoise,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Số dư hiện tại:",
                                            color = TextGray,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = "${String.format("%,.0f", userAccount?.balance ?: 0.0)} VNĐ",
                                            color = GlowGreen,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = adminSelfBalanceText,
                                            onValueChange = { adminSelfBalanceText = it },
                                            placeholder = { Text("Nhập số tiền muốn đặt...", color = TextGray, fontSize = 11.sp) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = BrightTurquoise,
                                                unfocusedBorderColor = BorderGreen,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            singleLine = true,
                                            modifier = Modifier.weight(1.5f),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                        )

                                        Button(
                                            onClick = {
                                                val amt = adminSelfBalanceText.toDoubleOrNull()
                                                if (amt != null && amt >= 0) {
                                                    viewModel.setUserBalanceDirect(userAccount?.username ?: "quanghuy", amt)
                                                    viewModel.showToast("Đã điều chỉnh số dư của bản thân thành ${String.format("%,.0f", amt)}đ")
                                                    adminSelfBalanceText = ""
                                                } else {
                                                    viewModel.showToast("Số tiền không hợp lệ!")
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = BrightTurquoise),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("THIẾT LẬP", fontSize = 11.sp, color = DeepObsidian, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // --- 2. BẢNG TẦN NHÂN VIÊN (STAFF MANAGEMENT TABLE) ---
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                            border = BorderStroke(1.dp, BorderGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Badge, "Staff Management", tint = BrightTurquoise)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "BẢNG TẦN NHÂN VIÊN & CẤP TIỀN",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }

                                Text(
                                    text = "Danh sách các tài khoản nhân viên (STAFF). Kích chọn bất kỳ để thiết lập số dư.",
                                    color = TextGray,
                                    fontSize = 10.sp
                                )

                                // Simple grid or list for Staff
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(BorderStroke(0.5.dp, BorderGreen.copy(alpha = 0.5f)), RoundedCornerShape(8.dp))
                                        .background(Color.Black.copy(alpha = 0.3f))
                                        .padding(4.dp)
                                ) {
                                    if (staffAccounts.isEmpty()) {
                                        Text(
                                            text = "Không có tài khoản nhân viên nào.",
                                            color = TextGray,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    } else {
                                        staffAccounts.forEach { staff ->
                                            val isSelected = selectedStaffUsername == staff.username
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isSelected) BorderGreen.copy(alpha = 0.25f) else Color.Transparent)
                                                    .clickable { selectedStaffUsername = staff.username }
                                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(text = "@${staff.username}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    Text(text = "Vai trò: ${staff.customRole ?: "Nhân viên"}", color = TextGray, fontSize = 9.sp)
                                                }
                                                Text(
                                                    text = "${String.format("%,.0f", staff.balance)} VNĐ",
                                                    color = if (isSelected) BrightTurquoise else GlowGreen,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                if (selectedStaffUsername.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = staffAdjustmentAmountText,
                                            onValueChange = { staffAdjustmentAmountText = it },
                                            placeholder = { Text("Nhập tiền cho @$selectedStaffUsername...", color = TextGray, fontSize = 11.sp) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = BrightTurquoise,
                                                unfocusedBorderColor = BorderGreen,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            singleLine = true,
                                            modifier = Modifier.weight(1.5f),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                        )

                                        Button(
                                            onClick = {
                                                val amt = staffAdjustmentAmountText.toDoubleOrNull()
                                                if (amt != null && amt >= 0) {
                                                    viewModel.setUserBalanceDirect(selectedStaffUsername, amt)
                                                    viewModel.showToast("Đã đặt số dư cho @$selectedStaffUsername thành ${String.format("%,.0f", amt)}đ")
                                                    staffAdjustmentAmountText = ""
                                                    selectedStaffUsername = ""
                                                } else {
                                                    viewModel.showToast("Số tiền không hợp lệ!")
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = BorderGreen),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("CẤP TIỀN", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // --- 3. BẢNG TẦN NGƯỜI DÙNG & TÌM KIẾM THEO ID (USER BY ID MANAGE) ---
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                            border = BorderStroke(1.dp, BorderGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PeopleOutline, "User ID Manual Setup", tint = BrightTurquoise)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "BẢNG NGƯỜI DÙNG & TÌM THEO ID",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }

                                Text(
                                    text = "Tính bằng tên ID (Username) để bỏ tiền thủ công nếu AI không nhận diện tự động được.",
                                    color = TextGray,
                                    fontSize = 10.sp
                                )

                                // Search Field
                                OutlinedTextField(
                                    value = userSearchIdQuery,
                                    onValueChange = { userSearchIdQuery = it },
                                    placeholder = { Text("Nhập ID (Tên tài khoản) cần tìm...", color = TextGray, fontSize = 11.sp) },
                                    leadingIcon = { Icon(Icons.Default.Search, null, tint = TextGray, modifier = Modifier.size(16.dp)) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BrightTurquoise,
                                        unfocusedBorderColor = BorderGreen,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // User Table Scroll Box
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 180.dp)
                                        .border(BorderStroke(0.5.dp, BorderGreen.copy(alpha = 0.5f)), RoundedCornerShape(8.dp))
                                        .background(Color.Black.copy(alpha = 0.3f))
                                        .verticalScroll(rememberScrollState())
                                        .padding(4.dp)
                                ) {
                                    if (userAccounts.isEmpty()) {
                                        Text(
                                            text = "Không tìm thấy người dùng nào trùng khớp ID.",
                                            color = TextGray,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    } else {
                                        userAccounts.forEach { usr ->
                                            val isSelected = selectedUserUsername == usr.username
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isSelected) BorderGreen.copy(alpha = 0.25f) else Color.Transparent)
                                                    .clickable { selectedUserUsername = usr.username }
                                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(text = usr.username, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    Text(text = "Cấp bậc: ${usr.tier}", color = TextGray, fontSize = 9.sp)
                                                }
                                                Text(
                                                    text = "${String.format("%,.0f", usr.balance)} VNĐ",
                                                    color = if (isSelected) BrightTurquoise else GlowGreen,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                if (selectedUserUsername.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = userAdjustmentAmountText,
                                            onValueChange = { userAdjustmentAmountText = it },
                                            placeholder = { Text("Số tiền nạp cho ID @$selectedUserUsername...", color = TextGray, fontSize = 11.sp) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = BrightTurquoise,
                                                unfocusedBorderColor = BorderGreen,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            singleLine = true,
                                            modifier = Modifier.weight(1.5f),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                        )

                                        Button(
                                            onClick = {
                                                val amt = userAdjustmentAmountText.toDoubleOrNull()
                                                if (amt != null && amt >= 0) {
                                                    viewModel.setUserBalanceDirect(selectedUserUsername, amt)
                                                    viewModel.showToast("Nạp tiền thủ công cho ID @$selectedUserUsername thành công: ${String.format("%,.0f", amt)} VNĐ")
                                                    userAdjustmentAmountText = ""
                                                    selectedUserUsername = ""
                                                } else {
                                                    viewModel.showToast("Số tiền không hợp lệ!")
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = BrightTurquoise),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("NẠP TIỀN", fontSize = 11.sp, color = DeepObsidian, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserAccountCard(
    user: UserAccount,
    isCurrentUserAdmin: Boolean,
    onToggleAuth: (Boolean) -> Unit,
    onUpdatePassword: (String) -> Unit,
    onAdjustBalance: (Double, String) -> Unit,
    onAdjustTier: (String) -> Unit
) {
    var editPassVal by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkTealCard),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.5.dp, BorderGreen.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "@${user.username}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Email: ${user.email.ifBlank { "Không có" }}",
                        color = TextGray,
                        fontSize = 10.sp
                    )
                }

                // Tier label
                Box(
                    modifier = Modifier
                        .background(
                            when (user.tier) {
                                "ADMIN" -> CoralVibrant.copy(alpha = 0.2f)
                                "VIP2" -> GlowGreen.copy(alpha = 0.2f)
                                "VIP1" -> BrightTurquoise.copy(alpha = 0.2f)
                                else -> Color.White.copy(alpha = 0.1f)
                            },
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = user.tier,
                        color = when (user.tier) {
                            "ADMIN" -> CoralVibrant
                            "VIP2" -> GlowGreen
                            "VIP1" -> BrightTurquoise
                            else -> Color.White
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Show password plaintext
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VpnKey, "Key", tint = GlowGreen, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Mật khẩu: ",
                    color = TextGray,
                    fontSize = 11.sp
                )
                Text(
                    text = user.passwordHash,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Admin configuration toggles for this user
            if (isCurrentUserAdmin && user.tier != "ADMIN") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DeepObsidian.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (user.isUserDataAuthorized) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = "User Access Auth",
                            tint = if (user.isUserDataAuthorized) GlowGreen else CoralVibrant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cho phép kiểm tra dữ liệu",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Switch(
                        checked = user.isUserDataAuthorized,
                        onCheckedChange = onToggleAuth,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DeepObsidian,
                            checkedTrackColor = BrightTurquoise
                        ),
                        modifier = Modifier.scale(0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            } else if (user.tier != "ADMIN") {
                // For authorized managers, display whether they can check but read-only
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (user.isUserDataAuthorized) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (user.isUserDataAuthorized) GlowGreen else TextGray,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (user.isUserDataAuthorized) "Tài khoản có quyền kiểm tra dữ liệu" else "Tài khoản không có quyền kiểm tra dữ liệu",
                        color = if (user.isUserDataAuthorized) GlowGreen else TextGray,
                        fontSize = 9.sp
                    )
                }
            }

            // Quick Password reset fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = editPassVal,
                    onValueChange = { editPassVal = it },
                    placeholder = { Text("Mật khẩu mới...", color = TextGray, fontSize = 10.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrightTurquoise,
                        unfocusedBorderColor = BorderGreen,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(6.dp),
                    singleLine = true,
                    modifier = Modifier.weight(1.5f)
                )

                Button(
                    onClick = {
                        if (editPassVal.isNotBlank()) {
                            onUpdatePassword(editPassVal)
                            editPassVal = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralVibrant, contentColor = Color.White),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("ĐỔI MK", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            var showAdjustSection by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Số dư: ${java.text.DecimalFormat("#,###").format(user.balance)} VNĐ",
                    color = BrightTurquoise,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )

                Button(
                    onClick = { showAdjustSection = !showAdjustSection },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showAdjustSection) BrightTurquoise else Color.White.copy(alpha = 0.05f),
                        contentColor = if (showAdjustSection) DeepObsidian else Color.White
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (showAdjustSection) "ĐÓNG LẠI" else "ĐIỀU CHỈNH", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            AnimatedVisibility(visible = showAdjustSection) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(DeepObsidian.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .border(BorderStroke(0.5.dp, BorderGreen.copy(alpha = 0.3f)), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "1. ĐIỀU CHỈNH SỐ DƯ (NHẬP SỐ DƯƠNG HOẶC ÂM)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    var adjustAmountText by remember { mutableStateOf("") }
                    var adjustReasonText by remember { mutableStateOf("") }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = adjustAmountText,
                            onValueChange = { adjustAmountText = it },
                            placeholder = { Text("Ví dụ: 100000 hoặc -50000", color = TextGray, fontSize = 10.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = BrightTurquoise,
                                unfocusedBorderColor = BorderGreen
                            ),
                            shape = RoundedCornerShape(6.dp),
                            singleLine = true,
                            modifier = Modifier.weight(1.5f)
                        )

                        Button(
                            onClick = {
                                val amt = adjustAmountText.toDoubleOrNull()
                                if (amt != null) {
                                    onAdjustBalance(amt, adjustReasonText.ifBlank { "Điều chỉnh thủ công bởi Admin" })
                                    adjustAmountText = ""
                                    adjustReasonText = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CoralVibrant),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("CẬP NHẬT", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = adjustReasonText,
                        onValueChange = { adjustReasonText = it },
                        placeholder = { Text("Lý do điều chỉnh (Ví dụ: Khuyến mãi, hoàn phí...)", color = TextGray, fontSize = 10.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = BrightTurquoise,
                            unfocusedBorderColor = BorderGreen
                        ),
                        shape = RoundedCornerShape(6.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "2. THAY ĐỔI CẤP BẬC TÀI KHOẢN TRỰC TIẾP",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Tier chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("UNPAID", "VIP1", "VIP2", "STAFF", "MANAGER", "ADMIN").forEach { tier ->
                            val isSelected = user.tier == tier
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) BrightTurquoise else Color.White.copy(alpha = 0.05f))
                                    .clickable { onAdjustTier(tier) }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tier,
                                    color = if (isSelected) DeepObsidian else Color.White,
                                    fontSize = 8.sp,
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

// Simple modifier scale extensions for Composable scale
@Composable
fun Modifier.scale(scale: Float): Modifier = this.then(
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout((placeable.width * scale).toInt(), (placeable.height * scale).toInt()) {
            placeable.placeRelative(0, 0)
        }
    }
)
