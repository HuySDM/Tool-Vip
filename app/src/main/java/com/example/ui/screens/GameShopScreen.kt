package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.window.Dialog
import com.example.data.GameAccount
import com.example.ui.AppViewModel
import com.example.ui.TelexConverter
import com.example.ui.theme.*
import java.text.DecimalFormat

@Composable
fun GameShopScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val accounts by viewModel.allGameAccounts.collectAsState()
    val userAccount by viewModel.userAccount.collectAsState()
    val df = DecimalFormat("#,###")

    var selectedGameFilter by remember { mutableStateOf("ALL") }
    val gameFilters = listOf("ALL", "Liên Quân Mobile", "Free Fire", "PUBG Mobile", "Blood Strike")

    var notificationMessage by remember { mutableStateOf<String?>(null) }
    var showPinConfirmDialogForAcc by remember { mutableStateOf<GameAccount?>(null) }
    var pinInputState by remember { mutableStateOf("") }
    var pinErrorState by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(notificationMessage) {
        if (notificationMessage != null) {
            kotlinx.coroutines.delay(4000)
            notificationMessage = null
        }
    }

    val filteredAccounts = remember(accounts, selectedGameFilter) {
        if (selectedGameFilter == "ALL") accounts else accounts.filter { it.gameTitle == selectedGameFilter }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Shop Title banner
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
                    Text(
                        text = "SHOP ACC GAME VIP",
                        color = BrightTurquoise,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Bán tài khoản game xịn giá siêu rẻ, uy tín hàng đầu Việt Nam!",
                        color = TextGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // User current balance
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DeepObsidian)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Số dư hiện có:", color = TextGray, fontSize = 11.sp)
                        Text(
                            text = "${df.format(userAccount?.balance ?: 0.0)} VND",
                            color = GlowGreen,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (userAccount?.tier == "ADMIN") {
            item {
                var isFormExpanded by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkTealCard),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CoralVibrant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isFormExpanded = !isFormExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AddBox, "Add", tint = CoralVibrant)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ĐẶC QUYỀN ADMIN: THÊM ACC GAME",
                                    color = CoralVibrant,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Icon(
                                imageVector = if (isFormExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle Add Account",
                                tint = TextGray
                            )
                        }

                        AnimatedVisibility(visible = isFormExpanded) {
                            var gameTitleInput by remember { mutableStateOf("Liên Quân Mobile") }
                            var accountNameInput by remember { mutableStateOf("") }
                            var priceInput by remember { mutableStateOf("") }
                            var detailsInput by remember { mutableStateOf("") }
                            var showTitleDropdown by remember { mutableStateOf(false) }

                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                Divider(color = CoralVibrant.copy(alpha = 0.3f))
                                Spacer(modifier = Modifier.height(10.dp))

                                // Game Category Selector
                                Text("Chọn tựa game:", color = TextGray, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(DeepObsidian, RoundedCornerShape(8.dp))
                                        .border(0.5.dp, BorderGreen, RoundedCornerShape(8.dp))
                                        .clickable { showTitleDropdown = !showTitleDropdown }
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = gameTitleInput, color = Color.White, fontSize = 13.sp)
                                        Icon(Icons.Default.ArrowDropDown, "Dropdown", tint = TextGray)
                                    }

                                    DropdownMenu(
                                        expanded = showTitleDropdown,
                                        onDismissRequest = { showTitleDropdown = false },
                                        modifier = Modifier.background(DarkTealCard)
                                    ) {
                                        listOf("Liên Quân Mobile", "Free Fire", "PUBG Mobile", "Blood Strike").forEach { title ->
                                            DropdownMenuItem(
                                                text = { Text(title, color = Color.White) },
                                                onClick = {
                                                    gameTitleInput = title
                                                    showTitleDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Account Name Input
                                OutlinedTextField(
                                    value = accountNameInput,
                                    onValueChange = { accountNameInput = TelexConverter.convert(it) },
                                    label = { Text("Tiêu đề tài khoản (Ví dụ: Acc Trắng Thông Tin)", color = TextGray) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CoralVibrant,
                                        unfocusedBorderColor = BorderGreen,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Price Input
                                OutlinedTextField(
                                    value = priceInput,
                                    onValueChange = { if (it.all { char -> char.isDigit() }) priceInput = it },
                                    label = { Text("Giá tiền bán tùy ý (VND)", color = TextGray) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CoralVibrant,
                                        unfocusedBorderColor = BorderGreen,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Details Input
                                OutlinedTextField(
                                    value = detailsInput,
                                    onValueChange = { detailsInput = TelexConverter.convert(it) },
                                    label = { Text("Chi tiết tài khoản (Trang phục, skin, rank,...)", color = TextGray) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CoralVibrant,
                                        unfocusedBorderColor = BorderGreen,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 2
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        val priceVal = priceInput.toDoubleOrNull()
                                        if (accountNameInput.isNotBlank() && priceVal != null && detailsInput.isNotBlank()) {
                                            viewModel.addNewGameAccount(
                                                title = gameTitleInput,
                                                name = accountNameInput,
                                                price = priceVal,
                                                details = detailsInput
                                            )
                                            // Reset fields
                                            accountNameInput = ""
                                            priceInput = ""
                                            detailsInput = ""
                                            isFormExpanded = false
                                        } else {
                                            viewModel.showToast("Vui lòng điền đầy đủ và đúng định dạng các thông tin!")
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = CoralVibrant, contentColor = Color.White),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Save, "Save")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("ĐĂNG BÁN TÀI KHOẢN NGAY", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Notification toast inside view
        if (notificationMessage != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(TransparentGreen)
                        .border(1.dp, GlowGreen, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ShoppingCart, "Cart Info", tint = GlowGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = notificationMessage!!,
                            color = GlowGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Horizontal filter bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                gameFilters.forEach { filter ->
                    val isSelected = selectedGameFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) TransparentGreen else BorderGreen)
                            .border(
                                1.dp,
                                if (isSelected) BrightTurquoise else Color.Transparent,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedGameFilter = filter }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) BrightTurquoise else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Account listings
        if (filteredAccounts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Hiện tại không có tài khoản game nào ở danh mục này.", color = TextGray, fontSize = 13.sp)
                }
            }
        } else {
            items(filteredAccounts, key = { it.id }) { acc ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (acc.isBought) BorderGreen else BrightTurquoise.copy(alpha = 0.3f),
                            RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = DarkTealCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(BorderGreen)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = acc.gameTitle,
                                    color = BrightTurquoise,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            if (acc.isBought) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CoralVibrant.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "ĐÃ BÁN / ĐÃ SỞ HỮU",
                                        color = CoralVibrant,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            } else {
                                Text(
                                    text = "${df.format(acc.price)} VND",
                                    color = GlowGreen,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = acc.accountName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = acc.details,
                            color = TextGray,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (acc.isBought) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BorderGreen)
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "🔑 Thông tin tài khoản của bạn:",
                                        color = BrightTurquoise,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "Tài khoản: toolvip_buyer_${acc.id}\nMật khẩu: AdminVipPassword123",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    val bal = userAccount?.balance ?: 0.0
                                    if (bal >= acc.price) {
                                        pinInputState = ""
                                        pinErrorState = null
                                        showPinConfirmDialogForAcc = acc
                                    } else {
                                        notificationMessage = "Bạn không đủ số dư để mua! Vui lòng nạp thêm tiền tại ví nạp."
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = BrightTurquoise, contentColor = DeepObsidian),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.LocalMall, "Buy")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "MUA TÀI KHOẢN VỚI ${df.format(acc.price)} VND", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPinConfirmDialogForAcc != null) {
        val acc = showPinConfirmDialogForAcc!!
        var isPinVisible by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showPinConfirmDialogForAcc = null }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1E293B))
                    .border(1.dp, BorderGreen, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(CoralVibrant.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Shield Security",
                            tint = CoralVibrant,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Text(
                        text = "MÃ XÁC THỰC GIAO DỊCH",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Để mua tài khoản [${acc.accountName}], vui lòng nhập mã xác thực bảo mật 2 lớp (PIN) của bạn.",
                        color = TextGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    OutlinedTextField(
                        value = pinInputState,
                        onValueChange = {
                            pinInputState = it
                            pinErrorState = null
                        },
                        placeholder = { Text("Nhập mã PIN xác thực...", color = TextGray, fontSize = 12.sp) },
                        visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = BrightTurquoise,
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
                        isError = pinErrorState != null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (pinErrorState != null) {
                        Text(
                            text = pinErrorState!!,
                            color = CoralVibrant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(
                            onClick = {
                                showPinConfirmDialogForAcc = null
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("HỦY BỎ", color = TextGray, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                if (pinInputState.isNotBlank()) {
                                    viewModel.verifyCurrentUserAuthPin(pinInputState) { success ->
                                        if (success) {
                                            viewModel.purchaseGameAccount(acc)
                                            notificationMessage = "Mua thành công tài khoản! Mã truy cập đã được mở khóa trực tiếp!"
                                            showPinConfirmDialogForAcc = null
                                        } else {
                                            pinErrorState = "Mã xác thực 2 lớp sai! Vui lòng kiểm tra lại."
                                        }
                                    }
                                } else {
                                    pinErrorState = "Vui lòng nhập mã PIN xác thực."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrightTurquoise),
                            modifier = Modifier.weight(1.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("XÁC NHẬN", color = DeepObsidian, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
