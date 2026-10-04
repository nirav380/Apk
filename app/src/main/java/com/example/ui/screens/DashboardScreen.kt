package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LoanEntity
import com.example.ui.components.LoanStatementDialog
import com.example.ui.components.LoanTrackingCard
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.UpcomingRemindersBanner
import com.example.ui.components.formatRupee
import com.example.ui.theme.*
import com.example.ui.viewmodel.CoopViewModel

@Composable
fun DashboardScreen(
    viewModel: CoopViewModel,
    onNavigateToMembers: () -> Unit,
    onNavigateToLoans: () -> Unit,
    onNavigateToCollection: () -> Unit,
    onNavigateToRecovery: () -> Unit
) {
    val summary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
    val repayments by viewModel.repayments.collectAsStateWithLifecycle()
    val loans by viewModel.loans.collectAsStateWithLifecycle()

    var selectedLoanForStatement by remember { mutableStateOf<LoanEntity?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Navy800),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Society Operations",
                                style = MaterialTheme.typography.bodySmall,
                                color = Navy200
                            )
                            Text(
                                text = "Co-Operative Dashboard",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Amber600,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(
                                text = "LIVE DATA",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Navy900,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Outstanding", style = MaterialTheme.typography.bodySmall, color = Navy200)
                            Text(
                                text = formatRupee(summary.totalOutstanding),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Today's Collection", style = MaterialTheme.typography.bodySmall, color = Navy200)
                            Text(
                                text = formatRupee(summary.todayCollection),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Amber100
                            )
                        }
                    }
                }
            }
        }

        // 3-Day Upcoming Loan Payment Deadline Reminder System
        item {
            UpcomingRemindersBanner(
                loans = loans,
                onPayLoan = { onNavigateToCollection() }
            )
        }

        // Quick Actions Grid
        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionItem(
                    title = "New Member",
                    icon = Icons.Default.PersonAdd,
                    color = Navy800,
                    modifier = Modifier.weight(1f),
                    tag = "quick_new_member",
                    onClick = onNavigateToMembers
                )
                QuickActionItem(
                    title = "Disburse Loan",
                    icon = Icons.Default.AttachMoney,
                    color = Amber700,
                    modifier = Modifier.weight(1f),
                    tag = "quick_disburse_loan",
                    onClick = onNavigateToLoans
                )
                QuickActionItem(
                    title = "Daily Counter",
                    icon = Icons.Default.ReceiptLong,
                    color = Green700,
                    modifier = Modifier.weight(1f),
                    tag = "quick_daily_counter",
                    onClick = onNavigateToCollection
                )
                QuickActionItem(
                    title = "Recovery",
                    icon = Icons.Default.Warning,
                    color = Red700,
                    modifier = Modifier.weight(1f),
                    tag = "quick_recovery",
                    onClick = onNavigateToRecovery
                )
            }
        }

        // Summary Metric Cards
        item {
            Text(
                text = "Financial Portfolio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Disbursed",
                    value = formatRupee(summary.totalDisbursed),
                    subtitle = "${summary.totalLoansCount} Total Loans",
                    icon = Icons.Default.AccountBalanceWallet,
                    containerColor = Navy100.copy(alpha = 0.5f),
                    iconColor = Navy800,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToLoans
                )
                StatCard(
                    title = "Total Recovered",
                    value = formatRupee(summary.totalRecovered),
                    subtitle = "${summary.closedLoansCount} Fully Repaid",
                    icon = Icons.Default.TrendingUp,
                    containerColor = Green100.copy(alpha = 0.5f),
                    iconColor = Green700,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToCollection
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Active Members",
                    value = "${summary.activeMembers}",
                    subtitle = "${summary.pendingMembers} Approval Pending",
                    icon = Icons.Default.People,
                    containerColor = Amber100.copy(alpha = 0.5f),
                    iconColor = Amber700,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToMembers
                )
                StatCard(
                    title = "Month Collection",
                    value = formatRupee(summary.monthCollection),
                    subtitle = "October 2026",
                    icon = Icons.Default.CalendarMonth,
                    containerColor = Slate100,
                    iconColor = Slate700,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToCollection
                )
            }
        }

        // NPA & Overdue Alert Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Red100.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToRecovery() }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Red700)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "NPA & SMA Overdue Alerts",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Red700
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Red700)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        BucketChip(label = "SMA-0 (1-30d)", count = summary.sma0Count, color = Amber700)
                        BucketChip(label = "SMA-1 (31-60d)", count = summary.sma1Count, color = Amber700)
                        BucketChip(label = "SMA-2 (61-90d)", count = summary.sma2Count, color = Red700)
                        BucketChip(label = "NPA (>90d)", count = summary.totalNpaCount, color = Red700)
                    }
                }
            }
        }

        // Active Loan Tracking Section (Active loan details, EMI status, and upcoming deadlines)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Active Loan Tracker (સક્રિય લોન ટ્રેકિંગ)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "EMI Status, Progress & Payment Deadlines",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                }
                TextButton(onClick = onNavigateToLoans) {
                    Text("View All (${loans.count { it.status != "Closed" }})", color = Navy800, fontWeight = FontWeight.Bold)
                }
            }
        }

        val activeLoansForTracking = loans.filter { it.status != "Closed" }.take(3)
        if (activeLoansForTracking.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Slate200)
                ) {
                    Box(modifier = Modifier.padding(20.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No active loans at present.", color = Slate500)
                    }
                }
            }
        } else {
            items(activeLoansForTracking, key = { "tracker_${it.loanId}" }) { loan ->
                LoanTrackingCard(
                    loan = loan,
                    onPayEmiClick = { onNavigateToCollection() },
                    onViewStatementClick = { selectedLoanForStatement = it }
                )
            }
        }

        // Recent Repayments
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Daily Collections",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                TextButton(onClick = onNavigateToCollection) {
                    Text("View All")
                }
            }
        }

        if (repayments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate100)
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No collections recorded yet.", color = Slate600)
                    }
                }
            }
        } else {
            items(repayments.take(4)) { rep ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Green100),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Receipt, contentDescription = null, tint = Green700, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = rep.memberName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Text(
                                    text = "${rep.loanNo} • ${rep.paymentMode} • ${rep.paymentDate}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate500
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "+${formatRupee(rep.amountPaid)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Green700
                            )
                            Text(
                                text = rep.receiptNo,
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400
                            )
                        }
                    }
                }
            }
        }
    }

    selectedLoanForStatement?.let { loan ->
        LoanStatementDialog(
            loan = loan,
            repayments = repayments,
            onDismiss = { selectedLoanForStatement = null }
        )
    }
}

@Composable
private fun QuickActionItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    tag: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .testTag(tag)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Slate800
            )
        }
    }
}

@Composable
private fun BucketChip(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Slate600
        )
    }
}
