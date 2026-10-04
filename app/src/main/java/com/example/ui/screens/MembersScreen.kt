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
import com.example.data.model.MemberEntity
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatRupee
import com.example.ui.theme.*
import com.example.ui.viewmodel.CoopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersScreen(
    viewModel: CoopViewModel,
    onNavigateToApplyLoan: (MemberEntity) -> Unit
) {
    val members by viewModel.members.collectAsStateWithLifecycle()
    val loans by viewModel.loans.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    var detailedMember by remember { mutableStateOf<MemberEntity?>(null) }

    val filteredMembers = remember(members, searchQuery, selectedFilter) {
        members.filter { member ->
            val matchesFilter = when (selectedFilter) {
                "Approved" -> member.approvalStatus == "Approved"
                "Pending" -> member.approvalStatus == "Pending"
                "Rejected" -> member.approvalStatus == "Rejected"
                else -> true
            }
            val matchesQuery = searchQuery.isBlank() ||
                    member.fullName.contains(searchQuery, ignoreCase = true) ||
                    member.memberNo.contains(searchQuery, ignoreCase = true) ||
                    member.mobile.contains(searchQuery) ||
                    member.aadharNo.contains(searchQuery)
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
                            text = "Member Registry",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "${filteredMembers.size} Society Members listed",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_member_button")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Member")
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_search_input"),
                    placeholder = { Text("Search by Name, Mobile, Member No...") },
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
                val filters = listOf("All", "Approved", "Pending", "Rejected")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filters) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Navy800,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            if (filteredMembers.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate100)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.SearchOff, contentDescription = null, tint = Slate400, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No members found", fontWeight = FontWeight.Bold, color = Slate700)
                            Text("Try adjusting search or filters.", style = MaterialTheme.typography.bodySmall, color = Slate500)
                        }
                    }
                }
            } else {
                items(filteredMembers, key = { it.memberId }) { member ->
                    val memberLoans = loans.filter { it.memberId == member.memberId }
                    val activeBorrowing = memberLoans.filter { it.status != "Closed" }.sumOf { it.outstandingBalance }

                    MemberCard(
                        member = member,
                        activeLoanCount = memberLoans.count { it.status != "Closed" },
                        activeBorrowing = activeBorrowing,
                        onClick = { detailedMember = member }
                    )
                }
            }
        }
    }

    // Add Member Dialog
    if (showAddDialog) {
        AddMemberDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { fullName, mobile, email, address, aadhar, pan, father, gender, occ, bank, acc, ifsc, branch ->
                viewModel.addMember(fullName, mobile, email, address, aadhar, pan, father, gender, occ, bank, acc, ifsc, branch)
                showAddDialog = false
            }
        )
    }

    // Member Details Dialog
    detailedMember?.let { member ->
        val memberLoans = loans.filter { it.memberId == member.memberId }

        MemberDetailDialog(
            member = member,
            loans = memberLoans,
            isAdmin = currentUser.role.contains("Admin"),
            onDismiss = { detailedMember = null },
            onApprove = {
                viewModel.approveMember(member.memberId)
                detailedMember = null
            },
            onReject = { reason ->
                viewModel.rejectMember(member.memberId, reason)
                detailedMember = null
            },
            onApplyLoan = {
                detailedMember = null
                onNavigateToApplyLoan(member)
            }
        )
    }
}

@Composable
fun MemberCard(
    member: MemberEntity,
    activeLoanCount: Int,
    activeBorrowing: Double,
    onClick: () -> Unit
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
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Navy100),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = member.fullName.take(2).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = Navy800,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = member.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "${member.memberNo} • ${member.mobile}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }
                }
                StatusBadge(status = member.approvalStatus)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Slate100)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Occupation", style = MaterialTheme.typography.bodySmall, color = Slate500)
                    Text(member.occupation, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Slate800)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Active Loans", style = MaterialTheme.typography.bodySmall, color = Slate500)
                    Text(
                        text = if (activeLoanCount > 0) "$activeLoanCount (${formatRupee(activeBorrowing)})" else "None",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (activeLoanCount > 0) Navy800 else Slate600
                    )
                }
            }
        }
    }
}

@Composable
fun MemberDetailDialog(
    member: MemberEntity,
    loans: List<com.example.data.model.LoanEntity>,
    isAdmin: Boolean,
    onDismiss: () -> Unit,
    onApprove: () -> Unit,
    onReject: (String) -> Unit,
    onApplyLoan: () -> Unit
) {
    var showRejectInput by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }

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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = member.fullName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "Member ID: ${member.memberNo} • Joined ${member.joinDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // KYC Info Section
                    SectionHeader(title = "KYC & Identity")
                    InfoRow("Mobile Number", member.mobile)
                    InfoRow("Aadhar Number", if (member.aadharNo.isNotBlank()) member.aadharNo else "Not provided")
                    InfoRow("PAN Number", if (member.panNo.isNotBlank()) member.panNo else "Not provided")
                    InfoRow("Father/Spouse", if (member.fatherSpouseName.isNotBlank()) member.fatherSpouseName else "N/A")
                    InfoRow("Gender / Occupation", "${member.gender} • ${member.occupation}")
                    InfoRow("Address", if (member.address.isNotBlank()) member.address else "Rajkot, Gujarat")

                    // Bank Info Section
                    SectionHeader(title = "Bank Account Details")
                    InfoRow("Bank Name", if (member.bankName.isNotBlank()) member.bankName else "N/A")
                    InfoRow("Account Number", if (member.accountNumber.isNotBlank()) member.accountNumber else "N/A")
                    InfoRow("IFSC Code", if (member.ifscCode.isNotBlank()) member.ifscCode else "N/A")
                    InfoRow("Branch", if (member.bankBranch.isNotBlank()) member.bankBranch else "N/A")

                    // Loans Section
                    SectionHeader(title = "Loan History (${loans.size})")
                    if (loans.isEmpty()) {
                        Text("No active or past loans for this member.", style = MaterialTheme.typography.bodySmall, color = Slate500)
                    } else {
                        loans.forEach { loan ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Slate50),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(loan.loanNo, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text("${loan.loanType} • EMI: ${formatRupee(loan.emiAmount)}", style = MaterialTheme.typography.bodySmall, color = Slate600)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        StatusBadge(status = loan.status)
                                        Text("Bal: ${formatRupee(loan.outstandingBalance)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }

                    // Approval Status details
                    if (member.approvalStatus == "Rejected" && !member.rejectReason.isNullOrBlank()) {
                        Surface(
                            color = Red100,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Rejection Reason: ${member.rejectReason}",
                                color = Red700,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Actions
                if (member.approvalStatus == "Pending" && isAdmin) {
                    if (showRejectInput) {
                        Column {
                            OutlinedTextField(
                                value = rejectReason,
                                onValueChange = { rejectReason = it },
                                label = { Text("Reason for Rejection") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { showRejectInput = false }, modifier = Modifier.weight(1f)) {
                                    Text("Cancel")
                                }
                                Button(
                                    onClick = { onReject(rejectReason.ifBlank { "Incomplete KYC document" }) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Red700),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Confirm Reject")
                                }
                            }
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { showRejectInput = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Red700),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reject KYC")
                            }
                            Button(
                                onClick = onApprove,
                                colors = ButtonDefaults.buttonColors(containerColor = Green700),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Approve Member")
                            }
                        }
                    }
                } else if (member.approvalStatus == "Approved") {
                    Button(
                        onClick = onApplyLoan,
                        colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Disburse New Loan to Member")
                    }
                }
            }
        }
    }
}

@Composable
fun AddMemberDialog(
    onDismiss: () -> Unit,
    onAdd: (
        fullName: String,
        mobile: String,
        email: String,
        address: String,
        aadhar: String,
        pan: String,
        father: String,
        gender: String,
        occupation: String,
        bankName: String,
        accountNo: String,
        ifsc: String,
        branch: String
    ) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var aadhar by remember { mutableStateOf("") }
    var pan by remember { mutableStateOf("") }
    var father by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var occupation by remember { mutableStateOf("Agriculture") }
    var bankName by remember { mutableStateOf("") }
    var accountNo by remember { mutableStateOf("") }
    var ifsc by remember { mutableStateOf("") }
    var branch by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
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
                        text = "Register New Member",
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

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) mobile = it },
                        label = { Text("Mobile Number (10 digits) *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = aadhar,
                        onValueChange = { aadhar = it },
                        label = { Text("Aadhar Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = pan,
                        onValueChange = { pan = it.uppercase() },
                        label = { Text("PAN Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = father,
                        onValueChange = { father = it },
                        label = { Text("Father / Spouse Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = occupation,
                        onValueChange = { occupation = it },
                        label = { Text("Occupation / Source of Income") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Residential Address") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    SectionHeader(title = "Bank Account Details")
                    OutlinedTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = { Text("Bank Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = accountNo,
                        onValueChange = { accountNo = it },
                        label = { Text("Bank Account Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = ifsc,
                        onValueChange = { ifsc = it.uppercase() },
                        label = { Text("IFSC Code") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = branch,
                        onValueChange = { branch = it },
                        label = { Text("Bank Branch") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (fullName.isBlank() || mobile.length < 10) {
                            errorMessage = "Please enter valid Full Name and 10-digit Mobile Number."
                        } else {
                            onAdd(fullName, mobile, email, address, aadhar, pan, father, gender, occupation, bankName, accountNo, ifsc, branch)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy800)
                ) {
                    Text("Submit for Approval")
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Navy800,
        modifier = Modifier.padding(top = 8.dp)
    )
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
