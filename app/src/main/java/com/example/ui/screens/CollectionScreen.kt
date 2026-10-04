package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LoanEntity
import com.example.data.model.RepaymentEntity
import com.example.ui.components.ReceiptDialog
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatRupee
import com.example.ui.theme.*
import com.example.ui.viewmodel.CoopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen(
    viewModel: CoopViewModel,
    preselectedLoan: LoanEntity? = null
) {
    val loans by viewModel.loans.collectAsStateWithLifecycle()
    val repayments by viewModel.repayments.collectAsStateWithLifecycle()
    val lastReceipt by viewModel.lastGeneratedReceipt.collectAsStateWithLifecycle()

    val activeLoans = remember(loans) {
        loans.filter { it.status != "Closed" && it.outstandingBalance > 0 }
    }

    var selectedLoan by remember { mutableStateOf(preselectedLoan ?: activeLoans.firstOrNull()) }
    var amountStr by remember { mutableStateOf(selectedLoan?.emiAmount?.toString() ?: "5000") }
    var paymentMode by remember { mutableStateOf("Cash") }
    var remarks by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showReceiptForRepayment by remember { mutableStateOf<RepaymentEntity?>(null) }

    LaunchedEffect(selectedLoan) {
        selectedLoan?.let {
            amountStr = it.emiAmount.toString()
        }
    }

    LaunchedEffect(lastReceipt) {
        if (lastReceipt != null) {
            showReceiptForRepayment = lastReceipt
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Daily Collection Counter",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "Fast EMI & Principal repayment receipting",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
            }
        }

        // Collection Form Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "New Collection Entry",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Navy800
                    )

                    errorMessage?.let {
                        Text(it, color = Red700, style = MaterialTheme.typography.bodySmall)
                    }

                    // Loan selector
                    var loanDropdownOpen by remember { mutableStateOf(false) }
                    Text("Select Borrower / Loan Account *", style = MaterialTheme.typography.labelMedium, color = Slate700)
                    Box {
                        OutlinedTextField(
                            value = selectedLoan?.let { "${it.memberName} - ${it.loanNo} (Bal: ${formatRupee(it.outstandingBalance)})" } ?: "Select Borrower",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().clickable { loanDropdownOpen = true },
                            trailingIcon = {
                                IconButton(onClick = { loanDropdownOpen = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = loanDropdownOpen,
                            onDismissRequest = { loanDropdownOpen = false }
                        ) {
                            activeLoans.forEach { l ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text("${l.memberName} • ${l.loanNo}", fontWeight = FontWeight.Bold)
                                            Text("Bal: ${formatRupee(l.outstandingBalance)} • EMI: ${formatRupee(l.emiAmount)}", style = MaterialTheme.typography.bodySmall, color = Slate600)
                                        }
                                    },
                                    onClick = {
                                        selectedLoan = l
                                        loanDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }

                    selectedLoan?.let { l ->
                        Surface(
                            color = Navy100.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Member No:", style = MaterialTheme.typography.bodySmall, color = Slate600)
                                    Text(l.memberNo, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Outstanding Balance:", style = MaterialTheme.typography.bodySmall, color = Slate600)
                                    Text(formatRupee(l.outstandingBalance), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = if (l.status == "Overdue") Red700 else Navy800)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Monthly Installment (EMI):", style = MaterialTheme.typography.bodySmall, color = Slate600)
                                    Text(formatRupee(l.emiAmount), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Green700)
                                }
                            }
                        }
                    }

                    // Amount input
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Amount Collected (₹) *") },
                        modifier = Modifier.fillMaxWidth().testTag("collection_amount_input"),
                        singleLine = true,
                        leadingIcon = { Text("₹", modifier = Modifier.padding(start = 12.dp), fontWeight = FontWeight.Bold) },
                        trailingIcon = {
                            selectedLoan?.let { l ->
                                TextButton(onClick = { amountStr = l.emiAmount.toString() }) {
                                    Text("Set EMI")
                                }
                            }
                        }
                    )

                    // Payment Mode Selector
                    Text("Payment Mode", style = MaterialTheme.typography.labelMedium, color = Slate700)
                    val modes = listOf("Cash", "UPI", "Cheque", "Bank Transfer")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        modes.forEach { mode ->
                            FilterChip(
                                selected = paymentMode == mode,
                                onClick = { paymentMode = mode },
                                label = { Text(mode, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Navy800,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text("Remarks / Transaction Ref / Note") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            val amount = amountStr.toDoubleOrNull() ?: 0.0
                            if (selectedLoan == null) {
                                errorMessage = "Please select an active loan."
                            } else if (amount <= 0) {
                                errorMessage = "Please enter a valid payment amount."
                            } else {
                                errorMessage = null
                                viewModel.processDailyCollection(
                                    loan = selectedLoan!!,
                                    amount = amount,
                                    paymentMode = paymentMode,
                                    remarks = remarks.ifBlank { "Daily Counter EMI Collection" }
                                )
                                remarks = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Green700),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("submit_collection_button")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Record Payment & Generate Receipt")
                    }
                }
            }
        }

        // Daily Register
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Collections Register",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "${repayments.size} receipts",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
            }
        }

        if (repayments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate100)
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No collection transactions recorded yet.", color = Slate600)
                    }
                }
            }
        } else {
            items(repayments, key = { it.repaymentId }) { rep ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showReceiptForRepayment = rep }
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
                                    .size(38.dp)
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
                                    text = "${rep.receiptNo} • ${rep.paymentMode} • ${rep.paymentDate}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate500
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = formatRupee(rep.amountPaid),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Green700
                            )
                            Text(
                                text = "Bal: ${formatRupee(rep.outstandingBalance)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate500
                            )
                        }
                    }
                }
            }
        }
    }

    // Receipt Dialog
    showReceiptForRepayment?.let { rep ->
        ReceiptDialog(
            repayment = rep,
            onDismiss = {
                showReceiptForRepayment = null
                viewModel.clearLastReceipt()
            }
        )
    }
}
