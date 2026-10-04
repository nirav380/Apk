package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.RepaymentEntity
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.*

fun formatRupee(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    formatter.maximumFractionDigits = 0
    return formatter.format(amount)
}

@Composable
fun TopBarHeader(
    currentRole: String,
    isServerConnected: Boolean = false,
    onRoleChange: (String) -> Unit,
    onOpenReports: () -> Unit,
    onOpenServerSettings: () -> Unit,
    onOpenWebPortal: () -> Unit,
    onLogout: (() -> Unit)? = null
) {
    var showRoleMenu by remember { mutableStateOf(false) }

    Surface(
        color = Navy900,
        contentColor = Color.White,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "Shri Kamdar Credit Co-Operative Society Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Shri Kamdar Credit Co-Op",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = (-0.2).sp
                        )
                        Text(
                            text = "Sahakari Mandali • Reg: SK-4892",
                            style = MaterialTheme.typography.bodySmall,
                            color = Navy200
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Somee.com Server Cloud Button
                    IconButton(
                        onClick = onOpenServerSettings,
                        modifier = Modifier.testTag("server_settings_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (isServerConnected) {
                                    Badge(containerColor = Emerald500)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isServerConnected) Icons.Default.CloudDone else Icons.Default.CloudSync,
                                contentDescription = "Somee Server Sync",
                                tint = if (isServerConnected) Emerald500 else Gold100
                            )
                        }
                    }

                    // Web Portal Button
                    IconButton(
                        onClick = onOpenWebPortal,
                        modifier = Modifier.testTag("web_portal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Somee Web Portal",
                            tint = Color.White
                        )
                    }

                    // Reports Button
                    IconButton(
                        onClick = onOpenReports,
                        modifier = Modifier.testTag("reports_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = "Reports",
                            tint = Color.White
                        )
                    }

                    // Role Switcher
                    Box {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Navy800,
                            border = BorderStroke(1.dp, Navy700),
                            modifier = Modifier
                                .clickable { showRoleMenu = true }
                                .testTag("role_switcher")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = "Role",
                                    tint = Gold500,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentRole.contains("Admin")) "Admin" else if (currentRole.contains("Recovery")) "Officer" else "Member",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showRoleMenu,
                            onDismissRequest = { showRoleMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Admin / Manager") },
                                onClick = {
                                    onRoleChange("Admin / Manager")
                                    showRoleMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Security, contentDescription = null, tint = Navy900) }
                            )
                            DropdownMenuItem(
                                text = { Text("Recovery Officer") },
                                onClick = {
                                    onRoleChange("Recovery Officer")
                                    showRoleMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Emerald600) }
                            )
                            DropdownMenuItem(
                                text = { Text("Member View") },
                                onClick = {
                                    onRoleChange("Member")
                                    showRoleMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Gold600) }
                            )
                            if (onLogout != null) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                DropdownMenuItem(
                                    text = { Text("Logout / લૉગઆઉટ", color = Rose600, fontWeight = FontWeight.SemiBold) },
                                    onClick = {
                                        showRoleMenu = false
                                        onLogout()
                                    },
                                    leadingIcon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Rose600) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor) = when (status) {
        "Approved", "Active", "Kept", "Connected" -> Pair(Emerald100, Emerald700)
        "Pending", "SMA-0", "SMA-1" -> Pair(Gold100, Gold600)
        "Overdue", "Broken", "SMA-2", "NPA", "Rejected", "Offline" -> Pair(Rose100, Rose700)
        "Closed" -> Pair(Slate200, Slate700)
        else -> Pair(Navy100, Navy900)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = Modifier.padding(2.dp)
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
            }
        }
    }
}

@Composable
fun ReceiptDialog(
    repayment: RepaymentEntity,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Emerald100),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Emerald600,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Payment Collected!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "Shri Kamdar Credit Co-Operative Society",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate50, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    ReceiptRow(label = "Receipt No", value = repayment.receiptNo)
                    ReceiptRow(label = "Date", value = repayment.paymentDate)
                    ReceiptRow(label = "Member", value = "${repayment.memberName} (${repayment.memberNo})")
                    ReceiptRow(label = "Loan Account", value = repayment.loanNo)
                    ReceiptRow(label = "Mode", value = repayment.paymentMode)
                    ReceiptRow(label = "Collector", value = repayment.collectedBy)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ReceiptRow(label = "Principal Split", value = formatRupee(repayment.principalPaid))
                    ReceiptRow(label = "Interest Split", value = formatRupee(repayment.interestPaid))
                    ReceiptRow(
                        label = "Amount Received",
                        value = formatRupee(repayment.amountPaid),
                        isBold = true,
                        highlightColor = Emerald600
                    )
                    ReceiptRow(
                        label = "Balance Outstanding",
                        value = formatRupee(repayment.outstandingBalance),
                        isBold = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close")
                    }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Navy900)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share Receipt")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    highlightColor: Color? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Slate600
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = highlightColor ?: Slate900
        )
    }
}
