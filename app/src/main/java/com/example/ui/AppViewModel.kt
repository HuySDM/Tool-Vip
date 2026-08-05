package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository
    private val sharedPrefs = application.getSharedPreferences("tool_vip_prefs", android.content.Context.MODE_PRIVATE)
    val currentUsername = MutableStateFlow<String?>(null)
    private var lastScheduledOptTime = 0L

    suspend fun getCurrentUserDirect(): UserAccount? {
        val username = currentUsername.value ?: return null
        return repository.getUserAccountDirect(username)
    }

    fun showToast(message: String) {
        viewModelScope.launch(Dispatchers.Main) {
            android.widget.Toast.makeText(getApplication(), message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    // --- ADMIN CENTRALIZED MANAGEMENT CONTROLS ---
    fun updateAccountTierDirectly(newTier: String, targetUser: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val usernameToUpdate = targetUser ?: currentUsername.value ?: return@launch
            val account = repository.getUserAccountDirect(usernameToUpdate) ?: return@launch
            val expiry = if (newTier == "ADMIN") {
                0L // Infinite
            } else if (newTier == "UNPAID") {
                0L
            } else {
                System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000) // 30 days
            }
            repository.saveUserAccount(account.copy(tier = newTier, expiryTimestamp = expiry))
            showToast("Đã thay đổi cấp bậc cho [$usernameToUpdate] thành $newTier!")
        }
    }

    fun simulateVipExpiryDirectly() {
        viewModelScope.launch(Dispatchers.IO) {
            val account = getCurrentUserDirect() ?: return@launch
            if (account.tier == "VIP1" || account.tier == "VIP2") {
                val expiredTimestamp = System.currentTimeMillis() - 10000L
                repository.saveUserAccount(account.copy(expiryTimestamp = expiredTimestamp))
                showToast("Đã lùi hạn sử dụng về quá khứ! Chờ xử lý nền trong 30 giây...")
            } else {
                val expiredTimestamp = System.currentTimeMillis() - 10000L
                repository.saveUserAccount(
                    account.copy(
                        tier = "VIP1",
                        expiryTimestamp = expiredTimestamp,
                        customRole = "Hội viên VIP 1"
                    )
                )
                showToast("Đã bật VIP 1 và lùi hạn dùng về quá khứ! Chờ xử lý nền trong 30 giây...")
            }
        }
    }

    fun updateAccountBalanceDirectly(newBalance: Double, targetUser: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val usernameToUpdate = targetUser ?: currentUsername.value ?: return@launch
            val account = repository.getUserAccountDirect(usernameToUpdate) ?: return@launch
            val diff = newBalance - account.balance
            repository.saveUserAccount(account.copy(balance = newBalance.coerceAtLeast(0.0)))
            
            repository.addTransaction(
                TransactionItem(
                    username = usernameToUpdate,
                    amount = diff,
                    type = "MANUAL_ADJUST",
                    status = "SUCCESS",
                    referenceNote = "Điều chỉnh số dư trực tiếp bởi Admin"
                )
            )
            showToast("Đã cập nhật số dư ví cho [$usernameToUpdate] thành ${java.text.DecimalFormat("#,###").format(newBalance)}đ!")
        }
    }

    fun modifyAccountBalanceByAmount(delta: Double, targetUser: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val usernameToUpdate = targetUser ?: currentUsername.value ?: return@launch
            val account = repository.getUserAccountDirect(usernameToUpdate) ?: return@launch
            val newBalance = (account.balance + delta).coerceAtLeast(0.0)
            repository.saveUserAccount(account.copy(balance = newBalance))
            
            repository.addTransaction(
                TransactionItem(
                    username = usernameToUpdate,
                    amount = delta,
                    type = "MANUAL_ADJUST",
                    status = "SUCCESS",
                    referenceNote = if (delta >= 0) "Cộng tiền thủ công bởi Admin" else "Trừ tiền thủ công bởi Admin"
                )
            )
            val deltaStr = if (delta >= 0) "+${java.text.DecimalFormat("#,###").format(delta)}" else "${java.text.DecimalFormat("#,###").format(delta)}"
            showToast("Đã chi phối dòng tiền [$usernameToUpdate]: ${deltaStr}đ (Số dư mới: ${java.text.DecimalFormat("#,###").format(newBalance)}đ)!")
        }
    }

    // UI States
    val userAccount: StateFlow<UserAccount?>
    val allApps: StateFlow<List<AppItem>>
    val allGameAccounts: StateFlow<List<GameAccount>>
    val chatHistory: StateFlow<List<ChatMessage>>
    val allUserAccounts: StateFlow<List<UserAccount>>
    val allTransactions: StateFlow<List<TransactionItem>>
    val userTransactions: StateFlow<List<TransactionItem>>

    // Live Monitoring States
    private val _cpuCores = MutableStateFlow(listOf(1805, 1805, 1805, 1805, 2419, 2419, 2419, 1075))
    val cpuCores: StateFlow<List<Int>> = _cpuCores.asStateFlow()

    private val _gpuFreq = MutableStateFlow(587)
    val gpuFreq: StateFlow<Int> = _gpuFreq.asStateFlow()

    private val _gpuHistory = MutableStateFlow(listOf(550f, 560f, 580f, 570f, 587f, 585f, 587f, 580f, 590f))
    val gpuHistory: StateFlow<List<Float>> = _gpuHistory.asStateFlow()

    private val _cpuHistory = MutableStateFlow(listOf(40f, 45f, 55f, 48f, 50f, 62f, 58f, 45f, 48f, 50f))
    val cpuHistory: StateFlow<List<Float>> = _cpuHistory.asStateFlow()

    // Temporary action states
    private val _isOptimizing = MutableStateFlow(false)
    val isOptimizing: StateFlow<Boolean> = _isOptimizing.asStateFlow()

    private val _optimizationMessage = MutableStateFlow("")
    val optimizationMessage: StateFlow<String> = _optimizationMessage.asStateFlow()

    // Search & Filter state for Apps
    val searchQuery = MutableStateFlow("")
    val appFilter = MutableStateFlow("ALL") // ALL, SYSTEM, USER, FROZEN
    val selectedApps = MutableStateFlow<Set<String>>(emptySet())

    // AI Loading State
    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // --- BATTERY HEALTH & GAMING OPTIMIZATION STATES ---
    private val _batteryHealth = MutableStateFlow("Tốt (Good)")
    val batteryHealth: StateFlow<String> = _batteryHealth.asStateFlow()

    private val _batteryLevel = MutableStateFlow(95)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val _batteryTemp = MutableStateFlow("35.2°C")
    val batteryTemp: StateFlow<String> = _batteryTemp.asStateFlow()

    private val _batteryOptimizationApplied = MutableStateFlow(false)
    val batteryOptimizationApplied: StateFlow<Boolean> = _batteryOptimizationApplied.asStateFlow()

    // --- UI THEME & INTERFACE CUSTOMIZATION STATES (TÙY CHỈNH GIAO DIỆN) ---
    private val _themeStyle = MutableStateFlow(sharedPrefs.getString("ui_theme_style", "DeepObsidian") ?: "DeepObsidian")
    val themeStyle: StateFlow<String> = _themeStyle.asStateFlow()

    private val _cardTransparency = MutableStateFlow(sharedPrefs.getFloat("ui_card_transparency", 0.95f))
    val cardTransparency: StateFlow<Float> = _cardTransparency.asStateFlow()

    private val _cardCornerRadius = MutableStateFlow(sharedPrefs.getInt("ui_card_corner_radius", 16))
    val cardCornerRadius: StateFlow<Int> = _cardCornerRadius.asStateFlow()

    private val _glowIntensity = MutableStateFlow(sharedPrefs.getString("ui_glow_intensity", "Normal") ?: "Normal")
    val glowIntensity: StateFlow<String> = _glowIntensity.asStateFlow()

    private val _dynamicPulseEnabled = MutableStateFlow(sharedPrefs.getBoolean("ui_dynamic_pulse_enabled", true))
    val dynamicPulseEnabled: StateFlow<Boolean> = _dynamicPulseEnabled.asStateFlow()

    fun setThemeStyle(style: String) {
        _themeStyle.value = style
        sharedPrefs.edit().putString("ui_theme_style", style).apply()
    }

    fun setCardTransparency(transparency: Float) {
        _cardTransparency.value = transparency
        sharedPrefs.edit().putFloat("ui_card_transparency", transparency).apply()
    }

    fun setCardCornerRadius(radius: Int) {
        _cardCornerRadius.value = radius
        sharedPrefs.edit().putInt("ui_card_corner_radius", radius).apply()
    }

    fun setGlowIntensity(intensity: String) {
        _glowIntensity.value = intensity
        sharedPrefs.edit().putString("ui_glow_intensity", intensity).apply()
    }

    fun setDynamicPulseEnabled(enabled: Boolean) {
        _dynamicPulseEnabled.value = enabled
        sharedPrefs.edit().putBoolean("ui_dynamic_pulse_enabled", enabled).apply()
    }

    // --- GAME BOOSTER LEVEL STATES (CHẾ ĐỘ TĂNG TỐC GAME) ---
    private val _boosterLevel = MutableStateFlow(sharedPrefs.getInt("booster_level", 2))
    val boosterLevel: StateFlow<Int> = _boosterLevel.asStateFlow()

    fun setBoosterLevel(level: Int) {
        _boosterLevel.value = level
        sharedPrefs.edit().putInt("booster_level", level).apply()
    }

    // --- AI-DRIVEN GAME PROFILE STATES ---
    private val _isGameProfileAiEnabled = MutableStateFlow(sharedPrefs.getBoolean("game_profile_ai_enabled", true))
    val isGameProfileAiEnabled: StateFlow<Boolean> = _isGameProfileAiEnabled.asStateFlow()

    private val _selectedGameProfilePackage = MutableStateFlow(sharedPrefs.getString("selected_game_profile_package", "com.garena.game.kgvn") ?: "com.garena.game.kgvn")
    val selectedGameProfilePackage: StateFlow<String> = _selectedGameProfilePackage.asStateFlow()

    fun setGameProfileAiEnabled(enabled: Boolean) {
        _isGameProfileAiEnabled.value = enabled
        sharedPrefs.edit().putBoolean("game_profile_ai_enabled", enabled).apply()
    }

    fun setSelectedGameProfilePackage(packageName: String) {
        _selectedGameProfilePackage.value = packageName
        sharedPrefs.edit().putString("selected_game_profile_package", packageName).apply()
    }

    private val _selectedGraphicsProfile = MutableStateFlow(sharedPrefs.getString("game_graphics_profile", "Zero-Lag Balanced") ?: "Zero-Lag Balanced")
    val selectedGraphicsProfile: StateFlow<String> = _selectedGraphicsProfile.asStateFlow()

    fun setSelectedGraphicsProfile(profile: String) {
        _selectedGraphicsProfile.value = profile
        sharedPrefs.edit().putString("game_graphics_profile", profile).apply()
    }

    // --- AUTO-CLEAR CACHE SETTING STATES ---
    private val _isAutoClearCacheEnabled = MutableStateFlow(sharedPrefs.getBoolean("auto_clear_cache_enabled", true))
    val isAutoClearCacheEnabled: StateFlow<Boolean> = _isAutoClearCacheEnabled.asStateFlow()

    fun setAutoClearCacheEnabled(enabled: Boolean) {
        _isAutoClearCacheEnabled.value = enabled
        sharedPrefs.edit().putBoolean("auto_clear_cache_enabled", enabled).apply()
    }

    // --- SCHEDULER & IDLE MODE STATES ---
    private val _isDeviceIdle = MutableStateFlow(true)
    val isDeviceIdle: StateFlow<Boolean> = _isDeviceIdle.asStateFlow()

    private val _simulatedJunkSizeMb = MutableStateFlow(sharedPrefs.getFloat("simulated_junk_size_mb", 342.8f))
    val simulatedJunkSizeMb: StateFlow<Float> = _simulatedJunkSizeMb.asStateFlow()

    private val _isIdleModeOnlyEnabled = MutableStateFlow(sharedPrefs.getBoolean("idle_mode_only_enabled", true))
    val isIdleModeOnlyEnabled: StateFlow<Boolean> = _isIdleModeOnlyEnabled.asStateFlow()

    private val _networkPingBoosted = MutableStateFlow(sharedPrefs.getBoolean("network_ping_boosted", true))
    val networkPingBoosted: StateFlow<Boolean> = _networkPingBoosted.asStateFlow()

    fun setIdleModeOnlyEnabled(enabled: Boolean) {
        _isIdleModeOnlyEnabled.value = enabled
        sharedPrefs.edit().putBoolean("idle_mode_only_enabled", enabled).apply()
        showToast(if (enabled) "Đã bật: Chỉ tự động dọn dẹp khi thiết bị rảnh" else "Đã tắt: Chạy dọn dẹp định kỳ bất kể trạng thái")
    }

    fun setNetworkPingBoosted(enabled: Boolean) {
        _networkPingBoosted.value = enabled
        sharedPrefs.edit().putBoolean("network_ping_boosted", enabled).apply()
        showToast(if (enabled) "🚀 Đã kích hoạt tối ưu hóa Ping mạng & đường truyền game siêu tốc!" else "Đã tắt tối ưu hóa Ping.")
    }

    fun toggleDeviceIdleState() {
        _isDeviceIdle.value = !_isDeviceIdle.value
        showToast("Trạng thái thiết bị: ${if (_isDeviceIdle.value) "Đang rảnh (Idle)" else "Đang hoạt động (Active)"}")
    }

    fun setSimulatedJunkSize(size: Float) {
        _simulatedJunkSizeMb.value = size
        sharedPrefs.edit().putFloat("simulated_junk_size_mb", size).apply()
    }

    fun activateAiGameProfile(gameName: String) {
        setSelectedGraphicsProfile("Extreme 120 FPS Max")
        setBoosterLevel(4)
        
        val currentLogs = _aiScanLogs.value.toMutableList()
        currentLogs.add(0, "✨ [AI Profile] Đã phát hiện khởi chạy game: $gameName")
        currentLogs.add(0, "🖥️ [AI Profile] Tự động chuyển cấu hình đồ họa sang Extreme 120 FPS")
        currentLogs.add(0, "⚡ [AI Profile] Tự động nâng cấp Game Boost lên Mức 4 (Mở giới hạn)")
        currentLogs.add(0, "🧹 [AI Profile] Đã dọn dẹp toàn bộ bộ nhớ đệm ngầm để tối ưu hóa CPU/GPU")
        _aiScanLogs.value = currentLogs.take(15)
        
        showToast("AI Game Profile: Đã kích hoạt đồ họa Extreme 120 FPS & Boost mức 4 cho $gameName!")
    }

    // --- AI STATUS BOARD (BẢNG TRẠNG THÁI AI) STATES ---
    private val _aiStatusScore = MutableStateFlow(78)
    val aiStatusScore: StateFlow<Int> = _aiStatusScore.asStateFlow()

    private val _aiScanLogs = MutableStateFlow<List<String>>(listOf(
        "Khởi tạo lõi phân tích thần kinh AI...",
        "Đã liên kết hệ thống giám sát tiến trình Android...",
        "Trạng thái lõi: Sẵn sàng tối ưu"
    ))
    val aiScanLogs: StateFlow<List<String>> = _aiScanLogs.asStateFlow()

    private val _aiRecommendation = MutableStateFlow("Hệ thống hoạt động khá mượt mà. AI phát hiện 3 tiến trình ngầm có thể giải phóng.")
    val aiRecommendation: StateFlow<String> = _aiRecommendation.asStateFlow()

    private val _isAiStatusBoardScanning = MutableStateFlow(false)
    val isAiStatusBoardScanning: StateFlow<Boolean> = _isAiStatusBoardScanning.asStateFlow()

    fun runNeuralCoreScan() {
        viewModelScope.launch {
            if (_isAiStatusBoardScanning.value) return@launch
            _isAiStatusBoardScanning.value = true
            val logs = mutableListOf<String>()
            
            logs.add("🚀 Bắt đầu quét Lõi Thần Kinh AI...")
            _aiScanLogs.value = logs.toList()
            delay(800)
            
            logs.add("🔍 Đang kết nối Sensor cảm biến phần cứng...")
            _aiScanLogs.value = logs.toList()
            delay(600)
            
            logs.add("⚙️ Đang phân tích bảng phân bổ RAM (64-bit)...")
            _aiScanLogs.value = logs.toList()
            delay(700)
            
            logs.add("📡 Quét các tiến trình rác chạy ẩn gây nghẽn cổ chai...")
            _aiScanLogs.value = logs.toList()
            delay(800)
            
            logs.add("⚡ Phát hiện com.facebook.katana & com.shopee.vn chiếm dụng RAM bất thường.")
            _aiScanLogs.value = logs.toList()
            delay(600)
            
            logs.add("💡 AI Đề xuất: Hãy dùng tính năng Đóng Băng Tự Động hoặc Dọn Rác thủ công.")
            _aiScanLogs.value = logs.toList()
            delay(500)
            
            logs.add("✅ Quét thần kinh hoàn tất! Lõi AI đề xuất phương án tối ưu.")
            _aiScanLogs.value = logs.toList()
            
            _aiStatusScore.value = (94..99).random()
            _aiRecommendation.value = "Tốt! Hệ thống tối ưu đạt mức cao sau phân tích AI. Nên đóng băng các tệp rác để duy trì ổn định."
            _isAiStatusBoardScanning.value = false
        }
    }

    // --- ROLE APPROVAL SYSTEM ---
    private val _pendingRoleRequests = MutableStateFlow<List<UserAccount>>(emptyList())
    val pendingRoleRequests: StateFlow<List<UserAccount>> = _pendingRoleRequests.asStateFlow()

    // --- CUSTOM GIFTCODES SYSTEM ---
    private val _customGiftCodes = MutableStateFlow<List<CustomGiftCode>>(emptyList())
    val customGiftCodes: StateFlow<List<CustomGiftCode>> = _customGiftCodes.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database.appDao())
        loadCustomGiftCodes()

        val savedUser = sharedPrefs.getString("logged_in_user", null)
        currentUsername.value = savedUser

        @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
        userAccount = currentUsername
            .flatMapLatest { username ->
                if (username != null) {
                    repository.getUserAccount(username)
                } else {
                    flowOf(null)
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

        allApps = repository.allApps.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allGameAccounts = repository.allGameAccounts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        chatHistory = repository.chatHistory.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allUserAccounts = repository.allUserAccounts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allTransactions = repository.allTransactions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
        userTransactions = currentUsername
            .flatMapLatest { username ->
                if (username != null) {
                    repository.getTransactionsForUser(username)
                } else {
                    flowOf(emptyList())
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        // Initialize with default values if database is empty
        viewModelScope.launch(Dispatchers.IO) {
            setupInitialData()
        }

        // Start live hardware monitoring loop
        startHardwareMonitoring()

        // Start live subscription expiry monitoring job
        startSubscriptionExpiryJob()

        // Start automated scheduling check for AI optimization
        startScheduledOptimizationJob()

        // Refresh battery and load pending approvals
        refreshBatteryStatus()
        loadPendingRoleRequests()
    }

    private suspend fun setupInitialData() {
        // Clear insecure default "admin" and "user" accounts
        repository.deleteUserAccount("admin")
        repository.deleteUserAccount("user")

        // Prepopulate default secure admin account for the owner
        val directAdmin = repository.getUserAccountDirect("quanghuy")
        if (directAdmin == null) {
            repository.saveUserAccount(
                UserAccount(
                    username = "quanghuy",
                    passwordHash = "quanghuy1789",
                    tier = "ADMIN",
                    balance = 5000000.0,
                    customRole = "QUẢN TRỊ VIÊN HỆ THỐNG",
                    email = "quanghuypham1789@gmail.com",
                    authPin = "10293847"
                )
            )
        }

        // Seed default employees (Staff) and users
        if (repository.getUserAccountDirect("nhanvien1") == null) {
            repository.saveUserAccount(
                UserAccount(
                    username = "nhanvien1",
                    passwordHash = "123456",
                    tier = "STAFF",
                    balance = 100000.0,
                    customRole = "Nhân viên hỗ trợ 1",
                    email = "nhanvien1@toolvip.com"
                )
            )
        }
        if (repository.getUserAccountDirect("nhanvien2") == null) {
            repository.saveUserAccount(
                UserAccount(
                    username = "nhanvien2",
                    passwordHash = "123456",
                    tier = "STAFF",
                    balance = 150000.0,
                    customRole = "Nhân viên hỗ trợ 2",
                    email = "nhanvien2@toolvip.com"
                )
            )
        }
        if (repository.getUserAccountDirect("user_test1") == null) {
            repository.saveUserAccount(
                UserAccount(
                    username = "user_test1",
                    passwordHash = "123456",
                    tier = "VIP1",
                    balance = 80000.0,
                    customRole = "Hội viên VIP 1",
                    email = "usertest1@gmail.com"
                )
            )
        }
        if (repository.getUserAccountDirect("user_test2") == null) {
            repository.saveUserAccount(
                UserAccount(
                    username = "user_test2",
                    passwordHash = "123456",
                    tier = "UNPAID",
                    balance = 0.0,
                    customRole = "Thành viên thường",
                    email = "usertest2@gmail.com"
                )
            )
        }

        // Prepopulate standard trash & system apps
        repository.allApps.first().let { currentApps ->
            if (currentApps.isEmpty()) {
                val initialApps = listOf(
                    AppItem("com.facebook.katana", "Facebook", isSystemApp = false, isFrozen = false, ramUsage = 185.0, isTrash = true),
                    AppItem("com.zhiliaoapp.musically", "TikTok", isSystemApp = false, isFrozen = false, ramUsage = 340.0, isTrash = true),
                    AppItem("com.shopee.vn", "Shopee", isSystemApp = false, isFrozen = false, ramUsage = 210.0, isTrash = true),
                    AppItem("com.instagram.android", "Instagram", isSystemApp = false, isFrozen = false, ramUsage = 160.0, isTrash = true),
                    AppItem("com.tencent.ig", "PUBG Mobile", isSystemApp = false, isFrozen = false, ramUsage = 720.0, isTrash = false),
                    AppItem("com.garena.game.kgvn", "Liên Quân Mobile", isSystemApp = false, isFrozen = false, ramUsage = 640.0, isTrash = false),
                    AppItem("com.garena.game.codm", "Call of Duty", isSystemApp = false, isFrozen = false, ramUsage = 850.0, isTrash = false),
                    AppItem("com.garena.game.fof", "Free Fire", isSystemApp = false, isFrozen = false, ramUsage = 480.0, isTrash = false),
                    
                    AppItem("com.google.android.gms", "Google Play Services", isSystemApp = true, isFrozen = false, ramUsage = 120.0, isTrash = false),
                    AppItem("com.android.systemui", "System UI", isSystemApp = true, isFrozen = false, ramUsage = 195.0, isTrash = false),
                    AppItem("com.android.chrome", "Chrome Browser", isSystemApp = true, isFrozen = false, ramUsage = 280.0, isTrash = true),
                    AppItem("com.google.android.youtube", "YouTube", isSystemApp = true, isFrozen = false, ramUsage = 310.0, isTrash = true),
                    AppItem("com.android.vending", "Google Play Store", isSystemApp = true, isFrozen = false, ramUsage = 95.0, isTrash = false),
                    AppItem("com.android.providers.telephony", "Phone Services", isSystemApp = true, isFrozen = false, ramUsage = 40.0, isTrash = false),
                    AppItem("com.sec.android.app.camera", "Samsung Camera Service", isSystemApp = true, isFrozen = false, ramUsage = 150.0, isTrash = false)
                )
                repository.saveApps(initialApps)
            }
        }

        // Prepopulate high value game accounts
        repository.allGameAccounts.first().let { currentAccounts ->
            if (currentAccounts.isEmpty()) {
                val initialAccounts = listOf(
                    GameAccount(
                        gameTitle = "Liên Quân Mobile",
                        accountName = "Acc Tướng 115 - Trang phục 450",
                        price = 150000.0,
                        details = "Full ngọc, có 5 trang phục SSS hữu hạn cực xịn, Rank Thách Đấu, tỉ lệ thắng 68%. Bao đổi thông tin sạch 100%."
                    ),
                    GameAccount(
                        gameTitle = "Free Fire",
                        accountName = "Acc Quỷ Dạ Xoa - Mp40 Mãng Xà LvMax",
                        price = 250000.0,
                        details = "Mp40 Mãng Xà LvMax, AK Rồng Xanh Lv5, M1014 Tiếng Thét lv4. Nhiều skin súng vip khác, Rank Huyền Thoại."
                    ),
                    GameAccount(
                        gameTitle = "PUBG Mobile",
                        accountName = "Acc M416 Băng Giá LvMax - Set Xác Ướp",
                        price = 350000.0,
                        details = "M416 Băng Giá LvMax, M24 Nhà Giả Kim Lv4. Có áo Xác Ướp Vàng độc quyền, xe Tesla, Rank Chí Tôn."
                    ),
                    GameAccount(
                        gameTitle = "Blood Strike",
                        accountName = "Acc Săn Lùng Đỉnh Cao - Full Vũ Khí Vàng",
                        price = 80000.0,
                        details = "Đầy đủ các loại súng vàng, nhân vật tối tân nhất, Rank Tinh Anh, an toàn tuyệt đối."
                    )
                )
                repository.saveGameAccounts(initialAccounts)
            }
        }
    }

    // --- AUTHENTICATION FLOWS ---
    fun login(usernameInput: String, passwordInput: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val username = usernameInput.trim()
            val password = passwordInput.trim()
            if (username.isBlank() || password.isBlank()) {
                onResult(false, "Vui lòng nhập đầy đủ tài khoản và mật khẩu!")
                return@launch
            }
            val account = repository.getUserAccountDirect(username)
            if (account == null) {
                onResult(false, "Tài khoản không tồn tại trên hệ thống!")
            } else if (account.passwordHash != password) {
                onResult(false, "Mật khẩu không chính xác!")
            } else {
                currentUsername.value = account.username
                sharedPrefs.edit().putString("logged_in_user", account.username).apply()
                onResult(true, "Đăng nhập thành công! Chào mừng quay lại.")
            }
        }
    }

    fun register(usernameInput: String, passwordInput: String, emailInput: String, promoCode: String = "", onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val username = usernameInput.trim()
            val password = passwordInput.trim()
            val email = emailInput.trim()
            if (username.isBlank() || password.isBlank() || email.isBlank()) {
                onResult(false, "Vui lòng nhập đầy đủ tài khoản, mật khẩu và email liên kết!")
                return@launch
            }
            if (username.length < 3) {
                onResult(false, "Tên đăng nhập phải chứa tối thiểu 3 ký tự!")
                return@launch
            }
            if (!email.contains("@") || !email.contains(".")) {
                onResult(false, "Vui lòng nhập địa chỉ email hợp lệ!")
                return@launch
            }
            val existing = repository.getUserAccountDirect(username)
            if (existing != null) {
                onResult(false, "Tài khoản [$username] đã tồn tại trên hệ thống!")
                return@launch
            }

            val cleanPromo = promoCode.trim().uppercase()
            val isStaticPromo = cleanPromo == "VIP1_FREE_MOBI" || 
                                cleanPromo == "VIP1_FREE" || 
                                cleanPromo == "VIP1_GIFT" || 
                                cleanPromo == "VIP1" || 
                                cleanPromo == "VIP1VIP" ||
                                cleanPromo == "VIP1_AI" ||
                                cleanPromo == "VIP"

            val initialTier = if (isStaticPromo) "VIP1" else "UNPAID"
            val initialRole = if (isStaticPromo) "Hội viên VIP 1 (Mã đăng ký)" else "Thành viên thường"
            val expiry = if (isStaticPromo) System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000) else 0L

            val defaultVip1Price = sharedPrefs.getFloat("default_vip1_price", 50000.0f).toDouble()
            val defaultVip2Price = sharedPrefs.getFloat("default_vip2_price", 120000.0f).toDouble()

            val newAccount = UserAccount(
                username = username,
                passwordHash = password,
                email = email,
                tier = initialTier,
                customRole = initialRole,
                expiryTimestamp = expiry,
                balance = 0.0,
                vip1Price = defaultVip1Price,
                vip2Price = defaultVip2Price
            )
            repository.saveUserAccount(newAccount)
            currentUsername.value = username
            sharedPrefs.edit().putString("logged_in_user", username).apply()
            
            val welcomeMsg = if (isStaticPromo) {
                "Đăng ký thành công & Kích hoạt VIP 1 thành công bằng Giftcode!"
            } else {
                "Đăng ký thành công! Đang tự động đăng nhập..."
            }
            onResult(true, welcomeMsg)
        }
    }

    fun logout() {
        viewModelScope.launch {
            currentUsername.value = null
            sharedPrefs.edit().remove("logged_in_user").apply()
            showToast("Đã đăng xuất tài khoản!")
        }
    }

    private fun startHardwareMonitoring() {
        viewModelScope.launch {
            while (true) {
                delay(1200)
                // Fluctuate CPU Cores
                val baseCores = listOf(1805, 1805, 1805, 1805, 2419, 2419, 2419, 1075)
                val newCores = baseCores.map { base ->
                    val variance = Random.nextInt(-45, 45)
                    (base + variance).coerceAtLeast(400)
                }
                _cpuCores.value = newCores

                // Fluctuate GPU Freq
                val prevGpu = _gpuFreq.value
                val varianceGpu = Random.nextInt(-15, 15)
                val newGpu = (587 + varianceGpu).coerceIn(400, 650)
                _gpuFreq.value = newGpu

                // Update histories
                val currentGpuHistory = _gpuHistory.value.toMutableList()
                currentGpuHistory.removeAt(0)
                currentGpuHistory.add(newGpu.toFloat())
                _gpuHistory.value = currentGpuHistory

                val currentCpuHistory = _cpuHistory.value.toMutableList()
                currentCpuHistory.removeAt(0)
                val randomCpuPercent = Random.nextInt(35, 75).toFloat()
                currentCpuHistory.add(randomCpuPercent)
                _cpuHistory.value = currentCpuHistory
            }
        }
    }

    private fun startSubscriptionExpiryJob() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                try {
                    checkAllSubscriptions()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(30000) // Check every 30 seconds for real-time responsiveness during demo/testing
            }
        }
    }

    private suspend fun checkAllSubscriptions() {
        val now = System.currentTimeMillis()
        val accounts = repository.allUserAccounts.first()
        for (account in accounts) {
            val isPremium = account.tier == "VIP1" || account.tier == "VIP2"
            if (isPremium && account.expiryTimestamp > 0L && now > account.expiryTimestamp) {
                val price = if (account.tier == "VIP1") account.vip1Price else account.vip2Price
                if (account.isAutoRenew && account.balance >= price) {
                    // Auto-renew successfully
                    val newBalance = account.balance - price
                    val newExpiry = now + (30L * 24 * 60 * 60 * 1000) // Extend 30 days
                    val renewedAccount = account.copy(
                        balance = newBalance,
                        expiryTimestamp = newExpiry,
                        customRole = if (account.tier == "VIP1") "Hội viên VIP 1" else "Hội viên VIP 2 Pro"
                    )
                    repository.saveUserAccount(renewedAccount)
                    showToast("Tài khoản [${account.username}] đã tự động gia hạn thành công gói ${account.tier}!")
                } else {
                    // Expired - Revoke access
                    val expiredAccount = account.copy(
                        tier = "EXPIRED",
                        customRole = "Đã hết hạn (Expired)"
                    )
                    repository.saveUserAccount(expiredAccount)
                    showToast("Tài khoản [${account.username}] đã hết hạn sử dụng VIP và bị thu hồi quyền truy cập!")
                }
            }
        }
    }

    // --- RECHARGE AND PAYWALL CONTROL LOGIC ---
    fun rechargeAccount(amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val account = getCurrentUserDirect() ?: return@launch
            val newBalance = account.balance + amount
            repository.saveUserAccount(account.copy(balance = newBalance))
            
            // Log in transaction history
            repository.addTransaction(
                TransactionItem(
                    username = account.username,
                    amount = amount,
                    type = "DEPOSIT",
                    status = "SUCCESS",
                    referenceNote = "Nạp tiền mô phỏng thành công"
                )
            )
        }
    }

    fun calculateDurationMs(value: Int, unit: String): Long {
        return when (unit.lowercase()) {
            "giờ", "hours" -> value * 60L * 60 * 1000
            "tuần", "weeks" -> value * 7L * 24 * 60 * 60 * 1000
            "tháng", "months" -> value * 30L * 24 * 60 * 60 * 1000
            "vĩnh viễn", "permanent" -> 3650L * 24 * 60 * 60 * 1000 // ~10 years (vĩnh viễn)
            else -> value * 24L * 60 * 60 * 1000 // "ngày"
        }
    }

    fun buyVipTier(tierName: String, price: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val account = getCurrentUserDirect() ?: return@launch
            if (account.balance >= price) {
                val newBalance = account.balance - price
                val durValue = if (tierName == "VIP2") _vip2DurationValue.value else _vip1DurationValue.value
                val durUnit = if (tierName == "VIP2") _vip2DurationUnit.value else _vip1DurationUnit.value
                val durationMs = calculateDurationMs(durValue, durUnit)
                val expiry = System.currentTimeMillis() + durationMs
                val roleLabel = if (tierName == "VIP2") "Hội viên VIP 2 Pro" else "Hội viên VIP 1"
                repository.saveUserAccount(
                    account.copy(
                        tier = tierName,
                        balance = newBalance,
                        expiryTimestamp = expiry,
                        customRole = roleLabel
                    )
                )
                
                // Log in transaction history
                repository.addTransaction(
                    TransactionItem(
                        username = account.username,
                        amount = -price,
                        type = if (tierName == "VIP2") "UPGRADE_VIP2" else "UPGRADE_VIP1",
                        status = "SUCCESS",
                        referenceNote = "Đăng ký gói VIP qua ví ($durValue $durUnit)"
                    )
                )
            }
        }
    }

    fun enableAdminMode() {
        viewModelScope.launch(Dispatchers.IO) {
            val account = getCurrentUserDirect() ?: return@launch
            repository.saveUserAccount(account.copy(tier = "ADMIN", customRole = "Quản trị viên tối cao"))
        }
    }

    fun logoutOrReset() {
        viewModelScope.launch(Dispatchers.IO) {
            val account = getCurrentUserDirect() ?: return@launch
            repository.saveUserAccount(
                account.copy(
                    tier = "UNPAID",
                    customRole = "Thành viên thường",
                    expiryTimestamp = 0L,
                    link1Passed = false,
                    link2Passed = false,
                    lastGeneratedCode = ""
                )
            )
        }
    }

    fun updateAccountTierAndRoleDirectly(newTier: String, newRoleLabel: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val account = getCurrentUserDirect() ?: return@launch
            val expiry = if (newTier == "ADMIN" || newTier == "STAFF" || newTier == "MANAGER") {
                0L // Infinite/Unrestricted
            } else if (newTier == "UNPAID") {
                0L
            } else {
                System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000) // 30 days
            }
            repository.saveUserAccount(
                account.copy(
                    tier = newTier,
                    customRole = newRoleLabel,
                    expiryTimestamp = expiry
                )
            )
            showToast("Đã bổ nhiệm chức vụ thành công: $newRoleLabel!")
        }
    }

    fun updateVipPrices(vip1: Double, vip2: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val account = getCurrentUserDirect() ?: return@launch
            repository.saveUserAccount(
                account.copy(
                    vip1Price = vip1.coerceAtLeast(0.0),
                    vip2Price = vip2.coerceAtLeast(0.0)
                )
            )
            showToast("Đã thay đổi giá kích hoạt thành công!")
        }
    }

    fun addNewGameAccount(title: String, name: String, price: Double, details: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newAcc = GameAccount(
                gameTitle = title,
                accountName = name,
                price = price.coerceAtLeast(0.0),
                details = details
            )
            repository.saveGameAccounts(listOf(newAcc))
            showToast("Đã thêm tài khoản game $title mới thành công!")
        }
    }

    fun setLinkPassed(linkIndex: Int, passed: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val account = getCurrentUserDirect() ?: return@launch
            val updated = if (linkIndex == 1) {
                account.copy(link1Passed = passed)
            } else {
                account.copy(link2Passed = passed)
            }
            repository.saveUserAccount(updated)
            if (passed) {
                showToast("Đã vượt thành công Liên kết $linkIndex!")
            }
        }
    }

    private val _isGeneratingPromoCode = MutableStateFlow(false)
    val isGeneratingPromoCode: StateFlow<Boolean> = _isGeneratingPromoCode.asStateFlow()

    private val _aiPromoCodeMessage = MutableStateFlow<String>("")
    val aiPromoCodeMessage: StateFlow<String> = _aiPromoCodeMessage.asStateFlow()

    fun generateAiPromoCode() {
        viewModelScope.launch(Dispatchers.IO) {
            val account = getCurrentUserDirect() ?: return@launch
            if (!account.link1Passed || !account.link2Passed) {
                showToast("Bạn cần vượt qua cả 2 liên kết trước khi nhận mã từ AI!")
                return@launch
            }

            _isGeneratingPromoCode.value = true
            _aiPromoCodeMessage.value = "AI đang phân tích & thiết kế mã quà tặng VIP 1..."

            val systemPrompt = """
                Bạn là mô-đun AI hóa tự động phát Giftcode của Tool Vip. Bạn hãy tạo ra một Giftcode ngẫu nhiên duy nhất bắt đầu bằng tiền tố 'VIP1_AI_...' và viết 1 lời chúc sành điệu, hào sảng bằng tiếng Việt dành riêng cho sếp (VD chúc sếp chơi game bất bại, mượt mà điện thoại).
                Định dạng trả về chính xác như sau:
                GIFTCODE: <mã_code_viet_hoa_viet_lien>
                CHÚC: <lời_chúc_máy_mượt_chơi_game_hay>
            """.trimIndent()

            val randomSuffix = (1000..9999).random()
            val promptText = "Hãy tạo mã Giftcode ngẫu nhiên có hậu tố $randomSuffix và viết lời chúc độc quyền."

            var reply = ""
            try {
                reply = GeminiClient.generateResponse(prompt = promptText, systemPrompt = systemPrompt)
            } catch (e: Exception) {
                reply = "GIFTCODE: VIP1_AI_LUCKY_$randomSuffix\nCHÚC: Chúc sếp dùng máy siêu mượt, quét sạch rác hệ thống và bất bại trên mọi trận đấu game!"
            }

            // Extract code
            val codeRegex = "GIFTCODE:\\s*(\\S+)".toRegex()
            val match = codeRegex.find(reply)
            val extractedCode = match?.groupValues?.get(1)?.trim() ?: "VIP1_AI_LUCKY_$randomSuffix"

            // Save in database
            repository.saveUserAccount(account.copy(lastGeneratedCode = extractedCode))
            
            _aiPromoCodeMessage.value = reply
            _isGeneratingPromoCode.value = false
            showToast("AI đã hoàn thiện mã Giftcode VIP 1!")
        }
    }

    fun submitPromoCode(enteredCode: String): Boolean {
        val account = userAccount.value ?: return false
        val cleanEntered = enteredCode.trim().uppercase()
        val cleanGenerated = account.lastGeneratedCode.trim().uppercase()

        // 1. Check custom gift codes
        val matchedCustomCode = _customGiftCodes.value.find { it.code == cleanEntered }
        if (matchedCustomCode != null) {
            if (matchedCustomCode.isUsed) {
                showToast("Mã quà tặng này đã được sử dụng từ trước!")
                return false
            }
            if (matchedCustomCode.expiryTimestamp > 0L && System.currentTimeMillis() > matchedCustomCode.expiryTimestamp) {
                showToast("Mã quà tặng này đã hết hạn sử dụng!")
                return false
            }
            // Mark as used
            val updatedList = _customGiftCodes.value.map {
                if (it.code == cleanEntered) it.copy(isUsed = true) else it
            }
            saveCustomGiftCodes(updatedList)

            viewModelScope.launch(Dispatchers.IO) {
                val durationMs = calculateDurationMs(matchedCustomCode.durationValue, matchedCustomCode.durationUnit)
                val expiry = System.currentTimeMillis() + durationMs
                
                val roleName = if (matchedCustomCode.tier == "VIP2") "Hội viên VIP 2 Pro" else "Hội viên VIP 1"
                repository.saveUserAccount(
                    account.copy(
                        tier = matchedCustomCode.tier,
                        customRole = "$roleName (Mã quà tặng)",
                        expiryTimestamp = expiry,
                        link1Passed = false,
                        link2Passed = false,
                        lastGeneratedCode = ""
                    )
                )
                val durationDesc = if (matchedCustomCode.durationUnit == "vĩnh viễn") "Vĩnh viễn" else "${matchedCustomCode.durationValue} ${matchedCustomCode.durationUnit}"
                showToast("Nhận thành công $durationDesc ${matchedCustomCode.tier} miễn phí từ mã quà tặng!")
            }
            return true
        }

        // 2. Check static/AI-generated promo codes
        val isStaticPromo = cleanEntered == "VIP1_FREE_MOBI" || 
                            cleanEntered == "VIP1_FREE" || 
                            cleanEntered == "VIP1_GIFT" || 
                            cleanEntered == "VIP1" || 
                            cleanEntered == "VIP1VIP" ||
                            cleanEntered == "VIP1_AI" ||
                            cleanEntered == "VIP"

        if (cleanEntered.isNotBlank() && (cleanEntered == cleanGenerated || isStaticPromo)) {
            viewModelScope.launch(Dispatchers.IO) {
                val expiry = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000) // 30 days of VIP 1
                repository.saveUserAccount(
                    account.copy(
                        tier = "VIP1",
                        customRole = "Hội viên VIP 1 (Mã quà tặng)",
                        expiryTimestamp = expiry,
                        link1Passed = false, // reset
                        link2Passed = false,
                        lastGeneratedCode = "" // consume
                    )
                )
                showToast("Nhận thành công 30 ngày VIP 1 miễn phí từ mã quà tặng!")
            }
            return true
        } else {
            showToast("Mã xác nhận không chính xác hoặc đã hết hạn!")
            return false
        }
    }

    private fun loadCustomGiftCodes() {
        val savedStr = sharedPrefs.getString("custom_gift_codes", "") ?: ""
        if (savedStr.isEmpty()) {
            _customGiftCodes.value = emptyList()
            return
        }
        val list = mutableListOf<CustomGiftCode>()
        savedStr.split(";").forEach { item ->
            val parts = item.split(",")
            if (parts.size >= 6) {
                list.add(
                    CustomGiftCode(
                        code = parts[0],
                        tier = parts[1],
                        durationValue = parts[2].toIntOrNull() ?: 30,
                        durationUnit = parts[3],
                        isUsed = parts[4] == "true",
                        expiryTimestamp = parts[5].toLongOrNull() ?: 0L
                    )
                )
            } else if (parts.size == 5) {
                list.add(
                    CustomGiftCode(
                        code = parts[0],
                        tier = parts[1],
                        durationValue = parts[2].toIntOrNull() ?: 30,
                        durationUnit = parts[3],
                        isUsed = parts[4] == "true",
                        expiryTimestamp = 0L
                    )
                )
            } else if (parts.size == 4) {
                // Backward compatibility
                list.add(
                    CustomGiftCode(
                        code = parts[0],
                        tier = parts[1],
                        durationValue = parts[2].toIntOrNull() ?: 30,
                        durationUnit = "ngày",
                        isUsed = parts[3] == "true",
                        expiryTimestamp = 0L
                    )
                )
            }
        }
        _customGiftCodes.value = list
    }

    private fun saveCustomGiftCodes(list: List<CustomGiftCode>) {
        val serialized = list.joinToString(";") { "${it.code},${it.tier},${it.durationValue},${it.durationUnit},${it.isUsed},${it.expiryTimestamp}" }
        sharedPrefs.edit().putString("custom_gift_codes", serialized).apply()
        _customGiftCodes.value = list
    }

    fun addCustomGiftCode(code: String, tier: String, durationValue: Int, durationUnit: String, expiryTimestamp: Long = 0L) {
        val current = _customGiftCodes.value.toMutableList()
        val cleanCode = code.trim().uppercase()
        current.removeAll { it.code == cleanCode }
        current.add(CustomGiftCode(cleanCode, tier, durationValue, durationUnit, false, expiryTimestamp))
        saveCustomGiftCodes(current)
        val durationDesc = if (durationUnit == "vĩnh viễn") "Vĩnh viễn" else "$durationValue $durationUnit"
        val expiryDesc = if (expiryTimestamp > 0L) {
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
            "Hạn: " + sdf.format(java.util.Date(expiryTimestamp))
        } else {
            "Không hết hạn"
        }
        showToast("Đã tạo thành công mã Giftcode: $cleanCode ($tier - $durationDesc) • $expiryDesc")
    }

    fun deleteCustomGiftCode(code: String) {
        val current = _customGiftCodes.value.toMutableList()
        current.removeAll { it.code == code.trim().uppercase() }
        saveCustomGiftCodes(current)
        showToast("Đã xóa mã Giftcode: ${code.trim().uppercase()}")
    }

    fun revokePromoCode() {
        val account = userAccount.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveUserAccount(
                account.copy(
                    tier = "UNPAID",
                    customRole = "Thành viên thường",
                    expiryTimestamp = 0L,
                    lastGeneratedCode = ""
                )
            )
            showToast("Đã gỡ bỏ mã quà tặng thành công!")
        }
    }

    fun setAutoRenew(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val account = getCurrentUserDirect() ?: return@launch
            repository.saveUserAccount(account.copy(isAutoRenew = enabled))
        }
    }

    fun setAutoFreeze(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val account = getCurrentUserDirect() ?: return@launch
            repository.saveUserAccount(account.copy(isAutoFreeze = enabled))
        }
    }

    fun setFreezeThreshold(threshold: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val account = getCurrentUserDirect() ?: return@launch
            repository.saveUserAccount(account.copy(freezeThreshold = threshold))
        }
    }

    // --- GAME SHOPPING SYSTEM ---
    fun purchaseGameAccount(gameAccount: GameAccount) {
        viewModelScope.launch(Dispatchers.IO) {
            val account = getCurrentUserDirect() ?: return@launch
            if (account.balance >= gameAccount.price && !gameAccount.isBought) {
                val newBalance = account.balance - gameAccount.price
                repository.saveUserAccount(account.copy(balance = newBalance))
                repository.updateGameAccount(gameAccount.copy(isBought = true, buyerEmail = "quanghuypham1789@gmail.com"))
            }
        }
    }

    // --- APP FREEZING & UNFREEZING ENGINE ---
    fun toggleFreezeApp(app: AppItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val newFrozenState = !app.isFrozen
            repository.updateApp(app.copy(isFrozen = newFrozenState))
            val msg = if (newFrozenState) "Đã đóng băng thành công ${app.appName}!" else "Đã rã băng thành công ${app.appName}!"
            showToast(msg)
        }
    }

    fun toggleFreezeSelected() {
        viewModelScope.launch(Dispatchers.IO) {
            val appsToModify = allApps.value.filter { selectedApps.value.contains(it.packageName) }
            val updated = appsToModify.map { it.copy(isFrozen = !it.isFrozen) }
            repository.updateApps(updated)
            selectedApps.value = emptySet()
            showToast("Đã cập nhật trạng thái đóng băng cho các ứng dụng đã chọn!")
        }
    }

    fun freezeAllSelected() {
        viewModelScope.launch(Dispatchers.IO) {
            val appsToModify = allApps.value.filter { selectedApps.value.contains(it.packageName) }
            val updated = appsToModify.map { it.copy(isFrozen = true) }
            repository.updateApps(updated)
            selectedApps.value = emptySet()
            showToast("Đã đóng băng tất cả ứng dụng đã chọn thành công!")
        }
    }

    fun unfreezeAllSelected() {
        viewModelScope.launch(Dispatchers.IO) {
            val appsToModify = allApps.value.filter { selectedApps.value.contains(it.packageName) }
            val updated = appsToModify.map { it.copy(isFrozen = false) }
            repository.updateApps(updated)
            selectedApps.value = emptySet()
            showToast("Đã rã băng tất cả ứng dụng đã chọn thành công!")
        }
    }

    fun optimizeRamDirectly(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isOptimizing.value = true
            _optimizationMessage.value = "Đang khởi động AI quét hệ thống..."
            delay(1000)
            _optimizationMessage.value = "Phân tích RAM tiêu hao của các app rác..."
            delay(1200)
            _optimizationMessage.value = "Đang dọn dẹp cache và giải phóng băng thông mạng..."
            delay(1500)

            // Auto-freeze low priority/trash apps if auto-freeze is active
            val apps = allApps.value
            val trashApps = apps.filter { it.isTrash && !it.isFrozen }
            if (trashApps.isNotEmpty()) {
                viewModelScope.launch(Dispatchers.IO) {
                    val updated = trashApps.map { it.copy(isFrozen = true) }
                    repository.updateApps(updated)
                }
                _optimizationMessage.value = "AI đã đóng băng ${trashApps.size} ứng dụng rác chạy ngầm!"
                delay(1200)
            }

            val reclaimedRam = if (trashApps.isNotEmpty()) trashApps.sumOf { it.ramUsage } else 1420.0
            val cleanedCount = if (trashApps.isNotEmpty()) trashApps.size else 6
            addOptimizationCycle(cleanedCount, reclaimedRam, "Tối ưu RAM")

            _isOptimizing.value = false
            _optimizationMessage.value = "Tối ưu RAM thành công! Giải phóng thêm ${(reclaimedRam / 1024.0).let { String.format("%.1f", it) }}GB RAM."
            onComplete()
        }
    }

    // --- AI BOT & CHAT SYSTEM ---
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            // Save User message
            val userMsg = ChatMessage(sender = "USER", message = text)
            repository.addMessage(userMsg)

            _isAiLoading.value = true

            // Formulate prompt with full system instructions to enforce roles
            val systemPrompt = """
                Bạn là Siêu Trí Tuệ Nhân Tạo (Tool Vip AI Bot) - trợ lý chuyên sâu bậc nhất dành cho game thủ và quản lý hệ thống.
                Hãy trả lời thông minh, chuyên nghiệp và đầy cuốn hút về tất cả các khía cạnh:
                1. Cách lên đồ (build đồ), bảng ngọc, phù hiệu và cách combo chuẩn xác cho các vị tướng Liên Quân Mobile (như Florentino, Nakroth, Raz, Elsu, Yorn, Capheny, Tulen, v.v.). Tư vấn cả lối đi đường, cách khắc chế cực kỳ thuyết phục và am hiểu.
                2. Độ nhạy Free Fire (Độ nhạy FF), nút bắn và cấu hình DPI tối ưu nhất cho từng dòng máy (iPhone, Samsung, Oppo, Xiaomi, Realme) giúp kéo tâm siêu mượt và dễ dàng bắn headshot.
                3. Cách vận hành app Tool Vip, kích hoạt Đóng băng sâu (Deep Freeze), tối ưu hóa RAM, giảm ping, tăng độ nhạy màn hình và mở khóa 60fps/120fps cho mọi tựa game.
                4. Giải thích các gói VIP của ứng dụng:
                   - VIP 1: Tối ưu chạm nhạy, DNS ưu tiên, mở khóa cấu hình FPS cơ bản.
                   - VIP 2: Đóng băng ứng dụng nâng cao, quét dọn sâu bằng AI nền, ưu tiên luồng xử lý CPU/GPU.
                   - ADMIN: Toàn quyền quản lý, kiểm soát dòng tiền, tự tạo Giftcode thời hạn tùy thích (1 ngày, 1 tuần, 1 tháng, vĩnh viễn) cho thành viên.

                Hãy nói tiếng Việt tự nhiên, sành điệu, hào sảng của một game thủ Esports chuyên nghiệp. Sử dụng gạch đầu dòng rõ ràng và bố cục trực quan để câu trả lời dễ đọc nhất!
            """.trimIndent()

            // Call Gemini
            val reply = GeminiClient.generateResponse(prompt = text, systemPrompt = systemPrompt)

            // Save AI reply
            val aiMsg = ChatMessage(sender = "AI", message = reply)
            repository.addMessage(aiMsg)

            _isAiLoading.value = false

            // Interpret special command texts inside chat to trigger actions!
            interpretCommand(text)
        }
    }

    private fun interpretCommand(text: String) {
        val lowerText = text.trim().lowercase()
        if (lowerText.contains("tối ưu") || lowerText.contains("ram") || lowerText.contains("dọn rác")) {
            optimizeRamDirectly()
        }

        // Support change password via AI Chat: "đổi mật khẩu thành <new_password>" or "đổi mk <new_password>"
        if (lowerText.contains("đổi mật khẩu") || lowerText.contains("đổi mk") || lowerText.contains("thay mk")) {
            val parts = text.split("\\s+".toRegex())
            val keywords = listOf("đổi", "mật", "khẩu", "thành", "mk", "thay")
            val filtered = parts.filter { it.lowercase() !in keywords }
            if (filtered.isNotEmpty()) {
                val newPass = filtered.last().trim()
                if (newPass.length >= 4) {
                    viewModelScope.launch(Dispatchers.IO) {
                        val current = currentUsername.value
                        if (current != null) {
                            val account = repository.getUserAccountDirect(current)
                            if (account != null) {
                                repository.saveUserAccount(account.copy(passwordHash = newPass))
                                delay(800)
                                repository.addMessage(
                                    ChatMessage(
                                        sender = "AI",
                                        message = "🤖 AI Bot: Đã đổi mật khẩu tài khoản [$current] thành [$newPass] thành công theo yêu cầu hỗ trợ đổi mật khẩu!"
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearChatHistory()
        }
    }

    // --- AI SCANNER & AUTO-FREEZE STATES ---
    private val _vip1DurationValue = MutableStateFlow(sharedPrefs.getInt("vip1_duration_value", 30))
    val vip1DurationValue: StateFlow<Int> = _vip1DurationValue.asStateFlow()

    private val _vip1DurationUnit = MutableStateFlow(sharedPrefs.getString("vip1_duration_unit", "ngày") ?: "ngày")
    val vip1DurationUnit: StateFlow<String> = _vip1DurationUnit.asStateFlow()

    private val _vip2DurationValue = MutableStateFlow(sharedPrefs.getInt("vip2_duration_value", 30))
    val vip2DurationValue: StateFlow<Int> = _vip2DurationValue.asStateFlow()

    private val _vip2DurationUnit = MutableStateFlow(sharedPrefs.getString("vip2_duration_unit", "ngày") ?: "ngày")
    val vip2DurationUnit: StateFlow<String> = _vip2DurationUnit.asStateFlow()

    fun updateVipConfig(tier: String, value: Int, unit: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val validValue = value.coerceAtLeast(1)
            if (tier == "VIP1") {
                _vip1DurationValue.value = validValue
                _vip1DurationUnit.value = unit
                sharedPrefs.edit()
                    .putInt("vip1_duration_value", validValue)
                    .putString("vip1_duration_unit", unit)
                    .apply()
            } else {
                _vip2DurationValue.value = validValue
                _vip2DurationUnit.value = unit
                sharedPrefs.edit()
                    .putInt("vip2_duration_value", validValue)
                    .putString("vip2_duration_unit", unit)
                    .apply()
            }
            showToast("Đã lưu thiết lập thời hạn gói $tier thành công!")
        }
    }

    fun updateVipPricesGlobally(vip1: Double, vip2: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            sharedPrefs.edit()
                .putFloat("default_vip1_price", vip1.toFloat())
                .putFloat("default_vip2_price", vip2.toFloat())
                .apply()
            
            val accounts = repository.allUserAccounts.first()
            for (acc in accounts) {
                repository.saveUserAccount(acc.copy(
                    vip1Price = vip1.coerceAtLeast(0.0),
                    vip2Price = vip2.coerceAtLeast(0.0)
                ))
            }
            showToast("Đã cập nhật giá VIP 1 & VIP 2 hệ thống!")
        }
    }

    private val _aiScanSuggestions = MutableStateFlow<String>("Nhấn nút 'Quét & Đề xuất AI' để quét và nhận đề xuất tối ưu từ Gemini.")
    val aiScanSuggestions: StateFlow<String> = _aiScanSuggestions.asStateFlow()

    private val _isAiScanning = MutableStateFlow(false)
    val isAiScanning: StateFlow<Boolean> = _isAiScanning.asStateFlow()

    private val _isDeepFreezeEnabled = MutableStateFlow(false)
    val isDeepFreezeEnabled: StateFlow<Boolean> = _isDeepFreezeEnabled.asStateFlow()

    private val _isBackgroundPreventionEnabled = MutableStateFlow(false)
    val isBackgroundPreventionEnabled: StateFlow<Boolean> = _isBackgroundPreventionEnabled.asStateFlow()

    private val _selectedGameForAiOpt = MutableStateFlow("Liên Quân Mobile")
    val selectedGameForAiOpt: StateFlow<String> = _selectedGameForAiOpt.asStateFlow()

    private val _aiGameOptResult = MutableStateFlow<String>("Chọn một tựa game phía trên rồi nhấn 'Phân tích hồ sơ game' để nhận đề xuất tối ưu FPS cao nhất từ Gemini.")
    val aiGameOptResult: StateFlow<String> = _aiGameOptResult.asStateFlow()

    private val _isAiGameOptimizing = MutableStateFlow(false)
    val isAiGameOptimizing: StateFlow<Boolean> = _isAiGameOptimizing.asStateFlow()

    private val _isDeepCleaning = MutableStateFlow(false)
    val isDeepCleaning: StateFlow<Boolean> = _isDeepCleaning.asStateFlow()

    private val _deepCleanStatus = MutableStateFlow<String>("Nhấn nút 'DỌN DẸP SÂU (DEEP CLEAN)' để giải phóng triệt để bộ nhớ đệm cache của tất cả các ứng dụng rác và mạng xã hội ngầm.")
    val deepCleanStatus: StateFlow<String> = _deepCleanStatus.asStateFlow()

    fun toggleDeepFreeze(enabled: Boolean) {
        val tier = userAccount.value?.tier ?: "UNPAID"
        if (tier == "UNPAID") {
            showToast("Tính năng Đóng băng sâu (Deep Freeze) yêu cầu đặc quyền VIP 1 trở lên!")
            return
        }
        _isDeepFreezeEnabled.value = enabled
        if (enabled) {
            viewModelScope.launch(Dispatchers.IO) {
                // Find non-system, non-frozen apps that use more than 150MB of RAM (resource-heavy non-essential)
                val heavyApps = allApps.value.filter { 
                    !it.isSystemApp && !it.isFrozen && it.ramUsage > 150.0 
                }
                if (heavyApps.isNotEmpty()) {
                    val updated = heavyApps.map { it.copy(isFrozen = true) }
                    repository.updateApps(updated)
                    val appNames = heavyApps.joinToString(", ") { it.appName }
                    showToast("❄️ Deep Freeze đã tự động đóng băng các ứng dụng tốn tài nguyên: $appNames để tối ưu chiến game mượt nhất!")
                } else {
                    showToast("❄️ Đã bật Deep Freeze! Không tìm thấy ứng dụng chạy ngầm nặng nào.")
                }
            }
        } else {
            showToast("Đã tắt Đóng băng sâu (Deep Freeze).")
        }
    }

    fun toggleBackgroundPrevention(enabled: Boolean) {
        val tier = userAccount.value?.tier ?: "UNPAID"
        if (tier == "UNPAID") {
            showToast("Tính năng chặn chạy ngầm yêu cầu đặc quyền VIP 1 trở lên!")
            return
        }
        _isBackgroundPreventionEnabled.value = enabled
        showToast(if (enabled) "Đã kích hoạt ngăn chặn chạy ngầm tuyệt đối!" else "Đã tắt ngăn chặn chạy ngầm.")
    }

    fun selectGameForAiOpt(gameTitle: String) {
        _selectedGameForAiOpt.value = gameTitle
    }

    fun addNewCustomGame(appName: String, packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newGame = com.example.data.AppItem(
                packageName = packageName,
                appName = appName,
                isSystemApp = false,
                isFrozen = false,
                ramUsage = (320..880).random().toDouble(),
                isTrash = false
            )
            repository.saveApps(listOf(newGame))
        }
    }

    fun runAiGameSpecificOptimization(gameTitle: String) {
        viewModelScope.launch {
            _isAiGameOptimizing.value = true
            _aiGameOptResult.value = "AI đang thiết lập môi trường phân tích tối ưu cho $gameTitle..."
            
            val apps = allApps.value
            val activeApps = apps.filter { !it.isFrozen }
            if (activeApps.isEmpty()) {
                _aiGameOptResult.value = "Tất cả ứng dụng chạy ngầm không thiết yếu đã được đóng băng. Môi trường chơi game $gameTitle cực kỳ lý tưởng!"
                _isAiGameOptimizing.value = false
                return@launch
            }

            val appSummary = activeApps.joinToString("\n") { 
                "- Tên: ${it.appName}, Gói: ${it.packageName}, RAM: ${it.ramUsage}MB, Hệ thống: ${if (it.isSystemApp) "Có" else "Không"}" 
            }

            val systemPrompt = """
                Bạn là Chuyên gia tối ưu hoá phần cứng & Hệ thống Gaming của Tool Vip. Nhiệm vụ của bạn là đưa ra hồ sơ tối ưu hoá tối đa RAM và CPU cho game [$gameTitle].
                Hãy dựa trên danh sách tiến trình chạy ngầm gửi lên để phân tích xem những tiến trình nào nên bị đóng băng để tránh lag giật, rớt khung hình (FPS drop) khi chơi game.
                Hãy trình bày thật ngắn gọn, sinh động, dễ đọc bằng tiếng Việt gồm các phần sau:
                1. ĐÁNH GIÁ MÔI TRƯỜNG GAMING (Nêu nhiệt độ, RAM dư giả thế nào)
                2. DỰ BÁO HIỆU NĂNG CHO GAME $gameTitle (Dự báo FPS đạt được sau khi đóng băng các ứng dụng không cần thiết)
                3. ĐỀ XUẤT ĐÓNG BĂNG CHI TIẾT (Liệt kê rõ tên và gói ứng dụng cần đóng băng ngay)
                Cuối cùng hãy trả về chính xác danh sách các gói ứng dụng (packageName) đề xuất đóng băng ở dòng cuối cùng có định dạng:
                [PACKAGES_TO_FREEZE]: com.example.app1, com.example.app2
            """.trimIndent()

            val promptText = "Hãy phân tích danh sách và tối ưu cho tựa game: $gameTitle\n\n$appSummary"

            val response = GeminiClient.generateResponse(prompt = promptText, systemPrompt = systemPrompt)
            _aiGameOptResult.value = response
            _isAiGameOptimizing.value = false
            showToast("Hồ sơ tối ưu AI cho game $gameTitle đã được chuẩn bị!")
        }
    }

    fun applyAiGameSpecificFreeze() {
        val text = _aiGameOptResult.value
        if (!text.contains("[PACKAGES_TO_FREEZE]")) {
            showToast("Chưa có đề xuất đóng băng nào mới được chuẩn bị.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val lines = text.split("\n")
            val packagesLine = lines.firstOrNull { it.contains("[PACKAGES_TO_FREEZE]") } ?: ""
            val packagesStr = packagesLine.replace("[PACKAGES_TO_FREEZE]:", "").trim()
            val packagesList = packagesStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }

            if (packagesList.isEmpty()) {
                showToast("Không tìm thấy gói ứng dụng nào trong đề xuất.")
                return@launch
            }

            val apps = allApps.value
            val appsToFreeze = apps.filter { packagesList.contains(it.packageName) && !it.isFrozen }

            if (appsToFreeze.isNotEmpty()) {
                val updated = appsToFreeze.map { it.copy(isFrozen = true) }
                repository.updateApps(updated)
                showToast("AI đã đóng băng thành công ${appsToFreeze.size} ứng dụng tối ưu cho game!")
            } else {
                showToast("Các ứng dụng đề xuất đã được đóng băng từ trước.")
            }
        }
    }

    fun runAiAppScan() {
        viewModelScope.launch {
            _isAiScanning.value = true
            _aiScanSuggestions.value = "Đang quét danh sách tiến trình ngầm..."
            
            val apps = allApps.value
            if (apps.isEmpty()) {
                _aiScanSuggestions.value = "Không tìm thấy ứng dụng nào để phân tích."
                _isAiScanning.value = false
                return@launch
            }

            val appSummary = apps.joinToString("\n") { 
                "- Tên: ${it.appName}, Gói: ${it.packageName}, RAM: ${it.ramUsage}MB, Rác: ${if (it.isTrash) "Có" else "Không"}, Hệ thống: ${if (it.isSystemApp) "Có" else "Không"}, Trạng thái: ${if (it.isFrozen) "Đã đóng băng" else "Đang chạy"}" 
            }

            val systemPrompt = """
                Bạn là Chuyên gia AI Tối ưu hóa của Tool Vip. Nhiệm vụ của bạn là phân tích danh sách ứng dụng chạy ngầm của người dùng và đưa ra đề xuất đóng băng những ứng dụng không thiết yếu (đặc biệt là ứng dụng rác, mạng xã hội, mua sắm) để giải phóng RAM tối đa mà không ảnh hưởng tới hoạt động của hệ thống.
                Hãy viết lời nhận định thật sắc sảo, ngắn gọn bằng tiếng Việt (3-4 câu), nêu rõ tổng lượng RAM ước tính có thể giải phóng, và liệt kê rõ tên các ứng dụng khuyên người dùng nên đóng băng ngay lập tức.
            """.trimIndent()

            val promptText = "Hãy phân tích danh sách ứng dụng chạy ngầm sau đây và đưa ra đề xuất tối ưu chi tiết:\n\n$appSummary"

            val response = GeminiClient.generateResponse(prompt = promptText, systemPrompt = systemPrompt)
            _aiScanSuggestions.value = response
            _isAiScanning.value = false
            showToast("AI đã hoàn thành quét tài nguyên hệ thống!")
        }
    }

    fun runAiAutoFreeze() {
        viewModelScope.launch {
            _isAiScanning.value = true
            _aiScanSuggestions.value = "AI đang tự động phân tích và ra quyết định đóng băng..."

            val apps = allApps.value
            val activeApps = apps.filter { !it.isFrozen }
            if (activeApps.isEmpty()) {
                _aiScanSuggestions.value = "Tất cả ứng dụng rác và ứng dụng không thiết yếu đã được đóng băng sạch sẽ!"
                _isAiScanning.value = false
                showToast("AI quét tài nguyên hoàn tất: Hệ thống đã sạch rác!")
                return@launch
            }

            val appSummary = activeApps.joinToString("\n") { 
                "- Tên: ${it.appName}, Gói: ${it.packageName}, RAM: ${it.ramUsage}MB, Rác: ${if (it.isTrash) "Có" else "Không"}, Hệ thống: ${if (it.isSystemApp) "Có" else "Không"}" 
            }

            val systemPrompt = """
                Bạn là mô-đun Tự Động Đóng Băng AI hóa của Tool Vip. Bạn phải quyết định đóng băng các ứng dụng không thiết yếu và ứng dụng rác chạy ngầm dựa trên danh sách gửi lên.
                Yêu cầu trả về chính xác định dạng JSON như sau:
                {
                  "packagesToFreeze": ["com.example.app1", "com.example.app2"],
                  "explanation": "Lời giải thích ngắn gọn bằng tiếng Việt về lý do AI tự động đóng băng các ứng dụng này."
                }
                Chỉ trả về chuỗi JSON thô, không chứa thẻ markdown ```json hay ký tự thừa nào ngoài JSON.
            """.trimIndent()

            val promptText = "Hãy phân tích danh sách và ra quyết định đóng băng tự động:\n\n$appSummary"

            val response = GeminiClient.generateResponse(prompt = promptText, systemPrompt = systemPrompt)
            
            val jsonClean = response.trim().removeSurrounding("```json", "```").trim()
            val appsToFreeze = activeApps.filter { app ->
                jsonClean.contains(app.packageName)
            }

            if (appsToFreeze.isNotEmpty()) {
                val updated = appsToFreeze.map { it.copy(isFrozen = true) }
                repository.updateApps(updated)
                
                val explanation = if (jsonClean.contains("\"explanation\"")) {
                    val expIndex = jsonClean.indexOf("\"explanation\"")
                    val sub = jsonClean.substring(expIndex)
                    val quoteStart = sub.indexOf(":") + 1
                    val subQuote = sub.substring(quoteStart).trim()
                    val firstQuote = subQuote.indexOf("\"")
                    val secondQuote = subQuote.indexOf("\"", firstQuote + 1)
                    if (firstQuote != -1 && secondQuote != -1) {
                        subQuote.substring(firstQuote + 1, secondQuote)
                    } else {
                        "Đã đóng băng ${appsToFreeze.size} ứng dụng chạy ngầm không thiết yếu."
                    }
                } else {
                    "Đã đóng băng ${appsToFreeze.size} ứng dụng chạy ngầm không thiết yếu."
                }

                _aiScanSuggestions.value = "🤖 **QUYẾT ĐỊNH AI AUTO-FREEZE**:\n\n" +
                        "$explanation\n\n" +
                        "**Ứng dụng đã tự động đóng băng:**\n" +
                        appsToFreeze.joinToString("\n") { "✅ ${it.appName} (${it.ramUsage} MB)" }
                
                showToast("AI đã hoàn thành quét tài nguyên và tự động đóng băng rác!")
            } else {
                _aiScanSuggestions.value = "🤖 **QUYẾT ĐỊNH AI AUTO-FREEZE**:\n\n" +
                        "AI nhận thấy không có ứng dụng chạy ngầm nào cần đóng băng tại thời điểm này để đảm bảo hệ thống mượt mà và an toàn."
                showToast("AI đã hoàn thành quét tài nguyên hệ thống!")
            }
            
            _isAiScanning.value = false
        }
    }

    fun setScheduledOptEnabled(enabled: Boolean) {
        val tier = userAccount.value?.tier ?: "UNPAID"
        if (tier == "UNPAID") {
            showToast("Tự động tối ưu định kỳ yêu cầu đặc quyền VIP 1 trở lên!")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val account = getCurrentUserDirect() ?: return@launch
            repository.saveUserAccount(account.copy(isScheduledOptEnabled = enabled))
            if (enabled) {
                lastScheduledOptTime = System.currentTimeMillis()
                showToast("Đã bật tự động tối ưu hóa định kỳ!")
            } else {
                showToast("Đã tắt tự động tối ưu hóa định kỳ.")
            }
        }
    }

    fun setOptIntervalMinutes(minutes: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val account = getCurrentUserDirect() ?: return@launch
            repository.saveUserAccount(account.copy(optIntervalMinutes = minutes))
            lastScheduledOptTime = System.currentTimeMillis()
            showToast("Đã đặt khoảng thời gian tối ưu hóa thành $minutes phút!")
        }
    }

    private fun startScheduledOptimizationJob() {
        viewModelScope.launch(Dispatchers.IO) {
            delay(5000)
            while (true) {
                try {
                    // Slowly pile up junk cache in the background (0.1MB to 0.6MB every 10 seconds)
                    val currentJunk = _simulatedJunkSizeMb.value
                    if (currentJunk < 1024f) { // cap at 1GB
                        val addedJunk = java.util.concurrent.ThreadLocalRandom.current().nextDouble(0.1, 0.6).toFloat()
                        _simulatedJunkSizeMb.value = currentJunk + addedJunk
                    }

                    val account = getCurrentUserDirect()
                    if (account != null && account.isScheduledOptEnabled) {
                        val isIdleCorrect = !_isIdleModeOnlyEnabled.value || _isDeviceIdle.value
                        if (isIdleCorrect) {
                            val now = System.currentTimeMillis()
                            val intervalMs = account.optIntervalMinutes * 60 * 1000L
                            if (lastScheduledOptTime == 0L) {
                                lastScheduledOptTime = now
                            } else if (now - lastScheduledOptTime >= intervalMs) {
                                lastScheduledOptTime = now
                                runAiScheduledOptimization()
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(10000) // check state every 10 seconds
            }
        }
    }

    private fun runAiScheduledOptimization() {
        viewModelScope.launch {
            _isAiScanning.value = true
            _aiScanSuggestions.value = "🤖 [AUTOMATED] AI đang tự động dọn dẹp & tối ưu hệ thống định kỳ..."
            
            val apps = allApps.value
            val activeApps = apps.filter { !it.isFrozen }
            
            val appSummary = activeApps.joinToString("\n") { 
                "- Tên: ${it.appName}, Gói: ${it.packageName}, RAM: ${it.ramUsage}MB, Rác: ${if (it.isTrash) "Có" else "Không"}" 
            }

            val systemPrompt = """
                Bạn là hệ thống Tối ưu hóa Tự Động Định Kỳ của Tool Vip. Nhiệm vụ của bạn là dọn dẹp RAM bằng cách tự động đóng băng các ứng dụng chạy ngầm không thiết yếu (mạng xã hội, mua sắm, ứng dụng rác).
                Hãy trả về JSON thô duy nhất có định dạng:
                {
                  "packagesToFreeze": ["com.example.app1"],
                  "explanation": "Đã tự động đóng băng các tiến trình chạy ngầm để giải phóng bộ nhớ."
                }
            """.trimIndent()

            val response = GeminiClient.generateResponse(
                prompt = "Tối ưu hóa định kỳ hệ thống:\n\n$appSummary",
                systemPrompt = systemPrompt
            )

            val jsonClean = response.trim().removeSurrounding("```json", "```").trim()
            val appsToFreeze = activeApps.filter { app ->
                jsonClean.contains(app.packageName)
            }

            // Clear cache and free RAM
            val clearedCacheMB = _simulatedJunkSizeMb.value
            _simulatedJunkSizeMb.value = java.util.concurrent.ThreadLocalRandom.current().nextDouble(1.5, 4.5).toFloat() // clear cache back to minimal values

            val reclaimedRamFromApps = appsToFreeze.sumOf { it.ramUsage }.toDouble()
            val totalReclaimedRamMB = reclaimedRamFromApps + clearedCacheMB

            if (appsToFreeze.isNotEmpty()) {
                val updated = appsToFreeze.map { it.copy(isFrozen = true) }
                repository.updateApps(updated)
            }

            // Always register the optimization cycle
            addOptimizationCycle(
                processesCleaned = appsToFreeze.size + 3, // apps + system junk processes
                ramReclaimedMb = totalReclaimedRamMB,
                type = "Tự động định kỳ (AI)"
            )

            // Dynamic ping booster status
            val pingStatusMsg = if (_networkPingBoosted.value) {
                "\n📶 [PING BOOSTER] Đã kích hoạt định tuyến DNS ưu tiên. Giảm Ping mạng xuống ổn định 18ms - 25ms."
            } else ""

            _aiScanSuggestions.value = "🤖 **TỐI ƯU HÓA ĐỊNH KỲ HOÀN TẤT (KHI THIẾT BỊ RẢNH)**:\n\n" +
                    "Hệ thống đã tự động dọn dẹp sạch ${String.format("%.1f", clearedCacheMB)} MB tệp bộ nhớ đệm tạm thời (temp cache) và giải phóng các tiến trình rác chạy ngầm.\n\n" +
                    "**Kết quả tối ưu:**\n" +
                    "• Đã thu hồi: **${String.format("%.1f", totalReclaimedRamMB)} MB** bộ nhớ RAM.\n" +
                    "• Số ứng dụng không thiết yếu đã đóng băng: **${appsToFreeze.size} ứng dụng**.\n" +
                    "• Hạ nhiệt CPU thành công, khôi phục tốc độ ban đầu.$pingStatusMsg"

            viewModelScope.launch(Dispatchers.IO) {
                val frozenAppNames = if (appsToFreeze.isNotEmpty()) " và đóng băng ${appsToFreeze.size} ứng dụng ngầm (${appsToFreeze.joinToString { it.appName }})" else ""
                repository.addMessage(
                    ChatMessage(
                        sender = "AI",
                        message = "🤖 [TỰ ĐỘNG TỐI ƯU] Đã dọn dẹp sạch ${String.format("%.1f", clearedCacheMB)} MB cache rác$frozenAppNames thành công khi thiết bị ở trạng thái rảnh.$pingStatusMsg"
                    )
                )
            }

            showToast("AI đã tự động tối ưu cache & giải phóng RAM thành công!")
            _isAiScanning.value = false
        }
    }

    fun runDeepClean() {
        viewModelScope.launch {
            _isDeepCleaning.value = true
            _deepCleanStatus.value = "⚡ Khởi tạo Công cụ dọn dẹp chuyên sâu Deep Clean...\n"
            delay(800)
            
            _deepCleanStatus.value += "🔍 Đang quét phân vùng bộ nhớ đệm (Cache Partition)...\n"
            delay(1000)

            val apps = allApps.value
            val nonEssentialApps = apps.filter { !it.isSystemApp || it.isTrash }
            
            if (nonEssentialApps.isEmpty()) {
                _deepCleanStatus.value += "✨ Không tìm thấy tệp cache rác nào từ các ứng dụng không thiết yếu."
                _isDeepCleaning.value = false
                showToast("Hệ thống đã sạch sẽ hoàn hảo!")
                return@launch
            }

            var totalClearedMB = 0.0
            val updatedApps = mutableListOf<AppItem>()
            val logBuilder = StringBuilder(_deepCleanStatus.value)

            nonEssentialApps.forEach { app ->
                if (app.ramUsage > 30.0) {
                    val baseRam = (15..35).random().toDouble()
                    val cleared = app.ramUsage - baseRam
                    if (cleared > 0) {
                        totalClearedMB += cleared
                        updatedApps.add(app.copy(ramUsage = baseRam, lastOptimized = System.currentTimeMillis()))
                        logBuilder.append("🧹 [${app.appName}] -> Đã dọn dẹp bộ nhớ đệm: ${String.format("%.1f", cleared)} MB (RAM giảm còn ${String.format("%.1f", baseRam)} MB)\n")
                        _deepCleanStatus.value = logBuilder.toString()
                        delay(450)
                    }
                } else {
                    logBuilder.append("✓ [${app.appName}] -> Bộ nhớ đệm đã được dọn dẹp tối ưu từ trước\n")
                    _deepCleanStatus.value = logBuilder.toString()
                    delay(250)
                }
            }

            if (updatedApps.isNotEmpty()) {
                repository.updateApps(updatedApps)
            }

            logBuilder.append("\n🎉 **HOÀN THÀNH DỌN DẸP SÂU**:\n")
            logBuilder.append("⭐ Tổng dung lượng cache đã dọn dẹp: ${String.format("%.1f", totalClearedMB)} MB RAM.\n")
            logBuilder.append("⚡ Trạng thái phân vùng hệ thống: HOÀN HẢO (EXCELLENT).\n")
            _deepCleanStatus.value = logBuilder.toString()

            viewModelScope.launch(Dispatchers.IO) {
                repository.addMessage(
                    ChatMessage(
                        sender = "AI",
                        message = "🤖 [DEEP CLEAN COMPLETE] Đã dọn dẹp sạch sẽ bộ nhớ đệm của các ứng dụng chạy ngầm không thiết yếu. Tổng dung lượng bộ nhớ đệm đã giải phóng thành công: ${String.format("%.1f", totalClearedMB)} MB RAM."
                    )
                )
            }

            showToast("Deep Clean hoàn thành! Giải phóng ${String.format("%.1f", totalClearedMB)} MB cache.")
            _isDeepCleaning.value = false
        }
    }

    // Battery state properties moved to top of class to avoid class initialization order NullPointerException

    fun refreshBatteryStatus() {
        val context = getApplication<Application>()
        try {
            val intentFilter = android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = context.registerReceiver(null, intentFilter)
            if (batteryStatus != null) {
                val level = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1)
                val scale = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1)
                val pct = if (scale > 0) (level * 100 / scale) else 95
                _batteryLevel.value = pct

                val tempDeci = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_TEMPERATURE, -1)
                val tempC = if (tempDeci != -1) (tempDeci / 10.0) else 35.2
                _batteryTemp.value = "${String.format("%.1f", tempC)}°C"

                val healthConst = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_HEALTH, -1)
                val healthStr = when (healthConst) {
                    android.os.BatteryManager.BATTERY_HEALTH_GOOD -> "Tốt (Good)"
                    android.os.BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Quá nhiệt (Overheat)"
                    android.os.BatteryManager.BATTERY_HEALTH_DEAD -> "Hỏng (Dead)"
                    android.os.BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Quá áp (Over Voltage)"
                    android.os.BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Lỗi không xác định"
                    else -> "Hoạt động hoàn hảo"
                }
                _batteryHealth.value = healthStr
            } else {
                _batteryLevel.value = 95
                _batteryTemp.value = "35.2°C"
                _batteryHealth.value = "Tốt (Good)"
            }
        } catch (e: Exception) {
            _batteryLevel.value = 95
            _batteryTemp.value = "35.2°C"
            _batteryHealth.value = "Tốt (Good)"
        }
    }

    fun applyGameBatteryOptimization(enabled: Boolean) {
        _batteryOptimizationApplied.value = enabled
        if (enabled) {
            showToast("Đã bật tối ưu Pin chơi game! Đóng băng ngầm cực hạn, giới hạn FPS, hạ sáng 15%.")
        } else {
            showToast("Đã tắt chế độ tối ưu Pin chơi game.")
        }
    }

    // Role approval system properties moved to top of class to avoid class initialization order NullPointerException

    fun loadPendingRoleRequests() {
        viewModelScope.launch(Dispatchers.IO) {
            val accounts = repository.allUserAccounts.first()
            _pendingRoleRequests.value = accounts.filter { it.hasPendingRequest }
        }
    }

    fun submitRoleRequest(requestedTier: String, requestedRoleName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val username = currentUsername.value ?: return@launch
            val account = repository.getUserAccountDirect(username) ?: return@launch
            
            val updated = account.copy(
                requestedTier = requestedTier,
                requestedRoleName = requestedRoleName,
                hasPendingRequest = true
            )
            repository.saveUserAccount(updated)
            showToast("Đã gửi yêu cầu phê duyệt thành $requestedRoleName!")
            loadPendingRoleRequests()
        }
    }

    fun approveRoleRequest(targetUsername: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val account = repository.getUserAccountDirect(targetUsername) ?: return@launch
            if (!account.hasPendingRequest) return@launch
            
            val updated = account.copy(
                tier = account.requestedTier,
                customRole = account.requestedRoleName,
                requestedTier = "",
                requestedRoleName = "",
                hasPendingRequest = false
            )
            repository.saveUserAccount(updated)
            showToast("Đã phê duyệt tài khoản [$targetUsername] lên ${account.requestedRoleName}!")
            
            repository.addMessage(
                ChatMessage(
                    sender = "AI",
                    message = "🤖 [HỆ THỐNG DUYỆT] Tài khoản người dùng [$targetUsername] đã chính thức được phê duyệt vai trò mới: **${account.requestedRoleName}**!"
                )
            )
            loadPendingRoleRequests()
        }
    }

    fun rejectRoleRequest(targetUsername: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val account = repository.getUserAccountDirect(targetUsername) ?: return@launch
            if (!account.hasPendingRequest) return@launch
            
            val updated = account.copy(
                requestedTier = "",
                requestedRoleName = "",
                hasPendingRequest = false
            )
            repository.saveUserAccount(updated)
            showToast("Đã từ chối yêu cầu của [$targetUsername].")
            loadPendingRoleRequests()
        }
    }

    // --- DATA SECURITY & USER ACCESS CONTROL & PASSWORD AI BOT ---
    fun setUserUserDataAuthorized(targetUsername: String, authorized: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val account = repository.getUserAccountDirect(targetUsername) ?: return@launch
            val updated = account.copy(isUserDataAuthorized = authorized)
            repository.saveUserAccount(updated)
            showToast("Đã ${if (authorized) "CẤP" else "TƯỚC"} quyền truy cập dữ liệu người dùng cho [$targetUsername]!")
        }
    }

    fun modifyUserPasswordDirect(targetUsername: String, newPass: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val account = repository.getUserAccountDirect(targetUsername)
            if (account == null) {
                showToast("Không tìm thấy tài khoản [$targetUsername]!")
                onResult(false)
                return@launch
            }
            val updated = account.copy(passwordHash = newPass.trim())
            repository.saveUserAccount(updated)
            showToast("Đã thay đổi mật khẩu tài khoản [$targetUsername] thành: $newPass")
            onResult(true)
        }
    }

    fun askAiToManagePasswords(prompt: String, onComplete: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _isAiLoading.value = true
            val accounts = repository.allUserAccounts.first()
            val accountsFormatted = accounts.joinToString("\n") { 
                "- Tài khoản: ${it.username}, Email: ${it.email.ifBlank { "Chưa liên kết" }}, Quyền: ${it.tier}, Mật khẩu hiện tại: ${it.passwordHash}"
            }

            val systemInstruction = """
                Bạn là Bot AI Trợ Giúp Mật Khẩu (AI Password Assistant) của hệ thống quản trị tối cao Tool Vip.
                Dưới đây là danh sách toàn bộ tài khoản người dùng hiện tại trong cơ sở dữ liệu:
                $accountsFormatted

                Nhiệm vụ của bạn:
                1. Hỗ trợ người dùng/quản lý tra cứu thông tin tài khoản, lấy lại mật khẩu hoặc cấp mật khẩu tạm thời.
                2. Nếu yêu cầu là muốn đặt lại mật khẩu, đổi mật khẩu hoặc cấp mật khẩu tạm thời cho ai đó, bạn hãy phân tích xem là tài khoản nào, tạo ra một mật khẩu mới phù hợp hoặc mật khẩu tạm thời, sau đó phản hồi mô tả thân thiện và bắt buộc đính kèm cú pháp lệnh ở cuối câu phản hồi của bạn để hệ thống tự động thực thi thay đổi mật khẩu trực tiếp:
                   Cú pháp lệnh: [RESET_PASSWORD: <username>, <new_password>]
                   Ví dụ: "Tôi đã cấp lại mật khẩu tạm thời là '998241' cho tài khoản 'quanghuy'. [RESET_PASSWORD: quanghuy, 998241]"
                   Hãy chắc chắn tên tài khoản phải chính xác 100% so với danh sách trên.
                3. Hãy trả lời ngắn gọn, tập trung và sử dụng tiếng Việt chuyên nghiệp, lịch sự.
            """.trimIndent()

            val aiResponse = GeminiClient.generateResponse(prompt, systemInstruction)
            
            // Look for [RESET_PASSWORD: username, new_password]
            val resetPattern = "\\[RESET_PASSWORD:\\s*([^\\s,]+)\\s*,\\s*([^\\s\\]]+)\\s*\\]".toRegex()
            val match = resetPattern.find(aiResponse)
            if (match != null) {
                val targetUser = match.groupValues[1].trim()
                val newPassword = match.groupValues[2].trim()
                
                val account = repository.getUserAccountDirect(targetUser)
                if (account != null) {
                    val updated = account.copy(passwordHash = newPassword)
                    repository.saveUserAccount(updated)
                    // Post a small delay toast
                    viewModelScope.launch(Dispatchers.Main) {
                        showToast("🤖 AI đã tự động cập nhật mật khẩu cho [$targetUser]!")
                    }
                }
            }

            _isAiLoading.value = false
            onComplete(aiResponse)
        }
    }

    // --- PASSWORD RECOVERY VIA EMAIL ---
    private val activeResetCodes = mutableMapOf<String, String>()

    private val _bgTaskMessage = MutableStateFlow<String?>(null)
    val bgTaskMessage: StateFlow<String?> = _bgTaskMessage.asStateFlow()

    private val _isBgTaskRunning = MutableStateFlow(false)
    val isBgTaskRunning: StateFlow<Boolean> = _isBgTaskRunning.asStateFlow()

    fun requestRecoveryCode(usernameInput: String, emailInput: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val username = usernameInput.trim()
            val email = emailInput.trim()
            if (username.isBlank() || email.isBlank()) {
                onResult(false, "Vui lòng nhập đầy đủ tên tài khoản và email!")
                return@launch
            }
            val account = repository.getUserAccountDirect(username)
            if (account == null) {
                onResult(false, "Tên tài khoản [$username] không tồn tại trên hệ thống!")
                return@launch
            }
            if (account.email.lowercase() != email.lowercase()) {
                onResult(false, "Email liên kết của tài khoản [$username] không chính xác!")
                return@launch
            }
            
            // Generate a random 6-digit code
            val code = (100000..999999).random().toString()
            activeResetCodes[username] = code
            onResult(true, "Mã đặt lại mật khẩu tạm thời đã được gửi mô phỏng tới email $email.\n\n👉 MÃ XÁC MINH CỦA BẠN LÀ: **$code**\n\n(Hãy điền mã này và mật khẩu mới ở bước tiếp theo để tiến hành đặt lại)")
        }
    }

    fun verifyAndResetPassword(usernameInput: String, code: String, newPasswordInput: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val username = usernameInput.trim()
            val inputCode = code.trim()
            val newPassword = newPasswordInput.trim()
            
            if (username.isBlank() || inputCode.isBlank() || newPassword.isBlank()) {
                onResult(false, "Vui lòng nhập đầy đủ Mã xác minh và Mật khẩu mới!")
                return@launch
            }
            
            val savedCode = activeResetCodes[username]
            if (savedCode == null || savedCode != inputCode) {
                onResult(false, "Mã xác minh không chính xác hoặc đã hết hạn!")
                return@launch
            }
            
            val account = repository.getUserAccountDirect(username)
            if (account == null) {
                onResult(false, "Tài khoản không còn tồn tại!")
                return@launch
            }
            
            val updated = account.copy(passwordHash = newPassword)
            repository.saveUserAccount(updated)
            activeResetCodes.remove(username)
            onResult(true, "Đổi mật khẩu thành công! Bạn có thể sử dụng mật khẩu mới [$newPassword] để đăng nhập ngay bây giờ.")
        }
    }

    fun runBackgroundVoiceOrTextCommand(command: String) {
        if (command.isBlank()) return
        
        viewModelScope.launch(Dispatchers.Default) {
            _isBgTaskRunning.value = true
            _bgTaskMessage.value = "🎯 [AI chạy ngầm] Đang tiếp nhận lệnh: \"$command\""
            delay(1500)
            
            val cmdLower = command.lowercase()
            
            when {
                cmdLower.contains("mượt") || cmdLower.contains("ram") || cmdLower.contains("tối ưu") -> {
                    _bgTaskMessage.value = "⚙️ [Background AI] Đang tối ưu RAM phân luồng thấp (giảm 85% tải CPU)..."
                    delay(2000)
                    
                    val apps = allApps.value
                    val trashApps = apps.filter { it.isTrash && !it.isFrozen }
                    if (trashApps.isNotEmpty()) {
                        val updated = trashApps.map { it.copy(isFrozen = true) }
                        repository.updateApps(updated)
                        _bgTaskMessage.value = "🚀 Đã tối ưu xong! Tự động đóng băng ${trashApps.size} app chạy ngầm không giật lag game."
                    } else {
                        _bgTaskMessage.value = "🚀 RAM đã được làm sạch tối đa! Luồng game được ưu tiên hàng đầu."
                    }
                    delay(2500)
                }
                
                cmdLower.contains("pin") || cmdLower.contains("battery") || cmdLower.contains("tiết kiệm") -> {
                    _bgTaskMessage.value = "🔋 [Background AI] Đang kích hoạt chế độ siêu tiết kiệm pin chơi game..."
                    delay(2000)
                    _batteryOptimizationApplied.value = true
                    _bgTaskMessage.value = "✅ Đã đóng băng ngầm cực hạn, giới hạn FPS ở mức 60, hạ sáng 15%."
                    delay(2500)
                }
                
                cmdLower.contains("rác") || cmdLower.contains("dọn dẹp") || cmdLower.contains("quét") -> {
                    _bgTaskMessage.value = "🧹 [Background AI] Quét rác ngầm không ảnh hưởng tiến trình game..."
                    delay(2000)
                    _bgTaskMessage.value = "✨ Hệ thống đã giải phóng thành công 2.45GB tập tin tạm ngầm!"
                    delay(2500)
                }
                
                cmdLower.contains("lag") || cmdLower.contains("game") || cmdLower.contains("booster") || cmdLower.contains("tăng tốc") -> {
                    _bgTaskMessage.value = "⚡ [Background AI] Thiết lập chế độ Game Booster vĩnh viễn..."
                    delay(2000)
                    _bgTaskMessage.value = "🎮 Đã giải phóng tài nguyên GPU/CPU, ưu tiên tối đa băng thông mạng cho trò chơi!"
                    delay(2500)
                }
                
                else -> {
                    _bgTaskMessage.value = "🤖 [Background AI] AI đang phân tích và tối ưu hóa hệ thống ngầm..."
                    delay(2500)
                    _bgTaskMessage.value = "✨ Đã dọn dẹp bộ nhớ đệm và tối ưu luồng xử lý cho yêu cầu: \"$command\"."
                    delay(2000)
                }
            }
            
            _isBgTaskRunning.value = false
            _bgTaskMessage.value = null
        }
    }

    // --- ADMIN CREDENTIALS MODIFICATION ---
    fun updateAdminCredentials(newUsernameInput: String, newPasswordInput: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val newUsername = newUsernameInput.trim()
            val newPassword = newPasswordInput.trim()
            if (newUsername.isBlank() || newPassword.isBlank()) {
                onResult(false, "Tên tài khoản và mật khẩu Admin không được để trống!")
                return@launch
            }
            val currentAdminUser = currentUsername.value ?: "admin"
            val account = repository.getUserAccountDirect(currentAdminUser)
            if (account == null) {
                onResult(false, "Không tìm thấy phiên làm việc của Admin hiện tại!")
                return@launch
            }
            if (account.tier != "ADMIN") {
                onResult(false, "Chỉ tài khoản Admin tối cao mới được quyền đổi thông tin này!")
                return@launch
            }
            
            if (newUsername != currentAdminUser) {
                val existing = repository.getUserAccountDirect(newUsername)
                if (existing != null) {
                    onResult(false, "Tên tài khoản mới [$newUsername] đã được sử dụng bởi người khác!")
                    return@launch
                }
                
                // Primary Key is username, so delete old and save new
                val updated = account.copy(username = newUsername, passwordHash = newPassword)
                repository.saveUserAccount(updated)
                repository.deleteUserAccount(currentAdminUser)
            } else {
                val updated = account.copy(passwordHash = newPassword)
                repository.saveUserAccount(updated)
            }
            
            currentUsername.value = newUsername
            sharedPrefs.edit().putString("logged_in_user", newUsername).apply()
            onResult(true, "Đã cập nhật thông tin đăng nhập Admin tối cao thành công!")
        }
    }

    fun updateUserCredentials(newUsernameInput: String, newPasswordInput: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val username = currentUsername.value ?: return@launch
            val newUsername = newUsernameInput.trim()
            val newPassword = newPasswordInput.trim()
            if (newUsername.isBlank() || newPassword.isBlank()) {
                onResult(false, "Tên tài khoản và mật khẩu không được trống!")
                return@launch
            }
            if (newUsername.length < 4 || newPassword.length < 4) {
                onResult(false, "Tên tài khoản và mật khẩu phải từ 4 ký tự trở lên!")
                return@launch
            }
            val account = repository.getUserAccountDirect(username)
            if (account == null) {
                onResult(false, "Không tìm thấy tài khoản hiện tại!")
                return@launch
            }
            if (newUsername != username) {
                val existing = repository.getUserAccountDirect(newUsername)
                if (existing != null) {
                    onResult(false, "Tên tài khoản mới [$newUsername] đã được sử dụng bởi người khác!")
                    return@launch
                }
                // Re-create account with new primary key username
                val updated = account.copy(username = newUsername, passwordHash = newPassword)
                repository.saveUserAccount(updated)
                repository.deleteUserAccount(username)
            } else {
                val updated = account.copy(passwordHash = newPassword)
                repository.saveUserAccount(updated)
            }
            
            currentUsername.value = newUsername
            sharedPrefs.edit().putString("logged_in_user", newUsername).apply()
            onResult(true, "Cập nhật thông tin tài khoản & mật khẩu mới thành công!")
        }
    }

    fun verifyAndEnableAdminMode(usernameInput: String, passwordInput: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val username = usernameInput.trim()
            val password = passwordInput.trim()
            val account = repository.getUserAccountDirect(username)
            if (account == null) {
                onResult(false, "Tài khoản Admin không tồn tại!")
                return@launch
            }
            if (account.passwordHash != password) {
                onResult(false, "Mật khẩu Admin không chính xác!")
                return@launch
            }
            if (account.tier != "ADMIN") {
                onResult(false, "Tài khoản [$username] không phải là Admin hệ thống!")
                return@launch
            }
            currentUsername.value = username
            sharedPrefs.edit().putString("logged_in_user", username).apply()
            onResult(true, "Đăng nhập Admin tối cao [$username] thành công!")
            loadPendingRoleRequests()
        }
    }

    fun updateUserEmail(newEmailInput: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val username = currentUsername.value ?: return@launch
            val account = repository.getUserAccountDirect(username) ?: return@launch
            val updated = account.copy(email = newEmailInput.trim())
            repository.saveUserAccount(updated)
            showToast("Đã liên kết thành công email: ${newEmailInput.trim()}!")
        }
    }

    // --- ADMIN AI BOT STATE & LOGIC ---
    private val _aiAdminLogs = MutableStateFlow<List<String>>(
        listOf(
            "🤖 Admin AI Bot: Đang khởi động hệ thống phân tích...",
            "🤖 Admin AI Bot: Đã kết nối cổng thanh toán VietQR...",
            "🤖 Admin AI Bot: Đang quét sao kê ngân hàng Pham Quang Huy..."
        )
    )
    val aiAdminLogs: StateFlow<List<String>> = _aiAdminLogs.asStateFlow()

    fun addAiAdminLog(log: String) {
        val current = _aiAdminLogs.value.toMutableList()
        current.add(log)
        if (current.size > 80) current.removeAt(0)
        _aiAdminLogs.value = current
    }

    fun clearAiAdminLogs() {
        _aiAdminLogs.value = listOf("🤖 Admin AI Bot: Nhật ký được dọn sạch. Sẵn sàng quét mới...")
    }

    // Process a simulated deposit transaction that is evaluated by the Admin AI Bot
    fun simulateBankDeposit(amount: Double, transferContent: String, depositorName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val timestamp = System.currentTimeMillis()
            addAiAdminLog("💰 [BIẾN ĐỘNG SỐ DƯ] +${java.text.DecimalFormat("#,###").format(amount)}đ từ $depositorName. ND: \"$transferContent\"")
            
            delay(1200)
            addAiAdminLog("🔍 [PHÂN TÍCH AI] Đang đối chiếu thông tin giao dịch...")
            delay(1000)
            
            val cleanedContent = transferContent.trim().lowercase()
            var detectedUsername: String? = null
            
            // Analyze the transfer content meticulously. Many people send money but don't use the app.
            // Expected formats: "tv <username>" or "<username>"
            val parts = cleanedContent.split("\\s+".toRegex())
            if (parts.isNotEmpty()) {
                if (parts[0] == "tv" && parts.size > 1) {
                    detectedUsername = parts[1]
                } else {
                    detectedUsername = parts[0]
                }
            }

            if (detectedUsername == null || detectedUsername.isBlank()) {
                addAiAdminLog("⚠️ [CẢNH BÁO AI] Không tìm thấy tên tài khoản hợp lệ từ nội dung: \"$transferContent\".")
                addAiAdminLog("❌ [TỰ ĐỘNG BỎ QUA] Giao dịch từ $depositorName chuyển sang Chờ Duyệt Thủ Công.")
                repository.addTransaction(
                    TransactionItem(
                        username = "KHÁCH_VÃNG_LAI",
                        amount = amount,
                        type = "DEPOSIT",
                        status = "PENDING_AI",
                        referenceNote = "VietQR từ $depositorName: Nội dung không có tài khoản (\"$transferContent\")",
                        timestamp = timestamp
                    )
                )
                return@launch
            }

            // Check if user exists in our app database
            val targetUser = repository.getUserAccountDirect(detectedUsername)
            if (targetUser == null) {
                addAiAdminLog("❌ [SAI TÊN TÀI KHOẢN] Không tìm thấy tài khoản [$detectedUsername] trong database.")
                addAiAdminLog("⚠️ [BỎ QUA TỰ ĐỘNG] Giao dịch từ $depositorName bị hủy tự động, gắn cờ lỗi.")
                repository.addTransaction(
                    TransactionItem(
                        username = detectedUsername,
                        amount = amount,
                        type = "DEPOSIT",
                        status = "FAILED",
                        referenceNote = "Tên tài khoản không tồn tại: $depositorName (Nội dung: \"$transferContent\")",
                        timestamp = timestamp
                    )
                )
            } else {
                addAiAdminLog("✅ [KHỚP THÀNH CÔNG] Đã định vị tài khoản: [${targetUser.username}].")
                delay(800)
                
                val newBalance = targetUser.balance + amount
                
                // Auto-upgrade VIP levels
                val upgradedTier = if (newBalance >= targetUser.vip2Price && targetUser.tier != "ADMIN") {
                    "VIP2"
                } else if (newBalance >= targetUser.vip1Price && targetUser.tier != "ADMIN" && targetUser.tier != "VIP2") {
                    "VIP1"
                } else {
                    targetUser.tier
                }
                
                val upgradedRole = when (upgradedTier) {
                    "VIP2" -> "Hội viên VIP 2 Pro"
                    "VIP1" -> "Hội viên VIP 1"
                    else -> targetUser.customRole
                }

                repository.saveUserAccount(
                    targetUser.copy(
                        balance = newBalance,
                        tier = upgradedTier,
                        customRole = upgradedRole
                    )
                )

                // Add to transaction history
                repository.addTransaction(
                    TransactionItem(
                        username = targetUser.username,
                        amount = amount,
                        type = "DEPOSIT",
                        status = "SUCCESS",
                        referenceNote = "VietQR tự động duyệt bởi AI (Từ $depositorName)",
                        timestamp = timestamp
                    )
                )

                addAiAdminLog("🚀 [HOÀN THÀNH] Cộng thành công +${java.text.DecimalFormat("#,###").format(amount)}đ cho [${targetUser.username}]. Cấp bậc: $upgradedTier.")
                
                if (targetUser.username == currentUsername.value) {
                    showToast("🤖 AI Admin: Đã tự động phê duyệt chuyển khoản +${java.text.DecimalFormat("#,###").format(amount)}đ thành công!")
                }
            }
        }
    }

    fun clearAllTransactions() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllTransactions()
            showToast("Đã dọn sạch toàn bộ lịch sử giao dịch!")
        }
    }

    fun verifyCurrentUserPassword(passwordInput: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val username = currentUsername.value
            if (username == null) {
                onResult(false)
                return@launch
            }
            val account = repository.getUserAccountDirect(username)
            if (account != null && account.passwordHash == passwordInput.trim()) {
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    val tempRecoveryCode = MutableStateFlow<String?>(null)

    fun updateUserAuthPin(newPinInput: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val username = currentUsername.value ?: return@launch
            val newPin = newPinInput.trim()
            if (newPin.isBlank()) {
                onResult(false, "Mã xác thực không được trống!")
                return@launch
            }
            if (newPin.length < 4) {
                onResult(false, "Mã xác thực phải từ 4 ký tự trở lên!")
                return@launch
            }
            val account = repository.getUserAccountDirect(username)
            if (account == null) {
                onResult(false, "Không tìm thấy tài khoản!")
                return@launch
            }
            if (newPin == account.passwordHash) {
                onResult(false, "Mã xác thực không trùng với mật khẩu!")
                return@launch
            }
            
            repository.saveUserAccount(account.copy(authPin = newPin))
            onResult(true, "Đã cập nhật mã xác thực 2 lớp mới thành công!")
        }
    }

    fun requestForgotPinRecovery(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val username = currentUsername.value ?: return@launch
            val account = repository.getUserAccountDirect(username)
            if (account == null) {
                onResult(false, "Không tìm thấy tài khoản!")
                return@launch
            }
            val email = if (account.email.isNotBlank()) account.email else "quanghuypham1789@gmail.com"
            
            // Generate a random 6-digit OTP
            val otp = (100000..999999).random().toString()
            tempRecoveryCode.value = otp
            
            // Simulate sending automated email
            delay(800)
            showToast("Đã gửi mã OTP tạm thời tới email $email!")
            
            // Push message to log so the owner can easily see and test it
            addAiAdminLog("📩 [KHÔI PHỤC] Đã gửi mã xác thực tạm thời [$otp] tới email $email.")
            
            onResult(true, "Mã khôi phục tạm thời của bạn là [$otp] đã gửi về email [$email]. Hãy nhập mã tạm thời này để thiết lập lại mã xác thực.")
        }
    }

    fun verifyRecoveryCodeAndResetPin(otpInput: String, newPinInput: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val username = currentUsername.value ?: return@launch
            val expectedOtp = tempRecoveryCode.value
            if (expectedOtp == null || otpInput.trim() != expectedOtp) {
                onResult(false, "Mã OTP khôi phục không chính xác!")
                return@launch
            }
            val newPin = newPinInput.trim()
            if (newPin.isBlank()) {
                onResult(false, "Mã xác thực mới không được trống!")
                return@launch
            }
            if (newPin.length < 4) {
                onResult(false, "Mã xác thực mới phải từ 4 ký tự trở lên!")
                return@launch
            }
            val account = repository.getUserAccountDirect(username)
            if (account == null) {
                onResult(false, "Không tìm thấy tài khoản!")
                return@launch
            }
            if (newPin == account.passwordHash) {
                onResult(false, "Mã xác thực mới không được trùng với mật khẩu!")
                return@launch
            }
            
            repository.saveUserAccount(account.copy(authPin = newPin))
            tempRecoveryCode.value = null // clear OTP
            onResult(true, "Xác thực OTP thành công! Đã đổi mã xác thực 2 lớp mới!")
        }
    }

    fun verifyCurrentUserAuthPin(pinInput: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val username = currentUsername.value
            if (username == null) {
                onResult(false)
                return@launch
            }
            val account = repository.getUserAccountDirect(username)
            if (account != null) {
                // If the account has no PIN set yet, they can login directly or set a default
                val expectedPin = if (account.authPin.isBlank()) "10293847" else account.authPin
                if (expectedPin == pinInput.trim()) {
                    onResult(true)
                } else {
                    onResult(false)
                }
            } else {
                onResult(false)
            }
        }
    }

    fun approveTransactionManual(transactionId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val transaction = allTransactions.value.find { it.id == transactionId }
            if (transaction != null && (transaction.status == "PENDING_AI" || transaction.status == "FAILED")) {
                val targetUsername = transaction.username
                val targetUser = repository.getUserAccountDirect(targetUsername)
                if (targetUser != null) {
                    val newBalance = targetUser.balance + transaction.amount
                    val upgradedTier = if (newBalance >= targetUser.vip2Price && targetUser.tier != "ADMIN") {
                        "VIP2"
                    } else if (newBalance >= targetUser.vip1Price && targetUser.tier != "ADMIN" && targetUser.tier != "VIP2") {
                        "VIP1"
                    } else {
                        targetUser.tier
                    }
                    val upgradedRole = when (upgradedTier) {
                        "VIP2" -> "Hội viên VIP 2 Pro"
                        "VIP1" -> "Hội viên VIP 1"
                        else -> targetUser.customRole
                    }
                    repository.saveUserAccount(
                        targetUser.copy(
                            balance = newBalance,
                            tier = upgradedTier,
                            customRole = upgradedRole
                        )
                    )
                }
                
                // Update transaction status
                repository.addTransaction(
                    transaction.copy(
                        status = "SUCCESS",
                        referenceNote = transaction.referenceNote + " (Duyệt thủ công bởi Admin)"
                    )
                )
                addAiAdminLog("✅ [ADMIN DUYỆT] Đã duyệt thủ công giao dịch +${java.text.DecimalFormat("#,###").format(transaction.amount)}đ cho [$targetUsername].")
                showToast("Đã duyệt thành công giao dịch số #$transactionId!")
            }
        }
    }

    fun rejectTransactionManual(transactionId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val transaction = allTransactions.value.find { it.id == transactionId }
            if (transaction != null) {
                repository.addTransaction(
                    transaction.copy(
                        status = "FAILED",
                        referenceNote = "Từ chối bởi Admin"
                    )
                )
                addAiAdminLog("❌ [ADMIN TỪ CHỐI] Đã từ chối giao dịch số #$transactionId của [${transaction.username}].")
                showToast("Đã từ chối giao dịch số #$transactionId!")
            }
        }
    }

    fun adjustUserBalanceDirect(targetUsername: String, adjustmentAmount: Double, reasonNote: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val targetUser = repository.getUserAccountDirect(targetUsername)
            if (targetUser == null) {
                showToast("Không tìm thấy người dùng [$targetUsername]!")
                return@launch
            }
            val newBalance = targetUser.balance + adjustmentAmount
            if (newBalance < 0) {
                showToast("Số dư mới không thể âm!")
                return@launch
            }

            val upgradedTier = if (newBalance >= targetUser.vip2Price && targetUser.tier != "ADMIN") {
                "VIP2"
            } else if (newBalance >= targetUser.vip1Price && targetUser.tier != "ADMIN" && targetUser.tier != "VIP2") {
                "VIP1"
            } else {
                targetUser.tier
            }
            val upgradedRole = when (upgradedTier) {
                "VIP2" -> "Hội viên VIP 2 Pro"
                "VIP1" -> "Hội viên VIP 1"
                else -> targetUser.customRole
            }

            repository.saveUserAccount(
                targetUser.copy(
                    balance = newBalance,
                    tier = upgradedTier,
                    customRole = upgradedRole
                )
            )

            // Log in transaction history
            repository.addTransaction(
                TransactionItem(
                    username = targetUsername,
                    amount = adjustmentAmount,
                    type = "MANUAL_ADJUST",
                    status = "SUCCESS",
                    referenceNote = "Điều chỉnh số dư bởi Admin: $reasonNote",
                    timestamp = System.currentTimeMillis()
                )
            )

            addAiAdminLog("⚙️ [ĐIỀU CHỈNH ADMIN] ${if (adjustmentAmount >= 0) "+" else ""}${java.text.DecimalFormat("#,###").format(adjustmentAmount)}đ cho [$targetUsername]. Lý do: $reasonNote")
            showToast("Đã điều chỉnh số dư thành công!")
        }
    }

    fun setUserBalanceDirect(targetUsername: String, absoluteAmount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val targetUser = repository.getUserAccountDirect(targetUsername)
            if (targetUser == null) {
                showToast("Không tìm thấy người dùng [$targetUsername]!")
                return@launch
            }
            if (absoluteAmount < 0) {
                showToast("Số dư mới không thể âm!")
                return@launch
            }
            
            val upgradedTier = if (absoluteAmount >= targetUser.vip2Price && targetUser.tier != "ADMIN" && targetUser.tier != "STAFF") {
                "VIP2"
            } else if (absoluteAmount >= targetUser.vip1Price && targetUser.tier != "ADMIN" && targetUser.tier != "STAFF" && targetUser.tier != "VIP2") {
                "VIP1"
            } else {
                targetUser.tier
            }
            val upgradedRole = when (upgradedTier) {
                "VIP2" -> "Hội viên VIP 2 Pro"
                "VIP1" -> "Hội viên VIP 1"
                else -> targetUser.customRole
            }

            repository.saveUserAccount(
                targetUser.copy(
                    balance = absoluteAmount,
                    tier = upgradedTier,
                    customRole = upgradedRole
                )
            )

            // Log in transaction history
            repository.addTransaction(
                TransactionItem(
                    username = targetUsername,
                    amount = absoluteAmount - targetUser.balance,
                    type = "MANUAL_SET",
                    status = "SUCCESS",
                    referenceNote = "Thiết lập số dư tuyệt đối bởi Admin",
                    timestamp = System.currentTimeMillis()
                )
            )

            addAiAdminLog("⚙️ [THIẾT LẬP ADMIN] Số dư mới: ${java.text.DecimalFormat("#,###").format(absoluteAmount)}đ cho [$targetUsername]")
            showToast("Đã thiết lập số dư cho [$targetUsername] thành ${java.text.DecimalFormat("#,###").format(absoluteAmount)} VNĐ!")
        }
    }

    fun adjustUserTierDirect(targetUsername: String, newTier: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val targetUser = repository.getUserAccountDirect(targetUsername)
            if (targetUser == null) {
                showToast("Không tìm thấy người dùng [$targetUsername]!")
                return@launch
            }
            val customRole = when (newTier) {
                "ADMIN" -> "QUẢN TRỊ VIÊN HỆ THỐNG"
                "MANAGER" -> "ĐIỀU PHỐI VIÊN CHÍNH"
                "STAFF" -> "NHÂN VIÊN HỖ TRỢ"
                "VIP2" -> "Hội viên VIP 2 Pro"
                "VIP1" -> "Hội viên VIP 1"
                else -> "Thành viên thường"
            }
            repository.saveUserAccount(
                targetUser.copy(
                    tier = newTier,
                    customRole = customRole
                )
            )
            showToast("Đã đổi cấp bậc [$targetUsername] thành $newTier!")
        }
    }

    // --- SPEED TEST & INTERNET BANDWIDTH STATES ---
    private val _speedTestProgress = MutableStateFlow(0f)
    val speedTestProgress: StateFlow<Float> = _speedTestProgress.asStateFlow()

    private val _speedTestPhase = MutableStateFlow("Idle") // Idle, Ping, Download, Upload, Done
    val speedTestPhase: StateFlow<String> = _speedTestPhase.asStateFlow()

    private val _speedTestDownload = MutableStateFlow(0f)
    val speedTestDownload: StateFlow<Float> = _speedTestDownload.asStateFlow()

    private val _speedTestUpload = MutableStateFlow(0f)
    val speedTestUpload: StateFlow<Float> = _speedTestUpload.asStateFlow()

    private val _speedTestPing = MutableStateFlow(0)
    val speedTestPing: StateFlow<Int> = _speedTestPing.asStateFlow()

    private val _speedTestJitter = MutableStateFlow(0)
    val speedTestJitter: StateFlow<Int> = _speedTestJitter.asStateFlow()

    private val _speedTestServer = MutableStateFlow("Hà Nội - FPT Telecom")
    val speedTestServer: StateFlow<String> = _speedTestServer.asStateFlow()

    private val _themeMode = MutableStateFlow(sharedPrefs.getString("theme_mode", "DARK") ?: "DARK")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _isDarkMode = MutableStateFlow(sharedPrefs.getBoolean("ui_is_dark_mode", true))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        val nextMode = when (_themeMode.value) {
            "DARK" -> "LIGHT"
            "LIGHT" -> "SYSTEM"
            else -> "DARK"
        }
        _themeMode.value = nextMode
        sharedPrefs.edit().putString("theme_mode", nextMode).apply()
        val viMode = when (nextMode) {
            "DARK" -> "Giao diện: Tối"
            "LIGHT" -> "Giao diện: Sáng"
            else -> "Giao diện: Tự động (Hệ thống)"
        }
        showToast(viMode)
    }

    fun setResolvedDarkMode(isDark: Boolean) {
        _isDarkMode.value = isDark
    }

    // --- BACKGROUND OPTIMIZATION CYCLES REPORT ---
    private val _optimizationCycles = MutableStateFlow<List<OptimizationCycle>>(listOf(
        OptimizationCycle(1, "04:12", 8, 412.5, "Hệ thống tự động"),
        OptimizationCycle(2, "08:45", 14, 824.0, "Đóng băng ngầm"),
        OptimizationCycle(3, "12:15", 6, 218.2, "Làm mát CPU"),
        OptimizationCycle(4, "16:30", 11, 580.0, "Giải phóng bộ nhớ"),
        OptimizationCycle(5, "20:55", 15, 915.8, "Tối ưu RAM định kỳ")
    ))
    val optimizationCycles: StateFlow<List<OptimizationCycle>> = _optimizationCycles.asStateFlow()

    fun addOptimizationCycle(processesCleaned: Int, ramReclaimedMb: Double, type: String) {
        val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        val timeStr = formatter.format(java.util.Date())
        val newId = (_optimizationCycles.value.maxOfOrNull { it.id } ?: 0) + 1
        val newCycle = OptimizationCycle(newId, timeStr, processesCleaned, ramReclaimedMb, type)
        _optimizationCycles.value = listOf(newCycle) + _optimizationCycles.value
    }

    fun setSpeedTestServer(server: String) {
        _speedTestServer.value = server
    }

    fun runSpeedTest() {
        if (_speedTestPhase.value != "Idle" && _speedTestPhase.value != "Done") return
        
        viewModelScope.launch {
            _speedTestPhase.value = "Ping"
            _speedTestProgress.value = 0f
            _speedTestPing.value = 0
            _speedTestJitter.value = 0
            _speedTestDownload.value = 0f
            _speedTestUpload.value = 0f
            
            // Phase 1: Ping / Jitter test (approx 2s)
            for (i in 1..20) {
                delay(100)
                _speedTestProgress.value = i * 0.05f
                _speedTestPing.value = (10..35).random()
                _speedTestJitter.value = (1..4).random()
            }
            
            // Phase 2: Download test (approx 4s)
            _speedTestPhase.value = "Download"
            val targetDownload = (150..380).random().toFloat()
            for (i in 1..40) {
                delay(100)
                _speedTestProgress.value = 0.2f + (i * 0.01f * 0.4f)
                val currentTarget = if (i < 15) {
                    (targetDownload * (i / 15f))
                } else {
                    targetDownload + (-12..12).random()
                }
                _speedTestDownload.value = currentTarget.coerceAtLeast(10f)
            }
            
            // Phase 3: Upload test (approx 4s)
            _speedTestPhase.value = "Upload"
            val targetUpload = (60..190).random().toFloat()
            for (i in 1..40) {
                delay(100)
                _speedTestProgress.value = 0.6f + (i * 0.01f * 0.4f)
                val currentTarget = if (i < 15) {
                    (targetUpload * (i / 15f))
                } else {
                    targetUpload + (-6..6).random()
                }
                _speedTestUpload.value = currentTarget.coerceAtLeast(5f)
            }
            
            _speedTestPhase.value = "Done"
            _speedTestProgress.value = 1f
            showToast("Đo tốc độ mạng hoàn tất!")
        }
    }
}

data class OptimizationCycle(
    val id: Int,
    val timeString: String,
    val processesCleaned: Int,
    val ramReclaimedMb: Double,
    val type: String
)

data class CustomGiftCode(
    val code: String,
    val tier: String, // VIP1, VIP2
    val durationValue: Int,
    val durationUnit: String, // "giờ", "ngày", "tuần", "tháng", "vĩnh viễn"
    val isUsed: Boolean = false,
    val expiryTimestamp: Long = 0L
)


