package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import com.example.ui.viewmodel.CoopViewModel

@Composable
fun LoginScreen(
    viewModel: CoopViewModel,
    onLoginSuccess: () -> Unit,
    onOpenWebLogin: () -> Unit
) {
    val context = LocalContext.current

    var mobileNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var generatedOtp by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Admin / Manager") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showQuickProfiles by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandPaper) // Soft authentic paper background #EEF3FA
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Main Login Card with top brand accent border
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BrandLine),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Top 5px solid brand blue stripe (identical to web app)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .background(BrandBlue)
                    )

                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Original Society Logo
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "Shri Kamdar Credit Co-Operative Society Logo",
                            modifier = Modifier
                                .width(180.dp)
                                .height(95.dp),
                            contentScale = ContentScale.Fit
                        )

                        Text(
                            text = "Log in to your account",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = BrandInk,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (!isOtpSent)
                                "Enter your registered mobile number. We will text you a 6-digit OTP.\n(તમારો નોંધાયેલ મોબાઈલ નંબર દાખલ કરો)"
                            else
                                "We sent a 6-digit OTP to +91 $mobileNumber\n(ઓટીપી દાખલ કરો)",
                            style = MaterialTheme.typography.bodySmall,
                            color = BrandMuted,
                            textAlign = TextAlign.Center
                        )

                        errorMessage?.let { msg ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BrandErrLight,
                                border = BorderStroke(1.dp, BrandErr.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = msg,
                                    color = BrandErr,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        if (!isOtpSent) {
                            // Mobile Number Input
                            OutlinedTextField(
                                value = mobileNumber,
                                onValueChange = {
                                    if (it.length <= 10 && it.all { c -> c.isDigit() }) {
                                        mobileNumber = it
                                        errorMessage = null
                                    }
                                },
                                label = { Text("Mobile number (મોબાઈલ નંબર)") },
                                placeholder = { Text("9876543210") },
                                leadingIcon = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 12.dp, end = 6.dp)
                                    ) {
                                        Text("+91", fontWeight = FontWeight.Bold, color = BrandInk)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(1.dp)
                                                .height(20.dp)
                                                .background(BrandLine)
                                        )
                                    }
                                },
                                trailingIcon = {
                                    if (mobileNumber.length == 10) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Valid", tint = BrandOk)
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("login_mobile_input"),
                                singleLine = true
                            )

                            // Role Selection
                            Text(
                                text = "Select Login Role",
                                style = MaterialTheme.typography.labelSmall,
                                color = BrandMuted,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            val roles = listOf("Admin / Manager", "Recovery Officer", "Member")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                roles.forEach { role ->
                                    FilterChip(
                                        selected = selectedRole == role,
                                        onClick = { selectedRole = role },
                                        label = {
                                            Text(
                                                text = if (role.contains("Admin")) "Admin" else if (role.contains("Recovery")) "Recovery" else "Member",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = BrandBlue,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }

                            // Send OTP Button
                            Button(
                                onClick = {
                                    if (mobileNumber.length < 10) {
                                        errorMessage = "Please enter a valid 10-digit mobile number."
                                    } else {
                                        errorMessage = null
                                        val simulatedOtp = "123456"
                                        generatedOtp = simulatedOtp
                                        isOtpSent = true
                                        Toast.makeText(context, "OTP Sent: $simulatedOtp", Toast.LENGTH_LONG).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("send_otp_button")
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Send OTP / ઓટીપી મોકલો", fontWeight = FontWeight.Bold)
                            }

                        } else {
                            // OTP Input
                            OutlinedTextField(
                                value = otpCode,
                                onValueChange = {
                                    if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                        otpCode = it
                                        errorMessage = null
                                    }
                                },
                                label = { Text("6-Digit OTP (ઓટીપી)") },
                                placeholder = { Text("Enter $generatedOtp") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BrandBlue) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("login_otp_input"),
                                singleLine = true
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BrandGoldLight,
                                border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Demo OTP: $generatedOtp",
                                        fontWeight = FontWeight.Bold,
                                        color = BrandGold,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    TextButton(
                                        onClick = { otpCode = generatedOtp },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Auto Fill", color = BrandBlueDark, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }

                            // Verify Button
                            Button(
                                onClick = {
                                    if (otpCode.length < 4) {
                                        errorMessage = "Please enter 6-digit OTP."
                                    } else {
                                        viewModel.loginUser(mobileNumber, selectedRole)
                                        onLoginSuccess()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandOk),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("verify_otp_button")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Verify & Login / લૉગિન કરો", fontWeight = FontWeight.Bold)
                            }

                            TextButton(
                                onClick = { isOtpSent = false }
                            ) {
                                Text("Change Mobile Number / નંબર બદલો", color = BrandBlue, style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = BrandLine)

                        // Quick 1-Tap Demo Logins
                        TextButton(
                            onClick = { showQuickProfiles = !showQuickProfiles }
                        ) {
                            Icon(Icons.Default.FlashOn, contentDescription = null, tint = BrandGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (showQuickProfiles) "Hide Quick Logins" else "Quick 1-Tap Logins (ડેમો લૉગિન)",
                                color = BrandInk,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        AnimatedVisibility(visible = showQuickProfiles) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BrandPaper, RoundedCornerShape(10.dp))
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                DemoLoginRow(
                                    title = "Branch Manager (Admin)",
                                    mobile = "9999900001"
                                ) {
                                    mobileNumber = "9999900001"
                                    selectedRole = "Admin / Manager"
                                    viewModel.loginUser("9999900001", "Admin / Manager")
                                    onLoginSuccess()
                                }
                                DemoLoginRow(
                                    title = "Recovery Officer (Inspector)",
                                    mobile = "9824099881"
                                ) {
                                    mobileNumber = "9824099881"
                                    selectedRole = "Recovery Officer"
                                    viewModel.loginUser("9824099881", "Recovery Officer")
                                    onLoginSuccess()
                                }
                                DemoLoginRow(
                                    title = "Society Member (Ramesh Patel)",
                                    mobile = "9825012345"
                                ) {
                                    mobileNumber = "9825012345"
                                    selectedRole = "Member"
                                    viewModel.loginUser("9825012345", "Member")
                                    onLoginSuccess()
                                }
                            }
                        }

                        // Direct Web Portal Login Option
                        OutlinedButton(
                            onClick = onOpenWebLogin,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, BrandLine),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandBlue)
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open Somee Web Login.aspx (વેબ પોર્ટલ)")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "શ્રી કામદાર ક્રેડિટ કો-ઓપરેટિવ સોસાયટી લી. • રજી. નં. SK-4892",
                style = MaterialTheme.typography.bodySmall,
                color = BrandMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DemoLoginRow(
    title: String,
    mobile: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BrandLine),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = BrandInk)
                Text("+91 $mobile", style = MaterialTheme.typography.bodySmall, color = BrandMuted)
            }
            Text("Login →", fontWeight = FontWeight.Bold, color = BrandBlue, style = MaterialTheme.typography.labelSmall)
        }
    }
}
