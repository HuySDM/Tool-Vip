package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: AppViewModel,
    onNavigateToAdmin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val userAccount by viewModel.userAccount.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Administration block for Admins and Managers
        val role = userAccount?.tier ?: ""
        if (role == "ADMIN" || role == "MANAGER" || role == "STAFF" || role == "CTV") {
            item {
                Text(
                    text = "QUẢN TRỊ HỆ THỐNG",
                    color = BrightTurquoise,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToAdmin() },
                    colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.2.dp, BrightTurquoise)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin Panel",
                                tint = BrightTurquoise,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "Bảng Điều Khiển Admin & Quản Lý",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Duyệt thành viên, cấu hình hệ thống & kiểm tra dữ liệu",
                                    color = TextGray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Open",
                            tint = TextGray
                        )
                    }
                }
            }
        }

        // App Version card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderGreen)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Verified, "Verified Icon", tint = BrightTurquoise, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tool Vip v9.0 Premium",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Tối Ưu Hoàn Hảo - Sức Mạnh Tối Đa",
                        color = TextGray,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Divider(color = BorderGreen)

                    Spacer(modifier = Modifier.height(10.dp))

                    // Device support details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Kiến trúc tương thích:", color = TextGray, fontSize = 12.sp)
                        Text(text = "32-bit & 64-bit (Tối ưu)", color = GlowGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Công nghệ biên dịch:", color = TextGray, fontSize = 12.sp)
                        Text(text = "AOT / JIT Code Scrambled", color = GlowGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Subscriptions auto-renew settings
        item {
            Text(
                text = "CẤU HÌNH GIA HẠN",
                color = BrightTurquoise,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

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
                        Text(
                            text = "Tự động gia hạn khi hết tháng",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Tự động trừ số dư để kích hoạt lại VIP sau 30 ngày.",
                            color = TextGray,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = userAccount?.isAutoRenew ?: true,
                        onCheckedChange = { viewModel.setAutoRenew(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DeepObsidian,
                            checkedTrackColor = BrightTurquoise
                        )
                    )
                }
            }
        }

        // Threshold controller slider
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderGreen)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Ngưỡng kích hoạt đóng băng RAM: ${userAccount?.freezeThreshold ?: 80}%",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "AI sẽ bắt đầu đóng băng hàng loạt ứng dụng rác khi lượng RAM sử dụng vượt quá mức này.",
                        color = TextGray,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Slider(
                        value = (userAccount?.freezeThreshold ?: 80).toFloat(),
                        onValueChange = { viewModel.setFreezeThreshold(it.toInt()) },
                        valueRange = 50f..95f,
                        colors = SliderDefaults.colors(
                            thumbColor = BrightTurquoise,
                            activeTrackColor = BrightTurquoise,
                            inactiveTrackColor = BorderGreen
                        )
                    )
                }
            }
        }

        // AI Scheduled Optimization Settings
        item {
            Text(
                text = "LẬP LỊCH TỐI ƯU HOÁ AI",
                color = BrightTurquoise,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Schedule Icon",
                                tint = BrightTurquoise
                            )
                            Column {
                                Text(
                                    text = "Tự động tối ưu định kỳ",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Chạy kiểm tra và đóng băng rác tự động theo chu kỳ (Yêu cầu VIP 1)",
                                    color = TextGray,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Switch(
                            checked = userAccount?.isScheduledOptEnabled ?: false,
                            onCheckedChange = { viewModel.setScheduledOptEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DeepObsidian,
                                checkedTrackColor = BrightTurquoise
                            )
                        )
                    }

                    if (userAccount?.isScheduledOptEnabled == true) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = BorderGreen.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Khoảng thời gian lặp lại:",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val intervals = listOf(1, 5, 15, 30, 60)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            intervals.forEach { mins ->
                                val isSelected = (userAccount?.optIntervalMinutes ?: 15) == mins
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) TransparentGreen else DeepObsidian)
                                        .border(
                                            1.dp,
                                            if (isSelected) BrightTurquoise else BorderGreen.copy(alpha = 0.4f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { viewModel.setOptIntervalMinutes(mins) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$mins Phút",
                                        color = if (isSelected) BrightTurquoise else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Developer Admin console tools (Only shown for real ADMIN accounts)
        if (role == "ADMIN") {
            item {
                Text(
                    text = "HỆ THỐNG QUẢN TRỊ VIÊN (CONSOLE)",
                    color = CoralVibrant,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CoralVibrant, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkTealCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Bạn đang truy cập bảng điều khiển Admin. Có thể hoán đổi các cấp độ tài khoản nhanh phục vụ việc kiểm thử:",
                            color = TextGray,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.enableAdminMode() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = CoralVibrant),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(text = "CẤP ADMIN", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.rechargeAccount(500000.0) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = BrightTurquoise, contentColor = DeepObsidian),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(text = "NẠP 500K VND", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { viewModel.simulateVipExpiryDirectly() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = CoralVibrant, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Timer, "Simulate Expiry")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "GIẢ LẬP HẾT HẠN VIP ĐỂ KIỂM TRA JOB NỀN", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { viewModel.logoutOrReset() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = BorderGreen, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.PowerSettingsNew, "Reset Status")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "MÔ PHỎNG HUỶ KÍCH HOẠT (CHƯA NẠP)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // RESET ACCOUNT & PASSWORD CARD (ĐẶT LẠI TÀI KHOẢN & MẬT KHẨU)
        item {
            var newUsernameState by remember(userAccount?.username) { mutableStateOf(userAccount?.username ?: "") }
            var newPasswordState by remember { mutableStateOf("") }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderGreen)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VpnKey, "Account Security", tint = BrightTurquoise)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Đặt Lại Tài Khoản & Mật Khẩu",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Đặt lại Tên đăng nhập và Mật khẩu bảo mật mới cho tài khoản của bạn. Sau khi đổi, hệ thống sẽ tự động đăng nhập và lưu dữ liệu mới.",
                        color = TextGray,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newUsernameState,
                        onValueChange = { newUsernameState = it },
                        label = { Text("Tên tài khoản mới", color = TextGray, fontSize = 11.sp) },
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
                        value = newPasswordState,
                        onValueChange = { newPasswordState = it },
                        label = { Text("Mật khẩu bảo mật mới", color = TextGray, fontSize = 11.sp) },
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

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.updateUserCredentials(newUsernameState, newPasswordState) { success, msg ->
                                viewModel.showToast(msg)
                                if (success) {
                                    newPasswordState = ""
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrightTurquoise, contentColor = DeepObsidian),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("XÁC NHẬN CẬP NHẬT TÀI KHOẢN", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // EMAIL BINDING CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderGreen)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, "Email Icon", tint = BrightTurquoise)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Liên Kết Email Khôi Phục",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val emailVal = userAccount?.email ?: ""
                    val isPlaceholder = emailVal == "none@example.com" || emailVal.isBlank()

                    if (isPlaceholder) {
                        Text(
                            text = "⚠️ Bạn chưa liên kết Email chính chủ. Vui lòng cập nhật ngay để có thể lấy lại mật khẩu nếu lỡ quên!",
                            color = CoralVibrant,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    } else {
                        Text(
                            text = "✅ Đã liên kết Email thành công:\n$emailVal",
                            color = GlowGreen,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    var emailInputState by remember(emailVal) { mutableStateOf(if (isPlaceholder) "" else emailVal) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = emailInputState,
                            onValueChange = { emailInputState = it },
                            placeholder = { Text("Nhập Email của bạn...", color = TextGray, fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = BrightTurquoise,
                                unfocusedBorderColor = BorderGreen
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            modifier = Modifier.weight(1.8f)
                        )

                        Button(
                            onClick = {
                                if (emailInputState.isNotBlank() && emailInputState.contains("@")) {
                                    viewModel.updateUserEmail(emailInputState)
                                } else {
                                    viewModel.showToast("Vui lòng nhập định dạng email hợp lệ!")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrightTurquoise, contentColor = DeepObsidian),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("LIÊN KẾT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Credits / Support info
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderGreen)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Liên hệ hỗ trợ kỹ thuật Admin:",
                        color = TextGray,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Email: quanghuypham1789@gmail.com\nĐiện thoại: +84 386 288 111",
                        color = BrightTurquoise,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
