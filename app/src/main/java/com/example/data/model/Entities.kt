package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class MemberEntity(
    @PrimaryKey(autoGenerate = true) val memberId: Int = 0,
    val memberNo: String,
    val fullName: String,
    val mobile: String,
    val email: String = "",
    val address: String = "",
    val joinDate: String,
    val isActive: Boolean = true,
    // KYC & Identity Details
    val aadharNo: String = "",
    val panNo: String = "",
    val fatherSpouseName: String = "",
    val dob: String = "",
    val gender: String = "Male",
    val occupation: String = "Agriculture",
    // Bank Details
    val bankName: String = "",
    val accountNumber: String = "",
    val ifscCode: String = "",
    val bankBranch: String = "",
    // Approval Workflow
    val approvalStatus: String = "Approved", // Pending, Approved, Rejected
    val approvedAt: String? = null,
    val approvedBy: String? = null,
    val rejectReason: String? = null,
    val photoPath: String = "",
    val roleId: Int = 2 // 1: Admin, 2: Member, 3: Agent
)

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey(autoGenerate = true) val loanId: Int = 0,
    val memberId: Int,
    val memberNo: String,
    val memberName: String,
    val mobile: String,
    val loanNo: String,
    val loanType: String, // Personal Loan, Agriculture Loan, Gold Loan, Business Loan, Vehicle Loan, Emergency Loan
    val principalAmount: Double,
    val interestRate: Double, // annual %
    val tenureMonths: Int,
    val emiAmount: Double,
    val totalAmount: Double,
    val disbursedDate: String,
    val dueDate: String,
    val outstandingBalance: Double,
    val totalPaid: Double = 0.0,
    val status: String = "Active", // Active, Overdue, Closed
    val daysOverdue: Int = 0,
    val remarks: String = "",
    val guarantor1Name: String = "",
    val guarantor1Mobile: String = "",
    val guarantor2Name: String = "",
    val guarantor2Mobile: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "repayments")
data class RepaymentEntity(
    @PrimaryKey(autoGenerate = true) val repaymentId: Int = 0,
    val loanId: Int,
    val loanNo: String,
    val memberNo: String,
    val memberName: String,
    val mobile: String,
    val receiptNo: String,
    val paymentDate: String,
    val amountPaid: Double,
    val principalPaid: Double,
    val interestPaid: Double,
    val outstandingBalance: Double,
    val paymentMode: String = "Cash", // Cash, UPI, Cheque, Bank Transfer
    val collectedBy: String = "Counter Staff",
    val remarks: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "follow_ups")
data class FollowUpEntity(
    @PrimaryKey(autoGenerate = true) val followUpId: Int = 0,
    val loanId: Int,
    val memberId: Int,
    val loanNo: String,
    val memberName: String,
    val followUpDate: String,
    val followUpType: String, // Phone Call, Field Visit, Demand Notice, Office Meeting
    val contactPerson: String,
    val staffOrAgent: String,
    val responseSummary: String,
    val remarks: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "promise_to_pay")
data class PtpEntity(
    @PrimaryKey(autoGenerate = true) val ptpId: Int = 0,
    val loanId: Int,
    val memberId: Int,
    val loanNo: String,
    val memberName: String,
    val mobile: String,
    val promisedAmount: Double,
    val promisedDate: String,
    val recordedBy: String,
    val status: String = "Pending", // Pending, Kept, Broken
    val remarks: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "demand_notices")
data class DemandNoticeEntity(
    @PrimaryKey(autoGenerate = true) val noticeId: Int = 0,
    val noticeNo: String,
    val loanId: Int,
    val loanNo: String,
    val memberId: Int,
    val memberName: String,
    val mobile: String,
    val address: String,
    val noticeType: String, // Form-1 Reminder, Form-2 Final Notice, Section 101 Advocate Notice
    val noticeDate: String,
    val outstandingPrincipal: Double,
    val overdueInterest: Double,
    val penalInterest: Double,
    val noticeCharges: Double = 150.0,
    val totalDemanded: Double,
    val noticeStatus: String = "Issued", // Issued, Served, Acknowledged
    val advocateName: String = "S. M. Trivedi, Advocate",
    val remarks: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "section_101_cases")
data class Section101CaseEntity(
    @PrimaryKey(autoGenerate = true) val caseId: Int = 0,
    val loanId: Int,
    val loanNo: String,
    val memberId: Int,
    val memberName: String,
    val mobile: String,
    val caseNo: String,
    val filingDate: String,
    val authorityCourt: String = "Board of Nominees / Registrar Court",
    val claimAmount: Double,
    val hearingDate: String? = null,
    val caseStatus: String = "Filed", // Filed, Notice Issued, Hearing Scheduled, Certificate Issued, Executed
    val certificateNo: String = "",
    val certificateDate: String? = null,
    val remarks: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
