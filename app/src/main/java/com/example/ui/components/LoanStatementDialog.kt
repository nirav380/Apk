package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.LoanEntity
import com.example.data.model.RepaymentEntity
import com.example.ui.theme.*

@Composable
fun LoanStatementDialog(
    loan: LoanEntity,
    repayments: List<RepaymentEntity>,
    onDismiss: () -> Unit
) {
    val loanRepayments = repayments.filter { it.loanNo == loan.loanNo }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BrandLine),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Loan Passbook / ખાતાવહી",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrandInk
                        )
                        Text(
                            text = "A/C: ${loan.loanNo} • ${loan.memberName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = BrandMuted
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = BrandMuted)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BrandLine)

                // Summary Stats
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BrandPaper,
                    border = BorderStroke(1.dp, BrandLine),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Sanctioned", style = MaterialTheme.typography.labelSmall, color = BrandMuted)
                            Text(formatRupee(loan.principalAmount), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = BrandInk)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Paid", style = MaterialTheme.typography.labelSmall, color = BrandOk)
                            Text(formatRupee(loan.totalPaid), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = BrandOk)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Balance Due", style = MaterialTheme.typography.labelSmall, color = BrandErr)
                            Text(formatRupee(loan.outstandingBalance), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = BrandErr)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Payment Ledger History (${loanRepayments.size} Entries)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandInk
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (loanRepayments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = BrandLine, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No repayments recorded yet for this loan.",
                                style = MaterialTheme.typography.bodySmall,
                                color = BrandMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(loanRepayments) { item ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, BrandLine),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(BrandOkLight),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = BrandOk, modifier = Modifier.size(20.dp))
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Receipt #${item.receiptNo}",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = BrandInk
                                            )
                                            Text(
                                                text = "${item.paymentDate} • ${item.paymentMode}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = BrandMuted
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = formatRupee(item.amountPaid),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandOk
                                        )
                                        Text(
                                            text = "Bal: ${formatRupee(item.outstandingBalance)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = BrandMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Close Passbook")
                }
            }
        }
    }
}
