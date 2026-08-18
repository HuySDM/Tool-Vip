package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.BrightTurquoise
import com.example.ui.theme.DarkTealCard
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel
import com.example.ui.TelexConverter
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AppViewModel,
    onLoginSuccess: () -> Unit
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var promoCode by remember { mutableStateOf("") }
    
    var showPinInputStep by remember { mutableStateOf(false) }
    var pinValueState by remember { mutableStateOf("") }
    var expectedPinCode by remember { mutableStateOf("") }
    
    var errorMsg by remember { mutableStateOf("") }
    var showRecoveryDialog by remember { mutableStateOf(false) }
    var infoMsg by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A), // Deep Slate
                        Color(0xFF030712)  // Near Black
                    )
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .verticalScroll(rememberScrollState())
                .background(
                    color = DarkTealCard,
                    shape = RoundedCornerShape(24.dp)
                )
                .border(
                    BorderStroke(1.5.dp, BrightTurquoise),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Logo or Icon Indicator
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

            Spacer(modifier = Modifier.height(16.dp))

            // App Heading
            Text(
                text = "SYSTEM TOOL VIP",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 2.sp
            )
            
            Text(
                text = if (isRegisterMode) "Đăng ký thành viên mới" else "Đăng nhập để tiếp tục",
                fontSize = 14.sp,
                color = Color.LightGray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (errorMsg.isNotBlank()) {
                Surface(
                    color = Color(0xFF7F1D1D),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = Color.Red
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMsg,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            if (infoMsg.isNotBlank()) {
                Surface(
                    color = Color(0xFF065F46),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Info",
                            tint = Color.Green
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = infoMsg,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            if (showPinInputStep && !isRegisterMode) {
                // PIN Input field
                OutlinedTextField(
                    value = pinValueState,
                    onValueChange = { pinValueState = it },
                    label = { Text("Nhập mã xác thực 2 lớp (PIN)", color = Color.Gray) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "PIN",
                            tint = Color.Gray
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFEF4444),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.White.copy(alpha = 0.02f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                )
                
                Text(
                    text = "Tài khoản của bạn đã kích hoạt bảo mật 2 lớp. Vui lòng nhập mã xác thực (mặc định: 10293847).",
                    color = Color.Yellow,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 8.dp).align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = {
                        showPinInputStep = false
                        pinValueState = ""
                        errorMsg = ""
                    }
                ) {
                    Text("← QUAY LẠI ĐĂNG NHẬP", color = Color.Gray, fontSize = 13.sp)
                }
            } else {
                // Username input
                OutlinedTextField(
                    value = username,
                    onValueChange = { newValue ->
                        username = TelexConverter.convert(newValue)
                    },
                    label = { Text("Tên tài khoản", color = Color.Gray) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User",
                            tint = Color.Gray
                        )
                    },
                    trailingIcon = {
                        if (username.isNotEmpty()) {
                            IconButton(onClick = { username = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = Color.Gray
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.White.copy(alpha = 0.02f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("username_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password input
                OutlinedTextField(
                    value = password,
                    onValueChange = { newValue ->
                        password = TelexConverter.convert(newValue)
                    },
                    label = { Text("Mật khẩu bảo mật", color = Color.Gray) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password",
                            tint = Color.Gray
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = "Visibility",
                                tint = Color.Gray
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.White.copy(alpha = 0.02f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    )
                )

                if (isRegisterMode) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email liên kết tài khoản", color = Color.Gray) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = Color.Gray
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.White.copy(alpha = 0.02f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = promoCode,
                        onValueChange = { promoCode = it },
                        label = { Text("Mã Giftcode VIP 1 (Không bắt buộc)", color = Color.Gray) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = "Giftcode",
                                tint = Color.Gray
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.White.copy(alpha = 0.02f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("promo_code_input"),
                        singleLine = true
                    )
                    Text(
                        text = "Mẹo: Nhập VIP1_FREE hoặc VIP1_GIFT để nhận ngay VIP 1!",
                        color = Color(0xFF10B981),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp).align(Alignment.Start)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = {
                    keyboardController?.hide()
                    errorMsg = ""
                    infoMsg = ""
                    isLoading = true
                    
                    if (isRegisterMode) {
                        viewModel.register(username, password, email, promoCode) { success, message ->
                            isLoading = false
                            if (success) {
                                infoMsg = message
                                onLoginSuccess()
                            } else {
                                errorMsg = message
                            }
                        }
                    } else {
                        if (showPinInputStep) {
                            if (pinValueState.trim() == expectedPinCode) {
                                viewModel.login(username, password) { success, message ->
                                    isLoading = false
                                    if (success) {
                                        infoMsg = message
                                        onLoginSuccess()
                                    } else {
                                        errorMsg = message
                                    }
                                }
                            } else {
                                isLoading = false
                                errorMsg = "Mã xác thực 2 lớp (PIN) không chính xác! Vui lòng kiểm tra lại."
                            }
                        } else {
                            viewModel.verifyLoginAndGetAuthPin(username, password) { success, message, pin ->
                                if (success) {
                                    if (!pin.isNullOrBlank()) {
                                        expectedPinCode = pin
                                        showPinInputStep = true
                                        isLoading = false
                                    } else {
                                        // Direct login
                                        viewModel.login(username, password) { loginSuccess, loginMsg ->
                                            isLoading = false
                                            if (loginSuccess) {
                                                onLoginSuccess()
                                            } else {
                                                errorMsg = loginMsg
                                            }
                                        }
                                    }
                                } else {
                                    isLoading = false
                                    errorMsg = message
                                }
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRegisterMode) Color(0xFF10B981) else Color(0xFF3B82F6)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("login_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = if (isRegisterMode) "ĐĂNG KÝ NGAY" else if (showPinInputStep) "XÁC NHẬN PIN & ĐĂNG NHẬP" else "ĐĂNG NHẬP HỆ THỐNG",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Toggle mode button
            TextButton(
                onClick = {
                    isRegisterMode = !isRegisterMode
                    errorMsg = ""
                    infoMsg = ""
                    showPinInputStep = false
                    pinValueState = ""
                    expectedPinCode = ""
                }
            ) {
                Text(
                    text = if (isRegisterMode) "Đã có tài khoản? Đăng nhập ngay" else "Chưa có tài khoản? Đăng ký tài khoản mới",
                    color = Color(0xFF3B82F6),
                    fontSize = 14.sp
                )
            }

            if (!isRegisterMode) {
                TextButton(
                    onClick = { showRecoveryDialog = true }
                ) {
                    Text(
                        text = "Quên mật khẩu? Khôi phục qua Email liên kết",
                        color = Color(0xFFEF4444),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (showRecoveryDialog) {
            val coroutineScope = rememberCoroutineScope()
            var step by remember { mutableStateOf(1) }
            var recoverUsername by remember { mutableStateOf("") }
            var recoverEmail by remember { mutableStateOf("") }
            var verificationCodeInput by remember { mutableStateOf("") }
            var newPasswordInput by remember { mutableStateOf("") }
            var isProcessing by remember { mutableStateOf(false) }
            var statusMessage by remember { mutableStateOf("") }
            var isError by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showRecoveryDialog = false },
                title = {
                    Text(
                        text = if (step == 1) "Khôi phục qua Email - Bước 1" else "Đặt lại mật khẩu - Bước 2",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (step == 1) {
                            Text(
                                text = "Nhập tên tài khoản và địa chỉ email đã liên kết để nhận mã xác minh khôi phục mật khẩu tạm thời.",
                                color = Color.LightGray,
                                fontSize = 12.sp
                            )

                            OutlinedTextField(
                                value = recoverUsername,
                                onValueChange = { recoverUsername = it },
                                label = { Text("Tên tài khoản", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF3B82F6),
                                    unfocusedBorderColor = Color.Gray
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = recoverEmail,
                                onValueChange = { recoverEmail = it },
                                label = { Text("Email liên kết", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF3B82F6),
                                    unfocusedBorderColor = Color.Gray
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        } else {
                            Text(
                                text = "Mã xác minh đã được gửi về email thành công. Vui lòng nhập mã và mật khẩu mới của bạn.",
                                color = Color.LightGray,
                                fontSize = 12.sp
                            )

                            OutlinedTextField(
                                value = verificationCodeInput,
                                onValueChange = { verificationCodeInput = it },
                                label = { Text("Mã xác minh (6 số)", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF10B981),
                                    unfocusedBorderColor = Color.Gray
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = newPasswordInput,
                                onValueChange = { newPasswordInput = it },
                                label = { Text("Mật khẩu mới", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF10B981),
                                    unfocusedBorderColor = Color.Gray
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        if (statusMessage.isNotEmpty()) {
                            Text(
                                text = statusMessage,
                                color = if (isError) Color(0xFFEF4444) else Color(0xFF10B981),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (step == 1) {
                                isProcessing = true
                                isError = false
                                viewModel.requestRecoveryCode(recoverUsername, recoverEmail) { success, msg ->
                                    isProcessing = false
                                    statusMessage = msg
                                    if (success) {
                                        step = 2
                                    } else {
                                        isError = true
                                    }
                                }
                            } else {
                                isProcessing = true
                                isError = false
                                viewModel.verifyAndResetPassword(recoverUsername, verificationCodeInput, newPasswordInput) { success, msg ->
                                    isProcessing = false
                                    statusMessage = msg
                                    if (success) {
                                        username = recoverUsername
                                        password = newPasswordInput
                                        coroutineScope.launch {
                                            delay(2500)
                                            showRecoveryDialog = false
                                        }
                                    } else {
                                        isError = true
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (step == 1) Color(0xFF3B82F6) else Color(0xFF10B981)
                        )
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text(if (step == 1) "GỬI MÃ KHÔI PHỤC" else "XÁC NHẬN ĐẶT LẠI", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            if (step == 2) {
                                step = 1
                                statusMessage = ""
                            } else {
                                showRecoveryDialog = false
                            }
                        }
                    ) {
                        Text(if (step == 2) "QUAY LẠI" else "HỦY BỎ", color = Color.Gray)
                    }
                },
                containerColor = Color(0xFF1E293B),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
