package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LoanEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@Composable
fun LoanTrackingCard(
    loan: LoanEntity,
    modifier: Modifier = Modifier,
    onPayEmiClick: ((LoanEntity) -> Unit)? = null,
    onViewStatementClick: ((LoanEntity) -> Unit)? = null
) {
    var isExpanded by remember { mutableStateOf(false) }

    // Calculate progress (Paid vs Total)
    val totalAmount = if (loan.totalAmount > 0) loan.totalAmount else loan.principalAmount * 1.12
    val paidAmount = loan.totalPaid.coerceAtLeast(0.0)
    val progressRatio = (paidAmount / totalAmount).coerceIn(0.0, 1.0).toFloat()
    val animatedProgress by animateFloatAsState(targetValue = progressRatio, label = "LoanProgress")
    val progressPercent = (progressRatio * 100).roundToInt()

    // Calculate deadline status
    val deadlineInfo = remember(loan.dueDate, loan.status, loan.daysOverdue) {
        computeDeadlineInfo(loan.dueDate, loan.status, loan.daysOverdue)
    }

    val loanIcon = getLoanTypeIcon(loan.loanType)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (deadlineInfo.isDueSoon) AlertOrangeBg.copy(alpha = 0.5f) else Color.White),
        border = when {
            deadlineInfo.isOverdue -> BorderStroke(1.5.dp, BrandErr.copy(alpha = 0.6f))
            deadlineInfo.isDueSoon -> BorderStroke(2.dp, AlertOrangeBorder)
            else -> BorderStroke(1.dp, BrandLine)
        },
        elevation = CardDefaults.cardElevation(defaultElevation = if (deadlineInfo.isDueSoon) 4.dp else 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("loan_tracking_card_${loan.loanNo}")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top Accent Bar (Red if overdue, Amber-Orange if due in 3 days, Green if completed, Blue if normal)
            val topBarColor = when {
                deadlineInfo.isOverdue -> BrandErr
                loan.status == "Closed" -> BrandOk
                deadlineInfo.isDueSoon -> AlertOrange
                else -> BrandBlue
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (deadlineInfo.isDueSoon) 5.dp else 4.dp)
                    .background(topBarColor)
            )

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header: Loan Type, Account No, and Status Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(BrandBlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = loanIcon,
                                contentDescription = loan.loanType,
                                tint = BrandBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = loan.loanType,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandInk,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = "A/C: ${loan.loanNo} • ${loan.memberName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = BrandMuted
                            )
                        }
                    }

                    // Deadline / Overdue Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = deadlineInfo.badgeBgColor
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = deadlineInfo.icon,
                                contentDescription = null,
                                tint = deadlineInfo.badgeTextColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = deadlineInfo.badgeText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = deadlineInfo.badgeTextColor
                            )
                        }
                    }
                }

                HorizontalDivider(color = BrandLine.copy(alpha = 0.6f))

                // Key Loan Numbers: EMI Amount & Outstanding Balance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Monthly EMI (માસિક હપ્તો)",
                            style = MaterialTheme.typography.bodySmall,
                            color = BrandMuted
                        )
                        Text(
                            text = formatRupee(loan.emiAmount),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlueDark
                        )
                        Text(
                            text = "@ ${loan.interestRate}% p.a. • ${loan.tenureMonths} Mo",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrandMuted
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Balance Due (બાકી રકમ)",
                            style = MaterialTheme.typography.bodySmall,
                            color = BrandMuted
                        )
                        Text(
                            text = formatRupee(loan.outstandingBalance),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (deadlineInfo.isOverdue) BrandErr else BrandInk
                        )
                        Text(
                            text = "Total: ${formatRupee(totalAmount)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrandMuted
                        )
                    }
                }

                // Progress Bar: Paid vs Remaining
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Paid: ${formatRupee(paidAmount)} ($progressPercent%)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandOk
                        )
                        Text(
                            text = "Remaining: ${formatRupee(loan.outstandingBalance)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrandMuted
                        )
                    }
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (deadlineInfo.isOverdue) BrandErr else BrandOk,
                        trackColor = BrandLine.copy(alpha = 0.5f)
                    )
                }

                // Upcoming Payment Deadline Banner
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = deadlineInfo.bannerBgColor,
                    border = BorderStroke(1.dp, deadlineInfo.bannerBorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = "Due Date",
                                tint = deadlineInfo.bannerTextColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Due Date: ${loan.dueDate}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = deadlineInfo.bannerTextColor
                                )
                                Text(
                                    text = deadlineInfo.urgencyDetail,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = deadlineInfo.bannerTextColor.copy(alpha = 0.85f)
                                )
                            }
                        }

                        if (onPayEmiClick != null && loan.status != "Closed") {
                            Button(
                                onClick = { onPayEmiClick(loan) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (deadlineInfo.isOverdue) BrandErr else if (deadlineInfo.isDueSoon) AlertOrange else BrandOk
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp).testTag("pay_emi_btn_${loan.loanNo}")
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Pay EMI",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Expandable Details (Amortization Split & Guarantors)
                AnimatedVisibility(visible = isExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BrandPaper, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Loan Breakdown & Guarantor Details",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrandInk
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            LoanDetailRow(title = "Disbursed On", value = loan.disbursedDate)
                            LoanDetailRow(title = "Principal Sanctioned", value = formatRupee(loan.principalAmount))
                        }

                        val estimatedMonthlyInterest = (loan.outstandingBalance * (loan.interestRate / 100.0) / 12.0)
                        val estimatedMonthlyPrincipal = (loan.emiAmount - estimatedMonthlyInterest).coerceAtLeast(0.0)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            LoanDetailRow(title = "Est. Principal / EMI", value = formatRupee(estimatedMonthlyPrincipal))
                            LoanDetailRow(title = "Est. Interest / EMI", value = formatRupee(estimatedMonthlyInterest))
                        }

                        if (loan.guarantor1Name.isNotBlank()) {
                            HorizontalDivider(color = BrandLine.copy(alpha = 0.5f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                LoanDetailRow(title = "Guarantor 1 (જામીનદાર ૧)", value = "${loan.guarantor1Name} (${loan.guarantor1Mobile})")
                            }
                        }

                        if (loan.guarantor2Name.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                LoanDetailRow(title = "Guarantor 2 (જામીનદાર ૨)", value = "${loan.guarantor2Name} (${loan.guarantor2Mobile})")
                            }
                        }

                        if (onViewStatementClick != null) {
                            OutlinedButton(
                                onClick = { onViewStatementClick(loan) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, BrandLine),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandBlue)
                            ) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("View Loan Passbook / સ્ટેટમેન્ટ", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }

                // Expand / Collapse Footer Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded }
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isExpanded) "Hide Details (ઓછી વિગત)" else "View Breakdown & Guarantors (વધુ વિગત)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandBlue
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = BrandBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LoanDetailRow(title: String, value: String) {
    Column {
        Text(text = title, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = BrandMuted)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = BrandInk)
    }
}

private data class DeadlineInfo(
    val isOverdue: Boolean,
    val isDueSoon: Boolean,
    val badgeText: String,
    val badgeBgColor: Color,
    val badgeTextColor: Color,
    val bannerBgColor: Color,
    val bannerBorderColor: Color,
    val bannerTextColor: Color,
    val urgencyDetail: String,
    val icon: ImageVector
)

private fun computeDeadlineInfo(dueDateStr: String, status: String, daysOverdue: Int): DeadlineInfo {
    if (status == "Closed") {
        return DeadlineInfo(
            isOverdue = false,
            isDueSoon = false,
            badgeText = "Closed (પૂર્ણ)",
            badgeBgColor = BrandPaper,
            badgeTextColor = BrandMuted,
            bannerBgColor = BrandPaper,
            bannerBorderColor = BrandLine,
            bannerTextColor = BrandMuted,
            urgencyDetail = "All EMIs fully cleared. No pending balance.",
            icon = Icons.Default.CheckCircle
        )
    }

    if (daysOverdue > 0 || status == "Overdue") {
        val days = if (daysOverdue > 0) daysOverdue else 7
        return DeadlineInfo(
            isOverdue = true,
            isDueSoon = false,
            badgeText = "Overdue by $days d",
            badgeBgColor = BrandErrLight,
            badgeTextColor = BrandErr,
            bannerBgColor = BrandErrLight,
            bannerBorderColor = BrandErr.copy(alpha = 0.3f),
            bannerTextColor = BrandErr,
            urgencyDetail = "Immediate payment required to avoid ₹150 late fee & NPA classification.",
            icon = Icons.Default.Warning
        )
    }

    // Try parsing due date
    return try {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        val dueDate = sdf.parse(dueDateStr) ?: Date()
        val today = Date()
        val diffMs = dueDate.time - today.time
        val diffDays = (diffMs / (1000 * 60 * 60 * 24)).toInt()

        when {
            diffDays < 0 -> {
                DeadlineInfo(
                    isOverdue = true,
                    isDueSoon = false,
                    badgeText = "Overdue (${-diffDays}d ago)",
                    badgeBgColor = BrandErrLight,
                    badgeTextColor = BrandErr,
                    bannerBgColor = BrandErrLight,
                    bannerBorderColor = BrandErr.copy(alpha = 0.3f),
                    bannerTextColor = BrandErr,
                    urgencyDetail = "Due date passed! Please clear EMI immediately.",
                    icon = Icons.Default.Error
                )
            }
            diffDays in 0..3 -> {
                val alertLabel = when (diffDays) {
                    0 -> "🔔 DUE TODAY!"
                    1 -> "🔔 DUE IN 1 DAY"
                    else -> "🔔 DUE IN $diffDays DAYS"
                }
                DeadlineInfo(
                    isOverdue = false,
                    isDueSoon = true,
                    badgeText = alertLabel,
                    badgeBgColor = AlertOrangeLight,
                    badgeTextColor = AlertOrangeText,
                    bannerBgColor = AlertOrangeBg,
                    bannerBorderColor = AlertOrangeBorder,
                    bannerTextColor = AlertOrangeText,
                    urgencyDetail = "Payment deadline within 3 days! Pay on time to avoid penalties & protect credit score.",
                    icon = Icons.Default.NotificationsActive
                )
            }
            else -> {
                DeadlineInfo(
                    isOverdue = false,
                    isDueSoon = false,
                    badgeText = "Upcoming ($diffDays d)",
                    badgeBgColor = BrandOkLight,
                    badgeTextColor = BrandOk,
                    bannerBgColor = BrandPaper,
                    bannerBorderColor = BrandLine,
                    bannerTextColor = BrandBlueDark,
                    urgencyDetail = "Upcoming monthly deadline. Regular status.",
                    icon = Icons.Default.CheckCircle
                )
            }
        }
    } catch (e: Exception) {
        DeadlineInfo(
            isOverdue = false,
            isDueSoon = false,
            badgeText = "Active",
            badgeBgColor = BrandOkLight,
            badgeTextColor = BrandOk,
            bannerBgColor = BrandPaper,
            bannerBorderColor = BrandLine,
            bannerTextColor = BrandBlueDark,
            urgencyDetail = "Next EMI due on $dueDateStr.",
            icon = Icons.Default.CalendarToday
        )
    }
}

private fun getLoanTypeIcon(type: String): ImageVector {
    return when {
        type.contains("Agri", ignoreCase = true) -> Icons.Default.Agriculture
        type.contains("Gold", ignoreCase = true) -> Icons.Default.Diamond
        type.contains("Business", ignoreCase = true) -> Icons.Default.BusinessCenter
        type.contains("Vehicle", ignoreCase = true) -> Icons.Default.DirectionsCar
        type.contains("Emergency", ignoreCase = true) -> Icons.Default.Emergency
        else -> Icons.Default.CreditCard
    }
}
