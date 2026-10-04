package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {
    @Query("SELECT * FROM members ORDER BY memberId DESC")
    fun getAllMembers(): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE memberId = :memberId LIMIT 1")
    suspend fun getMemberById(memberId: Int): MemberEntity?

    @Query("SELECT * FROM members WHERE mobile = :mobile LIMIT 1")
    suspend fun getMemberByMobile(mobile: String): MemberEntity?

    @Query("SELECT * FROM members WHERE memberNo = :memberNo LIMIT 1")
    suspend fun getMemberByNo(memberNo: String): MemberEntity?

    @Query("SELECT COUNT(*) FROM members")
    fun getMemberCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MemberEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<MemberEntity>)

    @Update
    suspend fun updateMember(member: MemberEntity)

    @Query("UPDATE members SET approvalStatus = :status, approvedAt = :approvedAt, approvedBy = :approvedBy, rejectReason = :rejectReason WHERE memberId = :memberId")
    suspend fun updateApproval(memberId: Int, status: String, approvedAt: String?, approvedBy: String?, rejectReason: String?)

    @Delete
    suspend fun deleteMember(member: MemberEntity)
}

@Dao
interface LoanDao {
    @Query("SELECT * FROM loans ORDER BY loanId DESC")
    fun getAllLoans(): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE loanId = :loanId LIMIT 1")
    suspend fun getLoanById(loanId: Int): LoanEntity?

    @Query("SELECT * FROM loans WHERE memberId = :memberId ORDER BY loanId DESC")
    fun getLoansForMember(memberId: Int): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE status = :status ORDER BY loanId DESC")
    fun getLoansByStatus(status: String): Flow<List<LoanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: LoanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoans(loans: List<LoanEntity>)

    @Update
    suspend fun updateLoan(loan: LoanEntity)

    @Query("UPDATE loans SET outstandingBalance = :newBalance, totalPaid = :newPaid, status = :newStatus WHERE loanId = :loanId")
    suspend fun updateLoanBalance(loanId: Int, newBalance: Double, newPaid: Double, newStatus: String)

    @Delete
    suspend fun deleteLoan(loan: LoanEntity)
}

@Dao
interface RepaymentDao {
    @Query("SELECT * FROM repayments ORDER BY repaymentId DESC")
    fun getAllRepayments(): Flow<List<RepaymentEntity>>

    @Query("SELECT * FROM repayments WHERE loanId = :loanId ORDER BY repaymentId DESC")
    fun getRepaymentsForLoan(loanId: Int): Flow<List<RepaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepayment(repayment: RepaymentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepayments(repayments: List<RepaymentEntity>)
}

@Dao
interface RecoveryDao {
    @Query("SELECT * FROM follow_ups ORDER BY followUpId DESC")
    fun getAllFollowUps(): Flow<List<FollowUpEntity>>

    @Query("SELECT * FROM follow_ups WHERE loanId = :loanId ORDER BY followUpId DESC")
    fun getFollowUpsForLoan(loanId: Int): Flow<List<FollowUpEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowUp(followUp: FollowUpEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowUps(followUps: List<FollowUpEntity>)

    @Query("SELECT * FROM promise_to_pay ORDER BY ptpId DESC")
    fun getAllPtps(): Flow<List<PtpEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPtp(ptp: PtpEntity): Long

    @Query("UPDATE promise_to_pay SET status = :status WHERE ptpId = :ptpId")
    suspend fun updatePtpStatus(ptpId: Int, status: String)

    @Query("SELECT * FROM demand_notices ORDER BY noticeId DESC")
    fun getAllDemandNotices(): Flow<List<DemandNoticeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDemandNotice(notice: DemandNoticeEntity): Long

    @Query("SELECT * FROM section_101_cases ORDER BY caseId DESC")
    fun getAllSection101Cases(): Flow<List<Section101CaseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSection101Case(c: Section101CaseEntity): Long

    @Query("UPDATE section_101_cases SET caseStatus = :status, hearingDate = :hearingDate WHERE caseId = :caseId")
    suspend fun updateSection101Case(caseId: Int, status: String, hearingDate: String?)
}
