package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LoanEntity
import com.example.data.model.MemberEntity
import com.example.ui.components.LoanTrackingCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.UpcomingRemindersBanner
import com.example.ui.components.formatRupee
import com.example.ui.components.getUpcomingLoansWithinDays
import com.example.ui.theme.*
import com.example.ui.viewmodel.CoopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoansScreen(
    viewModel: CoopViewModel,
    preselectedMember: MemberEntity? = null,
    onNavigateToCollectionWithLoan: (LoanEntity) -> Unit
) {
    val loans by viewModel.loans.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()
    val repayments by viewModel.repayments.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var showApplyDialog by remember { mutableStateOf(preselectedMember != null) }
    var detailedLoan by remember { mutableStateOf<LoanEntity?>(null) }

    val filteredLoans = remember(loans, searchQuery, selectedFilter) {
        loans.filter { loan ->
            val matchesFilter = when (selectedFilter) {
                "Due in 3 Days" -> getUpcomingLoansWithinDays(listOf(loan), maxDays = 3).isNotEmpty()
                "Active" -> loan.status == "Active"
                "Overdue" -> loan.status == "Overdue"
                "Closed" -> loan.status == "Closed"
                else -> true
            }
            val matchesQuery = searchQuery.isBlank() ||
                    loan.loanNo.contains(searchQuery, ignoreCase = true) ||
                    loan.memberName.contains(searchQuery, ignoreCase = true) ||
                    loan.memberNo.contains(searchQuery, ignoreCase = true) ||
                    loan.loanType.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Loan Portfolio",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "${filteredLoans.size} Loans in register",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }

                    Button(
                        onClick = { showApplyDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("apply_loan_button")
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Apply / Disburse")
                    }
                }
            }

            // 3-Day Upcoming Loan Payment Deadline Reminder System
            item {
                UpcomingRemindersBanner(
                    loans = loans,
                    onPayLoan = { onNavigateToCollectionWithLoan(it) }
                )
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("loan_search_input"),
                    placeholder = { Text("Search by Loan No, Member Name, Type...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate500) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Navy800,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    ),
                    singleLine = true
                )
            }

            // Filter Chips
            item {
                val filters = listOf("All", "Due in 3 Days", "Active", "Overdue", "Closed")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filters) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(if (filter == "Due in 3 Days") "🔔 $filter" else filter) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (filter == "Due in 3 Days") Color(0xFFEA580C) else Navy800,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            if (filteredLoans.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate100)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = Slate400, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No loans found", fontWeight = FontWeight.Bold, color = Slate700)
                            Text("No records match the current filter.", style = MaterialTheme.typography.bodySmall, color = Slate500)
                        }
                    }
                }
            } else {
                items(filteredLoans, key = { it.loanId }) { loan ->
                    LoanTrackingCard(
                        loan = loan,
                        onPayEmiClick = { onNavigateToCollectionWithLoan(loan) },
                        onViewStatementClick = { detailedLoan = loan }
                    )
                }
            }
        }
    }

    // Apply Loan Dialog
    if (showApplyDialog) {
        ApplyLoanDialog(
            members = members.filter { it.approvalStatus == "Approved" },
            initialMember = preselectedMember,
            calculateEmi = { p, r, t -> viewModel.calculateEmi(p, r, t) },
            onDismiss = { showApplyDialog = false },
            onApply = { member, type, principal, rate, tenure, remarks, g1Name, g1Mobile, g2Name, g2Mobile ->
                viewModel.applyLoan(member, type, principal, rate, tenure, remarks, g1Name, g1Mobile, g2Name, g2Mobile)
                showApplyDialog = false
            }
        )
    }

    // Loan Detail Dialog
    detailedLoan?.let { loan ->
        val loanRepayments = repayments.filter { it.loanId == loan.loanId }
        LoanDetailDialog(
            loan = loan,
            repayments = loanRepayments,
            onDismiss = { detailedLoan = null },
            onCollect = {
                detailedLoan = null
                onNavigateToCollectionWithLoan(loan)
            }
        )
    }
}

@Composable
fun LoanCard(
    loan: LoanEntity,
    onClick: () -> Unit,
    onQuickPay: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (loan.status == "Overdue") Red100 else if (loan.status == "Closed") Slate100 else Green100
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (loan.status == "Overdue") Icons.Default.Warning else Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = if (loan.status == "Overdue") Red700 else if (loan.status == "Closed") Slate600 else Green700,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = loan.memberName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "${loan.loanNo} • ${loan.loanType}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }
                }
                StatusBadge(status = loan.status)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Slate100)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Principal", style = MaterialTheme.typography.bodySmall, color = Slate500)
                    Text(formatRupee(loan.principalAmount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("EMI (${loan.interestRate}%)", style = MaterialTheme.typography.bodySmall, color = Slate500)
                    Text(formatRupee(loan.emiAmount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Slate700)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Outstanding", style = MaterialTheme.typography.bodySmall, color = Slate500)
                    Text(
                        formatRupee(loan.outstandingBalance),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (loan.status == "Overdue") Red700 else Navy800
                    )
                }
            }

            if (loan.status != "Closed") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (loan.daysOverdue > 0) {
                        Text(
                            text = "⚠ ${loan.daysOverdue} days overdue",
                            color = Red700,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "Next Due: ${loan.dueDate}",
                            color = Slate500,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    OutlinedButton(
                        onClick = onQuickPay,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Navy800)
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Collect EMI", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun ApplyLoanDialog(
    members: List<MemberEntity>,
    initialMember: MemberEntity?,
    calculateEmi: (Double, Double, Int) -> Double,
    onDismiss: () -> Unit,
    onApply: (
        member: MemberEntity,
        loanType: String,
        principal: Double,
        rate: Double,
        tenureMonths: Int,
        remarks: String,
        g1Name: String,
        g1Mobile: String,
        g2Name: String,
        g2Mobile: String
    ) -> Unit
) {
    var selectedMember by remember { mutableStateOf(initialMember ?: members.firstOrNull()) }
    var loanType by remember { mutableStateOf("Personal Loan") }
    var principalStr by remember { mutableStateOf("50000") }
    var interestRateStr by remember { mutableStateOf("11.5") }
    var tenureStr by remember { mutableStateOf("12") }
    var remarks by remember { mutableStateOf("") }
    var g1Name by remember { mutableStateOf("") }
    var g1Mobile by remember { mutableStateOf("") }
    var g2Name by remember { mutableStateOf("") }
    var g2Mobile by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val principal = principalStr.toDoubleOrNull() ?: 0.0
    val interestRate = interestRateStr.toDoubleOrNull() ?: 0.0
    val tenure = tenureStr.toIntOrNull() ?: 0
    val estimatedEmi = remember(principal, interestRate, tenure) {
        calculateEmi(principal, interestRate, tenure)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Disburse Society Loan",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    errorMessage?.let {
                        Text(it, color = Red700, style = MaterialTheme.typography.bodySmall)
                    }

                    // Member Selection
                    Text("Select Society Member *", style = MaterialTheme.typography.labelMedium, color = Slate700)
                    var memberDropdownOpen by remember { mutableStateOf(false) }
                    Box {
                        OutlinedTextField(
                            value = selectedMember?.let { "${it.fullName} (${it.memberNo})" } ?: "Select Member",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().clickable { memberDropdownOpen = true },
                            trailingIcon = {
                                IconButton(onClick = { memberDropdownOpen = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = memberDropdownOpen,
                            onDismissRequest = { memberDropdownOpen = false }
                        ) {
                            members.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text("${m.fullName} (${m.memberNo} - ${m.mobile})") },
                                    onClick = {
                                        selectedMember = m
                                        memberDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }

                    // Loan Type
                    Text("Loan Scheme / Purpose", style = MaterialTheme.typography.labelMedium, color = Slate700)
                    val types = listOf("Personal Loan", "Agriculture Loan", "Gold Loan", "Business Loan", "Vehicle Loan", "Emergency Loan")
                    var typeDropdownOpen by remember { mutableStateOf(false) }
                    Box {
                        OutlinedTextField(
                            value = loanType,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().clickable { typeDropdownOpen = true },
                            trailingIcon = {
                                IconButton(onClick = { typeDropdownOpen = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = typeDropdownOpen,
                            onDismissRequest = { typeDropdownOpen = false }
                        ) {
                            types.forEach { t ->
                                DropdownMenuItem(
                                    text = { Text(t) },
                                    onClick = {
                                        loanType = t
                                        typeDropdownOpen = false
                                        // Default rates by scheme
                                        if (t == "Agriculture Loan") interestRateStr = "8.5"
                                        else if (t == "Gold Loan") interestRateStr = "9.0"
                                        else if (t == "Business Loan") interestRateStr = "11.0"
                                        else interestRateStr = "12.0"
                                    }
                                )
                            }
                        }
                    }

                    // Principal, Rate, Tenure
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = principalStr,
                            onValueChange = { principalStr = it },
                            label = { Text("Principal (₹) *") },
                            modifier = Modifier.weight(1.5f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = interestRateStr,
                            onValueChange = { interestRateStr = it },
                            label = { Text("Rate % p.a.") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = tenureStr,
                        onValueChange = { tenureStr = it },
                        label = { Text("Tenure (Months) *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // EMI Preview Box
                    Surface(
                        color = Amber100.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Monthly EMI:", fontWeight = FontWeight.Bold, color = Amber700)
                                Text(formatRupee(estimatedEmi), fontWeight = FontWeight.Bold, color = Amber700)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Repayable:", style = MaterialTheme.typography.bodySmall, color = Slate700)
                                Text(formatRupee(estimatedEmi * tenure), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Slate700)
                            }
                        }
                    }

                    Text("Guarantor Details (જામીનદાર વિગત)", fontWeight = FontWeight.Bold, color = Navy800, modifier = Modifier.padding(top = 6.dp))
                    OutlinedTextField(
                        value = g1Name,
                        onValueChange = { g1Name = it },
                        label = { Text("Guarantor 1 Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = g1Mobile,
                        onValueChange = { g1Mobile = it },
                        label = { Text("Guarantor 1 Mobile") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text("Remarks / Purpose Note") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (selectedMember == null) {
                            errorMessage = "Please select an approved member."
                        } else if (principal <= 0 || tenure <= 0) {
                            errorMessage = "Please enter valid Principal and Tenure."
                        } else {
                            onApply(selectedMember!!, loanType, principal, interestRate, tenure, remarks, g1Name, g1Mobile, g2Name, g2Mobile)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy800)
                ) {
                    Text("Approve & Disburse Loan")
                }
            }
        }
    }
}

@Composable
fun LoanDetailDialog(
    loan: LoanEntity,
    repayments: List<com.example.data.model.RepaymentEntity>,
    onDismiss: () -> Unit,
    onCollect: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = loan.loanNo,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "${loan.memberName} (${loan.memberNo})",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Status", style = MaterialTheme.typography.bodySmall, color = Slate500)
                        StatusBadge(status = loan.status)
                    }

                    InfoRow("Loan Scheme", loan.loanType)
                    InfoRow("Principal Disbursed", formatRupee(loan.principalAmount))
                    InfoRow("Interest Rate", "${loan.interestRate}% p.a.")
                    InfoRow("Monthly EMI", formatRupee(loan.emiAmount))
                    InfoRow("Tenure", "${loan.tenureMonths} Months")
                    InfoRow("Disbursed Date", loan.disbursedDate)
                    InfoRow("Next Due Date", loan.dueDate)
                    InfoRow("Total Repaid So Far", formatRupee(loan.totalPaid))
                    InfoRow("Outstanding Balance", formatRupee(loan.outstandingBalance))

                    if (loan.daysOverdue > 0) {
                        Surface(
                            color = Red100,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Overdue Alert: ${loan.daysOverdue} days past due date.",
                                color = Red700,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    if (loan.guarantor1Name.isNotBlank()) {
                        Text("Guarantor", fontWeight = FontWeight.Bold, color = Navy800, modifier = Modifier.padding(top = 6.dp))
                        InfoRow("Guarantor 1", "${loan.guarantor1Name} (${loan.guarantor1Mobile})")
                    }

                    Text("Repayment Ledger (${repayments.size})", fontWeight = FontWeight.Bold, color = Navy800, modifier = Modifier.padding(top = 6.dp))
                    if (repayments.isEmpty()) {
                        Text("No repayments collected yet.", style = MaterialTheme.typography.bodySmall, color = Slate500)
                    } else {
                        repayments.forEach { rep ->
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Slate50),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(rep.receiptNo, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                        Text("${rep.paymentDate} • ${rep.paymentMode}", style = MaterialTheme.typography.bodySmall, color = Slate500)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(formatRupee(rep.amountPaid), fontWeight = FontWeight.Bold, color = Green700)
                                        Text("Bal: ${formatRupee(rep.outstandingBalance)}", style = MaterialTheme.typography.bodySmall, color = Slate500)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (loan.status != "Closed") {
                    Button(
                        onClick = onCollect,
                        colors = ButtonDefaults.buttonColors(containerColor = Green700),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Collect Payment for this Loan")
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Slate500)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = Slate800)
    }
}
