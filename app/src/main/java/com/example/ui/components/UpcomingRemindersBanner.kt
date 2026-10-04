package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LoanEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

val AlertOrange = Color(0xFFEA580C)
val AlertOrangeLight = Color(0xFFFFEDD5)
val AlertOrangeBg = Color(0xFFFFF7ED)
val AlertOrangeBorder = Color(0xFFF97316)
val AlertOrangeText = Color(0xFF9A3412)

data class LoanDueAlert(
    val loan: LoanEntity,
    val daysRemaining: Int,
    val formattedDueDate: String
)

fun getUpcomingLoansWithinDays(loans: List<LoanEntity>, maxDays: Int = 3): List<LoanDueAlert> {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
    val today = Date()

    return loans.filter { it.status == "Active" }.mapNotNull { loan ->
        try {
            val dueDate = sdf.parse(loan.dueDate) ?: return@mapNotNull null
            val diffMs = dueDate.time - today.time
            val diffDays = (diffMs / (1000 * 60 * 60 * 24)).toInt()

            if (diffDays in 0..maxDays) {
                LoanDueAlert(loan, diffDays, loan.dueDate)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }.sortedBy { it.daysRemaining }
}

@Composable
fun UpcomingRemindersBanner(
    loans: List<LoanEntity>,
    onPayLoan: (LoanEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val upcomingAlerts = remember(loans) { getUpcomingLoansWithinDays(loans, maxDays = 3) }

    if (upcomingAlerts.isEmpty()) return

    var isExpanded by remember { mutableStateOf(true) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AlertOrangeBg),
        border = BorderStroke(2.dp, AlertOrangeBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(AlertOrange, Color(0xFFF97316))
                        )
                    )
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Alert Bell",
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "3-Day EMI Payment Deadline Reminder",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${upcomingAlerts.size} loans have EMI deadlines within 3 days (હપ્તા રીમાઇન્ડર)",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White
                ) {
                    Text(
                        text = "${upcomingAlerts.size} DUE",
                        fontWeight = FontWeight.Bold,
                        color = AlertOrange,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    upcomingAlerts.forEach { alert ->
                        ReminderLoanItem(
                            alert = alert,
                            onPay = { onPayLoan(alert.loan) },
                            onSendSms = { sendReminderSms(context, alert.loan, alert.daysRemaining) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderLoanItem(
    alert: LoanDueAlert,
    onPay: () -> Unit,
    onSendSms: () -> Unit
) {
    val daysText = when (alert.daysRemaining) {
        0 -> "DUE TODAY!"
        1 -> "DUE TOMORROW (1d)"
        else -> "DUE IN ${alert.daysRemaining} DAYS"
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.5.dp, AlertOrangeBorder.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AlertOrangeLight
                    ) {
                        Text(
                            text = daysText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AlertOrangeText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = alert.loan.loanNo,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrandInk
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${alert.loan.memberName} • +91 ${alert.loan.mobile}",
                    style = MaterialTheme.typography.bodySmall,
                    color = BrandInk,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "EMI Amount: ${formatRupee(alert.loan.emiAmount)} • Due: ${alert.formattedDueDate}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = AlertOrangeText
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // SMS/WhatsApp Reminder Button
                IconButton(
                    onClick = onSendSms,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Send Reminder",
                        tint = AlertOrange,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Pay Button
                Button(
                    onClick = onPay,
                    colors = ButtonDefaults.buttonColors(containerColor = AlertOrange),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Pay", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

private fun sendReminderSms(context: Context, loan: LoanEntity, daysRemaining: Int) {
    val dueStr = if (daysRemaining == 0) "TODAY" else "in $daysRemaining days (${loan.dueDate})"
    val message = "Dear ${loan.memberName}, your EMI of ₹${loan.emiAmount.toInt()} for Loan Account #${loan.loanNo} is due $dueStr. Please pay on time to avoid penalties. - Shri Kamdar Co-Op Society Ltd."

    try {
        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:${loan.mobile}")
            putExtra("sms_body", message)
        }
        context.startActivity(smsIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "Reminder SMS generated for ${loan.memberName}", Toast.LENGTH_SHORT).show()
    }
}
