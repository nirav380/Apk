package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.network.ConnectionTestResult
import com.example.data.network.ServerConnectionConfig
import com.example.data.network.SomeeApiClient
import com.example.data.network.SyncResult
import com.example.data.repository.CoopRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt

data class UserProfile(
    val name: String,
    val role: String, // "Admin / Manager", "Recovery Officer", "Member"
    val mobile: String
)

data class DashboardSummary(
    val totalMembers: Int = 0,
    val activeMembers: Int = 0,
    val pendingMembers: Int = 0,
    val totalLoansCount: Int = 0,
    val activeLoansCount: Int = 0,
    val overdueLoansCount: Int = 0,
    val closedLoansCount: Int = 0,
    val totalDisbursed: Double = 0.0,
    val totalOutstanding: Double = 0.0,
    val totalRecovered: Double = 0.0,
    val todayCollection: Double = 0.0,
    val monthCollection: Double = 0.0,
    val totalNpaCount: Int = 0,
    val totalNpaAmount: Double = 0.0,
    val sma0Count: Int = 0,
    val sma1Count: Int = 0,
    val sma2Count: Int = 0
)

class CoopViewModel(
    private val repository: CoopRepository,
    private val someeApiClient: SomeeApiClient
) : ViewModel() {

    // Server & Somee.com Connection State
    private val _serverConfig = MutableStateFlow(someeApiClient.getConfig())
    val serverConfig = _serverConfig.asStateFlow()

    private val _connectionStatus = MutableStateFlow<ConnectionTestResult?>(null)
    val connectionStatus = _connectionStatus.asStateFlow()

    private val _isTestingConnection = MutableStateFlow(false)
    val isTestingConnection = _isTestingConnection.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    private val _lastSyncResult = MutableStateFlow<SyncResult?>(null)
    val lastSyncResult = _lastSyncResult.asStateFlow()

    fun updateServerConfig(config: ServerConnectionConfig) {
        _serverConfig.value = config
        someeApiClient.saveConfig(config)
    }

    fun testConnection(url: String, apiKey: String) {
        viewModelScope.launch {
            _isTestingConnection.value = true
            val result = someeApiClient.testConnection(url, apiKey)
            _connectionStatus.value = result
            _isTestingConnection.value = false
        }
    }

    fun syncWithSomeeServer() {
        viewModelScope.launch {
            _isSyncing.value = true
            val result = someeApiClient.syncWithServer(
                members = members.value,
                loans = loans.value,
                repayments = repayments.value
            )
            _lastSyncResult.value = result
            if (result.isSuccess) {
                val now = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
                val updated = _serverConfig.value.copy(lastSyncTime = now)
                _serverConfig.value = updated
                someeApiClient.updateLastSyncTime(now)
            }
            _isSyncing.value = false
        }
    }

    // Current User Profile & Login Session
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _currentUser = MutableStateFlow(
        UserProfile("D. P. Solanki (Branch Manager)", "Admin / Manager", "9999900001")
    )
    val currentUser = _currentUser.asStateFlow()

    fun loginUser(mobile: String, role: String) {
        val userName = when {
            role.contains("Admin") -> "D. P. Solanki (Branch Manager)"
            role.contains("Recovery") -> "H. M. Jadeja (Recovery Exec)"
            else -> {
                val matchedMember = members.value.find { it.mobile == mobile }
                matchedMember?.let { "${it.fullName} (${it.memberNo})" } ?: "Ramesh Patel (Member SK-0001)"
            }
        }
        _currentUser.value = UserProfile(userName, role, mobile)
        _isLoggedIn.value = true
    }

    fun logout() {
        _isLoggedIn.value = false
    }

    fun switchUserRole(role: String) {
        when (role) {
            "Admin / Manager" -> _currentUser.value = UserProfile("D. P. Solanki (Branch Manager)", "Admin / Manager", "9999900001")
            "Recovery Officer" -> _currentUser.value = UserProfile("H. M. Jadeja (Recovery Exec)", "Recovery Officer", "9824099881")
            "Member" -> _currentUser.value = UserProfile("Ramesh Patel (Member SK-0001)", "Member", "9999900001")
        }
    }

    val members: StateFlow<List<MemberEntity>> = repository.allMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val loans: StateFlow<List<LoanEntity>> = repository.allLoans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val repayments: StateFlow<List<RepaymentEntity>> = repository.allRepayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val followUps: StateFlow<List<FollowUpEntity>> = repository.allFollowUps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ptps: StateFlow<List<PtpEntity>> = repository.allPtps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val demandNotices: StateFlow<List<DemandNoticeEntity>> = repository.allDemandNotices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val section101Cases: StateFlow<List<Section101CaseEntity>> = repository.allSection101Cases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard Metrics
    val dashboardSummary: StateFlow<DashboardSummary> = combine(
        members,
        loans,
        repayments
    ) { memberList, loanList, repaymentList ->
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(Date())
        val monthPrefix = todayStr.substring(0, 7) // "yyyy-MM"

        val activeMembers = memberList.count { it.isActive && it.approvalStatus == "Approved" }
        val pendingMembers = memberList.count { it.approvalStatus == "Pending" }

        val activeLoans = loanList.filter { it.status == "Active" }
        val overdueLoans = loanList.filter { it.status == "Overdue" }
        val closedLoans = loanList.filter { it.status == "Closed" }

        val totalDisbursed = loanList.sumOf { it.principalAmount }
        val totalOutstanding = loanList.sumOf { it.outstandingBalance }
        val totalRecovered = loanList.sumOf { it.totalPaid }

        val todayCollection = repaymentList.filter { it.paymentDate == todayStr }.sumOf { it.amountPaid }
        val monthCollection = repaymentList.filter { it.paymentDate.startsWith(monthPrefix) }.sumOf { it.amountPaid }

        // Overdue classification
        val npaLoans = overdueLoans.filter { it.daysOverdue > 90 }
        val sma0Loans = overdueLoans.filter { it.daysOverdue in 1..30 }
        val sma1Loans = overdueLoans.filter { it.daysOverdue in 31..60 }
        val sma2Loans = overdueLoans.filter { it.daysOverdue in 61..90 }

        DashboardSummary(
            totalMembers = memberList.size,
            activeMembers = activeMembers,
            pendingMembers = pendingMembers,
            totalLoansCount = loanList.size,
            activeLoansCount = activeLoans.size,
            overdueLoansCount = overdueLoans.size,
            closedLoansCount = closedLoans.size,
            totalDisbursed = totalDisbursed,
            totalOutstanding = totalOutstanding,
            totalRecovered = totalRecovered,
            todayCollection = todayCollection,
            monthCollection = monthCollection,
            totalNpaCount = npaLoans.size,
            totalNpaAmount = npaLoans.sumOf { it.outstandingBalance },
            sma0Count = sma0Loans.size,
            sma1Count = sma1Loans.size,
            sma2Count = sma2Loans.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())

    // Selected items for dialogs/sheets
    private val _selectedMember = MutableStateFlow<MemberEntity?>(null)
    val selectedMember = _selectedMember.asStateFlow()
    fun selectMember(member: MemberEntity?) { _selectedMember.value = member }

    private val _selectedLoan = MutableStateFlow<LoanEntity?>(null)
    val selectedLoan = _selectedLoan.asStateFlow()
    fun selectLoan(loan: LoanEntity?) { _selectedLoan.value = loan }

    private val _lastGeneratedReceipt = MutableStateFlow<RepaymentEntity?>(null)
    val lastGeneratedReceipt = _lastGeneratedReceipt.asStateFlow()
    fun clearLastReceipt() { _lastGeneratedReceipt.value = null }

    // Search & Filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    fun setSearchQuery(query: String) { _searchQuery.value = query }

    // --- Actions ---

    fun addMember(
        fullName: String,
        mobile: String,
        email: String,
        address: String,
        aadharNo: String,
        panNo: String,
        fatherSpouseName: String,
        gender: String,
        occupation: String,
        bankName: String,
        accountNumber: String,
        ifscCode: String,
        bankBranch: String
    ) {
        viewModelScope.launch {
            val count = members.value.size + 1
            val nextMemberNo = "SK-%04d".format(count)
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val member = MemberEntity(
                memberNo = nextMemberNo,
                fullName = fullName,
                mobile = mobile,
                email = email,
                address = address,
                joinDate = today,
                isActive = true,
                aadharNo = aadharNo,
                panNo = panNo,
                fatherSpouseName = fatherSpouseName,
                gender = gender,
                occupation = occupation,
                bankName = bankName,
                accountNumber = accountNumber,
                ifscCode = ifscCode,
                bankBranch = bankBranch,
                approvalStatus = "Pending", // Needs Admin approval
                roleId = 2
            )
            repository.addMember(member)
        }
    }

    fun approveMember(memberId: Int) {
        viewModelScope.launch {
            repository.updateMemberApproval(memberId, "Approved", _currentUser.value.name, null)
        }
    }

    fun rejectMember(memberId: Int, reason: String) {
        viewModelScope.launch {
            repository.updateMemberApproval(memberId, "Rejected", _currentUser.value.name, reason)
        }
    }

    fun calculateEmi(principal: Double, annualRate: Double, tenureMonths: Int): Double {
        if (principal <= 0 || tenureMonths <= 0) return 0.0
        val r = (annualRate / 100.0) / 12.0
        if (r <= 0) return principal / tenureMonths
        val emi = (principal * r * (1 + r).pow(tenureMonths.toDouble())) / ((1 + r).pow(tenureMonths.toDouble()) - 1)
        return (emi * 100.0).roundToInt() / 100.0
    }

    fun applyLoan(
        member: MemberEntity,
        loanType: String,
        principal: Double,
        interestRate: Double,
        tenureMonths: Int,
        remarks: String,
        guarantor1Name: String,
        guarantor1Mobile: String,
        guarantor2Name: String,
        guarantor2Mobile: String
    ) {
        viewModelScope.launch {
            val count = loans.value.size + 1
            val loanNo = "LN-2026-%03d".format(count)
            val emi = calculateEmi(principal, interestRate, tenureMonths)
            val totalAmount = emi * tenureMonths

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val disbursedDate = sdf.format(calendar.time)
            calendar.add(Calendar.MONTH, 1)
            val dueDate = sdf.format(calendar.time)

            val loan = LoanEntity(
                memberId = member.memberId,
                memberNo = member.memberNo,
                memberName = member.fullName,
                mobile = member.mobile,
                loanNo = loanNo,
                loanType = loanType,
                principalAmount = principal,
                interestRate = interestRate,
                tenureMonths = tenureMonths,
                emiAmount = emi,
                totalAmount = totalAmount,
                disbursedDate = disbursedDate,
                dueDate = dueDate,
                outstandingBalance = totalAmount,
                totalPaid = 0.0,
                status = "Active",
                daysOverdue = 0,
                remarks = remarks,
                guarantor1Name = guarantor1Name,
                guarantor1Mobile = guarantor1Mobile,
                guarantor2Name = guarantor2Name,
                guarantor2Mobile = guarantor2Mobile
            )
            repository.addLoan(loan)
        }
    }

    fun processDailyCollection(
        loan: LoanEntity,
        amount: Double,
        paymentMode: String,
        remarks: String
    ) {
        viewModelScope.launch {
            val collector = _currentUser.value.name
            val repayment = repository.recordRepayment(
                loan = loan,
                amountPaid = amount,
                paymentMode = paymentMode,
                collectedBy = collector,
                remarks = remarks
            )
            _lastGeneratedReceipt.value = repayment
        }
    }

    fun logRecoveryFollowUp(
        loan: LoanEntity,
        type: String,
        contactPerson: String,
        responseSummary: String,
        remarks: String
    ) {
        viewModelScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val followUp = FollowUpEntity(
                loanId = loan.loanId,
                memberId = loan.memberId,
                loanNo = loan.loanNo,
                memberName = loan.memberName,
                followUpDate = today,
                followUpType = type,
                contactPerson = contactPerson,
                staffOrAgent = _currentUser.value.name,
                responseSummary = responseSummary,
                remarks = remarks
            )
            repository.addFollowUp(followUp)
        }
    }

    fun recordPtp(
        loan: LoanEntity,
        amount: Double,
        date: String,
        remarks: String
    ) {
        viewModelScope.launch {
            val ptp = PtpEntity(
                loanId = loan.loanId,
                memberId = loan.memberId,
                loanNo = loan.loanNo,
                memberName = loan.memberName,
                mobile = loan.mobile,
                promisedAmount = amount,
                promisedDate = date,
                recordedBy = _currentUser.value.name,
                status = "Pending",
                remarks = remarks
            )
            repository.addPtp(ptp)
        }
    }

    fun updatePtpStatus(ptpId: Int, status: String) {
        viewModelScope.launch {
            repository.updatePtpStatus(ptpId, status)
        }
    }

    fun issueDemandNotice(
        loan: LoanEntity,
        noticeType: String,
        advocateName: String,
        penalRatePercent: Double = 2.0
    ) {
        viewModelScope.launch {
            val count = demandNotices.value.size + 1
            val noticeNo = "DN/2026/%03d".format(count)
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val penalInterest = loan.outstandingBalance * (penalRatePercent / 100.0)
            val overdueInterest = (loan.outstandingBalance * (loan.interestRate / 100.0)) / 12.0
            val noticeCharges = 150.0
            val totalDemanded = loan.outstandingBalance + overdueInterest + penalInterest + noticeCharges

            val member = repository.getMemberById(loan.memberId)

            val notice = DemandNoticeEntity(
                noticeNo = noticeNo,
                loanId = loan.loanId,
                loanNo = loan.loanNo,
                memberId = loan.memberId,
                memberName = loan.memberName,
                mobile = loan.mobile,
                address = member?.address ?: "Rajkot, Gujarat",
                noticeType = noticeType,
                noticeDate = today,
                outstandingPrincipal = loan.outstandingBalance,
                overdueInterest = (overdueInterest * 100).roundToInt() / 100.0,
                penalInterest = (penalInterest * 100).roundToInt() / 100.0,
                noticeCharges = noticeCharges,
                totalDemanded = (totalDemanded * 100).roundToInt() / 100.0,
                noticeStatus = "Issued",
                advocateName = advocateName,
                remarks = "Issued via Registered A.D. Post & Officer delivery"
            )
            repository.addDemandNotice(notice)
        }
    }

    fun fileSection101Case(
        loan: LoanEntity,
        authorityCourt: String,
        claimAmount: Double,
        remarks: String
    ) {
        viewModelScope.launch {
            val count = section101Cases.value.size + 1
            val caseNo = "ARB/101/2026/%03d".format(count)
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val caseEntity = Section101CaseEntity(
                loanId = loan.loanId,
                loanNo = loan.loanNo,
                memberId = loan.memberId,
                memberName = loan.memberName,
                mobile = loan.mobile,
                caseNo = caseNo,
                filingDate = today,
                authorityCourt = authorityCourt,
                claimAmount = claimAmount,
                hearingDate = null,
                caseStatus = "Filed",
                remarks = remarks
            )
            repository.addSection101Case(caseEntity)
        }
    }
}

class CoopViewModelFactory(
    private val repository: CoopRepository,
    private val someeApiClient: SomeeApiClient
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CoopViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CoopViewModel(repository, someeApiClient) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
