package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.SimulatedNotificationBar
import com.example.ui.screens.*
import com.example.ui.theme.*

// Nav destinations
const val ROUTE_BOOSTER = "booster"
const val ROUTE_FREEZER = "freezer"
const val ROUTE_SHOP = "shop"
const val ROUTE_AI = "ai_bot"
const val ROUTE_RECHARGE = "recharge"
const val ROUTE_SETTINGS = "settings"
const val ROUTE_ADMIN_PANEL = "admin_panel"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolVipApp() {
    val viewModel: AppViewModel = viewModel()
    val themeMode by viewModel.themeMode.collectAsState()
    val systemInDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDarkMode = remember(themeMode, systemInDark) {
        when (themeMode) {
            "DARK" -> true
            "LIGHT" -> false
            else -> systemInDark
        }
    }

    // Keep the viewModel's isDarkMode updated in case other screens collect it
    LaunchedEffect(isDarkMode) {
        viewModel.setResolvedDarkMode(isDarkMode)
    }

    MyApplicationTheme(isDarkTheme = isDarkMode) {
        val userAccount by viewModel.userAccount.collectAsState()
        val currentUsername by viewModel.currentUsername.collectAsState()
        val themeStyle by viewModel.themeStyle.collectAsState()
        val activeColors = remember(themeStyle, isDarkMode) { ThemeColors.getColors(themeStyle, isDarkMode) }

    var currentScreen by remember { mutableStateOf(ROUTE_BOOSTER) }
    var activeBoosterTab by remember { mutableStateOf(0) }
    var showUpgradePromptForFreezer by remember { mutableStateOf(false) }

    // --- RE-AUTHENTICATION / APP LOCK SYSTEM ---
    val lifecycleOwner = LocalLifecycleOwner.current
    var isAppLocked by remember { mutableStateOf(false) }

    val isPrivilegedUser = remember(userAccount) {
        val tier = userAccount?.tier ?: ""
        tier == "ADMIN" || tier == "MANAGER" || tier == "STAFF" || tier == "CTV"
    }

    // Lock app on launch disabled as requested - users only authenticate once at login
    LaunchedEffect(userAccount) {
        isAppLocked = false
    }

    // Force re-auth disabled as requested - users only authenticate once at login
    DisposableEffect(lifecycleOwner, currentUsername) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_STOP) {
                // Do not lock the app again
                isAppLocked = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (currentUsername == null) {
        LoginScreen(
            viewModel = viewModel,
            onLoginSuccess = { currentScreen = ROUTE_BOOSTER }
        )
    } else if (isAppLocked && isPrivilegedUser) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(activeColors.background),
            contentAlignment = Alignment.Center
        ) {
            var lockPassword by remember { mutableStateOf("") }
            var isPinVisible by remember { mutableStateOf(false) }

            // Recovery states inside Lock Screen
            var showRecoveryInLock by remember { mutableStateOf(false) }
            var lockOtpState by remember { mutableStateOf("") }
            var lockNewPinState by remember { mutableStateOf("") }
            var isLockNewPinVisible by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .border(BorderStroke(1.dp, CoralVibrant), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(CoralVibrant.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "App Locked",
                            tint = CoralVibrant,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "XÁC THỰC BẢO MẬT 2 LỚP (2FA)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Tài khoản [${userAccount?.username}] yêu cầu mã xác thực bảo mật để truy cập ứng dụng.",
                        color = TextGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (!showRecoveryInLock) {
                        OutlinedTextField(
                            value = lockPassword,
                            onValueChange = { lockPassword = it },
                            placeholder = { Text("Nhập mã xác thực (PIN)...", color = TextGray, fontSize = 12.sp) },
                            visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = CoralVibrant,
                                unfocusedBorderColor = BorderGreen
                            ),
                            trailingIcon = {
                                IconButton(onClick = { isPinVisible = !isPinVisible }) {
                                    Icon(
                                        imageVector = if (isPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle PIN",
                                        tint = TextGray
                                    )
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.logout()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("ĐĂNG XUẤT", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    if (lockPassword.isNotBlank()) {
                                        viewModel.verifyCurrentUserAuthPin(lockPassword) { success ->
                                            if (success) {
                                                isAppLocked = false
                                            } else {
                                                viewModel.showToast("Mã xác thực 2 lớp sai! Vui lòng kiểm tra lại.")
                                            }
                                        }
                                    } else {
                                        viewModel.showToast("Vui lòng nhập mã xác thực.")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CoralVibrant),
                                modifier = Modifier.weight(1.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("XÁC THỰC", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Quên mã xác thực? Bấm vào đây để khôi phục qua email",
                            color = BrightTurquoise,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    viewModel.requestForgotPinRecovery { success, msg ->
                                        viewModel.showToast(msg)
                                        if (success) {
                                            showRecoveryInLock = true
                                        }
                                    }
                                }
                                .padding(4.dp)
                        )
                    } else {
                        // OTP recovery input directly on lock screen
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "KHÔI PHỤC QUA EMAIL",
                                color = BrightTurquoise,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = lockOtpState,
                                onValueChange = { lockOtpState = it },
                                label = { Text("Mã OTP nhận qua Email (Ví dụ: 123456)", color = TextGray, fontSize = 10.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = BrightTurquoise,
                                    unfocusedBorderColor = BorderGreen
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = lockNewPinState,
                                onValueChange = { lockNewPinState = it },
                                label = { Text("Mã xác thực mới muốn đặt", color = TextGray, fontSize = 10.sp) },
                                visualTransformation = if (isLockNewPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = BrightTurquoise,
                                    unfocusedBorderColor = BorderGreen
                                ),
                                trailingIcon = {
                                    IconButton(onClick = { isLockNewPinVisible = !isLockNewPinVisible }) {
                                        Icon(
                                            imageVector = if (isLockNewPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle",
                                            tint = TextGray
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "( Lưu ý: Không nên để mã xác thực là MK của TK )",
                                color = Color.Red,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { showRecoveryInLock = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("QUAY LẠI", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        viewModel.verifyRecoveryCodeAndResetPin(lockOtpState, lockNewPinState) { success, msg ->
                                            viewModel.showToast(msg)
                                            if (success) {
                                                lockOtpState = ""
                                                lockNewPinState = ""
                                                showRecoveryInLock = false
                                                isAppLocked = false // automatically unlock since they just successfully reset and authenticated
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrightTurquoise, contentColor = DeepObsidian),
                                    modifier = Modifier.weight(1.3f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("ĐỔI & MỞ KHÓA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        val isUnpaid = userAccount == null || userAccount?.tier == "UNPAID" || userAccount?.tier == "EXPIRED" || userAccount?.tier == "Expired"

        Scaffold(
        topBar = {
            Column {
                // System notification bar drawer
                SimulatedNotificationBar(viewModel = viewModel)

                // Sophisticated Dark gradient top bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(activeColors.background, Color.Transparent)
                            )
                        )
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                if (!isUnpaid) {
                                    currentScreen = ROUTE_RECHARGE
                                }
                            }
                        ) {
                            // Custom TV Logo with gradient & subtle glow border
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(activeColors.primary, activeColors.border)
                                        )
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = activeColors.primary.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "TV",
                                    color = activeColors.background,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Tool Vip",
                                        fontWeight = FontWeight.Bold,
                                        color = activeColors.primary,
                                        fontSize = 18.sp,
                                        letterSpacing = (-0.5).sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                when (userAccount?.tier) {
                                                    "VIP1" -> activeColors.primary
                                                    "VIP2" -> activeColors.secondary
                                                    "ADMIN" -> activeColors.tertiary
                                                    else -> Color.DarkGray
                                                }
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = userAccount?.tier ?: "LƯU TRỮ",
                                            color = if (userAccount?.tier == "ADMIN") Color.White else activeColors.background,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    // Pulsing indicator style dot
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(GlowGreen)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "SYSTEM ACTIVE • 64BIT",
                                        color = TextGray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }

                        // Top header action icons & Wallet balance indicator
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!isUnpaid) {
                                val df = java.text.DecimalFormat("#,###")
                                // Wallet balance shortcut button
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .border(width = 1.dp, color = Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(12.dp))
                                        .clickable { currentScreen = ROUTE_RECHARGE }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = "Wallet Balance",
                                        tint = GlowGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${df.format(userAccount?.balance ?: 0.0)}đ",
                                        color = GlowGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            IconButton(
                                onClick = { viewModel.toggleDarkMode() },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(19.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .border(width = 1.dp, color = Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(19.dp))
                            ) {
                                Icon(
                                    imageVector = when (themeMode) {
                                        "DARK" -> Icons.Default.DarkMode
                                        "LIGHT" -> Icons.Default.LightMode
                                        else -> Icons.Default.SettingsBrightness
                                    },
                                    contentDescription = "Toggle Theme",
                                    tint = if (isDarkMode) Color(0xFFFFD700) else activeColors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { currentScreen = ROUTE_SETTINGS },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(19.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .border(width = 1.dp, color = Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(19.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Cài đặt",
                                    tint = BrightTurquoise,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.logout() },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(19.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .border(width = 1.dp, color = Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(19.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = "Đăng xuất",
                                    tint = CoralVibrant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (!isUnpaid) {
                NavigationBar(
                    containerColor = activeColors.cardBg,
                    tonalElevation = 8.dp,
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = activeColors.border.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                ) {
                    // Booster tab
                    NavigationBarItem(
                        selected = currentScreen == ROUTE_BOOSTER,
                        onClick = { currentScreen = ROUTE_BOOSTER },
                        icon = { Icon(Icons.Default.SportsEsports, "Booster") },
                        label = { Text("Game Boost", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = activeColors.background,
                            selectedTextColor = activeColors.primary,
                            indicatorColor = activeColors.primary,
                            unselectedIconColor = TextGray,
                            unselectedTextColor = TextGray
                        )
                    )

                    // Freezer tab (VIP 2 and ADMIN exclusive check)
                    NavigationBarItem(
                        selected = currentScreen == ROUTE_FREEZER,
                        onClick = {
                            val tier = userAccount?.tier
                            if (tier == "VIP2" || tier == "ADMIN") {
                                currentScreen = ROUTE_FREEZER
                            } else {
                                showUpgradePromptForFreezer = true
                            }
                        },
                        icon = { Icon(Icons.Default.AcUnit, "Freezer") },
                        label = { Text("Đóng băng", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = activeColors.background,
                            selectedTextColor = activeColors.primary,
                            indicatorColor = activeColors.primary,
                            unselectedIconColor = TextGray,
                            unselectedTextColor = TextGray
                        )
                    )

                    // Shop Tab
                    NavigationBarItem(
                        selected = currentScreen == ROUTE_SHOP,
                        onClick = { currentScreen = ROUTE_SHOP },
                        icon = { Icon(Icons.Default.ShoppingBag, "Shop") },
                        label = { Text("Shop Acc", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = activeColors.background,
                            selectedTextColor = activeColors.primary,
                            indicatorColor = activeColors.primary,
                            unselectedIconColor = TextGray,
                            unselectedTextColor = TextGray
                        )
                    )

                    // AI chatbot Tab
                    NavigationBarItem(
                        selected = currentScreen == ROUTE_AI,
                        onClick = { currentScreen = ROUTE_AI },
                        icon = { Icon(Icons.Default.SmartToy, "AI Trợ Lý") },
                        label = { Text("AI Trợ Lý", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = activeColors.background,
                            selectedTextColor = activeColors.primary,
                            indicatorColor = activeColors.primary,
                            unselectedIconColor = TextGray,
                            unselectedTextColor = TextGray
                        )
                    )

                    // Subscription & Wallet Tab (replaces Cài đặt tab in bottom bar)
                    NavigationBarItem(
                        selected = currentScreen == ROUTE_RECHARGE,
                        onClick = { currentScreen = ROUTE_RECHARGE },
                        icon = { Icon(Icons.Default.AccountBalanceWallet, "Ví & VIP") },
                        label = { Text("Ví & VIP", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = activeColors.background,
                            selectedTextColor = activeColors.primary,
                            indicatorColor = activeColors.primary,
                            unselectedIconColor = TextGray,
                            unselectedTextColor = TextGray
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(activeColors.background, activeColors.background.copy(alpha = 0.8f))))
                .padding(innerPadding)
        ) {
            // Screen router
            if (isUnpaid) {
                RechargeScreen(viewModel = viewModel)
            } else {
                when (currentScreen) {
                    ROUTE_BOOSTER -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            TabRow(
                                selectedTabIndex = activeBoosterTab,
                                containerColor = activeColors.background,
                                contentColor = activeColors.primary
                            ) {
                                Tab(
                                    selected = activeBoosterTab == 0,
                                    onClick = { activeBoosterTab = 0 },
                                    text = { Text("Game Dashboard", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    icon = { Icon(Icons.Default.SportsEsports, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                )
                                Tab(
                                    selected = activeBoosterTab == 1,
                                    onClick = { activeBoosterTab = 1 },
                                    text = { Text("Tính năng VIP", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    icon = { Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                )
                                Tab(
                                    selected = activeBoosterTab == 2,
                                    onClick = { activeBoosterTab = 2 },
                                    text = { Text("Cấu hình tối ưu", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    icon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                when (activeBoosterTab) {
                                    0 -> GameBoosterDashboard(viewModel = viewModel)
                                    1 -> FeaturesScreen(viewModel = viewModel)
                                    else -> MainHubScreen(viewModel = viewModel)
                                }
                            }
                        }
                    }
                    ROUTE_FREEZER -> AppFreezerScreen(viewModel = viewModel)
                    ROUTE_SHOP -> GameShopScreen(viewModel = viewModel)
                    ROUTE_AI -> AiCompanionScreen(viewModel = viewModel)
                    ROUTE_RECHARGE -> RechargeScreen(viewModel = viewModel)
                    ROUTE_SETTINGS -> SettingsScreen(
                        viewModel = viewModel,
                        onNavigateToAdmin = { currentScreen = ROUTE_ADMIN_PANEL }
                    )
                    ROUTE_ADMIN_PANEL -> AdminPanelScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = ROUTE_SETTINGS }
                    )
                }
            }

            // Paywall Upgrade Prompt modal if VIP1 user tries to enter freezer tab
            if (showUpgradePromptForFreezer) {
                AlertDialog(
                    onDismissRequest = { showUpgradePromptForFreezer = false },
                    containerColor = DarkTealCard,
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, "VIP 2 Needed", tint = CoralVibrant)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Yêu Cầu VIP 2 PRO", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    text = {
                        Text(
                            text = "Tính năng Đóng băng tệp nguồn hệ thống và Ứng dụng chạy ngầm nâng cao chỉ có sẵn cho tài khoản VIP 2 PRO trở lên.\n\nVui lòng gia hạn hoặc nâng cấp tài khoản của bạn để trải nghiệm tính năng siêu cấp này!",
                            color = TextGray,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showUpgradePromptForFreezer = false
                                // Redirect straight to Subscription/Payment dashboard to upgrade!
                                currentScreen = ROUTE_RECHARGE
                            }
                        ) {
                            Text("NÂNG CẤP NGAY", color = GlowGreen, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showUpgradePromptForFreezer = false }) {
                            Text("HUỶ BỎ", color = TextGray)
                        }
                    }
                )
            }
        }
    }
}
}
}
