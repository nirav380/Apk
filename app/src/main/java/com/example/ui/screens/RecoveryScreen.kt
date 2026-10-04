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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatRupee
import com.example.ui.theme.*
import com.example.ui.viewmodel.CoopViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RecoveryScreen(
    viewModel: CoopViewModel
) {
    val loans by viewModel.loans.collectAsStateWithLifecycle()
    val followUps by viewModel.followUps.collectAsStateWithLifecycle()
    val ptps by viewModel.ptps.collectAsStateWithLifecycle()
    val notices by viewModel.demandNotices.collectAsStateWithLifecycle()
    val s101Cases by viewModel.section101Cases.collectAsStateWithLifecycle()
    val summary by viewModel.dashboardSummary.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Defaulters", "Follow-Ups", "PTP Tracker", "Legal & S.101")

    val overdueLoans = remember(loans) {
        loans.filter { it.status == "Overdue" || it.daysOverdue > 0 }
    }

    var activeDialogLoan by remember { mutableStateOf<LoanEntity?>(null) }
    var dialogType by remember { mutableStateOf<String?>(null) } // "FollowUp", "PTP", "Notice", "Section101"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "NPA & Recovery Management",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "Overdue tracking, follow-ups & Gujarat Co-Op Section 101",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
            }
        }

        // Overdue & NPA Metrics Header Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Navy800),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Delinquency Classification",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Red700,
                            modifier = Modifier.padding(2.dp)
                        ) {
                            Text(
                                text = "${overdueLoans.size} ACCOUNTS OVERDUE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricBlock(label = "SMA-0 (1-30d)", count = summary.sma0Count, color = Amber100)
                        MetricBlock(label = "SMA-1 (31-60d)", count = summary.sma1Count, color = Amber600)
                        MetricBlock(label = "SMA-2 (61-90d)", count = summary.sma2Count, color = Red600)
                        MetricBlock(label = "NPA (>90d)", count = summary.totalNpaCount, color = Red100)
                    }
                }
            }
        }

        // Tab Selector
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Navy800,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }
        }

        // Tab 0: Defaulter Accounts
        if (selectedTab == 0) {
            if (overdueLoans.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Slate100)) {
                        Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No overdue accounts! All loan EMIs are up to date.", color = Slate600)
                        }
                    }
                }
            } else {
                items(overdueLoans, key = { it.loanId }) { loan ->
                    val bucket = if (loan.daysOverdue > 90) "NPA" else if (loan.daysOverdue > 60) "SMA-2" else if (loan.daysOverdue > 30) "SMA-1" else "SMA-0"
                    DefaulterCard(
                        loan = loan,
                        bucket = bucket,
                        onLogFollowUp = {
                            activeDialogLoan = loan
                            dialogType = "FollowUp"
                        },
                        onRecordPtp = {
                            activeDialogLoan = loan
                            dialogType = "PTP"
                        },
                        onIssueNotice = {
                            activeDialogLoan = loan
                            dialogType = "Notice"
                        },
                        onFileSection101 = {
                            activeDialogLoan = loan
                            dialogType = "Section101"
                        }
                    )
                }
            }
        }

        // Tab 1: Follow-Ups
        if (selectedTab == 1) {
            if (followUps.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Slate100)) {
                        Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No recovery follow-ups recorded yet.", color = Slate600)
                        }
                    }
                }
            } else {
                items(followUps, key = { it.followUpId }) { f ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PhoneCallback, contentDescription = null, tint = Navy800, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(f.memberName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                }
                                Surface(shape = RoundedCornerShape(8.dp), color = Navy100) {
                                    Text(f.followUpType, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Navy800, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Loan: ${f.loanNo} • Date: ${f.followUpDate} • Staff: ${f.staffOrAgent}", style = MaterialTheme.typography.bodySmall, color = Slate500)
                            Text("Contacted: ${f.contactPerson}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Slate700)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(f.responseSummary, style = MaterialTheme.typography.bodyMedium, color = Slate800)
                            if (f.remarks.isNotBlank()) {
                                Text("Note: ${f.remarks}", style = MaterialTheme.typography.bodySmall, color = Slate500)
                            }
                        }
                    }
                }
            }
        }

        // Tab 2: PTP Tracker
        if (selectedTab == 2) {
            if (ptps.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Slate100)) {
                        Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No Promise To Pay commitments active.", color = Slate600)
                        }
                    }
                }
            } else {
                items(ptps, key = { it.ptpId }) { ptp ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(ptp.memberName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("${ptp.loanNo} • Contact: ${ptp.mobile}", style = MaterialTheme.typography.bodySmall, color = Slate500)
                                }
                                StatusBadge(status = ptp.status)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Slate100)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Promised Amount", style = MaterialTheme.typography.bodySmall, color = Slate500)
                                    Text(formatRupee(ptp.promisedAmount), fontWeight = FontWeight.Bold, color = Green700)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Commitment Date", style = MaterialTheme.typography.bodySmall, color = Slate500)
                                    Text(ptp.promisedDate, fontWeight = FontWeight.SemiBold, color = Amber700)
                                }
                            }
                            if (ptp.status == "Pending") {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { viewModel.updatePtpStatus(ptp.ptpId, "Kept") },
                                        colors = ButtonDefaults.buttonColors(containerColor = Green700),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        Text("Mark Kept", style = MaterialTheme.typography.labelSmall)
                                    }
                                    OutlinedButton(
                                        onClick = { viewModel.updatePtpStatus(ptp.ptpId, "Broken") },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(vertical = 4.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Red700)
                                    ) {
                                        Text("Mark Broken", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Tab 3: Legal & Section 101
        if (selectedTab == 3) {
            item {
                Text("Demand Notices Issued (${notices.size})", fontWeight = FontWeight.Bold, color = Navy800)
            }
            if (notices.isEmpty()) {
                item {
                    Text("No demand notices generated yet.", style = MaterialTheme.typography.bodySmall, color = Slate500)
                }
            } else {
                items(notices, key = { it.noticeId }) { notice ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(notice.noticeNo, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text("${notice.memberName} • ${notice.loanNo}", style = MaterialTheme.typography.bodySmall, color = Slate600)
                                }
                                StatusBadge(status = notice.noticeStatus)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Notice Type: ${notice.noticeType} • Date: ${notice.noticeDate}", style = MaterialTheme.typography.bodySmall, color = Slate500)
                            Text("Total Demanded: ${formatRupee(notice.totalDemanded)} (Incl charges & penal interest)", fontWeight = FontWeight.SemiBold, color = Red700, style = MaterialTheme.typography.bodySmall)
                            Text("Advocate: ${notice.advocateName}", style = MaterialTheme.typography.bodySmall, color = Slate500)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text("Section 101 Co-Operative Recovery Cases (${s101Cases.size})", fontWeight = FontWeight.Bold, color = Navy800)
            }
            if (s101Cases.isEmpty()) {
                item {
                    Text("No Section 101 cases filed.", style = MaterialTheme.typography.bodySmall, color = Slate500)
                }
            } else {
                items(s101Cases, key = { it.caseId }) { c ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(c.caseNo, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text("${c.memberName} • ${c.loanNo}", style = MaterialTheme.typography.bodySmall, color = Slate600)
                                }
                                StatusBadge(status = c.caseStatus)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Court: ${c.authorityCourt} • Filed: ${c.filingDate}", style = MaterialTheme.typography.bodySmall, color = Slate500)
                            Text("Claim Amount: ${formatRupee(c.claimAmount)}", fontWeight = FontWeight.Bold, color = Red700, style = MaterialTheme.typography.bodySmall)
                            c.hearingDate?.let {
                                Text("Next Hearing Date: $it", fontWeight = FontWeight.SemiBold, color = Amber700, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog for Follow-up / PTP / Notice / Section 101
    activeDialogLoan?.let { loan ->
        when (dialogType) {
            "FollowUp" -> {
                LogFollowUpDialog(
                    loan = loan,
                    onDismiss = { activeDialogLoan = null },
                    onSubmit = { type, contact, summary, remarks ->
                        viewModel.logRecoveryFollowUp(loan, type, contact, summary, remarks)
                        activeDialogLoan = null
                    }
                )
            }
            "PTP" -> {
                RecordPtpDialog(
                    loan = loan,
                    onDismiss = { activeDialogLoan = null },
                    onSubmit = { amount, date, remarks ->
                        viewModel.recordPtp(loan, amount, date, remarks)
                        activeDialogLoan = null
                    }
                )
            }
            "Notice" -> {
                IssueNoticeDialog(
                    loan = loan,
                    onDismiss = { activeDialogLoan = null },
                    onSubmit = { noticeType, advocate ->
                        viewModel.issueDemandNotice(loan, noticeType, advocate)
                        activeDialogLoan = null
                    }
                )
            }
            "Section101" -> {
                FileSection101Dialog(
                    loan = loan,
                    onDismiss = { activeDialogLoan = null },
                    onSubmit = { court, claim, remarks ->
                        viewModel.fileSection101Case(loan, court, claim, remarks)
                        activeDialogLoan = null
                    }
                )
            }
        }
    }
}

@Composable
private fun MetricBlock(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "$count", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Slate400)
    }
}

@Composable
fun DefaulterCard(
    loan: LoanEntity,
    bucket: String,
    onLogFollowUp: () -> Unit,
    onRecordPtp: () -> Unit,
    onIssueNotice: () -> Unit,
    onFileSection101: () -> Unit
) {
    val penalRate = 2.0 // 2% per month default
    val penalAmount = loan.outstandingBalance * (penalRate / 100.0)
    val totalDemand = loan.outstandingBalance + penalAmount

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
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
                            .background(Red100),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${loan.daysOverdue}d",
                            fontWeight = FontWeight.Bold,
                            color = Red700,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(loan.memberName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Slate900)
                        Text("${loan.loanNo} • Mobile: ${loan.mobile}", style = MaterialTheme.typography.bodySmall, color = Slate500)
                    }
                }
                StatusBadge(status = bucket)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Slate100)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Overdue Principal", style = MaterialTheme.typography.bodySmall, color = Slate500)
                    Text(formatRupee(loan.outstandingBalance), fontWeight = FontWeight.Bold, color = Red700)
                }
                Column {
                    Text("Penal Int. (2%)", style = MaterialTheme.typography.bodySmall, color = Slate500)
                    Text(formatRupee(penalAmount), fontWeight = FontWeight.SemiBold, color = Slate700)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Demand", style = MaterialTheme.typography.bodySmall, color = Slate500)
                    Text(formatRupee(totalDemand), fontWeight = FontWeight.Bold, color = Slate900)
                }
            }

            if (loan.guarantor1Name.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Guarantor: ${loan.guarantor1Name} (${loan.guarantor1Mobile})",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onLogFollowUp,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Text("Follow-Up", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick = onRecordPtp,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Text("Commit PTP", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick = onIssueNotice,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Amber700)
                ) {
                    Text("Notice", style = MaterialTheme.typography.labelSmall)
                }
                Button(
                    onClick = onFileSection101,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Red700)
                ) {
                    Text("Sec 101", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun LogFollowUpDialog(
    loan: LoanEntity,
    onDismiss: () -> Unit,
    onSubmit: (type: String, contact: String, summary: String, remarks: String) -> Unit
) {
    var type by remember { mutableStateOf("Phone Call") }
    var contact by remember { mutableStateOf("Borrower (${loan.memberName})") }
    var summary by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Log Recovery Follow-Up", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${loan.memberName} • ${loan.loanNo}", style = MaterialTheme.typography.bodySmall, color = Slate600)

                val types = listOf("Phone Call", "Field Visit", "Demand Notice", "Office Meeting")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    types.forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("Person Contacted") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("Response Summary *") },
                    placeholder = { Text("What did the borrower/guarantor say?") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Next Action / Remarks") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = {
                            if (summary.isNotBlank()) onSubmit(type, contact, summary, remarks)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Navy800)
                    ) { Text("Save Log") }
                }
            }
        }
    }
}

@Composable
fun RecordPtpDialog(
    loan: LoanEntity,
    onDismiss: () -> Unit,
    onSubmit: (amount: Double, date: String, remarks: String) -> Unit
) {
    var amountStr by remember { mutableStateOf(loan.emiAmount.toString()) }
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }
    var dateStr by remember { mutableStateOf(sdf.format(cal.time)) }
    var remarks by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Record Promise To Pay (PTP)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${loan.memberName} • ${loan.loanNo}", style = MaterialTheme.typography.bodySmall, color = Slate600)

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Promised Amount (₹)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dateStr,
                    onValueChange = { dateStr = it },
                    label = { Text("Promised Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Borrower commitment remarks") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = {
                            val amt = amountStr.toDoubleOrNull() ?: 0.0
                            if (amt > 0) onSubmit(amt, dateStr, remarks)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Green700)
                    ) { Text("Record PTP") }
                }
            }
        }
    }
}

@Composable
fun IssueNoticeDialog(
    loan: LoanEntity,
    onDismiss: () -> Unit,
    onSubmit: (noticeType: String, advocate: String) -> Unit
) {
    var noticeType by remember { mutableStateOf("Form-1 Reminder") }
    var advocate by remember { mutableStateOf("S. M. Trivedi, High Court Advocate") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Generate Legal Demand Notice", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Borrower: ${loan.memberName} • Balance: ${formatRupee(loan.outstandingBalance)}", style = MaterialTheme.typography.bodySmall, color = Slate600)

                val noticeTypes = listOf("Form-1 Reminder", "Form-2 Final Notice", "Section 101 Advocate Notice")
                Column {
                    noticeTypes.forEach { nt ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { noticeType = nt }.padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = noticeType == nt, onClick = { noticeType = nt })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(nt, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                OutlinedTextField(
                    value = advocate,
                    onValueChange = { advocate = it },
                    label = { Text("Society Legal Advocate") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = { onSubmit(noticeType, advocate) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Amber700)
                    ) { Text("Issue Notice") }
                }
            }
        }
    }
}

@Composable
fun FileSection101Dialog(
    loan: LoanEntity,
    onDismiss: () -> Unit,
    onSubmit: (court: String, claim: Double, remarks: String) -> Unit
) {
    var court by remember { mutableStateOf("Board of Nominees / Registrar Court, Rajkot") }
    var claimStr by remember { mutableStateOf((loan.outstandingBalance + 2500).toString()) }
    var remarks by remember { mutableStateOf("Recovery Certificate Application under Gujarat Co-operative Societies Act Section 101.") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("File Section 101 Co-Op Case", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Red700)
                Text("Statutory recovery suit against borrower & guarantors", style = MaterialTheme.typography.bodySmall, color = Slate600)

                OutlinedTextField(
                    value = court,
                    onValueChange = { court = it },
                    label = { Text("Authority / Court") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = claimStr,
                    onValueChange = { claimStr = it },
                    label = { Text("Total Claim Amount (₹)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Legal Grounds / Case Notes") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = {
                            val claim = claimStr.toDoubleOrNull() ?: loan.outstandingBalance
                            onSubmit(court, claim, remarks)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Red700)
                    ) { Text("Submit Case") }
                }
            }
        }
    }
}
