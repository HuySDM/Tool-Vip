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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    Triple(0, Icons.Default.HowToReg, "Duyệt vai trò"),
                    Triple(1, Icons.Default.Settings, "Hệ thống"),
                    Triple(2, Icons.Default.People, "Dữ liệu người dùng")
                ).forEach { (index, icon, label) ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
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
                                        }
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

@Composable
fun UserAccountCard(
    user: UserAccount,
    isCurrentUserAdmin: Boolean,
    onToggleAuth: (Boolean) -> Unit,
    onUpdatePassword: (String) -> Unit
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
