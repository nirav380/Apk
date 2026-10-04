package com.example.data.repository

import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class CoopRepository(
    private val memberDao: MemberDao,
    private val loanDao: LoanDao,
    private val repaymentDao: RepaymentDao,
    private val recoveryDao: RecoveryDao
) {
    val allMembers: Flow<List<MemberEntity>> = memberDao.getAllMembers()
    val allLoans: Flow<List<LoanEntity>> = loanDao.getAllLoans()
    val allRepayments: Flow<List<RepaymentEntity>> = repaymentDao.getAllRepayments()
    val allFollowUps: Flow<List<FollowUpEntity>> = recoveryDao.getAllFollowUps()
    val allPtps: Flow<List<PtpEntity>> = recoveryDao.getAllPtps()
    val allDemandNotices: Flow<List<DemandNoticeEntity>> = recoveryDao.getAllDemandNotices()
    val allSection101Cases: Flow<List<Section101CaseEntity>> = recoveryDao.getAllSection101Cases()

    suspend fun getMemberById(id: Int): MemberEntity? = memberDao.getMemberById(id)
    suspend fun getMemberByMobile(mobile: String): MemberEntity? = memberDao.getMemberByMobile(mobile)

    suspend fun addMember(member: MemberEntity): Long {
        return memberDao.insertMember(member)
    }

    suspend fun updateMember(member: MemberEntity) {
        memberDao.updateMember(member)
    }

    suspend fun updateMemberApproval(memberId: Int, status: String, approver: String, reason: String? = null) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())
        memberDao.updateApproval(memberId, status, today, approver, reason)
    }

    suspend fun addLoan(loan: LoanEntity): Long {
        return loanDao.insertLoan(loan)
    }

    suspend fun updateLoan(loan: LoanEntity) {
        loanDao.updateLoan(loan)
    }

    suspend fun recordRepayment(
        loan: LoanEntity,
        amountPaid: Double,
        paymentMode: String,
        collectedBy: String,
        remarks: String
    ): RepaymentEntity {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())
        val receiptNo = "RCPT-${SimpleDateFormat("yyyyMMdd-HHmm", Locale.getDefault()).format(Date())}"

        // Calculate interest portion and principal portion
        val monthlyInterest = (loan.outstandingBalance * (loan.interestRate / 100.0)) / 12.0
        val interestPaid = minOf(amountPaid, monthlyInterest)
        val principalPaid = maxOf(0.0, amountPaid - interestPaid)
        val newOutstanding = maxOf(0.0, loan.outstandingBalance - principalPaid)
        val newTotalPaid = loan.totalPaid + amountPaid
        val newStatus = if (newOutstanding <= 0.0) "Closed" else if (loan.daysOverdue > 0 && newOutstanding > 0) "Active" else loan.status

        val repayment = RepaymentEntity(
            loanId = loan.loanId,
            loanNo = loan.loanNo,
            memberNo = loan.memberNo,
            memberName = loan.memberName,
            mobile = loan.mobile,
            receiptNo = receiptNo,
            paymentDate = today,
            amountPaid = amountPaid,
            principalPaid = principalPaid,
            interestPaid = interestPaid,
            outstandingBalance = newOutstanding,
            paymentMode = paymentMode,
            collectedBy = collectedBy,
            remarks = remarks
        )

        repaymentDao.insertRepayment(repayment)
        loanDao.updateLoanBalance(loan.loanId, newOutstanding, newTotalPaid, newStatus)
        return repayment
    }

    suspend fun addFollowUp(followUp: FollowUpEntity): Long {
        return recoveryDao.insertFollowUp(followUp)
    }

    suspend fun addPtp(ptp: PtpEntity): Long {
        return recoveryDao.insertPtp(ptp)
    }

    suspend fun updatePtpStatus(ptpId: Int, status: String) {
        recoveryDao.updatePtpStatus(ptpId, status)
    }

    suspend fun addDemandNotice(notice: DemandNoticeEntity): Long {
        return recoveryDao.insertDemandNotice(notice)
    }

    suspend fun addSection101Case(caseItem: Section101CaseEntity): Long {
        return recoveryDao.insertSection101Case(caseItem)
    }

    suspend fun updateSection101Status(caseId: Int, status: String, nextHearing: String?) {
        recoveryDao.updateSection101Case(caseId, status, nextHearing)
    }
}
